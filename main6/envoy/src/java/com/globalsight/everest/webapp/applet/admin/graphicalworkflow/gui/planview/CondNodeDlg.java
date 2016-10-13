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


import java.util.Vector;
import java.util.Enumeration;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.ItemEvent;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.ButtonGroup;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Color;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api.*;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.*;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.dmsview.DirectoryDialog;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;

import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;


public class CondNodeDlg extends JDialog implements ActionListener, ItemListener, MouseListener, WindowListener
{
    // add all these varibles in BranchInfo
    private int m_naOps[] = {BranchInfo.EQUAL_OP,BranchInfo.NOT_EQUALS_OP,BranchInfo.LESS_THAN_OP,BranchInfo.LESS_THAN_OR_EQUALS_OP,BranchInfo.GREATER_THAN_OP,BranchInfo.GREATER_THAN_OR_EQUALS_OP};
    private int m_naOpVal[];
    private int m_nOrder[];
    private int m_nRowCount = 20;
    private int m_nPrevSelected = - 1;
    private int m_nTab = 0;
    private boolean m_bDef[];
    private boolean m_bAttrActive;
    private boolean m_bAttrMissing;
    private boolean m_bNotify;
    public static final int NOTIFICATION_TAB = 0;
    public static final int LOGS_TAB = 1;
    public static final int DECISION_TAB = 2;
    private String m_straOps[] = {"=","!=","<","<=",">",">="};
    private JTextField m_txtAddress;
    private JTextArea m_txtPro;
    private JTextArea m_txtEpi;
    private JTextField m_txtVal[];
    private JButton m_btnOk;
    private JButton m_btnCancel;
    private JButton m_btnUp;
    private JButton m_btnDown;
    private JButton m_btnBrowseEpi;
    private JButton m_btnBrowsePro;
    // private JComboBox m_cbAttr;
    private JLabel m_cbAttr;

    private JCheckBox m_cbNotify;
    private JLabel m_lblAddress;
    private JRadioButton m_rbDef[];
    private JPanel m_pnlRow[];
    private JComboBox m_cbOps[];
    private JLabel m_lblDest[];
    private String m_strOps[];
    private String m_strDest[];
    private String m_strVal[];
    private Color m_clrPrev;
    private MessageCatalog m_msgCat;
    private String m_strAddress;
    private String m_strProlog;
    private String m_strEpilog;
    private JFrame m_Frame; 
    //private Node m_plNode;
    private WorkflowTask m_plNode;
    private CondNodeModel m_cnModel;
    private String m_strAttr;
    // DocRepository replace with map object
    // private DocRepository m_docRep;
    //private ProcessInstance m_procInst;
    private WorkflowInstance m_procInst;
    private boolean m_bEditMode = false;
    private JPanel pnl;
    private boolean flag = false;

    // get from servlet..
    private String m_DataItemName=GraphicalPane.condUdaDisplayValue ;
    private String m_DataItemType=WorkflowConstants.INTEGER;


    /**
       @roseuid 372F8C8A0259
     */
    private boolean saveChanges()
    {
        // m_strAttr = (String)m_cbAttr.getSelectedItem();
        /* m_strAttr = (String)m_cbAttr.getText();
         if (m_strAttr == null)
             m_strAttr = "";  */
        m_strAttr= WorkflowConstants.CONDITION_UDA;
        m_cnModel.setAttribute( m_strAttr );
        m_cnModel.setAttributeType(m_DataItemType);
        Vector vBI = new Vector();
        int nDef=0;

        // iterate thru branch info
        for (int i=0; i<m_nRowCount; i++)
        {
            BranchInfo bi = new BranchInfo();
            // save in current order
            int j = m_nOrder[i];
            // retrieve component values
            String strVal = m_txtVal[j].getText();
            bi.setValue(strVal);
            String strDest = m_lblDest[j].getText();
            bi.setArrowLabel(strDest);
            String strOp = (String)m_cbOps[j].getSelectedItem();
            int nOpVal = getOpValFromStr( strOp );
            bi.setComparisonOperator(nOpVal);
            boolean bDef = m_rbDef[j].isSelected();
            bi.setDefault(bDef);
            // add new set of branch info
            vBI.addElement(bi);
        }
        m_cnModel.setBranchInfo(vBI);

        // panel 1
        m_strAddress = m_txtAddress.getText();
        m_cnModel.setEmailAddr(m_strAddress);
        m_bNotify = m_cbNotify.isSelected();
        m_cnModel.setNotification(m_bNotify);
        // panel 2
        m_strProlog = m_txtPro.getText();
        m_cnModel.setProlog(m_strProlog);
        m_strEpilog = m_txtEpi.getText();
        m_cnModel.setEpilog(m_strEpilog);

        int nInvalidField = m_cnModel.saveModelData();
        if (nInvalidField >= 0)
        {
            // clear invalid data from field
            m_txtVal[nInvalidField].setText("");
            // pop up error dialog
            WindowUtil.showMsgDlg(m_Frame,
                                  AppletHelper.getI18nContent("msg_incorrect_data_type"),
                                  AppletHelper.getI18nContent("msg_data_conversion_error"), WindowUtil.TYPE_ERROR);
            return false;
        }
        return true;

    }

