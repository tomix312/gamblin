package pl.kasyno;

import org.bukkit.plugin.java.JavaPlugin;

public class KasynoPlugin extends JavaPlugin {

    private static KasynoPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        
        // Rejestracja komendy
        getCommand("kasyno").setExecutor(new KasynoCommand());
        
        // Rejestracja listenerow
        getServer().getPluginManager().registerEvents(new KasynoListener(), this);
        
        getLogger().info("[Kasyno] Plugin zostal wlaczony!");
    }

    @Override
    public void onDisable() {
        getLogger().info("[Kasyno] Plugin zostal wylaczony!");
    }

    public static KasynoPlugin getInstance() {
        return instance;
    }
}
