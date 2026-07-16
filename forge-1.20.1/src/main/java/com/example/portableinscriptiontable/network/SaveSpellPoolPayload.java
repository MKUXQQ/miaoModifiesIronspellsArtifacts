package com.example.portableinscriptiontable.network;

import com.example.portableinscriptiontable.pool.SpellPoolRow;
import com.example.portableinscriptiontable.pool.SpellPoolStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SaveSpellPoolPayload {
    public final int page;
    public final List<SpellPoolRow> rows;

    public SaveSpellPoolPayload(int page, List<SpellPoolRow> rows) {
        this.page = page;
        this.rows = rows;
    }

    public SaveSpellPoolPayload(FriendlyByteBuf buf) {
        this.page = buf.readVarInt();
        int size = buf.readVarInt();
        this.rows = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            String displayName = buf.readUtf();
            String source = buf.readUtf();
            String castType = buf.readUtf();
            boolean enabled = buf.readBoolean();
            rows.add(new SpellPoolRow(id, displayName, source, castType, enabled));
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(page);
        buf.writeVarInt(rows.size());
        for (SpellPoolRow row : rows) {
            buf.writeResourceLocation(row.spellId());
            buf.writeUtf(row.displayName());
            buf.writeUtf(row.source());
            buf.writeUtf(row.castType());
            buf.writeBoolean(row.enabled());
        }
    }

    public static void handle(SaveSpellPoolPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            SpellPoolStore.replacePage(payload.page, payload.rows);
            ModNetwork.sendToAll(new SyncSpellPoolPayload(payload.page, SpellPoolStore.snapshot(payload.page), false));
        });
        context.setPacketHandled(true);
    }
}
