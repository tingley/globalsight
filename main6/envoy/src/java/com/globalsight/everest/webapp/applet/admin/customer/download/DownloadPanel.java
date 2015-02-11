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
package com.globalsight.everest.webapp.applet.admin.customer.download;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.globalsight.everest.webapp.applet.common.EnvoyJPanel;

/**
 * The main panel for the DownloadApplet. This displays
 * a progress bar to show the user the status of the
 * download of a .zip file containing all the content
 * that needed to be downloaded from the server.
 * 
 * After the download completes, the applet extracts
 * the .zip file to the customer's filesystem.
 */
public class DownloadPanel extends EnvoyJPanel 
{
    private HashMap m_map = null;
    private DownloadTask m_downloadTask = null;

    /**
     * Constructor for creating a panel that will display 
     * the exploring like widgets.
     */
    public DownloadPanel() 
    {
        super();
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Implementation of EnvoyJTable's abstract methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the title of the panel.
     *
     * @return The panel's title.
     */
    public String getTitle()
    {
        return "Download Panel";
    }


    /**
     * Populate the applet based on the data passed in.
     * @param p_objects A Vector containing a HashMap which in turn
     * has all the data needed for this panel and its components.
     */
    public void populate(Vector p_objects) 
    {
        // get the info sent from the server
//        m_map = (HashMap)p_objects.elementAt(0);
        setLayout(new BorderLayout());
        setBackground(ENVOY_WHITE);
        JLabel lblTop = new JLabel("Download Progress");
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        JTextArea taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);
        taskOutput.setCursor(null);
        
        JPanel panel = new JPanel();
        panel.add(lblTop);
        panel.add(progressBar);
        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a directory to save to");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showSaveDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            String saveToDir = chooser.getSelectedFile().getAbsolutePath();
            m_downloadTask = new DownloadTask(getEnvoyJApplet(),
                                              progressBar,
                                              taskOutput,
                                              saveToDir);
            m_downloadTask.startDownload();
        }
        else
        {
            taskOutput.append("Operation canceled -- no local directory was chosen!");
        }
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Implementation of EnvoyJTable's abstract methods
    //////////////////////////////////////////////////////////////////////
}

