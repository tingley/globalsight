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
package com.globalsight.everest.webapp.applet.common;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.globalsight.util.collections.HashtableValueOrderWalker;

/**
 * The AbstractEnvoyDialog is an abstract class for envoy dialogs.
 */
public abstract class AbstractEnvoyDialog extends Dialog implements
        EnvoyAppletConstants
{
    // The ok button
    private Button okButton = null;
    // Contains the data/objects for the dialog
    private Hashtable m_hashtable = null;
    // Dialog type (based on the type, the buttons will be displayed)
    public static final int ERROR_TYPE = 0;
    // The applet.
    private Applet m_applet = null;

    //
    // Constructor
    //

    /**
     * Constructs a modal dialog.
     * 
     * @param p_parent
     *            - The parent frame of this dialog.
     * @param p_title
     *            - The tile of the dialog.
     * @param p_hashtable
     *            - A hashtable that contains the content of the dialog (labels,
     *            and possibly data).
     */
    public AbstractEnvoyDialog(Frame p_parent, String p_title,
            Hashtable p_hashtable)
    {
        this(p_parent, p_title, p_hashtable, 1);
    }

    /**
     * Constructs a modal dialog.
     * 
     * @param p_parent
     *            - The parent frame of this dialog.
     * @param p_title
     *            - The tile of the dialog.
     * @param p_hashtable
     *            - A hashtable that contains the content of the dialog (labels,
     *            and possibly data).
     * @param p_type
     *            - The dialog type (message or error/warning type).
     */
    public AbstractEnvoyDialog(Frame p_parent, String p_title,
            Hashtable p_hashtable, int p_type)
    {
        super(p_parent, p_title);
        this.setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        setModal(true);
        setResizable(false);
        m_hashtable = p_hashtable;

        if (AppletHelper.getI18nContents() == null)
        {
            AppletHelper.setI18nContents((Hashtable<String, String>) getValue(I18N_CONTENT));
        }

        // applet
        m_applet = (Applet) GlobalEnvoy.getParentComponent();
        // create components
        initControl(p_type);
        addListeners();
    }

    /**
     * Constructs a dialog (allows the user to specify if it should be non-modal
     * or modal.
     * 
     * @param p_parent
     *            - The parent frame of this dialog.
     * @param p_title
     *            - The tile of the dialog.
     * @param p_hashtable
     *            - A hashtable that contains the content of the dialog (labels,
     *            and possibly data).
     * @param p_type
     *            - The dialog type (message or error/warning type).
     * @param p_makeModal
     *            - 'True' means make it modal, 'false' means make it non-modal.
     */
    public AbstractEnvoyDialog(Frame p_parent, String p_title,
            Hashtable p_hashtable, int p_type, boolean p_makeModal)
    {
        // call another constructor to make the dialog
        this(p_parent, p_title, p_hashtable, p_type);
        // override what ever is set in previous constructor call
        setModal(p_makeModal);
    }

    //
    // Abstract Methods
    //

    /**
     * Get the panel that should be displayed in this dialog.
     * 
     * @return The editor panel.
     */
    public abstract Panel getEditorPanel();

    /**
     * Perform a specific action when the ok button is clicked.
     */
    public abstract void performAction();

    //
    // Create and add components
    //
    private void initControl(int p_type)
    {
        setLayout(new EnvoyLineLayout(5, 5, 5, 5));
        addNotify();

        // add the editor panel to the dialog
        add(getBorderedPanel(), new EnvoyConstraints(90, 50, 1,
                EnvoyConstraints.CENTER, EnvoyConstraints.X_RESIZABLE,
                EnvoyConstraints.Y_RESIZABLE, EnvoyConstraints.END_OF_LINE));
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        if (p_type > 0)
            buttonPanel.add(createCloseButton());
        buttonPanel.add(createOkButton());
        add(buttonPanel, new EnvoyConstraints(70,
                EnvoyAppletConstants.BUTTON_HEIGHT, 1, EnvoyConstraints.CENTER,
                EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_NOT_RESIZABLE,
                EnvoyConstraints.NOT_END_OF_LINE));

        int Width = getInsets().left + getInsets().right + getDialogWidth();
        setSize(Width, getInsets().top + getInsets().bottom + getDialogHeight());
    }

    private void addListeners()
    {
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                closeDialog();
            }
        });
        this.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    closeDialog();
            }
        });
    }

    // create the OK button
    private/* EnvoyButton */Button createOkButton()
    {
        // String[] imageLabels = (String[])getValue(
        // EnvoyAppletConstants.BTN_LABELS);
        String lbOk = AppletHelper.getI18nContent("lb_ok");
        okButton = new Button(lbOk);
        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // call the action command
                performAction();
            }
        });
        return okButton;
    }

    // create the close button
    private Button createCloseButton()
    {
        // String[] imageLabels =
        // (String[])getValue(EnvoyAppletConstants.BTN_LABELS);
        String lbCancel = AppletHelper.getI18nContent("lb_cancel");
        final Button closeButton = new Button(lbCancel);
        closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                closeDialog();
            }
        });
        return closeButton;
    }

    private Panel getBorderedPanel()
    {

        Panel panel = new Panel(new BorderLayout());
        Panel northPanel = new Panel(new BorderLayout());
        Panel southPanel = new Panel(new BorderLayout());
        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(southPanel, BorderLayout.SOUTH);
        panel.add(new EnvoyLabel(), BorderLayout.WEST);
        panel.add(new EnvoyLabel(), BorderLayout.EAST);
        panel.add(getEditorPanel(), BorderLayout.CENTER);
        return panel;
    }

    //
    // Override Methods
    //

    /**
     * This method overrides the show() method in class Dialog. The purpose of
     * this method is to show the dialog in the center of the screen.
     */
    public void show()
    {
        // put the dialog in the middle of the screen
        Dimension screen_size = getToolkit().getScreenSize();
        Dimension frame_size = getSize();

        setLocation((screen_size.width - frame_size.width) / 2,
                ((screen_size.height) - (frame_size.height - 120)) / 2);

        super.show();
    }

    //
    // Local Methods
    //

    public Image getImage(String p_imageURL)
    {
        return EnvoyImageLoader.getImage(m_applet.getCodeBase(), p_imageURL);
    }

    // TomyD -- The Logon dialog will override this method for
    // resetting the jflow server.
    /**
     * Invoked when the close/cancel button is clicked.
     */
    public void closeDialog()
    {
        setVisible(false);
        dispose();
    }

    /**
     * Enables/Disables the ok button based on the flag passed in.
     * 
     * @param enabled
     *            - The flag used for enabling the ok button.
     */
    public void updateButtonStatus(boolean enabled)
    {
        okButton.setEnabled(enabled);
    }

    /**
     * Get a particular object based on the key.
     * 
     * @param p_key
     *            - The key for a specific value.
     * @return The value if found, otherwise return null.
     */
    public Object getValue(String p_key)
    {
        if (m_hashtable == null)
        {
            return null;
        }

        return m_hashtable.get(p_key);
    }

    /**
     * Tests if the specified key is in the hash.
     * 
     * @param p_key
     *            The key for the specific value.
     * @return boolean.
     */
    public boolean containsKey(Object p_key)
    {
        if (m_hashtable == null)
        {
            return false;
        }

        return m_hashtable.containsKey(p_key);
    }

    /**
     * Get the height for the dialog to be displayed.
     * 
     * @return The default dialog height.
     */
    public int getDialogHeight()
    {
        return 150;
    }

    /**
     * Get the width for the dialog to be displayed.
     * 
     * @return The default dialog width.
     */
    public int getDialogWidth()
    {
        return 430;
    }

    /**
     * Facilitates display of user's previously selected values.
     */
    protected void choiceUpdate(Choice choice, Vector vector, Hashtable pairs,
            Integer itmp)
    {
        choice.addItem((String) pairs.get(itmp));
        vector.addElement(itmp);

        Enumeration enumeration = pairs.keys();
        while (enumeration.hasMoreElements())
        {
            Integer num = (Integer) enumeration.nextElement();

            if (num.intValue() != itmp.intValue())
            {
                choice.addItem((String) pairs.get(num));
                vector.addElement(num);
            }
        }
    }

    /**
     * Facilitates display of user's previously selected values.
     */
    protected void choiceUpdate(Choice choice, Vector vector, Hashtable pairs,
            Long ltmp)
    {
        choice.addItem((String) pairs.get(ltmp));
        vector.addElement(ltmp);

        Enumeration enumeration = pairs.keys();
        while (enumeration.hasMoreElements())
        {
            Long num = (Long) enumeration.nextElement();

            if (num.longValue() != ltmp.longValue())
            {
                choice.addItem((String) pairs.get(num));
                vector.addElement(num);
            }
        }
    }

    // facilitates display of user's previously selected values
    protected void choiceUpdate(Choice choice, Vector vector,
            HashtableValueOrderWalker pairs, Integer itmp)
    {
        choice.addItem((String) pairs.get(itmp));
        vector.addElement(itmp);

        for (int i = 0; i < pairs.size(); i++)
        {
            Integer num = (Integer) pairs.getKey(i);
            if (num.intValue() != itmp.intValue())
            {
                choice.addItem((String) pairs.getValue(i));
                vector.addElement(num);
            }
        }
    }

    /**
     * Facilitates display of user's previously selected values.
     */
    protected void choiceUpdate(Choice choice, Vector vector,
            HashtableValueOrderWalker pairs, Long ltmp)
    {
        choice.addItem((String) pairs.get(ltmp));
        vector.addElement(ltmp);

        for (int i = 0; i < pairs.size(); i++)
        {
            Long num = (Long) pairs.getKey(i);

            if (num.longValue() != ltmp.longValue())
            {
                choice.addItem((String) pairs.getValue(i));
                vector.addElement(num);
            }
        }
    }
}
