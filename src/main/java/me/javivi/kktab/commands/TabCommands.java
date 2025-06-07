package me.javivi.kktab.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.javivi.kktab.KindlyKlantab;
import me.javivi.kktab.config.TabConfig;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;
import me.javivi.kktab.config.AnnouncementConfig;
import net.minecraft.server.network.ServerPlayerEntity;

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
                .executes(TabCommands::showInfo))
            .then(CommandManager.literal("test")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    KindlyKlantab.getTabManager().updateTabList();
                    source.sendFeedback(() -> Text.literal("§aTAB actualizado manualmente para todos los jugadores"), false);
                    return 1;
                }))
            .then(CommandManager.literal("testannounce")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("mode", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        builder.suggest("chat");
                        builder.suggest("title");
                        builder.suggest("actionbar");
                        return builder.buildFuture();
                    })
                    .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            String mode = StringArgumentType.getString(context, "mode");
                            String message = StringArgumentType.getString(context, "message");
                            
                            // Crear anuncio de prueba temporal
                            AnnouncementConfig.Announcement testAnnouncement = new AnnouncementConfig.Announcement(message);
                            
                            // Guardar configuración original
                            AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
                            String originalMode = config.displayMode;
                            
                            // Cambiar temporalmente el modo
                            config.displayMode = mode;
                            
                            // Enviar anuncio
                            KindlyKlantab.getAnnouncementManager().sendAnnouncement(testAnnouncement);
                            
                            // Restaurar configuración
                            config.displayMode = originalMode;
                            
                            source.sendFeedback(() -> Text.literal("§aAnuncio de prueba enviado en modo " + mode), false);
                            return 1;
                        }))))
            .then(CommandManager.literal("debug")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("player", StringArgumentType.word())
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        String playerName = StringArgumentType.getString(context, "player");
                        MinecraftServer server = source.getServer();
                        
                        // Buscar el jugador
                        ServerPlayerEntity targetPlayer = server.getPlayerManager().getPlayer(playerName);
                        if (targetPlayer == null) {
                            source.sendError(Text.literal("§cJugador no encontrado: " + playerName));
                            return 0;
                        }
                        
                        // Obtener información del grupo
                        try {
                            TabConfig config = KindlyKlantab.getConfigManager().getTabConfig();
                            
                            // Usar reflection para acceder al método privado getPlayerGroup
                            var tabManager = KindlyKlantab.getTabManager();
                            var method = tabManager.getClass().getDeclaredMethod("getPlayerGroup", ServerPlayerEntity.class);
                            method.setAccessible(true);
                            var group = (TabConfig.TabGroup) method.invoke(tabManager, targetPlayer);
                            
                            source.sendFeedback(() -> Text.literal("§6=== DEBUG TAB para " + playerName + " ==="), false);
                            source.sendFeedback(() -> Text.literal("§eGrupo: §f" + group.permission), false);
                            source.sendFeedback(() -> Text.literal("§ePrefix: §f" + group.prefix), false);
                            source.sendFeedback(() -> Text.literal("§eSuffix: §f" + group.suffix), false);
                            source.sendFeedback(() -> Text.literal("§ePrioridad: §f" + group.priority), false);
                            
                            // Información de LuckPerms si está disponible
                            try {
                                var luckPerms = KindlyKlantab.getLuckPermsManager();
                                if (luckPerms != null && luckPerms.isAvailable()) {
                                    String lpGroup = luckPerms.getPrimaryGroup(targetPlayer);
                                    String lpPrefix = luckPerms.getPrefix(targetPlayer);
                                    String lpSuffix = luckPerms.getSuffix(targetPlayer);
                                    int lpWeight = luckPerms.getWeight(targetPlayer);
                                    
                                    source.sendFeedback(() -> Text.literal("§a=== LuckPerms Info ==="), false);
                                    source.sendFeedback(() -> Text.literal("§aGrupo LP: §f" + lpGroup), false);
                                    source.sendFeedback(() -> Text.literal("§aPrefix LP: §f" + lpPrefix), false);
                                    source.sendFeedback(() -> Text.literal("§aSuffix LP: §f" + lpSuffix), false);
                                    source.sendFeedback(() -> Text.literal("§aPeso LP: §f" + lpWeight), false);
                                } else {
                                    source.sendFeedback(() -> Text.literal("§cLuckPerms no disponible"), false);
                                }
                            } catch (Exception e) {
                                source.sendFeedback(() -> Text.literal("§cError obteniendo info de LuckPerms: " + e.getMessage()), false);
                            }
                            
                            // Probar actualización del nombre
                            try {
                                var updateMethod = tabManager.getClass().getDeclaredMethod("updatePlayerName", ServerPlayerEntity.class);
                                updateMethod.setAccessible(true);
                                updateMethod.invoke(tabManager, targetPlayer);
                                source.sendFeedback(() -> Text.literal("§aNombre TAB actualizado para " + playerName), false);
                            } catch (Exception e) {
                                source.sendFeedback(() -> Text.literal("§cError actualizando nombre TAB: " + e.getMessage()), false);
                            }
                            
                        } catch (Exception e) {
                            source.sendError(Text.literal("§cError en debug: " + e.getMessage()));
                            e.printStackTrace();
                        }
                        
                        return 1;
                    }))));
    }
    
    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        try {
            KindlyKlantab.getConfigManager().reloadConfigs();
            KindlyKlantab.getTabManager().reload();
            context.getSource().sendFeedback(() -> 
                Text.literal("§a✓ Configuración del TAB recargada correctamente"), false);
        } catch (Exception e) {
            context.getSource().sendFeedback(() -> 
                Text.literal("§c✗ Error recargando configuración: " + e.getMessage()), false);
            KindlyKlantab.LOGGER.error("Error recargando configuración", e);
        }
        return 1;
    }
    
    private static int updateTab(CommandContext<ServerCommandSource> context) {
        try {
            KindlyKlantab.getTabManager().updateTabList();
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
        
        TabConfig config = KindlyKlantab.getConfigManager().getTabConfig();
        config.header = header;
        KindlyKlantab.getConfigManager().saveTabConfig();
        KindlyKlantab.getTabManager().updateTabList();
        
        context.getSource().sendFeedback(() -> 
            Text.literal("§a✓ Header del TAB actualizado"), false);
        return 1;
    }
    
    private static int setFooter(CommandContext<ServerCommandSource> context) {
        String footer = StringArgumentType.getString(context, "text");
        
        TabConfig config = KindlyKlantab.getConfigManager().getTabConfig();
        config.footer = footer;
        KindlyKlantab.getConfigManager().saveTabConfig();
        KindlyKlantab.getTabManager().updateTabList();
        
        context.getSource().sendFeedback(() -> 
            Text.literal("§a✓ Footer del TAB actualizado"), false);
        return 1;
    }
    
    private static int toggleTab(CommandContext<ServerCommandSource> context) {
        TabConfig config = KindlyKlantab.getConfigManager().getTabConfig();
        config.enabled = !config.enabled;
        KindlyKlantab.getConfigManager().saveTabConfig();
        
        if (config.enabled) {
            KindlyKlantab.getTabManager().reload();
            context.getSource().sendFeedback(() -> 
                Text.literal("§a✓ TAB personalizado activado"), false);
        } else {
            context.getSource().sendFeedback(() -> 
                Text.literal("§e⚠ TAB personalizado desactivado"), false);
        }
        return 1;
    }
    
    private static int showInfo(CommandContext<ServerCommandSource> context) {
        TabConfig config = KindlyKlantab.getConfigManager().getTabConfig();
        
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