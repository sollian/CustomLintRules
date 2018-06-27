package com.sollian.lintjar;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.uast.UBlockExpression;
import org.jetbrains.uast.UBreakExpression;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UFile;
import org.jetbrains.uast.UForEachExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UReturnExpression;
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

    /**
     * for(Object obj: list) //此处的list为forListName{<br />
     * &nbsp;&nbsp;&nbsp;&nbsp;if(...){<br />
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;list.remove(obj); //此处的list为listName<br />
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;break;<br />
     * &nbsp;&nbsp;&nbsp;&nbsp;}<br />
     * }<br />
     *
     * @param context
     * @param node
     * @param method
     */
    @Override
    public void visitMethod(JavaContext context, UCallExpression node, PsiMethod method) {
        if (node == null || !context.getEvaluator().isMemberInClass(method, "java.util.List")) {
            return;
        }

        UExpression expression = node.getReceiver();
        if (!(expression instanceof USimpleNameReferenceExpression)) {
            return;
        }
        USimpleNameReferenceExpression simpleNameReferenceExpression = (USimpleNameReferenceExpression) expression;
        String listName = simpleNameReferenceExpression.getIdentifier();

        UElement parent = node;
        UElement tmpElemnt = null;
        boolean findBlock = false;
        while (true) {
            parent = parent.getUastParent();
            if (parent == null
                    || parent instanceof UMethod
                    || parent instanceof UClass
                    || parent instanceof UFile) {
                return;
            }

            if (!findBlock && parent instanceof UBlockExpression) {
                findBlock = true;
                if (findBreakOrReturn(parent, tmpElemnt)) {
                    return;
                }
            }

            if (!findBlock) {
                tmpElemnt = parent;
            }

            if (findBlock && findForExpression(parent, listName)) {
                context.report(ISSUE, node, context.getLocation(node), "可能引起ConcurrentModificationException");
                return;
            }
        }
    }

    /**
     * 若有break 或者return 语句，则排除。
     */
    private static boolean findBreakOrReturn(UElement parent, UElement tmpElemnt) {
        List<UExpression> expressions = ((UBlockExpression) parent).getExpressions();
        int index = tmpElemnt instanceof UExpression ? expressions.indexOf(tmpElemnt) : 0;
        for (int i = expressions.size() - 1; i >= index; i--) {
            UExpression exp = expressions.get(i);
            if (exp instanceof UBreakExpression
                    || exp instanceof UReturnExpression) {
                return true;
            }
        }
        return false;
    }

    private static boolean findForExpression(UElement parent, String listName) {
        if (!(parent instanceof UForEachExpression)) {
            return false;
        }

        UForEachExpression forEachExpression = (UForEachExpression) parent;
        String forListName = ((USimpleNameReferenceExpression) forEachExpression.getIteratedValue()).getIdentifier();

        return forListName.equals(listName);
    }
}
