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
package com.google.dart.tools.debug.ui.internal.browser;

import com.google.dart.tools.core.DartCore;
import com.google.dart.tools.core.frog.FrogCompiler;
import com.google.dart.tools.core.frog.FrogCompiler.CompilationResult;
import com.google.dart.tools.core.model.DartElement;
import com.google.dart.tools.core.model.DartLibrary;
import com.google.dart.tools.core.model.HTMLFile;
import com.google.dart.tools.debug.core.DartDebugCorePlugin;
import com.google.dart.tools.debug.core.DartLaunchConfigWrapper;
import com.google.dart.tools.debug.ui.internal.DartDebugUIPlugin;
import com.google.dart.tools.debug.ui.internal.DartUtil;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.console.IConsoleConstants;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Launches the Dart application (compiled to js) in the browser.
 */
public class BrowserLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

  /**
   * Match both the input and id, so that different types of editor can be opened on the same input.
   */
  @Override
  public void launch(ILaunchConfiguration config, String mode, ILaunch launch,
      IProgressMonitor monitor) throws CoreException {
    mode = ILaunchManager.RUN_MODE;
    DartLaunchConfigWrapper launchConfig = new DartLaunchConfigWrapper(config);
    String url;

    if (launchConfig.getShouldLaunchFile()) {
      IResource resource = launchConfig.getApplicationResource();

      if (resource == null) {
        throw new CoreException(new Status(IStatus.ERROR, DartDebugCorePlugin.PLUGIN_ID,
            Messages.BrowserLaunchConfigurationDelegate_HtmlFileNotFound));
      }

      try {
        compileJavascript(resource, monitor);
      } catch (OperationCanceledException ex) {
        // The user cancelled the launch.
        DartCore.getConsole().println("Launch cancelled.");

        return;
      }

      url = resource.getLocationURI().toString();
    } else {
      url = launchConfig.getUrl();
      try {
        String scheme = new URI(url).getScheme();
        if (scheme == null) { // add scheme else browser will not launch
          url = "http://" + url;
        }
      } catch (URISyntaxException e) {
        throw new CoreException(new Status(IStatus.ERROR, DartDebugCorePlugin.PLUGIN_ID,
            Messages.BrowserLaunchConfigurationDelegate_UrlError));
      }
    }

    if (launchConfig.getUseDefaultBrowser()) {
      IWebBrowser browser = null;
      try {
        browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
            IWorkbenchBrowserSupport.AS_EXTERNAL, "defaultBrowser", "Default Browser", "Browser");
        if (browser != null) {
          browser.openURL(new URL(url));
        } else {
          throw new CoreException(new Status(IStatus.ERROR, DartDebugCorePlugin.PLUGIN_ID,
              Messages.BrowserLaunchConfigurationDelegate_DefaultBrowserNotFound));
        }
      } catch (PartInitException e1) {
        throw new CoreException(new Status(IStatus.ERROR, DartDebugCorePlugin.PLUGIN_ID,
            Messages.BrowserLaunchConfigurationDelegate_DefaultBrowserNotFound));
      } catch (MalformedURLException e) {
        throw new CoreException(new Status(IStatus.ERROR, DartDebugCorePlugin.PLUGIN_ID,
            Messages.BrowserLaunchConfigurationDelegate_UrlError));
      }
    } else {
      launchInExternalBrowser(launchConfig, url);
    }
  }

  /**
   * Before proceeding with launch, check if Javascript has been generated.
   * 
   * @param resource
   * @throws CoreException
   */
  private void compileJavascript(IResource resource, IProgressMonitor monitor)
      throws CoreException, OperationCanceledException {
    DartElement element = DartCore.create(resource);

    if (element == null) {
      throw new CoreException(new Status(IStatus.ERROR, DartDebugUIPlugin.PLUGIN_ID,
          Messages.BrowserLaunchShortcut_NotInLibraryErrorMessage));
    } else if (!(element instanceof HTMLFile)) {
      throw new CoreException(new Status(IStatus.ERROR, DartDebugUIPlugin.PLUGIN_ID,
          Messages.BrowserLaunchShortcut_NotHtmlFileErrorMessage));
    } else {
      HTMLFile htmlFile = (HTMLFile) element;

      try {
        if (htmlFile.getReferencedLibraries().length > 0) {
          DartLibrary library = htmlFile.getReferencedLibraries()[0];

          showConsole();

          CompilationResult result = FrogCompiler.compileLibrary(library, monitor,
              DartCore.getConsole());

          if (result.getExitCode() != 0) {
            String errMsg = NLS.bind("Failure to launch - unable to generate Javascript for {0}.",
                resource.getName());

            throw new CoreException(new Status(IStatus.ERROR, DartDebugUIPlugin.PLUGIN_ID, errMsg));
          }
        }
      } catch (CoreException e) {
        DartDebugCorePlugin.logError(e);

        throw new CoreException(new Status(IStatus.ERROR, DartDebugUIPlugin.PLUGIN_ID,
            e.toString(), e));
      } finally {
        monitor.done();
      }
    }
  }

  private Program findProgram(String name) {
    Program[] programs = Program.getPrograms();
    for (Program program : programs) {
      if (program.getName().equals(name)) {
        return program;
      }
    }

    return null;
  }

  private void launchInExternalBrowser(DartLaunchConfigWrapper launchConfig, final String url)
      throws CoreException {
    final String browserName = launchConfig.getBrowserName();
    final int result[] = new int[1];

    if (!browserName.isEmpty()) {
      Display.getDefault().asyncExec(new Runnable() {
        @Override
        public void run() {
          try {
            Program program = findProgram(browserName);

            if (program != null) {
              program.execute(url);
              result[0] = 0;
            } else {
              result[0] = -1;
            }
          } catch (Throwable t) {
            DartDebugCorePlugin.logError(t);
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Launch Browser Error",
                t.getMessage());
          }
        }
      });

      if (result[0] == -1) {
        throw new CoreException(new Status(IStatus.ERROR, DartDebugCorePlugin.PLUGIN_ID,
            Messages.BrowserLaunchConfigurationDelegate_BrowserNotFound));
      }
    }
  }

  private void showConsole() {
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        try {
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
              IConsoleConstants.ID_CONSOLE_VIEW);
        } catch (PartInitException e) {
          DartUtil.logError(e);
        }
      }
    });
  }

}
