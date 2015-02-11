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


// JDK
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowListener;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
//GlobalSight
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api.WFApp;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.dmsview.DirectoryDialog;
import com.globalsight.everest.webapp.applet.common.GenericJTextField;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;
import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;


public class NodesDlg extends JDialog implements WindowListener, ActionListener, ItemListener
{

    private JFrame m_Frame;
    private int m_nTab = 0;
    private JButton m_btnOk;
    private JButton m_btnColor;
    private JButton m_btnCancel;
    private JButton m_btnBrowse;
    private JPanel m_pnlColor;
    private Color m_curClr;
    private JCheckBox m_ckbNotify;
    private JLabel m_lblAddress;
    private JTextArea m_txtDir;
    private JTextField m_txtDesc;
    private GenericJTextField m_txtName;
    private JTextField m_txtAddress;
    private NodeModel m_nodeMdl;
    private MessageCatalog m_msgCat;

    private WorkflowTask m_plNode;


    private WorkflowInstance m_procInst;

    private boolean m_bEditMode = false;

    public NodesDlg(JFrame p_Frame, WorkflowTask p_plNode, int p_nTab, WFApp p_wfApp )
    {
        super(p_Frame, true);
        m_Frame = p_Frame;
        m_procInst = null;
        m_nTab = p_nTab;
        m_msgCat = new MessageCatalog (GraphicalPane.localeName);
        
        constructUI();
        m_bEditMode = true;
        /* Plan pl = p_plNode.getPlan();
         if (pl != null)
         {
             if (!pl.isInEditMode()) // not in edit mode
             {
                 disableControls();
             }
             else // edit mode on
             {
                 m_bEditMode = true;
             }
         }  */
        // uncomment later
        /*if (m_docRep == null)
        {
            m_btnBrowse.setEnabled(false);
        }*/
    }

    public NodesDlg(JFrame p_Frame, WorkflowTaskInstance p_NodeInst, int p_nTab, WFApp p_wfApp )
    { //shailaja. Changed Frame to JFrame
        super(p_Frame, true);
        m_Frame = p_Frame;
        /*try
        {
            m_procInst = p_NodeInst.getProcessInstance();
        }
        catch(Exception me)
        {
            //Log.println(Log.LEVEL1,"NodesDlg:Error retrieving procInst.");
        } */
        m_nTab = p_nTab;
        m_msgCat = new MessageCatalog (GraphicalPane.localeName);

        constructUI();



        /* if (m_procInst != null)
         {
             if (!m_procInst.isInStructuralEditMode())
             {
                 disableControls();
             }
             else
             {
                 m_bEditMode = true;
             }
         }
         else
         {
             Plan pl = p_NodeInst.getPlan();
             if (pl != null)
             {
                 if (!pl.isInEditMode()) // not in edit mode
                 {
                     disableControls();
                 }
                 else // edit mode on
                 {
                   m_bEditMode = true;
                 }
             }
         } */
        m_bEditMode = true;

        /*if (m_docRep == null)
        {
            m_btnBrowse.setEnabled(false);
        } */
        //uncomment later and comments this
        m_btnBrowse.setEnabled(false);

    }

    private void constructUI()
    {
        JPanel container = new JPanel();
        container.setLayout( new BorderLayout() );

        JTabbedPane tabs = new JTabbedPane();
        JPanel pnlPref1 = buildPrefPanel1();
        JPanel pnlPref2 = buildPrefPanel2();
        JPanel pnlPref3 = buildPrefPanel3();
        tabs.addTab(AppletHelper.getI18nContent("lb_general"), null, pnlPref1 );
        tabs.addTab(AppletHelper.getI18nContent("lb_scripting"), null, pnlPref2 );
        // Notification tab removed until implementation by model
        //tabs.addTab( m_msgCat.getMsg("Notification"), null, pnlPref3 );
        tabs.setSelectedIndex(m_nTab);
        JPanel pnlButton = buildButtonPanel();
        // build button panel
        getRootPane().setDefaultButton(m_btnOk);

        // add subpanels to main panel
        container.add(tabs, BorderLayout.CENTER);
        container.add(pnlButton, BorderLayout.SOUTH);
        getContentPane().add(container);
        centerDialog();
        setSize(getInsets().left + getInsets().right + 500,getInsets().top + getInsets().bottom + 300);
        setResizable(false);
        addWindowListener(this);
        // setBackground to white 
        /*pnlPref1.setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        pnlPref2.setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        pnlPref3.setBackground(EnvoyAppletConstants.ENVOY_WHITE);

        pnlButton.setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        tabs.setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        container.setBackground(EnvoyAppletConstants.ENVOY_WHITE);*/
    }

