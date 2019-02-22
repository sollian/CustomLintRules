package com.sollian.lintjar;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UImportStatement;

import java.util.Arrays;
import java.util.List;

public class PrintStackTraceDetector extends com.android.tools.lint.detector.api.Detector
        implements com.android.tools.lint.detector.api.Detector.UastScanner {

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
    public void visitMethodCall(@NotNull JavaContext context,
                                @NotNull UCallExpression node,
                                @NotNull PsiMethod method) {
        if (context.getEvaluator().isMemberInClass(method, "java.lang.Throwable")) {
            /*
            报告该问题
             */
            context.report(ISSUE,
                    method,
                    context.getLocation(node),
                    "直接调用Throwable.printStackTrace()可能引起OOM，使用自定义方法替代",
                    getLintFix(context, node));
        }
    }

    /**
     * lint自动修复
     */
    private LintFix getLintFix(@NotNull JavaContext context,
                               @NotNull UCallExpression node) {
        /*
            先检查当前文件是否import了我们需要的类
             */
        boolean hasImport = false;
        List<UImportStatement> list = context.getUastFile().getImports();
        for (UImportStatement statement : list) {
            UElement element = statement.getImportReference();
            if ("com.sollian.customlintrules.utils.LogUtils".endsWith(element.asRenderString())) {
                hasImport = true;
                break;
            }
        }

       /*
        第一个修复，替换方法调用
        */
        LintFix fix = fix().replace()
                .all()
                .with("LogUtils.printStaceTrace(" + node.getReceiver().asRenderString() + ')')
                .autoFix()
                .build();

        /*
         第二个修复，import LogUtils类
         */
        LintFix importFix = null;
        if (!hasImport) {
            UImportStatement statement = list.get(list.size() - 1);
            String lastImport = statement.asRenderString() + ';';
            importFix = fix().replace()
                    //最后的一条import语句
                    .text(lastImport)
                    //替换为最后一条import语句，加上LogUtils类
                    .with(lastImport + "\nimport com.sollian.customlintrules.utils.LogUtils;")
                    //替换位置
                    .range(context.getLocation(statement))
                    .autoFix()
                    .build();
        }

        /*
         最终的修复方案
         */
        LintFix.GroupBuilder builder = fix().name("使用LogUtils.printStackTrace替换").composite();
        builder.add(fix);
        if (importFix != null) {
            builder.add(importFix);
        }

        return builder.build();
    }
}