    /**
       @roseuid 372F8C8A025A
     */
    private int getRowCount()
    {
        return m_nRowCount;

    }

    private void fillLable()
    {
        // m_cbAttr.setText(m_DataItemName);
        m_cbAttr.setText(AppletHelper.getI18nContent("lb_if")+"  "+m_DataItemName+"  "+AppletHelper.getI18nContent("lb_is"));



    }
    /**
       @roseuid 372F8C8A025B
     */
    /*private void fillComboBox()
    {
        String[] strAttr = m_cnModel.getDataSet();
        if (strAttr == null) return;
        m_cbAttr.addItem("           ");
        for (int i=0; i<strAttr.length; i++)
        {
            m_cbAttr.addItem(strAttr[i]);
        }
        if (m_strAttr.equals(""))
        {
            System.out.println("inside if"+m_strAttr+"ghgh");
            m_cbAttr.setSelectedItem("           ");
            m_btnOk.setEnabled(false);
            m_btnDown.setEnabled(false);
            m_btnUp.setEnabled(false);
            for (int i = 0 ;i < m_pnlRow.length; i ++)
            {
                m_pnlRow[i].setEnabled(false);
                m_rbDef[i].setEnabled(false);
                m_cbOps[i].setEnabled(false);
                m_txtVal[i].setEnabled(false);
            }
        }
        else
        {
            System.out.println("inside else "+m_strAttr+"ghgh");
            m_cbAttr.setSelectedItem(m_strAttr);
        }

    }*/

    /**
       @roseuid 372F8C8A025C
     */
    private void initPanelData()
    {
        m_cnModel = new CondNodeModel( m_plNode );

        // panel 3 (conditional decisions)
        /*m_strAttr = m_cnModel.getAttribute();
        if (m_strAttr == null)
        {
            m_strAttr = new String("");
            m_bAttrMissing = true;
        }*/
        // disassemble branch info values
        Vector vBI = m_cnModel.getBranchInfo();
        m_nRowCount = vBI.size();
        m_strDest = new String[m_nRowCount];
        m_strVal = new String[m_nRowCount];
        m_naOpVal = new int[m_nRowCount];
        m_bDef = new boolean[m_nRowCount];
        Enumeration enumeration = vBI.elements();
        int i=0;
        // iterate thru all branch info objects

        while (enumeration.hasMoreElements())
        {
            BranchInfo bi = (BranchInfo)enumeration.nextElement();
            // retrieve label of branch arrow
            m_strDest[i] = new String(bi.getArrowLabel());
            // get value for comparision
            String strVal = bi.getValue();
            m_strVal[i] = strVal;
            // get the comparision operand
            m_naOpVal[i] = bi.getComparisonOperator();
            // check if default branch
            m_bDef[i] = bi.isDefault();

            i++;
        }

        // panel 1 (email notification)
        m_strAddress = m_cnModel.getEmailAddr();
        m_bNotify = m_cnModel.getNotification();
        // panel 2 (prolog/epilog scripts)
        m_strProlog = m_cnModel.getProlog();
        m_strEpilog = m_cnModel.getEpilog();

    }

    /**
       @roseuid 372F8C8A0263
     */
    public void mouseExited(MouseEvent me)
    {

    }

    /**
       @roseuid 372F8C8A0265
     */
    public void btnOkAction(ActionEvent ae)
    {
        if (m_bEditMode == false)
        {
            dispose();
            return;
        }

        if (saveChanges() == false)
            return;

        setVisible(false);
        dispose();

    }

