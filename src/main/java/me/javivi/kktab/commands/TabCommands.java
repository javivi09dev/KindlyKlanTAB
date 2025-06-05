package me.javivi.kktab.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.javivi.kktab.KindlyKlanTab;
import me.javivi.kktab.config.TabConfig;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TabCommands {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, 
                              CommandRegistryAccess registryAccess, 
                              CommandManager.RegistrationEnvironment environment) {
        
        dispatcher.register(CommandManager.literal("kktab")
            .requires(source -> source.hasPermissionLevel(3))
            .then(CommandManager.literal("reload")
                .executes(TabCommands::reloadConfig))
            .then(CommandManager.literal("update")
                .executes(TabCommands::updateTab))
            .then(CommandManager.literal("header")
                .then(CommandManager.argument("text", StringArgumentType.greedyString())
                    .executes(TabCommands::setHeader)))
            .then(CommandManager.literal("footer")
                .then(CommandManager.argument("text", StringArgumentType.greedyString())
                    .executes(TabCommands::setFooter)))
            .then(CommandManager.literal("toggle")
                .executes(TabCommands::toggleTab))
            .then(CommandManager.literal("info")
                .executes(TabCommands::showInfo)));
    }
    
    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        try {
            KindlyKlanTab.getConfigManager().reloadConfigs();
            KindlyKlanTab.getTabManager().reload();
            context.getSource().sendFeedback(() -> 
                Text.literal("§a✓ Configuración del TAB recargada correctamente"), false);
        } catch (Exception e) {
            context.getSource().sendFeedback(() -> 
                Text.literal("§c✗ Error recargando configuración: " + e.getMessage()), false);
            KindlyKlanTab.LOGGER.error("Error recargando configuración", e);
        }
        return 1;
    }
    
    private static int updateTab(CommandContext<ServerCommandSource> context) {
        try {
            KindlyKlanTab.getTabManager().updateTabList();
            context.getSource().sendFeedback(() -> 
                Text.literal("§a✓ TAB actualizado manualmente"), false);
        } catch (Exception e) {
            context.getSource().sendFeedback(() -> 
                Text.literal("§c✗ Error actualizando TAB: " + e.getMessage()), false);
        }
        return 1;
    }
    
    private static int setHeader(CommandContext<ServerCommandSource> context) {
        String header = StringArgumentType.getString(context, "text");
        
        TabConfig config = KindlyKlanTab.getConfigManager().getTabConfig();
        config.header = header;
        KindlyKlanTab.getConfigManager().saveTabConfig();
        KindlyKlanTab.getTabManager().updateTabList();
        
        context.getSource().sendFeedback(() -> 
            Text.literal("§a✓ Header del TAB actualizado"), false);
        return 1;
    }
    
    private static int setFooter(CommandContext<ServerCommandSource> context) {
        String footer = StringArgumentType.getString(context, "text");
        
        TabConfig config = KindlyKlanTab.getConfigManager().getTabConfig();
        config.footer = footer;
        KindlyKlanTab.getConfigManager().saveTabConfig();
        KindlyKlanTab.getTabManager().updateTabList();
        
        context.getSource().sendFeedback(() -> 
            Text.literal("§a✓ Footer del TAB actualizado"), false);
        return 1;
    }
    
    private static int toggleTab(CommandContext<ServerCommandSource> context) {
        TabConfig config = KindlyKlanTab.getConfigManager().getTabConfig();
        config.enabled = !config.enabled;
        KindlyKlanTab.getConfigManager().saveTabConfig();
        
        if (config.enabled) {
            KindlyKlanTab.getTabManager().reload();
            context.getSource().sendFeedback(() -> 
                Text.literal("§a✓ TAB personalizado activado"), false);
        } else {
            context.getSource().sendFeedback(() -> 
                Text.literal("§e⚠ TAB personalizado desactivado"), false);
        }
        return 1;
    }
    
    private static int showInfo(CommandContext<ServerCommandSource> context) {
        TabConfig config = KindlyKlanTab.getConfigManager().getTabConfig();
        
        context.getSource().sendFeedback(() -> Text.literal(
            "§6§l═══════════ KindlyKlanTAB Info ═══════════\n" +
            "§7Estado: " + (config.enabled ? "§aActivado" : "§cDesactivado") + "\n" +
            "§7Intervalo de actualización: §e" + config.updateInterval + " ticks\n" +
            "§7Nombres personalizados: " + (config.enableCustomNames ? "§aActivados" : "§cDesactivados") + "\n" +
            "§7Grupos configurados: §e" + config.groups.size() + "\n" +
            "§6§l════════════════════════════════════════"
        ), false);
        return 1;
    }
} 