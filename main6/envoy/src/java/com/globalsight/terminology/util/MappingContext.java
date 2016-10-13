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

package com.globalsight.terminology.util;

import com.globalsight.terminology.Definition;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class mirrors the XmlToHtml conversions in the Termbase
 * Viewer's Javascript code (terminology/viewer/entry.js) to format an
 * entry or entry fragment as HTML. The MappingContext provides display
 * strings based on the database definition to the UI.
 */
public class MappingContext
{
    private Definition m_definition;

    public MappingContext(String p_definition)
    {
        try
        {
            m_definition = new Definition(p_definition);
        }
        catch (TermbaseException unexpected)
        {
            // UI should pass in an XML string derived from the
            // original termbase definition, so this exception
            // should never happen.
        }
    }

    public String mapEntry ()
    {
        return "Entry";
    }

    public String mapNewEntry ()
    {
        return "New Entry";
    }

    public String mapLanguage (String p_language)
    {
        return p_language;
    }

    public String mapTransac (String p_type)
    {
        if (p_type.equals("origination"))
        {
            return "Creation Date";
        }
        else if (p_type.equals("modification"))
        {
            return "Modification Date";
        }
        else
        {
            return p_type;
        }
    }

    public String mapNote (String p_type)
    {
        return "Note";
    }

    public String mapSource (String p_type)
    {
        return "Source";
    }

    public String mapTerm (Boolean p_isFirst)
    {
        if (p_isFirst == null)
        {
            return "Term";
        }
        else if (p_isFirst == Boolean.TRUE)
        {
            return "Main Term";
        }
        else
        {
            return "Synonym";
        }
    }

    public String mapDescrip (String p_type)
    {
        // Check database definition if a field of this type has been
        // defined.
        ArrayList fields = m_definition.getFields();
        for (int i = 0, max = fields.size(); i < max; i++)
        {
            Definition.Field field = (Definition.Field)fields.get(i);

            if (field.getType().equals(p_type))
            {
                return field.getName();
            }
        }

        // Otherwise produce title case.
        if (p_type.length() > 1)
        {
            return Character.toUpperCase(p_type.charAt(0)) +
                p_type.substring(1);
        }

        return p_type.toUpperCase();
    }
}
