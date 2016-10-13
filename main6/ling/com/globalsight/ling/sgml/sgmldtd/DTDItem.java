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

/** Represents any item in the DTD
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2009/04/14 15:09:38 $ by $Author: yorkjin $
 */
public abstract class DTDItem implements DTDOutput
{
/** Indicates how often the item may occur */
    public DTDCardinal cardinal;

    public DTDItem()
    {
        cardinal = DTDCardinal.NONE;
    }

    public DTDItem(DTDCardinal aCardinal)
    {
        cardinal = aCardinal;
    }

/** Writes out a declaration for this item */
    public abstract void write(PrintWriter out)
        throws IOException;

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDItem)) return false;

        DTDItem other = (DTDItem) ob;

        if (cardinal == null)
        {
            if (other.cardinal != null) return false;
        }
        else
        {
            if (!cardinal.equals(other.cardinal)) return false;
        }

        return true;
    }

/** Sets the cardinality of the item */
    public void setCardinal(DTDCardinal aCardinal)
    {
        cardinal = aCardinal;
    }

/** Retrieves the cardinality of the item */
    public DTDCardinal getCardinal()
    {
        return cardinal;
    }
}
