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
package com.globalsight.util.gxml;

//java
import java.util.List;
import java.util.ArrayList;

/**
 * Root Element of a Paginated Result Set, representing the
 * &lt;paginatedResultSetXml&gt; tag element.
 *
 */
public class PrsRootElement
    extends GxmlRootElement
{
    PrsRootElement()
    {
        super(GxmlElement.PRS_ROOT, GxmlNames.PRS_ROOT);
    }

    /**
     * <P>Get the Translatable and Localizable elements from this
     * document structure.  Get them in groups by RECORD element. The
     * result is a List of Lists and it keeps the same order as the
     * elements appear in the document.</P>
     *
     * @return A List of lists of elements of translatable or
     * localizable. Returns null if the result list is empty.  The
     * outer list represents each record in the Prs, while each
     * internal list represents the translatable and localizable
     * elements within each record element.
     */
    public List getTranslatableAndLocalizableGroupedByRecord()
    {
        List result = new ArrayList();
        List records = getChildElements();

        for (int i = 0; i < records.size(); i++)
        {
            List subResult = new ArrayList();

            GxmlElement aRecord = (GxmlElement)records.get(i);
            List columns = aRecord.getChildElements(GxmlElement.COLUMN);

            for (int j = 0; j < columns.size(); j++)
            {
                GxmlElement aColumn = (GxmlElement)columns.get(j);
                String contentMode =
                    aColumn.getAttribute(GxmlNames.COLUMN_CONTENTMODE);

                if (! contentMode.equals(GxmlNames.TRANSLATABLE))
                {
                    // ignore the non-translatable column
                    continue;
                }

                // based on the assumption that there is only one
                // content for each column
                GxmlElement content = (GxmlElement)aColumn.getChildElements(
                    GxmlElement.CONTENT).get(0);

                // based on the assumption that there is only one
                // gxmlRoot for each content
                GxmlElement gxmlRoot = content.getChildElement(0);
                List elmts = gxmlRoot.getChildElements();

                for (int k = 0; k < elmts.size(); k++)
                {
                    GxmlElement aElmt = (GxmlElement)elmts.get(k);

                    if (aElmt.getType() == GxmlElement.TRANSLATABLE
                        || aElmt.getType() == GxmlElement.LOCALIZABLE)
                    {
                        subResult.add(aElmt);
                    }
                }
            }

            result.add(subResult);
        }

        if (result.size() == 0)
        {
            return null;
        }

        return result;
    }
}
