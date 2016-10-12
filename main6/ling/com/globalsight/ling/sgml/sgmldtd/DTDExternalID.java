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

/** Represents an external ID in an entity declaration
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2009/04/14 15:09:38 $ by $Author: yorkjin $
 */
public abstract class DTDExternalID implements DTDOutput
{
    public String system;

    public DTDExternalID()
    {
    }

/** Writes out a declaration for this external ID */
    public abstract void write(PrintWriter out)
        throws IOException;

    public boolean equals(Object ob)
    {
        if (ob == this) return true;
        if (!(ob instanceof DTDExternalID)) return false;

        DTDExternalID other = (DTDExternalID) ob;

        if (system == null)
        {
            if (other.system != null) return false;
        }
        else
        {
            if (!system.equals(other.system)) return false;
        }

        return true;
    }

/** Sets the system ID */
    public void setSystem(String aSystem)
    {
        system = aSystem;
    }

/** Retrieves the system ID */
    public String getSystem()
    {
        return system;
    }
}
