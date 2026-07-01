package com.example.portableinscriptiontable.network;

import com.example.portableinscriptiontable.PortableInscriptionTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenInscriptionTablePayload() implements CustomPacketPayload {
    public static final Type<OpenInscriptionTablePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PortableInscriptionTable.MOD_ID, "open_inscription_table")
    );

    public static final StreamCodec<ByteBuf, OpenInscriptionTablePayload> STREAM_CODEC =
            StreamCodec.unit(new OpenInscriptionTablePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
