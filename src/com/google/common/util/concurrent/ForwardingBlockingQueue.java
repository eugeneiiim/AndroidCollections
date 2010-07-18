// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.common.util.concurrent;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ForwardingQueue;

/**
 * A {@link BlockingQueue} which forwards all its method calls to another
 * {@link BlockingQueue}. Subclasses should override one or more methods to
 * modify the behavior of the backing collection as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @author Raimundo Mirisola
 *
 * @param <E> the type of elements held in this collection
 * @since 4
 */
public abstract class ForwardingBlockingQueue<E> extends ForwardingQueue<E>
    implements BlockingQueue<E> {

  /** Constructor for use by subclasses. */
  protected ForwardingBlockingQueue() {}

  @Override protected abstract BlockingQueue<E> delegate();

  public int drainTo(
      Collection<? super E> c, int maxElements) {
    return delegate().drainTo(c, maxElements);
  }

  public int drainTo(Collection<? super E> c) {
    return delegate().drainTo(c);
  }

  public boolean offer(E e, long timeout, TimeUnit unit)
      throws InterruptedException {
    return delegate().offer(e, timeout, unit);
  }

  public E poll(long timeout, TimeUnit unit)
      throws InterruptedException {
    return delegate().poll(timeout, unit);
  }

  public void put(E e) throws InterruptedException {
    delegate().put(e);
  }

  public int remainingCapacity() {
    return delegate().remainingCapacity();
  }

  public E take() throws InterruptedException {
    return delegate().take();
  }
}
