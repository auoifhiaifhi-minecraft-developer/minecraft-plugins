package dev.auoifhiaifhi.tpsinfo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;
import java.util.Objects;

public final class TpsInfoPlugin extends JavaPlugin implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private BukkitTask alertTask;
    private long lastAlert;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Objects.requireNonNull(getCommand("tpsinfo"), "comando ausente no plugin.yml").setExecutor(this);
        startAlerts();
        getLogger().info("TpsInfo ativado.");
    }

    @Override
    public void onDisable() {
        stopAlerts();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("tpsinfo.reload")) {
                sender.sendMessage(msg("no-permission"));
                return true;
            }
            reloadConfig();
            stopAlerts();
            startAlerts();
            sender.sendMessage(msg("reload"));
            return true;
        }
        sendReport(sender);
        return true;
    }

    private void sendReport(CommandSender sender) {
        double[] tps = Bukkit.getTPS();
        double mspt = Bukkit.getAverageTickTime();
        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        long max = rt.maxMemory();

        sender.sendMessage(MM.deserialize("<dark_gray>----- <green>TpsInfo</green> -----"));
        sender.sendMessage(MM.deserialize("<gray>TPS (1m/5m/15m): "
                + fmtTps(tps[0]) + " <dark_gray>/</dark_gray> "
                + fmtTps(tps[1]) + " <dark_gray>/</dark_gray> "
                + fmtTps(tps[2])));
        sender.sendMessage(MM.deserialize("<gray>MSPT médio: <white>"
                + String.format(Locale.ROOT, "%.1f", mspt) + " ms"));
        sender.sendMessage(MM.deserialize("<gray>Memória: <white>" + mb(used) + " MB</white> / <white>"
                + mb(max) + " MB</white> (" + (used * 100 / max) + "%)"));

        for (World world : Bukkit.getWorlds()) {
            sender.sendMessage(MM.deserialize("<gray>" + MM.escapeTags(world.getName())
                    + ": <white>" + world.getChunkCount() + "</white> chunks, <white>"
                    + world.getEntityCount() + "</white> entidades, <white>"
                    + world.getPlayerCount() + "</white> jogadores"));
        }
    }

    private void startAlerts() {
        if (!getConfig().getBoolean("alert.enabled", true)) {
            return;
        }
        long period = Math.max(5L, getConfig().getLong("alert.check-interval-seconds", 30L)) * 20L;
        alertTask = getServer().getScheduler().runTaskTimer(this, this::checkTps, period, period);
    }

    private void stopAlerts() {
        if (alertTask != null) {
            alertTask.cancel();
            alertTask = null;
        }
    }

    private void checkTps() {
        double tps = Math.min(20.0, Bukkit.getTPS()[0]);
        if (tps >= getConfig().getDouble("alert.threshold", 17.0)) {
            return;
        }
        long now = System.currentTimeMillis();
        long cooldown = getConfig().getLong("alert.cooldown-seconds", 300L) * 1000L;
        if (now - lastAlert < cooldown) {
            return;
        }
        lastAlert = now;

        String text = String.format(Locale.ROOT, "%.1f", tps);
        getLogger().warning("TPS baixo: " + text);
        Component alert = msg("alert", "tps", text);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("tpsinfo.alert")) {
                player.sendMessage(alert);
            }
        }
    }

    private Component msg(String key, String... pairs) {
        String raw = getConfig().getString("prefix", "")
                + getConfig().getString("messages." + key, "<red>Mensagem ausente: " + key);
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            raw = raw.replace("%" + pairs[i] + "%", pairs[i + 1]);
        }
        return MM.deserialize(raw);
    }

    private static String fmtTps(double tps) {
        double value = Math.min(20.0, tps);
        String color = value >= 18.0 ? "green" : value >= 15.0 ? "yellow" : "red";
        return "<" + color + ">" + String.format(Locale.ROOT, "%.1f", value) + "</" + color + ">";
    }

    private static long mb(long bytes) {
        return bytes / (1024L * 1024L);
    }
}
