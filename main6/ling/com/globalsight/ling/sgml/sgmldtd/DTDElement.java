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

import java.util.*;
import java.io.*;

/** Represents an element defined with the ELEMENT DTD tag
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2009/04/14 15:09:38 $ by $Author: yorkjin $
 */
public class DTDElement implements DTDOutput
{
/** The name of the element */
    public String name;

/** The element's attributes */
    public Hashtable attributes;

/** The attributes in order the occur in ATTLIST declaration */
    public DTDAttlist attributeList;

/** The element's content */
    public DTDItem content;

/** Is the start tag optional ? */
    public boolean startTagOptional;

/** Is the end tag optional ? */
    public boolean endTagOptional;

/** List of inclusions specified by the exceptions list */
    public Vector inclusions;

    /** List of exclusions specified by the exceptions list */
    public Vector exclusions;

    public DTDElement()
    {
        attributes = new Hashtable();
        startTagOptional = false;
        endTagOptional = false;
    }

    public DTDElement(String aName)
    {
        name = aName;

        attributes = new Hashtable();
        startTagOptional = false;
        endTagOptional = false;
    }

/** Writes out an element declaration and an attlist declaration (if necessary)
    for this element */
    public void write(PrintWriter out)
        throws IOException
    {
        out.print("<!ELEMENT ");
        out.print(name);
        out.print(" ");
        if (content != null)
        {
            content.write(out);
        }
        else
        {
            out.print("ANY");
        }
        out.println(">");
        out.println();
    }

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDElement)) return false;

        DTDElement other = (DTDElement) ob;

        if (name == null)
        {
            if (other.name != null) return false;
        }
        else
        {
            if (!name.equals(other.name)) return false;
        }

        if (attributes == null)
        {
            if (other.attributes != null) return false;
        }
        else
        {
            if (!attributes.equals(other.attributes)) return false;
        }

        if (content == null)
        {
            if (other.content != null) return false;
        }
        else
        {
            if (!content.equals(other.content)) return false;
        }

        return true;
    }

/** Sets the name of this element */
    public void setName(String aName)
    {
        name = aName;
    }

/** Returns the name of this element */
    public String getName()
    {
        return name;
    }

/** Stores an attribute in this element */
    public void setAttribute(String attrName, DTDAttribute attr)
    {
        attributes.put(attrName, attr);
    }

/** Gets an attribute for this element */
    public DTDAttribute getAttribute(String attrName)
    {
        return (DTDAttribute) attributes.get(attrName);
    }

/** Sets the content type of this element */
    public void setContent(DTDItem theContent)
    {
        content = theContent;
    }

/** Returns the content type of this element */
    public DTDItem getContent()
    {
        return content;
    }

    /** Returns true if the start tag is optional */
    public boolean isStartTagOptional()
    {
        return startTagOptional;
    }

    /** Sets the start tag optional flag */
    public void setStartTagOptional(boolean flag)
    {
        startTagOptional = flag;
    }

    /** Returns true if the end tag is optional */
    public boolean isEndTagOptional()
    {
        return endTagOptional;
    }

    /** Sets the start end optional flag */
    public void setEndTagOptional(boolean flag)
    {
        endTagOptional = flag;
    }

    /** Returns the list of exclusions or null if none were declared */
    public Vector getExclusions()
    {
        return exclusions;
    }

    /** Sets the list of exclusions */
    public void setExclusions(Vector excl)
    {
        exclusions = excl;
    }

    /** Returns the list of inclusions or null if none were declared */
    public Vector getInclusions()
    {
        return inclusions;
    }

    /** Sets the list of inclusions */
    public void setInclusions(Vector incl)
    {
        inclusions = incl;
    }
}
