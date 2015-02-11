//                              -*- Mode: Java -*-
//
// Copyright (c) 2004-2005 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//

/*
 * ProjectPnl.java
 *
 * Created on July 29, 2004, 12:29 AM
 */

package galign.ui.view;

import galign.Setup;
import galign.ui.control.AlignmentPanel;
import galign.helpers.tmx.Tuv;
import galign.helpers.util.EditUtil;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * This displays the selected alignment.
 */
public class SelectedAlignmentPnl
    extends JPanel
{
    JTextPane m_srcTA = new JTextPane();
    JTextPane m_trgTA = new JTextPane();
    JLabel srcLabel = new javax.swing.JLabel(
        Setup.getLabel("label.selectedAlignment"));
    JLabel trgLabel = new javax.swing.JLabel();

    public SelectedAlignmentPnl()
    {
        JScrollPane srcJsp = new JScrollPane(m_srcTA);
        JScrollPane trgJsp = new JScrollPane(m_trgTA);
        srcJsp.setPreferredSize(new Dimension(100, 50));
        trgJsp.setPreferredSize(new Dimension(100, 50));

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        add(srcLabel, c);

        m_srcTA.setEditable(false);
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 1;
        add(srcJsp, c);

        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(0,5,0,0);
        add(trgLabel, c);

        m_trgTA.setEditable(false);
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 1;
        c.gridy = 1;
        add(trgJsp, c);
    }

    /**
     * Clears the textareas and set the labels.
     */
    public void init()
    {
        clear();
        srcLabel.setText(Setup.getLabel("label.selectedAlignment"));
        trgLabel.setText("");
    }

    /**
     * Clears the textareas.
     */
    public void clear()
    {
        m_srcTA.setText("");
        m_trgTA.setText("");
    }

    /**
     *  Fill in the source and target textareas with the selected data
     */
    public void updateText(TreeSet selected)
    {
        Locale sourceLocale = Setup.s_project.getSourceLocale();
        Locale targetLocale = Setup.s_project.getTargetLocale();

        srcLabel.setText(sourceLocale.getDisplayName());
        trgLabel.setText(targetLocale.getDisplayName());

        clear();

        if (EditUtil.isRTLLocale(sourceLocale))
        {
            m_srcTA.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        else
        {
            m_srcTA.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }

        if (EditUtil.isRTLLocale(targetLocale))
        {
            m_trgTA.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        else
        {
            m_trgTA.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        }

        TreeSet src = new TreeSet();
        TreeSet trg = new TreeSet();

        Iterator iter = selected.iterator();
        while (iter.hasNext())
        {
            AlignmentPanel.Line line = (AlignmentPanel.Line) iter.next();
            boolean res = src.add(line.start);
            res = trg.add(line.end);
        }

        displayText(src, m_srcTA);
        displayText(trg, m_trgTA);
    }


    /**
     * Pending approval text is displayed in gray; approved in black.
     */
    private void displayText(TreeSet set, JTextPane jtp)
    {
        MutableAttributeSet attr =  new SimpleAttributeSet();

        try
        {
            Iterator iter = set.iterator();
            while (iter.hasNext())
            {
                AlignmentPanel.AlignmentImage image =
                    (AlignmentPanel.AlignmentImage) iter.next();

                StyledDocument doc = jtp.getStyledDocument();

                if (image.getState() == AlignmentPanel.AlignmentImage.STATE_APPROVED)
                {
                    StyleConstants.setForeground(attr, Color.black);
                }
                else
                {
                    StyleConstants.setForeground(attr, Color.gray);
                }

                Tuv tuv = (Tuv)image.m_data.getTuvs().get(0);
                doc.insertString(doc.getLength(), tuv.getText(), attr);
            }
        }
        catch (Exception e)
        {
        }

        jtp.setCaretPosition(0);
    }
}
