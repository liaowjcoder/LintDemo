package com.example.lint

import com.android.tools.lint.checks.FragmentDetector
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass

class FragmentConstructorDetector : Detector(), Detector.UastScanner {
    companion object {
        private const val DESC = "Fragment要有无参构造，因为在内存重启时需要反射创建"
        private val IMPLEMENTATION = Implementation(
            FragmentConstructorDetector::class.java, Scope.JAVA_FILE_SCOPE
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            "FragmentConstructorIssue",
            DESC,
            DESC,
            Category.CORRECTNESS,
            10, Severity.WARNING,
            IMPLEMENTATION
        )
    }

    override fun applicableSuperClasses(): List<String>? {
        return listOf("androidx.fragment.app.Fragment")
    }

    override fun visitClass(context: JavaContext, declaration: UClass) {
        super.visitClass(context, declaration)
        val constructors = declaration.constructors
        var hasEmptyConstructor = false
        constructors.forEach { constructor ->
            if (constructor.parameterList.parametersCount == 0) {
                hasEmptyConstructor = true
            }
            if (!hasEmptyConstructor) {
                println("FragmentConstructorDetector:visitClass:${context.file.absolutePath}")
                val location = context.getNameLocation(constructor)
                context.report(
                    ISSUE, constructor, location, "${DESC} in ${location.file.absolutePath}"
                )
            }
        }

    }
}