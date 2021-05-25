package com.example.lint

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import org.w3c.dom.Element
import java.util.*
import java.util.regex.Pattern

class ChineseCheckDetector : Detector(), XmlScanner {
    override fun visitElement(context: XmlContext, element: Element) {
        println(">>>>>>>>ChineseCheckDetector<<<<<<<<<")
        when (element.tagName) {
            SdkConstants.TAG_STRING -> {
                val hasNameAttr = element.hasAttribute("name")
                if (hasNameAttr) {
                    val textContent = element.textContent
                    if (checkHasChineseValue(textContent)) {
                        println("textContent $textContent")
                        context.report(
                            ISSUE,
                            element,
                            context.getLocation(element),
                            DESC
                        )
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun getApplicableElements(): Collection<String>? {
        return listOf(SdkConstants.TAG_STRING)
    }

    companion object {
        private const val DESC = "不要在strings.xml中定义中文的value文案"
        private val IMPLEMENTATION = Implementation(
            ChineseCheckDetector::class.java, EnumSet.of(Scope.MANIFEST, Scope.ALL_RESOURCE_FILES)
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            "ChineseCheckFetal",
            DESC,
            DESC,
            CORRECTNESS,
            8, Severity.FATAL,
            IMPLEMENTATION
        )

        private fun checkHasChineseValue(textContent: String): Boolean {
            val regex = "[\\u4e00-\\u9fa5]".toRegex()
            val ret = regex.find(textContent)?.value
            return ret != null
        }
    }


}