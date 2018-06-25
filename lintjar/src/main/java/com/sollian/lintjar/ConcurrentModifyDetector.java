package com.sollian.lintjar;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UFile;
import org.jetbrains.uast.UForEachExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.USimpleNameReferenceExpression;

import java.util.Arrays;
import java.util.List;

/**
 * @author admin on 2018/6/25.
 */
public class ConcurrentModifyDetector extends Detector implements Detector.UastScanner {
    public static final Issue ISSUE = Issue.create(
            "ConcurrentModificationException",
            "可能引起ConcurrentModificationException",
            "可能引起ConcurrentModificationException",
            Category.SECURITY,
            9,
            Severity.ERROR,
            new Implementation(ConcurrentModifyDetector.class, Scope.JAVA_FILE_SCOPE)
    );

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("remove");
    }

    @Override
    public void visitMethod(JavaContext context, UCallExpression node, PsiMethod method) {
        if (node != null && context.getEvaluator().isMemberInClass(method, "java.util.List")) {
            UElement parent = node;
            while (true) {
                parent = parent.getUastParent();
                if (parent == null) {
                    break;
                }

                if (process(context, parent, node)) {
                    break;
                }
            }
        }
    }

    /**
     * @return true：已处理；false：未处理
     */
    private boolean process(JavaContext context, UElement parent, UCallExpression node) {
        if (parent instanceof UMethod
                || parent instanceof UClass
                || parent instanceof UFile) {
            return true;
        }

        if (!(parent instanceof UForEachExpression)) {
            return false;
        }

        UExpression expression = node.getReceiver();
        if (!(expression instanceof USimpleNameReferenceExpression)) {
            return false;
        }
        USimpleNameReferenceExpression simpleNameReferenceExpression = (USimpleNameReferenceExpression) expression;
        String listName = simpleNameReferenceExpression.getIdentifier();

        UForEachExpression forEachExpression = (UForEachExpression) parent;
        String forListName = ((USimpleNameReferenceExpression) forEachExpression.getIteratedValue()).getIdentifier();

        if (forListName.equals(listName)) {
            context.report(ISSUE, node, context.getLocation(node), getLogInfo());
            return true;
        }
        return false;
    }

    private static String getLogInfo() {
        return "可能引起ConcurrentModificationException\n"
                + "可以使用如下形式：\n"
                + "for(int i = list.size();i>=0;i--){\n"
                + "    Object item = list.get(i);\n"
                + "    if(...) {\n"
                + "        list.remove(item);\n"
                + "    }\n"
                + "}\n";
    }
}
