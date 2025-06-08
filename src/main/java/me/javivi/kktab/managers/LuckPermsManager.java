package me.javivi.kktab.managers;

import me.javivi.kktab.KindlyKlantab;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LuckPermsManager {
    private Object luckPerms;
    private boolean isAvailable = false;
    private boolean initializationAttempted = false;
    
    public void initialize() {
        if (initializationAttempted) return;
        initializationAttempted = true;
        
        try {
            // Usar reflexión para cargar LuckPerms de forma segura
            Class<?> luckPermsProviderClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            luckPerms = luckPermsProviderClass.getMethod("get").invoke(null);
            isAvailable = true;
            KindlyKlantab.LOGGER.info("✅ Integración con LuckPerms activada");
        } catch (ClassNotFoundException e) {
            isAvailable = false;
            KindlyKlantab.LOGGER.info("⚠️ LuckPerms no encontrado, usando sistema de permisos básico");
        } catch (Exception e) {
            isAvailable = false;
            KindlyKlantab.LOGGER.warn("⚠️ Error inicializando LuckPerms: " + e.getMessage());
        }
    }
    
    public boolean isAvailable() {
        return isAvailable && luckPerms != null;
    }
    
    @Nullable
    public String getPrefix(ServerPlayerEntity player) {
        if (!isAvailable()) return "";
        
        try {
            Object user = getUser(player);
            if (user == null) return "";
            
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            String prefix = (String) metaData.getClass().getMethod("getPrefix").invoke(metaData);
            
            return convertLegacyColors(prefix != null ? prefix : "");
        } catch (Exception e) {
            KindlyKlantab.LOGGER.debug("Error obteniendo prefix de LuckPerms para " + player.getName().getString(), e);
            return "";
        }
    }
    
    @Nullable
    public String getSuffix(ServerPlayerEntity player) {
        if (!isAvailable()) return "";
        
        try {
            Object user = getUser(player);
            if (user == null) return "";
            
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            String suffix = (String) metaData.getClass().getMethod("getSuffix").invoke(metaData);
            
            return convertLegacyColors(suffix != null ? suffix : "");
        } catch (Exception e) {
            KindlyKlantab.LOGGER.debug("Error obteniendo suffix de LuckPerms para " + player.getName().getString(), e);
            return "";
        }
    }
    
    /**
     * Convierte códigos de color legacy (&) a formato moderno (§)
     */
    private String convertLegacyColors(String text) {
        if (text == null || text.isEmpty()) return text;
        
        // Convertir códigos de color legacy & a formato moderno §
        return text.replace("&0", "§0")
                  .replace("&1", "§1")
                  .replace("&2", "§2")
                  .replace("&3", "§3")
                  .replace("&4", "§4")
                  .replace("&5", "§5")
                  .replace("&6", "§6")
                  .replace("&7", "§7")
                  .replace("&8", "§8")
                  .replace("&9", "§9")
                  .replace("&a", "§a")
                  .replace("&b", "§b")
                  .replace("&c", "§c")
                  .replace("&d", "§d")
                  .replace("&e", "§e")
                  .replace("&f", "§f")
                  .replace("&k", "§k")
                  .replace("&l", "§l")
                  .replace("&m", "§m")
                  .replace("&n", "§n")
                  .replace("&o", "§o")
                  .replace("&r", "§r");
    }
    
    @Nullable
    public String getPrimaryGroup(ServerPlayerEntity player) {
        if (!isAvailable() || player == null) return null;
        
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUuid());
            if (user == null) return null;
            
            return (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
        } catch (Exception e) {
            KindlyKlantab.LOGGER.debug("Error obteniendo grupo principal de LuckPerms para " + player.getName().getString() + ": " + e.getMessage());
            return null;
        }
    }
    
    public boolean hasPermission(ServerPlayerEntity player, String permission) {
        if (!isAvailable() || player == null || permission == null || permission.isEmpty()) {
            return false;
        }
        
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUuid());
            if (user == null) return false;
            
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object permissionData = cachedData.getClass().getMethod("getPermissionData").invoke(cachedData);
            Object result = permissionData.getClass().getMethod("checkPermission", String.class).invoke(permissionData, permission);
            return (Boolean) result.getClass().getMethod("asBoolean").invoke(result);
        } catch (Exception e) {
            KindlyKlantab.LOGGER.debug("Error verificando permiso " + permission + " para " + player.getName().getString() + ": " + e.getMessage());
            return false;
        }
    }
    
    public int getWeight(ServerPlayerEntity player) {
        if (!isAvailable() || player == null) return 0;
        
        try {
            String primaryGroup = getPrimaryGroup(player);
            if (primaryGroup == null) return 0;
            
            Object groupManager = luckPerms.getClass().getMethod("getGroupManager").invoke(luckPerms);
            Object group = groupManager.getClass().getMethod("getGroup", String.class).invoke(groupManager, primaryGroup);
            if (group == null) return 0;
            
            Object weightOptional = group.getClass().getMethod("getWeight").invoke(group);
            if (weightOptional.getClass().getMethod("isPresent").invoke(weightOptional).equals(Boolean.TRUE)) {
                return (Integer) weightOptional.getClass().getMethod("get").invoke(weightOptional);
            }
            return 0;
        } catch (Exception e) {
            KindlyKlantab.LOGGER.debug("Error obteniendo peso del grupo para " + player.getName().getString() + ": " + e.getMessage());
            return 0;
        }
    }
    
    @Nullable
    public String getMetaValue(ServerPlayerEntity player, String key) {
        if (!isAvailable() || player == null || key == null) return null;
        
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUuid());
            if (user == null) return null;
            
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            return (String) metaData.getClass().getMethod("getMetaValue", String.class).invoke(metaData, key);
        } catch (Exception e) {
            KindlyKlantab.LOGGER.debug("Error obteniendo meta " + key + " para " + player.getName().getString() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene el usuario de LuckPerms para un jugador
     */
    private Object getUser(ServerPlayerEntity player) {
        if (!isAvailable() || player == null) return null;
        
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            return userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUuid());
        } catch (Exception e) {
            KindlyKlantab.LOGGER.debug("Error obteniendo usuario de LuckPerms para " + player.getName().getString(), e);
            return null;
        }
    }
} 