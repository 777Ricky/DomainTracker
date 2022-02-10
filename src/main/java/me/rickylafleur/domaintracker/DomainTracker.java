package me.rickylafleur.domaintracker;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import me.lucko.helper.plugin.ap.PluginDependency;
import me.rickylafleur.domaintracker.commands.JoinsCommand;
import me.rickylafleur.domaintracker.listeners.JoinListener;
import me.rickylafleur.domaintracker.storage.Database;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

@Plugin(
        name = "DomainTracker",
        version = "1.0",
        authors = {"Ricky Lafleur"},
        depends = {@PluginDependency("helper")},
        apiVersion = "1.12"
)
public final class DomainTracker extends ExtendedJavaPlugin {

    private static DomainTracker plugin;

    private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    private Database database;

    @Override
    protected void enable() {
        plugin = this;

        database = new Database(this);

        try {
            database.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().log(Level.INFO, "Database connected successfully!");

        reload();

        // Commands
        bindModule(new JoinsCommand(this));

        // Listeners
        bindModule(new JoinListener(this));
    }

    @Override
    protected void disable() {
        database.disconnect();
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
    }

    public YamlConfiguration getConfig(String path) {
        if (!getFile(path).exists() && getResource(path) != null) {
            saveResource(path, true);
        }

        return YamlConfiguration.loadConfiguration(getFile(path));
    }

    public void saveConfig(YamlConfiguration config, String path) {
        try {
            config.save(getFile(path));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to save config", e);
        }
    }

    public File getFile(String path) {
        return new File(getDataFolder(), path.replace('/', File.separatorChar));
    }

    public Database getDatabase() {
        return database;
    }

    public SimpleDateFormat getFormat() {
        return format;
    }

    public static DomainTracker getInstance() {
        return plugin;
    }
}
