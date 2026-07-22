package com.compatmod.command;

import com.compatmod.ForgeCompatMod;
import com.compatmod.compat.ModelTransformCache;
import com.compatmod.compat.RegistryMappingTable;
import com.compatmod.core.ConfigLoader;
import com.compatmod.core.HealthChecker;
import com.compatmod.core.HealthChecker.ServiceHealth;
import com.compatmod.mixin.MixinCompatManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Map;

import static net.minecraft.commands.Commands.literal;

public final class DebugCommand {

    private DebugCommand() { throw new UnsupportedOperationException("Utility class"); }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(literal("compatmod")
            .then(literal("status").executes(DebugCommand::status))
            .then(literal("health").executes(DebugCommand::health))
            .then(literal("cache").executes(DebugCommand::cache))
            .then(literal("mods").executes(DebugCommand::mods))
            .then(literal("config").executes(DebugCommand::config))
            .then(literal("reload").requires(s -> s.hasPermission(2)).executes(DebugCommand::reload))
            .then(literal("test").executes(DebugCommand::testAll))
        );
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        var s = ctx.getSource();
        s.sendSystemMessage(Component.literal("=== CompatMod v" + ForgeCompatMod.VERSION + " ===").withStyle(ChatFormatting.GOLD));
        s.sendSystemMessage(Component.literal("").append(Component.literal("Status: ").withStyle(ChatFormatting.GRAY)).append(Component.literal(HealthChecker.formatStatus()).withStyle(ChatFormatting.GREEN)));
        s.sendSystemMessage(Component.literal("").append(Component.literal("Cache: ").withStyle(ChatFormatting.GRAY)).append(Component.literal(ModelTransformCache.formatStats()).withStyle(ChatFormatting.AQUA)));
        s.sendSystemMessage(Component.literal("").append(Component.literal("Coremods: ").withStyle(ChatFormatting.GRAY)).append(Component.literal(MixinCompatManager.getDetectedCoremods() + " detected").withStyle(ChatFormatting.YELLOW)));
        s.sendSystemMessage(Component.literal("").append(Component.literal("Mappings: ").withStyle(ChatFormatting.GRAY)).append(Component.literal(RegistryMappingTable.size() + " entries").withStyle(ChatFormatting.AQUA)));
        return Command.SINGLE_SUCCESS;
    }

    private static int health(CommandContext<CommandSourceStack> ctx) {
        var s = ctx.getSource();
        Map<String, ServiceHealth> r = HealthChecker.checkAll();
        s.sendSystemMessage(Component.literal("=== Health Check ===").withStyle(ChatFormatting.GOLD));
        for (ServiceHealth sh : r.values()) {
            ChatFormatting c = switch (sh.status()) {
                case HEALTHY -> ChatFormatting.GREEN;
                case DEGRADED -> ChatFormatting.YELLOW;
                case DOWN -> ChatFormatting.RED;
            };
            s.sendSystemMessage(Component.literal("").append(Component.literal("  [" + sh.status().name() + "] ").withStyle(c)).append(Component.literal(sh.name() + ": " + sh.detail()).withStyle(ChatFormatting.GRAY)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int cache(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSystemMessage(Component.literal("Cache: " + ModelTransformCache.formatStats()).withStyle(ChatFormatting.AQUA));
        return Command.SINGLE_SUCCESS;
    }

    private static int mods(CommandContext<CommandSourceStack> ctx) {
        var s = ctx.getSource();
        s.sendSystemMessage(Component.literal("=== Coremod Detection ===").withStyle(ChatFormatting.GOLD));
        s.sendSystemMessage(Component.literal("  OptiFine, Sodium, Embeddium, Rubidium, Oculus, Iris, Canvas").withStyle(ChatFormatting.GRAY));
        s.sendSystemMessage(Component.literal("  Detected: " + MixinCompatManager.getDetectedCoremods()).withStyle(ChatFormatting.YELLOW));
        return Command.SINGLE_SUCCESS;
    }

    private static int config(CommandContext<CommandSourceStack> ctx) {
        var s = ctx.getSource();
        s.sendSystemMessage(Component.literal("=== Config ===").withStyle(ChatFormatting.GOLD));
        s.sendSystemMessage(Component.literal("  safe_mode: " + ConfigLoader.isSafeMode()).withStyle(ChatFormatting.YELLOW));
        s.sendSystemMessage(Component.literal("  debug_mode: " + ConfigLoader.isDebugMode()).withStyle(ChatFormatting.AQUA));
        s.sendSystemMessage(Component.literal("  cache_size: " + ConfigLoader.getPatchCacheSize()).withStyle(ChatFormatting.GRAY));
        s.sendSystemMessage(Component.literal("  max_depth: " + ConfigLoader.getMaxModelDepth()).withStyle(ChatFormatting.GRAY));
        s.sendSystemMessage(Component.literal("  blacklisted: " + ConfigLoader.getBlacklistedMods().size() + " mods").withStyle(ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ConfigLoader.reload();
        ModelTransformCache.clear();
        ctx.getSource().sendSystemMessage(Component.literal("[CompatMod] Config reloaded + cache cleared").withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private static int testAll(CommandContext<CommandSourceStack> ctx) {
        var s = ctx.getSource();
        s.sendSystemMessage(Component.literal("=== Registry Mapping Test ===").withStyle(ChatFormatting.GOLD));
        s.sendSystemMessage(Component.literal("  grass -> " + RegistryMappingTable.lookup("minecraft:grass").orElse("NOT FOUND")).withStyle(ChatFormatting.GRAY));
        s.sendSystemMessage(Component.literal("  entity_horse -> " + RegistryMappingTable.lookup("minecraft:entity_horse").orElse("NOT FOUND")).withStyle(ChatFormatting.GRAY));
        s.sendSystemMessage(Component.literal("  Total: " + RegistryMappingTable.size() + " mappings").withStyle(ChatFormatting.AQUA));
        return Command.SINGLE_SUCCESS;
    }
}
