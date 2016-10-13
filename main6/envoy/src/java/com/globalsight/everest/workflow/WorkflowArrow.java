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
package com.globalsight.everest.workflow;

import java.awt.Point;
import java.io.Serializable;

/**
 * WorkflowArrow class is a wrapper for jbpm's Transition object.
 */

public class WorkflowArrow implements Serializable
{

    private static final long serialVersionUID = 6926343443462263217L;
    private long m_id = -1;
    private String m_name = null;
    private long m_arrowType = -1;
    private Point[] m_points = null;
    private Point m_startPoint = null;
    private Point m_endPoint = null;
    private WorkflowTask m_targetNode = null;
    private WorkflowTask m_sourceNode = null;
    private int m_structuralState = -1;

    // Flag to check if the arrow is on the default path.
    boolean isDefault = true;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * Default constructor
     */
    public WorkflowArrow()
    {
    }

    /**
     * Constructor for setting basic info of an arrow object.
     * 
     * @param p_arrowName
     *            - The name of the arrow.
     * @param p_arrowType
     *            - The type of the arrow (regular type only).
     * @param p_sourceNode
     *            - The node where the arrow is coming from.
     * @param p_targetNode
     *            - The node where the arrow is pointing to.
     */
    public WorkflowArrow(String p_arrowName, long p_arrowType,
            WorkflowTask p_sourceNode, WorkflowTask p_targetNode)

    {
        m_name = p_arrowName;
        m_arrowType = p_arrowType;
        m_sourceNode = p_sourceNode;
        m_targetNode = p_targetNode;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Helper Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the id of this arrow.
     * 
     * @return The arrow's unique id.
     */
    public long getArrowId()
    {
        return m_id;
    }

    /**
     * Get the arrow type.
     */
    public long getArrowType()
    {
        return m_arrowType;
    }

    /**
     * Get the end point for this arrow.
     * 
     * @return The arrow's end point.
     */
    public Point getEndPoint()
    {
        return m_endPoint;
    }

    /**
     * Get the name of the arrow.
     * 
     * @return The arrow's name.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Get the points for this arrow.
     * 
     * @return The arrow's points.
     */
    public Point[] getPoints()
    {
        return m_points;
    }

    /**
     * Get the source node of this arrow.
     * 
     * @return The node where the arrow starts from.
     */
    public WorkflowTask getSourceNode()
    {
        return m_sourceNode;
    }

    /**
     * Get the start point of this arrow.
     * 
     * @return The start point of this arrow.
     */
    public Point getStartPoint()
    {
        return m_startPoint;
    }

    /**
     * Get the structural state of this arrow (i.e. new, deleted, edited, or
     * unchanged).
     * 
     * @return The structural state of this arrow.
     */
    public int getStructuralState()
    {
        return m_structuralState;
    }

    /**
     * Get the target node of this arrow.
     * 
     * @return The node which this arrow is pointing to.
     */
    public WorkflowTask getTargetNode()
    {
        return m_targetNode;
    }

    /**
     * Determines whether this arrow is a valid object.
     * 
     * @return True if the arrow is valid. Otherwise, return false.
     */
    public boolean isValid()
    {
        return m_name != null;
    }

    /**
     * Set the end point of this arrow to be the specified value.
     * 
     * @param p_endPoint
     *            - The end point to be set.
     */
    public void setEndPoint(Point p_endPoint)
    {
        m_endPoint = p_endPoint;
    }

    /**
     * Set the name of this arrow to be the specified value.
     * 
     * @param p_name
     *            - The name to be set.
     */
    public void setName(String p_name)
    {
        if (m_sourceNode.getType() == WorkflowConstants.CONDITION)
        {
            m_sourceNode.getConditionSpec().updateBranchSpec(m_name, p_name);
        }
        m_name = p_name;
    }

    /**
     * Set the points of this arrow to be the specified value.
     * 
     * @param p_points
     *            - The points to be set.
     */
    public void setPoints(Point[] p_points)
    {
        m_points = p_points;
    }

    /**
     * Set the source node (starting point) of this arrow to be the specified
     * node.
     * 
     * @param p_sourceNode
     *            - The source node to be set.
     */
    public void setSourceNode(WorkflowTask p_sourceNode)
    {
        m_sourceNode = p_sourceNode;
    }

    /**
     * Set the start point of this arrow to be the specified value.
     * 
     * @param p_startPoint
     *            - The start point to be set.
     */
    public void setStartPoint(Point p_startPoint)
    {
        m_startPoint = p_startPoint;
    }

    /**
     * Set the structural state to be the specified value.
     * 
     * @param p_structuralState
     *            - The structural state to be set.
     */
    public void setStructuralState(int p_structuralState)
    {
        m_structuralState = p_structuralState;
    }

    /**
     * Set the target node (ending point) of this arrow to be the specified
     * node.
     * 
     * @param p_targetNode
     *            - The target node to be set.
     */
    public void setTargetNode(WorkflowTask p_targetNode)
    {
        m_targetNode = p_targetNode;
    }
    
    public boolean isDefault()
    {
        return isDefault;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Helper Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package Level Methods
    // ////////////////////////////////////////////////////////////////////
    // set the id of an existing arrow.
    void setArrowId(long p_id)
    {
        m_id = p_id;
    }
    
    void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Package Level Methods
    // ////////////////////////////////////////////////////////////////////
}
