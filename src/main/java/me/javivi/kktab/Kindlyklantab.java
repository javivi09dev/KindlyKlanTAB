package me.javivi.kktab;

import me.javivi.kktab.commands.TabCommands;
import me.javivi.kktab.commands.AnnouncementCommands;
import me.javivi.kktab.config.ConfigManager;
import me.javivi.kktab.managers.TabManager;
import me.javivi.kktab.managers.AnnouncementManager;
import me.javivi.kktab.managers.LuckPermsManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KindlyKlanTab implements ModInitializer {
    public static final String MOD_ID = "kindlyklantab";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static ConfigManager configManager;
    private static TabManager tabManager;
    private static AnnouncementManager announcementManager;
    private static LuckPermsManager luckPermsManager;

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando KindlyKlanTAB v1.0");
        
        // Inicializar managers
        configManager = new ConfigManager();
        tabManager = new TabManager();
        announcementManager = new AnnouncementManager();
        luckPermsManager = new LuckPermsManager();
        
        // Registrar comandos
        CommandRegistrationCallback.EVENT.register(TabCommands::register);
        CommandRegistrationCallback.EVENT.register(AnnouncementCommands::register);
        
        // Eventos del servidor
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            configManager.loadConfigs();
            luckPermsManager.initialize(); // Inicializar LuckPerms primero
            tabManager.initialize(server);
            announcementManager.initialize(server);
            LOGGER.info("KindlyKlanTAB cargado correctamente");
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            announcementManager.shutdown();
            LOGGER.info("KindlyKlanTAB desactivado");
        });
    }
    
    public static ConfigManager getConfigManager() {
        return configManager;
    }
    
    public static TabManager getTabManager() {
        return tabManager;
    }
    
    public static AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }
    
    public static LuckPermsManager getLuckPermsManager() {
        return luckPermsManager;
    }
}
