package me.javivi.kktab.managers;

import me.javivi.kktab.KindlyKlantab;
import me.javivi.kktab.config.TabConfig;
import me.javivi.kktab.utils.PlaceholderResolver;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TabManager {
    private MinecraftServer server;
    private ScheduledExecutorService scheduler;
    private PlaceholderResolver placeholderResolver;
    
    public void initialize(MinecraftServer server) {
        this.server = server;
        this.placeholderResolver = new PlaceholderResolver(server);
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        startTabUpdater();
        KindlyKlantab.LOGGER.info("TabManager inicializado");
    }
    
    private void startTabUpdater() {
        TabConfig config = KindlyKlantab.getConfigManager().getTabConfig();
        if (!config.enabled) return;
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateTabList();
            } catch (Exception e) {
                KindlyKlantab.LOGGER.error("Error actualizando TAB", e);
            }
        }, 0, config.updateInterval * 50L, TimeUnit.MILLISECONDS); // Convertir ticks a ms
    }
    
    public void updateTabList() {
        TabConfig config = KindlyKlantab.getConfigManager().getTabConfig();
        if (!config.enabled || server == null) return;
        
        String header = placeholderResolver.resolve(config.header);
        String footer = placeholderResolver.resolve(config.footer);
        
        Text headerText = Text.literal(header.replace("\\n", "\n"));
        Text footerText = Text.literal(footer.replace("\\n", "\n"));
        
        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket(headerText, footerText);
        
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.networkHandler.sendPacket(packet);
            
            if (config.enableCustomNames) {
                updatePlayerName(player);
            }
        }
    }
    
    private void updatePlayerName(ServerPlayerEntity player) {
        TabConfig config = KindlyKlantab.getConfigManager().getTabConfig();
        TabConfig.TabGroup group = getPlayerGroup(player);
        
        String nameFormat = config.nameFormat
                .replace("{PREFIX}", group.prefix)
                .replace("{PLAYER_NAME}", player.getGameProfile().getName())
                .replace("{SUFFIX}", group.suffix);
        
        nameFormat = placeholderResolver.resolve(nameFormat, player);
        
        // Actualizar el display name del jugador en la lista usando PlayerListEntry
        Text displayName = Text.literal(nameFormat);
        // Nota: En 1.21.1, el display name se maneja a través del PlayerListS2CPacket
        // Por ahora simplemente almacenamos el formato para usar cuando sea necesario
    }
    
    private TabConfig.TabGroup getPlayerGroup(ServerPlayerEntity player) {
        TabConfig config = KindlyKlantab.getConfigManager().getTabConfig();
        
        // Intentar usar LuckPerms si está disponible
        try {
            LuckPermsManager luckPerms = KindlyKlantab.getLuckPermsManager();
            if (luckPerms != null && luckPerms.isAvailable()) {
                String primaryGroup = luckPerms.getPrimaryGroup(player);
                int weight = luckPerms.getWeight(player);
                
                // Buscar si tenemos configuración específica para este grupo
                for (TabConfig.TabGroup group : config.groups) {
                    if (group.permission.equals(primaryGroup)) {
                        // Usar el peso de LuckPerms si está disponible
                        if (weight > 0) {
                            group.priority = 1000 - weight; // Convertir peso a prioridad (mayor peso = menor prioridad)
                        }
                        return group;
                    }
                }
                
                // Si no hay configuración específica, crear un grupo dinámico basado en LuckPerms
                String prefix = luckPerms.getPrefix(player);
                String suffix = luckPerms.getSuffix(player);
                
                if (prefix != null || suffix != null) {
                    TabConfig.TabGroup dynamicGroup = new TabConfig.TabGroup();
                    dynamicGroup.permission = primaryGroup != null ? primaryGroup : "default";
                    dynamicGroup.prefix = prefix != null ? prefix : "§7";
                    dynamicGroup.suffix = suffix != null ? suffix : "";
                    dynamicGroup.priority = weight > 0 ? (1000 - weight) : 999;
                    return dynamicGroup;
                }
            }
        } catch (Exception e) {
            // Fallback silencioso si LuckPerms no está disponible
            KindlyKlantab.LOGGER.debug("LuckPerms no disponible, usando sistema de grupos básico");
        }
        
        // Fallback al sistema original
        TabConfig.TabGroup selectedGroup = null;
        int highestPriority = Integer.MAX_VALUE;
        
        for (TabConfig.TabGroup group : config.groups) {
            if (hasPermission(player, group.permission) && group.priority < highestPriority) {
                selectedGroup = group;
                highestPriority = group.priority;
            }
        }
        
        // Si no se encontró ningún grupo, usar el grupo por defecto
        if (selectedGroup == null) {
            return config.groups.stream()
                    .filter(g -> g.permission.equals("default"))
                    .findFirst()
                    .orElse(new TabConfig.TabGroup("default", "§7", "", 999));
        }
        
        return selectedGroup;
    }
    
    private boolean hasPermission(ServerPlayerEntity player, String permission) {
        if (permission.isEmpty() || permission.equals("default")) {
            return true;
        }
        
        // Intentar usar LuckPerms si está disponible
        try {
            LuckPermsManager luckPerms = KindlyKlantab.getLuckPermsManager();
            if (luckPerms != null && luckPerms.isAvailable()) {
                // Para grupos, verificar si es el grupo principal o si tiene el permiso
                String primaryGroup = luckPerms.getPrimaryGroup(player);
                if (permission.equals(primaryGroup)) {
                    return true;
                }
                
                // Verificar permisos específicos
                return luckPerms.hasPermission(player, "kktab.group." + permission) ||
                       luckPerms.hasPermission(player, permission);
            }
        } catch (Exception e) {
            // Fallback silencioso si LuckPerms no está disponible
        }
        
        // Fallback al sistema básico de OP para permisos básicos
        switch (permission) {
            case "owner":
            case "admin":
            case "mod":
                return server.getPlayerManager().isOperator(player.getGameProfile());
            case "vip":
                return false; 
            default:
                return true;
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
        startTabUpdater();
        updateTabList();
        KindlyKlantab.LOGGER.info("TabManager recargado");
    }
} 