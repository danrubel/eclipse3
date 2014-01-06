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
package com.google.dart.engine.internal.html.angular;

import com.google.dart.engine.ast.Expression;
import com.google.dart.engine.ast.SimpleIdentifier;
import com.google.dart.engine.element.Element;
import com.google.dart.engine.error.AngularCode;
import com.google.dart.engine.error.StaticWarningCode;
import com.google.dart.engine.html.ast.HtmlUnitUtils;

public class AngularHtmlUnitResolverTest extends AngularTest {
  public void test_moduleAsLocalVariable() throws Exception {
    mainSource = contextHelper.addSource("/main.dart", createSource("",//
        "import 'angular.dart';",
        "",
        "@NgController(",
        "    selector: '[my-controller]',",
        "    publishAs: 'ctrl')",
        "class MyController {",
        "  String field;",
        "}",
        "",
        "class MyModule extends Module {",
        "  MyModule() {",
        "    type(MyController);",
        "  }",
        "}",
        "",
        "main() {",
        "  var module = new Module()",
        "    ..type(MyController);",
        "  ngBootstrap(module: module);",
        "}"));
    resolveIndex(//
        "<html ng-app>",
        "  <body>",
        "    <div my-controller>",
        "      {{ctrl.field}}",
        "    </div>",
        "    <script type='application/dart' src='main.dart'></script>",
        "  </body>",
        "</html>");
    assertNoErrors();
    verify(indexSource);
    assertResolvedIdentifier("ctrl", "MyController");
  }

  public void test_NgDirective_usedOnControllerClass() throws Exception {
    mainSource = contextHelper.addSource("/main.dart", createSource("",//
        "import 'angular.dart';",
        "",
        "@NgDirective(",
        "    selector: '[my-controller]',",
        "    publishAs: 'ctrl')",
        "class MyController {",
        "  String field;",
        "}",
        "",
        "class MyModule extends Module {",
        "  MyModule() {",
        "    type(MyController);",
        "  }",
        "}",
        "",
        "main() {",
        "  ngBootstrap(module: new MyModule());",
        "}"));
    resolveIndex(//
        "<html ng-app>",
        "  <body>",
        "    <div my-controller>",
        "      {{ctrl.field}}",
        "    </div>",
        "    <script type='application/dart' src='main.dart'></script>",
        "  </body>",
        "</html>");
    assertNoErrors();
    verify(indexSource);
    assertResolvedIdentifier("ctrl", "MyController");
  }

  public void test_ngModel_modelAfterUsage() throws Exception {
    addMyController();
    resolveIndex(createHtmlWithMyController(//
        "<h3>Hello {{name}}!</h3>",
        "<input type='text' ng-model='name'>"));
    assertNoErrors();
    verify(indexSource);
    assertResolvedIdentifier("name}}!", "String");
    assertResolvedIdentifier("name'>", "String");
  }

  public void test_ngModel_modelBeforeUsage() throws Exception {
    addMyController();
    resolveIndex(createHtmlWithMyController(//
        "<input type='text' ng-model='name'>",
        "<h3>Hello {{name}}!</h3>"));
    assertNoErrors();
    verify(indexSource);
    assertResolvedIdentifier("name}}!", "String");
    Element element = assertResolvedIdentifier("name'>", "String");
    assertEquals("name", element.getName());
    assertEquals(findOffset("name'>"), element.getNameOffset());
  }

  public void test_ngModel_notIdentifier() throws Exception {
    addMyController();
    resolveIndex(createHtmlWithMyController("<input type='text' ng-model='ctrl.field'>"));
    assertNoErrors();
    verify(indexSource);
    assertResolvedIdentifier("field'>", "String");
  }

  public void test_ngRepeat_bad_expectedIdentifier() throws Exception {
    addMyController();
    resolveIndex(createHtmlWithMyController(//
        "<li ng-repeat='name + 42 in ctrl.names'>",
        "</li>"));
    assertErrors(indexSource, AngularCode.EXPECTED_IDENTIFIER);
  }