    public boolean saveChanges()
    {
        // model valid?
        if (m_nodeMdl == null)
        {
            return false;
        }
        // get current values
        m_nodeMdl.setScript(m_txtDir.getText());
        m_nodeMdl.setNotification(m_ckbNotify.isSelected());
        m_nodeMdl.setAddress(m_txtAddress.getText());
        m_nodeMdl.setPropName(m_txtName.getText());        
        m_nodeMdl.setPropDesc(m_txtDesc.getText());
        // save the new values
        try
        {
            m_nodeMdl.saveChanges();
        }
        catch (Exception mie)
        {
            return true;                
        }
        return false;

    }

    public void windowClosing(WindowEvent e)
    {

        dispose();

    }
    public void windowOpened(WindowEvent e)
    {
    }
    public void windowClosed(WindowEvent e)
    {
    }
    public void windowIconified(WindowEvent e)
    {
    }
    public void windowDeiconified(WindowEvent e)
    {
    }
    public void windowActivated(WindowEvent e)
    {
    }
    public void windowDeactivated(WindowEvent e)
    {
    }

    protected void centerDialog()
    {
        Dimension screenSize = this.getToolkit().getScreenSize();
        Dimension size = this.getSize();
        screenSize.height = screenSize.height/2;
        screenSize.width = screenSize.width/2;
        size.height = size.height/2;
        size.width = size.width/2;
        int y = screenSize.height - size.height;
        int x = screenSize.width - size.width;
        this.setLocation(x,y);

    }


    public void initPanelData()
    {
        // use m_nodeMdl to get initial values
        if (m_nodeMdl == null)
        {
            return;
        }
        String strScript = m_nodeMdl.getScript();
        boolean bNotification = m_nodeMdl.getNotification();
        String strAddress = m_nodeMdl.getAddress();
        String strName = m_nodeMdl.getPropName();
        String strDesc = m_nodeMdl.getPropDesc();
        // set component values
        m_txtDir.setText(strScript);
        m_txtName.setText(strName);
        m_txtDesc.setText(strDesc);
        m_ckbNotify.setSelected(bNotification);
        m_txtAddress.setText(strAddress);

    }

    /**
       @roseuid 372F8C5D0132
     */
    public void btnOkAction(ActionEvent ae)
    {
        if (m_bEditMode == false || saveChanges()==false) // no errors
        {
            setVisible(false);
            dispose();
        }
    }

    /**
       @roseuid 372F8C5D0134
     */
    private JPanel buildPrefPanel1()
    {
        JPanel pnlPref1 = new JPanel();
        pnlPref1.setLayout( null);
        int nInsetsLeft = pnlPref1.getInsets().left;
        int nInsetsTop = pnlPref1.getInsets().top;
        // get label from page handler
        JLabel lblName = new JLabel(AppletHelper.getI18nContent("lb_name_c"));
        lblName.setBounds( nInsetsLeft + 20, nInsetsTop + 30,100,25);
        m_txtName = new GenericJTextField(30); // max of 30 chars 
        m_txtName.setBounds( nInsetsLeft + 20, nInsetsTop + 60,250,25);
        JLabel lblDesc = new JLabel(AppletHelper.getI18nContent("lb_description_c"));
        lblDesc.setBounds( nInsetsLeft + 20, nInsetsTop + 110,100,25);
        m_txtDesc = new JTextField();
        m_txtDesc.setBounds( nInsetsLeft + 20, nInsetsTop + 140,250,25);

        pnlPref1.add(lblName);
        pnlPref1.add(lblDesc);
        pnlPref1.add(m_txtName);
        pnlPref1.add(m_txtDesc);

        return pnlPref1;

    }

