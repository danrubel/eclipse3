/*
 * Copyright (c) 2013, the Dart project authors.
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
package com.google.dart.engine.internal.element;

import com.google.dart.engine.EngineTestCase;
import com.google.dart.engine.element.ClassElement;
import com.google.dart.engine.element.MethodElement;
import com.google.dart.engine.element.PropertyAccessorElement;
import com.google.dart.engine.internal.context.AnalysisContextImpl;

import static com.google.dart.engine.element.ElementFactory.classElement;
import static com.google.dart.engine.element.ElementFactory.getterElement;
import static com.google.dart.engine.element.ElementFactory.library;
import static com.google.dart.engine.element.ElementFactory.methodElement;
import static com.google.dart.engine.element.ElementFactory.setterElement;

public class ClassElementImplTest extends EngineTestCase {
  public void test_lookUpGetter_declared() {
    LibraryElementImpl library = library(new AnalysisContextImpl(), "lib");
    ClassElementImpl classA = (ClassElementImpl) classElement("A");
    String getterName = "g";
    PropertyAccessorElement getter = getterElement(getterName, false, null);
    classA.setAccessors(new PropertyAccessorElement[] {getter});
    ((CompilationUnitElementImpl) library.getDefiningCompilationUnit()).setTypes(new ClassElement[] {classA});
    assertSame(getter, classA.lookUpGetter(getterName, library));
  }

  public void test_lookUpGetter_inherited() {
    LibraryElementImpl library = library(new AnalysisContextImpl(), "lib");
    ClassElementImpl classA = (ClassElementImpl) classElement("A");
    String getterName = "g";
    PropertyAccessorElement getter = getterElement(getterName, false, null);
    classA.setAccessors(new PropertyAccessorElement[] {getter});
    ClassElementImpl classB = (ClassElementImpl) classElement("B", classA.getType());
    ((CompilationUnitElementImpl) library.getDefiningCompilationUnit()).setTypes(new ClassElement[] {
        classA, classB});
    assertSame(getter, classB.lookUpGetter(getterName, library));
  }

  public void test_lookUpGetter_undeclared() {
    LibraryElementImpl library = library(new AnalysisContextImpl(), "lib");
    ClassElementImpl classA = (ClassElementImpl) classElement("A");
    ((CompilationUnitElementImpl) library.getDefiningCompilationUnit()).setTypes(new ClassElement[] {classA});
    assertNull(classA.lookUpGetter("g", library));
  }

  public void test_lookUpMethod_declared() {
    LibraryElementImpl library = library(new AnalysisContextImpl(), "lib");
    ClassElementImpl classA = (ClassElementImpl) classElement("A");
    String methodName = "m";
    MethodElement method = methodElement(methodName, null);
    classA.setMethods(new MethodElement[] {method});
    ((CompilationUnitElementImpl) library.getDefiningCompilationUnit()).setTypes(new ClassElement[] {classA});
    assertSame(method, classA.lookUpMethod(methodName, library));
  }

  public void test_lookUpMethod_inherited() {
    LibraryElementImpl library = library(new AnalysisContextImpl(), "lib");
    ClassElementImpl classA = (ClassElementImpl) classElement("A");
    String methodName = "m";
    MethodElement method = methodElement(methodName, null);
    classA.setMethods(new MethodElement[] {method});
    ClassElementImpl classB = (ClassElementImpl) classElement("B", classA.getType());
    ((CompilationUnitElementImpl) library.getDefiningCompilationUnit()).setTypes(new ClassElement[] {
        classA, classB});
    assertSame(method, classB.lookUpMethod(methodName, library));
  }

  public void test_lookUpMethod_undeclared() {
    LibraryElementImpl library = library(new AnalysisContextImpl(), "lib");
    ClassElementImpl classA = (ClassElementImpl) classElement("A");
    ((CompilationUnitElementImpl) library.getDefiningCompilationUnit()).setTypes(new ClassElement[] {classA});
    assertNull(classA.lookUpMethod("m", library));
  }

  public void test_lookUpSetter_declared() {
    LibraryElementImpl library = library(new AnalysisContextImpl(), "lib");
    ClassElementImpl classA = (ClassElementImpl) classElement("A");
    String setterName = "s";
    PropertyAccessorElement setter = setterElement(setterName, false, null);
    classA.setAccessors(new PropertyAccessorElement[] {setter});
    ((CompilationUnitElementImpl) library.getDefiningCompilationUnit()).setTypes(new ClassElement[] {classA});
    assertSame(setter, classA.lookUpSetter(setterName, library));
  }

  public void test_lookUpSetter_inherited() {
    LibraryElementImpl library = library(new AnalysisContextImpl(), "lib");
    ClassElementImpl classA = (ClassElementImpl) classElement("A");
    String setterName = "s";
    PropertyAccessorElement setter = setterElement(setterName, false, null);
    classA.setAccessors(new PropertyAccessorElement[] {setter});
    ClassElementImpl classB = (ClassElementImpl) classElement("B", classA.getType());
    ((CompilationUnitElementImpl) library.getDefiningCompilationUnit()).setTypes(new ClassElement[] {
        classA, classB});
    assertSame(setter, classB.lookUpSetter(setterName, library));
  }

  public void test_lookUpSetter_undeclared() {
    LibraryElementImpl library = library(new AnalysisContextImpl(), "lib");
    ClassElementImpl classA = (ClassElementImpl) classElement("A");
    ((CompilationUnitElementImpl) library.getDefiningCompilationUnit()).setTypes(new ClassElement[] {classA});
    assertNull(classA.lookUpSetter("s", library));
  }
}
