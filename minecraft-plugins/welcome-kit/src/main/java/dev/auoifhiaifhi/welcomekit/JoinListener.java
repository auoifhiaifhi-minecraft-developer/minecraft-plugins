package dev.auoifhiaifhi.welcomekit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;

public final class JoinListener implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final WelcomeKitPlugin plugin;
    private final KitService kits;

    public JoinListener(WelcomeKitPlugin plugin, KitService kits) {
        this.plugin = plugin;
        this.kits = kits;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration cfg = plugin.getConfig();
        boolean firstJoin = !player.hasPlayedBefore();
        var name = Placeholder.unparsed("player", player.getName());

        String join = cfg.getString("join-message", "");
        if (!join.isBlank()) {
            event.joinMessage(MM.deserialize(join, name));
        }

        if (firstJoin && cfg.getBoolean("kit.give-on-first-join", true) && kits.hasItems()) {
            kits.give(player);
            kits.markClaimed(player.getUniqueId());
            player.sendMessage(plugin.msg("kit-received"));
        }

        if (cfg.getBoolean("welcome.enabled", true) && (firstJoin || !cfg.getBoolean("welcome.first-join-only", false))) {
            Component title = MM.deserialize(cfg.getString("welcome.title", ""), name);
            Component subtitle = MM.deserialize(cfg.getString("welcome.subtitle", ""), name);
            player.showTitle(Title.title(title, subtitle,
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(800))));
            for (String line : cfg.getStringList("welcome.messages")) {
                player.sendMessage(MM.deserialize(line, name));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String quit = plugin.getConfig().getString("quit-message", "");
        if (!quit.isBlank()) {
            event.quitMessage(MM.deserialize(quit, Placeholder.unparsed("player", event.getPlayer().getName())));
        }
    }
}
