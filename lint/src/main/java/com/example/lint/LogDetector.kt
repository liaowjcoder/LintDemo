package com.example.lint

import com.android.tools.lint.detector.api.*
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import org.jetbrains.uast.UCallExpression
import java.util.*


class LogDetector : Detector(), Detector.UastScanner {
    override fun getApplicableMethodNames(): List<String>? {
        return listOf("v", "d", "i", "w", "e", "wtf")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        super.visitMethodCall(context, node, method)
        println("LogDetector:visitMethodCall ${method.name} ${context.getLocation(node).file.absolutePath}")
        if (context.evaluator.isMemberInClass(method, "android.util.Log")) {
            context.report(
                ISSUE,
                node,
                context.getLocation(node),
                "请勿直接调用android.util.Log，应该使用统一工具类"
            )
        }
    }

    companion object {
        @JvmField
        val ISSUE: Issue = Issue.create(
            "LogUsage",
            "避免调用android.util.Log",
            "请勿直接调用android.util.Log，应该使用统一工具类",
            Category.SECURITY, 5, Severity.ERROR,
            Implementation(LogDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }
}