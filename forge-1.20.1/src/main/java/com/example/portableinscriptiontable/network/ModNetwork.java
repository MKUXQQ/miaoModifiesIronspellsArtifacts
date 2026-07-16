package com.example.portableinscriptiontable.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModNetwork {
    private static final String VERSION = "1.6";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ModNetworkIds.MAIN_CHANNEL)
            .networkProtocolVersion(() -> VERSION)
            .clientAcceptedVersions(VERSION::equals)
            .serverAcceptedVersions(VERSION::equals)
            .simpleChannel();

    private static int nextId;

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(nextId++, OpenInscriptionTablePayload.class,
                OpenInscriptionTablePayload::write,
                OpenInscriptionTablePayload::new,
                OpenInscriptionTableHandler::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(nextId++, RequestSpellBalancePayload.class,
                RequestSpellBalancePayload::write,
                RequestSpellBalancePayload::new,
                RequestSpellBalancePayload::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(nextId++, SaveSpellBalancePayload.class,
                SaveSpellBalancePayload::write,
                SaveSpellBalancePayload::new,
                SaveSpellBalancePayload::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(nextId++, SyncSpellBalancePayload.class,
                SyncSpellBalancePayload::write,
                SyncSpellBalancePayload::new,
                SyncSpellBalancePayload::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToAll(Object message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }
}
