// src/main/java/net/ookasamoti/gstationmod/command/NutritionCommands.java
package net.ookasamoti.gstationmod.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.ookasamoti.gstationmod.common.PlayerNutritionData;
import net.ookasamoti.gstationmod.config.ServerConfig;
import net.ookasamoti.gstationmod.net.SatietySync;

import java.util.List;

public final class NutritionCommands {

    public static void register(RegisterCommandsEvent event) {
        var root = Commands.literal("gstation")
                .then(Commands.literal("nutrition")
                        .then(Commands.literal("show")
                                .executes(NutritionCommands::showSelf)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .requires(src -> src.hasPermission(2))
                                        .executes(ctx -> show(ctx, EntityArgument.getPlayer(ctx, "player")))
                                )
                        )
                        .then(Commands.literal("resetNutrition")
                                .requires(src -> src.hasPermission(2))
                                .executes(NutritionCommands::resetNutritionSelf)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> resetNutrition(ctx, EntityArgument.getPlayer(ctx, "player")))
                                )
                        )
                        .then(Commands.literal("resetSatiety")
                                .requires(src -> src.hasPermission(2))
                                .executes(NutritionCommands::resetSatietySelf)
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> resetSatiety(ctx, EntityArgument.getPlayer(ctx, "player")))
                                )
                        )
                );

        event.getDispatcher().register(root);
    }

    private static int showSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer self = ctx.getSource().getPlayerOrException();
            return show(ctx, self);
        } catch (Exception ex) {
            ctx.getSource().sendFailure(Component.literal("プレイヤーのみ実行できます。").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int show(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        int cumulative = PlayerNutritionData.getCumulative(target);
        int msIdx      = ServerConfig.milestoneIndex(cumulative);
        List<String> last5 = PlayerNutritionData.last5(target);

        var src = ctx.getSource();
        src.sendSuccess(() -> Component.literal("=== 栄養情報: " + target.getGameProfile().getName() + " ===")
                .withStyle(ChatFormatting.GOLD), false);
        src.sendSuccess(() -> Component.literal("累積栄養: " + cumulative)
                .withStyle(ChatFormatting.YELLOW), false);
        src.sendSuccess(() -> Component.literal("到達マイルストーン: " + msIdx + " / 5")
                .withStyle(ChatFormatting.AQUA), false);
        src.sendSuccess(() -> Component.literal("直近5食: " + (last5.isEmpty() ? "(なし)" : String.join(", ", last5)))
                .withStyle(ChatFormatting.GRAY), false);

        return 1;
    }

    private static int resetNutritionSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer self = ctx.getSource().getPlayerOrException();
            return resetNutrition(ctx, self);
        } catch (Exception ex) {
            ctx.getSource().sendFailure(Component.literal("プレイヤーのみ実行できます。").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int resetNutrition(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        PlayerNutritionData.setCumulative(target, 0);
        PlayerNutritionData.setHpBonusApplied(target, 0);
        PlayerNutritionData.applyHealthBonus(target, 0, 0f);

        ctx.getSource().sendSuccess(
                () -> Component.literal("栄養値をリセットしました: " + target.getGameProfile().getName())
                        .withStyle(ChatFormatting.GREEN),
                true
        );
        target.displayClientMessage(
                Component.literal("あなたの累積栄養はリセットされました。").withStyle(ChatFormatting.YELLOW),
                false
        );
        return 1;
    }

    private static int resetSatietySelf(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer self = ctx.getSource().getPlayerOrException();
            return resetSatiety(ctx, self);
        } catch (Exception ex) {
            ctx.getSource().sendFailure(Component.literal("プレイヤーのみ実行できます。").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int resetSatiety(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        PlayerNutritionData.clearAllSatiety(target);

        SatietySync.syncSatiety(target);

        ctx.getSource().sendSuccess(
                () -> Component.literal("飽食値を全てリセットしました: " + target.getGameProfile().getName())
                        .withStyle(ChatFormatting.AQUA),
                true
        );
        target.displayClientMessage(
                Component.literal("あなたの全ての飽食値がリセットされました。").withStyle(ChatFormatting.YELLOW),
                false
        );
        return 1;
    }

    private NutritionCommands() {}
}
