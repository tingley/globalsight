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
package com.ui.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import com.config.properties.Resource;
import com.util.UIUtil;

/**
 * Shows the first page for update a patch or upgrade a server to a new server.
 * <p>
 * This page only show a message to note user close server and start mysql.
 * 
 */
public class WelcolmPage extends JDialog
{
    private static final long serialVersionUID = 451654912079019689L;
    private JButton nextButton = new JButton(Resource.get("button.next"));
    private JButton cancelButton = new JButton(Resource.get("button.cancel"));

    /**
     * Inits dialog.
     */
    public WelcolmPage()
    {
        super();
        initUI();
        initAction();
    }

    /**
     * Inits ui.
     */
    protected void initUI()
    {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLayout(null);
        this.setResizable(false);
        this.setTitle(Resource.get("title.mainFrame"));
        this.setSize(350, 210);
        this.setModal(true);
        this.setAlwaysOnTop(true);
        setLocation();

        JTextArea test = new JTextArea(Resource.get("msg.warnSwing"));
        test.setSize(350, 110);
        test.setLocation(20, 20);
        test.setBackground(this.getBackground());
        test.setEditable(false);
        test.setFont(UIUtil.getFrameFont());
        this.add(test);

        cancelButton.setSize(UIUtil.DEFAULT_WIDTH, UIUtil.DEFAULT_HEIGHT);
        cancelButton.setLocation(getWidth() - 2 * UIUtil.DEFAULT_WIDTH - 25,
                getHeight() - UIUtil.DEFAULT_HEIGHT - 50);
        this.add(cancelButton);

        nextButton.setSize(UIUtil.DEFAULT_WIDTH, UIUtil.DEFAULT_HEIGHT);
        nextButton.setLocation(getWidth() - UIUtil.DEFAULT_WIDTH - 20,
                getHeight() - UIUtil.DEFAULT_HEIGHT - 50);
        this.add(nextButton);
    }

    /**
     * Just for test.
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        UIUtil.setLookAndFeel();
        WelcolmPage page = new WelcolmPage();
        page.setVisible(true);
        System.exit(0);
    }

    /**
     * Adds all listener for buttons.
     */
    protected void initAction()
    {
        nextButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onClose();
            }
        });

        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onClose();
            }
        });
    }

    /**
     * Lets the dialog showed in the center of screen.
     */
    private void setLocation()
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = new Point((screen.width - getWidth()) / 2,
                (screen.height - getHeight()) / 2);

        setLocation(location);
    }

    /**
     * Let user confirm again while closing the dialog.
     */
    private void onClose()
    {
        if (JOptionPane.showConfirmDialog(this, Resource.get("confirm.exit"),
                Resource.get("title.exit"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
        else
        {
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
    }
}
