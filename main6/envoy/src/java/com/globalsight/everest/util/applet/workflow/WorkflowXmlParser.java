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

package com.globalsight.everest.util.applet.workflow;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.TaskInfoBean;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.GraphicalWorkflowInstanceHandler;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowArrowInstance;
import com.globalsight.everest.workflow.WorkflowConditionSpec;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.ObjectUtil;

/**
 * For GBS-4022. Remove applet from job detail page.
 *
 */
public class WorkflowXmlParser
{
    static private final Logger logger = Logger.getLogger(WorkflowXmlParser.class);

    private String xml;
    private Map<String, Integer> idMaps;
    private Document document;
    private WorkflowInstance wfic;
    private Hashtable modifiedTaskInfoMap;
    private Node node;
    private List<WorkflowTaskInstance> wts;
    private List<WorkflowArrowInstance> arrows;
    private Hashtable workflowDetailData;
    private Map<String, Activity> activities;
    
    private String startPosition;
    private String endPositon;

    public WorkflowXmlParser(String xml, String ids)
    {
        this.xml = xml;
        this.idMaps = new HashMap<>();
        String[] is = ids.split(",");
        for (String id : is)
        {
            String[] ns = id.split("_");
            String sequence = ns[0];
            String i = ns[1];
            if (i.startsWith("new"))
            {
                idMaps.put(sequence, WorkflowTask.ID_UNSET);
            }
            else
            {
                idMaps.put(sequence, Integer.parseInt(i));
            }
        }
    }

