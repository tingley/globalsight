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


//import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowBranchSpec;
import com.globalsight.everest.workflow.WorkflowConditionSpec;

public class CondNodeModel
{
    private int m_nRowCount;
    private boolean m_bNotify;
    private int m_nInvalidDataField;
    //private Node m_cndNode;
    private WorkflowTask m_cndNode;
    private WorkflowConditionSpec m_cndSpec;
    private java.util.List m_bsArr;
    private String m_strAttr;   
    private String m_strType;
    private Vector m_vBranchInfo;
    private String m_strAddr;
    private String m_strDefDir;
    private String m_strProlog;
    private String m_strEpilog;
    // private WorkflowDataItem m_diAttr[];

    /**
       @roseuid 372F8C8D0195
     */
    public CondNodeModel(WorkflowTask p_cndNode)
    {
        m_cndNode = p_cndNode;

        if (m_cndNode == null)
        {

            return;
        }

        m_vBranchInfo = new Vector();
        // get or build branch info
        convertFromPrivateData();
        // get the data items
        //m_diAttr = p_cndNode.getDataItemRefs();

        // get the scripts
        try
        {
            m_strProlog = p_cndNode.getPrologueScript();
            m_strEpilog = p_cndNode.getEpilogueScript();
        }
        catch (Exception mie)
        {
        }
    }

    /**
       @roseuid 372F8C8D01A0
     */
    private void convertFromPrivateData()
    {
        // retrieve condition spec
        try
        {
            m_cndSpec = m_cndNode.getConditionSpec();
        }
        catch (Exception mie)
        {
        }

        // get selected attribute value
        m_strAttr = m_cndSpec.getConditionAttribute();
        // disassemble branch info values
        if ((m_bsArr = m_cndSpec.getBranchSpecs())!=null)
        {

            //Log.println(Log.LEVEL3, "CNModel:convertFrom:getting "+m_bsArr.length+" branches");
            for (int i=0; i<m_bsArr.size(); i++)
            {
                WorkflowBranchSpec bs = (WorkflowBranchSpec)m_bsArr.get(i);
                if (bs.getStructuralState()!=WorkflowConstants.REMOVED)
                {

                    BranchInfo bi = new BranchInfo();

                    // copy branch spec values to our branch info object
                    bi.setComparisonOperator(bs.getComparisonOperator());
                    bi.setValue(bs.getValue());
                    bi.setArrowLabel(bs.getArrowLabel());
                    bi.setDefault(bs.isDefault());

                    // add branch info object to vector
                    m_vBranchInfo.addElement(bi);
                }
            }
        }
    }

    /**
       @roseuid 372F8C8D01A1
     */
    private boolean convertToPrivateData()
    {

        m_nInvalidDataField = -1;
        // default data item type
        //String strType = DataItemRef.TYPE_INTEGER;
        //String strType = WorkflowConstants.INTEGER;
        // if condition spec is null, something is wrong
        if (m_cndSpec == null)
        {

            return false;
        }

        // set selected attribute
        m_cndSpec.setConditionAttribute(m_strAttr);
        /*int nLen = m_diAttr.length;
        // get the selected attribute data type
        for (int nI=0; nI< nLen; nI++)
        {
            if (m_diAttr[nI].getName().equals(m_strAttr))
            {
                strType = m_diAttr[nI].getType();
                break;
            }
        } */

        // get copy for local processing           
        //WorkflowBranchSpec[] bsa = (WorkflowBranchSpec[])m_bsArr.clone();
        // java.util.List bsaList = m_bsArr.clone();
        /* java.util.List bsaList =m_bsArr;
       if (bsaList.size() != m_vBranchInfo.size())
       {

           return false;
       }         */

        java.util.List bsaList = new ArrayList();
        // iterate thru all ordered branch info objects
        for (int i=0; i<m_vBranchInfo.size(); i++)
        {
            // retrieve component values
            BranchInfo bi = (BranchInfo)m_vBranchInfo.elementAt(i);
            String strVal = bi.getValue();
            String strDest = bi.getArrowLabel();
            int nOp = bi.getComparisonOperator();
            boolean bDef = bi.isDefault();
            WorkflowBranchSpec bsa = null;
            // fill eval order array
            try
            {
                // bsa.[i] = m_cndSpec.getBranchSpec(strDest);         
                bsa =m_cndSpec.getBranchSpec(strDest);
            }
            catch (Exception e)
            {   // for testing 
                //bsa = new WorkflowBranchSpec();
            }

            // determine data type and add new set of branch info
            //if (strType == DataItemRef.TYPE_INTEGER)
            if (m_strType == WorkflowConstants.INTEGER)
            {

                // verify integer type
                try
                {
                    Integer n1 = Integer.valueOf(strVal);
                    m_cndSpec.setCondBranchSpecInfo(strDest, nOp, n1.toString(), bDef);
                }
                catch (NumberFormatException nfe)
                {
                    m_nInvalidDataField = i;
                    return false;                    
                }
            } //INTEGER
            else if (m_strType == WorkflowConstants.STRING) // string type accepts anything 
            {
                //Log.println(Log.LEVEL3,"value is STRING");
                // verify valid string (only alphanumeric)
                try
                {
                    m_cndSpec.setCondBranchSpecInfo(strDest, nOp, strVal, bDef);
                }
                catch (Exception mie)
                {
                    m_nInvalidDataField = i;
                    return false;                    
                }
            } //STRING
            else if (m_strType == WorkflowConstants.BOOLEAN)    // verify boolean type
            {
                // if strVal = "true", the boolean converts to true
                // otherwise anything else converts to false            

                Boolean b1 = Boolean.valueOf(strVal);
                m_cndSpec.setCondBranchSpecInfo(strDest, nOp, b1.toString(), bDef);
            } //BOOLEAN
            else if (m_strType == WorkflowConstants.FLOAT)     // verify float type
            {

                try
                {
                    Float f1 = Float.valueOf(strVal);
                    m_cndSpec.setCondBranchSpecInfo(strDest, nOp, f1.toString(), bDef);
                }
                catch (NumberFormatException nfe)
                {
                    m_nInvalidDataField = i;
                    return false;                    
                }
            }  //FLOAT 
            else if (m_strType == WorkflowConstants.LONG)     // verify long type
            {

                try
                {
                    Long l1 = Long.valueOf(strVal);
                    m_cndSpec.setCondBranchSpecInfo(strDest, nOp, l1.toString(), bDef);
                }
                catch (NumberFormatException nfe)
                {
                    m_nInvalidDataField = i;
                    return false;                    
                }
            } //LONG               

            // assign branch spec fields (used for ordering)            
            /*bsa[i].setValue(strVal);                
            bsa[i].setArrowLabel(strDest);                
            bsa[i].setComparisonOperator(nOp);                
            bsa[i].setDefault(bDef);  */
            bsa.setValue(strVal);                
            bsa.setArrowLabel(strDest);                
            bsa.setComparisonOperator(nOp);                
            bsa.setDefault(bDef);  
            bsaList.add(bsa);

        } // for loop

        // copy local array and set order
        //m_bsArr = bsa;
        //m_cndSpec.setEvalOrder(m_bsArr);
        m_cndSpec.setEvalOrder(bsaList);

        return true;
    }

