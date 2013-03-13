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

package com.google.dart.command.analyze;

import com.google.common.collect.Lists;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

// TODO: --enable--additional-warnings
//@Option(name = "--type-checks-for-inferred-types",
//usage = "[not in spec] Enables 'interface has no method/field' for receivers with inferred types.")

/**
 * Command line options accepted by the {@link AnalyzerMain} entry point.
 */
public class AnalyzerOptions {

  /**
   * Create a new AnalyzerOptions object from the given list of command-line args.
   * 
   * @param args
   * @return
   */
  public static AnalyzerOptions createFromArgs(String[] args) {
    args = processArgs(args);

    AnalyzerOptions options = new AnalyzerOptions();

    CmdLineParser cmdLineParser = new CmdLineParser(options);

    for (int i = 0, len = args.length; i < len; i++) {
      try {
        cmdLineParser.parseArgument(args);
      } catch (CmdLineException e) {
        String msg = e.getMessage();

        if (e.getMessage().endsWith(" is not a valid option")) {
          String option = msg.substring(1);
          int closeQuote = option.indexOf('\"');
          option = option.substring(0, closeQuote);

          List<String> newArgs = Lists.newArrayList();

          for (String arg : args) {
            if (arg.equals(option)) {
              System.out.println("Ignoring unrecognized flag: " + arg);
              continue;
            }
            newArgs.add(arg);
          }

          args = newArgs.toArray(new String[newArgs.size()]);
          cmdLineParser = new CmdLineParser(options);
          continue;
        }
      }

      break;
    }

    return options;
  }

  private static String[] processArgs(String[] args) {
    List<String> result = new ArrayList<String>();

    for (String arg : args) {
      if (arg.indexOf('=') != -1) {
        String[] strs = arg.split("=");
        result.add(strs[0]);
        result.add(strs[1]);
      } else {
        result.add(arg);
      }
    }

    return result.toArray(new String[result.size()]);
  }

  @Option(name = "--machine", //
  usage = "Print errors in a format suitable for parsing")
  private boolean machineFormat = false;

  @Option(name = "--help", //
  usage = "Prints this help message")
  private boolean showHelp = false;

  @Option(name = "--version", //
  usage = "Print the analyzer version")
  private boolean showVersion = false;

  @Option(name = "--dart-sdk", //
  metaVar = "<dir>", //
  usage = "The path to the Dart SDK")
  private File dartSdkPath = null;

  @Option(name = "--package-root", //
  metaVar = "<dir>", //
  usage = "The path to the package root")
  private File packageRootPath = null;

  @Option(name = "--batch", aliases = {"-batch"})
  private boolean batch = false;

  @Option(name = "--show-sdk-warnings")
  private boolean showSdkWarnings = false;

  @Option(name = "--fatal-warnings")
  private boolean warningsAreFatal = false;

  // TODO(devoncarew): this is unused, and is only for dartc compatibility
  @Option(name = "--fatal-type-errors")
  private boolean fatalTypeError = false;

  // TODO(devoncarew): this is unused, and is only for dartc compatibility
  @Option(name = "--error_format")
  private String errorFormat = "";

  @Option(name = "--create-sdk-index", //
  metaVar = "<file>")
  private File sdkIndexLocation = null;

  @Option(name = "--test")
  private boolean runTests = false;

  @SuppressWarnings("unused")
  @Option(name = "--ignore-unrecognized-flags")
  private boolean ignoreUnrecognizedFlags;

  @Argument
  private final String sourceFile = null;

  public AnalyzerOptions() {

  }

  /**
   * Return the path to the dart SDK.
   */
  public File getDartSdkPath() {
    return dartSdkPath;
  }

  public boolean getMachineFormat() {
    if ("machine".equals(errorFormat)) {
      return true;
    }

    return machineFormat;
  }

  /**
   * @return the package-root path, if specified
   */
  public File getPackageRootPath() {
    return packageRootPath;
  }

  public boolean getRunTests() {
    return runTests;
  }

  /**
   * @return the output location to use when creating an SDK index
   */
  public File getSdkIndexLocation() {
    return sdkIndexLocation;
  }

  /**
   * @return whether SDK warnings should be reported
   */
  public boolean getShowSdkWarnings() {
    return showSdkWarnings;
  }

  /**
   * @return whether we should print out the analyzer version
   */
  public boolean getShowVersion() {
    return showVersion;
  }

  /**
   * Returns the list of files passed to the analyzer.
   */
  public String getSourceFile() {
    return sourceFile;
  }

  /**
   * Return whether warnings are reported as fatal errors. This is only useful for batch mode.
   * 
   * @return whether warnings are reported as fatal errors
   */
  public boolean getWarningsAreFatal() {
    return warningsAreFatal;
  }

  /**
   * Print the tool usage to the given stream.
   * 
   * @param out
   */
  public void printUsage(PrintStream out) {
    CmdLineParser parser = new CmdLineParser(this);
    parser.printUsage(out);
  }

  public void setDartSdkPath(File dartSdkPath) {
    this.dartSdkPath = dartSdkPath;
  }

  /**
   * Return {@code true} if the analyzer should be run in batch mode, {@code false} otherwise.
   * <p>
   * (In batch mode, command line arguments are received through stdin and returning pass/fail
   * status through stdout. Batch mode is used in test execution.)
   */
  public boolean shouldBatch() {
    return batch;
  }

  /**
   * Returns {@code true} to indicate printing the help message.
   */
  public boolean showHelp() {
    return showHelp;
  }

  /**
   * Initialize the SDK path.
   */
  protected void initializeSdkPath() {
    if (dartSdkPath == null) {
      try {
        File directory = new File(".").getCanonicalFile();

        while (directory != null) {
          if (isSDKPath(directory)) {
            dartSdkPath = directory;

            return;
          }

          File childDir = new File(directory, "dart-sdk");

          if (isSDKPath(childDir)) {
            dartSdkPath = childDir;

            return;
          }

          directory = directory.getParentFile();
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  private boolean isSDKPath(File directory) {
    return directory.getName().equals("dart-sdk") && new File(directory, "VERSION").exists();
  }

}
