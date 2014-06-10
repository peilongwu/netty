/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.internal;

import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.Arrays;

/**
 * A simple class providing equivalent functionality to java.lang,ThreadLocal, but operating
 * over a predefined array, so it always operate in 0(1). This permits less indirection
 * and offers a slight performance improvement, so is useful when invoked frequently.
 *
 * The fast path is only possible on threads that extend FastThreadLocalThread, as this class
 * stores the necessary state. Access by any other kind of thread falls back to a regular ThreadLocal
 *
 * @param <V>
 */
public class FastThreadLocal<V> {
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(FastThreadLocal.class);
    private static final Object EMPTY = new Object();
    private static final int MAX_TYPES;

    static {
        MAX_TYPES = SystemPropertyUtil.getInt("io.netty.fastthreadlocal.maxTypes", 64);
        LOGGER.debug("-Dio.netty.fastthreadlocal.maxTypes: {}", MAX_TYPES);
    }

    /**
     * To utilise the FastThreadLocal fast-path, all threads accessing a FastThreadLocal must extend this class
     */
    public static class FastThreadLocalThread extends Thread {

        private final Object[] lookup = new Object[MAX_TYPES];

        public FastThreadLocalThread() {
            Arrays.fill(lookup, EMPTY);
        }

        public FastThreadLocalThread(Runnable target) {
            super(target);
            Arrays.fill(lookup, EMPTY);
        }

        public FastThreadLocalThread(ThreadGroup group, Runnable target) {
            super(group, target);
            Arrays.fill(lookup, EMPTY);
        }

        public FastThreadLocalThread(String name) {
            super(name);
            Arrays.fill(lookup, EMPTY);
        }

        public FastThreadLocalThread(ThreadGroup group, String name) {
            super(group, name);
            Arrays.fill(lookup, EMPTY);
        }

        public FastThreadLocalThread(Runnable target, String name) {
            super(target, name);
            Arrays.fill(lookup, EMPTY);
        }

        public FastThreadLocalThread(ThreadGroup group, Runnable target, String name) {
            super(group, target, name);
            Arrays.fill(lookup, EMPTY);
        }

        public FastThreadLocalThread(ThreadGroup group, Runnable target, String name, long stackSize) {
            super(group, target, name, stackSize);
            Arrays.fill(lookup, EMPTY);
        }
    }

    public static final class FastThreadLocalThreadFactory extends DefaultThreadFactory {
        public FastThreadLocalThreadFactory(Class<?> poolType) {
            super(poolType);
        }

        public FastThreadLocalThreadFactory(String poolName) {
            super(poolName);
        }

        public FastThreadLocalThreadFactory(Class<?> poolType, boolean daemon) {
            super(poolType, daemon);
        }

        public FastThreadLocalThreadFactory(String poolName, boolean daemon) {
            super(poolName, daemon);
        }

        public FastThreadLocalThreadFactory(Class<?> poolType, int priority) {
            super(poolType, priority);
        }

        public FastThreadLocalThreadFactory(String poolName, int priority) {
            super(poolName, priority);
        }

        public FastThreadLocalThreadFactory(Class<?> poolType, boolean daemon, int priority) {
            super(poolType, daemon, priority);
        }

        public FastThreadLocalThreadFactory(String poolName, boolean daemon, int priority) {
            super(poolName, daemon, priority);
        }

        @Override
        protected Thread newThread(Runnable r, String name) {
            return new FastThreadLocalThread(r, name);
        }
    }

    private static int nextIndex;
    private final int index;
    private final ThreadLocal<V> fallback = new ThreadLocal<V>() {
        @Override
        protected V initialValue() {
            return FastThreadLocal.this.initialValue();
        }
    };

    public FastThreadLocal() {
        synchronized (FastThreadLocal.class) {
            if (nextIndex >= MAX_TYPES) {
                LOGGER.info("Maximal number of optimized ThreadLocal reached. Fallback to normal ThreadLocal");
                // No more space in the backing array so fall-back to ThreadLocal
                index = -1;
            } else {
                index = nextIndex;
                nextIndex++;
            }
        }
    }

    /**
     * Override this method to define the default value to assign
     * when a thread first calls get() without a preceding set()
     *
     * @return the initial value
     */
    protected V initialValue() {
        return null;
    }

    /**
     * Set the value for the current thread
     */
    public void set(V value) {
        Thread thread = Thread.currentThread();
        if (index == -1 || !(thread instanceof FastThreadLocalThread)) {
            fallback.set(value);
            return;
        }
        Object[] lookup = ((FastThreadLocalThread) thread).lookup;
        lookup[index] = value;
    }

    /**
     * Sets the value to uninitialized; a proceeding call to get() will trigger a call to initialValue()
     */
    public void remove() {
        Thread thread = Thread.currentThread();
        if (index == -1 || !(thread instanceof FastThreadLocalThread)) {
            fallback.remove();
            return;
        }
        Object[] lookup = ((FastThreadLocalThread) thread).lookup;
        lookup[index] = EMPTY;
    }

    /**
     * @return the current value for the current thread
     */
    @SuppressWarnings("unchecked")
    public final V get() {
        Thread thread = Thread.currentThread();
        if (index == -1 || !(thread instanceof FastThreadLocalThread)) {
            return fallback.get();
        }
        Object[] lookup = ((FastThreadLocalThread) thread).lookup;
        Object v = lookup[index];
        if (v == EMPTY) {
            lookup[index] = v = initialValue();
        }
        return (V) v;
    }
}
