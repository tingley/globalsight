
function sendAjax(obj, method, callback)
{
	$.ajax(
	{
		url:"AjaxService?action=" + method,
		data:obj,
		dataType: "text", 
		success:function(data){
			eval(callback+'(data)');
		},
		error:function(error)
		{
			alert(error.message);
		}
	});
}

function uploadFile(fileType,formId, method, callback) {	
    var upload_url = "AjaxService?action=" + method + "&fileType=" + fileType;	     
    $("#"+formId+"").submit(function () {   
        $("#"+formId+"").ajaxSubmit({
            type: "post",
            url:   upload_url,
            success: function (data) 
            {
            	var str = data.replace(/<[^>]+>/g,"");
            	eval(callback+'(str)');
            },
            error: function(error)
            {
            	alert("Failed to upload file, please try later.");      	
            }
        });
           return false;
});
        $("#"+formId+"").submit();  
}
