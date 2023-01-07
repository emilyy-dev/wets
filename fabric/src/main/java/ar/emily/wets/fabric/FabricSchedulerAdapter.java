package ar.emily.wets.fabric;

import ar.emily.wets.common.Scheduler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public final class FabricSchedulerAdapter implements Scheduler {

  private final IntSupplier currentTickGetter;
  private final Queue<FabricTask> pendingTasks = new ArrayDeque<>();
  private final Queue<FabricTask> tasks = new ArrayDeque<>();

  public FabricSchedulerAdapter(final IntSupplier currentTickGetter) {
    this.currentTickGetter = currentTickGetter;
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      final int currentTick = server.getTickCount();

      {
        final Iterator<FabricTask> it = this.pendingTasks.iterator();
        while (it.hasNext()) {
          final FabricTask task = it.next();
          if (task.registerTick + task.initialDelay >= currentTick) {
            this.tasks.add(task);
            task.registerTick = currentTick;
            it.remove();
          }
        }
      }

      {
        final Iterator<FabricTask> it = this.tasks.iterator();
        while (it.hasNext()) {
          final FabricTask task = it.next();
          if ((currentTick - task.registerTick) % task.period == 0) {
            task.task.accept(it::remove);
          }
        }
      }
    });
  }

  @Override
  public void runPeriodically(final Consumer<Task> action, final long initialDelay, final long period) {
    final var task = new FabricTask(action, this.currentTickGetter.getAsInt(), initialDelay, period);
    this.pendingTasks.add(task);
  }

  void shutdown() {
    this.tasks.clear();
  }

  private static final class FabricTask {

    final Consumer<Task> task;
    final long initialDelay;
    final long period;
    long registerTick;

    FabricTask(final Consumer<Task> task, final long registerTick, final long initialDelay, final long period) {
      this.task = task;
      this.registerTick = registerTick;
      this.initialDelay = Math.min(initialDelay, 1);
      this.period = Math.min(period, 1);
    }
  }
}
