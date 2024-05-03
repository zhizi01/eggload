package org.zhizi.eggload;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.zhizi.eggload.manager.PlayerCuleManager;
import org.zhizi.eggload.manager.RuleManager;

import java.util.Objects;

public class Eggload implements ModInitializer {

    private static final String SERVER_NAME = "Eggload";
    private final RuleManager RuleManagerEx = new RuleManager();

    @Override
    public void onInitialize() {
        System.out.println("[Eggload] Mod is loaded.");
        System.out.println("[Eggload] by: Tomoko Aoyama  Github: https://github.com/zhizi01/mceggload");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("fls")
                .then(CommandManager.literal("eggload_rule")
                        .requires(source -> source.hasPermissionLevel(1)) // 仅允许OP进行配置
                        .then(CommandManager.literal("help")
                                .executes(this::RuleHelp))
                        .then(CommandManager.literal("zhenzhujiazai") // 珍珠加载
                                .then(CommandManager.literal("enable") // 添加开启规则的命令
                                        .executes(context -> RuleZhenZhuJiaZai(context, true)))
                                .then(CommandManager.literal("disable") // 添加关闭规则的命令
                                        .executes(context -> RuleZhenZhuJiaZai(context, false))))
                        .then(CommandManager.literal("gabengcuishencengyan") // 弱化深层岩
                                .then(CommandManager.literal("enable") // 添加开启规则的命令
                                        .executes(context -> RuleGaBengCuiShenCengYan(context, true)))
                                .then(CommandManager.literal("disable") // 添加关闭规则的命令
                                        .executes(context -> RuleGaBengCuiShenCengYan(context, false))))
                        .then(CommandManager.literal("niupixianzuzhi") // 阻止"牛皮癣"蔓延
                                .then(CommandManager.literal("enable") // 添加开启规则的命令
                                        .executes(context -> RuleNiuPiXianZuZhiManYan(context, true)))
                                .then(CommandManager.literal("disable") // 添加关闭规则的命令
                                        .executes(context -> RuleNiuPiXianZuZhiManYan(context, false))))
                )
                .then(CommandManager.literal("me_clue")
                        .requires(source -> source.hasPermissionLevel(0)) // 任何玩家都可以配置
                        .then(CommandManager.literal("help")
                                .executes(this::MeClueHelp)) // 提供帮助
                        .then(CommandManager.literal("join")
                                .then(CommandManager.argument("value", StringArgumentType.greedyString()) // 接受一个字符串参数
                                        .executes(context -> this.MeClueSetJoin(context, StringArgumentType.getString(context, "value")))))
                        .then(CommandManager.literal("quit")
                                .then(CommandManager.argument("value", StringArgumentType.greedyString()) // 接受一个字符串参数
                                        .executes(context -> this.MeClueSetQuit(context, StringArgumentType.getString(context, "value")))))
                )
        );
    }

    // === eggload_rule ====================================================================================

    // 帮助命令
    private int RuleHelp(CommandContext<ServerCommandSource> context){
        // 获取玩家Source
        ServerCommandSource PlayerSource = context.getSource();
        Text HelpLinMessage = Text.literal("")
                .append(Text.literal("[" + SERVER_NAME + "] 插件规则 - 帮助菜单:\n").formatted(Formatting.BLUE,Formatting.BOLD))
                .append(Text.literal("[" + SERVER_NAME + "] /fls eggload_rule help 帮助列表 \n").formatted(Formatting.WHITE))
                .append(Text.literal("[" + SERVER_NAME + "] /fls eggload_rule zhenzhujiazai <enable/disable> 珍珠加载规则 \n").formatted(Formatting.WHITE))
                .append(Text.literal("[" + SERVER_NAME + "] /fls eggload_rule gabengcuishencengyan <enable/disable> 弱化深层岩规则 \n").formatted(Formatting.WHITE))
                .append(Text.literal("[" + SERVER_NAME + "] /fls eggload_rule niupixianzuzhi <enable/disable> 阻止幽匿脉络蔓延规则 \n").formatted(Formatting.WHITE))
                .append(Text.literal("\n"));

        PlayerSource.sendFeedback(() ->HelpLinMessage, false);
        return 1;
    }

    // 通用的配置更改方法
    private int changeRule(CommandContext<ServerCommandSource> context, String ruleName, boolean enable) {
        ServerCommandSource playerSource = context.getSource();
        String value = enable ? "enable" : "disable";

        if (RuleManagerEx.RuleModelWrite(ruleName, value)) {
            Text successMessage = buildFeedbackMessage(Formatting.GREEN, "配置变更成功！", ruleName + " 当前为 " + value);
            playerSource.sendFeedback(() ->successMessage, false);
            return 1; // 成功
        } else {
            Text failureMessage = buildFeedbackMessage(Formatting.DARK_RED, "配置变更失败！", ruleName + " 无法改变，详细错误请查看服务器日志！");
            playerSource.sendFeedback(() ->failureMessage, false);
            return 0; // 失败
        }
    }

    // 构建反馈消息
    private Text buildFeedbackMessage(Formatting titleColor, String titleText, String detailText) {
        return Text.literal("")
                .append(Text.literal("[" + SERVER_NAME + "] " + titleText + "\n").formatted(titleColor, Formatting.BOLD))
                .append(Text.literal("[" + SERVER_NAME + "] " + detailText + " \n").formatted(Formatting.AQUA))
                .append(Text.literal("\n"));
    }

    // 对外暴露的方法
    private int RuleZhenZhuJiaZai(CommandContext<ServerCommandSource> context, boolean enable) {
        return changeRule(context, "zhenzhujiazai", enable);
    }

    private int RuleGaBengCuiShenCengYan(CommandContext<ServerCommandSource> context, boolean enable) {
        return changeRule(context, "gabengcuishencengyan", enable);
    }

    private int RuleNiuPiXianZuZhiManYan(CommandContext<ServerCommandSource> context, boolean enable) {
        return changeRule(context, "niupixianzuzhi", enable);
    }



    // === me_clue ====================================================================================

    private final PlayerCuleManager PlayerCuleManagerEx = new PlayerCuleManager();
    private static final int MAX_LENGTH = 64;
    private static final int MIN_LENGTH = 4;

    private int MeClueHelp(CommandContext<ServerCommandSource> context){
        // 获取玩家Source
        ServerCommandSource PlayerSource = context.getSource();
        Text HelpLinMessage = Text.literal("")
                .append(Text.literal("[" + SERVER_NAME + "] 个人提示语 - 帮助菜单:\n").formatted(Formatting.BLUE,Formatting.BOLD))
                .append(Text.literal("[" + SERVER_NAME + "] /fls me_clue help 帮助列表 \n").formatted(Formatting.WHITE))
                .append(Text.literal("[" + SERVER_NAME + "] /fls me_clue join <值> 设置自己加入服务器时的提示语句 \n").formatted(Formatting.WHITE))
                .append(Text.literal("[" + SERVER_NAME + "] /fls me_clue quit <值> 设置自己离开服务器时的提示语句 \n").formatted(Formatting.WHITE));
        PlayerSource.sendFeedback(() ->HelpLinMessage, false);
        return 1;
    }

    private int setPlayerMessage(ServerCommandSource source, String uuid, String value, String actionType) {
        if (uuid.isEmpty()) {
            sendFeedback(source, "读取你的UUID失败，请重新登录服务器！");
            return 0;
        }
        if (value.length() > MAX_LENGTH) {
            sendFeedback(source, "最大不允许超过" + MAX_LENGTH + "个字符，你想设置的有点长了！");
            return 0;
        }
        if (value.length() < MIN_LENGTH) {
            sendFeedback(source, "最小不允许低于" + MIN_LENGTH + "个字符，你想设置的有点短了！");
            return 0;
        }
        if (!PlayerCuleManagerEx.ClueModelWrite(uuid, actionType, value)) {
            sendFeedback(source, "更新你的配置失败，错误已经记录，请稍后再试试吧！");
            return 0;
        }
        sendFeedback(source, "你的" + (actionType.equals("join") ? "加入" : "离开") + "语已变更！");
        return 1;
    }

    private void sendFeedback(ServerCommandSource source, String message) {
        Text feedbackMessage = Text.literal("[" + SERVER_NAME + "] " + message).formatted(Formatting.RED, Formatting.BOLD);
        source.sendFeedback(() -> feedbackMessage, false);
    }

    public int MeClueSetJoin(CommandContext<ServerCommandSource> context, String setValue) {
        ServerCommandSource playerSource = context.getSource();
        String playerUUID = String.valueOf(playerSource.getPlayer().getUuid());
        return setPlayerMessage(playerSource, playerUUID, setValue, "join");
    }

    public int MeClueSetQuit(CommandContext<ServerCommandSource> context, String setValue) {
        ServerCommandSource playerSource = context.getSource();
        String playerUUID = String.valueOf(playerSource.getPlayer().getUuid());
        return setPlayerMessage(playerSource, playerUUID, setValue, "quit");
    }


}
