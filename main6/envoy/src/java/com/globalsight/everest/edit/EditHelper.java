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

package com.globalsight.everest.edit;

import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;

import java.util.Vector;

/**
 * A helper class for both online and offline.
 * 
 */
public class EditHelper
{
    public static boolean s_isParagraphEditorEnabled = false;

    public static boolean s_isGxmlEditorEnabled = false;

    /**
     * This is the install key that controls access to both the online and
     * offline paragraph editors.
     */
    public static boolean isParagraphEditorInstalled()
    {
        // EDT-2284-2039570341
        String expectedKey = "EDT-" + "GS".hashCode()
                + "paragrapheditor".hashCode();

        s_isParagraphEditorEnabled = SystemConfiguration
                .isKeyValid(SystemConfigParamNames.PARAGRAPH_EDITOR_INSTALL_KEY);

        return s_isParagraphEditorEnabled;
    }

    /**
     * This is the install key that controls access to the GXML editor.
     */
    public static boolean isGxmlEditorInstalled()
    {
        // GXM-2284128829373
        String expectedKey = "GXM-" + "GS".hashCode() + "gxmleditor".hashCode();

        s_isGxmlEditorEnabled = SystemConfiguration
                .isKeyValid(SystemConfigParamNames.GXML_EDITOR_INSTALL_KEY);

        return s_isGxmlEditorEnabled;
    }

    /**
     * Determines if a tuv's state is a protected state.
     */
    public static boolean isTuvInProtectedState(Tuv p_tuv, long p_jobId)
    {
        // Revised: 10-17-01 bb
        // We now protect all exact matches instead of just
        // LeverageGroupExactMatches.

        // return p_tuv.isLeverageGroupExactMatchLocalized();
        return p_tuv.isExactMatchLocalized(p_jobId);
    }

    /**
     * <p>
     * Determines if a subflow element is to be excluded from translation
     * because of its item type.
     * </p>
     * 
     * @param p_element
     *            a GxmlElement that represents a translatable, localizable, or
     *            SUB.
     * @param p_tuType
     *            the item type stored on TU level in case the type was not
     *            specified for the GxmlElement.
     */
    public static boolean isTuvExcluded(GxmlElement p_element, String p_tuType,
            Vector p_excludedItemTypes)
    {
        boolean result = false;
        String type = p_element.getAttribute(GxmlNames.SUB_TYPE);

        if (type == null)
        {
            type = p_tuType;
        }

        if (p_excludedItemTypes != null)
        {
            for (int i = 0; i < p_excludedItemTypes.size(); i++)
            {
                if (((String) p_excludedItemTypes.get(i)).equals(type))
                {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }
}