    /**
       @roseuid 372F8C8A0267
     */
    public void btnUpAction(ActionEvent ae)
    {
        int nIdx=0, i=0;
        // if no selection, return
        if (m_nPrevSelected < 0) return;
        // check to see if selection is topmost
        if (m_nOrder[0] == m_nPrevSelected) return;
        m_btnDown.setEnabled(true);
        // get bounds of current selection
        Rectangle r = m_pnlRow[m_nPrevSelected].getBounds();
        // get bounds of panel above it
        for (i=0; i<m_nRowCount; i++)
        {
            if (m_nOrder[i] == m_nPrevSelected)
            {
                nIdx = m_nOrder[i-1];
                break;
            }
        }
        Rectangle r2 = m_pnlRow[nIdx].getBounds();
        // reverse and update order array
        int nTmp = m_nOrder[i-1];
        m_nOrder[i-1] = m_nOrder[i];
        m_nOrder[i] = nTmp;
        m_pnlRow[m_nPrevSelected].setBounds(r2);
        m_pnlRow[nIdx].setBounds(r);
        m_pnlRow[m_nPrevSelected].repaint();
        m_pnlRow[nIdx].repaint();

        // disable up button if topmost
        if (m_nOrder[0] == m_nPrevSelected)
            m_btnUp.setEnabled(false);

    }

    /**
       @roseuid 372F8C8A0269
     */
    public void mouseClicked(MouseEvent me)
    {

        int i=0;

        if (m_bAttrActive == true)
        {
            m_bAttrActive = false;
            return;
        }

        for (i=0; i<m_nRowCount; i++)
        {
            if (me.getSource() == m_pnlRow[i])
            {
                if (m_nPrevSelected != -1)
                {
                    m_pnlRow[m_nPrevSelected].setBackground(m_clrPrev);
                    m_pnlRow[m_nPrevSelected].repaint();
                }
                m_pnlRow[i].setBackground(Color.yellow);
                m_pnlRow[i].repaint();
                m_nPrevSelected = i;
                // enable buttons
                m_btnDown.setEnabled(true);
                m_btnUp.setEnabled(true);
                // disable down button if bottommost
                if (m_nOrder[m_nRowCount-1] == i)
                    m_btnDown.setEnabled(false);
                // disable up button if topmost
                if (m_nOrder[0] == i)
                    m_btnUp.setEnabled(false);
                return;
            }
        }



    }

    /**
       @roseuid 372F8C8A026B
     */
    public void mouseEntered(MouseEvent me)
    {

    }

    /**
       @roseuid 372F8C8A026D
     */
    public void mousePressed(MouseEvent me)
    {

    }

    /**
       @roseuid 372F8C8A026F
     */
    public void mouseReleased(MouseEvent me)
    {

    }

    /**
       @roseuid 372F8C8A0271
     */
    private JPanel buildPrefPanel1()
    {
        JPanel pnlPref1 = new JPanel();
        pnlPref1.setLayout( null);
        // get lable from page handler
        m_lblAddress = new JLabel(AppletHelper.getI18nContent("lb_email_address_c"));
        m_lblAddress.setBounds(pnlPref1.getInsets().left + 20,pnlPref1.getInsets().top + 120,200,25);

        m_txtAddress = new JTextField();
        m_txtAddress.setBounds(pnlPref1.getInsets().left + 20,pnlPref1.getInsets().top + 150,350,25);
        m_txtAddress.setText(m_strAddress);

        m_cbNotify = new JCheckBox(AppletHelper.getI18nContent("lb_notification"));
        m_cbNotify.setBounds(pnlPref1.getInsets().left + 20,pnlPref1.getInsets().top + 50,250,25);
        m_cbNotify.addItemListener(this);
        m_cbNotify.addActionListener(this);
        m_cbNotify.setToolTipText(AppletHelper.getI18nContent("lb_toggles_email_notification"));
        m_cbNotify.setSelected(m_bNotify);

        pnlPref1.add(m_cbNotify);
        pnlPref1.add(m_lblAddress);
        pnlPref1.add(m_txtAddress);

        return pnlPref1;

    }

