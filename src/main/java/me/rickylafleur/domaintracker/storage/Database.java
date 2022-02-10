package me.rickylafleur.domaintracker.storage;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import me.rickylafleur.domaintracker.DomainTracker;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;
import java.util.UUID;

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

    public String getCountryFromIp(String ip) {
        try (WebServiceClient client = new WebServiceClient.Builder(674567, "Idmm6uSvBSOjmKQE")
                .build()) {

            InetAddress ipAddress = InetAddress.getByName(ip);
            CountryResponse response = client.country(ipAddress);

            return response.getCountry().getName();
        } catch (IOException | GeoIp2Exception e) {
            e.printStackTrace();
        }

        return "N/A";
    }
}