    protected JPanel buildPrefPanel2()
    {
        JPanel pnlPref2 = new JPanel();
        pnlPref2.setLayout( null );
        // get lale from pagehandler
        JLabel lblDir = new JLabel(AppletHelper.getI18nContent("lb_epilogue_c"));
        lblDir.setBounds(pnlPref2.getInsets().left + 20,pnlPref2.getInsets().top + 50,200,25);

        JScrollPane spScrpt = new JScrollPane();
        m_txtDir = new JTextArea();
        m_txtDir.setLineWrap(true);
        spScrpt.getViewport().setView(m_txtDir);
        spScrpt.setBounds(pnlPref2.getInsets().left + 20,pnlPref2.getInsets().top + 80,300,100);

        m_btnBrowse = new JButton(AppletHelper.getI18nContent("lb_browse_d"));
        if (WindowUtil.isUsingSolaris())
            m_btnBrowse.setBounds(pnlPref2.getInsets().left + 325,pnlPref2.getInsets().top + 80,120,40);
        //m_btnBrowse.setBounds(325,80,120,40); //Rajiv
        else
            m_btnBrowse.setBounds(pnlPref2.getInsets().left + 325,pnlPref2.getInsets().top + 80,120,25);
        m_btnBrowse.addActionListener(this);
        m_btnBrowse.setToolTipText(AppletHelper.getI18nContent("lb_enter_script_name"));

        pnlPref2.add(lblDir);
        pnlPref2.add(spScrpt);
        //uncomment later 
        //pnlPref2.add(m_btnBrowse);

        return pnlPref2;

    }

    /**
       @roseuid 372F8C5D0136
     */
    private JPanel buildPrefPanel3()
    {
        JPanel pnlPref3 = new JPanel();
        pnlPref3.setLayout( null);
        int nInsetsLeft = pnlPref3.getInsets().left;
        int nInsetsTop = pnlPref3.getInsets().top;
        // get label from page handler
        m_ckbNotify = new JCheckBox(AppletHelper.getI18nContent("lb_notification"));
        m_ckbNotify.setBounds(nInsetsLeft + 20,nInsetsTop + 50,250,25);
        m_ckbNotify.addItemListener(this);
        m_ckbNotify.addActionListener(this);
        m_ckbNotify.setToolTipText(AppletHelper.getI18nContent("lb_toggles_notification"));

        m_lblAddress = new JLabel(AppletHelper.getI18nContent("lb_email_address_c"));
        m_lblAddress.setBounds(nInsetsLeft + 20,nInsetsTop + 120,200,25);

        m_txtAddress = new JTextField();
        m_txtAddress.setBounds(nInsetsLeft + 20,nInsetsTop + 150,350,25);

        pnlPref3.add(m_ckbNotify);
        pnlPref3.add(m_lblAddress);
        pnlPref3.add(m_txtAddress);

        return pnlPref3;

    }

    /**
       @roseuid 372F8C5D0137
     */
    public void itemStateChanged(ItemEvent ie)
    {
        Object object = ie.getSource();
        if (object == m_ckbNotify)
            cbNotifyItemState(ie);

    }

    /**
       @roseuid 372F8C5D0139
     */
    public void btnColorAction(ActionEvent ae)
    {
        /*
                Color clr = JColorChooser.showDialog(this, m_msgCat.getMsg("Colors"), m_curClr);
                // return if 'cancel' button pressed
                if (clr == null) return;
                m_pnlColor.setBackground(clr);
                m_curClr = clr;
                m_pnlColor.repaint();
        */
    }

