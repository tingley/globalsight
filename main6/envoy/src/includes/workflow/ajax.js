var ajaxReturnString;
function getAjaxValue(method, data)
{
	$.ajax({
		type : "POST",
		url : 'AppletService?action=' + method,
		async : false,
		cache : false,
		dataType : 'text',
		data : data,
		success : function(data) {
			ajaxReturnString = data;
		},
		error : function(request, error, status) {
			ajaxReturnString = "";
			alert(error);
		}
	});
	
	return ajaxReturnString;
}

function getWorkflowData()
{
    return 	getAjaxValue("getWorkflowData");
}

function getWorkflowDetailData()
{
	var data = getAjaxValue("getWorkflowDetailData");
	return eval("(" + data + ")");
}

function getRoles(activity)
{
	var data = {
    	activity : activity
    };
    
    var value = getAjaxValue("getRoles", data);
	return value;
}

function getUserForActivity(activity)
{
    var data = {
    	activity : activity
    };
    
    var value = getAjaxValue("getParticipantUser", data);
	return eval("(" + value + ")");
}

function saveWorkflow(xml)
{
	var data= {
		xml : xml
	};
	
	var value = getAjaxValue("saveWorkflow", data);
	
	var theForm;
	if (document.layers)
		theForm = document.contentLayer.document.templateCancel;
	else
		theForm = document.all.templateCancel;
	theForm.submit();
}