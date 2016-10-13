/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.workflow;

import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.node.DecisionHandler;

public class WorkflowDecision implements DecisionHandler
{
    private static final long serialVersionUID = 1L;

    private String point;

    private String sequence;

    private String workflow_condition_spec;

    public String decide(ExecutionContext arg0) throws Exception
    {
        Object variable = arg0.getContextInstance().getVariable(
                WorkflowConstants.VARIABLE_GOTO);
        
        String go;
        if (variable == null || WorkflowConstants.VARIABLE_GOTO_SKIP.equals(variable))
        {
            go = refreshGo(arg0);
        }
        else
        {
            go = variable.toString();
        }

        /* update the default path */
        updateDefaultPath(arg0, go);

        return go;
    }

    private String refreshGo(ExecutionContext arg0)
    {

        Node node = arg0.getNode();
        WorkflowNodeParameter np = WorkflowNodeParameter.createInstance(node)
                .subNodeparameter(
                        WorkflowConstants.FIELD_WORKFLOW_CONDITION_SPEC);
        WorkflowNodeParameter subNp = null;
        int i = 0;

        while (true)
        {
            String value = WorkflowConstants.FIELD_WORKFLOW_BRANCH_SPEC + "_"
                    + i++;

            subNp = np.subNodeparameterDefaultNull(value);

            if (subNp.getBooleanAttribute(WorkflowConstants.FIELD_IS_DEFAULT))
            {
                return subNp.getAttribute(WorkflowConstants.FIELD_ARROW_LABEL);
            }

        }

    }

    /**
     * Updates the default arrow value for each arrow. <br>
     * In jbpm implementation, we cannot decides which arrow the workflow will
     * go throug without the name of arrow lable name. <br>
     * To solve this problem, update the arrow label value each time when the
     * user select the specified path.
     * 
     * @param arg0
     * @param go
     * @throws Exception
     */
    private void updateDefaultPath(ExecutionContext arg0, String go)
            throws Exception
    {

        Node node = arg0.getNode();

        WorkflowNodeParameter sumPara = WorkflowNodeParameter
                .createInstance(node);
        WorkflowNodeParameter nodeParameter = sumPara
                .subNodeparameter(WorkflowConstants.FIELD_WORKFLOW_CONDITION_SPEC);
        WorkflowNodeParameter arrowParameter;

        int i = 0;
        while (true)
        {
            String value = WorkflowConstants.FIELD_WORKFLOW_BRANCH_SPEC + "_"
                    + i;

            arrowParameter = nodeParameter.subNodeparameterDefaultNull(value);

            if (arrowParameter == null)
            {
                /* The element doesn't exist ,exit the loop */
                break;
            }

            if (go != null
                    && go.equals(arrowParameter
                            .getAttribute(WorkflowConstants.FIELD_ARROW_LABEL)))
            {
                /* Set the user selected arrow to default path */
                arrowParameter.setAttribute(WorkflowConstants.FIELD_IS_DEFAULT,
                        Boolean.TRUE.toString());
            }
            else
            {
                /* reset the arrow to nondefault path */
                arrowParameter.setAttribute(WorkflowConstants.FIELD_IS_DEFAULT,
                        Boolean.FALSE.toString());
            }

            i++;

        }

        WorkflowJbpmUtil.setConfigure(node, sumPara.restore());

    }

    public String getPoint()
    {
        return point;
    }

    public void setPoint(String point)
    {
        this.point = point;
    }

    public String getSequence()
    {
        return sequence;
    }

    public void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

    public String getWorkflow_condition_spec()
    {
        return workflow_condition_spec;
    }

    public void setWorkflow_condition_spec(String workflow_condition_spec)
    {
        this.workflow_condition_spec = workflow_condition_spec;
    }
}
