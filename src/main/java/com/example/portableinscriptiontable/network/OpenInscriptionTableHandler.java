package com.example.portableinscriptiontable.network;

import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class OpenInscriptionTableHandler {
    private OpenInscriptionTableHandler() {
    }

    public static void handle(OpenInscriptionTablePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> context.player().openMenu(new SimpleMenuProvider(
                (containerId, inventory, player) -> new InscriptionTableMenu(
                        containerId,
                        inventory,
                        ContainerLevelAccess.NULL
                ),
                Component.translatable("block.irons_spellbooks.inscription_table")
        )));
    }
}
