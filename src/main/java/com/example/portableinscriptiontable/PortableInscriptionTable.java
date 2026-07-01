package com.example.portableinscriptiontable;

import com.example.portableinscriptiontable.client.PortableInscriptionClientEvents;
import com.example.portableinscriptiontable.network.OpenInscriptionTableHandler;
import com.example.portableinscriptiontable.network.OpenInscriptionTablePayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(PortableInscriptionTable.MOD_ID)
public class PortableInscriptionTable {
    public static final String MOD_ID = "portable_inscription_table";

    public PortableInscriptionTable(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PortableInscriptionClientEvents.register(modEventBus);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID).versioned("1.0.0");
        registrar.playToServer(
                OpenInscriptionTablePayload.TYPE,
                OpenInscriptionTablePayload.STREAM_CODEC,
                OpenInscriptionTableHandler::handle
        );
    }
}
