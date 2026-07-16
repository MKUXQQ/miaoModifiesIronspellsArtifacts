package com.example.portableinscriptiontable;

import com.example.portableinscriptiontable.client.PortableInscriptionClientEvents;
import com.example.portableinscriptiontable.balance.SpellProjectileBalanceEvents;
import com.example.portableinscriptiontable.balance.SpellBalanceStore;
import com.example.portableinscriptiontable.network.OpenInscriptionTableHandler;
import com.example.portableinscriptiontable.network.OpenInscriptionTablePayload;
import com.example.portableinscriptiontable.network.RequestSpellBalancePayload;
import com.example.portableinscriptiontable.network.SaveSpellBalancePayload;
import com.example.portableinscriptiontable.network.SyncSpellBalancePayload;
import com.example.portableinscriptiontable.registry.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(PortableInscriptionTable.MOD_ID)
public class PortableInscriptionTable {
    public static final String MOD_ID = "portable_inscription_table";

    public PortableInscriptionTable(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(SpellProjectileBalanceEvents::onSpellPreCast);
        NeoForge.EVENT_BUS.addListener(SpellProjectileBalanceEvents::onSpellCast);
        NeoForge.EVENT_BUS.addListener(SpellProjectileBalanceEvents::onEntityJoinLevel);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PortableInscriptionClientEvents.register(modEventBus);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID).versioned("1.6");
        registrar.playToServer(
                OpenInscriptionTablePayload.TYPE,
                OpenInscriptionTablePayload.STREAM_CODEC,
                OpenInscriptionTableHandler::handle
        );
        registrar.playToServer(
                RequestSpellBalancePayload.TYPE,
                RequestSpellBalancePayload.STREAM_CODEC,
                RequestSpellBalancePayload::handle
        );
        registrar.playToServer(
                SaveSpellBalancePayload.TYPE,
                SaveSpellBalancePayload.STREAM_CODEC,
                SaveSpellBalancePayload::handle
        );
        registrar.playToClient(
                SyncSpellBalancePayload.TYPE,
                SyncSpellBalancePayload.STREAM_CODEC,
                SyncSpellBalancePayload::handle
        );
    }

    private void onServerStarted(ServerStartedEvent event) {
        SpellBalanceStore.loadAndApply();
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        SpellBalanceStore.loadAndApply();
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        SpellBalanceStore.loadAndApply();
    }
}
