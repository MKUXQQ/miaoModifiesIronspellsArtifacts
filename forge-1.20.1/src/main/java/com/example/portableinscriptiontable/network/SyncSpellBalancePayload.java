package com.example.portableinscriptiontable.network;

import com.example.portableinscriptiontable.balance.SpellBalanceRow;
import com.example.portableinscriptiontable.balance.SpellBalanceValues;
import com.example.portableinscriptiontable.client.SpellBalanceClientScreenBridge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncSpellBalancePayload {
    public final List<SpellBalanceRow> rows;
    public final boolean openScreen;

    public SyncSpellBalancePayload(List<SpellBalanceRow> rows, boolean openScreen) {
        this.rows = rows;
        this.openScreen = openScreen;
    }

    public SyncSpellBalancePayload(FriendlyByteBuf buf) {
        this.openScreen = buf.readBoolean();
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
        buf.writeBoolean(openScreen);
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

    public static void handle(SyncSpellBalancePayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> SpellBalanceClientScreenBridge.handleSync(payload.rows, payload.openScreen));
        context.setPacketHandled(true);
    }
}
