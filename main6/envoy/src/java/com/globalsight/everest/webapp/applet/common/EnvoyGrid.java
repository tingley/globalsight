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

// java
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Panel;
import java.util.Vector;

import javax.swing.JScrollPane;

/**
 * All panels displayed on the applet must extend this class.
 */
public abstract class EnvoyGrid extends Panel implements EnvoyAppletConstants
{
    /**
     * The applet.
     */
    protected EnvoyApplet m_applet = (EnvoyApplet) GlobalEnvoy
            .getParentComponent();

    /**
     * Get the title of the grid. This title is used by the print function.
     * <p>
     * 
     * @return The grid's title.
     */
    public abstract String getTitle();

    /**
     * Populate the data of the grid panel.
     * <p>
     * 
     * @param p_data
     *            - The data used for populating the panel components.
     */
    public abstract void populate(Vector p_data);

    /**
     * Returns the image requested.
     * <p>
     * 
     * @return A java.awt.Image
     */
    public Image getImage(String p_gifFile)
    {
        return EnvoyImageLoader.getImage(m_applet.getCodeBase(), p_gifFile);
    }

    // get the parent frame.
    public Frame getParentFrame()
    {
        Component comp = this;

        while (((comp.getParent()) != null) && !(comp instanceof Frame))
        {
            comp = comp.getParent();
        }

        return (Frame) comp;
    }

    public EnvoyApplet getEnvoyApplet()
    {
        return m_applet;
    }

    /**
     * Creates the grid and the grid borders.
     * 
     * @param p_grid
     *            The grid panel to be bordered.
     * @return A java.awt.Panel withe the grid panel bordered w/ images.
     */
    protected Panel getBorderedGridPanel(JScrollPane scrollPane)
    {
        // Create the frame labels.
        final EnvoyLabel topLabel = new EnvoyLabel(
                getImage(EnvoyLabel.APPLET_FRAME_TOP));
        final EnvoyLabel bottomLabel = new EnvoyLabel(
                getImage(EnvoyLabel.APPLET_FRAME_BOTTOM));
        final EnvoyLabel rightLabel = new EnvoyLabel(
                getImage(EnvoyLabel.APPLET_FRAME_RIGHT));

        Panel gridPanel = new Panel(new BorderLayout());
        // gridPanel.add(topLabel, BorderLayout.NORTH);
        // gridPanel.add(bottomLabel, BorderLayout.SOUTH);
        // gridPanel.add(new EnvoyLabel("", EnvoyLabel.LEFT, 20,
        // rightLabel.getHeight()), BorderLayout.WEST);
        // gridPanel.add(rightLabel, BorderLayout.EAST);
        gridPanel.add(scrollPane, BorderLayout.CENTER);
        return gridPanel;
    }

    /**
     * Creates the button panel.
     * 
     * @param p_buttons
     *            The list of buttons to be placed on the panel.
     */
    protected Panel getButtonPanel(Vector p_buttons)
    {
        Panel buttonPanel = new Panel(new EnvoyLineLayout(0, 0, 0, 0));
        buttonPanel.setBackground(ENVOY_WHITE);
        // addToPanel(buttonPanel, new
        // EnvoyLabel(getImage(EnvoyLabel.APPLET_FRAME_TOP_LEFT)),
        // BUTTON_WIDTH, APPLET_FRAME_TOP_LEFT_LABEL_HEIGHT,
        // EnvoyConstraints.Y_NOT_RESIZABLE);
        for (int i = 0; i < p_buttons.size(); i++)
        {
            if (p_buttons.elementAt(i) instanceof String)
            {

                /*
                 * if (((String)p_buttons.elementAt(i)).equals("end")) {
                 * addToPanel(buttonPanel, new
                 * EnvoyLabel(getImage(EnvoyLabel.APPLET_FRAME_LEFT)),
                 * BUTTON_WIDTH, APPLET_FRAME_BOTTOM_LEFT_LABEL_HEIGHT,
                 * EnvoyConstraints.Y_RESIZABLE); addToPanel(buttonPanel, new
                 * EnvoyLabel(getImage(EnvoyLabel.APPLET_FRAME_BOTTOM_LEFT)),
                 * BUTTON_WIDTH, APPLET_FRAME_BOTTOM_LEFT_LABEL_HEIGHT,
                 * EnvoyConstraints.Y_NOT_RESIZABLE); } else
                 * addToPanel(buttonPanel, new
                 * EnvoyLabel(getImage(EnvoyLabel.APPLET_FRAME_LEFT)),
                 * BUTTON_WIDTH, BUTTON_HEIGHT,
                 * EnvoyConstraints.Y_NOT_RESIZABLE);
                 */
            }
            else
            {
                addToPanel(buttonPanel, (EnvoyButton) p_buttons.elementAt(i),
                        BUTTON_WIDTH, BUTTON_HEIGHT,
                        EnvoyConstraints.Y_NOT_RESIZABLE);
            }
        }

        return buttonPanel;
    }

    // TomyD -- my changes are addition of getHorizontalButtonPanel
    // method, addition of a new addToPanel method and modification of
    // the existing one without affecting existing code.

    /**
     * Creates the button panel and places the buttons horizontally.
     */
    protected Panel getHorizontalButtonPanel(Vector p_buttons)
    {
        Panel buttonPanel = new Panel(new EnvoyLineLayout(0, 0, 0, 0));
        buttonPanel.setBackground(ENVOY_WHITE);

        for (int i = 0; i < p_buttons.size(); i++)
        {
            // In some UI's a string "end" or "blank" is used for
            // spacing purposes and some image is added to the panel.
            // In this case, we don't care about adding images since
            // you want a horizontal layout. I have commented out the
            // logic for adding a space for now.
            if (p_buttons.elementAt(i) instanceof String)
            {
                // this just adds some extra space between buttons.
                /*
                 * addToPanel(buttonPanel, new EnvoyLabel(), BUTTON_WIDTH,
                 * BUTTON_HEIGHT, EnvoyConstraints.Y_NOT_RESIZABLE,
                 * EnvoyConstraints.NOT_END_OF_LINE);
                 */
            }
            else
            {
                addToPanel(buttonPanel, (EnvoyButton) p_buttons.elementAt(i),
                        BUTTON_WIDTH, BUTTON_HEIGHT,
                        EnvoyConstraints.Y_NOT_RESIZABLE,
                        EnvoyConstraints.NOT_END_OF_LINE);
            }
        }

        return buttonPanel;
    }

    private void addToPanel(Panel p_panel, Component p_comp, int p_width,
            int p_height, boolean p_yResizable)
    {
        addToPanel(p_panel, p_comp, p_width, p_height, p_yResizable,
                EnvoyConstraints.END_OF_LINE);

        // commented out this one to call the next method...
        /*
         * p_panel.add(p_comp, new EnvoyConstraints(p_width, p_height, 1,
         * EnvoyConstraints.LEFT, EnvoyConstraints.X_NOT_RESIZABLE,
         * p_yResizable, EnvoyConstraints.END_OF_LINE));
         */
    }

    private void addToPanel(Panel p_panel, Component p_comp, int p_width,
            int p_height, boolean p_yResizable, boolean p_isEndOfLine)
    {
        p_panel.add(p_comp, new EnvoyConstraints(p_width, p_height, 1,
                EnvoyConstraints.LEFT, EnvoyConstraints.X_NOT_RESIZABLE,
                p_yResizable, p_isEndOfLine));
    }
}
