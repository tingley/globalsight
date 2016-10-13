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
package com.globalsight.ling.docproc;

import com.globalsight.ling.common.XmlWriter;
import com.globalsight.ling.common.DiplomatNames;
import java.util.Iterator;
import java.util.Properties;

/**
 * DiplomatWriter
 *
 *
 * Created: Fri Aug  4 16:19:01 2000
 *
 * @author Shigemichi Yazawa
 * @version 1.0
 */
public class DiplomatWriter
{
    /**
     * Converts a Output object to a GXML string.
     * @param diplomatAttribute Attributes of &lt;diplomat&gt; element
     * @param output Output object containing extracted result
     * @return GXML string
     */
    static public String WriteXML(Output output)
    {
        return writeXML(output, true);
    }


    static public String writeXML(Output output, boolean withXmlDecl)
    {
        XmlWriter writer = new XmlWriter();

        // XML declaration without encoding attribute.
        if (withXmlDecl)
        {
            writer.xmlDeclaration(null);
        }


        // <diplomat> document element
        writer.startElement(DiplomatNames.Element.DIPLOMAT,
            output.getDiplomatAttribute().CreateProperties());

        // Write out each elements
        Iterator it = output.documentElementIterator();
        while (it.hasNext())
        {
            DocumentElement element = (DocumentElement)it.next();
            element.toDiplomatString(output.getDiplomatAttribute(), writer);
        }

        // </diplomat>
        writer.endElement();
        return writer.getXml();
    }
}
