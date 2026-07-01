package com.example.portableinscriptiontable.client;

import com.example.portableinscriptiontable.PortableInscriptionTable;
import com.example.portableinscriptiontable.network.OpenInscriptionTablePayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PortableInscriptionClientEvents {
    private PortableInscriptionClientEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PortableInscriptionKeyMappings::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(PortableInscriptionClientEvents::onClientTick);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (PortableInscriptionKeyMappings.OPEN_INSCRIPTION_TABLE.consumeClick()) {
            PacketDistributor.sendToServer(new OpenInscriptionTablePayload());
        }
    }
}
