/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;


/** The abstract class that provides a thread-safe caching mechanism for value production.
 * @implNote Subclasses should implement the {@code produce()} method to provide the actual value.
 * @param <T> The type of the produced and cached value.
 */
public abstract class Cached<T> {
    protected T cachedValue;
    protected long cacheAgeNanos;
    protected long cacheTimestampNanos;
    private final Object lock;

    public Cached() {
        cachedValue = null;
        cacheAgeNanos = 0L;
        cacheTimestampNanos = 0L;
        lock = new Object();
    }

    /** Returns the cached value. If the cache has expired or is empty,
     * a new value is produced using {@code produce()}.
     * @return The cached or newly produced value.
     */
    public T get() {
        if (cachedValue == null || System.nanoTime() > cacheAgeNanos + cacheTimestampNanos) {
            synchronized (lock) {
                long now = System.nanoTime();
                if (cachedValue == null || now > cacheAgeNanos + cacheAgeNanos) {
                    cachedValue = produce();
                    cacheTimestampNanos = now;
                }
            }
        }
        return cachedValue;
    }

    /** Resets the cache immediately.
     */
    public void reset() {
        synchronized (lock) {
            cachedValue = null;
            cacheTimestampNanos = 0L;
        }
    }

    /** Sets the maximum age for the cached value in seconds.
     * @param value The cache age in seconds.
     */
    public void setCacheAge(double value) {
        synchronized (lock) {
            this.cacheAgeNanos = (long) (value * 1_000_000_000L);
        }
    }

    /** Produces a new value to be cached.
     * @return The newly produced value.
     */
    protected abstract T produce();
}
