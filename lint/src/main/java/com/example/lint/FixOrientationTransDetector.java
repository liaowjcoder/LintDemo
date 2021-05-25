package com.example.lint;

import com.android.SdkConstants;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;
import com.android.tools.lint.detector.api.XmlScanner;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class FixOrientationTransDetector extends Detector implements XmlScanner {
    private static final Implementation IMPLEMENTATION =
            new Implementation(FixOrientationTransDetector.class, EnumSet.of(Scope.MANIFEST,
                    Scope.ALL_RESOURCE_FILES));

    public static final Issue ISSUE = Issue.create(
            "FixOrientationTransError",
            "不要在 AndroidManifest.xml 文件里同时设置方向和透明主题",
            "Activity 同时设置方向和透明主题在 Android 8.0 手机会 Crash",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            IMPLEMENTATION);


    private final Map<ElementEntity, String> mThemeMap = new HashMap<>();

    @Override
    public void visitElement(@NotNull XmlContext context, @NotNull Element element) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>1");
        switch (element.getTagName()) {
            case SdkConstants.TAG_ACTIVITY:
                if (isFixedOrientation(element)) {
                    String theme = element.getAttributeNS(SdkConstants.ANDROID_URI,
                            SdkConstants.ATTR_THEME);
                    if ("@style/Theme.AppTheme.Transparent".equals(theme)) {
                        reportError(context, element);
                    } else {
                        // 将主题设置暂存起来
                        mThemeMap.put(new ElementEntity(context, element),
                                theme.substring(theme.indexOf('/') + 1));
                    }
                }
                break;
            case SdkConstants.TAG_STYLE:
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>2");
                String styleName = element.getAttribute(SdkConstants.ATTR_NAME);
                mThemeMap.forEach((elementEntity, theme) -> {
                    if (theme.equals(styleName)) {
                        if (isTranslucentOrFloating(element)) {
                            reportError(elementEntity.getContext(), elementEntity.getElement());
                        } else if (element.hasAttribute(SdkConstants.ATTR_PARENT)) {
                            // 替换成父主题
                            mThemeMap.put(elementEntity,
                                    element.getAttribute(SdkConstants.ATTR_PARENT));
                        }
                    }
                });
                break;
            default:
        }
    }

    private boolean isFixedOrientation(Element element) {
        switch (element.getAttributeNS(SdkConstants.ANDROID_URI, "screenOrientation")) {
            case "landscape":
            case "sensorLandscape":
            case "reverseLandscape":
            case "userLandscape":
            case "portrait":
            case "sensorPortrait":
            case "reversePortrait":
            case "userPortrait":
            case "locked":
                return true;
            default:
                return false;
        }
    }

    private boolean isTranslucentOrFloating(Element element) {
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element
                    && SdkConstants.TAG_ITEM.equals(((Element) child).getTagName())
                    && child.getFirstChild() != null
                    && SdkConstants.VALUE_TRUE.equals(child.getFirstChild().getNodeValue())) {
                switch (((Element) child).getAttribute(SdkConstants.ATTR_NAME)) {
                    case "android:windowIsTranslucent":
                    case "android:windowSwipeToDismiss":
                    case "android:windowIsFloating":
                        return true;
                    default:
                }
            }
        }
        return "Theme.AppTheme.Transparent".equals(element.getAttribute(SdkConstants.ATTR_PARENT));
    }

    private void reportError(XmlContext context, Element element) {
        context.report(
                ISSUE,
                element,
                context.getLocation(element),
                "请不要在 AndroidManifest.xml 文件里同时设置方向和透明主题"
        );
    }

    private static class ElementEntity {
        private final XmlContext mContext;
        private final Element mElement;

        public ElementEntity(XmlContext context, Element element) {
            mContext = context;
            mElement = element;
        }

        public XmlContext getContext() {
            return mContext;
        }

        public Element getElement() {
            return mElement;
        }
    }

    @Override
    public Collection<String> getApplicableElements() {
        return Arrays.asList(SdkConstants.TAG_ACTIVITY, SdkConstants.TAG_STYLE);
    }
}