  public void test_ngRepeat_bad_expectedIn() throws Exception {
    addMyController();
    resolveIndex(createHtmlWithMyController(//
        "<li ng-repeat='name : ctrl.names'>",
        "</li>"));
    assertErrors(indexSource, AngularCode.EXPECTED_IN);
  }

  public void test_ngRepeat_resolvedExpressions() throws Exception {
    addMyController();
    resolveIndex(createHtmlWithMyController(//
        "<li ng-repeat='name in ctrl.names'>",
        "  {{name}}",
        "</li>"));
    assertNoErrors();
    verify(indexSource);
    assertResolvedIdentifier("name in", "String");
    assertResolvedIdentifier("ctrl.", "MyController");
    assertResolvedIdentifier("names'", "List<String>");
    assertResolvedIdentifier("name}}", "String");
  }

  public void test_notResolved_no_ngBootstrap_invocation() throws Exception {
    contextHelper.addSource("/main.dart", "// just empty script");
    resolveIndex(//
        "<html ng-app>",
        "  <body>",
        "    <div my-marker>",
        "      {{ctrl.field}}",
        "    </div>",
        "    <script type='application/dart' src='main.dart'></script>",
        "  </body>",
        "</html>");
    assertNoErrors();
    // Angular is not initialized, so "ctrl" is not parsed
    Expression expression = HtmlUnitUtils.getExpression(indexUnit, findOffset("ctrl"));
    assertNull(expression);
  }

  public void test_notResolved_noDartScript() throws Exception {
    resolveIndex(//
        "<html ng-app>",
        "  <body>",
        "    <div my-marker>",
        "      {{ctrl.field}}",
        "    </div>",
        "  </body>",
        "</html>");
    assertNoErrors();
    // Angular is not initialized, so "ctrl" is not parsed
    Expression expression = HtmlUnitUtils.getExpression(indexUnit, findOffset("ctrl"));
    assertNull(expression);
  }

  public void test_notResolved_notAngular() throws Exception {
    resolveIndex(//
        "<html no-ng-app>",
        "  <body>",
        "    <div my-marker>",
        "      {{ctrl.field}}",
        "    </div>",
        "  </body>",
        "</html>");
    assertNoErrors();
    // Angular is not initialized, so "ctrl" is not parsed
    Expression expression = HtmlUnitUtils.getExpression(indexUnit, findOffset("ctrl"));
    assertNull(expression);
  }

  public void test_notResolved_wrongControllerMarker() throws Exception {
    addMyController();
    resolveIndex(//
        "<html ng-app>",
        "  <body>",
        "    <div not-my-marker>",
        "      {{ctrl.field}}",
        "    </div>",
        "    <script type='application/dart' src='main.dart'></script>",
        "  </body>",
        "</html>");
    assertErrors(indexSource, StaticWarningCode.UNDEFINED_IDENTIFIER);
    // "ctrl" is not resolved
    SimpleIdentifier identifier = findIdentifier("ctrl");
    assertNull(identifier.getBestElement());
  }

  public void test_resolveExpression_inAttribute() throws Exception {
    addMyController();
    resolveIndex(createHtmlWithMyController("<button title='{{ctrl.field}}'></button>"));
    assertNoErrors();
    verify(indexSource);
    assertResolvedIdentifier("ctrl", "MyController");
  }

  public void test_resolveExpression_inTag() throws Exception {
    addMyController();
    resolveIndex(createHtmlWithMyController("{{ctrl.field}}"));
    assertNoErrors();
    verify(indexSource);
    assertResolvedIdentifier("ctrl", "MyController");
  }

  public void test_resolveExpression_ngApp_onBody() throws Exception {
    addMyController();
    resolveIndex(//
        "<html>",
        "  <body ng-app>",
        "    <div my-controller>",
        "      {{ctrl.field}}",
        "    </div>",
        "    <script type='application/dart' src='main.dart'></script>",
        "  </body>",
        "</html>");
    assertNoErrors();
    verify(indexSource);
    assertResolvedIdentifier("ctrl", "MyController");
  }
}
