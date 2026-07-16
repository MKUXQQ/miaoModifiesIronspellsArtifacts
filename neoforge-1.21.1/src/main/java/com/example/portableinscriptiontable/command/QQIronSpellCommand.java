package com.example.portableinscriptiontable.command;

import com.example.portableinscriptiontable.pool.RandomSpellBookFactory;
import com.example.portableinscriptiontable.pool.SpellPoolStore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.List;

public final class QQIronSpellCommand {
    private QQIronSpellCommand() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("QQironspell")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("slots", IntegerArgumentType.integer(1, 32))
                                        .executes(context -> give(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets"),
                                                IntegerArgumentType.getInteger(context, "slots")
                                        )))))
                .then(Commands.literal("giveAll")
                        .then(Commands.argument("slots", IntegerArgumentType.integer(1, 32))
                                .executes(context -> give(
                                        context.getSource(),
                                        context.getSource().getServer().getPlayerList().getPlayers(),
                                        IntegerArgumentType.getInteger(context, "slots")
                                )))));
    }

    private static int give(CommandSourceStack source, Collection<ServerPlayer> players, int slots) {
        List<ResourceLocation> pool = SpellPoolStore.enabledSpellIdsAcrossAllPages();
        if (pool.isEmpty()) {
            source.sendFailure(Component.translatable("commands.portable_inscription_table.qqironspell.empty_pool"));
            return 0;
        }
        int given = 0;
        for (ServerPlayer player : players) {
            ItemStack stack = RandomSpellBookFactory.create(source.getLevel(), slots, pool);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            given++;
        }
        int finalGiven = given;
        source.sendSuccess(() -> Component.translatable("commands.portable_inscription_table.qqironspell.success", finalGiven, RandomSpellBookFactory.FINAL_BOOK_NAME), true);
        return given;
    }
}
