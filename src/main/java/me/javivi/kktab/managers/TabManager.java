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
        
        // En Minecraft 1.21.1, actualizar el display name del jugador en el TAB
        Text displayName = Text.literal(nameFormat);
        
        try {
            // Método simplificado para actualizar el nombre en el TAB
            // Usar el método más directo disponible en la API
            
            // Primero, establecer el custom name del jugador
            player.setCustomName(displayName);
            
            // Crear packet de actualización usando el constructor correcto
            var packet = new net.minecraft.network.packet.s2c.play.PlayerListS2CPacket(
                net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME,
                player
            );
            
            // Enviar a todos los jugadores conectados
            for (ServerPlayerEntity otherPlayer : server.getPlayerManager().getPlayerList()) {
                otherPlayer.networkHandler.sendPacket(packet);
            }
            
            KindlyKlantab.LOGGER.debug("Nombre TAB actualizado para " + player.getName().getString() + ": " + nameFormat);
            
        } catch (Exception e) {
            KindlyKlantab.LOGGER.error("Error actualizando nombre en TAB para " + player.getName().getString() + ": " + e.getMessage());
            
            // Fallback: al menos establecer el custom name
            try {
                player.setCustomName(displayName);
                KindlyKlantab.LOGGER.debug("Custom name establecido como fallback para " + player.getName().getString());
            } catch (Exception fallbackError) {
                KindlyKlantab.LOGGER.error("Error en fallback para " + player.getName().getString(), fallbackError);
            }
        }
    }
    
    private TabConfig.TabGroup getPlayerGroup(ServerPlayerEntity player) {
        TabConfig config = KindlyKlantab.getConfigManager().getTabConfig();
        
        // Intentar usar LuckPerms si está disponible
        try {
            LuckPermsManager luckPerms = KindlyKlantab.getLuckPermsManager();
            if (luckPerms != null && luckPerms.isAvailable()) {
                String primaryGroup = luckPerms.getPrimaryGroup(player);
                String prefix = luckPerms.getPrefix(player);
                String suffix = luckPerms.getSuffix(player);
                int weight = luckPerms.getWeight(player);
                
                KindlyKlantab.LOGGER.debug("LuckPerms - Jugador: " + player.getName().getString() + 
                    ", Grupo: " + primaryGroup + ", Prefix: " + prefix + ", Suffix: " + suffix + ", Weight: " + weight);
                
                // Buscar si tenemos configuración específica para este grupo de LuckPerms
                for (TabConfig.TabGroup group : config.groups) {
                    if (group.permission.equals(primaryGroup)) {
                        // Usar el peso de LuckPerms si está disponible
                        if (weight > 0) {
                            group.priority = 1000 - weight; // Convertir peso a prioridad (mayor peso = menor prioridad)
                        }
                        KindlyKlantab.LOGGER.debug("Usando grupo configurado: " + group.permission + " para " + player.getName().getString());
                        return group;
                    }
                }
                
                // Si hay prefix/suffix de LuckPerms pero no hay configuración específica, usar directamente LuckPerms
                if (prefix != null || suffix != null) {
                    TabConfig.TabGroup dynamicGroup = new TabConfig.TabGroup();
                    dynamicGroup.permission = primaryGroup != null ? primaryGroup : "default";
                    dynamicGroup.prefix = prefix != null ? prefix : "";
                    dynamicGroup.suffix = suffix != null ? suffix : "";
                    dynamicGroup.priority = weight > 0 ? (1000 - weight) : 999;
                    KindlyKlantab.LOGGER.debug("Creando grupo dinámico desde LuckPerms: " + dynamicGroup.permission + " para " + player.getName().getString());
                    return dynamicGroup;
                }
                
                // Si LuckPerms está disponible pero no hay prefix/suffix, mapear grupo conocidos
                if (primaryGroup != null) {
                    TabConfig.TabGroup mappedGroup = mapLuckPermsGroupToConfig(primaryGroup, config);
                    if (mappedGroup != null) {
                        KindlyKlantab.LOGGER.debug("Mapeando grupo LuckPerms '" + primaryGroup + "' a '" + mappedGroup.permission + "' para " + player.getName().getString());
                        return mappedGroup;
                    }
                }
            }
        } catch (Exception e) {
            KindlyKlantab.LOGGER.debug("Error usando LuckPerms para " + player.getName().getString() + ": " + e.getMessage());
        }
        
        // Fallback al sistema original basado en permisos/OP
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
            selectedGroup = config.groups.stream()
                    .filter(g -> g.permission.equals("default"))
                    .findFirst()
                    .orElse(new TabConfig.TabGroup("default", "§7", "", 999));
        }
        
        KindlyKlantab.LOGGER.debug("Usando grupo fallback: " + selectedGroup.permission + " para " + player.getName().getString());
        return selectedGroup;
    }
    
    private TabConfig.TabGroup mapLuckPermsGroupToConfig(String luckPermsGroup, TabConfig config) {
        // Mapeo de grupos comunes de LuckPerms a la configuración del mod
        String inputGroup = luckPermsGroup.toLowerCase();
        String mappedGroup;
        
        // Mapeos comunes
        switch (inputGroup) {
            case "owner":
            case "dueño":
            case "propietario":
                mappedGroup = "owner";
                break;
            case "admin":
            case "administrator":
            case "administrador":
                mappedGroup = "admin";
                break;
            case "moderator":
            case "mod":
                mappedGroup = "mod";
                break;
            case "vip":
            case "premium":
            case "plus":
                mappedGroup = "vip";
                break;
            case "default":
            case "member":
            case "player":
            case "miembro":
            case "jugador":
                mappedGroup = "default";
                break;
            default:
                mappedGroup = inputGroup; // Usar el grupo original si no hay mapeo
                break;
        }
        
        // Buscar el grupo mapeado en la configuración
        final String finalMappedGroup = mappedGroup;
        return config.groups.stream()
                .filter(g -> g.permission.equals(finalMappedGroup))
                .findFirst()
                .orElse(null);
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