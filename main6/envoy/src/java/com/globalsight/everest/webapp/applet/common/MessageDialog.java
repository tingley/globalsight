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

import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.util.Hashtable;

public class MessageDialog
    extends AbstractEnvoyDialog
{
    private boolean m_okClicked = false;

    //
    // Constructor
    //

    /**
     * Constructs a modal dialog.
     * @param p_parent - The parent frame of this dialog.
     * @param p_title - The tile of the dialog.
     * @param p_hashtable - A hashtable that contains the content of
     * the dialog (labels, and possibly data).
     */
    public MessageDialog(Frame p_parent, String p_title, Hashtable p_hashtable)
    {
        this(p_parent, p_title, p_hashtable, 1);
    }

    /**
     * Constructs a modal dialog.
     * @param p_parent - The parent frame of this dialog.
     * @param p_title - The tile of the dialog.
     * @param p_hashtable - A hashtable that contains the content of
     * the dialog (labels, and possibly data).
     * @param p_type - The dialog type (message or error/warning type).
     */
    public MessageDialog(Frame p_parent, String p_title,
        Hashtable p_hashtable, int p_type)
    {
        super(p_parent, p_title, p_hashtable, p_type);
        // "true" since the ok button is always enabled in this type of dialog.
        updateButtonStatus(true);
    }

    /**
     * Constructs a dialog.  Allows the user to specify if it
     * is modal or non-modal.
     * @param p_parent - The parent frame of this dialog.
     * @param p_title - The tile of the dialog.
     * @param p_hashtable - A hashtable that contains the content of
     * the dialog (labels, and possibly data).
     * @param p_type - The dialog type (message or error/warning type).
     * @param p_makeModal - If 'true' make the dialog modal, if
     * 'false' make it non-modal.
     */
    public MessageDialog(Frame p_parent, String p_title,
        Hashtable p_hashtable, int p_type, boolean p_makeModal)
    {
        super(p_parent, p_title, p_hashtable, p_type, p_makeModal);
        // "true" since the ok button is always enabled in this type of dialog.
        updateButtonStatus(true);
    }

    //
    // Abstract Methods
    //

    /**
     * Get the panel that should be displayed in this dialog.
     * @return The editor panel.
     */
    public Panel getEditorPanel()
    {
        String message = (String)getValue(EnvoyAppletConstants.MESSAGE);

        Panel cp = new Panel();
        cp.setLayout(new EnvoyLineLayout(5, 5, 5, 5));

        // days to complete
        TextArea messageTextArea = new TextArea();
        messageTextArea.setEditable(false);
        messageTextArea.setBackground(ENVOY_WHITE);
        cp.add(messageTextArea,
            new EnvoyConstraints(24, 24, 1, EnvoyConstraints.CENTER,
                EnvoyConstraints.X_RESIZABLE, EnvoyConstraints.Y_RESIZABLE,
                EnvoyConstraints.END_OF_LINE));

        // set the message...
        if (message != null)
        {
            // first parse the message
            EnvoyWordWrapper wrap = new EnvoyWordWrapper();
            String str = wrap.parseText(message,
                messageTextArea.getFontMetrics(DIALOG_FONT),
                getDialogWidth() - 100); // Subtract 100 to make the text 
                                         // fit
            messageTextArea.setText(str);
        }

        return cp;
    }


    /**
     * Perform a specific action when the ok button is clicked.
     */
    public void performAction()
    {
        m_okClicked = true;
        dispose();
    }

    //
    // Override Methods
    //

    /**
     * Get the height for the dialog to be displayed.
     * @return The dialog height.
     */
    public int getDialogHeight()
    {
        return 180;
    }


    /**
     * Get the width for the dialog to be displayed.
     * @return The dialog width.
     */
    public int getDialogWidth()
    {
        return 320;
    }

    // Call me to invoke this dialog as modal!
    public static boolean getMessageDialog(Frame p_parent, String p_title,
        Hashtable p_hashtable, int p_type)
    {
        MessageDialog dlg =
            new MessageDialog(p_parent, p_title, p_hashtable, p_type);

        return dlg.doModal();
    }

    // Call me to invoke this dialog specifying the type and if it
    // should be modal or not!
    public static boolean getMessageDialog(Frame p_parent, String p_title,
        Hashtable p_hashtable, int p_type, boolean p_modal)
    {
        MessageDialog dlg =
            new MessageDialog(p_parent, p_title, p_hashtable, p_type, p_modal);

        return dlg.doModal();
    }


    // Call me to invoke this dialog!
    public static boolean getMessageDialog(Frame p_parent, String p_title,
        Hashtable p_hashtable)
    {
        MessageDialog dlg = new MessageDialog(p_parent, p_title, p_hashtable);

        return dlg.doModal();
    }

    private boolean doModal()
    {
        show();
        return m_okClicked;
    }
}
