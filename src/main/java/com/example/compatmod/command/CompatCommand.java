
package com.example.compatmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CompatCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("compatmod")
            .executes(ctx -> {
                System.out.println("CompatMod v7.8.0");
                return 1;
            }));
    }
}
