package me.javivi.kktab.config;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementConfig {
    public boolean enabled = true;
    public int interval = 6000; // ticks (5 minutos por defecto)
    public boolean randomOrder = false;
    public String prefix = ""; // Sin prefix por defecto
    
    // Configuración de visualización de anuncios
    public String displayMode = "auto"; // "chat", "title", "actionbar", "auto"
    public boolean useTitleForFormatted = true; // Usar títulos para mensajes con decoraciones
    public int titleFadeIn = 10; // ticks (0.5 segundos)
    public int titleStay = 60; // ticks (3 segundos)
    public int titleFadeOut = 20; // ticks (1 segundo)
    
    public List<Announcement> announcements = new ArrayList<>();
    
    public AnnouncementConfig() {
        // Anuncios por defecto más bonitos y sin prefix
        announcements.add(new Announcement(
            "§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
            "§r§6§l                    ¡BIENVENIDO AL SERVIDOR!\n" +
            "§r§7                  Esperamos que disfrutes tu estadía\n" +
            "§r§e                    Usa §a/help §epara ver comandos\n" +
            "§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        ));
        
        announcements.add(new Announcement(
            "§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
            "§r§b§l                     ÚNETE A NUESTRO DISCORD\n" +
            "§r§7                    Conecta con la comunidad\n" +
            "§r§e                      §adiscord.gg/servidor\n" +
            "§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        ));
        
        announcements.add(new Announcement(
            "§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
            "§r§d§l                    VISITA NUESTRA WEB\n" +
            "§r§7                 Encuentra guías y noticias\n" +
            "§r§e                     §awww.servidor.com\n" +
            "§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        ));
        
        announcements.add(new Announcement(
            "§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
            "§r§c§l                      REGLAS DEL SERVIDOR\n" +
            "§r§7                   Es importante que las conozcas\n" +
            "§r§e                        Usa §a/rules\n" +
            "§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        ));
        
        announcements.add(new Announcement(
            "§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
            "§r§a§l                     ESTADO DEL SERVIDOR\n" +
            "§r§7                    Jugadores online: §e{PLAYER_COUNT}§7/§e{MAX_PLAYERS}\n" +
            "§r§7                      TPS: §e{TPS} §7| Uptime: §e{UPTIME}\n" +
            "§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        ));
    }
    
    public static class Announcement {
        public String message;
        public boolean enabled = true;
        public String permission = ""; // Permiso requerido para ver el anuncio (vacío = todos)
        
        public Announcement(String message) {
            this.message = message;
        }
        
        public Announcement() {
            // Constructor vacío para Gson
        }
    }
} 