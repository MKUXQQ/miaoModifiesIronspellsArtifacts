package com.example.portableinscriptiontable.client;

import com.example.portableinscriptiontable.PortableInscriptionTable;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class PortableInscriptionKeyMappings {
    public static final String CATEGORY = "key.categories.portable_inscription_table";

    public static final KeyMapping OPEN_INSCRIPTION_TABLE = new KeyMapping(
            "key.portable_inscription_table.open_inscription_table",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    private PortableInscriptionKeyMappings() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_INSCRIPTION_TABLE);
    }
}
