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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;

import com.globalsight.cxe.util.Dom4jUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.StringUtil;

public class WorkflowTemplateAdapter extends WorkflowHelper
{

    private static final Logger c_category = Logger
            .getLogger(WorkflowHelper.class);

    private WorkflowTask m_startTask = null;

    public static WorkflowTemplateAdapter createInstance()
    {
        return new WorkflowTemplateAdapter();
    }

    /**
     * Creates a new workflow template.
     * <p>
     * o
     * 
     * @param p_workflowTemplate
     *            - The template to be created.
     * @param p_ctx
     *            - The JbpmContext object used for template ownership.
     * @param p_worklfowOwners
     *            - The owner(s) of the workflow instances.
     */
    public WorkflowTemplate createWorkflowTemplate(
            WorkflowTemplate p_workflowTemplate, WorkflowOwners p_workflowOwners)
            throws Exception
    {
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            Document document = DocumentHelper.createDocument();
            setTemplateName(document, p_workflowTemplate.getName());
            setNodeNames(p_workflowTemplate.getWorkflowTasks());
            createTemplateNodes(document, p_workflowTemplate, p_workflowOwners);
            ProcessDefinition pd = ProcessDefinition.parseXmlString(Dom4jUtil
                    .formatXML(document, Dom4jUtil.UTF8));
            ctx.deployProcessDefinition(pd);
            p_workflowTemplate.setId(pd.getId());
            saveXmlToFileStore(document, p_workflowTemplate.getName());
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            ctx.close();
        }

