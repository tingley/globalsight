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

package com.globalsight.ling.rtf;

import java.util.Vector;
import java.io.PrintWriter;

public class RtfVariables
{
    Vector _variables;

    RtfVariables()
    {
        _variables = new Vector();
    }

    public int count()
    {
        return _variables.size();
    }

    public void addVariable(RtfVariable var)
    {
        _variables.addElement(var);
    }

    public RtfVariable getVariable(int idx)
    {
        return (RtfVariable)_variables.elementAt(idx);
    }

    public void Dump(PrintWriter out)
    {
        out.println("<variables>");
        for (int ii = 0; ii < _variables.size(); ii++) 
		{
            RtfVariable var = getVariable(ii);
            out.println("<variable name=\"" + var.getName() + 
				"\" value=\"" + var.getValue() + "\"/>");
        }
        out.println("</variables>");
    }
}
