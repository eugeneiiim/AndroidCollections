/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.base;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import androidcollections.annotations.Nullable;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;

/**
 * Useful suppliers.
 *
 * <p>All methods return serializable suppliers as long as they're given
 * serializable parameters.
 *
 * @author Laurence Gonsalves
 * @author Harry Heymann
 * @since 2 (imported from Google Collections Library)
 */
@GwtCompatible
public final class Suppliers {
  private Suppliers() {}

  /**
   * Returns a new supplier which is the composition of the provided function
   * and supplier. In other words, the new supplier's value will be computed by
   * retrieving the value from {@code supplier}, and then applying
   * {@code function} to that value. Note that the resulting supplier will not
   * call {@code supplier} or invoke {@code function} until it is called.
   */
  public static <F, T> Supplier<T> compose(
      Function<? super F, T> function, Supplier<F> supplier) {
    Preconditions.checkNotNull(function);
    Preconditions.checkNotNull(supplier);
    return new SupplierComposition<F, T>(function, supplier);
  }

  private static class SupplierComposition<F, T>
      implements Supplier<T>, Serializable {
    final Function<? super F, T> function;
    final Supplier<F> supplier;

    SupplierComposition(Function<? super F, T> function, Supplier<F> supplier) {
      this.function = function;
      this.supplier = supplier;
    }
    public T get() {
      return function.apply(supplier.get());
    }
    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a supplier which caches the instance retrieved during the first
   * call to {@code get()} and returns that value on subsequent calls to
   * {@code get()}. See:
   * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
   *
   * <p>The returned supplier is thread-safe. The supplier's serialized form
   * does not contain the cached value, which will be recalculated when {@code
   * get()} is called on the reserialized instance.
   */
  public static <T> Supplier<T> memoize(Supplier<T> delegate) {
    return new MemoizingSupplier<T>(Preconditions.checkNotNull(delegate));
  }

  @VisibleForTesting
  static class MemoizingSupplier<T> implements Supplier<T>, Serializable {
    final Supplier<T> delegate;
    transient boolean initialized;
    transient T value;

    MemoizingSupplier(Supplier<T> delegate) {
      this.delegate = delegate;
    }

    public synchronized T get() {
      if (!initialized) {
        value = delegate.get();
        initialized = true;
      }
      return value;
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a supplier that caches the instance supplied by the delegate and
   * removes the cached value after the specified time has passed. Subsequent
   * calls to {@code get()} return the cached value if the expiration time has
   * not passed. After the expiration time, a new value is retrieved, cached,
   * and returned. See:
   * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
   *
   * <p>The returned supplier is thread-safe. The supplier's serialized form
   * does not contain the cached value, which will be recalculated when {@code
   * get()} is called on the reserialized instance.
   *
   * @param duration the length of time after a value is created that it
   *     should stop being returned by subsequent {@code get()} calls
   * @param unit the unit that {@code duration} is expressed in
   * @throws IllegalArgumentException if {@code duration} is not positive
   * @since 2
   */
  public static <T> Supplier<T> memoizeWithExpiration(
      Supplier<T> delegate, long duration, TimeUnit unit) {
    return new ExpiringMemoizingSupplier<T>(delegate, duration, unit);
  }

  @VisibleForTesting static class ExpiringMemoizingSupplier<T>
      implements Supplier<T>, Serializable {
    final Supplier<T> delegate;
    final long durationNanos;
    transient boolean initialized;
    transient T value;
    transient long expirationNanos;

    ExpiringMemoizingSupplier(
        Supplier<T> delegate, long duration, TimeUnit unit) {
      this.delegate = Preconditions.checkNotNull(delegate);
      this.durationNanos = unit.toNanos(duration);
      Preconditions.checkArgument(duration > 0);
    }

    public synchronized T get() {
      if (!initialized || Platform.systemNanoTime() - expirationNanos >= 0) {
        value = delegate.get();
        initialized = true;
        expirationNanos = Platform.systemNanoTime() + durationNanos;
      }
      return value;
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a supplier that always supplies {@code instance}.
   */
  public static <T> Supplier<T> ofInstance(@Nullable T instance) {
    return new SupplierOfInstance<T>(instance);
  }

  private static class SupplierOfInstance<T>
      implements Supplier<T>, Serializable {
    final T instance;

    SupplierOfInstance(T instance) {
      this.instance = instance;
    }
    public T get() {
      return instance;
    }
    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a supplier whose {@code get()} method synchronizes on
   * {@code delegate} before calling it, making it thread-safe.
   */
  public static <T> Supplier<T> synchronizedSupplier(Supplier<T> delegate) {
    return new ThreadSafeSupplier<T>(Preconditions.checkNotNull(delegate));
  }

  private static class ThreadSafeSupplier<T>
      implements Supplier<T>, Serializable {
    final Supplier<T> delegate;

    ThreadSafeSupplier(Supplier<T> delegate) {
      this.delegate = delegate;
    }
    public T get() {
      synchronized (delegate) {
        return delegate.get();
      }
    }
    private static final long serialVersionUID = 0;
  }
}
