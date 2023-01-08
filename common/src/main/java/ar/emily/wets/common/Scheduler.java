package ar.emily.wets.common;

import java.util.function.Consumer;

public interface Scheduler {

  void flush();
  void runPeriodically(Consumer<Task> action, long initialDelay, long period);

  @FunctionalInterface
  interface Task {

    void cancel();
  }
}
