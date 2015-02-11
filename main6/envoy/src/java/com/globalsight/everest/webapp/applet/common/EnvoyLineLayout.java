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

import java.awt.LayoutManager2;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Button;

import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * EnvoyLineLayout is a layout manager that organizes its components
 * into lines, each of which can be formatted according to the
 * constraints (EnvoyConstraints) sent in with each component.
 * Components are laid out according to the order they were added, and
 * at their preferred size.
 */
public class EnvoyLineLayout
    implements LayoutManager2
{
    // The x coordinate of the container
    private int m_xCoordinate = 0;
    // The y coordinate of the container
    private int m_yCoordinate = 0;
    // The horizontal gap between components
    private int m_horizontalGap = 0;
    // The vertical gap between components
    private int m_verticalGap = 0;
    // The total width of the parent (container)
    private int m_totalWidth;
    // The total height of the parent (container)
    private int m_totalHeight;
    // The cumulative width during layout calculation
    private int m_cumulativeWidth;
    // The cumulative height during layout calculation
    private int m_cumulativeHeight;

    // A map that associates the specified constraint with a specified
    // component (key).
    private Hashtable m_componentHolderMap = null;
    // Keeps the array of lineComponentHolder built up during layout creation
    private Vector m_pageLayoutVector = null;

    //
    // Constructors
    //

    /**
     * Construct a layout with default coordinates and gaps
     * (horizontal and vertical).
     */
    public EnvoyLineLayout()
    {
        this(10, 10, 10, 10);
    }

    /**
     * Construct a layout with specified coordinates and gaps.
     * @param p_xCoordinate - The coordinate with respect to x-axis.
     * @param p_yCoordinate - The coordinate with respect to y-axis.
     * @param p_horizontalGap - The horizontal gap between components.
     * @param p_verticalGap - The vertical gap between components.
     */
    public EnvoyLineLayout(int p_xCoordinate, int p_yCoordinate,
        int p_horizontalGap, int p_verticalGap)
    {
        // instantiate the hashmap that holds the components with
        // their associated constraints
        m_componentHolderMap = new Hashtable();
        // create a new pageLayout for holding the lineComponentHolders
        m_pageLayoutVector = new Vector();

        m_yCoordinate = p_yCoordinate;
        m_xCoordinate = p_xCoordinate;

        m_horizontalGap = p_horizontalGap;
        m_verticalGap   = p_verticalGap;
    }

    //
    // LayoutManager2 Implementation
    //

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
     * @param p_component - the component to be added
     * @param p_constraints - where/how the component is added to the layout.
     */
    public void addLayoutComponent(Component p_component, Object p_constraints)
    {
        if (p_constraints == null)
        {
            throw new IllegalArgumentException(
                "Error - No constraint was specified!");
        }

        // the constraint should be set ONLY for EnvoyConstraints type.
        else if (p_constraints instanceof EnvoyConstraints)
        {
            setConstraints(p_component, (EnvoyConstraints)p_constraints);
        }
        else
        {
            throw new IllegalArgumentException(
                "Error - Constraint must be of type of EnvoyConstraints!");
        }
    }

    /**
     * Returns the alignment along the x axis.  This specifies how the
     * component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1 where
     * 0 represents alignment along the origin, 1 is aligned the
     * furthest away from the origin, 0.5 is centered, etc.
     *
     * @param target - The target container.
     */
    public float getLayoutAlignmentX(Container target)
    {
        return(float)0.5;
    }

    /**
     * Returns the alignment along the y axis.  This specifies how the
     * component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1 where
     * 0 represents alignment along the origin, 1 is aligned the
     * furthest away from the origin, 0.5 is centered, etc.
     *
     * @param target - The target container.
     */
    public float getLayoutAlignmentY(Container target)
    {
        return(float)0.5;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     *
     * @param targer - The target container.
     */
    public void invalidateLayout(Container target)
    {
        // do nothing
        return;
    }


    /**
     * Get the maximum size of this component.
     * @param target - The target container.
     * @return The maximum size of this component.
     */
    public Dimension maximumLayoutSize(Container target)
    {
        // default dimension
        return new Dimension(1000,1000);
    }

    //
    // LayoutManager Implementation
    //

    /**
     * Adds the specified component with the specified name to the layout.
     * @param name - the component name.
     * @param p_component the component to be added.
     */
    public void addLayoutComponent(String name, Component p_component)
    {
        // do nothing...- we're using the addLayoutComponent of LayoutManager2
        return;
    }


    /**
     * Lays out the container in the specified panel.
     * @param parent - the component which needs to be laid out
     */
    public void layoutContainer(Container parent)
    {
        // first we need to reset the pageLayout vector
        m_pageLayoutVector.removeAllElements();
        //get the container's insets
        Insets insets = parent.getInsets();
        m_cumulativeHeight = insets.top;
        m_totalWidth = parent.getSize().width - insets.right - insets.left;

        // if the size of the component has not been set (or is not
        // valid), return
        if (m_totalWidth < 0)
        {
            return;
        }

        m_totalHeight = parent.getSize().height - insets.top - insets.bottom;
        m_cumulativeHeight += m_yCoordinate;

        m_totalWidth = m_totalWidth - 2 * m_xCoordinate;
        m_totalHeight = m_totalHeight - 2 * m_yCoordinate;

        Component components[] = parent.getComponents();
        int numComponents = components.length;

        // loop through components
        LineRange lineRange = new LineRange(-1,-1);
        while ((lineRange = getLine(components, lineRange)).getEndingPoint() < numComponents)
        {
            // start building a lineComponentHolder and add it to the pageLayout
            m_pageLayoutVector.addElement(processLine(components, lineRange));
        }

        // fill up extra space vertically
        adjustVerticalPadding();
        // take our pageLayout description and apply its cached
        // dimensions to the actual components
        applyResizings();
    }


    /**
     * Calculates the minimum size dimensions for the specified panel
     * given the components in the specified parent container.
     * @param parent - the component to be laid out
     * @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        return new Dimension(100,100);
    }


    /**
     * Calculates the preferred size dimensions for the specified
     * panel given the components in the specified parent container.
     * @param parent the component to be laid out
     *
     * @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container parent)
    {
        return new Dimension(1000,1000);
    }


    /**
     * Removes the specified component from the layout.
     * @param p_component - the component to be removed
     */
    public void removeLayoutComponent(Component p_component)
    {
        m_componentHolderMap.remove(p_component);
    }

    //
    // Helper Methods
    //

    /**
     * Set the constraints of a particular component to be this
     * specific constraint.
     * @param p_component - The component to be added to the layout.
     * @param p_constraints - The constraints of the component.
     */
    private void setConstraints(Component p_component,
        EnvoyConstraints p_constraints)
    {
        m_componentHolderMap.put(p_component, p_constraints);
    }


    /**
     * Get the constraints of a particular component.
     * @param p_component - The component of which the constraint is
     * requested.
     * @return The constraints of the component.
     */
    private EnvoyConstraints getConstraints(Component p_component)
    {
        EnvoyConstraints results =
            (EnvoyConstraints)m_componentHolderMap.get(p_component);

        if (results == null)
        {
            throw new NoSuchElementException(
                "There were no constraints for " + p_component + ".");
        }

        return results;
    }


    /**
     * Get a line by going through components until END_OF_LINE is found
     * @param components - the container's components.
     * @param lineRange - The range of the line containing components.
     * @return A LineRange object (a line with a starting and an point).
     */
    private LineRange getLine(Component[] components, LineRange lineRange)
    {
        int startingPoint = lineRange.getEndingPoint() + 1;
        int endingPoint = startingPoint;//0;
        //endingPoint = startingPoint;

        // if the starting point is greater or equal to the length of
        // the components, it's end of the line
        if (startingPoint >= components.length)
        {
            return new LineRange(startingPoint, endingPoint);
        }

        EnvoyConstraints constraints = getConstraints(components[endingPoint]);

        while (true)
        {
            if (constraints.isEndOfLine())
            {
                return new LineRange(startingPoint, endingPoint);
            }

            endingPoint++;
            if (endingPoint >= components.length)
            {
                // we reached the end of the line, return what we have
                return new LineRange(startingPoint, endingPoint - 1);
            }

            constraints = getConstraints(components[endingPoint]);
        }
    }

    /**
     * Build a LineComponentHolder for components within the specified
     * line range (padding is applied).
     * @param components - The components to be placed in teh
     * lineComponentHolder.
     * @param lineRange - The line range for the components.
     */
    private LineComponentHolder processLine(Component[] components,
        LineRange lineRange)
    {
        // set some defaults
        int lineJustification = EnvoyConstraints.LEFT;
        LineComponentHolder lineComponentHolder = new LineComponentHolder();
        int numXStretchable = 0;
        // the size of the tallest component, so we can adjust y
        // offset after the line is done
        int maxComponentHeight = 0;
        m_cumulativeWidth = 0;
        // start at the left edge
        m_cumulativeWidth += m_xCoordinate;

        // cycle through the components on the current line
        for (int i = lineRange.getStartingPoint();
             i <= lineRange.getEndingPoint(); i++)
        {
            ComponentHolder componentHolder = new ComponentHolder(
                components[i], getConstraints(components[i]));

            // lay the component into the current line
            numXStretchable += positionComponentInCurrentLine(componentHolder);
            lineComponentHolder.addComponent(componentHolder);
            // get the current justification - note even though each
            // comp has a justification, only the last component is kept
            lineJustification = componentHolder.getConstraints().getJustification();
            // if the current component is stretchable, set the
            // lineComponentHolder flag
            if (componentHolder.getConstraints().isYResizable())
            {
                lineComponentHolder.setYStretchable(true);
            }

            // keep the maximum height for components in the row
            maxComponentHeight = java.lang.Math.max(maxComponentHeight,
                componentHolder.getConstraints().getHeight());
        }

        // the new m_cumulativeHeight gets moved down
        m_cumulativeHeight += maxComponentHeight + m_verticalGap;
        // now that we have a first pass at our line done, let's go
        // through and handle justification and padding
        formatLine(lineComponentHolder, lineJustification, numXStretchable);

        return lineComponentHolder;
    }


    /** Apply hard-wired dimensions for some components (like button)
     * or apply the preferred dimensions to the actual dimensions, and
     * maintain the cumulative width.
     * @return an integer specifying whether the component is
     * stretchable in the x direction * its weight
     */
    private int positionComponentInCurrentLine(ComponentHolder comp)
    {
        int result = 0;
        Component component = comp.getComponent();
        EnvoyConstraints constraints = (EnvoyConstraints)comp.getConstraints();

        // set the x and y of the component
        constraints.setXPosition(m_cumulativeWidth);
        constraints.setYPosition(m_cumulativeHeight);

        // if it's a button, it has to have specified width and
        // height, otherwise, take preferred settings
        if (component instanceof Button)
        {
            // if buttons are passed in with a preferred width and
            // height of -1, then assign them the default width,
            // otherwise give them the preferred width
            if (constraints.getComponentHeight() == -1 &&
                constraints.getComponentWidth() == -1)
            {
                m_cumulativeWidth += 70;
                constraints.setWidth(70);
                constraints.setHeight(22);
            }
            else
            {
                m_cumulativeWidth += constraints.getComponentWidth();
                constraints.setHeight(constraints.getComponentHeight());
                constraints.setWidth(constraints.getComponentWidth());
            }
            // TomyD -- Hack Hack....(it should be false if the size
            // is set based on the stringWidth)
            constraints.setYResizable(false);
            constraints.setXResizable(false);
        }
        else // any component other than button gets its preferred size
        {
            m_cumulativeWidth += constraints.getComponentWidth();
            constraints.setHeight(constraints.getComponentHeight());
            constraints.setWidth(constraints.getComponentWidth());
        }

        m_cumulativeWidth += m_horizontalGap;

        if (constraints.isXResizable())
        {
            result = constraints.getWeight();
        }

        return result;
    }



    /**
     * Apply justification, and pad the components to fit across the
     * line if conditions are right.
     */
    private void formatLine(LineComponentHolder lineComponentHolder,
        int lineJustification, int numStretchable)
    {
        // if numStretchable == 0, pad margins to achieve
        // justification, otherwise divide up extra space between
        // stretchable components
        m_cumulativeWidth = m_cumulativeWidth - m_horizontalGap - m_xCoordinate;
        int leftoverPixels = m_totalWidth - m_cumulativeWidth;
        if (numStretchable == 0)
        {
            switch (lineJustification)
            {
            case EnvoyConstraints.RIGHT:
                // insert all available space to the left of the components
                moveComponentsX(lineComponentHolder, 0, leftoverPixels);
                return;
            case EnvoyConstraints.CENTER:
                moveComponentsX(lineComponentHolder, 0, leftoverPixels/2);
            case EnvoyConstraints.LEFT:  // fall through
                return;
            }
        }
        else
        {
            // there are some stretchable components, so don't
            // justify; instead pad

            int increaseInEachComp = leftoverPixels / numStretchable;
            int numComponents = lineComponentHolder.numberOfComponents();
            int cumOffset = 0; // reset and use for the moving process
            int prevCumOffset = 0;

            for (int i = 0; i < numComponents; i++)
            {
                ComponentHolder componentHolder =
                    lineComponentHolder.getComponentHolderAt(i);

                if (((EnvoyConstraints)componentHolder.getConstraints()).
                    isXResizable())
                {
                    // we have a resizable one!
                    int thisComponentsShare = increaseInEachComp *
                        ((EnvoyConstraints)componentHolder.getConstraints()).getWeight();
                    int width = ((EnvoyConstraints)componentHolder.getConstraints()).getWidth() + thisComponentsShare;
                    componentHolder.getConstraints().setWidth(width);
                    cumOffset += thisComponentsShare;
                }
                // as we increase the a component's size, slide
                // subsequent components in the row over by the same
                // amount
                if (cumOffset != 0)
                {
                    int xPosition = ((EnvoyConstraints)componentHolder.getConstraints()).getXPosition();
                    // move the component over by the amount the
                    // previous component got increased
                    componentHolder.getConstraints().setXPosition(xPosition + prevCumOffset);
                    prevCumOffset = cumOffset;
                }
            }
        }
    }



    /**
     * Move all the components in the passed-in lineComponentHolder
     * over by an amount, starting at a given component.
     */
    private void moveComponentsX(LineComponentHolder lineComponentHolder,
        int startPos, int amount)
    {
        if (amount == 0)
        {
            return;
        }

        int numComponents = lineComponentHolder.numberOfComponents();
        for (int i = startPos; i < numComponents; i++)
        {
            ComponentHolder componentHolder =
                lineComponentHolder.getComponentHolderAt(i);

            int xPosition = ((EnvoyConstraints)componentHolder.getConstraints()).getXPosition();
            ((EnvoyConstraints)componentHolder.getConstraints()).setXPosition(xPosition + amount);
        }
    }


    /**
     * Move all the components in the passed-in lineComponentHolder
     * down by an amount.
     */
    private void moveComponentsY(LineComponentHolder lineComponentHolder,
        int amount)
    {
        if (amount == 0)
        {
            return;
        }

        int numComponents = lineComponentHolder.numberOfComponents();
        for (int i = 0; i < numComponents; i++)
        {
            ComponentHolder componentHolder =
                lineComponentHolder.getComponentHolderAt(i);

            int xPosition = ((EnvoyConstraints)componentHolder.getConstraints()).getYPosition();
            ((EnvoyConstraints)componentHolder.getConstraints()).setYPosition(xPosition + amount);
        }
    }


    /**
     * After we have a page layout built, we need to either stretch
     * the vertically stretchable components to fit the new component
     * size, or if there are no stretchable components, apply extra
     * pixels evenly to all the lines.
     */
    private void adjustVerticalPadding()
    {
        // we need to strip out one top border, one gutter, and insets
        // from our accumulated height
        m_cumulativeHeight = m_cumulativeHeight - m_verticalGap - m_yCoordinate;

        // cycle through all lineComponentHolders, count how many are resizable
        int numLines = m_pageLayoutVector.size();
        int numYStretchable = 0;
        for (int i = 0; i < numLines; i++)
        {
            if (((LineComponentHolder)m_pageLayoutVector./*get*/elementAt(i)).
                isYStretchable())
            {
                numYStretchable++;
            }
        }

        // now, if there are no y resizable components, pad all the lines
        if (numYStretchable == 0)
        {
            if (numLines == 1)
            {
                // we don't need to do anything in the case of one line
                return;
            }

            int yPadding = (m_totalHeight - m_cumulativeHeight) / (numLines - 1);
            int amount = yPadding;
            for (int i = 1; i < numLines; i++)
            {
                moveComponentsY((LineComponentHolder)m_pageLayoutVector./*get*/elementAt(i), amount);
                amount += yPadding;
            }
        }
        else
        {
            // otherwise, if any components are resizable, distribute
            // extra pixels among them
            int yPadding = (m_totalHeight - m_cumulativeHeight) / numYStretchable;
            boolean yPadded = false;
            int amountToMove = 0;
            for (int i = 0; i < numLines; i++)
            {
                yPadded = false;
                LineComponentHolder lineComponentHolder =
                    (LineComponentHolder)m_pageLayoutVector./*get*/elementAt(i);
                if (lineComponentHolder.isYStretchable())
                {
                    // this line needs stretching, so let's apply
                    // resizing to all components
                    for (int j = 0; j < lineComponentHolder.numberOfComponents(); j++)
                    {
                        EnvoyConstraints constr =
                            lineComponentHolder.getComponentHolderAt(j).getConstraints();
                        constr.setHeight(constr.getHeight() + yPadding);
                        yPadded = true;
                    }
                }

                moveComponentsY((LineComponentHolder)m_pageLayoutVector./*get*/elementAt(i), amountToMove);
                if (yPadded)
                {
                    amountToMove += yPadding;
                }
            }
        }
    }

    /**
     * Now that the page has been laid out in the pageLayout vector,
     * apply the calculated bounds in each component's constraints to
     * the component itself.
     */
    private void applyResizings()
    {
        // all the components' constraints have been reset, now apply
        // to the actual components
        int lineCount = m_pageLayoutVector.size();
        for (int i = 0; i < lineCount; i++)
        {
            LineComponentHolder lineComponentHolder =
                (LineComponentHolder)m_pageLayoutVector./*get*/elementAt(i);
            // for each component in the line, apply its x and y,
            // width and height
            int compCount = lineComponentHolder.numberOfComponents();
            for (int j = 0; j < compCount; j++)
            {
                ComponentHolder componentHolder = lineComponentHolder.getComponentHolderAt(j);
                int xPos = ((EnvoyConstraints)componentHolder.getConstraints()).getXPosition();
                int yPos = ((EnvoyConstraints)componentHolder.getConstraints()).getYPosition();
                int width = ((EnvoyConstraints)componentHolder.getConstraints()).getWidth();
                int height = ((EnvoyConstraints)componentHolder.getConstraints()).getHeight();
                componentHolder.getComponent().setBounds(xPos, yPos, width, height);
            }
        }
    }

    //
    // Inner Classes
    //

    // This class serves as a struct for setting and getting the
    // starting and ending points of a line
    public class LineRange
    {
        // The line's starting point
        private int startingPoint   = 0;
        // The line's ending point
        private int endingPoint     = 0;

        // Constructor
        public LineRange(int startingPoint, int endingPoint)
        {
            this.startingPoint = startingPoint;
            this.endingPoint = endingPoint;
        }

        // Get the line's starting point
        private int getStartingPoint()
        {
            return startingPoint;
        }

        // Get the line's ending point
        private int getEndingPoint()
        {
            return endingPoint;
        }
    }


    // This class holds a component with its constraint
    private class ComponentHolder
    {
        private Component comp = null;
        private EnvoyConstraints constraints = null;

        // Constructor
        ComponentHolder(Component comp, EnvoyConstraints constraints)
        {
            this.comp = comp;
            this.constraints = constraints;
        }

        // Get the component
        private Component getComponent()
        {
            return comp;
        }

        // Get the component's constraint
        public EnvoyConstraints getConstraints()
        {
            return constraints;
        }
    }


    // This class holds the collection of ComponentHolders on a line.
    private class LineComponentHolder
    {
        private Vector components = null;
        // if any component on the line is y-stretchable, then the
        // line gets marked
        private boolean isYStretchable = false;

        // Constructor
        public LineComponentHolder()
        {
            components = new Vector();
        }

        // Detemines whether the component is y-stretchable.
        public boolean isYStretchable()
        {
            return isYStretchable;
        }

        // Set the y-stretchable flag to the specified value.
        public void setYStretchable(boolean stretchable)
        {
            isYStretchable = stretchable;
        }

        // Add the component to this line.
        public void addComponent(ComponentHolder comp)
        {
            components./*add*/addElement(comp);
        }

        // Get the components of a specified line.
        public Vector getComponents()
        {
            return components;
        }

        // Get the number of components on a line.
        public int numberOfComponents()
        {
            return components.size();
        }

        // Get a specifierd component from the line.
        public ComponentHolder getComponentHolderAt(int i)
        {
            return(ComponentHolder)components./*get*/elementAt(i);
        }
    }
}
