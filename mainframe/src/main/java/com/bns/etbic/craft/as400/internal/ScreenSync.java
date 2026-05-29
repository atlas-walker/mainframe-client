package com.bns.etbic.craft.as400.internal;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.tn5250j.event.ScreenListener;
import org.tn5250j.framework.tn5250.Screen5250;

/**
 * Tracks host repaints by listening to the emulator and recording when the screen
 * last changed, so callers can block until the host responds instead of sleeping.
 *
 * <p>This is internal plumbing used by {@link com.bns.etbic.craft.as400.As400Driver};
 * it is not part of the public API.
 *
 * @author Andres Acosta
 * @since 1.0.14
 */
public final class ScreenSync implements ScreenListener {

    private final Screen5250 screen;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition changed = lock.newCondition();
    private volatile long lastChangeNanos = System.nanoTime();

    /**
     * Attaches to the given screen and starts tracking its repaints.
     *
     * @param screen the emulator screen to observe
     */
    public ScreenSync(Screen5250 screen) {
        this.screen = screen;
        screen.addScreenListener(this);
    }

    /** Stops observing the screen. */
    public void detach() {
        screen.removeScreenListener(this);
    }

    /**
     * Returns when the screen last changed.
     *
     * @return the {@link System#nanoTime()} timestamp of the last screen change
     */
    public long lastChangeNanos() {
        return lastChangeNanos;
    }

    /**
     * Waits for the next screen change.
     *
     * @param timeoutMs the maximum time to wait, in milliseconds
     * @return {@code true} if a change was signalled before the timeout
     */
    public boolean awaitChange(long timeoutMs) {
        lock.lock();
        try {
            return changed.await(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Waits until the screen changes after the given timestamp, returning immediately
     * if it already has.
     *
     * @param sinceNanos the baseline timestamp from a prior {@link #lastChangeNanos()}
     * @param timeoutMs  the maximum time to wait, in milliseconds
     * @return {@code true} if a change after {@code sinceNanos} was observed in time
     */
    public boolean awaitChangeSince(long sinceNanos, long timeoutMs) {
        long deadline = System.nanoTime() + timeoutMs * 1_000_000L;
        lock.lock();
        try {
            while (lastChangeNanos <= sinceNanos) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) return false;
                changed.await(remaining, java.util.concurrent.TimeUnit.NANOSECONDS);
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onScreenChanged(int inUpdate, int startRow, int startCol, int endRow, int endCol) {
        lock.lock();
        try {
            lastChangeNanos = System.nanoTime();
            changed.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onScreenSizeChanged(int rows, int cols) {
        lock.lock();
        try {
            lastChangeNanos = System.nanoTime();
            changed.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
