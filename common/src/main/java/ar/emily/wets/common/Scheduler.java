package ar.emily.wets.common;

import com.sk89q.worldedit.util.Location;

import java.util.function.Consumer;

public interface Scheduler {

  void flush();
  void runPeriodically(Consumer<Task> action, long initialDelay, long period);
  void runAt(Location pos, Runnable action);

  @FunctionalInterface
  interface Task {

    void cancel();
  }
}
