package com.example.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.ApiKt;
import com.android.tools.lint.detector.api.Issue;
import com.google.common.annotations.Beta;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class IssueRegister extends IssueRegistry {


    @NotNull
    @Override
    public List<Issue> getIssues() {
        ArrayList<Issue> issues = new ArrayList<>();
        issues.add(ParseColorDetector.ISSUE);
        issues.add(ChineseCheckDetector.ISSUE);
        issues.add(AbsModuleIntentDetector.ISSUE);
        issues.add(FragmentConstructorDetector.ISSUE);
        issues.add(LogDetector.ISSUE);
        return issues;
    }

    @Override
    public int getApi() {
        return ApiKt.CURRENT_API;
    }
}