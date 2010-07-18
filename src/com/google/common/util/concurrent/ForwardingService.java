/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.common.util.concurrent;

import java.util.concurrent.Future;

import com.google.common.annotations.Beta;
import com.google.common.base.Service;
import com.google.common.collect.ForwardingObject;

/**
 * A {@link Service} that forwards all method calls to another service.
 *
 * @author Chris Nokleberg
 * @since 1
 */
@Beta
public abstract class ForwardingService extends ForwardingObject
    implements Service {

  /** Constructor for use by subclasses. */
  protected ForwardingService() {}

  @Override
protected abstract Service delegate();

  public Future<State> start() {
    return delegate().start();
  }

  public State state() {
    return delegate().state();
  }

  public Future<State> stop() {
    return delegate().stop();
  }

  public State startAndWait() {
    return delegate().startAndWait();
  }

  public State stopAndWait() {
    return delegate().stopAndWait();
  }

  public boolean isRunning() {
    return delegate().isRunning();
  }
}
