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
package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview;

import java.awt.LayoutManager;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Component;

public class ColumnLayout implements LayoutManager
{
    int yGap = 2;
    int xInset = 5;
    int yInset = 5;

    /**
       @roseuid 372F8C8102D9
     */
    public ColumnLayout()
    {
    }

    /**
       @roseuid 372F8C8102DA
     */
    public void layoutContainer(Container c)
    {
        Insets insets = c.getInsets();
        int height = yInset + insets.top;

        Component[] children = c.getComponents();
        Dimension compSize = null;
        for (int i = 0; i < children.length; i++)
        {
            compSize = children[i].getPreferredSize();
            children[i].setSize(compSize.width, compSize.height);
            children[i].setLocation( xInset + insets.left, height);
            height += compSize.height + yGap;
        }

    }

    /**
       @roseuid 372F8C8102DE
     */
    public void addLayoutComponent(String s, Component c)
    {

    }

    /**
       @roseuid 372F8C8102E2
     */
    public Dimension minimumLayoutSize(Container c)
    {
        Insets insets = c.getInsets();
        int height = yInset + insets.top;
        int width = 0 + insets.left + insets.right;

        Component[] children = c.getComponents();
        Dimension compSize = null;
        for (int i = 0; i < children.length; i++)
        {
            compSize = children[i].getPreferredSize();
            height += compSize.height + yGap;
            width = Math.max(width, compSize.width + insets.left + insets.right + xInset*2);
        }
        height += insets.bottom;
        return new Dimension( width, height);

    }

    /**
       @roseuid 372F8C8102E4
     */
    public Dimension preferredLayoutSize(Container c)
    {
        return minimumLayoutSize(c);

    }

    /**
       @roseuid 373A31900082
     */
    public void removeLayoutComponent(Component c)
    {

    }
}
