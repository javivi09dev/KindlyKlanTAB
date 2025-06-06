package me.javivi.kktab.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.javivi.kktab.KindlyKlantab;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configDir;
    
    private TabConfig tabConfig;
    private AnnouncementConfig announcementConfig;
    
    public ConfigManager() {
        this.configDir = FabricLoader.getInstance().getConfigDir().resolve("kindlyklantab");
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            KindlyKlantab.LOGGER.error("Error creando directorio de configuración", e);
        }
    }
    
    public void loadConfigs() {
        loadTabConfig();
        loadAnnouncementConfig();
    }
    
    private void loadTabConfig() {
        Path tabConfigPath = configDir.resolve("tab.json");
        try {
            if (Files.exists(tabConfigPath)) {
                String content = Files.readString(tabConfigPath);
                tabConfig = GSON.fromJson(content, TabConfig.class);
                KindlyKlantab.LOGGER.info("Configuración del TAB cargada");
            } else {
                tabConfig = new TabConfig();
                saveTabConfig();
                KindlyKlantab.LOGGER.info("Configuración del TAB creada por defecto");
            }
        } catch (Exception e) {
            KindlyKlantab.LOGGER.error("Error cargando configuración del TAB", e);
            tabConfig = new TabConfig();
        }
    }
    
    private void loadAnnouncementConfig() {
        Path announcementConfigPath = configDir.resolve("announcements.json");
        try {
            if (Files.exists(announcementConfigPath)) {
                String content = Files.readString(announcementConfigPath);
                announcementConfig = GSON.fromJson(content, AnnouncementConfig.class);
                KindlyKlantab.LOGGER.info("Configuración de anuncios cargada");
            } else {
                announcementConfig = new AnnouncementConfig();
                saveAnnouncementConfig();
                KindlyKlantab.LOGGER.info("Configuración de anuncios creada por defecto");
            }
        } catch (Exception e) {
            KindlyKlantab.LOGGER.error("Error cargando configuración de anuncios", e);
            announcementConfig = new AnnouncementConfig();
        }
    }
    
    public void saveTabConfig() {
        Path tabConfigPath = configDir.resolve("tab.json");
        try {
            Files.writeString(tabConfigPath, GSON.toJson(tabConfig));
        } catch (IOException e) {
            KindlyKlantab.LOGGER.error("Error guardando configuración del TAB", e);
        }
    }
    
    public void saveAnnouncementConfig() {
        Path announcementConfigPath = configDir.resolve("announcements.json");
        try {
            Files.writeString(announcementConfigPath, GSON.toJson(announcementConfig));
        } catch (IOException e) {
            KindlyKlantab.LOGGER.error("Error guardando configuración de anuncios", e);
        }
    }
    
    public TabConfig getTabConfig() {
        return tabConfig;
    }
    
    public AnnouncementConfig getAnnouncementConfig() {
        return announcementConfig;
    }
    
    public void reloadConfigs() {
        loadConfigs();
        KindlyKlantab.LOGGER.info("Configuraciones recargadas");
    }
} 