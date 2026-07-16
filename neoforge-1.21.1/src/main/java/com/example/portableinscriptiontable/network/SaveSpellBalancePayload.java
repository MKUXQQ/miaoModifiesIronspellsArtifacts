package com.example.portableinscriptiontable.network;

import com.example.portableinscriptiontable.PortableInscriptionTable;
import com.example.portableinscriptiontable.balance.SpellBalanceRow;
import com.example.portableinscriptiontable.balance.SpellBalanceStore;
import com.example.portableinscriptiontable.balance.SpellBalanceValues;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class SaveSpellBalancePayload implements CustomPacketPayload {
    public static final Type<SaveSpellBalancePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PortableInscriptionTable.MOD_ID, "save_spell_balance")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SaveSpellBalancePayload> STREAM_CODEC =
            CustomPacketPayload.codec(SaveSpellBalancePayload::write, SaveSpellBalancePayload::new);

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

    public static void handle(SaveSpellBalancePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            SpellBalanceStore.replaceAll(payload.rows);
            PacketDistributor.sendToAllPlayers(new SyncSpellBalancePayload(SpellBalanceStore.snapshot(), false));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
