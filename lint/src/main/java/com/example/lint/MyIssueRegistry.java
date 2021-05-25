package com.example.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class MyIssueRegistry extends IssueRegistry {
    @NotNull
    @Override
    public List<Issue> getIssues() {
        System.out.println("'''''''''''''''''''");
        List<Issue> issues = new ArrayList<Issue>();
        issues.add(FixOrientationTransDetector.ISSUE);
        issues.add(ParseColorDetector.ISSUE);
        return issues;
    }
}