package com.sollian.lintjar;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;

import java.util.Collections;
import java.util.List;

/**
 * @author lishouxian on 2019/4/17.
 */
public class PopupWindowDetector extends Detector implements Detector.UastScanner {
    public static final Issue ISSUE = Issue.create(
            "PopupWindowDetector",
            "如需在区域外点击消失，需要设置背景不为null，否则6.0以下不起作用",
            "",
            Category.LINT,
            5,
            Severity.ERROR,
            new Implementation(PopupWindowDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public List<String> getApplicableConstructorTypes() {
        return Collections.singletonList("android.widget.PopupWindow");
    }

    @Override
    public void visitConstructor(@NotNull JavaContext context,
                                 @NotNull UCallExpression node,
                                 @NotNull PsiMethod constructor) {
        UElement uParent = node.getUastParent();
        if (uParent == null) {
            return;
        }

        PsiElement psiElement = uParent.getJavaPsi();
        if (psiElement == null) {
            return;
        }

        UElement reportElement = node;

        boolean findKeyMethod = false;
        PsiElement nextSibling = psiElement.getParent();
        //往下找最多5行（每一行由PsiWhiteSpace + PsiDeclarationStateMent组成）
        int count = 10;
        while (count > 0 && nextSibling != null) {
            nextSibling = nextSibling.getNextSibling();
            count--;

            if (nextSibling == null) {
                break;
            }

            //跳过注释
            if (nextSibling instanceof PsiComment) {
                continue;
            }

            String text = nextSibling.getText();
            if (text == null) {
                continue;
            }

            text = text.trim();
            if (text.isEmpty()) {
                continue;
            }

            if (text.contains("setBackgroundDrawable(null)")) {
                reportElement = context.getUastContext().convertElement(nextSibling, null, null);
                break;
            }
            if (text.contains("setBackgroundDrawable")) {
                findKeyMethod = true;
                break;
            }
        }

        if (reportElement == null) {
            reportElement = node;
        }

        if (!findKeyMethod) {
            context.report(ISSUE,
                    reportElement,
                    context.getLocation(reportElement),
                    "如需在区域外点击消失，需要设置背景不为null，否则6.0以下不起作用");
        }
    }
}