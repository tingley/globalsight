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

import java.awt.Image;
import java.util.Vector;
import javax.swing.JPanel;

/**
 * All JPanel components displayed on the EnvoyJApplet must extend this class.
 */
public abstract class EnvoyJPanel extends JPanel implements
        EnvoyAppletConstants
{
    /**
     * The applet.
     */
    protected EnvoyJApplet m_applet = (EnvoyJApplet) GlobalEnvoy
            .getParentComponent();

    /**
     * Get the title of the panel.
     * 
     * @return The panel's title.
     */
    public abstract String getTitle();

    /**
     * Populate the data of the panel.
     * 
     * @param p_data
     *            - The data used for populating the panel components.
     */
    public abstract void populate(Vector p_data);

    /**
     * Returns the image requested.
     * 
     * @return A java.awt.Image
     */
    public Image getImage(String p_gifFile)
    {
        return EnvoyImageLoader.getImage(m_applet.getCodeBase(),
                EnvoyJApplet.class, p_gifFile);
    }

    public EnvoyJApplet getEnvoyJApplet()
    {
        return m_applet;
    }
}
