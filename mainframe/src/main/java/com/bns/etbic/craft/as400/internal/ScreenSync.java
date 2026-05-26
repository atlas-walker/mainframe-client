package com.bns.etbic.craft.as400.internal;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.tn5250j.event.ScreenListener;
import org.tn5250j.framework.tn5250.Screen5250;

public final class ScreenSync implements ScreenListener {

    private final Screen5250 screen;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition changed = lock.newCondition();
    private volatile long lastChangeNanos = System.nanoTime();

    public ScreenSync(Screen5250 screen) {
        this.screen = screen;
        screen.addScreenListener(this);
    }

    public void detach() {
        screen.removeScreenListener(this);
    }

    public long lastChangeNanos() {
        return lastChangeNanos;
    }

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
