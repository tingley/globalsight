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
package com.globalsight.everest.webapp.applet.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Simple username, password dialog. It can handle NT Domain, but
 * does not show a separate field for it. Intead it assumes the format
 * DOMAIN\\username if domain is required or expected.
 */
public class PasswordDialog extends JDialog
{
    private String m_domain;
    private JTextField m_username;
    private JPasswordField m_password;

    private JButton m_okButton;
    private JButton m_cancelButton;

    private JLabel m_usernameLabel;
    private JLabel m_passwordLabel;
    private JLabel m_msgLabel;

    private boolean m_clickedOK = false;
    private boolean m_needDomain = false;
    private String m_msg = "Please enter your username and password.";

    /**
     * Creates a PasswordDialog
     * 
     * @param p_parent parent frame (or null)
     * @param p_title  window title
     * @param p_msg    reason for prompting for password
     * @param p_needDomain
     *                 true if the username should include the domain
     */
    public PasswordDialog(Frame p_parent, String p_title, String p_msg, boolean p_needDomain)
    {
        super(p_parent, p_title, true);
        setLocationRelativeTo(p_parent);
        m_needDomain = p_needDomain;
        m_msg = p_msg;
        m_msgLabel.setText(m_msg);
        pack();
    }


    /**
     * Gets the domain value from what the user entered for username.
     * If the user enters "GLOBALSJ1\ragade" then "GLOBALSJ1" is returned
     * assuming needsDomain is true. Otherwise there is no domain, and
     * null is returned.
     * 
     * @return String
     */
    public String getDomain()
    {
        if (m_needDomain && m_username.getText().indexOf("\\") > 1)
        {
            String parts[] = m_username.getText().split("\\\\");
            if (parts.length > 1)
                return parts[0];
            else
                return null;
        }
        else
            return null;
    }


    /**
     * Gets the username from what the user entered in the
     * username field, after the domain has been split out.
     * If the user enters "GLOBALSJ1\ragade" then "ragade"
     * is returned, assuming needsDomain is true, otherwise
     * whatever the user entered is returned.
     * 
     * @return String
     */
    public String getUsername()
    {
        if (m_needDomain && m_username.getText().indexOf("\\") > 1)
        {
            String parts[] = m_username.getText().split("\\\\");
            if (parts.length > 1)
                return parts[1];
            else
                return m_username.getText();
        }
        else
            return m_username.getText();
    }


    /**
     * Gets the password entered by the user
     * 
     * @return String
     */
    public String getPassword()
    {
        return new String (m_password.getPassword());
    }


    /**
     * Inits and sets up the dialog
     */
    protected void dialogInit()
    {
        m_username = new JTextField("", 20);
        m_password = new JPasswordField("", 20);
        m_usernameLabel = new JLabel("Username ");
        m_passwordLabel = new JLabel("Password ");
        m_msgLabel = new JLabel(m_msg);

        m_okButton = new JButton("OK");
        m_cancelButton = new JButton("Cancel");
        super.dialogInit();

        KeyListener keyListener = (new KeyAdapter() {
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
                        (e.getSource() == m_cancelButton
                        && e.getKeyCode() == KeyEvent.VK_ENTER)){
                    m_clickedOK = false;
                    PasswordDialog.this.setVisible(false);
                }
                if (e.getSource() == m_okButton &&
                        e.getKeyCode() == KeyEvent.VK_ENTER){
                    m_clickedOK = true;
                    PasswordDialog.this.setVisible(false);
                }
            }
        });
        addKeyListener(keyListener);

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Object source = e.getSource();
                if (source == m_username){
                    // the user pressed enter in the name field.
                    m_username.transferFocus();
                } else {
                    // other actions close the dialog.
                    if (source == m_password || source == m_okButton)
                        m_clickedOK = true;
                    else
                        m_clickedOK = false;
                    PasswordDialog.this.setVisible(false);
                }
            }
        };

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.insets.top = 5;
        c.insets.bottom = 5;
        JPanel pane = new JPanel(gridbag);
        pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

        c.anchor = GridBagConstraints.EAST;
        c.gridx = 0;
	c.gridy = 0;
	c.gridwidth = 2;
	c.gridheight = 1;
	c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(m_msgLabel, c);
        pane.add(m_msgLabel);

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.gridy = 1;
        gridbag.setConstraints(m_usernameLabel, c);
        pane.add(m_usernameLabel);

        gridbag.setConstraints(m_username, c);
        m_username.addActionListener(actionListener);
        m_username.addKeyListener(keyListener);
        pane.add(m_username);

        c.gridy = 2;
        gridbag.setConstraints(m_passwordLabel, c);
        pane.add(m_passwordLabel);

        gridbag.setConstraints(m_password, c);
        m_password.addActionListener(actionListener);
        m_password.addKeyListener(keyListener);
        pane.add(m_password);

        c.gridy = 3;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.CENTER;
        JPanel panel = new JPanel();
        m_okButton.addActionListener(actionListener);
        m_okButton.addKeyListener(keyListener);
        panel.add(m_okButton);
        m_cancelButton.addActionListener(actionListener);
        m_cancelButton.addKeyListener(keyListener);
        panel.add(m_cancelButton);
        gridbag.setConstraints(panel, c);
        pane.add(panel);

        getContentPane().add(pane);

        pack();
    }

    /**
     * Shows the dialog
     * 
     * @return Returns true is OK was clicked
     */
    public boolean showDialog()
    {
        setVisible(true);
        return m_clickedOK;
    }
}

