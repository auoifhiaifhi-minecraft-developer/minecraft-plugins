package dev.auoifhiaifhi.welcomekit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Objects;

public final class WelcomeKitPlugin extends JavaPlugin implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private KitService kits;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        kits = new KitService(this);

        getServer().getPluginManager().registerEvents(new JoinListener(this, kits), this);
        Objects.requireNonNull(getCommand("kit"), "comando ausente no plugin.yml").setExecutor(this);
        Objects.requireNonNull(getCommand("welcomekit"), "comando ausente no plugin.yml").setExecutor(this);
        getLogger().info("WelcomeKit ativado.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        if (name.equals("kit")) {
            return claimKit(sender);
        }
        if (name.equals("welcomekit")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                kits.reloadItems();
                sender.sendMessage(msg("reload"));
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean claimKit(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg("only-players"));
            return true;
        }
        long left = kits.remainingSeconds(player.getUniqueId());
        if (left > 0) {
            player.sendMessage(msg("kit-cooldown", Placeholder.unparsed("time", formatTime(left))));
            return true;
        }
        kits.give(player);
        kits.markClaimed(player.getUniqueId());
        player.sendMessage(msg("kit-received"));
        return true;
    }

    /** Lê uma mensagem do config.yml (com prefixo) e converte de MiniMessage. */
    public Component msg(String key, TagResolver... resolvers) {
        String raw = getConfig().getString("prefix", "")
                + getConfig().getString("messages." + key, "<red>Mensagem ausente: " + key);
        return MM.deserialize(raw, resolvers);
    }

    private static String formatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0) {
            return h + "h " + m + "min";
        }
        if (m > 0) {
            return m + "min " + s + "s";
        }
        return s + "s";
    }
}