    /**
       @roseuid 372F8C8D01A2
     */
    /*  public String getAttribute()
      {
          // check to see if selection cond attr is still part of data item set
          boolean bFound = false;
          if( m_strAttr==null)
          //if (m_strAttr.length() == 0)
              return m_strAttr;
  
          int nLen = m_diAttr.length;
          for (int i=0; i<nLen; i++)
          {
              if (m_diAttr[i].getName().equals(m_strAttr))
              {
                  bFound = true;
                  break;
              }
          }
          if (bFound == false)
          {
  
              return null;
          }
  
          return m_strAttr;
  
      }  */

    /**
       @roseuid 372F8C8D01A3
     */
    public Vector getBranchInfo()
    {
        return m_vBranchInfo;

    }

    /**
       @roseuid 372F8C8D01A4
     */
    public String getEmailAddr()
    {
        return m_strAddr;

    }

    /**
       @roseuid 372F8C8D01A5
     */
    public boolean getNotification()
    {
        return m_bNotify;

    }

    /**
       @roseuid 372F8C8D01A6
     */
    public String getDefDir()
    {
        return m_strDefDir;

    }

    /**
       @roseuid 372F8C8D01A7
     */
    public String getProlog()
    {
        return m_strProlog;

    }

    /**
       @roseuid 372F8C8D01A8
     */
    public String getEpilog()
    {
        return m_strEpilog;

    }

    /**
       @roseuid 372F8C8D01A9
     */
    /* public String[] getDataSet()
     {
 
         String[] strData = null;
        // int nLen = m_diAttr.length;
 
         if( m_diAttr ==null)
             return new String[0];
 
         //if (nLen < 1)
          //   return new String[0];
         int nLen = m_diAttr.length;
         if (nLen < 1)
           return new String[0];
 
         strData = new String[nLen];
         // fill data items
         for (int i=0; i< nLen; i++)
         {
             strData[i] = m_diAttr[i].getName();
         }
 
         return strData;
 
     }  */


    /**
       @roseuid 372F8C8D01AA
     */
    public void setAttribute(String p_strAttr)
    {
        m_strAttr = p_strAttr;

    }
    public void setAttributeType(String p_strType)
    {
        m_strType = p_strType;

    }


    /**
       @roseuid 372F8C8D01AC
     */
    public void setBranchInfo(Vector p_vBranchInfo)
    {
        m_vBranchInfo = p_vBranchInfo;

    }

    /**
       @roseuid 372F8C8D01AE
     */
    public void setEmailAddr(String p_strAddr)
    {
        m_strAddr = p_strAddr;

    }

    /**
       @roseuid 372F8C8D01B0
     */
    public void setNotification(boolean p_bNotify)
    {
        m_bNotify = p_bNotify;

    }

    /**
       @roseuid 372F8C8D01B2
     */
    public void setDefDir(String p_strDefDir)
    {
        m_strDefDir = p_strDefDir;

    }

    /**
       @roseuid 372F8C8D01B4
     */
    public void setProlog(String p_strProlog)
    {
        m_strProlog = p_strProlog;

    }

    /**
       @roseuid 372F8C8D01B6
     */
    public void setEpilog(String p_strEpilog)
    {
        m_strEpilog = p_strEpilog;

    }

    /**
       @roseuid 372F8C8D01B8
     */
    public int saveModelData()
    {
        boolean bRetVal = convertToPrivateData();
        // check if building branch specs failed
        if (bRetVal == false)
        {
            return m_nInvalidDataField;
        }
        try
        {
            // set condition spec
            m_cndNode.setConditionSpec(m_cndSpec);            
            // set scripts
            m_cndNode.setPrologueScript(m_strProlog);
            m_cndNode.setEpilogueScript(m_strEpilog);
        }
        catch (Exception e)
        {

        }
        return -1;
    }
}
