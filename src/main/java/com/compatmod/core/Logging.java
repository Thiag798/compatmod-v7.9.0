package com.compatmod.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class Logging {

    private static final Logger LOG = LogUtils.getLogger();

    public static final Marker TRANSFORM = MarkerFactory.getMarker("COMPAT_TRANSFORM");
    public static final Marker COMPAT    = MarkerFactory.getMarker("COMPAT_MAPPING");
    public static final Marker MIXIN     = MarkerFactory.getMarker("COMPAT_MIXIN");
    public static final Marker SECURITY  = MarkerFactory.getMarker("COMPAT_SECURITY");
    public static final Marker PERF      = MarkerFactory.getMarker("COMPAT_PERF");
    public static final Marker INIT      = MarkerFactory.getMarker("COMPAT_INIT");

    static {
        for (Marker m : new Marker[]{TRANSFORM, COMPAT, MIXIN, SECURITY, PERF, INIT}) {
            m.add(MarkerFactory.getMarker("COMPATMOD"));
        }
    }

    private Logging() { throw new UnsupportedOperationException("Utility class"); }

    public static void transformation(String msg, Object... args) { LOG.info(TRANSFORM, msg, args); }
    public static void compatibility(String msg, Object... args)  { LOG.info(COMPAT, msg, args); }
    public static void mixin(String msg, Object... args)          { LOG.debug(MIXIN, msg, args); }
    public static void securityIncident(String msg, Object... args) { LOG.warn(SECURITY, msg, args); }
    public static void performance(String msg, Object... args)     { LOG.info(PERF, msg, args); }
    public static void initialization(String msg, Object... args)  { LOG.info(INIT, msg, args); }
}
