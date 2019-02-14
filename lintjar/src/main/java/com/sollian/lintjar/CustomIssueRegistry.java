package com.sollian.lintjar;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.ApiKt;
import com.android.tools.lint.detector.api.Issue;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CustomIssueRegistry extends IssueRegistry {
    @Override
    public int getApi() {
        return ApiKt.CURRENT_API;
    }

    @NotNull
    @Override
    public List<Issue> getIssues() {
        return Arrays.asList(
                LogDetector.ISSUE
                , NewThreadDetector.ISSUE
                , ConcurrentModifyDetector.ISSUE
                , ModuleAccessibleDetector.ISSUE
                , DrawableAttrDetector.ISSUE
                , PrintStackTraceDetector.ISSUE
        );
    }
}
