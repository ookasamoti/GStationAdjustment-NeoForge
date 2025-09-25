// src/main/java/net/ookasamoti/gstationmod/command/LunchboxCommands.java
package net.ookasamoti.gstationmod.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.ookasamoti.gstationmod.lunchbox.LunchboxUnlocks;
import net.ookasamoti.gstationmod.lunchbox.net.LunchboxTierSync;

public final class LunchboxCommands {

    public static void register(RegisterCommandsEvent e) {
        e.getDispatcher().register(
                Commands.literal("gstation")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("lunchbox")
                                .then(Commands.literal("getTier")
                                        .executes(ctx -> {
                                            CommandSourceStack src = ctx.getSource();
                                            var level = src.getLevel();
                                            var data  = LunchboxUnlocks.get(level);
                                            int tier  = data.unlockedTier();

                                            src.sendSuccess(
                                                    () -> Component.literal("Current lunchbox unlock tier: T" + tier)
                                                            .withStyle(ChatFormatting.AQUA),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("setTier")
                                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 4))
                                                .executes(ctx -> {
                                                    CommandSourceStack src = ctx.getSource();
                                                    int tier   = IntegerArgumentType.getInteger(ctx, "tier");
                                                    var level  = src.getLevel();

                                                    var data = LunchboxUnlocks.get(level);
                                                    data.setTo(tier);

                                                    LunchboxTierSync.sendToAll(tier);

                                                    src.sendSuccess(
                                                            () -> Component.literal("Lunchbox upgrades set to T" + tier)
                                                                    .withStyle(ChatFormatting.GREEN),
                                                            true
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }

    private LunchboxCommands() {}
}
