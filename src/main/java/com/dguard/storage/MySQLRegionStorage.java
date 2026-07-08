package com.dguard.storage;

import com.dguard.model.Region;
import com.dguard.model.RegionFlag;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MySQLRegionStorage implements RegionStorage {

    private final JavaPlugin plugin;
    private final String url;
    private final String username;
    private final String password;

    private Connection connection;

    public MySQLRegionStorage(JavaPlugin plugin, String host, int port, String database,
                               String username, String password, boolean useSSL) {
        this.plugin = plugin;
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + useSSL + "&allowPublicKeyRetrieval=true&autoReconnect=true";
        this.username = username;
        this.password = password;
    }

    @Override
    public void init() throws Exception {
        Class.forName(com.mysql.cj.jdbc.Driver.class.getName());
        connect();
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS dguard_regions (" +
                    "name_lower VARCHAR(32) PRIMARY KEY," +
                    "display_name VARCHAR(32) NOT NULL," +
                    "world VARCHAR(64) NOT NULL," +
                    "min_x INT NOT NULL, min_y INT NOT NULL, min_z INT NOT NULL," +
                    "max_x INT NOT NULL, max_y INT NOT NULL, max_z INT NOT NULL," +
                    "priority INT NOT NULL DEFAULT 0," +
                    "created_by VARCHAR(16)," +
                    "created_at BIGINT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS dguard_region_flags (" +
                    "region VARCHAR(32) NOT NULL," +
                    "flag VARCHAR(32) NOT NULL," +
                    "allowed BOOLEAN NOT NULL," +
                    "PRIMARY KEY (region, flag))");
        }
    }

    private void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
        }
    }

    @Override
    public Map<String, Region> loadRegions() throws SQLException {
        Map<String, Region> result = new HashMap<>();
        connect();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM dguard_regions")) {
            while (rs.next()) {
                Region region = new Region(
                        rs.getString("display_name"),
                        rs.getString("world"),
                        rs.getInt("min_x"), rs.getInt("min_y"), rs.getInt("min_z"),
                        rs.getInt("max_x"), rs.getInt("max_y"), rs.getInt("max_z"),
                        rs.getInt("priority"),
                        rs.getString("created_by"),
                        rs.getLong("created_at"));
                result.put(rs.getString("name_lower"), region);
            }
        }

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT region, flag, allowed FROM dguard_region_flags")) {
            while (rs.next()) {
                Region region = result.get(rs.getString("region"));
                if (region == null) continue;
                RegionFlag flag = RegionFlag.fromInput(rs.getString("flag"));
                if (flag != null) {
                    region.setFlag(flag, rs.getBoolean("allowed"));
                }
            }
        }

        return result;
    }

    @Override
    public void saveRegion(Region region) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                connect();
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO dguard_regions (name_lower, display_name, world, min_x, min_y, min_z, " +
                                "max_x, max_y, max_z, priority, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE display_name = VALUES(display_name), priority = VALUES(priority)")) {
                    ps.setString(1, region.getNameLower());
                    ps.setString(2, region.getName());
                    ps.setString(3, region.getWorld());
                    ps.setInt(4, region.getMinX());
                    ps.setInt(5, region.getMinY());
                    ps.setInt(6, region.getMinZ());
                    ps.setInt(7, region.getMaxX());
                    ps.setInt(8, region.getMaxY());
                    ps.setInt(9, region.getMaxZ());
                    ps.setInt(10, region.getPriority());
                    ps.setString(11, region.getCreatedBy());
                    ps.setLong(12, region.getCreatedAt());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement("DELETE FROM dguard_region_flags WHERE region = ?")) {
                    ps.setString(1, region.getNameLower());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO dguard_region_flags (region, flag, allowed) VALUES (?, ?, ?)")) {
                    for (Map.Entry<RegionFlag, Boolean> entry : region.getFlags().entrySet()) {
                        ps.setString(1, region.getNameLower());
                        ps.setString(2, entry.getKey().getId());
                        ps.setBoolean(3, entry.getValue());
                        ps.addBatch();
                    }
                    if (!region.getFlags().isEmpty()) ps.executeBatch();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Erreur MySQL lors de la sauvegarde de la région " + region.getName(), e);
            }
        });
    }

    @Override
    public void deleteRegion(String nameLower) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                connect();
                try (PreparedStatement ps = connection.prepareStatement("DELETE FROM dguard_region_flags WHERE region = ?")) {
                    ps.setString(1, nameLower);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement("DELETE FROM dguard_regions WHERE name_lower = ?")) {
                    ps.setString(1, nameLower);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Erreur MySQL lors de la suppression de la région " + nameLower, e);
            }
        });
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erreur lors de la fermeture de la connexion MySQL", e);
        }
    }
}
