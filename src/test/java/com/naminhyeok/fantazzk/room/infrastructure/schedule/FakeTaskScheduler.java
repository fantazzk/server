package com.naminhyeok.fantazzk.room.infrastructure.schedule;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

final class FakeTaskScheduler implements TaskScheduler {
    private final List<ScheduledTask> scheduledTasks = new ArrayList<>();

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        throw new UnsupportedOperationException("Trigger-based scheduling is not used in these tests");
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
        FakeScheduledFuture future = new FakeScheduledFuture(startTime);
        scheduledTasks.add(new ScheduledTask(task, startTime, future));
        return future;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
        throw new UnsupportedOperationException("Fixed-rate scheduling is not used in these tests");
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
        throw new UnsupportedOperationException("Fixed-rate scheduling is not used in these tests");
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
        throw new UnsupportedOperationException("Fixed-delay scheduling is not used in these tests");
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
        throw new UnsupportedOperationException("Fixed-delay scheduling is not used in these tests");
    }

    List<Instant> scheduledInstants() {
        return scheduledTasks.stream().map(ScheduledTask::scheduledAt).toList();
    }

    List<Instant> activeScheduledInstants() {
        return scheduledTasks.stream()
            .filter(task -> !task.future().isCancelled() && !task.future().isDone())
            .map(ScheduledTask::scheduledAt)
            .toList();
    }

    List<Instant> cancelledInstants() {
        return scheduledTasks.stream()
            .filter(task -> task.future().isCancelled())
            .map(ScheduledTask::scheduledAt)
            .toList();
    }

    void runLatest() {
        ScheduledTask latestTask =
            scheduledTasks.stream()
                .filter(task -> !task.future().isCancelled())
                .max(Comparator.comparing(ScheduledTask::scheduledAt))
                .orElseThrow(() -> new IllegalStateException("예약된 작업이 없습니다"));
        latestTask.task().run();
        latestTask.future().markDone();
    }

    private record ScheduledTask(Runnable task, Instant scheduledAt, FakeScheduledFuture future) {
    }

    private static final class FakeScheduledFuture implements ScheduledFuture<Object> {
        private final Instant scheduledAt;
        private boolean cancelled;
        private boolean done;

        private FakeScheduledFuture(Instant scheduledAt) {
            this.scheduledAt = Objects.requireNonNull(scheduledAt, "scheduledAt must not be null");
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(Duration.between(Instant.now(), scheduledAt));
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(getDelay(TimeUnit.NANOSECONDS), other.getDelay(TimeUnit.NANOSECONDS));
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            done = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        private void markDone() {
            done = true;
        }
    }
}
