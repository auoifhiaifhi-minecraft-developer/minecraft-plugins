package dev.auoifhiaifhi.simplehomes;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class HomeCommands implements CommandExecutor, TabCompleter {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_-]{1,16}");

    private final SimpleHomesPlugin plugin;
    private final HomeManager homes;
    private final Set<UUID> pending = new HashSet<>();

    public HomeCommands(SimpleHomesPlugin plugin, HomeManager homes) {
        this.plugin = plugin;
        this.homes = homes;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.msg("only-players"));
            return true;
        }
        switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "sethome" -> setHome(player, args);
            case "home" -> goHome(player, args);
            case "delhome" -> delHome(player, args);
            case "homes" -> listHomes(player);
            default -> { }
        }
        return true;
    }

    private void setHome(Player player, String[] args) {
        String name = args.length > 0 ? args[0] : "home";
        if (!VALID_NAME.matcher(name).matches()) {
            player.sendMessage(plugin.msg("invalid-name"));
            return;
        }
        name = name.toLowerCase(Locale.ROOT);

        UUID id = player.getUniqueId();
        int max = plugin.getConfig().getInt("max-homes", 3);
        boolean exists = homes.get(id, name) != null;
        if (!exists && homes.count(id) >= max && !player.hasPermission("simplehomes.unlimited")) {
            player.sendMessage(plugin.msg("home-limit", "max", String.valueOf(max)));
            return;
        }

        homes.set(id, name, Home.of(player.getLocation()));
        player.sendMessage(plugin.msg("home-set", "name", name));
    }

    private void goHome(Player player, String[] args) {
        String rawName = args.length > 0 ? args[0] : "home";
        if (!VALID_NAME.matcher(rawName).matches()) {
            player.sendMessage(plugin.msg("invalid-name"));
            return;
        }
        final String name = rawName.toLowerCase(Locale.ROOT);

        Home home = homes.get(player.getUniqueId(), name);
        if (home == null) {
            player.sendMessage(plugin.msg("home-not-found", "name", name));
            return;
        }
        final Location target = home.toLocation();
        if (target == null) {
            player.sendMessage(plugin.msg("world-missing"));
            return;
        }

        int delay = plugin.getConfig().getInt("teleport-delay-seconds", 3);
        if (delay <= 0 || player.hasPermission("simplehomes.bypass-delay")) {
            teleport(player, target, name);
            return;
        }

        final UUID id = player.getUniqueId();
        if (!pending.add(id)) {
            player.sendMessage(plugin.msg("pending"));
            return;
        }

        final Location start = player.getLocation();
        player.sendMessage(plugin.msg("warmup", "seconds", String.valueOf(delay)));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pending.remove(id);
            if (!player.isOnline()) {
                return;
            }
            if (movedBlock(start, player.getLocation())) {
                player.sendMessage(plugin.msg("cancelled"));
                return;
            }
            teleport(player, target, name);
        }, delay * 20L);
    }

    private void delHome(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(plugin.msg("usage-delhome"));
            return;
        }
        if (!VALID_NAME.matcher(args[0]).matches()) {
            player.sendMessage(plugin.msg("invalid-name"));
            return;
        }
        String name = args[0].toLowerCase(Locale.ROOT);
        if (homes.remove(player.getUniqueId(), name)) {
            player.sendMessage(plugin.msg("home-deleted", "name", name));
        } else {
            player.sendMessage(plugin.msg("home-not-found", "name", name));
        }
    }

    private void listHomes(Player player) {
        var names = homes.names(player.getUniqueId());
        if (names.isEmpty()) {
            player.sendMessage(plugin.msg("no-homes"));
            return;
        }
        player.sendMessage(plugin.msg("list", "list", String.join(", ", names)));
    }

    private void teleport(Player player, Location target, String name) {
        player.teleportAsync(target);
        player.sendMessage(plugin.msg("teleported", "name", name));
    }

    private static boolean movedBlock(Location a, Location b) {
        return a.getWorld() != b.getWorld()
                || a.getBlockX() != b.getBlockX()
                || a.getBlockY() != b.getBlockY()
                || a.getBlockZ() != b.getBlockZ();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || args.length != 1) {
            return List.of();
        }
        String cmd = command.getName().toLowerCase(Locale.ROOT);
        if (!cmd.equals("home") && !cmd.equals("delhome")) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return homes.names(player.getUniqueId()).stream()
                .filter(n -> n.startsWith(prefix))
                .toList();
    }
}
