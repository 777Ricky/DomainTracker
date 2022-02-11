package me.rickylafleur.domaintracker.storage;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import me.lucko.helper.Schedulers;
import me.rickylafleur.domaintracker.DomainTracker;
import me.rickylafleur.domaintracker.storage.objects.JoinData;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Ricky Lafleur
 */
public class Database {

    private final DomainTracker plugin;

    private Connection connection;

    public Database(DomainTracker plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        if (!isConnected()) {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + plugin.getConfig().getString("mysql.address") + ":" + plugin.getConfig().getInt("mysql.port") + "/" + plugin.getConfig().getString("mysql.database") + "?useSSL=" + plugin.getConfig().getBoolean("mysql.useSSL"),
                    plugin.getConfig().getString("mysql.username"),
                    plugin.getConfig().getString("mysql.password")
            );

            Schedulers.async().run(this::createTable);
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    public Connection getConnection() {
        return connection;
    }

    public void addData(String date, String uuid, String domain, String country) {
        if (playerExists(UUID.fromString(uuid))) return;

        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO domain_tracker (DATE,UUID,DOMAIN,COUNTRY) VALUES (?,?,?,?);");

            ps.setString(1, date);
            ps.setString(2, uuid);
            ps.setString(3, domain);
            ps.setString(4, country);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, JoinData> getJoinData(String date) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM domain_tracker WHERE DATE = ?;");
            ps.setString(1, date);

            ResultSet rs = ps.executeQuery();

            if (ps.isClosed()) return Collections.emptyMap();

            Map<String, JoinData> joinDataMap = new HashMap<>();

            while (rs.next()) {
                joinDataMap.put(date, new JoinData(
                        rs.getString("DATE"),
                        UUID.fromString(rs.getString("UUID")),
                        rs.getString("DOMAIN"),
                        rs.getString("COUNTRY")
                ));
            }

            ps.close();
            rs.close();

            return joinDataMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyMap();
    }

    public boolean playerExists(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM domain_tracker WHERE UUID = ?;");
            ps.setString(1, uuid.toString());

            ResultSet rs = ps.executeQuery();

            if (ps.isClosed() || !rs.next()) return false;

            ps.close();
            rs.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getCountryFromIp(InetAddress address) {
        try {
            CountryResponse response = plugin.getMaxMindReader().country(address);

            return response.getCountry().getName();
        } catch (IOException | GeoIp2Exception e) {
            Bukkit.getLogger().log(Level.INFO, "IP not found in database defaulting to N/A");
        }

        return "N/A";
    }

    private void createTable() {
        try {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS domain_tracker (`DATE` char(10), `UUID` char(36), `DOMAIN` char(50), `COUNTRY` char(50));");
            ps.executeUpdate();

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
