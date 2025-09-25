package net.ookasamoti.gstationmod.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.ookasamoti.gstationmod.config.FoodOverrideConfig;

import java.util.ArrayList;
import java.util.List;

public final class FoodOverrideDebugCommands {

    private static final int PAGE_SIZE = 10;

    public static void register(RegisterCommandsEvent e) {
        e.getDispatcher().register(
                Commands.literal("gstation")
                        .then(Commands.literal("food")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.literal("listKeys")
                                        // /gstation food listKeys
                                        .executes(ctx -> showPage(ctx.getSource(), 1))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(ctx -> {
                                                    int page = IntegerArgumentType.getInteger(ctx, "page");
                                                    return showPage(ctx.getSource(), page);
                                                })
                                        )
                                )
                        )
        );
    }

    private static int showPage(CommandSourceStack src, int page) {
        List<String> all = new ArrayList<>(FoodOverrideConfig.allKeys());
        int total = all.size();
        if (total == 0) {
            src.sendSuccess(() -> Component.literal("[FoodOverrides] key undefined")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }

        int maxPage = Math.max(1, (int)Math.ceil(total / (double)PAGE_SIZE));
        int p = Math.min(Math.max(1, page), maxPage);
        int from = (p - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);

        src.sendSuccess(() -> Component.literal(
                        String.format("[FoodOverrides] %d （Page %d / %d）", total, p, maxPage))
                .withStyle(ChatFormatting.GOLD), false);

        for (int i = from; i < to; i++) {
            String key = all.get(i);
            src.sendSuccess(() -> Component.literal(String.format(" - %s", key))
                    .withStyle(key.startsWith("#") ? ChatFormatting.AQUA : ChatFormatting.WHITE), false);
        }

        if (p < maxPage) {
            src.sendSuccess(() -> Component.literal(
                            String.format("Next Page: /gstation food listKeys %d", p + 1))
                    .withStyle(ChatFormatting.GRAY), false);
        }
        return 1;
    }

    private FoodOverrideDebugCommands() {}
}
