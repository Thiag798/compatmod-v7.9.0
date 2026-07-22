
package com.example.compatmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LegacyTransformLogger {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void log(String msg) {
        LOGGER.info("[CompatMod Legacy] {}", msg);
    }
}
