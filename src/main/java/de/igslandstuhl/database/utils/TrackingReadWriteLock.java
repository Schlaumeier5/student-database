package de.igslandstuhl.database.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TrackingReadWriteLock implements ReadWriteLock {
    private final ReadWriteLock intern = new ReentrantReadWriteLock(true);
    private final Set<Thread> readingThreads = new HashSet<>();
    private Thread writingThread = null;

    private final ReadLock readLock = new ReadLock();
    private final WriteLock writeLock = new WriteLock();

    private class ReadLock implements Lock {
        @Override
        public void lock() {
            intern.readLock().lock();
            readingThreads.add(Thread.currentThread());
        }
        @Override
        public void lockInterruptibly() throws InterruptedException {
            intern.readLock().lockInterruptibly();
            readingThreads.add(Thread.currentThread());
        }
        @Override
        public boolean tryLock() {
            if (intern.readLock().tryLock()) {
                readingThreads.add(Thread.currentThread());
                return true;
            } else {
                return false;
            }
        }
        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            if (intern.readLock().tryLock(time, unit)) {
                readingThreads.add(Thread.currentThread());
                return true;
            } else {
                return false;
            }
        }
        @Override
        public void unlock() {
            readingThreads.remove(Thread.currentThread());
            intern.readLock().unlock();
        }
        @Override
        public Condition newCondition() {
            return intern.readLock().newCondition();
        }
    }
    private class WriteLock implements Lock {
        @Override
        public void lock() {
            intern.writeLock().lock();
            writingThread = Thread.currentThread();
        }
        @Override
        public void lockInterruptibly() throws InterruptedException {
            intern.writeLock().lockInterruptibly();
            writingThread = Thread.currentThread();
        }
        @Override
        public boolean tryLock() {
            if (intern.writeLock().tryLock()) {
                writingThread = Thread.currentThread();
                return true;
            } else {
                return false;
            }
        }
        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            if (intern.writeLock().tryLock(time, unit)) {
                writingThread = Thread.currentThread();
                return true;
            } else {
                return false;
            }
        }
        @Override
        public void unlock() {
            writingThread = null;
            intern.writeLock().unlock();
        }
        @Override
        public Condition newCondition() {
            return intern.writeLock().newCondition();
        }
    }

    @Override
    public Lock readLock() {
        return readLock;
    }
    @Override
    public Lock writeLock() {
        return writeLock;
    }
    public void interruptReadingThreads() {
        for (Thread thread : readingThreads) {
            thread.interrupt();
        }
    }
    public void interruptWritingThread() {
        if (writingThread != null) {
            writingThread.interrupt();
        }
    }
    public void interruptAll() {
        interruptReadingThreads();
        interruptWritingThread();
    }
}
