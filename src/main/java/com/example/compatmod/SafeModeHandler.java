
package com.example.compatmod;

public class SafeModeHandler {
    public static void handle() {
        if (ModConfig.safeMode) {
            System.out.println("[CompatMod] Safe mode activated");
        }
    }
}