    private void init(HttpSession session)
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Workflow wf = (Workflow) sessionMgr.getAttribute(WorkflowTemplateConstants.WF_INSTANCE);
        WorkflowInstance wfi = wf.getIflowInstance();
        wfic = ObjectUtil.deepClone(wfi);
        modifiedTaskInfoMap = new Hashtable();
        wts = new ArrayList<>();
        arrows = new ArrayList<>();
        workflowDetailData = (Hashtable) sessionMgr.getAttribute("workflowDetailData");
        Vector<Activity> as = (Vector) workflowDetailData.get(WorkflowTemplateConstants.ACTIVITIES);
        activities = new HashMap<>();
        for (Activity a : as)
        {
            activities.put(a.getActivityName(), a);
        }
    }

    public void parse(HttpSession session)
    {
        init(session);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try
        {
            db = dbf.newDocumentBuilder();
            document = db.parse(new StringInputStream(xml));
            Node n = getNextNode();
            while (n != null)
            {
                if ("task-node".equals(n.getNodeName()))
                {
                    handleTaskNode(n);
                }
                else if ("start-state".equals(n.getNodeName()))
                {
                    handleStart(n);
                }
                else if ("end-state".equals(n.getNodeName()))
                {
                    handleEnd(n);
                }
                else if ("decision".equals(n.getNodeName()))
                {
                    handleDecision(n);
                }
                
                n = getNextNode();
            }
        }
        catch (Exception e)
        {
            logger.error(e);
            return;
        }

        updateTasks();
        updateArrow();
        GraphicalWorkflowInstanceHandler handler = new GraphicalWorkflowInstanceHandler();
        handler.saveWorkflow(session, sessionMgr, wfic, modifiedTaskInfoMap);
    }

    private void updateTasks()
    {
        Vector<WorkflowTaskInstance> ts = wfic.getWorkflowInstanceTasks();
        for (WorkflowTaskInstance w : ts)
        {
            if ("Start".equals(w.getName()))
            {
                setPosiont(w, startPosition);
                continue;
            }
            
            if ("Exit".equals(w.getName()))
            {
                setPosiont(w, endPositon);
                continue;
            }
            
            boolean found = false;
            for (WorkflowTaskInstance w2 : wts)
            {
                if (w2.getTaskId() == w.getTaskId())
                {
                    found = true;
                    updateTask(w, w2);
                    wts.remove(w2);

                    break;
                }
            }

            if (!found)
            {
                w.setStructuralState(WorkflowConstants.REMOVED);
            }
        }

        for (WorkflowTaskInstance w2 : wts)
        {
            wfic.addWorkflowTaskInstance(w2);
        }
    }

    private void updateArrow()
    {
        Vector<WorkflowTaskInstance> ts = wfic.getWorkflowInstanceTasks();
        Map<String, WorkflowTaskInstance> taskMaps = new HashMap<>();
        List<WorkflowArrowInstance> ais = new ArrayList<>();
        for (WorkflowTaskInstance w : ts)
        {
            ais.addAll(w.getOutgoingArrows());

            if ("Start".equals(w.getActivityName()))
            {
                taskMaps.put("Start", w);
            }
            else if ("Exit".equals(w.getActivityName()))
            {
                taskMaps.put("Exit", w);
            }
            else if (w.getType() == WorkflowConstants.CONDITION)
            {
                taskMaps.put(w.getName(), w);
            }
            else
            {
                taskMaps.put("node_" + w.getSequence() + "_" + w.getActivityName(), w);
            }

        }

        // remove all arrow.
        for (WorkflowArrowInstance a : ais)
        {
            wfic.removeWorkflowArrowInstance(a);
        }

        // add all arrow.
        for (WorkflowArrowInstance a : arrows)
        {
            WorkflowTaskInstance source = taskMaps.get(a.getSourceNode().getActivityName());
            WorkflowTaskInstance target = taskMaps.get(a.getTargetNode().getActivityName());

            wfic.addArrowInstance(a.getName(), WorkflowConstants.REGULAR_ARROW, source, target);
        }
    }

    private void updateModifiedTaskInfoMap(WorkflowTaskInstance w2)
    {
        String estimatedHours = null;
        String actualHours = null;

        Rate expenseRate = HibernateUtil.get(Rate.class, w2.getExpenseRateId());
        Rate revenueRate = HibernateUtil.get(Rate.class, w2.getRevenueRateId());

        TaskInfoBean taskInfo = new TaskInfoBean(w2.getTaskId(), estimatedHours, actualHours,
                expenseRate, revenueRate, w2.getRateSelectionCriteria(), w2.getActivityName(),
                w2.getReportUploadCheck(),w2.getActivityCommentUploadCheck());

        if (taskInfo.getTaskId() == WorkflowTask.ID_UNSET)
        {
            modifiedTaskInfoMap.put(new Long(w2.getSequence()), taskInfo);
        }
        else
        {
            modifiedTaskInfoMap.put(new Long(taskInfo.getTaskId()), taskInfo);
        }
    }

    private void updateTask(WorkflowTaskInstance w1, WorkflowTaskInstance w2)
    {
        if (w1.getType() == WorkflowConstants.ACTIVITY)
        {
            if (w1.getActivity().getId() != w2.getActivity().getId())
            {
                updateModifiedTaskInfoMap(w2);
            }

            w1.setActivity(w2.getActivity());
            w1.setReportUploadCheck(w2.getReportUploadCheck());
            w1.setActivityCommentUploadCheck(w2.getActivityCommentUploadCheck());
            w1.setRoles(w2.getRoles());
            w1.setAcceptedTime(w2.getAcceptTime());
            w1.setCompletedTime(w2.getCompletedTime());
            w1.setOverdueToPM(w2.getOverdueToPM());
            w1.setOverdueToUser(w2.getOverdueToUser());
            w1.setRoleType(w2.getRoleType());
            w1.setSequence(w2.getSequence());
            w1.setRateSelectionCriteria(w2.getRateSelectionCriteria());
            w1.setExpenseRateId(w2.getExpenseRateId());
            w1.setRevenueRateId(w2.getRevenueRateId());
            w1.setDisplayRoleName(w2.getDisplayRoleName());
            w1.setActionType(w2.getActionType());
            w1.setRolePreference(w2.getRolePreference());
        }
        else if (w1.getType() == WorkflowConstants.CONDITION)
        {
            w1.setSequence(w2.getSequence());
            w1.setNodeName(w2.getNodeName());
            w1.setName(w2.getName());
        }
        
        w1.setPosition(w2.getPosition());
    }

    private void handleStart(Node n)
    {
        Node transition = getFirstChild(n);
        WorkflowArrowInstance wa = new WorkflowArrowInstance();
        WorkflowTask sourceTask = new WorkflowTask(getAttribute(n, "name"), 0);
        NamedNodeMap atts = transition.getAttributes();
        WorkflowTask targetTask = new WorkflowTask(atts.getNamedItem("to").getNodeValue(), 0);

        wa.setSourceNode(sourceTask);
        wa.setTargetNode(targetTask);
        wa.setName(atts.getNamedItem("name").getNodeValue());
        arrows.add(wa);
    }
    
    private void handleEnd(Node n)
    {
        Node action = getFirstChild(n);
        Node start = getFirstChild(action);
        Node point = getNextSibling(start);
        endPositon = point.getFirstChild().getNodeValue();
        startPosition = getValues(start).get("point");
    }
    
    private String getDefaultArrow(Node condition)
    {
       
        Node branchAttribute = getFirstChild(condition);
        Node branch = getNextSibling(branchAttribute);
        
        while (branch != null)
        {
            if (branch.getNodeName().startsWith("workflow_branch_spec"))
            {
                Map<String, String> m = getValues(branch);
                if ("true".equals(m.get("is_default")))
                {
                    return m.get("arrow_label");
                }
            }
            branch = getNextSibling(branch);
        }
        
        return "";
    }
    
    private void handleDecision(Node n)
    {
        String name = getAttribute(n, "name");
        WorkflowTaskInstance wt = new WorkflowTaskInstance(
                name, WorkflowConstants.CONDITION);
        WorkflowConditionSpec m_workflowConditionSpec = new WorkflowConditionSpec();
        wt.setConditionSpec(m_workflowConditionSpec);
        
        Node handler = getFirstChild(n);
        Node point = getFirstChild(handler);
        String p = point.getFirstChild().getNodeValue();
        setPosiont(wt, p);
        Node sequence = getNextSibling(point);
        String sn = sequence.getFirstChild().getNodeValue();
        wt.setTaskId(WorkflowTask.ID_UNSET);
        wt.setSequence(Integer.parseInt(sn));
        wt.setNodeName(name);
        
        Node condition = getNextSibling(sequence);
        String defaultArrow = getDefaultArrow(condition);
        
        List<WorkflowArrowInstance> arrs = new ArrayList<>();
        Node transition = getNextSibling(handler);
        while (transition != null && transition.getNodeName().equals("transition"))
        {
            NamedNodeMap atts = transition.getAttributes();
            WorkflowArrowInstance wa = new WorkflowArrowInstance();
            WorkflowTask targetTask = new WorkflowTask(atts.getNamedItem("to").getNodeValue(), 0);

            wa.setSourceNode(wt);
            wa.setTargetNode(targetTask);
            
            String arrName = atts.getNamedItem("name").getNodeValue();
            wa.setName(arrName);
            
            // the default should be ad first.
            if (arrName.equals(defaultArrow))
            {
                arrows.add(wa);
            }
            else
            {
                arrs.add(wa);
            }
            
            transition = getNextSibling(transition);
        }
        
        arrows.addAll(arrs);
        wts.add(wt);
    }

    private void handleTaskNode(Node n)
    {
        Node task = getFirstChild(n);

        Node assignment = getFirstChild(task);
        Map<String, String> m = getValues(assignment);

        Node transition = getNextSibling(task);
        NamedNodeMap atts = transition.getAttributes();

        WorkflowArrowInstance wa = new WorkflowArrowInstance();
        WorkflowTask sourceTask = new WorkflowTask(getAttribute(n, "name"), 0);
        WorkflowTask targetTask = new WorkflowTask(atts.getNamedItem("to").getNodeValue(), 0);

        wa.setSourceNode(sourceTask);
        wa.setTargetNode(targetTask);
        wa.setName(atts.getNamedItem("name").getNodeValue());
        arrows.add(wa);

        String name = m.get("activity");
        WorkflowTaskInstance wt = new WorkflowTaskInstance(name, WorkflowConstants.ACTIVITY);
        wt.setActivity(activities.get(name));
        updateAssignment(wt, m);
        wt.setTaskId(idMaps.get(m.get("sequence")));

        wts.add(wt);
    }

    private void updateAssignment(WorkflowTaskInstance wt, Map<String, String> m)
    {
        wt.setReportUploadCheck(Integer.parseInt(m.get("report_upload_check")));
        wt.setActivityCommentUploadCheck(Integer.parseInt(m.get("activity_comment_upload_check")));
        wt.setRoles(m.get("roles").split(","));
        wt.setAcceptedTime(Long.parseLong(m.get("accepted_time")));
        wt.setCompletedTime(Long.parseLong(m.get("completed_time")));
        wt.setOverdueToPM(Long.parseLong(m.get("overdueToPM_time")));
        wt.setOverdueToUser(Long.parseLong(m.get("overdueToUser_time")));
        wt.setRoleType(Boolean.parseBoolean(m.get("role_type")));
        wt.setSequence(Integer.parseInt(m.get("sequence")));
        wt.setRateSelectionCriteria(Integer.parseInt(m.get("rate_selection_criteria")));
        wt.setExpenseRateId(Long.parseLong(m.get("expense_rate_id")));
        wt.setRevenueRateId(Long.parseLong(m.get("revenue_rate_id")));
        wt.setDisplayRoleName(m.get("role_name"));
        wt.setActionType(m.get("action_type"));
        wt.setRolePreference(m.get("role_preference"));

        setPosiont(wt, m.get("point"));
    }

    private void setPosiont(WorkflowTaskInstance wt, String position)
    {
        String[] ps = position.split(":");
        Float x = Float.parseFloat(ps[0]);
        Float y = Float.parseFloat(ps[1]);
        int px = x.intValue();
        int py = y.intValue();

        wt.setPosition(new Point(px, py));
    }

    private String getAttribute(Node n, String name)
    {
        NamedNodeMap atts = n.getAttributes();
        return atts.getNamedItem(name).getNodeValue().trim();
    }

    private String getAttribute(NamedNodeMap atts, String name)
    {
        return atts.getNamedItem(name).getNodeValue().trim();
    }

    private Map<String, String> getValues(Node n)
    {
        Map m = new HashMap();
        NodeList ns = n.getChildNodes();

        for (int i = 0; i < ns.getLength(); i++)
        {
            Node cn = ns.item(i);
            if (cn.getNodeType() == Node.TEXT_NODE)
                continue;

            Node txt = cn.getFirstChild();
            String value = "";

            if (txt != null)
                value = txt.getNodeValue().trim();

            m.put(cn.getNodeName(), value);
        }
        return m;
    }

    private Node getFirstChild(Node node)
    {
        Node n = node.getFirstChild();
        if (n.getNodeType() == Node.TEXT_NODE)
            n = n.getNextSibling();

        return n;
    }

    private Node getNextSibling(Node node)
    {
        Node n = node.getNextSibling();
        if (n == null)
            return n;

        if (n.getNodeType() == Node.TEXT_NODE)
            n = n.getNextSibling();

        return n;
    }

    private Node getNextNode()
    {
        if (node == null)
        {
            node = document.getFirstChild().getFirstChild();
            if (node.getNodeType() == Node.TEXT_NODE)
                node = node.getNextSibling();

            return node;
        }

        node = getNextSibling(node);
        return node;
    }
}
