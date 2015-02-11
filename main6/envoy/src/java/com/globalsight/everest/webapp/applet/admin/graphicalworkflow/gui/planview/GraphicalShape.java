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

import java.awt.Color;
//import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.AffineTransform;
//import java.awt.Graphics2D.*;//shailaja

    //start tomyd
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Image;
import com.globalsight.everest.webapp.applet.common.EnvoyAppletConstants;
import com.globalsight.everest.webapp.applet.common.EnvoyJApplet;
import com.globalsight.everest.webapp.applet.common.EnvoyApplet;
import com.globalsight.everest.webapp.applet.common.GlobalEnvoy;
import com.globalsight.everest.webapp.applet.common.EnvoyImageLoader;
import com.globalsight.everest.workflow.WorkflowConstants;
//end TomyD

abstract class GraphicalShape
{
    Point position;
    AffineTransform at;

    private Image m_deactiveNode;
    private Image m_activeNode;
    private Image m_completedNode;
    private Image m_startNode;
    private Image m_exitNode;

    /**
       @roseuid 372F8CA1022A
     */
    public GraphicalShape(Point inPosition)
    {
        position = inPosition;        
    }

    /**
       @roseuid 372F8CA1022C
     */
    public abstract boolean contains(Point p);

    /**
       @roseuid 372F8CA1022E
     */
    public abstract void dragging(java.awt.Graphics2D g2, Point p);

    /**
       @roseuid 372F8CA10231
     */
    public abstract void paint(java.awt.Graphics2D g2, Point p, Color c, 
                               float zoomRatio);

    /**
       @roseuid 372F8CA10236
     */
    public abstract void paint(java.awt.Graphics2D g2, Point p, Color c, 
                               float zoomRatio, boolean selected);


    //start TomyD --
    // Draw the particular image for a node.
    protected void paintImage(Graphics2D g2, int x, int y, int p_state)
    {
        int m_imageWidth = 100;
        int m_imageHeight = 70;
        Image image1 = getValidImage(p_state);
        if (image1 != null)
        {
            m_imageWidth = image1.getWidth(GlobalEnvoy.getParentComponent());
            m_imageHeight = image1.getHeight(GlobalEnvoy.getParentComponent());
            g2.drawImage(image1, x, y, 
                        m_imageWidth, m_imageHeight, 
                         GlobalEnvoy.getParentComponent());            
        }
        else
        {
            g2.drawString(" X ", x + 3, 
                         y + m_imageHeight/2 + 2);
        }        
    }
    
    
     // Get the image that represents the deactive node. 
    private Image getDeactiveNode()
    {
        if (m_deactiveNode == null)
        {
            m_deactiveNode = EnvoyImageLoader.getImage(
                ((EnvoyJApplet)GlobalEnvoy.getParentComponent()).
                getCodeBase(), EnvoyApplet.class, EnvoyAppletConstants.DEACTIVE_NODE_IMAGE);
        }
        return m_deactiveNode;
    }


    // Get the image that represents an active node.
    private Image getActiveNode()
    {
        if (m_activeNode == null)
        {
            m_activeNode = EnvoyImageLoader.getImage(
                ((EnvoyJApplet)GlobalEnvoy.getParentComponent()).
                getCodeBase(), EnvoyApplet.class, EnvoyAppletConstants.ACTIVE_NODE_IMAGE);
        }
        return m_activeNode;
    }


    //Get the image that represents a completed node.     
    private Image getCompletedNode()
    {
        if (m_completedNode == null)
        {
            m_completedNode = EnvoyImageLoader.getImage(
                ((EnvoyJApplet)GlobalEnvoy.getParentComponent()).
                getCodeBase(), EnvoyApplet.class, EnvoyAppletConstants.COMPLETED_NODE_IMAGE);            
        }
        return m_completedNode;
    }        
    
    
    // Get the image that represents the start node.
    private Image getStartNode()
    {
        if (m_startNode == null)
        {
            m_startNode = EnvoyImageLoader.getImage(
                ((EnvoyJApplet)GlobalEnvoy.getParentComponent()).
                getCodeBase(), EnvoyApplet.class, EnvoyAppletConstants.START_NODE_IMAGE);
        }
        return m_startNode;               
    }

    
    // Get the image that represents the exit node.
    private Image getExitNode()
    {
        if (m_exitNode == null)
        {
            m_exitNode = EnvoyImageLoader.getImage(
                ((EnvoyJApplet)GlobalEnvoy.getParentComponent()).
                getCodeBase(), EnvoyApplet.class, EnvoyAppletConstants.EXIT_NODE_IMAGE);
        }
        return m_exitNode;               
    }
    
    /*
     * Get image based on the state 
     */
    private Image getValidImage(int p_state)
    {
        if (p_state == -2)
        {
            return getStartNode();
        }
        else if (p_state == -3)
        {
            return getExitNode();
        }
        else if (p_state == WorkflowConstants.TASK_COMPLETED)
        {
            return getCompletedNode();
        }
        else if (p_state == WorkflowConstants.TASK_ACTIVE)
        {
            return getActiveNode();
        }
        else
        {
            return getDeactiveNode();
        }
    }
    //End TomyD
}
