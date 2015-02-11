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
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.config.properties.Resource;
import com.util.UIUtil;

/**
 * A dialog that let user specify the path of GlobalSight server. <br>
 * If no path is specified, system will exit.
 * 
 */
public class TryAgainDialog extends JDialog
{
    private static final long serialVersionUID = -2158159981016286935L;

    private JTextField pathField = new JTextField();
    private JButton cancelButton = new JButton(Resource.get("button.cancel"));
    private JButton tryButton = new JButton(Resource.get("button.try"));

    private static final int DIALOG_WIDTH = 350;
    private static final int DIALOG_HEIGHT = 210;
    private String message;

    /**
     * Init.
     */
    public TryAgainDialog(String msg)
    {
        this.message = msg;
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocation();
        setResizable(false);
        setTitle(Resource.get("title.specifyServerPath"));

        initUI();
        initAction();

        this.setModal(true);
        this.setVisible(true);
    }

    /**
     * Init the UI.
     */
    private void initUI()
    {
        this.setLayout(null);
        JTextArea test = new JTextArea(message);
        test.setSize(getWidth() - 40, 110);
        test.setLocation(20, 20);
        test.setBackground(this.getBackground());
        test.setEditable(false);
        test.setFont(UIUtil.getFrameFont());
        test.setLineWrap(true);
        this.add(test);
        
        cancelButton.setSize(UIUtil.DEFAULT_WIDTH, UIUtil.DEFAULT_HEIGHT);
        cancelButton.setLocation(getWidth() - 2 * UIUtil.DEFAULT_WIDTH - 25,
                getHeight() - UIUtil.DEFAULT_HEIGHT - 50);
        this.add(cancelButton);

        tryButton.setSize(UIUtil.DEFAULT_WIDTH, UIUtil.DEFAULT_HEIGHT);
        tryButton.setLocation(getWidth() - UIUtil.DEFAULT_WIDTH - 20,
                getHeight() - UIUtil.DEFAULT_HEIGHT - 50);
        this.add(tryButton);
    }

    private void setLocation()
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Point location = new Point((screen.width - DIALOG_WIDTH) / 2,
                (screen.height - DIALOG_HEIGHT) / 2);

        setLocation(location);
    }

    public String getServerPath()
    {
        return pathField.getText();
    }

    private void initAction()
    {
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                exit();
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                exit();
            }
        });
        
        tryButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        });

    }

    private void exit()
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

    public static void main(String[] args)
    {
        UIUtil.setLookAndFeel();
        new TryAgainDialog(Resource.get("msg.deleteFile"));
    }
}
