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
package com.globalsight.everest.webapp.applet.admin;
// java
import java.awt.Choice;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.Label;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Vector;
import java.util.Hashtable;
// com.globalsight
import com.globalsight.everest.webapp.applet.common.AbstractEnvoyDialog;
import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;
import com.globalsight.everest.webapp.applet.common.EnvoyConstraints;
import com.globalsight.everest.webapp.applet.common.EnvoyLineLayout;
import com.globalsight.everest.webapp.applet.common.*;

public class WorkflowStatusDialog extends AbstractEnvoyDialog {

    private boolean m_ok;

    /**
     * Create a new WorkflowStatusDialog.
     * @param p_parent - The parent frame component.
     * @param p_title - The title of the dialog.
     * @param p_hashtable - Contains the labels, list of activities, and the default values.
     */
    public WorkflowStatusDialog(WorkflowListPanel p_parent,
                                String p_title,
                                Hashtable p_hashtable,
                                int p_type) {
        super(p_parent.getParentFrame(), p_title, p_hashtable, p_type);
    }

    public Panel getEditorPanel() {
        String[] labels = (String[]) getValue(EnvoyAppletConstants.LABELS);
        String[] values = (String[]) getValue(EnvoyAppletConstants.VALUES);

        Panel panel = new Panel();
        panel.setLayout(new EnvoyLineLayout(5, 5, 5, 5));

        Vector leftStringList = new Vector();
        Vector rightStringList = new Vector();
        for (int i = 0; i < labels.length; i++) {
            String curLabel = labels[i];
            String curValue = values[i];
            leftStringList.addElement(curLabel);
            rightStringList.addElement(curValue);
        }

        int leftStringWidth = GlobalEnvoy.getStringWidth(leftStringList) + 10;
        int rightStringWidth = GlobalEnvoy.getStringWidth(rightStringList) + 10;

        for (int j = 0; j < labels.length; j++) {
            panel.add(new Label(labels[j], Label.RIGHT),
                      new EnvoyConstraints(leftStringWidth, 24, 1, EnvoyConstraints.LEFT,
                                           EnvoyConstraints.X_NOT_RESIZABLE,
                                           EnvoyConstraints.Y_NOT_RESIZABLE,
                                           EnvoyConstraints.NOT_END_OF_LINE));
            panel.add(new Label(values[j], Label.LEFT),
                      new EnvoyConstraints(rightStringWidth, 24, 1, EnvoyConstraints.LEFT,
                                           EnvoyConstraints.X_NOT_RESIZABLE,
                                           EnvoyConstraints.Y_NOT_RESIZABLE,
                                           EnvoyConstraints.END_OF_LINE));
        }

        return panel;
    }

    public void performAction() {
        m_ok = true;
        dispose();
    }

    private boolean doModal() {
        show();
        return m_ok;
    }

    public static boolean getWorkflowStatusDialog(WorkflowListPanel p_parent,
                                                  String p_title,
                                                  Hashtable p_hashtable,
                                                  int p_type) {
        WorkflowStatusDialog dlg = new WorkflowStatusDialog(p_parent, p_title, p_hashtable, p_type);
        return dlg.doModal();
    }

    public int getDialogHeight() {
        return 200;
    }

    public int getDialogWidth() {
        return 330;
    }

}


