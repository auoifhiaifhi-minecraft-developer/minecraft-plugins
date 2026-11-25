package dev.auoifhiaifhi.simplehomes;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

/** Guarda as casas em memória e as salva em plugins/SimpleHomes/homes.yml. */
public final class HomeManager {

    private final SimpleHomesPlugin plugin;
    private final File file;
    private final Map<UUID, Map<String, Home>> data = new HashMap<>();

    public HomeManager(SimpleHomesPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "homes.yml");
    }

    public void load() {
        data.clear();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yml.getConfigurationSection("homes");
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            UUID id;
            try {
                id = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            ConfigurationSection player = root.getConfigurationSection(key);
            if (player == null) {
                continue;
            }
            Map<String, Home> homes = new TreeMap<>();
            for (String name : player.getKeys(false)) {
                ConfigurationSection h = player.getConfigurationSection(name);
                if (h == null) {
                    continue;
                }
                homes.put(name, new Home(
                        h.getString("world", "world"),
                        h.getDouble("x"), h.getDouble("y"), h.getDouble("z"),
                        (float) h.getDouble("yaw"), (float) h.getDouble("pitch")));
            }
            data.put(id, homes);
        }
    }

    public void save() {
        YamlConfiguration yml = new YamlConfiguration();
        data.forEach((id, homes) -> homes.forEach((name, h) -> {
            String path = "homes." + id + "." + name + ".";
            yml.set(path + "world", h.world());
            yml.set(path + "x", h.x());
            yml.set(path + "y", h.y());
            yml.set(path + "z", h.z());
            yml.set(path + "yaw", h.yaw());
            yml.set(path + "pitch", h.pitch());
        }));
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Não foi possível salvar homes.yml", e);
        }
    }

    public Home get(UUID id, String name) {
        Map<String, Home> homes = data.get(id);
        return homes == null ? null : homes.get(name);
    }

    public void set(UUID id, String name, Home home) {
        data.computeIfAbsent(id, k -> new TreeMap<>()).put(name, home);
        save();
    }

    public boolean remove(UUID id, String name) {
        Map<String, Home> homes = data.get(id);
        if (homes == null || homes.remove(name) == null) {
            return false;
        }
        save();
        return true;
    }

    public Set<String> names(UUID id) {
        Map<String, Home> homes = data.get(id);
        return homes == null ? Set.of() : Collections.unmodifiableSet(homes.keySet());
    }

    public int count(UUID id) {
        return names(id).size();
    }
}
