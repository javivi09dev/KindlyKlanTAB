package me.javivi.kktab.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.javivi.kktab.KindlyKlantab;
import me.javivi.kktab.config.AnnouncementConfig;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class AnnouncementCommands {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, 
                              CommandRegistryAccess registryAccess, 
                              CommandManager.RegistrationEnvironment environment) {
        
        dispatcher.register(CommandManager.literal("kkannounce")
            .requires(source -> source.hasPermissionLevel(3))
            .then(CommandManager.literal("send")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                    .executes(AnnouncementCommands::sendAnnouncement)))
            .then(CommandManager.literal("add")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                    .executes(AnnouncementCommands::addAnnouncement)))
            .then(CommandManager.literal("remove")
                .then(CommandManager.argument("index", IntegerArgumentType.integer(0))
                    .executes(AnnouncementCommands::removeAnnouncement)))
            .then(CommandManager.literal("list")
                .executes(AnnouncementCommands::listAnnouncements))
            .then(CommandManager.literal("reload")
                .executes(AnnouncementCommands::reloadConfig))
            .then(CommandManager.literal("toggle")
                .executes(AnnouncementCommands::toggleAnnouncements))
            .then(CommandManager.literal("interval")
                .then(CommandManager.argument("ticks", IntegerArgumentType.integer(200))
                    .executes(AnnouncementCommands::setInterval)))
            .then(CommandManager.literal("info")
                .executes(AnnouncementCommands::showInfo)));
    }
    
    private static int sendAnnouncement(CommandContext<ServerCommandSource> context) {
        String message = StringArgumentType.getString(context, "message");
        
        try {
            KindlyKlantab.getAnnouncementManager().sendCustomAnnouncement(message);
            context.getSource().sendFeedback(() -> 
                Text.literal("§a✓ Anuncio enviado"), false);
        } catch (Exception e) {
            context.getSource().sendFeedback(() -> 
                Text.literal("§c✗ Error enviando anuncio: " + e.getMessage()), false);
        }
        return 1;
    }
    
    private static int addAnnouncement(CommandContext<ServerCommandSource> context) {
        String message = StringArgumentType.getString(context, "message");
        
        try {
            KindlyKlantab.getAnnouncementManager().addAnnouncement(message, "");
            context.getSource().sendFeedback(() -> 
                Text.literal("§a✓ Anuncio añadido a la lista automática"), false);
        } catch (Exception e) {
            context.getSource().sendFeedback(() -> 
                Text.literal("§c✗ Error añadiendo anuncio: " + e.getMessage()), false);
        }
        return 1;
    }
    
    private static int removeAnnouncement(CommandContext<ServerCommandSource> context) {
        int index = IntegerArgumentType.getInteger(context, "index");
        
        try {
            AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
            if (index >= config.announcements.size()) {
                context.getSource().sendFeedback(() -> 
                    Text.literal("§c✗ Índice inválido. Usa /kkannounce list para ver los anuncios"), false);
                return 0;
            }
            
            KindlyKlantab.getAnnouncementManager().removeAnnouncement(index);
            context.getSource().sendFeedback(() -> 
                Text.literal("§a✓ Anuncio eliminado"), false);
        } catch (Exception e) {
            context.getSource().sendFeedback(() -> 
                Text.literal("§c✗ Error eliminando anuncio: " + e.getMessage()), false);
        }
        return 1;
    }
    
    private static int listAnnouncements(CommandContext<ServerCommandSource> context) {
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        
        if (config.announcements.isEmpty()) {
            context.getSource().sendFeedback(() -> 
                Text.literal("§e⚠ No hay anuncios configurados"), false);
            return 0;
        }
        
        StringBuilder list = new StringBuilder("§6§l═══ Lista de Anuncios ═══\n");
        for (int i = 0; i < config.announcements.size(); i++) {
            AnnouncementConfig.Announcement announcement = config.announcements.get(i);
            String status = announcement.enabled ? "§a✓" : "§c✗";
            list.append("§7[§e").append(i).append("§7] ").append(status).append(" §f")
                .append(announcement.message.substring(0, Math.min(50, announcement.message.length())))
                .append(announcement.message.length() > 50 ? "..." : "").append("\n");
        }
        
        context.getSource().sendFeedback(() -> Text.literal(list.toString()), false);
        return 1;
    }
    
    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        try {
            KindlyKlantab.getConfigManager().reloadConfigs();
            KindlyKlantab.getAnnouncementManager().reload();
            context.getSource().sendFeedback(() -> 
                Text.literal("§a✓ Configuración de anuncios recargada"), false);
        } catch (Exception e) {
            context.getSource().sendFeedback(() -> 
                Text.literal("§c✗ Error recargando configuración: " + e.getMessage()), false);
        }
        return 1;
    }
    
    private static int toggleAnnouncements(CommandContext<ServerCommandSource> context) {
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        config.enabled = !config.enabled;
        KindlyKlantab.getConfigManager().saveAnnouncementConfig();
        
        if (config.enabled) {
            KindlyKlantab.getAnnouncementManager().reload();
            context.getSource().sendFeedback(() -> 
                Text.literal("§a✓ Anuncios automáticos activados"), false);
        } else {
            context.getSource().sendFeedback(() -> 
                Text.literal("§e⚠ Anuncios automáticos desactivados"), false);
        }
        return 1;
    }
    
    private static int setInterval(CommandContext<ServerCommandSource> context) {
        int ticks = IntegerArgumentType.getInteger(context, "ticks");
        
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        config.interval = ticks;
        KindlyKlantab.getConfigManager().saveAnnouncementConfig();
        KindlyKlantab.getAnnouncementManager().reload();
        
        int seconds = ticks / 20;
        context.getSource().sendFeedback(() -> 
            Text.literal("§a✓ Intervalo de anuncios establecido a " + ticks + " ticks (" + seconds + " segundos)"), false);
        return 1;
    }
    
    private static int showInfo(CommandContext<ServerCommandSource> context) {
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        
        int seconds = config.interval / 20;
        context.getSource().sendFeedback(() -> Text.literal(
            "§6§l═══════ KindlyKlanTAB Anuncios ═══════\n" +
            "§7Estado: " + (config.enabled ? "§aActivado" : "§cDesactivado") + "\n" +
            "§7Intervalo: §e" + config.interval + " ticks (" + seconds + " segundos)\n" +
            "§7Orden aleatorio: " + (config.randomOrder ? "§aActivado" : "§cDesactivado") + "\n" +
            "§7Anuncios totales: §e" + config.announcements.size() + "\n" +
            "§7Prefijo: §r" + config.prefix + "\n" +
            "§6§l═══════════════════════════════════"
        ), false);
        return 1;
    }
} 