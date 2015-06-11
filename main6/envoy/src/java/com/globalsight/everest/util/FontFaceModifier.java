/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.TuType;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.TextNode;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * This class modifies font face name so CJK text is displayed correctly in
 * Netscape 4.x
 */
public class FontFaceModifier
{
    private static final Logger CATEGORY = Logger
            .getLogger(FontFaceModifier.class.getName());

    static private REProgram font_names;
    static
    {
        try
        {
            RECompiler dragon = new RECompiler();
            font_names = dragon.compile(",\\s*");
        }
        catch (RESyntaxException e)
        {
            CATEGORY.error("FontFaceModifier: pilot error in regex", e);
        }
    }

    static private String DOUBLE_QUOTE = "&quot;";
    static private String SINGLE_QUOTE = "&apos;";

    /**
     * This method look for a subflow or the segment itself whose type is
     * css-font-family, which is a comma delimited font face name list. "WAS" is
     * added to each font name and storeed back into Tuv object
     */
    public static void addWasToFontFace(Tuv p_tuv, long p_jobId)
    {
        // check if Tuv type is css-font-family
        if (p_tuv.getTu(p_jobId).getTuType()
                .equals(TuType.CSS_FONT_FAMILY.getName()))
        {
            String newFonts = modifyFonts(p_tuv.getGxmlExcludeTopTags());
            p_tuv.setGxmlExcludeTopTags(newFonts, p_jobId);
        }
        else
        {
            Map modifiedSubs = new HashMap();
            // get all the subflows of the TUV
            List subs = p_tuv.getSubflowsAsGxmlElements();
            Iterator it = subs.iterator();
            while (it.hasNext())
            {
                GxmlElement sub = (GxmlElement) it.next();
                String type = sub.getAttribute(GxmlNames.SUB_TYPE);
                if (type == null)
                    continue;
                // check if the type of subflow is css-font-family
                if ((type != null)
                        && type.equals(TuType.CSS_FONT_FAMILY.getName()))
                {
                    List children = sub.getChildElements(GxmlElement.TEXT_NODE);
                    // we assume there is only one text node under sub element
                    TextNode textNode = (TextNode) children.get(0);
                    // add WAS to font faces
                    String newFonts = modifyFonts(textNode.getTextValue());

                    // save it in a Map for storing it in TUV later
                    modifiedSubs.put(sub.getAttribute(GxmlNames.SUB_ID),
                            newFonts);
                }
            }
            // set the subs in TUV
            p_tuv.setSubflowsGxml(modifiedSubs);
        }

    }

    // font face string is a comma delimited face name list. It may be
    // enclosed by quotations
    private static String modifyFonts(String p_fontFace)
    {
        RE re = new RE(font_names);
        String[] faces = re.split(p_fontFace);

        String quote = null;
        if (faces[0].startsWith(DOUBLE_QUOTE))
        {
            quote = DOUBLE_QUOTE;
        }
        else if (faces[0].startsWith(SINGLE_QUOTE))
        {
            quote = SINGLE_QUOTE;
        }

        if (quote != null)
        {
            // delete the first quote
            faces[0] = faces[0].substring(quote.length());
            // delete the last quote
            String lastString = faces[faces.length - 1];
            faces[faces.length - 1] = lastString.substring(0,
                    lastString.length() - quote.length());
        }

        StringBuffer modified = new StringBuffer();
        // add the opening quote
        if (quote != null)
        {
            modified.append(quote);
        }

        // add WAS
        for (int i = 0; i < faces.length; i++)
        {
            modified.append("WAS");
            modified.append(faces[i]);
            if (i + 1 < faces.length)
            {
                modified.append(", ");
            }
        }

        // add the closing quote
        if (quote != null)
        {
            modified.append(quote);
        }
        return modified.toString();
    }

}
