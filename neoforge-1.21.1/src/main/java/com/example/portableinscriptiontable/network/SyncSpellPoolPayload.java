package com.example.portableinscriptiontable.network;

import com.example.portableinscriptiontable.PortableInscriptionTable;
import com.example.portableinscriptiontable.client.SpellPoolClientScreenBridge;
import com.example.portableinscriptiontable.pool.SpellPoolRow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class SyncSpellPoolPayload implements CustomPacketPayload {
    public static final Type<SyncSpellPoolPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PortableInscriptionTable.MOD_ID, "sync_spell_pool")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSpellPoolPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SyncSpellPoolPayload::write, SyncSpellPoolPayload::new);

    public final int page;
    public final List<SpellPoolRow> rows;
    public final boolean openScreen;

    public SyncSpellPoolPayload(int page, List<SpellPoolRow> rows, boolean openScreen) {
        this.page = page;
        this.rows = rows;
        this.openScreen = openScreen;
    }

    public SyncSpellPoolPayload(FriendlyByteBuf buf) {
        this.page = buf.readVarInt();
        this.openScreen = buf.readBoolean();
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
        buf.writeBoolean(openScreen);
        buf.writeVarInt(rows.size());
        for (SpellPoolRow row : rows) {
            buf.writeResourceLocation(row.spellId());
            buf.writeUtf(row.displayName());
            buf.writeUtf(row.source());
            buf.writeUtf(row.castType());
            buf.writeBoolean(row.enabled());
        }
    }

    public static void handle(SyncSpellPoolPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> SpellPoolClientScreenBridge.handleSync(payload.page, payload.rows, payload.openScreen));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
