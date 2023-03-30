package ar.emily.wets.bukkit;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

final class SchedulerAdapter {

  private static final Instance INSTANCE = get();

  static Consumer<Runnable> scheduleGlobal(final Plugin plugin, final long initialDelay, final long period) {
    return INSTANCE.scheduleGlobal(plugin, initialDelay, period);
  }

  static void runAt(final Plugin plugin, final Location pos, final Runnable task) {
    INSTANCE.runAt(plugin, pos, task);
  }

  private static Instance get() {
    try {
      Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
      return new FoliaSchedulerAdapter();
    } catch (final ClassNotFoundException ex) {
      return new BukkitSchedulerAdapter();
    }
  }

  private SchedulerAdapter() {
  }

  interface Instance {

    Consumer<Runnable> scheduleGlobal(Plugin plugin, long initialDelay, long period);
    void runAt(Plugin plugin, Location pos, Runnable task);
  }
}
