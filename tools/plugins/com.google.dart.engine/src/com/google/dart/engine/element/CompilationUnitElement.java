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
package com.google.dart.engine.element;

/**
 * The interface {@code CompilationUnitElement} defines the behavior of elements representing a
 * compilation unit.
 * 
 * @coverage dart.engine.element
 */
public interface CompilationUnitElement extends Element {
  /**
   * Return an array containing all of the top-level accessors (getters and setters) contained in
   * this compilation unit.
   * 
   * @return the top-level accessors contained in this compilation unit
   */
  public PropertyAccessorElement[] getAccessors();

  /**
   * Return the library in which this compilation unit is defined.
   * 
   * @return the library in which this compilation unit is defined
   */
  @Override
  public LibraryElement getEnclosingElement();

  /**
   * Return an array containing all of the top-level functions contained in this compilation unit.
   * 
   * @return the top-level functions contained in this compilation unit
   */
  public FunctionElement[] getFunctions();

  /**
   * Return an array containing all of the top-level variables contained in this compilation unit.
   * 
   * @return the top-level variables contained in this compilation unit
   */
  public TopLevelVariableElement[] getTopLevelVariables();

  /**
   * Return an array containing all of the type aliases contained in this compilation unit.
   * 
   * @return the type aliases contained in this compilation unit
   */
  public TypeAliasElement[] getTypeAliases();

  /**
   * Return an array containing all of the classes contained in this compilation unit.
   * 
   * @return the classes contained in this compilation unit
   */
  public ClassElement[] getTypes();
}