        return p_workflowTemplate;
    }

    /**
     * Import a workflow template.
     * <p>
     * o
     * 
     * @param p_workflowTemplate
     *            - The template to be created.
     * @param p_ctx
     *            - The JbpmContext object used for template ownership.
     * @param doc
     *            The workflow xml template document.
     */
    public WorkflowTemplate importWorkflowTemplate(
            WorkflowTemplate p_workflowTemplate, Document doc) throws Exception
    {
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessDefinition pd = ProcessDefinition
                    .parseXmlString(doc.asXML());
            ctx.deployProcessDefinition(pd);
            p_workflowTemplate.setId(pd.getId());
            saveXmlToFileStore(doc, p_workflowTemplate.getName());
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            ctx.close();
        }

        return p_workflowTemplate;
    }

    /**
     * Saves the workflow template xml to the file storage dir.
     * 
     * @param p_document
     *            - the xml document of the workflow template.
     * @param p_templateName
     *            - the workflow template name.
     * 
     */
    private void saveXmlToFileStore(Document p_document, String p_templateName)
    {
        OutputFormat format = OutputFormat.createCompactFormat();
        XMLWriter writer = null;
        try
        {
            String aaa = AmbFileStoragePathUtils.getWorkflowTemplateXmlDir()
                    .getAbsolutePath();
            writer = new XMLWriter(new FileOutputStream(AmbFileStoragePathUtils
                    .getWorkflowTemplateXmlDir().getAbsolutePath()
                    + File.separator
                    + p_templateName
                    + WorkflowConstants.SUFFIX_XML), format);
            writer.write(p_document);
        }
        catch (Exception e)
        {
            c_category
                    .info("Exception occurs when saving the template xml to file storage");
        }
        finally
        {
            try
            {
                if (writer != null)
                    writer.close();
            }
            catch (IOException e)
            {
                // ignore
            }
        }
    }

    /**
     * Gets a workflow template object by converting jBPM's ProcessDefinition
     * object to the WorkflowTemplate object.
     * <p>
     * 
     * @param p_plan
     *            - i-Flow's plan object.
     * @param p_wfSession
     *            - The WFSession object based on the user.
     * 
     * @exception Exception
     *                - i-Flow related exception.
     * 
     */
    public WorkflowTemplate getWorkflowTemplate(long p_templateId)
            throws Exception
    {

        WorkflowTemplate wfTemplate = new WorkflowTemplate();
        JbpmContext context = null;

        try
        {

            /* The map contains the WorkflowNodeParameter */
            Map<String, WorkflowNodeParameter> map = new HashMap<String, WorkflowNodeParameter>();
            List<WorkflowTask> tasks = new ArrayList<WorkflowTask>();
            context = WorkflowConfiguration.getInstance().getJbpmContext();

            ProcessDefinition processDefinition = context.getGraphSession()
                    .getProcessDefinition(p_templateId);

            /* gets the nodes in the processdefinition */
            List nodeList = processDefinition.getNodes();

            /* Create the workflowTask first */
            createWorkflowTask(wfTemplate, nodeList, map, tasks);

            /* add the arrow to each node */
            addArrowToTask(nodeList, tasks, wfTemplate, map);

            /* add the properties to the template task */
            addPropertiesToTemplateTask(tasks, nodeList, map);

            /* add the common properties to the template */
            addCommonProperties(wfTemplate, map, processDefinition);

            /* set the max sequence to the template */
            wfTemplate.setMaxSequence(WorkflowAdapterHelper.getMaxSeq(tasks));

        }
        catch (Exception e)
        {
            c_category
                    .error("error occured when the get the template, the id is "
                            + p_templateId);
            c_category.error("error occured ", e);
        }
        finally
        {
            context.close();
        }

        return wfTemplate;
    }

    /**
     * Sets the workflow template name in the xml.
     * 
     * @param p_document
     *            - the xml document of the workflow template.
     * @param p_templateName
     *            - The workflow template name
     */
    private void setTemplateName(Document p_document, String p_templateName)
    {
        p_document.addElement(WorkflowConstants.PROCESS_DEFINITION)
                .addAttribute(WorkflowConstants.ATTR_NAME, p_templateName);
    }

    //
    //
    // //TomyD -- This method can not be used since i-Flow does not allow the
    // modification
    // // of a template with an associated process instance.
    // /**
    // * Update an existing workflow template.
    // * <p>
    // * @param p_workflowtemplate - The template to be modified.
    // * @param p_wfSession - The WFSession object used for template ownership.
    // *
    // * @exception Exception - i-Flow related exception.
    // */
    // public static void updateWorkflowTemplate( WorkflowTemplate
    // p_workflowtemplate,
    // WFSession p_wfSession)
    // throws Exception
    // {
    // Plan template = null;
    // try
    // {
    // template = (Plan)WFObjectFactory.getPlan(
    // p_workflowtemplate.getId(), p_wfSession);
    // template.startEdit();
    // template.setName(p_workflowtemplate.getName());
    // template.setDesc(p_workflowtemplate.getDescription());
    //
    // Node[] nodes = template.getNodes();
    // int size = nodes.length;
    // for (int i = 0 ; i < nodes.length ; i++)
    // {
    // //Remove all the arrows associated with this node
    // Arrow[] arrows = nodes[i].getOutgoingArrows();
    // for (int j =0;j<arrows.length;j++)
    // {
    // nodes[i].removeArrow(arrows[j].getArrowId());
    // }
    // template.removeNode(nodes[i].getNodeId());
    // }
    // template.removeAllDataItemRefs();
    // createTemplateNodes(p_workflowtemplate.getWorkflowTasks(), template);
    // createArrows(p_workflowtemplate.getWorkflowTasks(), template);
    // setNodeProperties(p_workflowtemplate.getWorkflowTasks(), template);
    // template.commitEdit();
    // }
    // catch (Exception e)
    // {
    // try
    // {
    // template.cancelEdit();
    // }
    // catch (Exception ex)
    // {
    // }
    //
    // throw e;
    // }
    // }

    // //TomyD -- This method can not be used since i-Flow does not allow the
    // modification
    // // of a template with an associated process instance.
    // /**
    // * Update an existing workflow template.
    // * <p>
    // * @param p_workflowtemplate - The template to be modified.
    // * @param p_wfSession - The WFSession object used for template ownership.
    // *
    // * @exception Exception - i-Flow related exception.
    // */
    // public static void updateWorkflowTemplate( WorkflowTemplate
    // p_workflowtemplate,
    // WFSession p_wfSession)
    // throws Exception
    // {
    // Plan template = null;
    // try
    // {
    // template = (Plan)WFObjectFactory.getPlan(
    // p_workflowtemplate.getId(), p_wfSession);
    // template.startEdit();
    // template.setName(p_workflowtemplate.getName());
    // template.setDesc(p_workflowtemplate.getDescription());
    //
    // Node[] nodes = template.getNodes();
    // int size = nodes.length;
    // for (int i = 0 ; i < nodes.length ; i++)
    // {
    // //Remove all the arrows associated with this node
    // Arrow[] arrows = nodes[i].getOutgoingArrows();
    // for (int j =0;j<arrows.length;j++)
    // {
    // nodes[i].removeArrow(arrows[j].getArrowId());
    // }
    // template.removeNode(nodes[i].getNodeId());
    // }
    // template.removeAllDataItemRefs();
    // createTemplateNodes(p_workflowtemplate.getWorkflowTasks(), template);
    // createArrows(p_workflowtemplate.getWorkflowTasks(), template);
    // setNodeProperties(p_workflowtemplate.getWorkflowTasks(), template);
    // template.commitEdit();
    // }
    // catch (Exception e)
    // {
    // try
    // {
    // template.cancelEdit();
    // }
    // catch (Exception ex)
    // {
    // }
    //
    // throw e;
    // }
    // }

    /**
     * Sets the workflow task node names.
     * 
     * @param p_workflowTasks
     *            - the list of workflow tasks.
     */
    private void setNodeNames(Vector p_workflowTasks)
    {
        int size = p_workflowTasks == null ? 0 : p_workflowTasks.size();

        for (int i = 0; i < size; i++)
        {
            WorkflowTask wfTask = (WorkflowTask) p_workflowTasks.get(i);
            if (wfTask.getStructuralState() != WorkflowConstants.REMOVED)
            {
                if (wfTask.getType() != WorkflowConstants.START
                        && wfTask.getType() != WorkflowConstants.STOP)
                {
                    wfTask.setNodeName(WorkflowJbpmUtil.generateNodeName(
                            wfTask.getActivityName(), i));
                }
                else if (wfTask.getType() == WorkflowConstants.STOP)
                {
                    wfTask.setNodeName(wfTask.getActivityName());
                }
            }
        }
    }

    /**
     * Adds the workflow tasks to the template xml.
     * 
     * @param p_document
     *            - the xml document of the workflow template.
     * @param p_workflowTemplate
     *            - the workflow template.
     * @param p_workflowOwners
     *            - the owner(s) of the workflow instances.
     * @throws Exception
     */
    private void createTemplateNodes(Document p_document,
            WorkflowTemplate p_workflowTemplate, WorkflowOwners p_workflowOwners)
            throws Exception
    {
        Vector workflowTasks = p_workflowTemplate.getWorkflowTasks();
        int size = workflowTasks == null ? 0 : workflowTasks.size();
        Element root = p_document.getRootElement();

        for (int i = 0; i < size; i++)
        {
            WorkflowTask wfTask = (WorkflowTask) workflowTasks.get(i);
            if (wfTask.getStructuralState() != WorkflowConstants.REMOVED)
            {
                switch (wfTask.getType())
                {
                    case WorkflowConstants.START:
                        createStartState(root, wfTask);
                        m_startTask = wfTask;
                        break;
                    case WorkflowConstants.ACTIVITY:
                        createActivityNodes(root, wfTask, p_workflowOwners);
                        break;
                    case WorkflowConstants.CONDITION:
                        createDecisionNodes(root, wfTask);
                        break;
                }
            }
        }
        createEndState(root, workflowTasks, p_workflowTemplate,
                p_workflowOwners);
    }

    /**
     * Creates the end state node in the template xml.
     * 
     * @param p_root
     *            - the root element of the xml document of the workflow
     *            template.
     * @param p_workflowTasks
     *            - the list of workflow tasks.
     * @param p_workflowTemplate
     *            - the workflow template.
     * @param p_workflowOwners
     *            - the owner(s) of the workflow instances.
     */
    private void createEndState(Element p_root, Vector p_workflowTasks,
            WorkflowTemplate p_workflowTemplate, WorkflowOwners p_workflowOwners)
    {
        int size = p_workflowTasks == null ? 0 : p_workflowTasks.size();

        for (int i = 0; i < size; i++)
        {
            WorkflowTask wfTask = (WorkflowTask) p_workflowTasks.get(i);
            if (wfTask.getStructuralState() != WorkflowConstants.REMOVED)
            {
                if (wfTask.getType() == WorkflowConstants.STOP)
                {
                    createEndState(p_root, wfTask, p_workflowTemplate,
                            p_workflowOwners, p_workflowTasks.size());
                }
            }
        }
    }

    /**
     * Creates the Decision nodes in the template xml.
     * 
     * @param p_root
     *            - the root element of the xml document of the workflow
     *            template.
     * @param p_wfTask
     *            - the current workflow task.
     * @throws Exception
     */
    private void createDecisionNodes(Element p_root, WorkflowTask p_wfTask)
            throws Exception
    {
        Element decision = p_root.addElement(WorkflowConstants.DECISION)
                .addAttribute(WorkflowConstants.ATTR_NAME,
                        p_wfTask.getNodeName());
        // add handler element to decision node
        Element handler = decision.addElement(WorkflowConstants.HANDLER)
                .addAttribute(WorkflowConstants.ATTR_CLASS,
                        WorkflowDecision.class.getName());
        // point field
        handler.addElement(WorkflowConstants.FIELD_POINT).addText(
                String.valueOf(p_wfTask.getPosition().getX())
                        + WorkflowConstants.POINT_SEPARATOR
                        + String.valueOf(p_wfTask.getPosition().getY()));
        // sequence field
        handler.addElement(WorkflowConstants.FIELD_SEQUENCE).addText(
                String.valueOf(p_wfTask.getSequence()));
        // workflow-condition-spec field
        WorkflowConditionSpec wfConditionSpec = p_wfTask.getConditionSpec();
        if (wfConditionSpec != null)
        {
            Element wcs = handler
                    .addElement(WorkflowConstants.FIELD_WORKFLOW_CONDITION_SPEC);
            // condition-attribute field
            String attributeName = wfConditionSpec.getConditionAttribute() == null ? ""
                    : wfConditionSpec.getConditionAttribute();
            wcs.addElement(WorkflowConstants.FIELD_CONDITION_ATTRIBUTE)
                    .addText(attributeName);
            // workflow-branch-spec field
            List workflowBranchSpecs = wfConditionSpec.getBranchSpecs();
            if (workflowBranchSpecs != null && !workflowBranchSpecs.isEmpty())
            {
                for (int t = 0; t < workflowBranchSpecs.size(); t++)
                {
                    WorkflowBranchSpec workflowBranchSpec = (WorkflowBranchSpec) workflowBranchSpecs
                            .get(t);
                    // arrow-label field
                    Element wbs = wcs
                            .addElement(WorkflowConstants.FIELD_WORKFLOW_BRANCH_SPEC
                                    + WorkflowConstants.NAME_SEPARATOR + t);
                    wbs.addElement(WorkflowConstants.FIELD_ARROW_LABEL)
                            .addText(workflowBranchSpec.getArrowLabel());
                    // comparison-operator field
                    wbs.addElement(WorkflowConstants.FIELD_COMPARISON_OPERATOR)
                            .addText(
                                    String.valueOf(workflowBranchSpec
                                            .getComparisonOperator()));
                    // structual-state field
                    wbs.addElement(WorkflowConstants.FIELD_STRUCTUAL_STATE)
                            .addText(
                                    String.valueOf(workflowBranchSpec
                                            .getStructuralState()));
                    // is-default field
                    wbs.addElement(WorkflowConstants.FIELD_IS_DEFAULT).addText(
                            String.valueOf(workflowBranchSpec.isDefault()));
                    // branch-value field
                    String branchValue = workflowBranchSpec.getValue() == null ? ""
                            : workflowBranchSpec.getValue();
                    wbs.addElement(WorkflowConstants.FIELD_BRANCH_VALUE)
                            .addText(branchValue);
                }
            }
        }

        Vector outgoingArrows = p_wfTask.getOutgoingArrows();
        for (int j = 0; j < outgoingArrows.size(); j++)
        {
            WorkflowArrow outgoingArrow = (WorkflowArrow) outgoingArrows.get(j);
            if (outgoingArrow.getStructuralState() != WorkflowConstants.REMOVED)
            {
                // transition filed
                decision.addElement(WorkflowConstants.TRANSITION)
                        .addAttribute(WorkflowConstants.ATTR_NAME,
                                outgoingArrow.getName())
                        .addAttribute(WorkflowConstants.ATTR_TO,
                                outgoingArrow.getTargetNode().getNodeName());
            }
        }
        // add controller element to task node
        Element controller = decision.addElement(WorkflowConstants.CONTROLLER);
        controller
                .addElement(WorkflowConstants.VARIABLE)
                .addAttribute(WorkflowConstants.ATTR_NAME,
                        WorkflowConstants.VARIABLE_GOTO)
                .addAttribute(WorkflowConstants.ATTR_ACCESS,
                        WorkflowConstants.VARIABLE_ACCESS_RW);
    }

    /**
     * Creates the activity nodes in the template xml.
     * 
     * @param p_root
     *            - the root element of the xml document of the workflow
     *            template.
     * @param p_wfTask
     *            - the current workflow task.
     * @param p_workflowOwners
     *            - the owner(s) of the workflow instances.
     * @throws Exception
     */
    private void createActivityNodes(Element p_root, WorkflowTask p_wfTask,
            WorkflowOwners p_workflowOwners) throws Exception
    {
        Element taskNode = p_root.addElement(WorkflowConstants.TASK_NODE)
                .addAttribute(WorkflowConstants.ATTR_NAME,
                        p_wfTask.getNodeName());
        // add task element to task-node node
        Element task = taskNode.addElement(WorkflowConstants.TASK)
                .addAttribute(
                        WorkflowConstants.ATTR_NAME,
                        WorkflowJbpmUtil.generateTaskName(p_wfTask
                                .getActivityName()));
        // add assignment element to task node
        Element assignment = task.addElement(WorkflowConstants.ASSIGNMENT)
                .addAttribute(WorkflowConstants.ATTR_CLASS,
                        WorkflowAssignment.class.getName());
        // workflow PM field
        String pm = p_workflowOwners.getProjectManagerId() == null ? ""
                : p_workflowOwners.getProjectManagerId();
        assignment.addElement(WorkflowConstants.FIELD_PM).addText(pm);
        // workflow manager field
        String[] managers = p_workflowOwners.getWorkflowManagerIds();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < managers.length; i++)
        {
            if (i != 0)
            {
                sb.append(",");
            }
            sb.append(managers[i]);
        }
        assignment.addElement(WorkflowConstants.FIELD_MANAGER).addText(
                sb.toString());
        // activity field
        assignment.addElement(WorkflowConstants.FIELD_ACTIVITY).addText(
                p_wfTask.getActivityName());
        // report_upload_check field
        assignment.addElement(WorkflowConstants.FIELD_REPORT_UPLOAD_CHECK).addText(
               String.valueOf(p_wfTask.getReportUploadCheck()));
        // roles field
        assignment.addElement(WorkflowConstants.FIELD_ROLES).addText(
                p_wfTask.getRolesAsString());
        // accepted-time field
        assignment.addElement(WorkflowConstants.FIELD_ACCEPTED_TIME).addText(
                String.valueOf(p_wfTask.getAcceptTime()));
        // completed-time field
        assignment.addElement(WorkflowConstants.FIELD_COMPLETED_TIME).addText(
                String.valueOf(p_wfTask.getCompletedTime()));

        if (p_wfTask.getOverdueToPM() == -1)
        {
            assignment.addElement(WorkflowConstants.FIELD_OVERDUE_PM_TIME)
                    .addText("0");
        }
        else
        {
            assignment.addElement(WorkflowConstants.FIELD_OVERDUE_PM_TIME)
                    .addText(String.valueOf(p_wfTask.getOverdueToPM()));
        }

        if (p_wfTask.getOverdueToUser() == -1)
        {
            assignment.addElement(WorkflowConstants.FIELD_OVERDUE_USER_TIME)
                    .addText("0");
        }
        else
        {
            assignment.addElement(WorkflowConstants.FIELD_OVERDUE_USER_TIME)
                    .addText(String.valueOf(p_wfTask.getOverdueToUser()));
        }

        // role-type field
        assignment.addElement(WorkflowConstants.FIELD_ROLE_TYPE).addText(
                String.valueOf(p_wfTask.getRoleType()));
        // sequence field
        assignment.addElement(WorkflowConstants.FIELD_SEQUENCE).addText(
                String.valueOf(p_wfTask.getSequence()));
        // structual-state field
        assignment.addElement(WorkflowConstants.FIELD_STRUCTUAL_STATE).addText(
                String.valueOf(p_wfTask.getStructuralState()));
        // rate-selection-criteria field
        if (WorkflowAdapterHelper.isCostingEnabled())
        {
            assignment.addElement(
                    WorkflowConstants.FIELD_RATE_SELECTION_CRITERIA).addText(
                    String.valueOf(p_wfTask.getRateSelectionCriteria()));
            // expense-rate-id field
            assignment.addElement(WorkflowConstants.FIELD_EXPENSE_RATE_ID)
                    .addText(String.valueOf(p_wfTask.getExpenseRateId()));
            // revenue-rate-id field
            if (WorkflowAdapterHelper.isRevenueEnabled())
            {
                assignment.addElement(WorkflowConstants.FIELD_REVENUE_RATE_ID)
                        .addText(String.valueOf(p_wfTask.getRevenueRateId()));
            }
        }
        // role-name field
        String roleName = p_wfTask.getDisplayRoleName() == null ? "" : p_wfTask
                .getDisplayRoleName();
        assignment.addElement(WorkflowConstants.FIELD_ROLE_NAME).addText(
                UserUtil.getUserIdsByNames(roleName));
        // role-id field
        String roleId = "0";
        assignment.addElement(WorkflowConstants.FIELD_ROLE_ID).addText(roleId);
        // action-type field
        String actionType = p_wfTask.getActionType() == null ? "" : p_wfTask
                .getActionType();
        assignment.addElement(WorkflowConstants.FIELD_ACTION_TYPE).addText(
                actionType);
        // role-preference field
        String rolePreference = p_wfTask.getRolePreference() == null ? ""
                : p_wfTask.getRolePreference();
        assignment.addElement(WorkflowConstants.FIELD_ROLE_PREFERENCE).addText(
                rolePreference);
        // workflow-data-item field
        WorkflowDataItem[] dataItems = p_wfTask.getDataItemRefs();
        if (dataItems != null && dataItems.length > 0)
        {
            for (int t = 0; t < dataItems.length; t++)
            {
                WorkflowDataItem di = dataItems[t];
                String name = di.getName() == null ? "" : di.getName();
                Element dataItem = assignment
                        .addElement(WorkflowConstants.FIELD_WORKFLOW_DATA_ITEM
                                + WorkflowConstants.NAME_SEPARATOR + t);
                dataItem.addElement(
                        WorkflowConstants.FIELD_WORKFLOW_DATA_ITEM_NAME)
                        .addText(name);
                String type = di.getType() == null ? "" : di.getType();
                dataItem.addElement(
                        WorkflowConstants.FIELD_WORKFLOW_DATA_ITEM_TYPE)
                        .addText(type);
                String value = di.getValue() == null ? "" : di.getValue();
                dataItem.addElement(
                        WorkflowConstants.FIELD_WORKFLOW_DATA_ITEM_VALUE)
                        .addText(value);
            }
        }

        // point field
        assignment.addElement(WorkflowConstants.FIELD_POINT).addText(
                String.valueOf(p_wfTask.getPosition().getX())
                        + WorkflowConstants.POINT_SEPARATOR
                        + String.valueOf(p_wfTask.getPosition().getY()));

        Vector outgoingArrows = p_wfTask.getOutgoingArrows();
        for (int j = 0; j < outgoingArrows.size(); j++)
        {
            WorkflowArrow outgoingArrow = (WorkflowArrow) outgoingArrows.get(j);
            if (outgoingArrow.getStructuralState() != WorkflowConstants.REMOVED)
            {
                // transition filed
                taskNode.addElement(WorkflowConstants.TRANSITION)
                        .addAttribute(WorkflowConstants.ATTR_NAME,
                                outgoingArrow.getName())
                        .addAttribute(WorkflowConstants.ATTR_TO,
                                outgoingArrow.getTargetNode().getNodeName());
            }
        }
        // add controller element to task node
        Element controller = task.addElement(WorkflowConstants.CONTROLLER);
        controller
                .addElement(WorkflowConstants.VARIABLE)
                .addAttribute(WorkflowConstants.ATTR_NAME,
                        WorkflowConstants.VARIABLE_IS_REJECTED)
                .addAttribute(WorkflowConstants.ATTR_ACCESS,
                        WorkflowConstants.VARIABLE_ACCESS_RW);
        controller
                .addElement(WorkflowConstants.VARIABLE)
                .addAttribute(WorkflowConstants.ATTR_NAME,
                        WorkflowConstants.VARIABLE_USER_ID)
                .addAttribute(WorkflowConstants.ATTR_ACCESS,
                        WorkflowConstants.VARIABLE_ACCESS_RW);
        controller
                .addElement(WorkflowConstants.VARIABLE)
                .addAttribute(WorkflowConstants.ATTR_NAME,
                        WorkflowConstants.VARIABLE_GOTO)
                .addAttribute(WorkflowConstants.ATTR_ACCESS,
                        WorkflowConstants.VARIABLE_ACCESS_RW);
    }

    /**
     * Creates the end state node in the template xml.
     * 
     * @param p_root
     *            - the root element of the xml document of the workflow
     *            template.
     * @param p_wfTask
     *            - the current workflow task.
     * @param p_workflowTemplate
     *            - the workflow template.
     * @param p_workflowOwners
     *            - the owner(s) of the workflow instances.
     * @param p_max
     *            the max node id
     */
    private void createEndState(Element p_root, WorkflowTask p_wfTask,
            WorkflowTemplate p_workflowTemplate,
            WorkflowOwners p_workflowOwners, int p_max)
    {
        Element endState = p_root.addElement(WorkflowConstants.END_STATE)
                .addAttribute(WorkflowConstants.ATTR_NAME,
                        p_wfTask.getActivityName());
        // add action element to end-state node
        Element action = endState.addElement(WorkflowConstants.ACTION)
                .addAttribute(WorkflowConstants.ATTR_CLASS,
                        WorkflowAction.class.getName());
        // start-state field
        Element startState = action
                .addElement(WorkflowConstants.FIELD_START_STATE);
        setPropertiesForStartState(startState, p_workflowTemplate,
                p_workflowOwners);
        // point field
        action.addElement(WorkflowConstants.FIELD_POINT).addText(
                String.valueOf(p_wfTask.getPosition().getX())
                        + WorkflowConstants.POINT_SEPARATOR
                        + String.valueOf(p_wfTask.getPosition().getY()));
        // sequence field
        action.addElement(WorkflowConstants.FIELD_SEQUENCE).addText(
                String.valueOf(p_wfTask.getSequence()));
        // structual-state field
        action.addElement(WorkflowConstants.FIELD_STRUCTUAL_STATE).addText(
                String.valueOf(p_wfTask.getStructuralState()));
        // max-node-id field
        action.addElement(WorkflowConstants.FIELD_MAX_NODE_ID).addText(
                String.valueOf(p_max));

    }

    /**
     * Sets the properties for start state in the end state node.
     * 
     * @param p_startState
     *            - the start state node in the action element in the end state
     *            node.
     * @param p_workflowTemplate
     *            - the workflow template.
     * @param p_worklfowOwners
     *            - The owner(s) of the workflow instances.
     */
    private void setPropertiesForStartState(Element p_startState,
            WorkflowTemplate p_workflowTemplate, WorkflowOwners p_workflowOwners)
    {
        // workflow description field
        p_startState.addElement(WorkflowConstants.FIELD_DESCRIPTION).addText(
                p_workflowTemplate.getDescription());
        // workflow PM field
        String pm = p_workflowOwners.getProjectManagerId() == null ? ""
                : p_workflowOwners.getProjectManagerId();
        p_startState.addElement(WorkflowConstants.FIELD_PM).addText(pm);
        // workflow manager field
        String[] managers = p_workflowOwners.getWorkflowManagerIds();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < managers.length; i++)
        {
            if (i != 0)
            {
                sb.append(",");
            }
            sb.append(managers[i]);
        }
        p_startState.addElement(WorkflowConstants.FIELD_MANAGER).addText(
                sb.toString());
        // point field
        p_startState.addElement(WorkflowConstants.FIELD_POINT).addText(
                String.valueOf(m_startTask.getPosition().getX())
                        + WorkflowConstants.POINT_SEPARATOR
                        + String.valueOf(m_startTask.getPosition().getY()));
        // sequence field
        p_startState.addElement(WorkflowConstants.FIELD_SEQUENCE).addText(
                String.valueOf(m_startTask.getSequence()));
        // structual-state field
        p_startState.addElement(WorkflowConstants.FIELD_STRUCTUAL_STATE)
                .addText(String.valueOf(m_startTask.getStructuralState()));

    }

    /**
     * Creates the start state node in the template xml.
     * 
     * @param p_root
     *            - the root element of the xml document of the workflow
     *            template.
     * @param p_wfTask
     *            - the current workflow task.
     */
    private void createStartState(Element p_root, WorkflowTask p_wfTask)
    {
        Element startState = p_root.addElement(WorkflowConstants.START_STATE)
                .addAttribute(WorkflowConstants.ATTR_NAME,
                        WorkflowConstants.START_NODE);

        Vector outgoingArrows = p_wfTask.getOutgoingArrows();
        for (int i = 0; i < outgoingArrows.size(); i++)
        {
            WorkflowArrow outgoingArrow = (WorkflowArrow) outgoingArrows.get(i);
            if (outgoingArrow.getStructuralState() != WorkflowConstants.REMOVED)
            {
                // transition field
                startState
                        .addElement(WorkflowConstants.TRANSITION)
                        .addAttribute(WorkflowConstants.ATTR_NAME,
                                outgoingArrow.getName())
                        .addAttribute(WorkflowConstants.ATTR_TO,
                                outgoingArrow.getTargetNode().getNodeName());
            }
        }
    }

    /*
     * It also sets the properties for all other nodes presented in the UI
     */

    /*
     * Adds properties to the task instances
     */

    private void addPropertiesToTemplateTask(List p_wfTasks, List p_nodeList,
            Map p_map) throws Exception
    {

        for (Iterator it = p_nodeList.iterator(); it.hasNext();)
        {
            Node node = (Node) it.next();
            WorkflowTask wfTask = WorkflowJbpmUtil.getTaskById(p_wfTasks,
                    node.getId());

            switch (wfTask.getType())
            {
                case WorkflowConstants.ACTIVITY:
                    addActivityNodeProperties(p_map, wfTask, node);
                    break;
                case WorkflowConstants.CONDITION:
                    addConditionNodeProperties(wfTask, node, p_map);
                    break;

                case WorkflowConstants.OR:
                case WorkflowConstants.AND:
                    /* the and/or node will not be supported in current system. */
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Adds the required properties of the activity node.
     * 
     * @param p_map
     *            The map contains the {@code WorkflowNodeParameter}.
     * @param p_task
     *            The {@code WorkflowTaskInstance}.
     * @param p_node
     *            The {@code Node}.
     * @throws Exception
     */
    private void addActivityNodeProperties(Map p_map, WorkflowTask p_task,
            Node p_node) throws Exception
    {

        String nodeName = p_node.getName();
        /* Gets the nodeParameter */
        WorkflowNodeParameter nodeParameter = (WorkflowNodeParameter) p_map
                .get(nodeName);

        Activity activity = ServerProxy.getJobHandler().getActivity(
                nodeParameter.getAttribute(WorkflowConstants.FIELD_ACTIVITY));
        // sequence of the node
        int sequence = WorkflowAdapterHelper.parseInt(p_task.getDesc(), -9);

        /*
         * the role type represents select all or specied user in activity
         * properties
         */
        boolean isUserRole = nodeParameter
                .getBooleanAttribute(WorkflowConstants.FIELD_ROLE_TYPE);

        if (WorkflowAdapterHelper.isCostingEnabled())
        {
            // expense rate selection criteria
            int rateSelectionCriteria = nodeParameter.getIntAttribute(
                    WorkflowConstants.FIELD_RATE_SELECTION_CRITERIA,
                    WorkflowConstants.USE_ONLY_SELECTED_RATE);
            // expense rate id
            long expenseRateId = nodeParameter.getLongAttribute(
                    WorkflowConstants.FIELD_EXPENSE_RATE_ID,
                    WorkflowTaskInstance.NO_RATE);

            p_task.setRateSelectionCriteria(rateSelectionCriteria);
            p_task.setExpenseRateId(expenseRateId);

            if (WorkflowAdapterHelper.isRevenueEnabled())
            {
                // revenue rate id
                long revenueRateId = nodeParameter.getLongAttribute(
                        WorkflowConstants.FIELD_REVENUE_RATE_ID,
                        WorkflowTaskInstance.NO_RATE);
                p_task.setRevenueRateId(revenueRateId);
            }
        }

        long timeToAccept = nodeParameter.getLongAttribute(
                WorkflowConstants.FIELD_ACCEPTED_TIME,
                WorkflowTaskInstance.NO_RATE);

        long timeToComplete = nodeParameter.getLongAttribute(
                WorkflowConstants.FIELD_COMPLETED_TIME,
                WorkflowTaskInstance.NO_RATE);

        long overduePM = nodeParameter.getLongAttribute(
                WorkflowConstants.FIELD_OVERDUE_PM_TIME,
                WorkflowTaskInstance.NO_RATE);

        long overdueUser = nodeParameter.getLongAttribute(
                WorkflowConstants.FIELD_OVERDUE_USER_TIME,
                WorkflowTaskInstance.NO_RATE);

        String displayRoleName = nodeParameter.getAttribute(
                WorkflowConstants.FIELD_ROLE_NAME,
                WorkflowTaskInstance.DEFAULT_ROLE_NAME);

        // system action type
        String actionType = nodeParameter.getAttribute(
                WorkflowConstants.FIELD_ACTION_TYPE,
                WorkflowTaskInstance.NO_ACTION);
        
        int reportUploadCheck = nodeParameter.getIntAttribute(
                WorkflowConstants.FIELD_REPORT_UPLOAD_CHECK,
                WorkflowConstants.REPORT_UPLOAD_CHECK);

        String rolePreference = nodeParameter
                .getAttribute(WorkflowConstants.FIELD_ROLE_PREFERENCE);

        String[] roles = nodeParameter
                .getArrayAttribute(WorkflowConstants.FIELD_ROLES);

        // Role Preference
        p_task.setRolePreference(rolePreference);
        p_task.setActionType(actionType);
        p_task.setActivity(activity);
        p_task.setSequence(sequence);
        p_task.setRoleType(isUserRole);
        p_task.setRoles(roles);
        p_task.setAcceptedTime(timeToAccept);
        p_task.setCompletedTime(timeToComplete);
        p_task.setOverdueToPM(overduePM);
        p_task.setOverdueToUser(overdueUser);
        p_task.setReportUploadCheck(reportUploadCheck);
        p_task.setDisplayRoleName(UserUtil.getUserNamesByIds(displayRoleName));
    }

    //
    //

    /*
     * Add the condition spec properties to the workflowTask
     */

    /*
     * Add condition properties to the condition node instance
     */
    private void addConditionNodeProperties(WorkflowTask p_WfTask, Node p_node,
            Map p_map) throws Exception
    {
        WorkflowNodeParameter nodeParameter = ((WorkflowNodeParameter) p_map
                .get(p_node.getName()))
                .getsubNodeParameter(WorkflowConstants.FIELD_WORKFLOW_CONDITION_SPEC);

        WorkflowConditionSpec p_wfCondSpec = p_WfTask.getConditionSpec();

        p_wfCondSpec.setConditionAttribute(nodeParameter
                .getAttribute(WorkflowConstants.FIELD_CONDITION_ATTRIBUTE));

        List<WorkflowBranchSpec> m_workflowBranchSpecs = new ArrayList<WorkflowBranchSpec>();
        List transitionList = p_node.getLeavingTransitions();

        for (int i = 0; i < transitionList.size(); i++)
        {
            String name = WorkflowConstants.FIELD_WORKFLOW_BRANCH_SPEC
                    + WorkflowConstants.NAME_SEPARATOR + i;
            WorkflowNodeParameter subNodeParameter = nodeParameter
                    .getsubNodeParameter(name);
            WorkflowBranchSpec p_workflowBranchSpec = p_wfCondSpec
                    .setCondBranchSpecInfo(
                            subNodeParameter
                                    .getAttribute(WorkflowConstants.FIELD_ARROW_LABEL),
                            subNodeParameter
                                    .getIntAttribute(WorkflowConstants.FIELD_COMPARISON_OPERATOR),
                            subNodeParameter
                                    .getAttribute(WorkflowConstants.FIELD_BRANCH_VALUE),
                            subNodeParameter
                                    .getBooleanAttribute(WorkflowConstants.FIELD_IS_DEFAULT));
            m_workflowBranchSpecs.add(p_workflowBranchSpec);
        }
        p_wfCondSpec.setEvalOrder(m_workflowBranchSpecs);
        p_WfTask.setConditionSpec(p_wfCondSpec);
    }

    /**
     * Creates the workflow instance.
     * 
     * @param p_wfInst
     *            The workflow instance.
     * @param p_nodeList
     *            The node list.
     * @param p_map
     *            The map of the {@code WorkfowNodeParameter}
     * @param p_taskList
     *            The node instance task list.
     */
    private void createWorkflowTask(WorkflowTemplate wfTemplate,
            List p_nodeList, Map<String, WorkflowNodeParameter> p_map,
            List<WorkflowTask> p_taskList)
    {

        /*
         * because Jbpm cannot store the data in the startstate, we put the
         * content in the endstate to store the startstate related parameter.
         */
        /* record the parameter in the endstate for startstate */
        WorkflowNodeParameter startNodeParameter = null;
        /* record the task of the statrt state */
        WorkflowTask starttask = null;

        for (Iterator it = p_nodeList.iterator(); it.hasNext();)
        {
            Node node = (Node) it.next();

            WorkflowNodeParameter workflowNodeParameter = WorkflowNodeParameter
                    .createInstance(WorkflowJbpmUtil.getConfigure(node));

            if (c_category.isDebugEnabled())
            {
                c_category.debug("The content of the node "
                        + WorkflowJbpmUtil.getTaskName(node) + " is "
                        + WorkflowJbpmUtil.getConfigure(node));
            }

            /* build the task */
            WorkflowTask wfTaskInst = wfTemplate.addWorkflowTask(
                    WorkflowJbpmUtil.getActivityName(node),
                    WorkflowJbpmUtil.getNodeType(node));
            wfTaskInst.setNodeName(node.getName());
            wfTaskInst.setTaskId(node.getId());

            if (StringUtil.isEmpty(WorkflowJbpmUtil.getConfigure(node)))
            {
                /* the node is startstate */
                p_taskList.add(wfTaskInst);
                starttask = wfTaskInst;
                continue;
            }

            wfTaskInst.setDesc(workflowNodeParameter
                    .getAttribute(WorkflowConstants.FIELD_SEQUENCE));
            wfTaskInst.setPosition(workflowNodeParameter
                    .getPointAttribute(WorkflowConstants.FIELD_POINT));

            /* add the task to the list */
            p_taskList.add(wfTaskInst);
            /* add the parameter to the map */
            p_map.put(node.getName(), workflowNodeParameter);

            if (wfTaskInst.getType() == WorkflowConstants.STOP)
            {
                /* The node is end state */
                startNodeParameter = workflowNodeParameter
                        .getsubNodeParameter(WorkflowConstants.FIELD_START_STATE);
            }

        }

        /* set the parameter for the start node */
        starttask.setDesc(startNodeParameter
                .getAttribute(WorkflowConstants.FIELD_SEQUENCE));
        starttask.setPosition(startNodeParameter
                .getPointAttribute(WorkflowConstants.FIELD_POINT));
        p_map.put(WorkflowConstants.START_NODE, startNodeParameter);

    }

    /*
     * This function adds all the incoming arrows and outgoing arrows to the
     * nodeInstances
     */
    private void addArrowToTask(List p_nodeList, List p_wfTaskInstances,
            WorkflowTemplate wfTemplate, Map p_map) throws Exception
    {
        for (Iterator it = p_nodeList.iterator(); it.hasNext();)
        {
            /* iterator the node */
            Node node = (Node) it.next();

            List transitionList = node.getLeavingTransitions();
            if (transitionList == null)
                continue;

            for (int i = 0; i < transitionList.size(); i++)
            {
                /* iterator the leaving transition */
                Transition transition = (Transition) transitionList.get(i);
                Node sourceNode = transition.getFrom();
                Node targetNode = transition.getTo();
                WorkflowTask wfSourceNode = WorkflowJbpmUtil.getTaskById(
                        p_wfTaskInstances, sourceNode.getId());
                WorkflowTask wfTargetNode = WorkflowJbpmUtil.getTaskById(
                        p_wfTaskInstances, targetNode.getId());

                WorkflowArrow p_outgoingArrow = wfTemplate.addArrow(
                        transition.getName(), WorkflowConstants.REGULAR_ARROW,
                        wfSourceNode, wfTargetNode);

                WorkflowNodeParameter sourceParameter = (WorkflowNodeParameter) p_map
                        .get(sourceNode.getName());
                WorkflowNodeParameter targetParameter = (WorkflowNodeParameter) p_map
                        .get(targetNode.getName());

                setPropertiesForArrow(p_outgoingArrow, sourceParameter,
                        targetParameter, i);

            }

        }

    }

    /**
     * Sets the properties for the arrow.
     * 
     * @param p_outgoingArrow
     *            The arrow.
     * @param p_sourceParameter
     *            The parameter object for the source node.
     * @param p_targetParameter
     *            The parameter object for the target node.
     * @param p_arrowNumber
     *            The number of the arrow.
     */
    private void setPropertiesForArrow(WorkflowArrow p_outgoingArrow,
            WorkflowNodeParameter p_sourceParameter,
            WorkflowNodeParameter p_targetParameter, int p_arrowNumber)
    {
        p_outgoingArrow.setStartPoint(new Point(0, 0));
        p_outgoingArrow.setEndPoint(new Point(0, 0));
        p_outgoingArrow.setPoints(new Point[0]);
    }

    /**
     * Adds the common properties to the {@code WorkflowTempalte}
     * 
     * @param wfTemplate
     * @param map
     * @param processDefinition
     */
    private void addCommonProperties(WorkflowTemplate wfTemplate, Map map,
            ProcessDefinition processDefinition)
    {
        wfTemplate.setId(processDefinition.getId());
        wfTemplate.setName(processDefinition.getName());
        /* The description was set in the start node */
        WorkflowNodeParameter np = (WorkflowNodeParameter) map
                .get(processDefinition.getStartState().getName());
        wfTemplate.setDescription(np.getAttribute(
                WorkflowConstants.FIELD_DESCRIPTION, StringUtil.EMPTY_STRING));

    }
    // //add arrow to TemplateTask
    // private static void addArrowToTemplateTask(WorkflowTask p_WfTask,
    // Node node,
    // Vector p_workflowtasks,
    // WorkflowTemplate p_workFlowTemplate)
    // {
    //
    // Arrow[] outgoingArrows = node.getOutgoingArrows();
    // for (int i=0;i<outgoingArrows.length;i++)
    // {
    // Node sourceNode = outgoingArrows[i].getSourceNode();
    // Node targetNode = outgoingArrows[i].getTargetNode();
    // WorkflowTask wfSourceNode = null ;
    // WorkflowTask wfTargetNode = null ;
    //
    // int size = p_workflowtasks == null ?
    // 0 :
    // p_workflowtasks.size();
    //
    // for (int j = 0 ; j < size ; j++)
    // {
    // WorkflowTask task = (WorkflowTask)p_workflowtasks.get(j) ;
    // if (task.getTaskId() == sourceNode.getNodeId())
    // {
    // wfSourceNode = task;
    // break;
    // }
    // }
    // for (int j = 0 ; j < size ; j++)
    // {
    // WorkflowTask task = (WorkflowTask)p_workflowtasks.get(j) ;
    // if (task.getTaskId() == targetNode.getNodeId())
    // {
    // wfTargetNode = task;
    // break;
    // }
    // }
    // WorkflowArrow outgoingArrow = p_workFlowTemplate.addArrow(
    // outgoingArrows[i].getName(),outgoingArrows[i].getType(),
    // wfSourceNode,wfTargetNode);
    // outgoingArrow.setStartPoint(outgoingArrows[i].getStartPoint());
    // outgoingArrow.setArrowId(outgoingArrows[i].getArrowId());
    // outgoingArrow.setEndPoint(outgoingArrows[i].getEndPoint());
    // outgoingArrow.setPoints (outgoingArrows[i].getPoints());
    // }
    // }
    //
    //
    // /*
    // It also sets the properties for all other nodes presented in the UI
    // */
    // private static void addPropertiesToTemplateTask(Plan p_plan,
    // WorkflowTask p_WfTask,
    // Node p_node)
    // throws Exception
    // {
    // p_WfTask.setPosition(p_node.getPosition());
    //
    // switch (p_WfTask.getType())
    // {
    // case WorkflowConstants.ACTIVITY :
    // createActivityWorkflowTask(p_plan, p_WfTask,p_node);
    //
    // break;
    // case WorkflowConstants.CONDITION:
    // createConditionWorkFlowTaskProp(p_WfTask, p_node);
    // //Condition node supports both the scripts
    // p_WfTask.setPrologueScript(p_node.getPrologueScript());
    // p_WfTask.setEpilogueScript(p_node.getEpilogueScript());
    // break;
    //
    // case WorkflowConstants.OR:
    // case WorkflowConstants.AND:
    // p_WfTask.setEpilogueScript(p_node.getEpilogueScript());
    // break;
    // }
    // }
    //
    //
    // /**
    // * Get the workflow task object for the given node and plan.
    // * @param return a workflow task.
    // */
    // private static void createActivityWorkflowTask(Plan p_workflow,
    // WorkflowTask p_task,
    // Node p_node)
    // throws Exception
    // {
    // //node name
    // String nodeName = p_node.getName();
    // //sequence of the node
    // int sequence = WorkflowAdapterHelper.parseInt(p_node.getDesc(), -9);
    //
    // /*
    // * NOTE: we're using the form in iflow to store the roleType variable.
    // */
    // boolean isUserRole = WorkflowConstants.USER_ROLE.equals(
    // WorkflowInstanceHelper.getRoleType(p_node));
    // // Rate selection criteria
    // int rateSelectionCriteria = WorkflowConstants.USE_ONLY_SELECTED_RATE;
    // // expense rate id
    // long expenseRateId = WorkflowTaskInstance.NO_RATE;
    // // revenue rate id
    // long revenueRateId = WorkflowTaskInstance.NO_RATE;
    //
    // if(WorkflowAdapterHelper.isCostingEnabled())
    // {
    // DataItemRef dir = WorkflowAdapterHelper.getDataItemRef(
    // p_workflow, WorkflowConstants.PREFIX_RATE_SELECTION_CRITERIA_NAME +
    // sequence);
    // if (dir != null)
    // {
    // rateSelectionCriteria = WorkflowAdapterHelper.parseInt(
    // dir.getValue(), rateSelectionCriteria);
    // }
    //
    // dir = WorkflowAdapterHelper.getDataItemRef(
    // p_workflow, WorkflowConstants.PREFIX_RATE_NAME + sequence);
    // if (dir != null)
    // {
    // expenseRateId = WorkflowAdapterHelper.parseLong(
    // dir.getValue(), expenseRateId);
    // }
    //
    // if(WorkflowAdapterHelper.isCostingEnabled())
    // {
    // dir = WorkflowAdapterHelper.getDataItemRef(
    // p_workflow, WorkflowConstants.PREFIX_REVENUE_RATE_NAME + sequence);
    // if (dir != null)
    // {
    // revenueRateId = WorkflowAdapterHelper.parseLong(
    // dir.getValue(), revenueRateId);
    // }
    // }
    // }
    //
    // long timeToAccept = WorkflowTaskInstance.NO_RATE;;
    // DataItemRef acceptTime = WorkflowAdapterHelper.getDataItemRef(
    // p_workflow, WorkflowConstants.ACCEPT + "_" +sequence);
    //
    // if (acceptTime != null)
    // {
    // timeToAccept = WorkflowAdapterHelper.parseLong(
    // acceptTime.getValue(), timeToAccept);
    // }
    //
    // long timeToComplete=WorkflowTaskInstance.NO_RATE;
    // DataItemRef completeTime = WorkflowAdapterHelper.getDataItemRef(
    // p_workflow, WorkflowConstants.COMPLETE + "_" + sequence);
    // if (completeTime != null)
    // {
    // timeToComplete = WorkflowAdapterHelper.parseLong(
    // completeTime.getValue(), timeToComplete);
    // }
    // String displayRoleName = "";
    // DataItemRef role_name = WorkflowAdapterHelper.getDataItemRef(
    // p_workflow, WorkflowConstants.PREFIX_ROLE_NAME + sequence);
    // if (role_name != null)
    // {
    // displayRoleName = role_name.getValue();
    // }
    //
    // // set the system aciton type
    // DataItemRef actionType = WorkflowAdapterHelper.getDataItemRef(
    // p_workflow, WorkflowConstants.CSTF_ACTION + sequence);
    // if (actionType != null)
    // {
    // p_task.setActionType(actionType.getValue());
    // }
    //
    // // set the role preference
    // DataItemRef rolePreference = WorkflowAdapterHelper.getDataItemRef(
    // p_workflow, WorkflowConstants.ROLE_PREFERENCE + sequence);
    // if (rolePreference != null)
    // {
    // p_task.setRolePreference(rolePreference.getValue());
    // }
    //
    // Activity activity = ServerProxy.getJobHandler().getActivity(nodeName);
    // p_task.setActivity(activity);
    // p_task.setTaskId(p_node.getNodeId());
    // p_task.setSequence(sequence);
    // p_task.setRoles(getRoles(p_workflow, sequence));
    // p_task.setRoleType(isUserRole);
    // if(WorkflowAdapterHelper.isCostingEnabled())
    // {
    // p_task.setRateSelectionCriteria(rateSelectionCriteria);
    // p_task.setExpenseRateId(expenseRateId);
    // if(WorkflowAdapterHelper.isCostingEnabled())
    // {
    // p_task.setRevenueRateId(revenueRateId);
    // }
    // }
    // p_task.setAcceptedTime(timeToAccept);
    // p_task.setCompletedTime(timeToComplete);
    // p_task.setDisplayRoleName(displayRoleName);
    // }
    //
    //
    // /*
    // Add the condition spec properties to the workflowTask
    // */
    // private static void createConditionWorkFlowTaskProp(WorkflowTask
    // p_WfTask,Node p_node)
    // throws Exception
    // {
    // ConditionSpec condSpec= p_node.getConditionSpec();
    // WorkflowConditionSpec p_wfCondSpec= p_WfTask.getConditionSpec();
    //
    // p_wfCondSpec.setConditionAttribute(condSpec.getConditionAttribute());
    //
    // BranchSpec[] branchSpecs = condSpec.getBranchSpecs();
    // List m_workflowBranchSpecs = new ArrayList();
    // for (int j=0;j<branchSpecs.length;j++)
    // {
    // WorkflowBranchSpec p_workflowBranchSpec=
    // p_wfCondSpec.setCondBranchSpecInfo(
    // branchSpecs[j].getArrowLabel(),
    // branchSpecs[j].getComparisonOperator(),
    // branchSpecs[j].getValue(),
    // branchSpecs[j].isDefault());
    // m_workflowBranchSpecs.add(p_workflowBranchSpec);
    // }
    // p_wfCondSpec.setEvalOrder(m_workflowBranchSpecs);
    //
    // p_WfTask.setConditionSpec(p_wfCondSpec);
    // }
}
