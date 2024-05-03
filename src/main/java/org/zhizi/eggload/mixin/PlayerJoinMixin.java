package org.zhizi.eggload.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zhizi.eggload.manager.PlayerCuleManager;

@Mixin(PlayerManager.class)
public class PlayerJoinMixin {

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {

        PlayerCuleManager playerCuleManagerEx = new PlayerCuleManager();
        String playerUUID = String.valueOf(player.getUuid());
        String defaultQuitClue = "加入了服务器，他刚掘完凤梨！";
        String quitClue = playerCuleManagerEx.ClueModelRead(playerUUID, "join");
        // 如果从数据库中读取的离开信息为空，使用默认离开信息
        if (quitClue.isEmpty()) {
            quitClue = defaultQuitClue;
        }

        MinecraftServer server = player.getServer();

        // 玩家名称使用橙色加粗，离开语使用红色加粗
        MutableText playerName = Text.literal("[" + player.getName().getString() + "] ")
                .formatted(Formatting.GOLD, Formatting.BOLD);
        MutableText leaveMessage = Text.literal(quitClue)
                .formatted(Formatting.GREEN, Formatting.BOLD);
        // 将玩家名称和离开语合并为一条消息
        MutableText finalMessage = playerName.append(leaveMessage);
        server.getPlayerManager().broadcast(finalMessage, false);
    }

}
