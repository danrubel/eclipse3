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
package com.google.dart.tools.ui.internal.viewsupport;

import com.google.dart.core.IPackageFragmentRoot;
import com.google.dart.tools.core.model.CompilationUnit;
import com.google.dart.tools.core.model.DartElement;
import com.google.dart.tools.core.model.DartFunction;
import com.google.dart.tools.core.model.DartModelException;
import com.google.dart.tools.ui.DartElementLabels;
import com.google.dart.tools.ui.DartToolsPlugin;
import com.google.dart.tools.ui.internal.viewsupport.ColoredString.Style;
import com.google.dart.tools.ui.text.editor.tmp.Signature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class ColoredDartElementLabels {

  public static final Style QUALIFIER_STYLE = new Style(ColoredViewersManager.QUALIFIER_COLOR_NAME);
  public static final Style COUNTER_STYLE = new Style(ColoredViewersManager.COUNTER_COLOR_NAME);
  public static final Style DECORATIONS_STYLE = new Style(
      ColoredViewersManager.DECORATIONS_COLOR_NAME);

  public final static long COLORIZE = 1L << 55;

  public static ColoredString decorateColoredString(ColoredString string, String decorated,
      Style color) {
    String label = string.getString();
    int originalStart = decorated.indexOf(label);
    if (originalStart == -1) {
      return new ColoredString(decorated); // the decorator did something wild
    }
    if (originalStart > 0) {
      ColoredString newString = new ColoredString(decorated.substring(0, originalStart), color);
      newString.append(string);
      string = newString;
    }
    if (decorated.length() > originalStart + label.length()) { // decorator
                                                               // appended
                                                               // something
      return string.append(decorated.substring(originalStart + label.length()), color);
    }
    return string; // no change
  }

  /**
   * Appends the label for a compilation unit to a {@link ColoredString}. Considers the CU_* flags.
   * 
   * @param cu The element to render.
   * @param flags The rendering flags. Flags with names starting with 'CU_' are considered.
   * @param result The buffer to append the resulting label to.
   */
  public static void getCompilationUnitLabel(CompilationUnit cu, long flags, ColoredString result) {
//    if (getFlag(flags, DartElementLabels.CU_QUALIFIED)) {
//      IPackageFragment pack = (IPackageFragment) cu.getParent();
//      if (!pack.isDefaultPackage()) {
//        getPackageFragmentLabel(pack, (flags & QUALIFIER_FLAGS), result);
//        result.append('.');
//      }
//    }

    result.append(cu.getElementName());

    if (getFlag(flags, DartElementLabels.CU_POST_QUALIFIED)) {
      int offset = result.length();
      result.append(DartElementLabels.CONCAT_STRING);
//      getPackageFragmentLabel((IPackageFragment) cu.getParent(), flags
//          & QUALIFIER_FLAGS, result);
      if (getFlag(flags, COLORIZE)) {
        result.colorize(offset, result.length() - offset, QUALIFIER_STYLE);
      }
    }
  }

  public static void getFunctionLabel(DartFunction function, long flags, ColoredString result) {
//    BindingKey resolvedKey = getFlag(flags, DartElementLabels.USE_RESOLVED)
//        && method.isResolved() ? new BindingKey(method.getKey()) : null;
//    String resolvedSig = (resolvedKey != null) ? resolvedKey.toSignature()
//        : null;
//
//    // return type
//    if (getFlag(flags, DartElementLabels.M_PRE_RETURNTYPE) && method.exists()
//        && !method.isConstructor()) {
//      String returnTypeSig = resolvedSig != null
//          ? Signature.getReturnType(resolvedSig) : method.getReturnType();
//      getTypeSignatureLabel(returnTypeSig, flags, result);
//      result.append(' ');
//    }
    if (function.getElementName() != null) {
      result.append(function.getElementName());
    }
    getCommonFunctionLabelElements(function, flags, result);
  }

  /**
   * Appends the label for a package fragment root to a {@link ColoredString}. Considers the ROOT_*
   * flags.
   * 
   * @param root The element to render.
   * @param flags The rendering flags. Flags with names starting with ROOT_' are considered.
   * @param result The buffer to append the resulting label to.
   */
  public static void getPackageFragmentRootLabel(IPackageFragmentRoot root, long flags,
      ColoredString result) {
    if (root.isArchive()) {
      getArchiveLabel(root, flags, result);
    } else {
      getFolderLabel(root, flags, result);
    }
  }

  /**
   * Returns the label of the given object. The object must be of type {@link DartElement} or adapt
   * to {@link org.eclipse.ui.model.IWorkbenchAdapter}. The empty string is returned if the element
   * type is not known.
   * 
   * @param obj Object to get the label from.
   * @param flags The rendering flags
   * @return Returns the label or the empty string if the object type is not supported.
   */
  public static ColoredString getTextLabel(Object obj, long flags) {
    if (obj instanceof IResource) {
      return new ColoredString(((IResource) obj).getName());
//    } else if (obj instanceof JsGlobalScopeContainer) {
//      JsGlobalScopeContainer container = (JsGlobalScopeContainer) obj;
//      return getContainerEntryLabel(container.getClasspathEntry().getPath(),
//          container.getJavaProject());
    }
    return new ColoredString(DartElementLabels.getTextLabel(obj, flags));
  }

  private static void getArchiveLabel(IPackageFragmentRoot root, long flags, ColoredString result) {
//    // Handle variables different
//    if (getFlag(flags, DartElementLabels.ROOT_VARIABLE)
//        && getVariableLabel(root, flags, result)) {
//      return;
//    }
    boolean external = root.isExternal();
    if (external) {
      getExternalArchiveLabel(root, flags, result);
    } else {
      getInternalArchiveLabel(root, flags, result);
    }
  }

  private static void getCommonFunctionLabelElements(DartFunction method, long flags,
      ColoredString result) {
    try {
      // parameters
      result.append('(');
      if (getFlag(flags, DartElementLabels.M_PARAMETER_TYPES | DartElementLabels.M_PARAMETER_NAMES)) {
        String[] types = null;
        int nParams = 0;
        boolean renderVarargs = false;
        if (getFlag(flags, DartElementLabels.M_PARAMETER_TYPES)) {
//        if (resolvedSig != null) {
//          types = Signature.getParameterTypes(resolvedSig);
//        } else {
//          types = method.getParameterTypes();
//        }
//        nParams = types.length;
//        renderVarargs = method.exists() && Flags.isVarargs(method.getFlags());
        }
        String[] names = null;
        if (getFlag(flags, DartElementLabels.M_PARAMETER_NAMES) && method.exists()) {
          names = method.getParameterNames();
          if (types == null) {
            nParams = names.length;
//          } else { // types != null
//            if (nParams != names.length) {
//              String resolvedSig = null;
//              if (resolvedSig != null && types.length > names.length) {
//                // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=99137
//                nParams = names.length;
//                String[] typesWithoutSyntheticParams = new String[nParams];
//                System.arraycopy(types, types.length - nParams, typesWithoutSyntheticParams, 0,
//                    nParams);
//                types = typesWithoutSyntheticParams;
//              } else {
//                // https://bugs.eclipse.org/bugs/show_bug.cgi?id=101029
//                // DartToolsPlugin.logErrorMessage("DartElementLabels: Number of param types(" + nParams + ") != number of names(" + names.length + "): " + method.getElementName());   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
//                names = null; // no names rendered
//              }
//            }
          }
        }

        for (int i = 0; i < nParams; i++) {
          if (i > 0) {
            result.append(DartElementLabels.COMMA_STRING);
          }
          if (types != null) {
            String paramSig = types[i];
            if (renderVarargs && (i == nParams - 1)) {
//            int newDim = Signature.getArrayCount(paramSig) - 1;
//            getTypeSignatureLabel(Signature.getElementType(paramSig), flags,
//                result);
//            for (int k = 0; k < newDim; k++) {
//              result.append('[').append(']');
//            }
              result.append(DartElementLabels.ELLIPSIS_STRING);
            } else {
              getTypeSignatureLabel(paramSig, flags, result);
            }
          }
          if (names != null) {
            if (types != null) {
              result.append(' ');
            }
            result.append(names[i]);
          }
        }
      } else {
        if (method.getParameterNames().length > 0) {
          result.append(DartElementLabels.ELLIPSIS_STRING);
        }
      }
      result.append(')');
    } catch (DartModelException e) {
      DartToolsPlugin.log(e); // NotExistsException will not reach this point
    }
  }

  private static void getExternalArchiveLabel(IPackageFragmentRoot root, long flags,
      ColoredString result) {
    IPath path = root.getPath();
    if (getFlag(flags, DartElementLabels.REFERENCED_ROOT_POST_QUALIFIED)) {
      int segements = path.segmentCount();
      if (segements > 0) {
        result.append(path.segment(segements - 1));
        int offset = result.length();
        if (segements > 1 || path.getDevice() != null) {
          result.append(DartElementLabels.CONCAT_STRING);
          result.append(path.removeLastSegments(1).toOSString());
        }
        if (getFlag(flags, COLORIZE)) {
          result.colorize(offset, result.length() - offset, QUALIFIER_STYLE);
        }
      } else {
        result.append(path.toOSString());
      }
    } else {
      result.append(path.toOSString());
    }
  }

  private static final boolean getFlag(long flags, long flag) {
    return (flags & flag) != 0;
  }

  private static void getFolderLabel(IPackageFragmentRoot root, long flags, ColoredString result) {
    IResource resource = root.getResource();
    boolean rootQualified = getFlag(flags, DartElementLabels.ROOT_QUALIFIED);
    boolean referencedQualified = getFlag(flags, DartElementLabels.REFERENCED_ROOT_POST_QUALIFIED)
        && isReferenced(root);
    if (rootQualified) {
      result.append(root.getPath().makeRelative().toString());
    } else {
      if (resource != null) {
        IPath projectRelativePath = resource.getProjectRelativePath();
        if (projectRelativePath.segmentCount() == 0) {
          result.append(resource.getName());
          referencedQualified = false;
        } else {
          result.append(projectRelativePath.toString());
        }
      } else {
        result.append(root.getElementName());
      }
      int offset = result.length();
      if (referencedQualified) {
        result.append(DartElementLabels.CONCAT_STRING);
        result.append(resource.getProject().getName());
      } else if (getFlag(flags, DartElementLabels.ROOT_POST_QUALIFIED)) {
        result.append(DartElementLabels.CONCAT_STRING);
        result.append(root.getParent().getElementName());
      } else {
        return;
      }
      if (getFlag(flags, COLORIZE)) {
        result.colorize(offset, result.length() - offset, QUALIFIER_STYLE);
      }
    }
  }

  private static void getInternalArchiveLabel(IPackageFragmentRoot root, long flags,
      ColoredString result) {
    IResource resource = root.getResource();
    boolean rootQualified = getFlag(flags, DartElementLabels.ROOT_QUALIFIED);
    boolean referencedQualified = getFlag(flags, DartElementLabels.REFERENCED_ROOT_POST_QUALIFIED)
        && isReferenced(root);
    if (rootQualified) {
      result.append(root.getPath().makeRelative().toString());
    } else {
      result.append(root.getElementName());
      int offset = result.length();
      if (referencedQualified) {
        result.append(DartElementLabels.CONCAT_STRING);
        result.append(resource.getParent().getFullPath().makeRelative().toString());
      } else if (getFlag(flags, DartElementLabels.ROOT_POST_QUALIFIED)) {
        result.append(DartElementLabels.CONCAT_STRING);
        result.append(root.getParent().getPath().makeRelative().toString());
      } else {
        return;
      }
      if (getFlag(flags, COLORIZE)) {
        result.colorize(offset, result.length() - offset, QUALIFIER_STYLE);
      }
    }
  }

  private static void getTypeArgumentSignaturesLabel(String[] typeArgsSig, long flags,
      ColoredString result) {
    if (typeArgsSig.length > 0) {
      result.append('<');
      for (int i = 0; i < typeArgsSig.length; i++) {
        if (i > 0) {
          result.append(DartElementLabels.COMMA_STRING);
        }
        getTypeSignatureLabel(typeArgsSig[i], flags, result);
      }
      result.append('>');
    }
  }

  @SuppressWarnings("unused")
  private static void getTypeParameterSignaturesLabel(String[] typeParamSigs, long flags,
      ColoredString result) {
    if (typeParamSigs.length > 0) {
      result.append('<');
      for (int i = 0; i < typeParamSigs.length; i++) {
        if (i > 0) {
          result.append(DartElementLabels.COMMA_STRING);
        }
        result.append(Signature.getTypeVariable(typeParamSigs[i]));
      }
      result.append('>');
    }
  }

  private static void getTypeSignatureLabel(String typeSig, long flags, ColoredString result) {
    int sigKind = Signature.getTypeSignatureKind(typeSig);
    switch (sigKind) {
      case Signature.BASE_TYPE_SIGNATURE:
        result.append(Signature.toString(typeSig));
        break;
      case Signature.ARRAY_TYPE_SIGNATURE:
        getTypeSignatureLabel(Signature.getElementType(typeSig), flags, result);
        for (int dim = Signature.getArrayCount(typeSig); dim > 0; dim--) {
          result.append('[').append(']');
        }
        break;
      case Signature.CLASS_TYPE_SIGNATURE:
        String baseType = Signature.toString(typeSig);
        result.append(Signature.getSimpleName(baseType));

        getTypeArgumentSignaturesLabel(new String[0], flags, result);
        break;
      default:
        // unknown
    }
  }

  /**
   * @param root
   * @return <code>true</code> if the given package fragment root is referenced. This means it is
   *         owned by a different project but is referenced by the root's parent. Returns
   *         <code>false</code> if the given root doesn't have an underlying resource.
   */
  private static boolean isReferenced(IPackageFragmentRoot root) {
    IResource resource = root.getResource();
    if (resource != null) {
      IProject jarProject = resource.getProject();
      IProject container = root.getJavaScriptProject().getProject();
      return !container.equals(jarProject);
    }
    return false;
  }

}
