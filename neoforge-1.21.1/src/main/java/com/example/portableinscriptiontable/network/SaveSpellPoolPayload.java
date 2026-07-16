package com.example.portableinscriptiontable.network;

import com.example.portableinscriptiontable.PortableInscriptionTable;
import com.example.portableinscriptiontable.pool.SpellPoolRow;
import com.example.portableinscriptiontable.pool.SpellPoolStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class SaveSpellPoolPayload implements CustomPacketPayload {
    public static final Type<SaveSpellPoolPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PortableInscriptionTable.MOD_ID, "save_spell_pool")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SaveSpellPoolPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SaveSpellPoolPayload::write, SaveSpellPoolPayload::new);

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

    public static void handle(SaveSpellPoolPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            SpellPoolStore.replacePage(payload.page, payload.rows);
            PacketDistributor.sendToAllPlayers(new SyncSpellPoolPayload(payload.page, SpellPoolStore.snapshot(payload.page), false));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
