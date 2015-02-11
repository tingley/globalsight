/**
 * @author Mark
 */
      // "Available":Task.STATE_ACTIVE 3
        // "In Progress":Task.STATE_ACCEPTED 8
        // "Finished": Task.STATE_COMPLETED -1
var stack={"fixInfo":{"rootName":"login","navTagName":"ready"}};
var Urlparam="";
var UsefulIdClass={"listJob":"jobId","listActivity":"taskId","detailJob":"wfId","available":"assignees","accepted":"acceptor"};
var PrarentUrl={"detailJob":"listJob","detailActivity":"listActivity"};
var UrlMap={"Discard":{"listJob":"discardJob","detailJob":"discardWorkflow"},
"Dispatch":{"listJob":"dispatchJob","detailJob":"dispatchWorkflow"},
"Accept":{"listActivity":"acceptTask","detailActivity":"acceptTask"},
"Complete":{"listActivity":"completeTask","detailActivity":"completeTask"}};
var URLManager = {
	
    requestInfo: function (rootName, navTagName) {
    	if(!navTagName){
    		navTagName=this.getnavTagName();
    	}
		if(!rootName){
			rootName=this.getRootName();
		}
    	stack["requestInfo"]={"rootName":rootName,"navTagName":navTagName};
    },

    fixInfo: function (rootName, navTagName) {
    	stack["fixInfo"]={"rootName":rootName,"navTagName":navTagName};
    },
	fixParam:function(param){
//		param["usedId"]=temp;
//		param["dmarg"]=doName;
		Urlparam=UrlMap[param];
	},
    fixRequest:function(){
		Urlparam="";
    	if(stack["requestInfo"]){
    		stack["fixInfo"]=stack["requestInfo"];
    		stack["requestInfo"]=null;
    	}
    	
    },

    rollback: function () {
    	stack["requestInfo"]=null;
        
    },
    getUsfulobj:function(){
    	var obj;
    	if(stack["requestInfo"]){
    		obj=stack["requestInfo"];
    		
    	}else{
    		obj=stack["fixInfo"];
    	}
    	return obj;
    },
	//******************************************************************************
    getUrl:function(updata){
    	var obj=this.getUsfulobj();
//		action=discardJob&jobId=1956
		if(Urlparam){
			return "&action="+Urlparam[obj["rootName"]];
			
		}
    	return "&action="+obj["rootName"]+"&status="+obj["navTagName"];
    },
	
	
    getRootName:function(){
    	var obj=this.getUsfulobj();
    	return obj["rootName"]
    },
	getUsefulIdClass:function(){
		var obj=this.getUsfulobj();
    	return UsefulIdClass[obj["rootName"]];
	},
	getUsefulAsscc:function(){
		var obj=this.getUsfulobj();
    	return UsefulIdClass[obj["navTagName"]];
	},
	getPrarentUrl:function(){
		var obj=this.getUsfulobj();
    	return PrarentUrl[obj["rootName"]];
	},
    getnavTagName:function(){
    	var obj=this.getUsfulobj();
    	return obj["navTagName"]
    },
    
    getDebugPath:function(){
    	var obj;
    	if(stack["requestInfo"]){
    		obj=stack["requestInfo"];
    		
    	}else{
    		obj=stack["fixInfo"];
    	}
    	return obj["rootName"]+"/"+obj["navTagName"];
    }
};



