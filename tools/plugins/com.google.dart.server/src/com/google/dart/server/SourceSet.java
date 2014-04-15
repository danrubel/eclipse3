/*
 * Copyright (c) 2014, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.dart.server;

import com.google.dart.engine.source.Source;

/**
 * The interface {@code SourceSet} defines the behavior of objects that represent a set of
 * {@link Source}s.
 * 
 * @coverage dart.server
 */
public interface SourceSet {
  /**
   * Return the kind of the this source set.
   */
  SourceSetKind getKind();

  /**
   * Returns {@link Source}s that belong to this source set, if {@link SourceSetKind#LIST} is used;
   * an empty array otherwise.
   */
  Source[] getSources();
}
