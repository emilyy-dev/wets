package ar.emily.wets.common;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public final class AbstractScheduler implements Scheduler {

  private final IntSupplier currentTickGetter;
  private final Queue<AbstractTask> pendingTasks = new ArrayDeque<>();
  private final Queue<AbstractTask> tasks = new ArrayDeque<>();
  private final Consumer<Runnable> underlyingScheduler;

  public AbstractScheduler(final IntSupplier currentTickGetter, final Consumer<Runnable> underlyingScheduler) {
    this.currentTickGetter = currentTickGetter;
    this.underlyingScheduler = underlyingScheduler;
  }

  public void setup() {
    this.underlyingScheduler.accept(() -> {
      processPendingTasks();
      processTasks();
    });
  }

  @Override
  public void runPeriodically(final Consumer<Task> action, final long initialDelay, final long period) {
    this.pendingTasks.add(new AbstractTask(action, this.currentTickGetter.getAsInt(), initialDelay, period));
  }

  public void flush() {
    this.tasks.addAll(this.pendingTasks);
    this.pendingTasks.clear();

    final Iterator<AbstractTask> it = this.tasks.iterator();
    while (it.hasNext()) {
      final AbstractTask task = it.next();
      task.task.accept(it::remove);
    }

    this.tasks.clear();
  }

  private void processPendingTasks() {
    final int currentTick = this.currentTickGetter.getAsInt();
    final Iterator<AbstractTask> it = this.pendingTasks.iterator();
    while (it.hasNext()) {
      final AbstractTask task = it.next();
      if (task.registerTick + task.initialDelay >= currentTick) {
        this.tasks.add(task);
        task.registerTick = currentTick;
        it.remove();
      }
    }
  }

  private void processTasks() {
    final int currentTick = this.currentTickGetter.getAsInt();
    final Iterator<AbstractTask> it = this.tasks.iterator();
    while (it.hasNext()) {
      final AbstractTask task = it.next();
      if ((currentTick - task.registerTick) % task.period == 0) {
        task.task.accept(it::remove);
      }
    }
  }

  private static final class AbstractTask {

    final Consumer<Task> task;
    final long initialDelay;
    final long period;
    long registerTick;

    AbstractTask(final Consumer<Task> task, final long registerTick, final long initialDelay, final long period) {
      this.task = task;
      this.registerTick = registerTick;
      this.initialDelay = Math.min(initialDelay, 1);
      this.period = Math.min(period, 1);
    }
  }
}
