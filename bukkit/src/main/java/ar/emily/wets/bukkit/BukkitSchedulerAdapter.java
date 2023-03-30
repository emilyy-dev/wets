package ar.emily.wets.bukkit;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

final class BukkitSchedulerAdapter implements SchedulerAdapter.Instance {

  @Override
  public Consumer<Runnable> scheduleGlobal(final Plugin plugin, final long initialDelay, final long period) {
    return task -> plugin.getServer().getScheduler().runTaskTimer(plugin, task, initialDelay, period);
  }

  @Override
  public void runAt(final Plugin plugin, final Location pos, final Runnable task) {
    final Server server = plugin.getServer();
    if (server.isPrimaryThread()) {
      task.run();
    } else {
      server.getScheduler().runTask(plugin, task);
    }
  }
}
