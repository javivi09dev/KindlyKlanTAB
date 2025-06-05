package me.javivi.kktab.config;

import java.util.ArrayList;
import java.util.List;

public class TabConfig {
    public boolean enabled = true;
    public int updateInterval = 20; // ticks (1 segundo)
    
    // Header y Footer del TAB
    public String header = "§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n§e§l                      ¡Bienvenido al Servidor!                      \n§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";
    public String footer = "§7Jugadores conectados: §a{PLAYER_COUNT}§7 | TPS: §a{TPS}\n§7Discord: §bdiscord.gg/servidor §7| Web: §bwww.servidor.com";
    
    // Configuración de nombres en el TAB
    public boolean enableCustomNames = true;
    public String nameFormat = "{PREFIX} {PLAYER_NAME} {SUFFIX}";
    
    // Prefijos y sufijos por grupo/permiso
    public List<TabGroup> groups = new ArrayList<>();
    
    public TabConfig() {
        // Grupos por defecto
        groups.add(new TabGroup("owner", "§4[Dueño] ", "", 1));
        groups.add(new TabGroup("admin", "§c[Admin] ", "", 2));
        groups.add(new TabGroup("mod", "§e[Mod] ", "", 3));
        groups.add(new TabGroup("vip", "§6[VIP] ", "", 4));
        groups.add(new TabGroup("default", "§7", "", 5));
    }
    
    public static class TabGroup {
        public String permission;
        public String prefix;
        public String suffix;
        public int priority; // menor número = mayor prioridad
        
        public TabGroup(String permission, String prefix, String suffix, int priority) {
            this.permission = permission;
            this.prefix = prefix;
            this.suffix = suffix;
            this.priority = priority;
        }
        
        public TabGroup() {
            // Constructor vacío para Gson
        }
    }
} 