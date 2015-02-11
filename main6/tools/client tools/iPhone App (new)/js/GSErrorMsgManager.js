/**
 * @author Mark
 */
var updateAccessTime=0;
var needBreak=false;

var GSErrorMsgManager={
	receiveMsg:function(msg){
		needBreak=false;
		if(!msg){
			updateAccessTime++;
			if(updateAccessTime>1){
				//URLManager.rollback();
				this.showAlert("Server is no response.");
				return logoff();
			}else{
				useAjax();
			}
			
			$.mobile.hidePageLoadingMsg();	
			return needBreak;
		}else if(msg.GSErrorMsg){
			updateAccessTime=0;
   			var temp=""+msg.GSErrorMsg;
			var promt=temp.split("::")[0];
			$.mobile.hidePageLoadingMsg();	
			URLManager.rollback();
			if(promt){
				if(promt.match("list")){
					return needBreak;
				}
				var info=msg.GSErrorMsg.split("::")[1];
				this.showAlert(info);
				if(info.match('accessToken')){
					return logoff();
				}
			}				
		}
		return needBreak;
	},
	checkConfirm:function(doName,isWorkflow,confirmCallBack){
		
		if(doName=="Discard"){
			var info;
			if(isWorkflow){
				info="Are you sure to discard selected workflow(s)?"
			}else{
				info="Are you sure to discard selected job(s)?"
			}
			return  this.showConfirm(info,confirmCallBack)
		}else{
			
			return true;
		}
	},
	checkState:function(state,doName){
		if(doName=="Dispatch"){
			if(state!="Ready"){
				this.showAlert("Only workflows in 'Ready' state can be dispatched.");
				return false;
			}
		}
		if(doName=="Discard"){
			if(state=="Ready"||state=="Dispatched"){
				return true;
			}else{
				this.showAlert("Only workflows in 'Ready' and 'Dispatched' states can be discarded.");
				return false;
			}
		}
		
		return true;
	},
	parsererror:function(textStatus){
		switch(textStatus){
				case "timeout":break;
				default :break;
		}
		URLManager.rollback();
		this.showAlert("Can't load data. please check your web connection...");
		$.mobile.hidePageLoadingMsg();			
		$("#logoImg").show("slow");
		if($("#ipname:visible"))return;
		$('.content').children().toggle('slow');
	},
	showAlert:function (info){
		if(!navigator.notification){
			alert(info)
		}else{
			navigator.notification.alert(
				info,  // msg
			   	alertDismissed,         // callback
			   	'GlobalSight',            // title
			   	'Done'                  // btn name
			);
		}
	},
	showConfirm:function (info,confirmCallBack) {
		if(!navigator.notification){
			return confirm(info)
		}else{
		   	 return navigator.notification.confirm(
			   	info,   
			   	confirmCallBack,              
			   	'GlobalSight',            
			   	'Yes,Cancel'          
		   	)
		}
	 }
	
}

function alertDismissed() {
	// TODO
}

function logoff(){	
	gm.accessToken="";
	gm.companyNames=[];
	URLManager.fixInfo(views.A,"ready");
	$.mobile.hidePageLoadingMsg();	
	
	needBreak=true;
	return needBreak;
}