    /**
       @roseuid 372F8C8A0272
     */
    protected JPanel buildPrefPanel2()
    {
        JPanel pnlPref2 = new JPanel();
        pnlPref2.setLayout( null );
        // get label from page handler
        JLabel lblPro = new JLabel(AppletHelper.getI18nContent("lb_prologue_c"));
        lblPro.setBounds(pnlPref2.getInsets().left + 10,pnlPref2.getInsets().top + 10,200,25);

        JScrollPane spPro = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        m_txtPro = new JTextArea();
        m_txtPro.setLineWrap(true);
        spPro.getViewport().setView(m_txtPro);
        spPro.setBounds(pnlPref2.getInsets().left + 10,pnlPref2.getInsets().top + 30,300,225);//100);
        m_txtPro.setText(m_strProlog);

        JLabel lblEpi = new JLabel(AppletHelper.getI18nContent("lb_epilogue_c"));
        lblEpi.setBounds(pnlPref2.getInsets().left + 10,pnlPref2.getInsets().top + 140,200,25);

        JScrollPane spEpi = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        m_txtEpi = new JTextArea();
        m_txtEpi.setLineWrap(true);
        spEpi.getViewport().setView(m_txtEpi);
        spEpi.setBounds(pnlPref2.getInsets().left + 10,pnlPref2.getInsets().top + 160,475,100);
        m_txtEpi.setText(m_strEpilog);

        m_btnBrowsePro = new JButton(AppletHelper.getI18nContent("lb_browse_d"));

        //m_btnBrowsePro.setBounds(pnlPref2.getInsets().left + 325,pnlPref2.getInsets().top + 80,120,25);
        m_btnBrowsePro.setBounds(pnlPref2.getInsets().left + 325,pnlPref2.getInsets().top + 80,120, 31);

        m_btnBrowsePro.addActionListener(this);
        m_btnBrowsePro.setToolTipText(AppletHelper.getI18nContent("lb_select_dms_dir"));

        m_btnBrowseEpi = new JButton(AppletHelper.getI18nContent("lb_browse_d"));
        m_btnBrowseEpi.setBounds(pnlPref2.getInsets().left + 325,pnlPref2.getInsets().top + 180,120,25);
        m_btnBrowseEpi.addActionListener(this);
        m_btnBrowseEpi.setToolTipText(AppletHelper.getI18nContent("lb_select_dms_dir"));

        pnlPref2.add(lblPro);
//        pnlPref2.add(lblEpi);
        pnlPref2.add(spPro);
//        pnlPref2.add(spEpi);

        //    un comment later    pnlPref2.add(m_btnBrowsePro);
//        pnlPref2.add(m_btnBrowseEpi);

        return pnlPref2;

    }

