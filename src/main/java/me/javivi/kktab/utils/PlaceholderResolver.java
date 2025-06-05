package me.javivi.kktab.utils;

import me.javivi.kktab.KindlyKlanTab;
import me.javivi.kktab.managers.LuckPermsManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PlaceholderResolver {
    private final MinecraftServer server;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    public PlaceholderResolver(MinecraftServer server) {
        this.server = server;
    }
    
    public String resolve(String text) {
        return resolve(text, null);
    }
    
    public String resolve(String text, ServerPlayerEntity player) {
        if (text == null) return "";
        
        String result = text;
        
        // Placeholders generales del servidor
        result = result.replace("{PLAYER_COUNT}", String.valueOf(server.getCurrentPlayerCount()));
        result = result.replace("{MAX_PLAYERS}", String.valueOf(server.getMaxPlayerCount()));
        result = result.replace("{TPS}", String.format("%.1f", calculateTPS()));
        result = result.replace("{MSPT}", String.format("%.2f", server.getAverageTickTime()));
        result = result.replace("{TIME}", timeFormat.format(new Date()));
        result = result.replace("{DATE}", dateFormat.format(new Date()));
        result = result.replace("{UPTIME}", formatUptime());
        result = result.replace("{SERVER_NAME}", server.getServerMotd());
        
        // Placeholders específicos del jugador
        if (player != null) {
            result = result.replace("{PLAYER_NAME}", player.getGameProfile().getName());
            result = result.replace("{PLAYER_UUID}", player.getGameProfile().getId().toString());
            result = result.replace("{PLAYER_PING}", String.valueOf(player.networkHandler.getLatency()));
            result = result.replace("{PLAYER_WORLD}", player.getServerWorld().getRegistryKey().getValue().toString());
            result = result.replace("{PLAYER_X}", String.valueOf((int) player.getX()));
            result = result.replace("{PLAYER_Y}", String.valueOf((int) player.getY()));
            result = result.replace("{PLAYER_Z}", String.valueOf((int) player.getZ()));
            
            // Placeholders de LuckPerms (si está disponible)
            result = resolveLuckPermsPlaceholders(result, player);
        }
        
        return result;
    }
    
    private String resolveLuckPermsPlaceholders(String text, ServerPlayerEntity player) {
        try {
            LuckPermsManager luckPerms = KindlyKlanTab.getLuckPermsManager();
            if (luckPerms != null && luckPerms.isAvailable()) {
                // Placeholders básicos de LuckPerms
                String prefix = luckPerms.getPrefix(player);
                String suffix = luckPerms.getSuffix(player);
                String primaryGroup = luckPerms.getPrimaryGroup(player);
                int weight = luckPerms.getWeight(player);
                
                text = text.replace("{LUCKPERMS_PREFIX}", prefix != null ? prefix : "");
                text = text.replace("{LUCKPERMS_SUFFIX}", suffix != null ? suffix : "");
                text = text.replace("{LUCKPERMS_GROUP}", primaryGroup != null ? primaryGroup : "default");
                text = text.replace("{LUCKPERMS_WEIGHT}", String.valueOf(weight));
                
                // Placeholders heredados para compatibilidad
                text = text.replace("{PREFIX}", prefix != null ? prefix : "");
                text = text.replace("{SUFFIX}", suffix != null ? suffix : "");
                text = text.replace("{GROUP}", primaryGroup != null ? primaryGroup : "default");
                
                // Placeholder para meta personalizada
                // Formato: {LUCKPERMS_META_<key>}
                if (text.contains("{LUCKPERMS_META_")) {
                    text = resolveLuckPermsMetaPlaceholders(text, player, luckPerms);
                }
            } else {
                // Limpiar placeholders de LuckPerms si no está disponible
                text = text.replace("{LUCKPERMS_PREFIX}", "");
                text = text.replace("{LUCKPERMS_SUFFIX}", "");
                text = text.replace("{LUCKPERMS_GROUP}", "default");
                text = text.replace("{LUCKPERMS_WEIGHT}", "0");
                text = text.replace("{PREFIX}", "");
                text = text.replace("{SUFFIX}", "");
                text = text.replace("{GROUP}", "default");
            }
        } catch (Exception e) {
            KindlyKlanTab.LOGGER.error("Error resolviendo placeholders de LuckPerms", e);
        }
        
        return text;
    }
    
    private String resolveLuckPermsMetaPlaceholders(String text, ServerPlayerEntity player, LuckPermsManager luckPerms) {
        String result = text;
        int startIndex = 0;
        
        while ((startIndex = result.indexOf("{LUCKPERMS_META_", startIndex)) != -1) {
            int endIndex = result.indexOf("}", startIndex);
            if (endIndex != -1) {
                String placeholder = result.substring(startIndex, endIndex + 1);
                String metaKey = placeholder.substring(16, placeholder.length() - 1); // Remover {LUCKPERMS_META_ y }
                
                String metaValue = luckPerms.getMetaValue(player, metaKey);
                result = result.replace(placeholder, metaValue != null ? metaValue : "");
                
                startIndex = endIndex;
            } else {
                break;
            }
        }
        
        return result;
    }
    
    private double calculateTPS() {
        // Cálculo simple de TPS basado en el tick time promedio
        double averageTickTime = server.getAverageTickTime();
        if (averageTickTime == 0) return 20.0;
        return Math.min(20.0, 1000.0 / averageTickTime);
    }
    
    private String formatUptime() {
        long uptimeMs = System.currentTimeMillis() - server.getTimeReference();
        long days = TimeUnit.MILLISECONDS.toDays(uptimeMs);
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMs) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(uptimeMs));
        
        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
} 