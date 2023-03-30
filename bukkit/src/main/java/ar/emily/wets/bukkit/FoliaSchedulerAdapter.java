package ar.emily.wets.bukkit;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

final class FoliaSchedulerAdapter {

  private static final boolean IS_FOLIA;

  static {
    boolean isFolia;

    try {
      Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
      isFolia = true;
    } catch (final ClassNotFoundException ex) {
      isFolia = false;
    }

    IS_FOLIA = isFolia;
  }

  static Consumer<Runnable> scheduleGlobal(final Plugin plugin, final long initialDelay, final long period) {
    final Server server = plugin.getServer();
    return IS_FOLIA ?
        (task -> server.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), initialDelay, period)) :
        (task -> server.getScheduler().runTaskTimer(plugin, task, initialDelay, period));
  }

  static void runAt(final Plugin plugin, final Location pos, final Runnable task) {
    final Server server = plugin.getServer();
    if (!IS_FOLIA || server.isOwnedByCurrentRegion(pos)) {
      task.run();
    } else {
      server.getRegionScheduler().execute(plugin, pos, task);
    }
  }

  private FoliaSchedulerAdapter() {
  }
}
