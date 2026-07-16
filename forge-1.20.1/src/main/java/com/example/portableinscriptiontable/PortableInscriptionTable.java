package com.example.portableinscriptiontable;

import com.example.portableinscriptiontable.client.PortableInscriptionClientEvents;
import com.example.portableinscriptiontable.command.QQIronSpellCommand;
import com.example.portableinscriptiontable.balance.SpellBalanceStore;
import com.example.portableinscriptiontable.balance.SpellProjectileBalanceEvents;
import com.example.portableinscriptiontable.network.OpenInscriptionTableHandler;
import com.example.portableinscriptiontable.network.ModNetwork;
import com.example.portableinscriptiontable.registry.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(PortableInscriptionTable.MOD_ID)
public class PortableInscriptionTable {
    public static final String MOD_ID = "portable_inscription_table";

    public PortableInscriptionTable() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modEventBus);
        ModNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onDatapackSync);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(QQIronSpellCommand::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(SpellProjectileBalanceEvents::onSpellPreCast);
        MinecraftForge.EVENT_BUS.addListener(SpellProjectileBalanceEvents::onSpellCast);
        MinecraftForge.EVENT_BUS.addListener(SpellProjectileBalanceEvents::onEntityJoinLevel);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PortableInscriptionClientEvents.register(modEventBus);
        }
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