    /**
       @roseuid 372F8C8A0273
     */
    protected JPanel buildPrefPanel3()
    {
        JPanel pnlPref3 = new JPanel();
        pnlPref3.setLayout( null );
        getRowCount();
        m_pnlRow =  new JPanel[m_nRowCount];
        m_rbDef = new JRadioButton[m_nRowCount];
        m_txtVal = new JTextField[m_nRowCount];
        m_nOrder = new int[m_nRowCount];
        m_cbOps = new JComboBox[m_nRowCount];
        m_lblDest = new JLabel[m_nRowCount];
        JLabel[] lblThen = new JLabel[m_nRowCount];

        // get msg from label 
        //JLabel lblIf = new JLabel(m_msgCat.getMsg("if"));
        //JLabel lblIs = new JLabel(m_msgCat.getMsg("is"));
        String strThen = new String(AppletHelper.getI18nContent("lb_then"));
        m_cbAttr = new JLabel();
        //m_cbAttr = new JComboBox();
        //m_cbAttr.addItemListener(this);
        // m_cbAttr.addActionListener(this);       
        m_btnUp = new JButton(AppletHelper.getI18nContent("lb_up"));
        m_btnUp.addActionListener(this);
        m_btnDown = new JButton(AppletHelper.getI18nContent("lb_down"));
        m_btnDown.addActionListener(this);
        ButtonGroup bg = new ButtonGroup();
        pnl = new JPanel();
        int nInsetsL = pnlPref3.getInsets().left;
        int nInsetsT = pnlPref3.getInsets().top;

        pnl.setLayout(new ColumnLayout());
        //pnl.setBackground(EnvoyAppletConstants.ENVOY_WHITE);

        //lblIf.setBounds(nInsetsL + 20, nInsetsT + 20, 30, 25);
        m_cbAttr.setBounds(nInsetsL + 70, nInsetsT + 20, 150, 25);
        //lblIs.setBounds(nInsetsL + 250, nInsetsT + 20, 30, 25);
        //m_btnUp.setBounds(nInsetsL + 350, nInsetsT + 120, 80, 25); 
        m_btnUp.setBounds(nInsetsL + 350, nInsetsT + 120, 80, 31);

        //m_btnDown.setBounds(nInsetsL + 350, nInsetsT + 170, 80, 25); 
        m_btnDown.setBounds(nInsetsL + 350, nInsetsT + 170, 80, 31);

        // construct panel with nCount rows
        for (int i=0; i<m_nRowCount; i++)
        {
            m_nOrder[i] = i;
            m_pnlRow[i] = new JPanel();
            //m_pnlRow[i].setBackground(EnvoyAppletConstants.ENVOY_WHITE);
            m_pnlRow[i].setLayout(new FlowLayout(FlowLayout.LEFT));
            m_pnlRow[i].addMouseListener(this);

            m_rbDef[i] = new JRadioButton();
            //m_rbDef[i].setBackground(EnvoyAppletConstants.ENVOY_WHITE);
            bg.add(m_rbDef[i]);
            m_rbDef[i].addActionListener(this);
            m_rbDef[i].setSelected(m_bDef[i]);

            m_cbOps[i] = new JComboBox(m_straOps);
            Dimension dimCB = m_cbOps[i].getPreferredSize();
            Dimension dimCB_1 = new Dimension(dimCB.width+10, dimCB.height);
            m_cbOps[i].setPreferredSize(dimCB_1);

            String strOp = getOpStrFromVal(m_naOpVal[i]);
            m_cbOps[i].setSelectedItem(strOp);
            m_txtVal[i] = new JTextField(10);
            m_txtVal[i].addActionListener(this);
            m_txtVal[i].setText(m_strVal[i]);

            m_lblDest[i] = new JLabel(m_strDest[i]);
            lblThen[i] = new JLabel(strThen);

            m_pnlRow[i].add(m_rbDef[i]);
            m_pnlRow[i].add(m_cbOps[i]);
            m_pnlRow[i].add(m_txtVal[i]);
            m_pnlRow[i].add(lblThen[i]);
            m_pnlRow[i].add(m_lblDest[i]);

            Dimension dimPnl1 = m_pnlRow[i].getPreferredSize();
            Dimension dimPnl1_1 = new Dimension(dimPnl1.width+75, dimPnl1.height);
            m_pnlRow[i].setPreferredSize(dimPnl1_1);

            pnl.add(m_pnlRow[i]);
        }
        // use JLabel instead of JCombobox
        // fillComboBox();
        fillLable();
        JScrollPane sp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.getViewport().setView(pnl);
        sp.setBounds(nInsetsL + 20, nInsetsT + 70, 300, 150);
        pnlPref3.add(sp);
        //pnlPref3.add(lblIf);
        //pnlPref3.add(lblIs);
        pnlPref3.add(m_cbAttr);
        pnlPref3.add(m_btnUp);
        pnlPref3.add(m_btnDown);
        if (m_nRowCount > 0)
            m_clrPrev = m_pnlRow[0].getBackground();
        return pnlPref3;

    }

    /**
       @roseuid 372F8C8A0274
     */
    public void btnDownAction(ActionEvent ae)
    {
        int nIdx=0, i=0;
        // if no selection or if selection is bottommost, return
        if ((m_nPrevSelected < 0) || (m_nOrder[m_nRowCount-1] == m_nPrevSelected))
            return;
        m_btnUp.setEnabled(true);
        // get bounds of current selection
        Rectangle r = m_pnlRow[m_nPrevSelected].getBounds();
        // get bounds of panel below it
        for (i=0; i<m_nRowCount; i++)
        {
            if (m_nOrder[i] == m_nPrevSelected)
            {
                nIdx = m_nOrder[i+1];
                break;
            }
        }
        Rectangle r2 = m_pnlRow[nIdx].getBounds();
        // reverse and update order array
        int nTmp = m_nOrder[i+1];
        m_nOrder[i+1] = m_nOrder[i];
        m_nOrder[i] = nTmp;
        // repaint swapped panels
        m_pnlRow[m_nPrevSelected].setBounds(r2);
        m_pnlRow[nIdx].setBounds(r);
        m_pnlRow[m_nPrevSelected].repaint();
        m_pnlRow[nIdx].repaint();

        // disable down button if bottommost
        if (m_nOrder[m_nRowCount-1] == m_nPrevSelected)
            m_btnDown.setEnabled(false);

    }

