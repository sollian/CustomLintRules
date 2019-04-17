package com.sollian.lintjar;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
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
public class LinearLayoutManagerDetector extends Detector implements Detector.UastScanner {
    public static final Issue ISSUE = Issue.create(
            "LinearLayoutManagerDetector",
            "如需覆写Adapter的onViewDetachedFromWindow方法，请调用LinearLayoutManager#setRecycleChildrenOnDetach方法",
            "默认情况下，Adapter的onViewDetachedFromWindow在页面退出时，不会被调用。如果有解注册的行为，可能会引起内存泄漏",
            Category.LINT,
            5,
            Severity.ERROR,
            new Implementation(LinearLayoutManagerDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public List<String> getApplicableConstructorTypes() {
        return Collections.singletonList(
                "androidx.recyclerview.widget.LinearLayoutManager"
        );
    }

    @Override
    public void visitConstructor(@NotNull JavaContext context,
                                 @NotNull UCallExpression node,
                                 @NotNull PsiMethod constructor) {
        Location location = context.getLocation(node);

        UElement uParent = node.getUastParent();
        if (uParent == null) {
            return;
        }

        PsiElement psiElement = uParent.getJavaPsi();
        if (psiElement == null) {
            return;
        }

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

            if (text.contains("setRecycleChildrenOnDetach")) {
                findKeyMethod = true;
                break;
            }
        }

        if (!findKeyMethod) {
            context.report(ISSUE,
                    node,
                    location,
                    "如需覆写Adapter的onViewDetachedFromWindow方法，请调用LinearLayoutManager#setRecycleChildrenOnDetach方法");
        }
    }
}