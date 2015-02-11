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

import java.io.*;

/** Represents the possible values for an attribute declaration
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2009/04/14 15:09:38 $ by $Author: yorkjin $
 */
public class DTDDecl implements DTDOutput
{
    public static final DTDDecl FIXED = new DTDDecl(0, "FIXED");
    public static final DTDDecl REQUIRED = new DTDDecl(1, "REQUIRED");
    public static final DTDDecl IMPLIED = new DTDDecl(2, "IMPLIED");
    public static final DTDDecl VALUE = new DTDDecl(3, "VALUE");

    public int type;
    public String name;

    public DTDDecl(int aType, String aName)
    {
            type = aType;
            name = aName;
    }

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDDecl)) return false;

        DTDDecl other = (DTDDecl) ob;
        if (other.type == type) return true;
        return false;
    }

    public void write(PrintWriter out)
        throws IOException
    {
        if (this == FIXED)
        {
            out.print(" #FIXED");
        }
        else if (this == REQUIRED)
        {
            out.print(" #REQUIRED");
        }
        else if (this == IMPLIED)
        {
            out.print(" #IMPLIED");
        }
        // Don't do anything for value since there is no associated DTD keyword
    }
}
