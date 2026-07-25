package com.compatmod.command;

import com.compatmod.CompatMod;
import com.compatmod.config.BlacklistConfig;
import com.compatmod.config.ModConfig;
import com.compatmod.logging.LegacyTransformLogger;
import com.compatmod.patch.CompatRegistry;
import com.compatmod.cache.CacheInspector;
import com.compatmod.safemode.SafeModeHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CompatCommand {
    private static final String PREFIX = "compatmod.command.";

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        var root = Commands.literal("compatmod")
            .requires(s -> s.hasPermission(2))
            .then(Commands.literal("status")
                .executes(ctx -> {
                    int patches = CompatRegistry.getPatches().size();
                    int applied = CacheInspector.getPatchedCount();
                    // FIXED (2026-07-24): SafeModeHandler.isOperational() existed
                    // but nothing ever called it -- /compatmod status had no way
                    // to tell an admin the mod was silently inert (safe mode on,
                    // or the patch list came back empty after a bad reload).
                    String state = SafeModeHandler.isOperational()
                        ? "ACTIVE" : "INACTIVE (safe mode or no patches loaded)";
                    ctx.getSource().sendSuccess(() ->
                        Component.translatable(PREFIX + "status", state, patches, applied), false);
                    return 1;
                }))
            .then(Commands.literal("cache")
                .executes(ctx -> {
                    var snap = CacheInspector.snapshot();
                    ctx.getSource().sendSuccess(() ->
                        Component.translatable(PREFIX + "cache",
                            snap.cached(), snap.patched(),
                            snap.safeModeActive() ? "yes" : "no"), false);
                    return 1;
                }))
            .then(Commands.literal("reload")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() ->
                        Component.translatable(PREFIX + "reload"), true);
                    CompatRegistry.reload();
                    ctx.getSource().sendSuccess(() ->
                        Component.translatable(PREFIX + "reload.done",
                            CompatRegistry.getPatches().size()), true);
                    return 1;
                }))
            .then(Commands.literal("safemode")
                .executes(ctx -> {
                    boolean current = ModConfig.isSafeMode();
                    ModConfig.setSafeMode(!current);
                    String key = !current ? PREFIX + "safemode.enabled" : PREFIX + "safemode.disabled";
                    ctx.getSource().sendSuccess(() -> Component.translatable(key), true);
                    CompatMod.LOGGER.warn("Safe mode {}", !current ? "ENABLED" : "DISABLED");
                    return 1;
                }))
            .then(Commands.literal("blacklist")
                .then(Commands.literal("add")
                    .then(Commands.argument("model", StringArgumentType.string())
                        .executes(ctx -> {
                            String m = StringArgumentType.getString(ctx, "model");
                            BlacklistConfig.add(m);
                            ctx.getSource().sendSuccess(() ->
                                Component.translatable(PREFIX + "blacklist.add", m), true);
                            return 1;
                        })))
                .then(Commands.literal("remove")
                    .then(Commands.argument("model", StringArgumentType.string())
                        .executes(ctx -> {
                            String m = StringArgumentType.getString(ctx, "model");
                            BlacklistConfig.remove(m);
                            ctx.getSource().sendSuccess(() ->
                                Component.translatable(PREFIX + "blacklist.remove", m), true);
                            return 1;
                        })))
                .then(Commands.literal("list")
                    .executes(ctx -> {
                        var all = BlacklistConfig.getAll();
                        ctx.getSource().sendSuccess(() ->
                            Component.translatable(PREFIX + "blacklist.list",
                                all.size(), String.join(", ", all)), false);
                        return 1;
                    })))
            .then(Commands.literal("patches")
                .executes(ctx -> {
                    var names = CompatRegistry.getPatches().stream()
                        .map(p -> p.name()).toList();
                    ctx.getSource().sendSuccess(() ->
                        Component.translatable(PREFIX + "patches",
                            String.join(", ", names)), false);
                    return 1;
                }));
        d.register(root);
    }

    // FIXED (2026-07-24): LegacyTransformLogger.shutdown() existed but was
    // never wired to anything, so the background writer thread (and its
    // queue of pending log lines) never got a clean signal to stop -- it
    // just got killed when the JVM exited. This class is already registered
    // on MinecraftForge.EVENT_BUS (see CompatMod's constructor), so this is
    // the natural place to add the hook rather than registering a second
    // listener elsewhere.
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LegacyTransformLogger.shutdown();
    }
}