    /**
       @roseuid 372F8C8A0276
     */
    public void itemStateChanged(ItemEvent ie)
    {
        //Log.println(Log.LEVEL3, "itemStateChanged(ItemEvent ie");
        Object object = ie.getSource();
        if (object == m_cbNotify)
            cbNotifyItemState(ie);
        /* else if (object == m_cbAttr)
         {
             m_bAttrActive = true;
         } */
    }  
    /**
       @roseuid 372F8C8A0278
     */
    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout ( new FlowLayout(FlowLayout.RIGHT) );

        // get label from page handler
        m_btnCancel = new JButton(AppletHelper.getI18nContent("lb_cancel"));
        m_btnCancel.addActionListener(this);
        buttonPanel.add( m_btnCancel );
        m_btnOk = new JButton(AppletHelper.getI18nContent("lb_ok"));
        m_btnOk.addActionListener(this);
        buttonPanel.add( m_btnOk );
        return buttonPanel;

    }

    /**
       @roseuid 372F8C8A0279
     */
    public void btnCancelAction(ActionEvent ae)
    {
        setVisible(false);
        dispose();

    }

    /**
       @roseuid 372F8C8A027B
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
       @roseuid 372F8C8A027D
     */
    public void actionPerformed(ActionEvent ae)
    {
        //Log.println(Log.LEVEL3, "actionPerformed(ActionEvent ae)");
        Object object = ae.getSource();
        if (object == m_btnOk)
            btnOkAction(ae);
        else if (object == m_btnCancel)
            btnCancelAction(ae);
        else if (object == m_btnUp)
            btnUpAction(ae);
        else if (object == m_btnDown)
            btnDownAction(ae);
        else if (object == m_btnBrowsePro)
            btnBrowseProAction(ae);
        else if (object == m_btnBrowseEpi)
            btnBrowseEpiAction(ae);
        /* else if (object == m_cbAttr)
         {
             if (flag)
             {
                 m_strAttr = (String)m_cbAttr.getSelectedItem();
                 if (m_strAttr.equals("           "))
                 {
                     //Log.println(Log.LEVEL3, "else if (object == m_cbAttr)");
                     m_btnOk.setEnabled(false);  
                     m_btnDown.setEnabled(false);
                     m_btnUp.setEnabled(false);
                     for (int i = 0 ;i < m_pnlRow.length; i ++)
                     {
                         m_pnlRow[i].setEnabled(false);
                         m_rbDef[i].setEnabled(false);
                         m_cbOps[i].setEnabled(false);
                         m_txtVal[i].setEnabled(false);
                     }
                 }
                 else
                 {
                     //Log.println(Log.LEVEL3, "else if (object == m_cbAttr): else");
                     m_btnOk.setEnabled(true);
                     for (int i = 0 ;i < m_pnlRow.length; i ++)
                     {
                         m_pnlRow[i].setEnabled(true);
                         m_rbDef[i].setEnabled(true);
                         m_cbOps[i].setEnabled(true);
                         m_txtVal[i].setEnabled(true);
                     }
                 }
             }
         }    */



    }

    /**
       @roseuid 372F8C8A027F
     */
    public void btnBrowseEpiAction(ActionEvent ae)
    {

    }

    /**
       @roseuid 372F8C8A0281
     */
    public void btnBrowseProAction(ActionEvent ae)
    {

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


        // uncomments later     
        /* DirectoryDialog dirDlg = new DirectoryDialog(m_Frame, true, m_docRep);
         dirDlg.setTitle(m_msgCat.getMsg("Select Script..."));
         WindowUtil.center(dirDlg);
         dirDlg.hideTitleField();
         dirDlg.show();
         if (dirDlg.isCancelled())
         {
             return;
         }
         
         String strPath = dirDlg.getSelectedPath();
         File fileLocal = null;
         
         try 
         {
             fileLocal = m_docRep.checkOut(strPath, false);
         } 
         catch (DMSException dmse) 
         {
             Log.printStack(Log.LEVEL3, dmse);
             return;
         } 
         catch (Exception e)
         {
             WindowUtil.showMsgDlg(m_Frame,
                 m_msgCat.getMsg(ErrorCodes.getMsg(e.getErrorCode())),
                 m_msgCat.getMsg("Internal Error!"), WindowUtil.TYPE_ERROR);
             return;
         }
         
         StringBuffer strbufContent = new StringBuffer();
         FileInputStream fis = null;
         try 
         {
             fis = new FileInputStream(fileLocal.getPath());
         } catch (FileNotFoundException fnf)
         {
             
         }
         int c;
         try
         {
             while ((c = fis.read()) != -1)
             {
                 strbufContent.append((char)c);
             }
             fis.close();
         } catch (IOException ioe)
         {
             
         }
         
         if (strbufContent != null)
             // update text field
             m_txtPro.setText(strbufContent.toString());
         else
             
         
         return;
           */
    }

    /**
       @roseuid 372F8C8A0283
     */


    public CondNodeDlg(JFrame p_Frame, WorkflowTask p_plNode, WFApp p_wfApp, int p_nTab)
    { //shailaja. Changed Frame to JFrame

        super(p_Frame, true);

        m_Frame = p_Frame;
        m_plNode = p_plNode;
        m_procInst = null;
        m_nTab = p_nTab;
        // uncoment later 
        // m_docRep = p_wfApp.getDocRepository();
        m_bAttrActive = false;
        m_bAttrMissing = false;
        m_msgCat = new MessageCatalog (GraphicalPane.localeName);



        constructUI();
        flag = true;
        /*WorkflowTemplate pl = p_plNode.getPlan();
            if (pl != null)
            {
                // disable if not in edit mode
                if (!pl.isInEditMode())
                {
                    disableControls();
                }
                else
                {
                    m_bEditMode = true;
                }
            } */
        m_bEditMode = true;

        addWindowListener(this);
    }

    public CondNodeDlg(JFrame p_Frame, WorkflowTaskInstance p_NodeInst, WFApp p_wfApp, int p_nTab)
    { //shailaja. Changed Frame to JFrame

        super(p_Frame, true);

        m_Frame = p_Frame;
        m_plNode = (WorkflowTask)p_NodeInst;
        /* try
         {
             m_procInst = p_NodeInst.getProcessInstance();
         }
         catch(Exception me)
         {
             
         } */
        m_nTab = p_nTab;
        // m_docRep = p_wfApp.getDocRepository();
        m_bAttrActive = false;    
        m_bAttrMissing = false;    
        m_msgCat = new MessageCatalog (GraphicalPane.localeName);



        constructUI();
        flag = true;
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
             WorkflowTemplate pl = p_NodeInst.getPlan();
             if (pl != null)
             {
                 // disable if not in edit mode
                 if (!pl.isInEditMode())
                 {
                     disableControls();
                 }
                 else
                 {
                         m_bEditMode = true;
                 }
             }
         }*/
        m_bEditMode = true;

        addWindowListener(this);
    }

    public CondNodeDlg(JFrame p_Frame, WorkflowTask p_plNode, WFApp p_wfApp)
    {
        this(p_Frame, p_plNode, p_wfApp, 0);
    }

    public CondNodeDlg(JFrame p_Frame, WorkflowTaskInstance p_nodeInst, WFApp p_wfApp)
    {
        this(p_Frame, p_nodeInst, p_wfApp, 0);
    }

    private void constructUI()
    {
        JPanel container = new JPanel();
        container.setLayout( new BorderLayout() );
        initPanelData();

        // assemble tab panes
        JTabbedPane tabs = new JTabbedPane();
        JPanel pnlButton = buildButtonPanel();
        JPanel pnlPref1 = buildPrefPanel1();
        JPanel pnlPref2 = buildPrefPanel2();
        JPanel pnlPref3 = buildPrefPanel3();


        //get label from page handler
        tabs.addTab(AppletHelper.getI18nContent("lb_decisions"), null, pnlPref3 );
        tabs.addTab(AppletHelper.getI18nContent("lb_scripting"), null, pnlPref2 );
        // Notification tab removed until implementation by model
        // tabs.addTab( m_msgCat.getMsg("Notification"), null, pnlPref1 );
        tabs.setSelectedIndex(m_nTab);     // build button panel

        getRootPane().setDefaultButton(m_btnOk);
        // add subpanels to main panel
        container.add(tabs, BorderLayout.CENTER);
        container.add(pnlButton, BorderLayout.SOUTH);
        getContentPane().add(container);
        setTitle(AppletHelper.getI18nContent("lb_conditional_node_properties"));
        if ( WindowUtil.isUsingNetscape() )
            setSize(getInsets().left + getInsets().right + 500, getInsets().top + getInsets().bottom + 390);
        else
            setSize(getInsets().left + getInsets().right + 500, getInsets().top + getInsets().bottom + 350);
        setResizable(true);
        // setBackground to white 
        /*pnlPref1.setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        pnlPref2.setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        pnlPref3.setBackground(EnvoyAppletConstants.ENVOY_WHITE);

        pnlButton.setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        tabs.setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        container.setBackground(EnvoyAppletConstants.ENVOY_WHITE);*/

    }   

    /**
       @roseuid 372F8C8A028A
     */
    /*private void disableControls()
    {
        m_cbAttr.setEnabled(false);
        for (int i=0; i<m_rbDef.length; i++)
        {
            m_rbDef[i].setEnabled(false);
            m_cbOps[i].setEnabled(false);
            m_txtVal[i].setEnabled(false);
            m_pnlRow[i].removeMouseListener(this);
        }
        m_btnUp.setEnabled(false);
        m_btnDown.setEnabled(false);
        m_txtPro.setEnabled(false);
        m_txtEpi.setEnabled(false);
        m_cbNotify.setEnabled(false);
        m_txtAddress.setEnabled(false);
        m_btnBrowsePro.setEnabled(false);
        m_btnBrowseEpi.setEnabled(false);
    }*/

    /**
       @roseuid 372F8C8A028B
     */
    private String getOpStrFromVal(int p_nVal)
    {
        for (int i=0; i<m_naOps.length; i++)
        {
            if (m_naOps[i] == p_nVal)
                return m_straOps[i];
        }
        return "X";  

    }

    /**
       @roseuid 372F8C8A028D
     */
    private int getOpValFromStr(String p_strStr)
    {
        for (int i=0; i<m_straOps.length; i++)
        {
            if (m_straOps[i] == p_strStr)
                return m_naOps[i];
        }
        return 0; 
    }

    /**
       @roseuid 372F8C8A028F
     */
    public void windowClosed(WindowEvent event)
    {
    }

    /**
       This is a method of <code>WindowListener</code> interface.
       @see #WindowListener
       @roseuid 372F8C8A0291
     */
    public void windowOpened(WindowEvent event)
    {


        // if (m_bAttrMissing == true)
        // {
        //   m_bAttrMissing = false;
        // if (m_cbAttr.getItemCount() > 0)
        // {
        /*                
                        WindowUtil.showMsgDlg(m_Frame,
                            m_msgCat.getMsg("The previously selected conditional attribute no longer \nexists in the current data set.  Please make another selection."),
                            m_msgCat.getMsg("Attribute Error"), WindowUtil.TYPE_INFO);
        */
        // m_cbAttr.showPopup();
        // }
        /*    	    
                    else{
                        WindowUtil.showMsgDlg(m_Frame,
                            m_msgCat.getMsg("There are no conditional attributes defined.  \nPlease define attributes and make a selection."),
                            m_msgCat.getMsg("Attribute Error"), WindowUtil.TYPE_INFO);
                    }
        */
        // }

    }

    /**
       This is a method of <code>WindowListener</code> interface.
       @see #WindowListener
       @roseuid 372F8C8A0293
     */
    public void windowClosing(WindowEvent event)
    {

        dispose();
    }

    /**
       This is a method of <code>WindowListener</code> interface.
       @see #WindowListener
       @roseuid 372F8C8A0295
     */
    public void windowIconified(WindowEvent event)
    {

    }

    /**
       This is a method of <code>WindowListener</code> interface.
       @see #WindowListener
       @roseuid 372F8C8A0297
     */
    public void windowActivated(WindowEvent event)
    {


    }

    /**
       This is a method of <code>WindowListener</code> interface.
       @see #WindowListener
       @roseuid 372F8C8A0299
     */
    public void windowDeiconified(WindowEvent event)
    {

    }

    /**
       This is a method of <code>WindowListener</code> interface.
       @see #WindowListener
       @roseuid 372F8C8A029B
     */
    public void windowDeactivated(WindowEvent event)
    {

    }

    /* HF,hiro commented out from
    public JButton getBtnBrowsePro() {
        return m_btnBrowsePro;
    }

    public JButton getBtnUp() {
        return m_btnUp;
    }

    public JButton getBtnDown() {
        return m_btnDown;
    } end*/
}
