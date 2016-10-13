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

package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview;


public class BranchInfo
{
    private int m_nOp;
    private boolean m_bDefault = false;
    private String m_strVal;
    private String m_strArrow;

    // BranchSpec Varibles   decare later 
    public static final int EQUAL_OP=0;
    public static final int NOT_EQUALS_OP=1;
    public static final int LESS_THAN_OP=2;
    public static final int LESS_THAN_OR_EQUALS_OP=3;
    public static final int GREATER_THAN_OP=4;
    public static final int GREATER_THAN_OR_EQUALS_OP=5;



    /**
       @roseuid 372F8C7D0079
     */
    public BranchInfo()
    {
    }

    /**
       @roseuid 372F8C7D007A
     */
    public int getComparisonOperator()
    {
        return m_nOp;

    }

    /**
       @roseuid 372F8C7D007B
     */
    public String getValue()
    {
        return m_strVal;

    }

    /**
       @roseuid 372F8C7D007C
     */
    public String getArrowLabel()
    {
        return m_strArrow;

    }

    /**
       @roseuid 372F8C7D007D
     */
    public void setComparisonOperator(int p_nOp)
    {
        m_nOp = p_nOp;

    }

    /**
       @roseuid 372F8C7D007F
     */
    public void setValue(String p_strVal)
    {
        m_strVal = p_strVal;

    }

    /**
       @roseuid 372F8C7D0081
     */
    public void setArrowLabel(String p_strArrow)
    {
        m_strArrow = p_strArrow;

    }

    /**
       @roseuid 372F8C7D0083
     */
    public void setDefault(boolean bDef)
    {
        m_bDefault = bDef;
    }

    /**
       @roseuid 372F8C7D0085
     */
    public boolean isDefault()
    {
        return m_bDefault;
    }
}
