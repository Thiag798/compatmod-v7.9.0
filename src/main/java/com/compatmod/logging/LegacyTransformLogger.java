package com.compatmod.logging;

import com.compatmod.CompatMod;
import com.compatmod.config.ModConfig;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LegacyTransformLogger {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private static Path logFile;
    private static Thread writerThread;
    private static volatile boolean running = true;

    public static void init() {
        logFile = ModConfig.getConfigDir().resolve("compatmod-transforms.log");
        writerThread = new Thread(() -> {
            try (BufferedWriter w = Files.newBufferedWriter(logFile,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                w.write("=== CompatMod Transform Log started at " +
                    LocalDateTime.now().format(FMT) + " ===\n");
                w.flush();
                while (running || !queue.isEmpty()) {
                    String entry = queue.poll();
                    if (entry != null) {
                        w.write(entry);
                        w.newLine();
                        w.flush();
                    } else {
                        Thread.sleep(100);
                    }
                }
            } catch (IOException | InterruptedException e) {
                CompatMod.LOGGER.error("Transform logger error: {}", e.getMessage());
            }
        }, "CompatMod-TransformLogger");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    public static void log(ResourceLocation location, String patchName) {
        log(location.toString(), patchName);
    }

    // NEW (2026-07-30): String overload, same reasoning as
    // BlacklistConfig.isBlacklisted(String) above.
    public static void log(String locationString, String patchName) {
        if (!ModConfig.isLogEnabled()) return;
        String entry = String.format("[%s] %s -> %s",
            LocalDateTime.now().format(FMT), locationString, patchName);
        queue.offer(entry);
    }

    public static void shutdown() {
        running = false;
        if (writerThread != null) {
            try { writerThread.join(2000); } catch (InterruptedException ignored) {}
        }
    }
}
