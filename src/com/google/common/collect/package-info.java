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

/**
 * This package contains generic collection interfaces and implementations, and
 * other utilities for working with collections. It is a part of the open-source
 * <a href="http://guava-libraries.googlecode.com">Guava libraries</a>.
 *
 * <h2>Collection Types</h2>
 *
 * <dl>
 * <dt>{@link com.google.common.collect.BiMap}
 * <dd>An extension of {@link java.util.Map} that guarantees the uniqueness of
 *     its values as well as that of its keys. This is sometimes called an
 *     "invertible map," since the restriction on values enables it to support
 *     an {@linkplain com.google.common.collect.BiMap#inverse inverse view} --
 *     which is another instance of {@code BiMap}.
 *
 * <dt>{@link com.google.common.collect.Multiset}
 * <dd>An extension of {@link java.util.Collection} that may contain duplicate
 *     values like a {@link java.util.List}, yet has order-independent equality
 *     like a {@link java.util.Set}.  One typical use for a multiset is to
 *     represent a histogram.
 *
 * <dt>{@link com.google.common.collect.Multimap}
 * <dd>A new type, which is similar to {@link java.util.Map}, but may contain
 *     multiple entries with the same key. Some behaviors of
 *     {@link com.google.common.collect.Multimap} are left unspecified and are
 *     provided only by the subtypes mentioned below.
 *
 * <dt>{@link com.google.common.collect.ListMultimap}
 * <dd>An extension of {@link com.google.common.collect.Multimap} which permits
 *     duplicate entries, supports random access of values for a particular key,
 *     and has <i>partially order-dependent equality</i> as defined by
 *     {@link com.google.common.collect.ListMultimap#equals(Object)}. {@code
 *     ListMultimap} takes its name from the fact that the {@linkplain
 *     com.google.common.collect.ListMultimap#get collection of values}
 *     associated with a given key fulfills the {@link java.util.List} contract.
 *
 * <dt>{@link com.google.common.collect.SetMultimap}
 * <dd>An extension of {@link com.google.common.collect.Multimap} which has
 *     order-independent equality and does not allow duplicate entries; that is,
 *     while a key may appear twice in a {@code SetMultimap}, each must map to a
 *     different value.  {@code SetMultimap} takes its name from the fact that
 *     the {@linkplain com.google.common.collect.SetMultimap#get collection of
 *     values} associated with a given key fulfills the {@link java.util.Set}
 *     contract.
 *
 * <dt>{@link com.google.common.collect.SortedSetMultimap}
 * <dd>An extension of {@link com.google.common.collect.SetMultimap} for which
 *     the {@linkplain com.google.common.collect.SortedSetMultimap#get
 *     collection values} associated with a given key is a
 *     {@link java.util.SortedSet}.
 *
 * <dt>{@link com.google.common.collect.ClassToInstanceMap}
 * <dd>An extension of {@link java.util.Map} that associates a raw type with an
 *     instance of that type.
 * </dl>
 *
 * <h2>Collection Implementations</h2>
 *
 * <h3>of {@link java.util.List}</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.ImmutableList}
 * </ul>
 *
 * <h3>of {@link java.util.Set}</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.ImmutableSet}
 * <dt>{@link com.google.common.collect.ImmutableSortedSet}
 * </ul>
 *
 * <h3>of {@link java.util.Map}</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.ImmutableMap}
 * <dt>{@link com.google.common.collect.ImmutableSortedMap}
 * <dt>{@link com.google.common.collect.MapMaker}
 * </ul>
 *
 * <h3>of {@link com.google.common.collect.BiMap}</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.ImmutableBiMap}
 * <dt>{@link com.google.common.collect.HashBiMap}
 * <dt>{@link com.google.common.collect.EnumBiMap}
 * <dt>{@link com.google.common.collect.EnumHashBiMap}
 * </dl>
 *
 * <h3>of {@link com.google.common.collect.Multiset}</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.ImmutableMultiset}
 * <dt>{@link com.google.common.collect.HashMultiset}
 * <dt>{@link com.google.common.collect.LinkedHashMultiset}
 * <dt>{@link com.google.common.collect.TreeMultiset}
 * <dt>{@link com.google.common.collect.EnumMultiset}
 * <dt>{@link com.google.common.collect.ConcurrentHashMultiset}
 * </dl>
 *
 * <h3>of {@link com.google.common.collect.Multimap}</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.ImmutableMultimap}
 * <dt>{@link com.google.common.collect.ImmutableListMultimap}
 * <dt>{@link com.google.common.collect.ImmutableSetMultimap}
 * <dt>{@link com.google.common.collect.ArrayListMultimap}
 * <dt>{@link com.google.common.collect.HashMultimap}
 * <dt>{@link com.google.common.collect.TreeMultimap}
 * <dt>{@link com.google.common.collect.LinkedHashMultimap}
 * <dt>{@link com.google.common.collect.LinkedListMultimap}
 * </dl>
 *
 * <h3>of {@link com.google.common.collect.ClassToInstanceMap}</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.ImmutableClassToInstanceMap}
 * <dt>{@link com.google.common.collect.MutableClassToInstanceMap}
 * </dl>
 *
 * <h2>Classes of static utility methods</h2>
 *
 * <dl>
 * <dt>{@link com.google.common.collect.Collections2}
 * <dt>{@link com.google.common.collect.Iterators}
 * <dt>{@link com.google.common.collect.Iterables}
 * <dt>{@link com.google.common.collect.Lists}
 * <dt>{@link com.google.common.collect.Maps}
 * <dt>{@link com.google.common.collect.Sets}
 * <dt>{@link com.google.common.collect.Multisets}
 * <dt>{@link com.google.common.collect.Multimaps}
 * <dt>{@link com.google.common.collect.ObjectArrays}
 * </dl>

 * <h2>Comparison</h2>
 *
 * <dl>
 * <dt>{@link com.google.common.collect.Ordering}
 * <dt>{@link com.google.common.collect.ComparisonChain}
 * </ul>
 *
 * <h2>Abstract implementations</h2>
 *
 * <dl>
 * <dt>{@link com.google.common.collect.AbstractIterator}
 * <dt>{@link com.google.common.collect.ImmutableCollection}
 * <dt>{@link com.google.common.collect.UnmodifiableIterator}
 * </dl>
 *
 * <h2>Other</h2>
 *
 * <dt>{@link com.google.common.collect.Interner},
 *     {@link com.google.common.collect.Interners}
 * <dt>{@link com.google.common.collect.Constraint},
 *     {@link com.google.common.collect.Constraints}
 * <dt>{@link com.google.common.collect.MapConstraint},
 *     {@link com.google.common.collect.MapConstraints}
 * <dt>{@link com.google.common.collect.MapDifference}
 * <dt>{@link com.google.common.collect.PeekingIterator}
 *
 * <h2>Forwarding collections</h2>
 *
 * <dl>
 * <dt>{@link com.google.common.collect.ForwardingCollection}
 * <dt>{@link com.google.common.collect.ForwardingConcurrentMap}
 * <dt>{@link com.google.common.collect.ForwardingIterator}
 * <dt>{@link com.google.common.collect.ForwardingList}
 * <dt>{@link com.google.common.collect.ForwardingListIterator}
 * <dt>{@link com.google.common.collect.ForwardingListMultimap}
 * <dt>{@link com.google.common.collect.ForwardingMap}
 * <dt>{@link com.google.common.collect.ForwardingMapEntry}
 * <dt>{@link com.google.common.collect.ForwardingMultimap}
 * <dt>{@link com.google.common.collect.ForwardingMultiset}
 * <dt>{@link com.google.common.collect.ForwardingObject}
 * <dt>{@link com.google.common.collect.ForwardingQueue}
 * <dt>{@link com.google.common.collect.ForwardingSet}
 * <dt>{@link com.google.common.collect.ForwardingSetMultimap}
 * <dt>{@link com.google.common.collect.ForwardingSortedMap}
 * <dt>{@link com.google.common.collect.ForwardingSortedSet}
 * <dt>{@link com.google.common.collect.ForwardingSortedSetMultimap}
 * </dl>
 */
@ParametersAreNonnullByDefault
package com.google.common.collect;
import androidcollections.annotations.ParametersAreNonnullByDefault;

