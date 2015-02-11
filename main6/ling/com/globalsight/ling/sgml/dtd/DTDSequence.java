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
package com.globalsight.ling.sgml.dtd;

import java.io.*;
import java.util.*;

/** Represents a sequence in an element's content.
 * A sequence is declared in the DTD as (value1,value2,value3,etc.)
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2009/04/14 15:09:38 $ by $Author: yorkjin $
 */
public class DTDSequence
    extends DTDContainer
{
    public DTDSequence()
    {
    }

    /** Writes out a declaration for this sequence */
    public void write(PrintWriter out)
        throws IOException
    {
        out.print("(");

        Enumeration e = getItemsVec().elements();
        boolean isFirst = true;

        while (e.hasMoreElements())
        {
            if (!isFirst) out.print(",");
            isFirst = false;

            DTDItem item = (DTDItem) e.nextElement();
            item.write(out);
        }
        out.print(")");
        cardinal.write(out);
    }

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDSequence)) return false;

        return super.equals(ob);
    }
}
