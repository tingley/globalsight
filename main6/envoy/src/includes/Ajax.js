
function sendAjax(obj, method, callback)
{
	dojo.xhrPost(
	{
		url:"AjaxService?action=" + method,
		content:obj,
		handleAs: "text", 
		load:function(data){
			eval(callback+'(data)');
		},
		error:function(error)
		{
			alert(error.message);
		}
	});
}

function uploadFile(fileType, formId, method, callback) {
	
     dojo.require("dojo.io.iframe");
     var upload_url = "AjaxService?action=" + method + "&fileType=" + fileType;
     
     dojo.io.iframe.send({
		form: dojo.byId(formId),
		url:  upload_url, 
        method: 'POST', 
        contentType: "multipart/form-data",
		handleAs: "text/plain",
		handle: function(response, ioArgs){
			if(response instanceof Error){
				alert("Failed to upload file, please try later.");
			}else{
				eval(callback+'(response)');
			}	
		}
	});

}

