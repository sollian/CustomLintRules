package com.sollian.lintjar;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.uast.UCallExpression;

import java.util.Arrays;
import java.util.List;

public class PrintStackTraceDetector extends com.android.tools.lint.detector.api.Detector implements com.android.tools.lint.detector.api.Detector.UastScanner {

    public static final Issue ISSUE = Issue.create(
            "PrintStackTraceUsage",
            "避免直接调用Throwable.printStackTrace()",
            "直接调用Throwable.printStackTrace()可能引起OOM",
            Category.LINT, 5, Severity.ERROR,
            new Implementation(PrintStackTraceDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("printStackTrace");
    }

    @Override
    public void visitMethod(JavaContext context, UCallExpression node, PsiMethod method) {
        if (context.getEvaluator().isMemberInClass(method, "java.lang.Throwable")) {
            context.report(ISSUE, node, context.getLocation(node), "直接调用Throwable.printStackTrace()可能引起OOM，使用自定义方法替代");
        }
    }
}