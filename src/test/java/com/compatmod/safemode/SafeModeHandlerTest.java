package com.compatmod.safemode;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafeModeHandlerTest {

    @Test
    void testHandlerClassExists() {
        assertNotNull(SafeModeHandler.class);
    }

    @Test
    void testIsOperationalMethodExists() throws NoSuchMethodException {
        SafeModeHandler.class.getDeclaredMethod("isOperational");
    }

    @Test
    void testOnClientSetupAnnotation() throws NoSuchMethodException {
        var method = SafeModeHandler.class.getDeclaredMethod("onClientSetup",
            net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent.class);
        assertNotNull(method.getAnnotation(net.minecraftforge.eventbus.api.SubscribeEvent.class));
    }
}
