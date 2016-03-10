var roleMap={};

var Dialog = {
	editNode : null,
	init : function() {
		initActivityType();
		initSystemActivity();
		
		UI.dialogUserSelect.change(function() {
			var n = UI.dialogUserSelect.val();
			if (n == 1) {
				UI.dialogUserTable.show();
			} else {
				UI.dialogUserTable.hide();
			}
		});
		
		UI.selectAllUserCheckbox.change(function() {
			UI.dialogUserTable.find(".userCheckbox").attr("checked", this.checked);
			
		});
		
		$("#activityTypeSelect").change(function() {
			 var activity = $("#activityTypeSelect").val();
			 Dialog.updateUserTable(activity);
			 Dialog.updateRate(activity);
			 
			 $("#dialogUserSelect option[value='0']").attr("selected", true);
		     UI.dialogUserTable.hide();
		});
	},
	updateUserTable : function(activity, roleName) {
		$("#userTable tr:not(:first)").remove();
		if (activity == -1) {
			return;
		}
		
		var users = getUserForActivity(activity);
		var n = 1;
 		for (var i in users) {
 			n = n+1;
			var user = users[i];
			roleMap[user[2]] = user;
			
			var newRow = '<tr><td><input type="checkbox" class="userCheckbox" value="' + user[2] + '"></td><td>' + user[0] + '</td><td>'+user[1]+'</td><td>'+user[2]+'</td></tr>';
			 $("#userTable tr:last").after(newRow);
		}
 		
 		if (n > 4)
 			n = 4;
		
		$("#userTable tr:odd").addClass("tableRowEvenTM");
		$("#userTable tr:even").addClass("tableRowOddTM");
		$("#userTable tr:first").removeClass("tableRowOddTM").addClass("tableHeadingBasicTM");
		
		$("#dialogUserTable").css("height", n* 32 + "px");
		
		if (typeof(roleName) != "undefined"){
			var selectedUsers = roleName.split(",");
			for (var i in selectedUsers){
				$(".userCheckbox[value='" + selectedUsers[i] + "']").attr("checked", true);
			}	
		}
	},
	updateRate : function(activity, expenseRateId, revenueRateId) {
		
		var internalCostRate = $("#internalCostRate");
		internalCostRate.empty();
		internalCostRate.append("<option value='-1'>" + lb_rate_none + "</option>");
		var rates = workflowDetailData["rates"][activity];
		for (var i in rates){
			var rate = rates[i];
			if (expenseRateId == rate["id"]){
				internalCostRate.append("<option value='" + rate["id"] + "' selected='selected'>" + rate["name"] + "</option>");
			} else {
				internalCostRate.append("<option value='" + rate["id"] + "'>" + rate["name"] + "</option>");
			}
			
		}
		
		
		var billingChargeRate = $("#billingChargeRate");
		billingChargeRate.empty();
		billingChargeRate.append("<option value='-1'>" + lb_rate_none + "</option>");
		for (var i in rates){
			var rate = rates[i];
			if (revenueRateId == rate["id"]){
				billingChargeRate.append("<option value='" + rate["id"] + "' selected='selected'>" + rate["name"] + "</option>");
			} else {
				billingChargeRate.append("<option value='" + rate["id"] + "'>" + rate["name"] + "</option>");
			}
			
		}
	},
	updateNode : function(node) {
		Dialog.editNode = node;
		$("#activityTypeSelect option[value='-1']").attr("selected", true);
		$("#activityTypeSelect option[value='" + node.getAssignmentValue("activity") + "']").attr("selected", true);
		
		$("#systemActivitySelect option[value='-1']").attr("selected", true);
		$("#systemActivitySelect option[value='" + node.getAssignmentValue("action_type") + "']").attr("selected", true);
		
		var upload = node.getAssignmentValue("report_upload_check");
		if (upload == 0) {
			$("#uploadCheckbox").removeAttr("checked");// //
		} else {
			$("#uploadCheckbox").attr("checked", 'true');
		}

		var time = getTime(node.getAssignmentValue("accepted_time"));
		$("#accept_d").val(time.d);
		$("#accept_h").val(time.h);
		$("#accept_m").val(time.m);
		
		time = getTime(node.getAssignmentValue("completed_time"));
		$("#complete_d").val(time.d);
		$("#complete_h").val(time.h);
		$("#complete_m").val(time.m);
		
		time = getTime(node.getAssignmentValue("overdueToPM_time"));
		$("#overduePM_d").val(time.d);
		$("#overduePM_h").val(time.h);
		$("#overduePM_m").val(time.m);
		
		time = getTime(node.getAssignmentValue("overdueToUser_time"));
		$("#overdueUser_d").val(time.d);
		$("#overdueUser_h").val(time.h);
		$("#overdueUser_m").val(time.m);
		
		var activity = node.getAssignmentValue("activity");
		var roleName = node.getAssignmentValue("role_name");
		var useAllUsers = node.getAssignmentValue("role_type") == "false";
		if(useAllUsers){
			var rolePreference = node.getAssignmentValue("role_preference");
			if (rolePreference == "availableResources")
				$("#dialogUserSelect option[value='-1']").attr("selected", true);
			else if (rolePreference == "fastestResources")
				$("#dialogUserSelect option[value='-2']").attr("selected", true);
			else
			    $("#dialogUserSelect option[value='0']").attr("selected", true);
			
			UI.dialogUserTable.hide();
		} else {
			$("#dialogUserSelect option[value='1']").attr("selected", true);
			UI.dialogUserTable.show();
		}
		
		// user table
		
		 Dialog.updateUserTable(activity, roleName);
		
		//Internal Costing Criteria
		var v = node.getAssignmentValue("rate_selection_criteria");
		$('input:radio[name=internalCostCriteria]')[v - 1].checked = true;
		
		//rate
		var expenseRateId = node.getAssignmentValue("expense_rate_id");
		var revenueRateId = node.getAssignmentValue("revenue_rate_id");
		Dialog.updateRate(activity, expenseRateId, revenueRateId);
	},
	saveNode : function() {
		var node = Dialog.editNode;
		var activityType = $("#activityTypeSelect").val();
		
		if (activityType == -1) {
			alert(msg_noActivity);
			return false;
		}
		
		var ad = $("#accept_d").val();
		var ah = $("#accept_h").val();
		var am = $("#accept_m").val();		
		if (!timesAreValid(ad, ah, am)){
			alert(msg_time_accept);
			return false;
		}
		
		var cd = $("#complete_d").val();
		var ch = $("#complete_h").val();
		var cm = $("#complete_m").val();		
		if (!timesAreValid(cd, ch, cm)){
			alert(msg_time_complete);
			return false;
		}
		
		var acceptTime = timeToNumber(ad, ah, am);
		var completeTime = timeToNumber(cd, ch, cm);
		if (acceptTime == 0 || completeTime == 0) {
			alert(msg_loc_profiles_time_restrictions);
			return false;
		}
		
		var pd = $("#overduePM_d").val();
		var ph = $("#overduePM_h").val();
		var pm = $("#overduePM_m").val();		
		if (!timesAreValid(pd, ph, pm)){
			alert(msg_time_pm);
			return false;
		}
		
		var ud = $("#overdueUser_d").val();
		var uh = $("#overdueUser_h").val();
		var um = $("#overdueUser_m").val();		
		if (!timesAreValid(cd, ch, cm)){
			alert(msg_time_user);
			return false;
		}
		
		var overduePMTime = timeToNumber(pd, ph, pm);
		var overdueUserTime = timeToNumber(ud, uh, um);
		if (overduePMTime == 0 || overdueUserTime == 0) {
			alert(msg_loc_profiles_overdue_time_restrictions);
			return false;
		}
		
		if (overduePMTime < overdueUserTime
                || overduePMTime == overdueUserTime)
        {
			alert(jsmsg_system_parameters_notification_days);
            return false;
        }
		
		//for jquery 1.6 issues
		var us = $(".userCheckbox");
		for (var i = 0; i < us.length; i++){
			var u = $(us[i]);
			if (u.attr("checked") == "checked"){
				u.attr("checked", true);
			}
		}
		
		$("#selectAllUserCheckbox").attr("checked", false);
		var dialogUserSelect = $("#dialogUserSelect").val();
		if (dialogUserSelect < 1){
			var us = $(".userCheckbox");
			if (us.length == 0) {
				alert(msg_no_participant);
				return false;
			}
		} else {
			var us = $(".userCheckbox[checked]");
			if (us.length == 0) {
				alert(msg_no_participant);
				return false;
			}
		}
		
		node.updateAssignmentValue("activity", activityType);		
		node.updateAssignmentValue("action_type", $("#systemActivitySelect").val());
		
		if ($("#uploadCheckbox").attr("checked")) {
			node.updateAssignmentValue("report_upload_check", 1);
		} else {
			node.updateAssignmentValue("report_upload_check", 0);
		}
		
		node.updateAssignmentValue("accepted_time", acceptTime);
		node.updateAssignmentValue("completed_time", completeTime);
		node.updateAssignmentValue("overdueToPM_time", overduePMTime);
		node.updateAssignmentValue("overdueToUser_time", overdueUserTime);
		
		if (dialogUserSelect <= 0){
			node.updateAssignmentValue("role_type", "false"); 
			node.updateAssignmentValue("role_name", "All qualified users");
			var roles = getRoles(activityType);
			node.updateAssignmentValue("roles", roles);
			
			if (dialogUserSelect == -1){
				node.updateAssignmentValue("role_preference", "availableResources");
			} else if (dialogUserSelect == -2){
				node.updateAssignmentValue("role_preference", "fastestResources");
			} else {
				node.updateAssignmentValue("role_preference", "");
			}
		} else {
			var us = $(".userCheckbox[checked]");
			
			var role;
			var roles;
			var isFirst = true;
			for (var i = 0; i < us.length; i++) {
				var u = us[i];
				var val = $(u).val();
				
				if (!isFirst) {
					role = role + "," + val;
					roles = roles + "," + roleMap[val];
				} else {
					isFirst = false;
					role = val;
					roles =  roleMap[val];
				}
			}
			
			node.updateAssignmentValue("role_type", "true"); 
			node.updateAssignmentValue("role_name", role);
			node.updateAssignmentValue("roles", roles);
		}
		
		node.updateAssignmentValue("rate_selection_criteria", $('input:radio[name=internalCostCriteria][checked]').val());
		
		node.updateAssignmentValue("expense_rate_id" , $("#internalCostRate").val());
		node.updateAssignmentValue("revenue_rate_id" , $("#billingChargeRate").val());
		
		node.showTxt();
		return true;
	}
}

function initSystemActivity() {
	var actions = workflowDetailData["systemAction"];
	for (var i in actions){
		$("#systemActivitySelect").append("<option value='" + actions[i]["type"] + "'>" + actions[i]["displayName"] + "</option>");
	}
}

function initActivityType() {
	var activities = workflowDetailData["activities"];
	for (var i in activities){
		$("#activityTypeSelect").append("<option value='" + activities[i]["name"] + "'>" + activities[i]["displayName"] + "</option>");
	}
}

function showProperties(node) {
	if (node.type != "activityNode")
		return;
	
	Menu.showProperty = true;
	UI.propertiesDiv.dialog({
		title : msg_edit_activity,
		width : 650,
		resizable : false,
		buttons : {

			Cancel : function() {
				hideProperties();
			},
			"OK" : function() {
				saveProperties();
			}
		},
		close : function() {
			Menu.showProperty = false;
		}
	});
	
	Dialog.updateNode(node);
}

function checkProperties() {
	
}

function saveProperties() {
	
	if (Dialog.saveNode()) {
		hideProperties();
	}
}

function hideProperties() {
	UI.propertiesDiv.dialog('close');
	Menu.showProperty = false;
}