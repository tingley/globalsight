var ptagsReturn;
function getPtagString() {
	$.ajax({
		type : "POST",
		url : 'OnlineService?action=getPtagString',
		async : false,
		cache : false,
		dataType : 'text',
		success : function(data) {
			ptagsReturn = data;
		},
		error : function(request, error, status) {
			alert(error);
		}
	});

	return ptagsReturn;
}

var targetDiplomatReturn;
function getTargetDiplomat(text) {
	$.ajax({
		type : "POST",
		url : 'OnlineService?action=getTargetDiplomat',
		async : false,
		cache : false,
		data : {
			text : text
		},
		dataType : 'text',
		success : function(data) {
			targetDiplomatReturn = data;
		},
		error : function(request, error, status) {
			alert(error);
		}
	});

	return targetDiplomatReturn;
}

var ptagToNativeMappingTableReturn;
function getPtagToNativeMappingTable() {
	$.ajax({
		type : "POST",
		url : 'OnlineService?action=getPtagToNativeMappingTable',
		async : false,
		cache : false,
		dataType : 'text',
		success : function(data) {
			ptagToNativeMappingTableReturn = data;
		},
		error : function(request, error, status) {
			alert(error);
		}
	});

	return ptagToNativeMappingTableReturn;
}

var htmlSegmentReturn;
function getHtmlSegment(text, isFromTarget) {
	$.ajax({
		type : "POST",
		url : 'OnlineService?action=getHtmlSegment',
		async : false,
		cache : false,
		data : {
			text : text,
			isFromTarget : isFromTarget
		},
		dataType : 'text',
		success : function(data) {
			htmlSegmentReturn = data;
		},
		error : function(request, error, status) {
			alert(error);
		}
	});

	return htmlSegmentReturn;
}

var initTargetReturn;
function initTarget2(text) {
	$.ajax({
		type : "POST",
		url : 'OnlineService?action=initTarget',
		async : false,
		cache : false,
		data : {
			text : text,
			verbose : verbose,
			colorPtags : colorPtags
		},
		dataType : 'text',
		success : function(data) {
			initTargetReturn = data;
		},
		error : function(request, error, status) {
			alert(error);
		}
	});

	return initTargetReturn;
}

var errorCheckReturn;
var newTargetReturn;
var errorMsgReturn;
function doErrorCheck() {
	var text = fr_editor.GetTargetSegment();
	$.ajax({
		type : "POST",
		url : 'OnlineService?action=doErrorCheck',
		async : false,
		cache : false,
		data : {
			text : text
		},
		dataType : 'text',
		success : function(data) {
			var ob = eval("(" + data + ")");
			internalTagMsg = ob.internalTagMsg;
			newTargetReturn = ob.newTarget;
			errorMsgReturn = ob.msg;
		},
		error : function(request, error, status) {
			alert(error);
		}
	});

	return errorMsgReturn;
}
