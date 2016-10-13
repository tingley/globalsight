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

package com.globalsight.ling.sgml.sgmldtd;

import com.globalsight.ling.sgml.GlobalSightDtd;

import java.util.ArrayList;
import java.util.Enumeration;

import com.globalsight.ling.sgml.sgmldtd.DTD;
import com.globalsight.ling.sgml.sgmldtd.DTDAttribute;
import com.globalsight.ling.sgml.sgmldtd.DTDElement;
import com.globalsight.ling.sgml.sgmldtd.DTDEntity;
import com.globalsight.ling.sgml.sgmldtd.DTDItem;

public class DtdAdapter implements GlobalSightDtd
{
    private DTD dtd;

    public DtdAdapter(DTD dtd)
    {
        this.dtd = dtd;
    }

    /** Returns a list of elements (as internal DTDElement objects). */
    public ArrayList getElements()
    {
        ArrayList result = new ArrayList();

        for (Enumeration e = dtd.items.elements(); e.hasMoreElements(); )
        {
            Object item = e.nextElement();

            if (item instanceof DTDElement)
            {
                result.add(item);
            }
        }

        return result;
    }

    /** Returns a list of element names (as string). */
    public ArrayList getElementNames()
    {
        ArrayList result = new ArrayList();

        for (Enumeration e = dtd.items.elements(); e.hasMoreElements(); )
        {
            Object item = e.nextElement();

            if (item instanceof DTDElement)
            {
                DTDElement element = (DTDElement) item;
                result.add(element.name);
            }
        }

        return result;
    }

    /**
     * Returns a list of attributes for the specified element (as
     * internal DTDAttribute objects).
     */
    public ArrayList getAttributes(String p_element)
    {
        ArrayList result = new ArrayList();

        DTDElement elem = (DTDElement) dtd.elements.get(p_element);

        if (elem != null && elem.attributeList != null)
        {
            for (Enumeration e = elem.attributeList.attributes.elements();
                 e.hasMoreElements();)
            {
                result.add(e.nextElement());
            }
        }

        return result;
    }

    /** Returns a list of GlobalSightAttribute. */
    public ArrayList getAttributeNames(String p_element)
    {
        ArrayList result = new ArrayList();

        DTDElement elem = (DTDElement) dtd.elements.get(p_element);

        if (elem != null && elem.attributeList != null)
        {
            for (Enumeration e = elem.attributeList.attributes.elements();
                 e.hasMoreElements();)
            {
                DTDAttribute attr = (DTDAttribute) e.nextElement();

                result.add(attr.name);
            }
        }

        return result;
    }

    /** Returns a list of GlobalSightEntity objects. */
    public ArrayList getEntities()
    {
        ArrayList result = new ArrayList();
        for (Enumeration e = dtd.items.elements(); e.hasMoreElements(); )
        {
            Object item = e.nextElement();

            if (item instanceof DTDEntity)
            {
                DTDEntity entity = (DTDEntity) item;

                // Filter out the entities that aren't either CDATA,
                // SDATA or NDATA.  To get all entities, take out the
                // if statement and keep only the list.add(...);
                if ((entity.type == DTDEntity.CDATA) ||
                    (entity.type == DTDEntity.SDATA) ||
                    (entity.type == DTDEntity.NDATA))
                {
                    result.add(new EntityAdapter(entity.name, entity.value));
                }
            }
        }

        return result;
    }
}
