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

package com.google.common.collect;

import java.util.Comparator;
import java.util.SortedMap;

import com.google.common.annotations.GwtCompatible;

/**
 * A sorted map which forwards all its method calls to another sorted map.
 * Subclasses should override one or more methods to modify the behavior of
 * the backing sorted map as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @author Mike Bostock
 * @since 2 (imported from Google Collections Library)
 */
@GwtCompatible
public abstract class ForwardingSortedMap<K, V> extends ForwardingMap<K, V>
    implements SortedMap<K, V> {

  /** Constructor for use by subclasses. */
  protected ForwardingSortedMap() {}

  @Override protected abstract SortedMap<K, V> delegate();

  public Comparator<? super K> comparator() {
    return delegate().comparator();
  }

  public K firstKey() {
    return delegate().firstKey();
  }

  public SortedMap<K, V> headMap(K toKey) {
    return delegate().headMap(toKey);
  }

  public K lastKey() {
    return delegate().lastKey();
  }

  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    return delegate().subMap(fromKey, toKey);
  }

  public SortedMap<K, V> tailMap(K fromKey) {
    return delegate().tailMap(fromKey);
  }
}
