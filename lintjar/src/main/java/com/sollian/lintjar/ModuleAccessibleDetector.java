package com.sollian.lintjar;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UQualifiedReferenceExpression;
import org.jetbrains.uast.UTypeReferenceExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sollian on 2018/4/11.
 */
public class ModuleAccessibleDetector extends Detector implements Detector.UastScanner {
    public static final Issue ISSUE = Issue.create(
            "ModuleAccessible",
            "该类/方法/字段模块私有，外部禁止使用",
            "避免使用模块私有的类/方法/字段",
            Category.A11Y,
            5,
            Severity.ERROR,
            new Implementation(ModuleAccessibleDetector.class, Scope.JAVA_FILE_SCOPE)
    );

    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> list = new ArrayList<>();
        list.add(UElement.class);
        list.add(UTypeReferenceExpression.class);
        list.add(UCallExpression.class);
        list.add(UQualifiedReferenceExpression.class);
        return list;
    }

    @Nullable
    @Override
    public UElementHandler createUastHandler(@NotNull final JavaContext context) {
        return new UElementHandler() {
            @Override
            public void visitElement(@NotNull UElement node) {
                PsiElement psiElement = node.getJavaPsi();
                if (!(psiElement instanceof PsiNewExpression)) {
                    return;
                }

                PsiNewExpression psiNewExpression = (PsiNewExpression) psiElement;
                PsiJavaCodeReferenceElement psiJavaCodeReferenceElement = psiNewExpression.getClassReference();
                if (psiJavaCodeReferenceElement == null) {
                    return;
                }

                PsiElement psiElement1 = psiJavaCodeReferenceElement.resolve();
                if (!(psiElement1 instanceof PsiClass)) {
                    return;
                }

                process(context, node, (PsiModifierListOwner) psiElement1);
            }

            @Override
            public void visitTypeReferenceExpression(@NotNull UTypeReferenceExpression node) {
                PsiType psiType = node.getType();
                if (!(psiType instanceof PsiClassType)) {
                    return;
                }
                PsiClassType psiClassType = (PsiClassType) psiType;
                PsiClass psiClass = psiClassType.resolve();
                process(context, node, psiClass);
            }

            @Override
            public void visitCallExpression(@NotNull UCallExpression node) {
                PsiMethod psiMethod = node.resolve();
                if (psiMethod == null) {
                    return;
                }
                if (process(context, node, psiMethod.getContainingClass())) {
                    return;
                }

                process(context, node, psiMethod);
            }

            @Override
            public void visitQualifiedReferenceExpression(@NotNull UQualifiedReferenceExpression node) {
                PsiElement psiElement = node.getPsi();
                if (!(psiElement instanceof PsiReference)) {
                    return;
                }
                PsiReference psiReference = (PsiReference) psiElement;
                PsiElement psiElement1 = psiReference.resolve();
                if (!(psiElement1 instanceof PsiField)) {
                    return;
                }

                PsiMember psiField = (PsiMember) psiElement1;
                if (process(context, node, psiField.getContainingClass())) {
                    return;
                }

                process(context, node, psiField);
            }
        };
    }

    private static boolean process(JavaContext context, UElement psiElement, PsiModifierListOwner resolvedElement) {
        if (resolvedElement == null) {
            return false;
        }
        PsiAnnotation[] psiAnnotations = context.getEvaluator().getAllAnnotations(resolvedElement, true);
        if (psiAnnotations.length <= 0) {
            return false;
        }
        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            String name = psiAnnotation.getQualifiedName();
            if (name != null && name.equals(Constants.ANNOTATION_ROOT + "ModuleAccessible")) {
                String projectName = context.getProject().getName();
                String moduleName = getModuleName(resolvedElement);
                //得到的moduleName可能是“Module/base”，所以要使用endsWith判断
                if (!moduleName.endsWith(projectName)) {
                    StringBuilder sb = new StringBuilder();
                    if (resolvedElement instanceof PsiClass) {
                        sb.append("该类");
                    } else if (resolvedElement instanceof PsiMethod) {
                        sb.append("该方法");
                    } else if (resolvedElement instanceof PsiField) {
                        sb.append("该字段");
                    } else {
                        sb.append("该类/方法/字段");
                    }

                    sb.append("属于")
                            .append(moduleName)
                            .append("模块私有，")
                            .append(projectName)
                            .append("不可访问");

                    context.report(ISSUE, psiElement, context.getLocation(psiElement),
                            sb.toString());
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private static String getModuleName(PsiElement element) {
        String rootProjectName = element.getProject().getName();
        String absolutePath = element.getContainingFile().getVirtualFile().getPath();

        String moduleName = absolutePath;
        int index = absolutePath.indexOf(rootProjectName);
        if (index > 0) {
            moduleName = moduleName.substring(index + rootProjectName.length() + 1);
        }
        index = moduleName.indexOf("/src/");
        if (index > 0) {
            moduleName = moduleName.substring(0, index);
        }
        return moduleName;
    }
}
