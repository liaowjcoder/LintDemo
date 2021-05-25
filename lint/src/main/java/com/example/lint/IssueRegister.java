package com.example.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.ApiKt;
import com.android.tools.lint.detector.api.Issue;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class IssueRegister extends IssueRegistry {


    @NotNull
    @Override
    public List<Issue> getIssues() {
        ArrayList issues = new ArrayList<Issue>();
        issues.add(ParseColorDetector.ISSUE);
        issues.add(ChineseCheckDetector.ISSUE);
        return issues;
    }
}