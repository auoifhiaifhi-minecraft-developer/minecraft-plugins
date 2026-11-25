package dev.auoifhiaifhi.welcomekit;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/** Lê o kit do config.yml, entrega os itens e controla o tempo de espera de cada jogador. */
public final class KitService {

    private final WelcomeKitPlugin plugin;
    private final File file;
    private final YamlConfiguration data;
    private final List<ItemStack> items = new ArrayList<>();

    public KitService(WelcomeKitPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        reloadItems();
    }

    public void reloadItems() {
        items.clear();
        for (String entry : plugin.getConfig().getStringList("kit.items")) {
            String[] parts = entry.split(":");
            Material material = Material.matchMaterial(parts[0].trim());
            if (material == null || !material.isItem()) {
                plugin.getLogger().warning("Item inválido no kit: " + entry);
                continue;
            }
            int amount = 1;
            if (parts.length > 1) {
                try {
                    amount = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Quantidade inválida no kit: " + entry);
                    continue;
                }
            }
            amount = Math.max(1, Math.min(amount, material.getMaxStackSize()));
            items.add(new ItemStack(material, amount));
        }
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    /** Entrega os itens; o que não couber no inventário cai aos pés do jogador. */
    public void give(Player player) {
        for (ItemStack item : items) {
            player.getInventory().addItem(item.clone()).values()
                    .forEach(rest -> player.getWorld().dropItemNaturally(player.getLocation(), rest));
        }
    }

    /** Segundos que faltam para o jogador poder pegar o kit de novo (0 = já pode). */
    public long remainingSeconds(UUID id) {
        long cooldown = plugin.getConfig().getLong("kit.cooldown-seconds", 86400L);
        long last = data.getLong("last-claim." + id, 0L);
        long left = (last + cooldown * 1000L - System.currentTimeMillis()) / 1000L;
        return Math.max(0L, left);
    }

    public void markClaimed(UUID id) {
        data.set("last-claim." + id, System.currentTimeMillis());
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Não foi possível salvar data.yml", e);
        }
    }
}
