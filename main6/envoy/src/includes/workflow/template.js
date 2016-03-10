<script type="text/x-mustache" id="allTemplate"><?xml version="1.0" encoding="UTF-8"?>
<process-definition name="{{name}}">{{{all}}}
</process-definition></script>


<script type="text/x-mustache" id="startTemplate">
<start-state name="Start">
<transition name="{{transition}}" to="{{transition_to}}" />
</start-state></script>
 
 <script type="text/x-mustache" id="taskNodeTemplate">
 <task-node name="node_{{sequence}}_{{activity}}">
	<task name="task_{{activity}}">
		<assignment class="com.globalsight.everest.workflow.WorkflowAssignment">
			<workflow_pm>{{workflow_pm}}</workflow_pm>
			<workflow_manager>{{workflow_manager}}</workflow_manager>
			<activity>{{activity}}</activity>
			<report_upload_check>{{report_upload_check}}</report_upload_check>
			<roles>{{roles}}</roles>
			<accepted_time>{{accepted_time}}</accepted_time>
			<completed_time>{{completed_time}}</completed_time>
			<overdueToPM_time>{{overdueToPM_time}}</overdueToPM_time>
			<overdueToUser_time>{{overdueToUser_time}}</overdueToUser_time>
			<role_type>{{role_type}}</role_type>
			<sequence>{{sequence}}</sequence>
			<structural_state>-1</structural_state>
			<rate_selection_criteria>{{rate_selection_criteria}}</rate_selection_criteria>
			<expense_rate_id>{{expense_rate_id}}</expense_rate_id>
			<revenue_rate_id>{{revenue_rate_id}}</revenue_rate_id>
			<role_name>{{role_name}}</role_name>
			<role_id>0</role_id>
			<action_type>{{action_type}}</action_type>
			<role_preference>{{role_preference}}</role_preference>
			<point>{{x}}:{{y}}</point>
		</assignment>
		<controller>
			<variable name="isRejected" access="read,write" />
			<variable name="userId" access="read,write" />
			<variable name="goTo" access="read,write" />
		</controller>
	</task>
	<transition name="{{transition}}" to="{{transition_to}}" />
</task-node></script>

<script type="text/x-mustache" id="branchTemplate">
			<workflow_branch_spec_{{i}}>
				<arrow_label>{{transition}}</arrow_label>
				<comparison_operator>0</comparison_operator>
				<structural_state>-1</structural_state>
				<is_default>{{isDefault}}</is_default>
				<branch_value>0</branch_value>
			</workflow_branch_spec_{{i}}></script>

<script type="text/x-mustache" id="transitionTemplate">
	<transition name="{{transition}}" to="{{transition_to}}" /></script>

 
<script type="text/x-mustache" id="decisionTemplate">
  <decision name="{{decision}}">
	<handler class="com.globalsight.everest.workflow.WorkflowDecision">
		<point>{{x}}:{{y}}</point>
		<sequence>{{sequence}}</sequence>
		<workflow_condition_spec>
			<condition_attribute></condition_attribute>{{{branchs}}}
		</workflow_condition_spec>
	</handler>{{{transitions}}}
	<controller>
		<variable name="goTo" access="read,write" />
	</controller>
</decision></script>

<script type="text/x-mustache" id="endTemplate">
<end-state name="Exit">
<action class="com.globalsight.everest.workflow.WorkflowAction">
	<start_node>
		<workflow_description></workflow_description>
		<workflow_pm>{{workflow_pm}}</workflow_pm>
		<workflow_manager>{{workflow_manager}}</workflow_manager>
		<point>{{x1}}:{{y1}}</point>
		<sequence>0</sequence>
		<structural_state>-1</structural_state>
	</start_node>
	<point>{{x2}}:{{y2}}</point>
	<sequence>{{sequence2}}</sequence>
	<structural_state>-1</structural_state>
	<max_node_id>{{max_node_id}}</max_node_id>
</action>
</end-state></script>