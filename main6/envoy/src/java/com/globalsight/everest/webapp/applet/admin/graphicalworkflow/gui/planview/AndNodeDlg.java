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


//JDK
import javax.swing.JFrame;
import javax.swing.JPanel;
//GlobalSight
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api.WFApp;
import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;




public class AndNodeDlg extends NodesDlg
{

    public static final int PROPERTY_TAB = 0;
    public static final int NOTIFICATION_TAB = 1;
    private MessageCatalog m_msgCat;

    public AndNodeDlg(JFrame p_Frame, WorkflowTaskInstance p_plNode, int p_nTab, WFApp p_wfApp)
    { 
        super(p_Frame, p_plNode, p_nTab, p_wfApp);
        m_msgCat = new MessageCatalog (GraphicalPane.localeName);
        NodeModel m_nm = new NodeModel( NodeModel.AND_NODE, p_plNode );
        setModel(m_nm);
        setTitle(AppletHelper.getI18nContent("lb_and_node_properties"));

    }

    public AndNodeDlg(JFrame p_Frame, WorkflowTask p_plNode, int p_nTab, WFApp p_wfApp)
    { 
        super(p_Frame, p_plNode, p_nTab, p_wfApp);
        m_msgCat = new MessageCatalog (GraphicalPane.localeName);
        NodeModel m_nm = new NodeModel( NodeModel.AND_NODE, p_plNode );
        setModel(m_nm);
        setTitle(AppletHelper.getI18nContent("lb_and_node_properties"));

    }

    public AndNodeDlg(JFrame p_Frame, WorkflowTaskInstance p_plNode, WFApp p_wfApp)
    { 
        this( p_Frame, p_plNode, 0, p_wfApp );

    }

    public AndNodeDlg(JFrame p_Frame, WorkflowTask p_plNode, WFApp p_wfApp)
    { 
        this( p_Frame, p_plNode, 0, p_wfApp );

    }

    protected JPanel buildPrefPanel2()
    {
        return(JPanel)super.buildPrefPanel2();
    }
}
