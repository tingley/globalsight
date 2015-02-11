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


/**
 * EnvoyConstraints is used as a constraint of the EnvoyLineLayout for
 * defining how a component will be added to the layout.
 */
public class EnvoyConstraints
{
    /**
     * This value indicates that the component should be left-justified.
     */
    static final public int LEFT = 1;
    /**
     * This value indicates that the component should be centered.
     */
    static final public int CENTER = 2;
    /**
     * This value indicates that the component should be right-justified.
     */
    static final public int RIGHT = 3;

    public static boolean X_NOT_RESIZABLE= false;
    public static boolean X_RESIZABLE = true;

    public static boolean Y_NOT_RESIZABLE = false;
    public static boolean Y_RESIZABLE = true;

    public static boolean NOT_END_OF_LINE = false;
    public static boolean END_OF_LINE = true;

    // the width of the component.
    private int m_componentWidth  = 0;
    // the height of the component.
    private int m_componentHeight = 0;
    // the weight of the component (for space distribution).
    private int m_weight          = 0;
    // holds the justificaiton value (left, right, or center).
    private int m_justification   = 0;

    // determines whether the component is resizable along the x-axis.
    private boolean m_xResizable  = false;
    // determines whether the component is resizable along the y-axis.
    private boolean m_yResizable  = false;
    // determines whether it's end of the line.
    private boolean m_endOfLine   = false;


    ///// NOTE: These values are being set from the layout manager /////
    // the x coordinate of the component.
    private int m_xPosition    = 0;
    // the y coordinate of the component.
    private int m_yPosition    = 0;
    // the height of the component including the padding.
    private int m_height  = 0;
    // the width of the component including the padding.
    private int m_width   = 0;

    //
    // Constructors
    //
    public EnvoyConstraints(int p_componentWidth, int p_componentHeight,
        int p_weight, int p_justification, boolean p_xResizable,
        boolean p_yResizable, boolean p_endOfLine)
    {
        m_componentWidth     = p_componentWidth;
        m_componentHeight    = p_componentHeight;
        m_weight             = p_weight;
        m_justification      = p_justification;
        m_xResizable         = p_xResizable;
        m_yResizable         = p_yResizable;
        m_endOfLine          = p_endOfLine;
    }

    /**
     * Set the component to be horizontally resizable.
     * @param resize - If true the component can be horizontally
     * resizable, otherwise the width of the component is fixed.
     */
    public void setXResizable(boolean p_resize)
    {
        m_xResizable = p_resize;
    }

    /**
     * Determines whether or not the component is horizontally resizable.
     * @return True if the component is horizontally resizable,
     * otherwise returns false.
     */
    public boolean isXResizable()
    {
        return m_xResizable;
    }

    /**
     * Set the component to be vertically resizable.
     * @param resize - If true the component can be vertically
     * resizable, otherwise the height of the component is fixed.
     */
    public void setYResizable(boolean p_resize)
    {
        m_yResizable = p_resize;
    }

    /**
     * Determines whether or not the component is vertically resizable.
     * @return True if the component is vertically resizable,
     * otherwise returns false.
     */
    public boolean isYResizable()
    {
        return m_yResizable;
    }

    /**
     * Get the preferred width of the component.
     * @return The width of the component.
     */
    public int getComponentWidth()
    {
        return m_componentWidth;
    }


    /**
     * Get the preferred height of the component.
     * @return The height of the component.
     */
    public int getComponentHeight()
    {
        return m_componentHeight;
    }

    /**
     * Determines whether it's end of the line and the next
     * component(s) should be on a new line.
     * @return True if it's end of the line, otherwise returns false.
     */
    public boolean isEndOfLine()
    {
        return m_endOfLine;
    }

    /**
     * Get the weight that specifies how to distribute extra space.
     * @return The weight for the extra space distribution.
     */
    public int getWeight()
    {
        return m_weight;
    }

    /**
     * Get the value that determines that justification of the
     * component.  There are currently three values for the
     * justification purposes: LEFT, CENTER, and RIGHT.
     * @return The justification value.
     */
    public int getJustification()
    {
        return m_justification;
    }

    /**
     * Set the x coordinate of the component.
     * @param xPosition - The x coordinate of the component.
     */
    public void setXPosition(int p_xPosition)
    {
        m_xPosition = p_xPosition;
    }

    /**
     * Get the x coordinate of the component.
     * @return The x coordinate of the component.
     */
    public int getXPosition()
    {
        return m_xPosition;
    }

    /**
     * Set the y coordinate of the component.
     * @param yPosition - The y coordinate of the component.
     */
    public void setYPosition(int p_yPosition)
    {
        m_yPosition = p_yPosition;
    }

    /**
     * Get the y coordinate of the component.
     * @return The y coordinate of the component.
     */
    public int getYPosition()
    {
        return m_yPosition;
    }

    /**
     * Set the height of the component including the padding.
     * @param height - the height of the component plus the padding.
     */
    public void setHeight(int p_height)
    {
        m_height = p_height;
    }

    /**
     * Get the height of the component (componentHeight plus the padding).
     * @return The total height of the component.
     */
    public int getHeight()
    {
        return m_height;
    }

    /**
     * Set the width of the component including the padding.
     * @param width - the width of the component plus the padding.
     */
    public void setWidth(int p_width)
    {
        m_width = p_width;
    }

    /**
     * Get the width of the component (componentWidth plus the padding).
     * @return The total width of the component.
     */
    public int getWidth()
    {
        return m_width;
    }
}
