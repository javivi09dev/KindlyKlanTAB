package me.javivi.kktab.managers;

import me.javivi.kktab.KindlyKlantab;
import me.javivi.kktab.config.AnnouncementConfig;
import me.javivi.kktab.utils.PlaceholderResolver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnnouncementManager {
    private MinecraftServer server;
    private ScheduledExecutorService scheduler;
    private PlaceholderResolver placeholderResolver;
    private int currentAnnouncementIndex = 0;
    
    public void initialize(MinecraftServer server) {
        this.server = server;
        this.placeholderResolver = new PlaceholderResolver(server);
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        startAnnouncementScheduler();
        KindlyKlantab.LOGGER.info("AnnouncementManager inicializado");
    }
    
    private void startAnnouncementScheduler() {
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        if (!config.enabled || config.announcements.isEmpty()) return;
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sendNextAnnouncement();
            } catch (Exception e) {
                KindlyKlantab.LOGGER.error("Error enviando anuncio", e);
            }
        }, config.interval * 50L, config.interval * 50L, TimeUnit.MILLISECONDS); // Convertir ticks a ms
    }
    
    private void sendNextAnnouncement() {
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        if (!config.enabled || config.announcements.isEmpty()) return;
        
        List<AnnouncementConfig.Announcement> activeAnnouncements = new ArrayList<>();
        for (AnnouncementConfig.Announcement announcement : config.announcements) {
            if (announcement.enabled) {
                activeAnnouncements.add(announcement);
            }
        }
        
        if (activeAnnouncements.isEmpty()) return;
        
        AnnouncementConfig.Announcement announcement;
        if (config.randomOrder) {
            Collections.shuffle(activeAnnouncements);
            announcement = activeAnnouncements.get(0);
        } else {
            if (currentAnnouncementIndex >= activeAnnouncements.size()) {
                currentAnnouncementIndex = 0;
            }
            announcement = activeAnnouncements.get(currentAnnouncementIndex);
            currentAnnouncementIndex++;
        }
        
        sendAnnouncement(announcement);
    }
    
    public void sendAnnouncement(AnnouncementConfig.Announcement announcement) {
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        
        String message = config.prefix + placeholderResolver.resolve(announcement.message);
        // Convertir \n en saltos de línea reales para mensajes multilinea
        message = message.replace("\\n", "\n");
        
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // Verificar permisos si es necesario
            if (announcement.permission.isEmpty() || hasPermission(player, announcement.permission)) {
                sendAnnouncementToPlayer(player, message);
            }
        }
        
        KindlyKlantab.LOGGER.debug("Anuncio enviado: " + announcement.message);
    }
    
    public void sendCustomAnnouncement(String message) {
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        String fullMessage = config.prefix + placeholderResolver.resolve(message);
        // Convertir \n en saltos de línea reales para mensajes multilinea
        fullMessage = fullMessage.replace("\\n", "\n");
        
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendAnnouncementToPlayer(player, fullMessage);
        }
        
        KindlyKlantab.LOGGER.info("Anuncio personalizado enviado: " + message);
    }
    
    private void sendAnnouncementToPlayer(ServerPlayerEntity player, String message) {
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        
        // Determinar modo de visualización
        String displayMode = config.displayMode.toLowerCase();
        boolean isFormattedTitle = message.contains("▬") || message.contains("■") || message.contains("═");
        
        // Aplicar lógica de modo automático
        if (displayMode.equals("auto")) {
            if (config.useTitleForFormatted && isFormattedTitle) {
                displayMode = "title";
            } else {
                displayMode = "chat";
            }
        }
        
        switch (displayMode) {
            case "title":
                sendAsTitle(player, message, config);
                break;
            case "actionbar":
                sendAsActionBar(player, message);
                break;
            case "chat":
            default:
                sendAsChat(player, message);
                break;
        }
    }
    
    private void sendAsTitle(ServerPlayerEntity player, String message, AnnouncementConfig config) {
        try {
            String[] lines = message.split("\n");
            if (lines.length >= 3) {
                // Usar título y subtítulo para mensajes largos
                Text titleText = Text.literal(lines[1].trim()); // Segunda línea como título
                Text subtitleText = Text.literal(lines.length > 2 ? lines[2].trim() : "");
                
                // Enviar título con configuración personalizada
                var titlePacket = new net.minecraft.network.packet.s2c.play.TitleS2CPacket(titleText);
                var subtitlePacket = new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(subtitleText);
                var timesPacket = new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(
                    config.titleFadeIn, config.titleStay, config.titleFadeOut
                );
                
                player.networkHandler.sendPacket(timesPacket);
                player.networkHandler.sendPacket(subtitlePacket);
                player.networkHandler.sendPacket(titlePacket);
            } else {
                // Para mensajes cortos, usar solo título
                Text titleText = Text.literal(message);
                var titlePacket = new net.minecraft.network.packet.s2c.play.TitleS2CPacket(titleText);
                var timesPacket = new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(
                    config.titleFadeIn, config.titleStay, config.titleFadeOut
                );
                
                player.networkHandler.sendPacket(timesPacket);
                player.networkHandler.sendPacket(titlePacket);
            }
        } catch (Exception e) {
            KindlyKlantab.LOGGER.warn("Error enviando título a " + player.getName().getString() + ", usando chat como fallback");
            sendAsChat(player, message);
        }
    }
    
    private void sendAsActionBar(ServerPlayerEntity player, String message) {
        try {
            // Limpiar saltos de línea para actionbar (solo una línea)
            String cleanMessage = message.replace("\n", " | ").trim();
            Text actionBarText = Text.literal(cleanMessage);
            
            var actionBarPacket = new net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket(actionBarText);
            player.networkHandler.sendPacket(actionBarPacket);
        } catch (Exception e) {
            KindlyKlantab.LOGGER.warn("Error enviando actionbar a " + player.getName().getString() + ", usando chat como fallback");
            sendAsChat(player, message);
        }
    }
    
    private void sendAsChat(ServerPlayerEntity player, String message) {
        Text messageText = Text.literal(message);
        player.sendMessage(messageText, false);
    }
    
    private boolean hasPermission(ServerPlayerEntity player, String permission) {
        if (permission.isEmpty()) return true;
        
        // Intentar usar LuckPerms si está disponible
        try {
            LuckPermsManager luckPerms = KindlyKlantab.getLuckPermsManager();
            if (luckPerms != null && luckPerms.isAvailable()) {
                return luckPerms.hasPermission(player, permission);
            }
        } catch (Exception e) {
            KindlyKlantab.LOGGER.debug("LuckPerms no disponible para verificar permiso: " + permission);
        }
        
        // Sistema de permisos fallback integrado
        // Para permisos específicos del mod
        if (permission.startsWith("kktab.")) {
            return checkModPermission(player, permission);
        }
        
        // Integración con permisos generales del servidor
        switch (permission.toLowerCase()) {
            case "admin":
            case "administrator":
            case "op":
                return server.getPlayerManager().isOperator(player.getGameProfile());
            case "mod":
            case "moderator":
                return server.getPlayerManager().isOperator(player.getGameProfile());
            case "vip":
            case "premium":
                // Verificar por grupos básicos o tags del jugador
                return checkVipStatus(player);
            case "default":
            case "member":
                return true; // Todos los jugadores tienen acceso básico
            default:
                // Por defecto, permitir si no hay sistema de permisos específico
                return true;
        }
    }
    
    /**
     * Verifica permisos específicos del mod
     */
    private boolean checkModPermission(ServerPlayerEntity player, String permission) {
        // Permisos del mod siempre requieren OP por defecto
        boolean isOp = server.getPlayerManager().isOperator(player.getGameProfile());
        
        switch (permission.toLowerCase()) {
            case "kktab.admin":
            case "kktab.config":
            case "kktab.reload":
                return isOp;
            case "kktab.announcements.send":
            case "kktab.announcements.manage":
                return isOp;
            case "kktab.tab.manage":
                return isOp;
            case "kktab.use":
            case "kktab.view":
                return true; // Acceso básico para todos
            default:
                return isOp; // Permisos desconocidos requieren OP
        }
    }
    
    /**
     * Verifica estado VIP básico
     * Puede ser expandido para integrar con otros sistemas
     */
    private boolean checkVipStatus(ServerPlayerEntity player) {
        // Implementación básica: verificar si es OP
        if (server.getPlayerManager().isOperator(player.getGameProfile())) {
            return true;
        }
        
        // Aquí se pueden añadir más verificaciones:
        // - Verificar tags NBT del jugador
        // - Verificar archivos de configuración externos
        // - Integrar con otros mods de permisos
        
        return false; // Por defecto, no VIP si no hay sistema externo
    }
    
    public void addAnnouncement(String message, String permission) {
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        AnnouncementConfig.Announcement announcement = new AnnouncementConfig.Announcement(message);
        announcement.permission = permission;
        config.announcements.add(announcement);
        KindlyKlantab.getConfigManager().saveAnnouncementConfig();
        KindlyKlantab.LOGGER.info("Anuncio añadido: " + message);
    }
    
    public void removeAnnouncement(int index) {
        AnnouncementConfig config = KindlyKlantab.getConfigManager().getAnnouncementConfig();
        if (index >= 0 && index < config.announcements.size()) {
            String removedMessage = config.announcements.get(index).message;
            config.announcements.remove(index);
            KindlyKlantab.getConfigManager().saveAnnouncementConfig();
            KindlyKlantab.LOGGER.info("Anuncio eliminado: " + removedMessage);
        }
    }
    
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
    
    public void reload() {
        shutdown();
        // Recrear el scheduler después de hacer shutdown
        this.scheduler = Executors.newScheduledThreadPool(1);
        startAnnouncementScheduler();
        KindlyKlantab.LOGGER.info("AnnouncementManager recargado");
    }
} 