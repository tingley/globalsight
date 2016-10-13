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

import java.awt.Label;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * EnvoyLabel is a subclass of AWT Label and is used for specifying
 * the size of a label (since in awt we can't set the preferred size
 * of a label)
 */
public class EnvoyLabel
    extends Label
{
    public static final String APPLET_FRAME_TOP     = "images/appletFrameTop.gif";
    public static final String APPLET_FRAME_BOTTOM  = "images/appletFrameBottom.gif";
    public static final String APPLET_FRAME_LEFT    = "images/appletFrameLeft.gif";
    public static final String APPLET_FRAME_RIGHT   = "images/appletFrameRight.gif";
    public static final String APPLET_FRAME_BOTTOM_LEFT = "images/appletFrameBottomLeft.gif";
    public static final String APPLET_FRAME_TOP_LEFT    = "images/appletFrameTopLeft.gif";

    public static final String DIALOG_FRAME_TOP_LEFT     = "images/dialogTopLeft.gif";
    public static final String DIALOG_FRAME_TOP          = "images/dialogTop.gif";
    public static final String DIALOG_FRAME_TOP_RIGHT    = "images/dialogTopRight.gif";
    public static final String DIALOG_FRAME_LEFT         = "images/dialogLeft.gif";
    public static final String DIALOG_FRAME_RIGHT        = "images/dialogRight.gif";
    public static final String DIALOG_FRAME_BOTTOM_LEFT  = "images/dialogBottomLeft.gif";
    public static final String DIALOG_FRAME_BOTTOM   = "images/dialogBottom.gif";
    public static final String DIALOG_FRAME_BOTTOM_RIGHT = "images/dialogBottomRight.gif";

    // default width and height
    private int m_width = 10;
    private int m_height = 10;
    private Image m_image = null;
    private boolean m_isImage = false;

    //
    // Constructor
    //

    /**
     * EnvoyLabel constructer - construct a label.
     */
    public EnvoyLabel()
    {
        super();
    }

    /**
     * EnvoyLabel constructor - construct a label based on a
     * particular string, width, and height.
     * @param p_label - The button's label.
     * @param p_width - The width of the label.
     * @param p_height - The height of the label.
     */
    public EnvoyLabel(String p_label, int p_alignment, int p_width, int p_height)
    {
        super(p_label, p_alignment);
        setBackground(EnvoyAppletConstants.ENVOY_WHITE);
        setFont(new Font("Arial", Font.PLAIN, 12));
        m_width = p_width;
        m_height = p_height;
    }

    /**
     * Constructor - construct a label based on a particular
     * image,width, and height.
     * @param p_image - The image to be displayed on the label.
     * @param p_width - The width of the label.
     * @param p_height - The height of the label.
     */
    public EnvoyLabel(Image p_image)
    {
        m_isImage = true;
        m_image = p_image;
        if (m_image != null)
        {
            m_width = m_image.getWidth(this);
            m_height = m_image.getHeight(this);
        }
    }

    //
    // Public Methods
    //

    /**
     * Set the image.
     * @param p_image - The image to be displayed on the button.
     */
    public void setImage(Image p_image)
    {
        m_isImage = true;
        m_image = p_image;
        if (m_image != null)
        {
            m_width = m_image.getWidth(this);
            m_height = m_image.getHeight(this);
        }
    }

    /**
     * Get the width.
     * @return The width of the button size.
     */
    public int getWidth()
    {
        return m_width;
    }

    /**
     * Set the width.
     * @param p_width - The width of the button size.
     */
    public void setWidth(int p_width)
    {
        m_width = p_width;
    }

    /**
     * Get the height.
     * @return The height of the button size.
     */
    public int getHeight()
    {
        return m_height;
    }

    /**
     * Set the height.
     * @param p_height - The height of the button size.
     */
    public void setHeight(int p_height)
    {
        m_height = p_height;
    }

    //
    // Override methods
    //

    /**
     * Get the minimun size of this button.
     * @return A dimension object indicating this button's minimum size.
     */
    public Dimension getMinimumSize()
    {
        return new Dimension(m_width, m_height);
    }

    /**
     * Get the preferred size of this button.
     * @return A dimension object indicating this button's preferred size.
     */
    public Dimension getPreferredSize()
    {
        return getMinimumSize();
    }

    /**
     * Component's paint override.
     * @param p_graphics - The graphics context to use for painting.
     */
    public void paint(Graphics p_graphics)
    {
        if (m_isImage)
        {
            drawImage(p_graphics);
        }
        else
        {
            super.paint(p_graphics);
        }
    }

    //
    // Private methods
    //

    // draw the image
    private void drawImage(Graphics p_graphics)
    {
        if (m_image != null)
        {
            // getBackGround() returns the button's background color.
            // if null, returns the parents.
            p_graphics.setColor(getBackground()); // rendering color...
            p_graphics.drawImage(m_image, 0, 0, m_width, m_height, this);
        }
    }
}
