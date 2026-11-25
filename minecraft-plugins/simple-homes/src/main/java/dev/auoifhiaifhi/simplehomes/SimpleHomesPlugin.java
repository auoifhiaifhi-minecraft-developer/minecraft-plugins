package dev.auoifhiaifhi.simplehomes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SimpleHomesPlugin extends JavaPlugin {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private HomeManager homes;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        homes = new HomeManager(this);
        homes.load();

        HomeCommands commands = new HomeCommands(this, homes);
        for (String name : new String[] {"sethome", "home", "delhome", "homes"}) {
            PluginCommand command = Objects.requireNonNull(getCommand(name), "comando ausente no plugin.yml: " + name);
            command.setExecutor(commands);
            command.setTabCompleter(commands);
        }
        getLogger().info("SimpleHomes ativado.");
    }

    @Override
    public void onDisable() {
        if (homes != null) {
            homes.save();
        }
    }

    /**
     * Lê uma mensagem do config.yml, troca os %marcadores% e converte de MiniMessage.
     * Os valores trocados vêm sempre de nomes já validados ([A-Za-z0-9_-]), então não
     * dá para injetar formatação.
     */
    public Component msg(String key, String... pairs) {
        String raw = getConfig().getString("prefix", "")
                + getConfig().getString("messages." + key, "<red>Mensagem ausente: " + key);
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            raw = raw.replace("%" + pairs[i] + "%", pairs[i + 1]);
        }
        return MM.deserialize(raw);
    }
}
