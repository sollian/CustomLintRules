package com.sollian.lintjar;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DrawableAttrDetector extends ResourceXmlDetector {//extends Detector implements Detector.XmlScanner {

    public static final Issue ISSUE = Issue.create(
            "XMLUsage",
            "版本不兼容或不支持",
            "api21开始才支持自定义Drawable使用attr属性",
            Category.USABILITY,
            5,
            Severity.ERROR,
            new Implementation(DrawableAttrDetector.class,
                    Scope.RESOURCE_FILE_SCOPE));

    private static final List<String> ATTRS = Arrays.asList(
            "android:color",
            "android:drawable"
    );

    @Override
    public boolean appliesTo(@NonNull ResourceFolderType folderType) {
        return folderType == ResourceFolderType.DRAWABLE;
    }


    @Override
    public Collection<String> getApplicableElements() {
        return Arrays.asList(
                "solid"
                , SdkConstants.TAG_ITEM

        );
    }
//
//    // Only name attribute
//    @Override
//    public Collection<String> getApplicableAttributes() {
//        return Arrays.asList(
//                "android:color"
//        );
//    }

    @Override
    public void visitElement(XmlContext context, Element element) {
        NamedNodeMap map = element.getAttributes();
        int len = map.getLength();
        for (int i = 0; i < len; i++) {
            Node node = map.item(i);
            if (node != null && ATTRS.contains(node.getNodeName())) {
                String value = node.getNodeValue();
                if (value != null && value.startsWith("?")) {
                    showTip(context, node);
                    break;
                }
            }
        }
    }

    private void showTip(XmlContext context, Node attributeNode) {
        context.report(ISSUE,
                attributeNode,
                context.getLocation(attributeNode),
                "版本不兼容或不支持");
    }
}