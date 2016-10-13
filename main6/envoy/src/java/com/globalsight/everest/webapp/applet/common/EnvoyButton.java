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

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics;

/**
 * EnvoyButton is a subclass of AWT Button and is used for specifying
 * the size of a button (since in awt we can't set the preferred size
 * of a button)
 */
public class EnvoyButton
    extends Button
{
    public final static String ARROW_UP_DISABLED_IMG_URL = "images/moveUpArrowx.gif";
    public final static String ARROW_DOWN_DISABLED_IMG_URL = "images/moveDownArrowx.gif";
    public final static String ARROW_UP_RELEASED_IMG_URL = "images/moveUpArrow.gif";
    public final static String ARROW_DOWN_RELEASED_IMG_URL = "images/moveDownArrow.gif";
    public final static String ARROW_UP_PRESSED_IMG_URL = "images/moveUpArrowb.gif";
    public final static String ARROW_DOWN_PRESSED_IMG_URL = "images/moveDownArrowb.gif";

    // Button Image State.
    public final static int PRESSED_STATE  = 0;
    public final static int RELEASED_STATE = 1;
    public final static int DISABLED_STATE = 2;

    // default width and height
    private int m_width = 10;
    private int m_height = 10;
    private Image m_pressedImage = null;
    private Image m_releasedImage = null;
    private Image m_disabledImage = null;
    private int m_state = -1;

    //
    // Constructor
    //

    /**
     * EnvoyButton constructer - construct a button.
     */
    public EnvoyButton()
    {
        super();
    }

    /**
     * EnvoyButton constructer - construct rendered button with just a
     * string label
     *
     * @param p_label The name that will be displayed on the button
     */
    public EnvoyButton(String p_label)
    {
        super(p_label);
    }

    /**
     * Constructor - construct a button based on a particular
     * image,width, and height.
     * @param p_releasedImage - The released image to be displayed on the button.
     * @param p_pressedImage - The pressed image to be displayed on the button.
     */
    public EnvoyButton(Image p_releasedImage, Image p_pressedImage)
    {
        m_releasedImage = p_releasedImage;
        m_pressedImage = p_pressedImage;
        m_width = m_releasedImage.getWidth(this);
        m_height = m_releasedImage.getHeight(this);
        m_state = RELEASED_STATE;
    }

    /**
     * Constructor - construct a button based on a particular image,
     * width, and height.
     * @param p_releasedImage - The released image to be displayed on the button.
     * @param p_pressedImage - The pressed image to be displayed on the button.
     * @param p_disabledImage - The disabled image to be displayed on the button.
     */
    public EnvoyButton(Image p_releasedImage, Image p_pressedImage,
        Image p_disabledImage)
    {
        m_releasedImage = p_releasedImage;
        m_pressedImage = p_pressedImage;
        m_disabledImage = p_disabledImage;
        m_width = m_disabledImage.getWidth(this);
        m_height = m_disabledImage.getHeight(this);
        m_state = DISABLED_STATE;
    }

    //
    // Public Methods
    //

    /**
     * Set the state of the button.
     * @param p_state - The state of the button.
     */
    public void setState(int p_state)
    {
        m_state = p_state;

        switch (m_state)
        {
        case RELEASED_STATE:
            m_width = m_releasedImage.getWidth(this);
            m_height = m_releasedImage.getHeight(this);
            break;
        case PRESSED_STATE:
            m_width = m_pressedImage.getWidth(this);
            m_height = m_pressedImage.getHeight(this);
            break;
        case DISABLED_STATE:
            m_width = m_disabledImage.getWidth(this);
            m_height = m_disabledImage.getHeight(this);
        }

        this.repaint();
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
     * Get the width.
     * @return The width of the button size.
     */
    public int getWidth()
    {
        return m_width;
    }

    /**
     * Set the height.
     * @param p_height - The height of the button size.
     */
    public void setHeight(int p_height)
    {
        m_height = p_height;
    }

    /**
     * Get the height.
     * @return The height of the button size.
     */
    public int getHeight()
    {
        return m_height;
    }
}
