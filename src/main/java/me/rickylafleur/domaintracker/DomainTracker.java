package me.rickylafleur.domaintracker;

import com.maxmind.geoip2.DatabaseReader;
import me.lucko.helper.Schedulers;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import me.lucko.helper.plugin.ap.PluginDependency;
import me.rickylafleur.domaintracker.commands.JoinsCommand;
import me.rickylafleur.domaintracker.listeners.JoinListener;
import me.rickylafleur.domaintracker.storage.Database;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

@Plugin(
        name = "DomainTracker",
        version = "1.0",
        authors = {"Ricky Lafleur"},
        apiVersion = "1.18"
)
public final class DomainTracker extends ExtendedJavaPlugin {

    private static DomainTracker plugin;

    private final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

    private Database database;

    private DatabaseReader maxMindReader = null;
    private File databaseFile;

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

        Schedulers.async().run(this::checkDatabase);

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

    public DatabaseReader getMaxMindReader() {
        return maxMindReader;
    }

    public SimpleDateFormat getFormat() {
        return format;
    }

    public static DomainTracker getInstance() {
        return plugin;
    }

    private void checkDatabase() {
        databaseFile = new File(getDataFolder(), "GeoIP2-Country.mmdb");

        if (!databaseFile.exists()) {
            if (getConfig().getBoolean("database.download-if-missing", true)) {
                downloadDatabase();
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "Cannot find GeoIP database.");
                return;
            }
        } else if (getConfig().getBoolean("database.update.enabled", true)) {
            final long diff = new Date().getTime() - databaseFile.lastModified();
            if (diff / 24 / 3600 / 1000 > getConfig().getLong("database.update.every-x-days", 30)) {
                downloadDatabase();
            }
        }
        try {
            maxMindReader = new DatabaseReader.Builder(databaseFile).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadDatabase() {
        try {
            String url = getConfig().getString("database.download-url", null);

            if (url == null || url.isEmpty()) {
                Bukkit.getLogger().log(Level.SEVERE, "GeoIP url empty.");
                return;
            }

            final String licenseKey = getConfig().getString("database.license-key", "");
            if (licenseKey == null || licenseKey.isEmpty()) {
                Bukkit.getLogger().log(Level.SEVERE, "GeoIP license missing.");
                return;
            }

            url = url.replace("{LICENSEKEY}", licenseKey);
            Bukkit.getLogger().log(Level.INFO, "Downloading GeoIP database.");

            final URL downloadUrl = new URL(url);
            final URLConnection connection = downloadUrl.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();

            InputStream input = connection.getInputStream();
            final OutputStream output = new FileOutputStream(databaseFile);
            final byte[] buffer = new byte[2048];

            if (url.contains("gz")) {
                input = new GZIPInputStream(input);
                if (url.contains("tar.gz")) {
                    String filename;
                    final TarInputStream tarInputStream = new TarInputStream(input);
                    TarEntry entry;
                    while ((entry = tarInputStream.getNextEntry()) != null) {
                        if (!entry.isDirectory()) {
                            filename = entry.getName();
                            if (filename.substring(filename.length() - 5).equalsIgnoreCase(".mmdb")) {
                                input = tarInputStream;
                                break;
                            }
                        }
                    }
                }
            }
            int length = input.read(buffer);
            while (length >= 0) {
                output.write(buffer, 0, length);
                length = input.read(buffer);
            }
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
