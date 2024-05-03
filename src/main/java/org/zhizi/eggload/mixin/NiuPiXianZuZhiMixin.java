package org.zhizi.eggload.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.SculkVeinBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zhizi.eggload.manager.RuleManager;

import java.util.Objects;

@Mixin(SculkVeinBlock.class)
public class NiuPiXianZuZhiMixin {

    private final RuleManager RuleManagerEx = new RuleManager();

    // 牛皮癣阻止蔓延
    @Inject(method = "spreadAtSamePosition", at = @At("HEAD"), cancellable = true)
    private void onSpreadAtSamePosition(WorldAccess world, BlockState state, BlockPos pos, Random random, CallbackInfo ci) {
        if(Objects.equals(RuleManagerEx.RuleModelRead("niupixianzuzhi"), "enable")){
            ci.cancel();
        }
    }

}
