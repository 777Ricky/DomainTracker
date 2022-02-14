package me.rickylafleur.domaintracker.storage;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import me.lucko.helper.sql.DatabaseCredentials;
import me.lucko.helper.sql.plugin.HelperSql;
import me.rickylafleur.domaintracker.DomainTracker;
import me.rickylafleur.domaintracker.storage.objects.JoinData;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Ricky Lafleur
 */
public class Database {

    private final DomainTracker plugin;

    private HelperSql helperSql;

    public Database(DomainTracker plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        helperSql = new HelperSql(DatabaseCredentials.fromConfig(plugin.getConfig().getConfigurationSection("mysql")));

        createTable();
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                helperSql.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        try {
            helperSql.getConnection();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Connection getConnection() {
        try {
            return helperSql.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addData(String date, String uuid, String domain, String country) {
        if (playerExists(UUID.fromString(uuid))) return;

        helperSql.executeAsync("INSERT INTO domain_tracker (DATE,UUID,DOMAIN,COUNTRY) VALUES (?,?,?,?);", ps -> {
            ps.setString(1, date);
            ps.setString(2, uuid);
            ps.setString(3, domain);
            ps.setString(4, country);
        });
    }

    public Set<JoinData> getJoinData() {
        return helperSql.queryAsync("SELECT * FROM domain_tracker;", rs -> {
            Set<JoinData> joinDataSet = new HashSet<>();

            while (rs.next()) {
                joinDataSet.add(new JoinData(
                        rs.getString("DATE"),
                        UUID.fromString(rs.getString("UUID")),
                        rs.getString("DOMAIN"),
                        rs.getString("COUNTRY")
                ));
            }

            return joinDataSet;
        }).join().orElse(Collections.emptySet());
    }

    public Set<JoinData> getJoinData(String date) {
        return helperSql.queryAsync("SELECT * FROM domain_tracker WHERE DATE = ?;", ps -> ps.setString(1, date), rs -> {
            Set<JoinData> joinDataSet = new HashSet<>();

            while (rs.next()) {
                joinDataSet.add(new JoinData(
                        rs.getString("DATE"),
                        UUID.fromString(rs.getString("UUID")),
                        rs.getString("DOMAIN"),
                        rs.getString("COUNTRY")
                ));
            }
            return joinDataSet;
        }).join().orElse(Collections.emptySet());
    }

    public boolean playerExists(UUID uuid) {
        return helperSql.queryAsync("SELECT * FROM domain_tracker WHERE `UUID` = ?;", ps -> ps.setString(1, uuid.toString()), ResultSet::next).join().orElse(false);
    }

    public String getCountryFromIp(InetAddress address) {
        try {
            CountryResponse response = plugin.getMaxMindReader().country(address);

            return response.getCountry().getName();
        } catch (IOException | GeoIp2Exception e) {
            Bukkit.getLogger().log(Level.INFO, "IP not found in database defaulting to Unknown");
        }

        return "Unknown";
    }

    private void createTable() {
        helperSql.executeAsync("CREATE TABLE IF NOT EXISTS `domain_tracker` (`DATE` char(10), `UUID` char(36), `DOMAIN` char(50), `COUNTRY` char(50));");
    }
}
