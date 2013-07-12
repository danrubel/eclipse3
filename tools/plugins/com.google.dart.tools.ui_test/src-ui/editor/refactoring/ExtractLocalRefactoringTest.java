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
package editor.refactoring;

import com.google.dart.tools.ui.actions.JdtActionConstants;
import com.google.dart.ui.test.driver.ShellClosedOperation;
import com.google.dart.ui.test.driver.ShellOperation;
import com.google.dart.ui.test.driver.Operation;
import com.google.dart.ui.test.helpers.WizardDialogHelper;
import com.google.dart.ui.test.util.UiContext;

import editor.AbstractDartEditorTest;


import org.eclipse.jface.action.IAction;

import java.util.concurrent.TimeUnit;

/**
 * Test for the "Extract Local" refactoring.
 */
public final class ExtractLocalRefactoringTest extends AbstractDartEditorTest {
  private static class WizardHelper extends WizardDialogHelper {
    public WizardHelper(UiContext context) {
      super(context);
    }

    public void setName(String name) {
      context.getTextByLabel("Variable name:").setText(name);
    }
  }

  public void test_singleExpression() throws Exception {
    openTestEditor(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  print(123);",
        "}");
    // run "Rename" action
    selectAndStartRename("123");
    // animate wizard dialog
    String wizardName = "Extract Local Variable";
    addOperation(new ShellOperation(wizardName) {
      @Override
      public void run(UiContext context) throws Exception {
        WizardHelper helper = new WizardHelper(context);
        // invalid name
        helper.setName("-name");
        helper.assertMessage("Variable name must not start with '-'.");
        // set new name
        helper.setName("res");
        helper.assertNoMessage();
        // done
        context.clickButton("OK");
      }
    });
    addOperation(new ShellClosedOperation(wizardName));
    // validate result
    runUiOperations(60, TimeUnit.SECONDS);
    assertTestUnitContent(
        "// filler filler filler filler filler filler filler filler filler filler",
        "main() {",
        "  var res = 123;",
        "  print(res);",
        "}");
  }

  private void selectAndStartRename(String pattern) throws Exception {
    selectRange(pattern);
    // run "Extract Local" action
    final IAction action = getEditorAction(JdtActionConstants.EXTRACT_LOCAL);
    addOperation(new Operation() {
      @Override
      public boolean isReady(UiContext context) throws Exception {
        return action.isEnabled();
      }

      @Override
      public void run(UiContext context) throws Exception {
        action.run();
      }
    });
  }
}
