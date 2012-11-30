/*
 * Copyright (c) 2012, the Dart project authors.
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
package com.google.dart.tools.core.builder;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A participant in the building of dart projects.
 */
public interface BuildParticipant {

  /**
   * Called when the participant should process resources to perform any analysis and building
   * required. Participants should use {@link BuildEvent#traverse(BuildParticipant)} to visit
   * resources that need to be processed
   * 
   * @param event the event (not <code>null</code>)
   * @param monitor the progress monitor (not <code>null</code>) to use for reporting progress to
   *          the user. It is the caller's responsibility to call done() on the given monitor.
   */
  void build(BuildEvent event, IProgressMonitor monitor) throws CoreException;

  /**
   * Called when the participant should discard any cached state from prior builds. It is
   * recommended that the participant delete derived resources that it generated and remove any
   * markers that it added.
   * 
   * @param event the event (not <code>null</code>)
   * @param monitor the progress monitor (not <code>null</code>) to use for reporting progress to
   *          the user. It is the caller's responsibility to call done() on the given monitor.
   */
  void clean(CleanEvent event, IProgressMonitor monitor) throws CoreException;

  /**
   * Called by {@link BuildEvent#traverse(BuildParticipant)} when a resource has been modified or
   * deleted.
   * 
   * @param delta the object describing the resource and the change (not <code>null</code>)
   * @param monitor the progress monitor (not <code>null</code>) to use for reporting progress to
   *          the user. It is the caller's responsibility to call done() on the given monitor.
   * @return <code>true</code> if the resource's children should be visited
   */
  boolean visit(IResourceDelta delta, IProgressMonitor monitor);

  /**
   * Called by {@link BuildEvent#traverse(BuildParticipant)} when a resource has been added or is
   * otherwise being visited for the first time since the last call to
   * {@link #clean(CleanEvent, IProgressMonitor)}.
   * 
   * @param proxy a proxy to the resource being visited
   * @param monitor the progress monitor (not <code>null</code>) to use for reporting progress to
   *          the user. It is the caller's responsibility to call done() on the given monitor.
   * @return <code>true</code> if the resource's children should be visited
   */
  boolean visit(IResourceProxy proxy, IProgressMonitor monitor);
}
