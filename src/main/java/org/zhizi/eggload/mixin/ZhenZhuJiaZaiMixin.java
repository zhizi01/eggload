package org.zhizi.eggload.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zhizi.eggload.manager.RuleManager;

@Mixin(EnderPearlEntity.class)
public abstract class ZhenZhuJiaZaiMixin extends ThrownItemEntity {

    // 是否开启珍珠的加载
    private final RuleManager RuleManagerEx = new RuleManager();

    // 创建一个末影珍珠加载的类型
    private static final ChunkTicketType<ChunkPos> ENDER_PEARL_TICKET =
            ChunkTicketType.create("ender_pearl", Comparator.comparingLong(ChunkPos::toLong), 2);

    // 是否需要同步
    private boolean sync = true;
    // 真实位置
    private Vec3d realPos = null;
    // 真实速度
    private Vec3d realVelocity = null;

    // 构造函数
    protected ZhenZhuJiaZaiMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    // 检查实体是否在实体加载的区块中
    private static boolean isEntityTickingChunk(WorldChunk chunk) {
        return (
                chunk != null && chunk.getLevelType() == ChunkLevelType.ENTITY_TICKING
        );
    }

    // 获取最高的阻挡高度
    private static int getHighestMotionBlockingY(NbtCompound nbtCompound) {
        int highestY = Integer.MIN_VALUE;
        if (nbtCompound != null) {
            for (long element : nbtCompound.getCompound("Heightmaps").getLongArray("MOTION_BLOCKING")) {
                for (int i = 0; i < 7; i++) {
                    int y = (int)(element & 0b111111111) - 1;
                    if (y > highestY) highestY = y;
                    element = element >> 9;
                }
            }
        }
        return highestY;
    }

    // 在 tick 方法的头部注入代码
    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void skippyChunkLoading(CallbackInfo ci) {
        World world = this.getEntityWorld();


        // 如果世界是服务器世界并且开启了末影珍珠加载
        if (world instanceof ServerWorld && Objects.equals(RuleManagerEx.RuleModelRead("zhenzhujiazai"), "enable")) {

            Vec3d currPos = this.getPos().add(Vec3d.ZERO);
            Vec3d currVelocity = this.getVelocity().add(Vec3d.ZERO);

            if (this.sync) {
                this.realPos = currPos;
                this.realVelocity = currVelocity;
            }

            // 下一个位置
            Vec3d nextPos = this.realPos.add(this.realVelocity);
            Vec3d nextVelocity = this.realVelocity.multiply(0.99F).subtract(0, this.getGravity(), 0);

            // 当前区块位置和下一个区块位置
            ChunkPos currChunkPos = new ChunkPos(new BlockPos((int) currPos.x, (int) currPos.y, (int) currPos.z));
            ChunkPos nextChunkPos = new ChunkPos(new BlockPos((int) nextPos.x, (int) nextPos.y, (int) nextPos.z));

            // 区块加载
            ServerChunkManager serverChunkManager = ((ServerWorld) world).getChunkManager();
            if (!this.sync || !isEntityTickingChunk(serverChunkManager.getWorldChunk(nextChunkPos.x, nextChunkPos.z))) {
                int highestMotionBlockingY = Integer.MIN_VALUE;
                try {
                    highestMotionBlockingY = Integer.max(
                            getHighestMotionBlockingY(serverChunkManager.threadedAnvilChunkStorage.getNbt(currChunkPos).get().orElse(null)),
                            getHighestMotionBlockingY(serverChunkManager.threadedAnvilChunkStorage.getNbt(nextChunkPos).get().orElse(null)));
                } catch (InterruptedException | ExecutionException e) { throw new RuntimeException("NbtCompound exception"); }

                // 获取世界维度
                DimensionType worldDimension = world.getDimension();
                // 兼容非零最小 Y 值的维度
                highestMotionBlockingY += worldDimension.minY();

                // 跳过区块加载
                if (this.realPos.y > highestMotionBlockingY
                        && nextPos.y > highestMotionBlockingY
                        && nextPos.y + nextVelocity.y > highestMotionBlockingY) {
                    // 保持不动
                    serverChunkManager.addTicket(ENDER_PEARL_TICKET, currChunkPos, 2, currChunkPos);
                    this.setVelocity(Vec3d.ZERO);
                    this.setPosition(currPos);
                    this.sync = false;
                } else {
                    // 移动
                    serverChunkManager.addTicket(ENDER_PEARL_TICKET, nextChunkPos, 2, nextChunkPos);
                    this.setVelocity(this.realVelocity);
                    this.setPosition(this.realPos);
                    this.sync = true;
                }
            }

            // 更新真实位置和速度
            this.realPos = nextPos;
            this.realVelocity = nextVelocity;
        }
    }
}
