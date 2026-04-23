package com.baldeagle.eaglenations.commands;

import com.baldeagle.eaglenations.Config;
import com.baldeagle.eaglenations.EagleNations;
import com.baldeagle.eaglenations.nation.Nation;
import com.baldeagle.eaglenations.politics.Law;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class NationCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            net.minecraft.commands.Commands.literal("nation")
            .requires(source -> source.getEntity() instanceof Player)
            .then(net.minecraft.commands.Commands.literal("info").executes(ctx -> info(ctx)))
            .then(net.minecraft.commands.Commands.literal("list").executes(ctx -> listNations(ctx)))
            .then(net.minecraft.commands.Commands.literal("claim").executes(ctx -> claim(ctx)))
            .then(net.minecraft.commands.Commands.literal("members").executes(ctx -> members(ctx)))
            .then(net.minecraft.commands.Commands.literal("laws").executes(ctx -> laws(ctx)))
            .then(net.minecraft.commands.Commands.literal("diplomacy")
                .then(net.minecraft.commands.Commands.argument("target", EntityArgument.player())
                    .then(net.minecraft.commands.Commands.literal("ally").executes(ctx -> setAlly(ctx)))
                    .then(net.minecraft.commands.Commands.literal("hostile").executes(ctx -> setHostile(ctx)))
                    .then(net.minecraft.commands.Commands.literal("neutral").executes(ctx -> setNeutral(ctx))))));
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        
        if (EagleNations.getNationManager() == null) {
            source.sendFailure(Component.literal("Nation system not available"));
            return 0;
        }
        
        var bridge = EagleNations.getFTBTeamBridge();
        if (bridge == null) {
            source.sendFailure(Component.literal("Use /ftbteams info"));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("Use /ftbteams info to view your nation"), true);
        return 1;
    }

    private static int listNations(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        
        if (EagleNations.getNationManager() == null) {
            source.sendFailure(Component.literal("Nation system not available"));
            return 0;
        }
        
        var nations = EagleNations.getNationManager().getAllNations();
        
        source.sendSuccess(() -> 
            Component.literal("Nations (" + nations.size() + "):"), true);
        
        for (var nation : nations) {
            source.sendSuccess(() -> 
                Component.literal("  - " + nation.getDisplayName()), false);
        }
        
        return nations.size();
    }

    private static int claim(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        
        if (!Config.ENABLE_TERRITORY_SYSTEM.get()) {
            source.sendFailure(Component.literal("Territory system is disabled"));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("Claim the chunk with /ftbchunks claim"), true);
        return 1;
    }

    private static int members(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        
        source.sendSuccess(() -> Component.literal("Use /ftbteams info to see members"), true);
        return 1;
    }

    private static int laws(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        
        source.sendSuccess(() -> Component.literal("=== Laws ==="), true);
        
        for (Law.LawType type : Law.LawType.values()) {
            source.sendSuccess(() -> 
                Component.literal(type.getName() + ": " + type.getDescription()), false);
        }
        
        return Law.LawType.values().length;
    }

    private static int setAlly(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("Use /ftbteams ally to create alliance"), true);
        return 1;
    }

    private static int setHostile(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("Relations managed through FTB Teams"), true);
        return 1;
    }

    private static int setNeutral(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("Relations managed through FTB Teams"), true);
        return 1;
    }
}