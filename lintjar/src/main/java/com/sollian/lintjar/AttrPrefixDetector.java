package com.sollian.lintjar;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * @author admin on 2018/11/15.
 */
public class AttrPrefixDetector extends ResourceXmlDetector {
    public static final Issue ISSUE = Issue.create("AttrNotPrefixed",
            "You must prefix your custom attr by `ct`",
            "We prefix all our attrs to avoid clashes.",
            Category.TYPOGRAPHY,
            5,
            Severity.WARNING,
            new Implementation(AttrPrefixDetector.class,
                    Scope.RESOURCE_FILE_SCOPE));

    // Only values folder
    @Override
    public boolean appliesTo(ResourceFolderType folderType) {
        return ResourceFolderType.VALUES == folderType;
    }

    // Only attr tag
    @Override
    public Collection<String> getApplicableElements() {
        return Collections.singletonList(SdkConstants.TAG_ATTR);
    }

    // Only name attribute
    @Override
    public Collection<String> getApplicableAttributes() {
        return Collections.singletonList(SdkConstants.ATTR_NAME);
    }

    @Override
    public void visitElement(XmlContext context, Element element) {
        final Attr attributeNode = element.getAttributeNode(SdkConstants.ATTR_NAME);
        if (attributeNode != null) {
            final String val = attributeNode.getValue();
            if (!val.startsWith("android:") && !val.startsWith("ct")) {
                context.report(ISSUE,
                        attributeNode,
                        context.getLocation(attributeNode),
                        "You must prefix your custom attr by `ct`");
            }
        }
    }
}