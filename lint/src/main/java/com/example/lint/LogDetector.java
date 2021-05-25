package com.example.lint;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import java.util.EnumSet;

public class LogDetector extends Detector {

    public LogDetector(){

    }
    public static final Issue ISSUE = Issue.create(
            "LogUsage",
            "不要在 AndroidManifest.xml 文件里同时设置方向和透明主题",
            "Activity 同时设置方向和透明主题在 Android 8.0 手机会 Crash",
            Category.SECURITY,
            5,
            Severity.ERROR,
            new Implementation(LogDetector.class,Scope.JAVA_FILE_SCOPE));


}
