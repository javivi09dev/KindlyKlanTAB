package me.javivi.kktab.config;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementConfig {
    public boolean enabled = true;
    public int interval = 6000; // ticks (5 minutos por defecto)
    public boolean randomOrder = false;
    public String prefix = "§8[§6Anuncio§8] §r";
    
    public List<Announcement> announcements = new ArrayList<>();
    
    public AnnouncementConfig() {
        // Anuncios por defecto
        announcements.add(new Announcement("§e¡Bienvenido al servidor! §7Disfruta tu estadía."));
        announcements.add(new Announcement("§a¿Necesitas ayuda? §7Usa §e/help §7para ver los comandos disponibles."));
        announcements.add(new Announcement("§b¡Únete a nuestro Discord! §7discord.gg/servidor"));
        announcements.add(new Announcement("§d¡Visita nuestra página web! §7www.servidor.com"));
        announcements.add(new Announcement("§c¡Recuerda leer las reglas! §7Usa §e/rules"));
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