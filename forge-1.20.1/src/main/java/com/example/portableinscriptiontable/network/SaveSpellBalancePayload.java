package com.example.portableinscriptiontable.network;

import com.example.portableinscriptiontable.balance.SpellBalanceRow;
import com.example.portableinscriptiontable.balance.SpellBalanceStore;
import com.example.portableinscriptiontable.balance.SpellBalanceValues;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SaveSpellBalancePayload {
    public final List<SpellBalanceRow> rows;

    public SaveSpellBalancePayload(List<SpellBalanceRow> rows) {
        this.rows = rows;
    }

    public SaveSpellBalancePayload(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.rows = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            String displayName = buf.readUtf();
            String source = buf.readUtf();
            String castType = buf.readUtf();
            SpellBalanceValues values = SpellBalanceValues.sanitize(
                    buf.readVarInt(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readBoolean(),
                    buf.readDouble()
            );
            rows.add(new SpellBalanceRow(id, displayName, source, castType, values));
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(rows.size());
        for (SpellBalanceRow row : rows) {
            buf.writeResourceLocation(row.spellId());
            buf.writeUtf(row.displayName());
            buf.writeUtf(row.source());
            buf.writeUtf(row.castType());
            buf.writeVarInt(row.values().castTimeTicks());
            buf.writeDouble(row.values().cooldownSeconds());
            buf.writeDouble(row.values().manaCostMultiplier());
            buf.writeDouble(row.values().powerMultiplier());
            buf.writeBoolean(row.values().survivalAllowed());
            buf.writeDouble(row.values().projectileSpeed());
        }
    }

    public static void handle(SaveSpellBalancePayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            SpellBalanceStore.replaceAll(payload.rows);
            ModNetwork.sendToAll(new SyncSpellBalancePayload(SpellBalanceStore.snapshot(), false));
        });
        context.setPacketHandled(true);
    }
}