    /**
       @roseuid 372F8C5D013B
     */
    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout ( new FlowLayout(FlowLayout.RIGHT) );
        m_btnCancel = new JButton(AppletHelper.getI18nContent("lb_cancel"));
        m_btnCancel.addActionListener(this);
        buttonPanel.add( m_btnCancel );
        m_btnOk = new JButton(AppletHelper.getI18nContent("lb_ok"));
        m_btnOk.addActionListener(this);
        buttonPanel.add( m_btnOk );
        return buttonPanel;

    }

    /**
       @roseuid 372F8C5D013C
     */
    public void btnCancelAction(ActionEvent ae)
    {
        setVisible(false);
        dispose();

    }

    /**
       @roseuid 372F8C5D013E
     */
    public void cbNotifyItemState(ItemEvent ie)
    {
        switch (ie.getStateChange())
        {
            case ItemEvent.SELECTED:
                // enable address txt field
                m_txtAddress.setEnabled(true);
                m_txtAddress.repaint();
                m_lblAddress.setEnabled(true);
                m_lblAddress.repaint();
                break;
            case ItemEvent.DESELECTED:
                // gray out the address text field
                m_txtAddress.setEnabled(false);
                m_txtAddress.repaint();
                m_lblAddress.setEnabled(false);
                m_lblAddress.repaint();
                break;
        }

    }

    /**
       @roseuid 372F8C5D0140
     */
    // used for getting script file information
    public void btnBrowseAction(ActionEvent ae)
    {
        // bring up DMS browser (true => modal)
        // uncomment later   
        // DirectoryDialog dd = new DirectoryDialog(m_Frame, true, m_docRep);
        DirectoryDialog dd = new DirectoryDialog(m_Frame, true);
        WindowUtil.center(dd);
        // get label from page handler
        dd.setTitle(AppletHelper.getI18nContent("lb_select_script_d"));
        dd.hideTitleField();
        dd.show();
        if (dd.isCancelled())
        {
            return;
        }

        // retrieve chosen file
        /* String strPath = dd.getSelectedPath();
         File fileLocal = null;
 
         try 
         {
             fileLocal = m_docRep.checkOut(strPath, false);
         } 
         catch (DMSException dmse) 
         {
             Log.printStack(Log.LEVEL0, dmse);
             return;
         }  //Parag
         catch (Exception e)
         {
             WindowUtil.showMsgDlg(m_Frame,
             m_msgCat.getMsg(e.getErrorCode()),
             m_msgCat.getMsg("Internal Error!"), WindowUtil.TYPE_ERROR);
             return;
         }
         
         StringBuffer _buf = new StringBuffer();
         FileInputStream in = null;
         try 
         {
             in = new FileInputStream(fileLocal.getPath());
         } catch (FileNotFoundException fnf)
         {
 
         }
         int c;
         try 
         {
             while ((c = in.read()) != -1)
             {
                 _buf.append((char)c);
             }
             in.close();
         } catch (IOException ioe)
         {
             
         }
         
         if (_buf != null)
             // update text field
             m_txtDir.setText(_buf.toString());
         else
             //Log.println(Log.LEVEL3,"NodesDlg:No file chosen"); */

    }

    /**
       @roseuid 372F8C5D0142
     */
    public void actionPerformed(ActionEvent ae)
    {
        Object object = ae.getSource();
        if (object == m_btnOk)
            btnOkAction(ae);
        else if (object == m_btnCancel)
            btnCancelAction(ae);
        else if (object == m_btnColor)
            btnColorAction(ae);
        else if (object == m_btnBrowse)
            btnBrowseAction(ae);

    }

    /**
       @roseuid 372F8C5D0144
     */
    public void setModel(NodeModel p_nodeMdl)
    {
        m_nodeMdl = p_nodeMdl;
        initPanelData();

    }

    /**
       @roseuid 372F8C5D0146
     */
    private void disableControls()
    {
        m_ckbNotify.setEnabled(false);
        m_txtDesc.setEnabled(false);
        m_txtName.setEnabled(false);
        m_txtDir.setEnabled(false);
        m_btnBrowse.setEnabled(false);
    }

    public JButton getBtnBrowse()
    {
        return m_btnBrowse;
    }    
}
