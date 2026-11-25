package dev.auoifhiaifhi.simplehomes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Uma casa salva. Guarda o nome do mundo (e não o objeto World) para que a casa
 * continue existindo mesmo se o mundo estiver descarregado no momento.
 */
public record Home(String world, double x, double y, double z, float yaw, float pitch) {

    public static Home of(Location loc) {
        return new Home(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /** Retorna null se o mundo não estiver carregado. */
    public Location toLocation() {
        World w = Bukkit.getWorld(world);
        return w == null ? null : new Location(w, x, y, z, yaw, pitch);
    }
}
