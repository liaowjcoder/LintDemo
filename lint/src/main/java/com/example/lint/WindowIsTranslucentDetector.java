package com.example.lint;

import com.android.SdkConstants;
import com.android.ide.common.resources.usage.ResourceUsageModel;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static com.android.ide.common.resources.usage.ResourceUsageModel.getResourceFieldName;

public class WindowIsTranslucentDetector extends ResourceXmlDetector {
    private static final Implementation IMPLEMENTATION =
            new Implementation(WindowIsTranslucentDetector.class, EnumSet.of(Scope.MANIFEST,
                    Scope.ALL_RESOURCE_FILES));

    public static final Issue ISSUE = Issue.create(
            "WindowIsTranslucentError",
            "请确认有没有在AndroidManifest.xml 文件里同时设置方向和透明主题：android:windowIsTranslucent",
            "Activity 同时设置方向和透明主题在 Android 8.0 手机会 Crash",
            Category.CORRECTNESS,
            8,
            Severity.WARNING,
            IMPLEMENTATION);

    @Override
    public boolean appliesTo(ResourceFolderType folderType) {
        return folderType == ResourceFolderType.VALUES;//只关注 values 文件夹下的资源
    }

    private final Map<ElementEntity, String> mThemeMap = new HashMap<>();

    @Override
    public void visitElement(@NotNull XmlContext context, @NotNull Element element) {
        super.visitElement(context, element);

        System.out.println("visitElement " + element.getTagName() + " file path " + context.file.getAbsolutePath());

        if (isTranslucentOrFloating(element)) {
            reportError(context,element);
        }
    }


    //    @Override
//    public void visitElement(@NotNull XmlContext context, @NotNull Element element) {
//
//        switch (element.getTagName()) {
//            case SdkConstants.TAG_ACTIVITY:
//                System.out.println("visitElement " + element.getTagName() + " file path " + context.file.getAbsolutePath());
//                if (isFixedOrientation(element)) {
//                    String theme = element.getAttributeNS(SdkConstants.ANDROID_URI,
//                            SdkConstants.ATTR_THEME);
//                    System.out.println("获取 activity theme " + theme);
//                    if ("@style/Theme.AppTheme.Transparent".equals(theme)) {
//                        reportError(context, element);
//                    } else {
//                        System.out.println("将主题" + theme + "暂存起来");
//                        // 将主题设置暂存起来
//                        mThemeMap.put(new ElementEntity(context, element),
//                                theme.substring(theme.indexOf('/') + 1));
//                    }
//                }
//                break;
//            case SdkConstants.TAG_STYLE:
//                System.out.println("visitElement " + element.getTagName() + " file path " + context.file.getAbsolutePath());
//                String styleName = element.getAttribute(SdkConstants.ATTR_NAME);
//                System.out.println("检测到 style 标签：ATTR_NAME is " + styleName);
//
//                mThemeMap.forEach((elementEntity, theme) -> {
//                    System.out.println("遍历得到 mThemeMap elements is " + elements + " theme is " + theme);
//                    if (theme.equals(styleName)) {
//                        if (isTranslucentOrFloating(element)) {
//                            reportError(elementEntity.getContext(), elementEntity.getElement());
//                        } else if (element.hasAttribute(SdkConstants.ATTR_PARENT)) {
////                            element.getAttribute("SdkConstants.ATTR_PARENT");
//                            System.out.println("替换成父主题 " + element.getAttribute(SdkConstants.ATTR_PARENT) + " theme is " + theme);
//                            // 替换成父主题
//                            mThemeMap.put(elementEntity,
//                                    element.getAttribute(SdkConstants.ATTR_PARENT));
//                            //将 parent 存起来。
//                            elements.put(element, element.getAttribute(SdkConstants.ATTR_PARENT));
//                        }
//                    }
//                });
//                break;
//            default:
//        }
//    }

    private HashMap<Element, String> elements = new HashMap<>();

    public ResourceUsageModel.Resource getResource(Element element, boolean declare) {
        ResourceType type = ResourceType.fromXmlTag(element);
        if (type != null) {
            String name = getResourceFieldName(element);
            ResourceUsageModel.Resource resource = null;
            if (resource == null && declare) {
                String value = null;
                int realValue = value != null ? Integer.decode(value) : -1;
                resource = new ResourceUsageModel.Resource(type, name, realValue);
                resource.setDeclared(true);
            }
            return resource;
        }

        return null;
    }

    private boolean isFixedOrientation(Element element) {
        System.out.println("isFixedOrientation " + element.getTagName());
        System.out.println("获取到 screenOrientation 是：" + element.getAttributeNS(SdkConstants.ANDROID_URI, "screenOrientation"));
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
        System.out.println("isTranslucentOrFloating " + element.getTagName());
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            //<item>
            System.out.println("isTranslucentOrFloating child.getNodeName " + child.getNodeName());
            if (child instanceof Element
                    && SdkConstants.TAG_ITEM.equals(((Element) child).getTagName())
                    && child.getFirstChild() != null
                    && SdkConstants.VALUE_TRUE.equals(child.getFirstChild().getNodeValue())) {


                System.out.println("isTranslucentOrFloating " + ((Element) child).getAttribute(SdkConstants.ATTR_NAME) + " child.getFirstChild().getNodeValue() " + child.getFirstChild().getNodeValue());
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
        System.out.println("reportError" + element.getTagName());
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

        @Override
        public String toString() {
            return "ElementEntity{" +
                    "mContext=" + mContext +
                    ", mElement=" + mElement +
                    '}';
        }
    }

//    @Override
//    public void visitAttribute(@NotNull XmlContext context, @NotNull Attr attribute) {
//        super.visitAttribute(context, attribute);
//
//        System.out.println("attr "+attribute.getName()+" "+attribute.getValue());
//    }

    @Override
    public Collection<String> getApplicableElements() {
        return Arrays.asList(SdkConstants.TAG_STYLE);
    }

//    @Nullable
//    @Override
//    public Collection<String> getApplicableAttributes() {
//        ArrayList<String> attrs = new ArrayList<>();
//        attrs.add(SdkConstants.TAG_ITEM);
//        return attrs;
//    }
}