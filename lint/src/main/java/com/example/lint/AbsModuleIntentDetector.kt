package com.example.lint

import com.android.tools.lint.checks.FragmentDetector
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass

class AbsModuleIntentDetector : Detector(), Detector.UastScanner {
    companion object {
        private const val DESC = "AbsModuleIntent的子类要有无参构造"
        private val IMPLEMENTATION = Implementation(
            AbsModuleIntentDetector::class.java, Scope.JAVA_FILE_SCOPE
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            "AbsModuleIntentIssue",
            DESC,
            DESC,
            Category.CORRECTNESS,
            10, Severity.FATAL,
            IMPLEMENTATION
        )
    }

    override fun applicableSuperClasses(): List<String>? {
        return listOf("com.example.lint.AbsModuleIntent")
    }

    override fun visitClass(context: JavaContext, declaration: UClass) {
        super.visitClass(context, declaration)
        println("visitClass:${context.file.absolutePath} ${this}")
        val constructors = declaration.constructors
        var hasEmptyConstructor = false
        constructors.forEach { constructor ->
            if (constructor.parameterList.parametersCount == 0) {
                hasEmptyConstructor = true
            }
            if (!hasEmptyConstructor) {
                val location = context.getNameLocation(constructor)
                context.report(
                    ISSUE, constructor, location, "${DESC} in ${location.file.absolutePath}"
                )
            }
        }

    }
}