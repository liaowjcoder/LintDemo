package com.example.lint

import com.android.SdkConstants
import com.android.tools.lint.detector.api.*
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import org.w3c.dom.Element
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap
import kotlin.random.Random

 class ChineseCheckDetector : Detector(), XmlScanner {
    override fun visitElement(context: XmlContext, element: Element) {
        when (element.tagName) {
            SdkConstants.TAG_STRING -> {
                val location = context.getLocation(element)
                // /Users/liaowj/.gradle/caches/transforms-2/files-2.1/d4610295426ca4e1edf3464da1b79cf2/appcompat-1.0.0/res/values-zh-rHK/values-zh-rHK.xml
                // /Users/liaowj/Documents/code/openSource/Lint/app/src/main/res/values/strings.xml
                //location.file.absolutePath.startsWith()
                if (location.file.absolutePath.contains(".gradle")) {
                    return
                }

                if (location.file.absolutePath.contains("values-ja")) {
                    //println("过滤掉日语的检测 ${location.file.absolutePath}")
                    return
                }
                if (location.file.absolutePath.contains("values-zh")) {
                    //println("过滤掉日语的检测 ${location.file.absolutePath}")
                    return
                }

                val hasNameAttr = element.hasAttribute("name")
                if (hasNameAttr) {
                    val textContent = element.textContent
                    if (checkHasChineseValue(textContent)) {
                        val nameAttr = element.getAttributeNode("name")
                        println("遍历到的文案：${nameAttr.name}=${nameAttr.value}")
                        println(
                            "ChineseCheckDetector 有问题的文案 $textContent \n ${
                                context.getLocation(
                                    element
                                ).file.absolutePath
                            }"
                        )
                        context.report(
                            ISSUE,
                            element,
                            context.getLocation(element),
                            DESC+"${nameAttr.name}=${textContent}"
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
            ChineseCheckDetector::class.java, Scope.RESOURCE_FILE_SCOPE
        )

        @JvmField
        val ISSUE: Issue = Issue.create(
            "ChineseCheckFetal",
            DESC,
            DESC,
            CORRECTNESS,
            10, Severity.FATAL,
            IMPLEMENTATION
        )

        private fun checkHasChineseValue(textContent: String): Boolean {
            val regex = "[\\u4e00-\\u9fa5]".toRegex()
            val ret = regex.find(textContent)?.value
            return ret != null
        }
    }


}