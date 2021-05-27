package com.example.lint;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTryStatement;
import com.intellij.psi.impl.source.tree.java.MethodElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UTryExpression;
import org.jetbrains.uast.UastUtils;
import org.jetbrains.uast.kotlin.KotlinUFile;

import java.util.Collections;
import java.util.List;

public class ParseColorDetector extends Detector implements Detector.UastScanner {
    private static final Implementation IMPLEMENTATION =
            new Implementation(ParseColorDetector.class, Scope.JAVA_FILE_SCOPE);
    public static final Issue ISSUE = Issue.create(
            "ParseColorError",
            "Color crash",
            "导致 crash",
            Category.SECURITY,
            10,
            Severity.FATAL, IMPLEMENTATION)
            .setAndroidSpecific(true);

    @Override
    public List<String> getApplicableMethodNames() {
        return Collections.singletonList("parseColor");
    }

    @Override
    public void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression node,
                                @NotNull PsiMethod method) {
        // 不是 android.graphics.Color 类的方法，直接返回
        if (!context.getEvaluator().isMemberInClass(method, "android.graphics.Color")) {
            return;
        }
        // 参数写死的比如 "#FFFFFF" 这种，简单判断如果是 # 号开头，直接返回
        if (isConstColor(node)) {
            return;
        }
        // 已经做了 try catch 处理，直接返回
        if (isWrappedByTryCatch(node, context)) {
            return;
        }
        System.out.println("ParseColorDetector error");
        reportError(context, node);
    }

    private boolean isConstColor(UCallExpression node) {
        try {
            return node.getValueArguments().get(0).evaluate().toString().startsWith("#");
        }catch (Exception e){
//            e.printStackTrace();
            return true;
        }
    }

    private boolean isWrappedByTryCatch(UCallExpression node, JavaContext context) {
        if (context.getUastFile() instanceof KotlinUFile) {
            return UastUtils.getParentOfType(node.getUastParent(), UTryExpression.class) != null;
        }
        for (PsiElement parent = node.getSourcePsi().getParent(); parent != null && !(parent instanceof MethodElement); parent = parent.getParent()) {
            if (parent instanceof PsiTryStatement) {
                return true;
            }
        }
        return false;
    }

    private void reportError(JavaContext context, UCallExpression node) {
        context.report(ISSUE, node, context.getLocation(node), "调用Color.parse建议 try-catch，色值不合规导致避免crash "+
                context.getLocation(node).getFile().getAbsolutePath());
    }
}