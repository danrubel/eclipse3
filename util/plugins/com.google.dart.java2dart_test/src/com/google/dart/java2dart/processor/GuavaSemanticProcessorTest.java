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
package com.google.dart.java2dart.processor;

/**
 * Test for {@link GuavaSemanticProcessor}.
 */
public class GuavaSemanticProcessorTest extends SemanticProcessorTest {

  public void test_Objects_equal() throws Exception {
    setFileLines(
        "com/google/common/base/Objects.java",
        toString(
            "// filler filler filler filler filler filler filler filler filler filler",
            "package com.google.common.base;",
            "public class Objects {",
            "  public boolean equal(Object a, Object b) {return false;}",
            "}"));
    translateSingleFile(
        "// filler filler filler filler filler filler filler filler filler filler",
        "package test;",
        "import com.google.common.base.Objects;",
        "public class Test {",
        "  public boolean run_equal(Object a, Object b) {",
        "    return Objects.equal(a, b);",
        "  }",
        "}");
    GuavaSemanticProcessor.INSTANCE.process(context, unit);
    assertFormattedSource(//
        "class Test {",
        "  bool run_equal(Object a, Object b) => a == b;",
        "}");
  }
}
