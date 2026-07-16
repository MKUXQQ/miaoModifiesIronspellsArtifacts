package com.example.portableinscriptiontable.client;

import com.example.portableinscriptiontable.pool.SpellPoolRow;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class SpellPoolClientScreenBridge {
    private SpellPoolClientScreenBridge() {
    }

    public static void handleSync(int page, List<SpellPoolRow> rows, boolean openScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (openScreen) {
            minecraft.setScreen(new SpellPoolScreen(page, rows));
        } else if (minecraft.screen instanceof SpellPoolScreen screen && screen.page() == page) {
            screen.replaceRows(rows);
        }
    }
}
