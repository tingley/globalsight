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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dialog;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;



class ColorLegendDlg extends Dialog implements ActionListener, WindowListener
{   
    private MessageCatalog msgCat = new MessageCatalog (GraphicalPane.localeName);

    private JButton okButton = new JButton (msgCat.getMsg("OK_KEY"));
    private JLabel inactiveLabel = new JLabel (msgCat.getMsg("INACTIVE_STATE_KEY"));
    private JLabel activeLabel = new JLabel (msgCat.getMsg("ACTIVE_STATE_KEY"));
    private JLabel completeLabel = new JLabel (msgCat.getMsg("COMPLETE_STATE_KEY"));
    private JLabel eventPendingLabel = new JLabel (msgCat.getMsg("EVENT_PENDING_KEY"));
    private JPanel inactivePanel = new JPanel ();   
    private JPanel activePanel = new JPanel ();
    private JPanel completePanel = new JPanel ();
    private JPanel eventPendingPanel = new JPanel ();
    private JLabel label1 = new JLabel (" ");
    private JLabel label2 = new JLabel (" ");
    private JLabel label3 = new JLabel (" ");
    private JLabel label4 = new JLabel (" ");
    private JLabel label5 = new JLabel (" ");
    private JLabel label6 = new JLabel (" ");
    private JLabel label7 = new JLabel (" ");
    private JLabel label8 = new JLabel (" ");

    /**
       @roseuid 372F8C800243
     */
    ColorLegendDlg(JFrame parent_frame)
    { //shailaja. Changed Frame to JFrame
        super(parent_frame, false);
        setLayout(new BorderLayout());
        inactivePanel.setBackground(UIObject.COLOR_INACTIVE);
        activePanel.setBackground(UIObject.COLOR_ACTIVE);
        completePanel.setBackground(UIObject.COLOR_COMPLETE);
        eventPendingPanel.setBackground(UIObject.COLOR_EVENT_PENDING);
        JPanel _center = new JPanel();
        _center.setLayout(new GridLayout(6, 2, 5, 5));
        _center.add(label1);
        _center.add(label2);
        _center.add(inactivePanel);
        _center.add(inactiveLabel);
        _center.add(activePanel);
        _center.add(activeLabel);
        _center.add(completePanel);
        _center.add(completeLabel);
        _center.add(eventPendingPanel);
        _center.add(eventPendingLabel);
        _center.add(label3);
        _center.add(label4);
        add("Center", _center);
        JPanel _bottom = new JPanel();
        _bottom.setLayout(new GridLayout(1, 5, 0, 0));
        _bottom.add(label5);
        _bottom.add(label6);
        _bottom.add(okButton);
        _bottom.add(label7);
        _bottom.add(label8);
        add("South", _bottom);

        setSize(210, 170);
        setBackground(Color.white);
        setTitle(msgCat.getMsg("COLOR_LEGEND_KEY"));

        okButton.addActionListener(this);
        addWindowListener(this);
    }

    /**
       @roseuid 372F8C800245
     */
    public void actionPerformed(ActionEvent _event)
    {
        if ( _event.getSource() == okButton )
        {
            dispose();
        }
    }

    /**
       @roseuid 372F8C800247
     */
    public void windowClosed(WindowEvent e)
    {
    }

    /**
       @roseuid 372F8C800249
     */
    public void windowOpened(WindowEvent e)
    {
    }

    /**
       @roseuid 372F8C80024B
     */
    public void windowIconified(WindowEvent e)
    {
    }

    /**
       @roseuid 372F8C80024D
     */
    public void windowDeiconified(WindowEvent e)
    {
    }

    /**
       @roseuid 372F8C80024F
     */
    public void windowActivated(WindowEvent e)
    {
    }

    /**
       @roseuid 372F8C800251
     */
    public void windowDeactivated(WindowEvent e)
    {
    }

    /**
       @roseuid 372F8C800253
     */
    public void windowClosing(WindowEvent e)
    {
        if (e.getSource() == this)
        {
            dispose();
        }
    }
}
