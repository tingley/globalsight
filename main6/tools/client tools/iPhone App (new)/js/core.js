/**w08.globalsight.com
 * @author mark
 */



var userAgent = navigator.userAgent.toLowerCase(); 
var isiPhone = (userAgent.indexOf('iphone') != -1) ? true : false; 
if(userAgent.indexOf('ipod') != -1) isiPhone = false; 
// turn off taps for iPod Touches 
clickEvent = isiPhone ? 'tap' : 'click';
//console.log(userAgent);

function w2mTransitionHandler( name, reverse, $to, $from ) {
    $(".ui-loader").css({"display": "block", "top": "252px !important" });
    var deferred = new $.Deferred(),
    reverseClass = reverse ? " reverse" : "",
    viewportClass = "ui-mobile-viewport-transitioning viewport-" + name,
    doneFunc = function() {

        $to.add( $from ).removeClass( "out in reverse " + name );

        if ( $from && $from[ 0 ] !== $to[ 0 ] ) {
            $from.removeClass( $.mobile.activePageClass );
        }
       
        $to.parent().removeClass( viewportClass );
        $(".ui-loader").css({ "display": "none" });
        deferred.resolve( name, reverse, $to, $from );
    };

    $to.animationComplete( doneFunc );

    $to.parent().addClass( viewportClass );

    if ( $from ) {
        $from.addClass( name + " out" + reverseClass );
    }
    $to.addClass( $.mobile.activePageClass + " " + name + " in" + reverseClass );

    return deferred.promise();
}







$(document).bind("mobileinit", function(){
 	$.support.cors = true; 
	$.mobile.allowCrossDomainPages = true;
	$.mobile.defaultTransitionHandler = w2mTransitionHandler;
    //$.mobile.page.prototype.options.addBackBtn = true;
//	$.mobile.page.prototype.options.domCache = true;
});
$(document).bind("deviceready", function(){
    //navigator.notification.alert("GlobalSight is initialized...");
});
// NOTE: if "isDebug" is true, it will use local dummy data, will not invoke server really, so this should be false actually.
const isDebug=false;
 var views={
 	A:"login",
 	B:"listJob",
 	C:"listActivity",
	D:"detailJob",
	E:"detailActivity",
	F:"black"
 }
var globalSightPath;
var me;
var loginIds=["ipname","port","userName","password","toggleswitch1"];
var GSDataClass={
	"SourceFiles":["fileName","fileProfile","jobFileWC"],
	"Workflows":["wfId","targetLocale","state","CurrentAc"],
	"Files":["fileName","taskFileWC"],
	"listJob":["jobId","jobName","dateCreated","jobWC"],
	"listActivity":["jobId","taskId","jobName","taskWC","locales","assignees","acceptor","activity"]
}
var clonedNode={
	"SourceFiles":null,
	"Workflows":null,
	"Files":null,
	"listJob":null,
	"listActivity":null,
	"logout":null
}
var buttomToolsNavCache={
	"listJob":"ready",
	"listActivity":"available"
}




var navName=["Available","Progress","Finished"];
var pageData=[];
var updata={};

var myScroll;
var	pullDownEl;
var $pullup;
var pullDownOffset;
//it used initListUI and dmarg
var thelist;
var theliElemnt;
var	generatedCount = 0;




var result;
var pageNum=0;
 		



var assignees={};
var AssigneesContainter;
var AssigneesRecerver;
var transition={
					transition: 'slideup',
					reverse: false
				}
var navTagName="ready";
const listCount=10;

var gm={
	controllers:{},
 	webSight:"http://10.10.215.60/",//just for test;
 	accessToken:"",
	myMenus:[],
	companyNames:[],
 	MobileService:"globalsight/MobileService?accessToken=",
 	//MobileService:"globalsight/MobileService?userName=comadmin&password=password&accessToken=",
 	
 	
 	localStoreBox:{"":{}},
	localViews:{},
	footerBar:function(viewName,activebtn){
			
			var refresh=URLManager.getRootName();
			pageNumber=1;
			updata={};
			rootName=viewName;
	 		navTagName=buttomToolsNavCache[rootName];
	 		URLManager.requestInfo(rootName,navTagName);
			
			callbackfunc=function(){
				//TODO name='activities'
				var footerNav=$(".LM-footer").find("a");
				footerNav.each(function(){
					var temp=$(this).attr("name");
					if(temp==activebtn){
						$(this).addClass("ui-btn-active");
					}else{
						$(this).removeClass("ui-btn-active");
					}
				});
				
				$.mobile.changePage ($("#"+viewName),transition);
				transition={
					transition: 'slideup',
					reverse: false
				};
				if(refresh==viewName){
					pushData();
				}
			};
			useAjax();		
			
	},
 	navBtn:{
		"listJob":function(name){
	 		gm.jobTools[name]();
		},
		"listActivity":function(name){	
			showToolBtn(name);
			$.each(assignees,function(key,obj){
				if(key==name){
					obj.show();
					obj.prev().show();
				}else{
					obj.hide();
					obj.prev().hide();
				}
				
			})
		}	
	},
 	buttomTools:{
		//"actives":
		"activities":function(){
			gm.footerBar(views.C,"activities")	
			
		},
		//href="listJob.html"
		"jobs":function(){
			gm.footerBar(views.B,"jobs")		
		},
		//name="logout"
		"logout":function(){
			URLManager.requestInfo("logout",navTagName);
			callcompletefunc=function(){
				gm.accessToken="";
				gm.companyNames=[];
				$.mobile.changePage ($("#login"),{
					transition: 'slidedown',
					reverse: false
				});
			}
			useAjax();
			gm.accessToken="";
			gm.companyNames=[];
			
		}
	
	},
	jobTools:{
			"ready":function(){
				$("#Dispatch").show();
				$("#Discard").show();
			},
			"dispatched":function(){
				$("#Discard").show();
		 		$("#Dispatch").hide();
			},
			"exported":function(){
				$("#Dispatch").hide();
				$("#Discard").hide();
			},
			"pending":function(){
				$("#Discard").show();
				$("#Dispatch").hide();
			}
	}
}


gm.run=function(pages){
 	var count=pages.length;
 	for(var i=0;i<count;i++){
 		var page=pages[i],
		id=page.id;
		e_array=page.event.split(',');
		for(var j=0;j<e_array.length;j++){
			var e=e_array[j];
			if($.trim(e).length==0)continue;
			$('#'+id).live(e,gm.controllers[id][e]);
			//the name must as same as app and contrillers
		}
 	}
 }

var DetailToolsBtn={
	"detailJob":checkDetailClick,
	"detailActivity":checkActivityClick,
	"listJob":gm.buttomTools["jobs"],
	"listActivity":gm.buttomTools["activities"]
}




 //TODO the cache will more the once triger this 
function nav4active(event){
	pageNumber=1;
	var sef=$(this);
	navTagName=$(this).attr("name");
	URLManager.requestInfo(null,navTagName);
	callbackfunc=function(){
		//sef.removeClass("ui-btn-active");
		if(navTagName==URLManager.getnavTagName()){
			sef.addClass("ui-btn-active")
		}else{
			return;
		}
		if(navTagName=="exported"||navTagName=="finished"){
			thelist.find(".ui-block-a").hide();
		}else{
			thelist.find(".ui-block-a").show();
		}
		gm.navBtn[rootName](navTagName);
		buttomToolsNavCache[rootName]=navTagName;
		pushData();
	};
	useAjax();
	//event.stopPropagation();				
	
}
var cop;
function createRadio() {
	$('.content').children().toggle('slow');
	$.mobile.hidePageLoadingMsg();
	$("#companyRadio").show('slow');
	var companyView=$("#genre");
	var node=cop.clone(true);
		node.val("");
		node.text(" ");
		companyView.append(node);
	for(var i=0;i<gm.companyNames.length;i++){
		var node=cop.clone(true);
		node.val(gm.companyNames[i]);
		node.text(gm.companyNames[i]);
		companyView.append(node);
		
	}
	companyView.change(selectedCompany);
	
}
function selectedCompany(){
	var tt=$(this).val();
	gm.accessToken+="&companyName="+tt;
	$(this).blur();
	transoutLogin();
	
}

 function transoutLogin(){
 	gm.myMenus=(""+pageData.myMenus).split(",");
	$("#logoImg").hide("slow");
 	$("#companyRadio").hide();
 	if(pageData.myMenus){
 		gm.buttomTools[gm.myMenus[0]]();
 	}else{
 		$.mobile.changePage( views.F+".html", { showLoadMsg: false } );
 		callbackfunc=null;
 	}
	$.mobile.loadPage( views.C+".html", { showLoadMsg: false } );
	$.mobile.loadPage(views.B+".html", { showLoadMsg: false } );
 }
 
 
 
 
 gm.controllers[views.A]={
 	pageinit:function(event){
		cop=$(".cop").clone(true);
 		$("input").each(function  () { 
 			var id=$(this).attr("id");
 		 	var cc=$.cookie(id);
 		 	if(cc){
 		 		
 		 	$(this).val(cc);			
 		 	}
 		 $(this).bind(clickEvent, function(){
 		 	$(this).val("");	
 		 });
	 	});
		
 		$("#loginbtn").bind(clickEvent, function(){	
	 		URLManager.fixInfo(views.A,"ready");
	 		gm.accessToken="";		
			$.each(loginIds,function(key,value){
				$.cookie(value, $("#"+value).val(), { expires: 7 });
				updata[value]=$("#"+value).val(); 	
				//updata+="&"+value+"="+$("#"+value).val();		
			});
			var ht=$("#toggleswitch1").val()=="off"?"http://":"https://";
			gm.webSight=ht+$("#ipname").val()+":"+$("#port").val()+"/";
			var transout=transoutLogin;
			callbackfunc=function (){
					gm.accessToken=pageData.accessToken;
					if(!gm.accessToken)return;
					if(pageData.companyNames){
						gm.companyNames=pageData.companyNames;
						//alert(gm.companyNames.length)
						createRadio();
						transout=function(){};
					};
					me=$.cookie("userName");
					
					transout();
					
					
					
			 }
			useAjax();			
		})
 	},
	pagecreate:function(event){//it before init
			
 	},
	pagebeforeshow:function(event){
 		
 	},
	pageshow:function(){
		$("#logoImg").show("slow");
		$(".content").children(":not('#companyRadio')").show();
		
		$("#genre").empty();
		//well jquery mobile do some span for UI so that the defaul only set onece.
		$(".cop").text("Select one...");
	}
 }
 
gm.controllers[views.B]={
 	pageinit:function(event){
		
 	},
	pagecreate:function(event){//it before init
		thelist=$("#thelist");
		clonedNode[views.B]=thelist;
		bindEvent(event);
		//TODO resolve the back bug
		initListUI();
		$.mobile.loadPage( views.D+".html", { showLoadMsg: false } );
			
 	},
	pagebeforeshow:function(event){
		// var orginUrl=URLManager.getRootName();
		// if(orginUrl!=views.B){
			// pageData=gm.localViews[views.B];
		// }
 		setTimeout(loadedJobs, 200);
 		thelist=$("#thelist");
		//gm.navBtn[rootName](navTagName);
		URLManager.fixInfo(views.B,navTagName);
		nav4active(new Event(clickEvent));
		pushData();
 		
 	},
	pageshow:function(){
		
	}
 }
 


 
 
 
 		
 gm.controllers[views.C]={
 	pageinit:function(event){
		
			
//		thelist.page({domCache: true });  
 	},
	pagecreate:function(event){
//		$(":jqmData(role='page')", e.target).subpage();
		thelist=$("#activeListContainter");
		clonedNode[views.C]=thelist;
 		bindEvent(event);
		//TODO resolve the back bug
		initListUI();	
		$.mobile.loadPage(views.E+".html", { showLoadMsg: false } );
 	},
 	pagebeforeshow:function(event){
		var orginUrl=URLManager.getRootName();
 		setTimeout(loadedAvialable, 200);
 		thelist=$("#activeListContainter");
		assignees["available"]=thelist.find(".assignees");
		assignees["accepted"]=thelist.find(".acceptor");
		
		//assignees["finished"]=thelist.find(".selected");
 		//gm.navBtn[rootName](navTagName);
		URLManager.fixInfo(views.C,navTagName);
		nav4active(new Event(clickEvent));
 		pushData();
 		
 	},
	pageshow:function(event){
		
		
 	}
 }
 
  gm.controllers[views.D]={
 	pageinit:function(event){
		
			
 	},
	pagecreate:function(event){
//		$(":jqmData(role='page')", e.target).subpage();
		initButtomUI()
		bindbuttomEvent(event)
 	},
	pagebeforeshow:function(event){
 		URLManager.fixInfo(views.D,URLManager.getnavTagName());
		pushDetailData();
		$(".imgContainer").each(function(){
			$(this).bind(clickEvent,toggleV);
		})
 		
 	},
	pageshow:function(event){
		
		// bindEvent(event);
		// $("tr[name='WorkflowsTr']").each(function(){
 			// $(this).bind(clickEvent, function(){
 				// var img=$(this).find("img:visible");
 				// img.trigger(clickEvent);
 			// });
 		// })
		
		
		
		
 	}
 }
 
 gm.controllers[views.E]={
 	pageinit:function(event){
		
			
 	},
	pagecreate:function(event){
//		$(":jqmData(role='page')", e.target).subpage();
		initButtomUI();
		bindbuttomEvent(event)	;
 	},
	pagebeforeshow:function(event){	
 		URLManager.fixInfo(views.E,URLManager.getnavTagName());
 		pushDetailData();
 		
 	},
	pageshow:function(event){
//		$(":jqmData(role='page')", e.target).subpage();
 		// bindEvent(event);
 		
 		
 	}
 }
 
 gm.controllers[views.F]={
 	pageinit:function(event){
		
 	},
	pagecreate:function(event){//it before init
		initButtomUI();
		bindbuttomEvent(event)	;
			
 	},
	pagebeforeshow:function(event){
 		
 	},
	pageshow:function(){
	}
 }
  // this will case back bug
 //TODO
 function initButtomUI(){
 	if($.inArray("activities", gm.myMenus)<0){
 		$(".buttomTools[name='activities']").hide();
	}else{
		$(".buttomTools[name='activities']").show();
	}
	if($.inArray("jobs", gm.myMenus)<0){
 		$(".buttomTools[name='jobs']").hide();
	}else{
		$(".buttomTools[name='jobs']").show();
	}
}
 
 
 
 function initListUI(){
 	initButtomUI();
	navTagName=URLManager.getnavTagName();
	$(".statetabbar").find("a[name='"+navTagName+"']").addClass("ui-btn-active");
 	theliElemnt=thelist.children().eq(0).clone(true);
		for(var i=1;i<listCount;i++){
			node=theliElemnt.clone(true);
			thelist.append(node);	
		}
 }
 
var rootName=views.B;

//@Detail page click the back accept complete

function checkDetailClick(){
	var doName=$(this).text();
	var ch=$("tr[name='WorkflowsTr']");
	var temp=[];
	var usedClass=URLManager.getUsefulIdClass();
	ch.each(function(){
		var bl=$(this).find(".selected").is(":visible");
		var state=$(this).find(".state").text();
		if(bl){	
			if(GSErrorMsgManager.checkState(state,doName)){
				
			}else{
				temp=[];
				return false;
			}		
			var value=$(this).find("."+usedClass).text();
			temp.push(value);			
		}
	})
	if(temp.length){
		confirmCallBack=function(button){
			if(button==2)return;
			updata[usedClass]=temp;
			// '&foo=bar1&foo=bar2
			for(var i=0;i<temp.length;i++){
				param+="&"+usedClass+"="+temp[i];
			}
			
			URLManager.fixParam(doName);
			callbackfunc=function(){
				$.mobile.hidePageLoadingMsg();
				if(pageData.GSErrorMsg){
					GSErrorMsgManager.showAlert(pageData.GSErrorMsg);
					return;
				}	
				var ch=$("tr[name='WorkflowsTr']");
				ch.each(function(){
					var bl=$(this).find(".selected").is(":visible");
					if(bl){
						if(doName=="Discard"){
							$(this).hide('slow');			
						}else if(doName=="Dispatch"){
							$(this).find(".state").text("Dispatched");
							$(this).find(".selected").trigger(clickEvent);
						}
				}
			})
			};
			useAjax();
			
		}
		
		if(!GSErrorMsgManager.checkConfirm(doName,true,confirmCallBack)){
			return;
		}else{
			confirmCallBack(1);
		}
		
	}
}

function checkActivityClick(){
	var container=$("#"+URLManager.getRootName());
	if($(this).hasClass("clicked")){
		return;
	}else{
		$(this).addClass("clicked");
	}
	var doName=$(this).text();
	var sef=$(this);
	URLManager.fixParam(doName);
	var temp=container.find(".taskId").text();
	if(!temp){
		GSErrorMsgManager.showAlert("I can't find useful task.");
		return;
	}
	updata[URLManager.getUsefulIdClass()]=temp;
	param+="&taskId="+temp;
	callbackfunc = function(){
		$.mobile.hidePageLoadingMsg();
		if(pageData.GSErrorMsg){
			GSErrorMsgManager.showAlert(pageData.GSErrorMsg);
			return;
		}
		if(doName=="Accept"){
			//well , we must get the node more;this not defined
			sef.children().text("Complete");
		}else if(doName=="Complete"){
			sef.empty();
			container.find(".rewriteBack").trigger(clickEvent);
		}
	}
	
	useAjax();
	setTimeout(function(){
		sef.removeClass("clicked");
	},1000);
}


//discard dispatch accept complete
function dmarg(){
	var doName=$(this).text();
	var ch=clonedNode[URLManager.getRootName()].children();
	var temp=[];
	var idClass=URLManager.getUsefulIdClass();
	var asscc=URLManager.getUsefulAsscc();
	
	var apoFun;
	if(doName=="Accept"||doName=="Complete"){
		apoFun=function(that){
			var ascer=that.find("."+asscc).text().split(",");
			if($.inArray(me, ascer)<0){
				temp={};
				GSErrorMsgManager.showAlert("you are not in the assignees.");
			}
		}
		
	}else{
		apoFun=function(that){};
	}
	ch.each(function(){
		var that=$(this);
		var bl=$(this).find(".selected").is(":visible");
		if(bl){
			var value=$(this).find("."+idClass).text();
			temp.push(value);
			apoFun(that);			
		}
	})
	if(temp.length){
		var confirmCallBack=function(button){
			if(button==2)return;
			updata[idClass]=temp;
			for(var i=0;i<temp.length;i++){
				param+="&"+idClass+"="+temp[i];
			}
			URLManager.fixParam(doName);
			callbackfunc=doWantedUI;
			useAjax();
		}
		
		if(!GSErrorMsgManager.checkConfirm(doName,false,confirmCallBack)){
			return;
		}else{
			confirmCallBack(1)
		}
	}
}

function doDetailClick(){
	$.mobile.hidePageLoadingMsg();
	if(pageData.GSErrorMsg){
		GSErrorMsgManager.showAlert(pageData.GSErrorMsg);
		return;
	}	
	var ch=$("tr[name='WorkflowsTr']");
	ch.each(function(){
				var bl=$(this).find(".selected").is(":visible");
				if(bl){
				$(this).hide('slow');			
			}
		})
}

function doWantedUI(){
	$.mobile.hidePageLoadingMsg();
	if(pageData.GSErrorMsg){
		GSErrorMsgManager.showAlert(pageData.GSErrorMsg);
		return;
	}	
	var ch=clonedNode[URLManager.getRootName()].children();
	if(!ch)return;
	ch.each(function(){
		var bl=$(this).find(".selected").is(":visible");
		if(bl){
			$(this).hide("slow"); 
		}
	})
}


function bindbuttomEvent(event){
	$(".buttomTools").each(function(){
		//href="listAvialable.html"
		if($(this).hasClass("binded"))return true;
		$(this).bind(clickEvent, gm.buttomTools[$(this).attr("name")]);
		$(this).addClass("binded");
	})
}
//TODO if used cache the navbar willstil bind more the once
function bindEvent(event){
	bindbuttomEvent(event);
	
	var navbar=$(".statetabbar").find("a");
	navbar.each(
		function(){
			if($(this).hasClass("binded"))return true;
			$(this).bind(clickEvent, nav4active);
			$(this).addClass("binded");
		}
		
	)
	
	var toolBtns=$(".tools");
	toolBtns.each(
		function(){
			if($(this).hasClass("binded"))return true;
			$(this).bind(clickEvent, dmarg);
			$(this).addClass("binded");
		}
		
	)
	var ceckImg=$(".ui-block-a")
	ceckImg.each(function(){
		$(this).bind(clickEvent,toggleV);
	})
	
	
	thelist.find(".listli").each(
		function(){
			$(this).bind(clickEvent, checkDetail);
		}
	)
	
	
	
}

var callbackfunc;
var param="";
// click the li to detail page
function checkDetail(){
		if(URLManager.getnavTagName()=="finished"){
			return;
		}
		self=$(this);
		var idClass=URLManager.getUsefulIdClass();
		var avid=self.find("."+idClass).text();
		var link=thelist.find("a").attr("name");
		if(avid){
			updata[idClass]=avid;
			param+="&"+idClass+"="+avid;
			$.each(GSDataClass[URLManager.getRootName()] ,function(key,name){
				//in the each can not used this!!!!
				
				gm.localStoreBox[name] =self.find("."+name).text();
				gm.localStoreBox[URLManager.getRootName()]=$(".tools:visible").clone(true);;
			})				
			callbackfunc=function(){
				$.mobile.changePage (link+".html",{
					transition: 'slide',
					reverse: false
				});
			}
			// well suggest request and ajax put as same time;
			URLManager.requestInfo(link);
			useAjax();
		}
	}


function pushDetailData(){
	var container=$("#"+URLManager.getRootName());
	var summery=container.find("ul[name='summery']");
	var url=URLManager.getPrarentUrl();
	var condition=URLManager.getnavTagName();
	var acceptor=summery.find("div[name='acceptor']");
	//TODO
	switch(condition){
		case "available":acceptor.addClass("assignees");break;
		case "accepted":acceptor.addClass("acceptor");break;
	}
	$.each(GSDataClass[url] ,function(key,name){
		summery.find("."+name).text(gm.localStoreBox[name] );
	})	
	if("listActivity"==url){
		var acsar=acceptor.text().split(",");
		if($.inArray(me, acsar)<0){
			url="";
		}
	}
	$.each(gm.localStoreBox[url],function(key,name){
		var toolsContainer=	container.find(".toolsContainer").eq(key);
		toolsContainer.empty();
		var ogj=$(name).children();
		toolsContainer.append(ogj);
		toolsContainer.show();
		toolsContainer.bind(clickEvent,DetailToolsBtn[URLManager.getRootName()])
	})
	switch(condition){
		case "available":		
		acceptor.removeClass("assignees");
		break;
		case "accepted":
		acceptor.removeClass("acceptor");
		break;
	}
	
	$(".rewriteBack").each(
		function(){
			if($(this).hasClass("binded"))return true;
			$(this).addClass("binded");
			$(this).bind(clickEvent,function(){
				transition={
					transition: 'slide',
					reverse: true
				};
				DetailToolsBtn[URLManager.getPrarentUrl()]();
			})
		}
	)
	if(URLManager.getRootName()==views.D){
		
		createTable("SourceFiles");
		var sourceFiles=pageData["Workflows"];
		//TODO remove if server updata
		if(sourceFiles){
			$.each(sourceFiles,function(key,name){
				if(name.state=="READY_TO_BE_DISPATCHED"){
					name.state="READY";
				}
			})
		};
		
		createTable("Workflows");	
	}
	if(URLManager.getRootName()==views.E){
		
		createTable("Files");	
	}
	
	$.mobile.hidePageLoadingMsg();
}



function createTable(tableName){
	var sourceFiles=pageData[tableName];
	var orginTr=$("tr[name='"+tableName+"Tr']").eq(0);
	if (!clonedNode[tableName]){		
		clonedNode[tableName] = orginTr.clone(true);
	}
	var PrimaryTable = $("table[name='"+tableName+"Table']").eq(0);
	orginTr.remove();
	if(!sourceFiles)return;
	for(var i=0;i<sourceFiles.length;i++){
		var node = clonedNode[tableName].clone(true);
		PrimaryTable.append(node);
		$.each(GSDataClass[tableName] ,function(key,name){
			var nodechildren =PrimaryTable.find("."+name).eq(i);
			nodechildren.text(sourceFiles[i][name]);
		})	
	
	}
}




function pushData(){
	var UIClass=GSDataClass[URLManager.getRootName()];
	var length=0;
	if(pageData){
		length=pageData[0]?pageData.length:0;
	}
	
	for(var i=0;i<length;i++){
		var children=thelist.children().eq(i);
		children.show();
		
		$.each(UIClass,function(key,name){
			var nodechildren =thelist.find("."+name).eq(i);
			nodechildren.text(pageData[i][name]);
		})		
	}
	
	for(var j=listCount;j>length-1;j--){
		var children=thelist.children().eq(j);
		children.hide();
	}
	$(".selected:visible").each(function(){
		$(this).trigger(clickEvent);
	})
	$.mobile.hidePageLoadingMsg();
	thelist.listview("refresh");
}



function toggleV(event){
	var sef=$(this);
	if(sef.hasClass("binded"))return;
	sef.addClass("binded");
	$(this).find("img").toggle();
	setTimeout(function(){
		sef.removeClass("binded")
	},500);
	event.stopPropagation();				
}


function showToolBtn(name){
	$(".tools:visible").each(function(){
		$(this).empty();
		var children=$("#"+name).children().clone(true);
		$(this).append(children);
		$(this).attr("name",name);
	})
	
}






function loadedJobs() {
	$pullup=$("#pullDown");
	$thelist=$("#thelist");
	myScroll=Scrollmanager.register("wrapperJob",pullDownAction,pullUpAction);	
	//$pullup.hide();
}

function loadedAvialable() {
	$pullup=$("#pullAvialable");
	$thelist=$("#activeListContainter");
	myScroll=Scrollmanager.register("wrapperAvialable",pullDownAction,pullUpAction);
	//$pullup.hide();	
}





var pageNumber;

var reuqestsCache={}
function useAjax(){
	$.mobile.loadingMessageTextVisible = true;
	$.mobile.showPageLoadingMsg( 'a', "Please wait..." ); 
	updata["pageNumber"]=pageNumber<1?1:pageNumber;
	
	// $(".ui-btn-active").each(function(){
		// if($(this).attr("name")){
			// param=$(this).attr("name")+".json?"+Math.random();
		// };
	// })
	// for(var i=0;i<updata.length;i++){
		// param+="&"+updata[i].name+"="+updata[i].content;
	// }
	globalSightPath=
	"ajax/"+URLManager.getDebugPath()+".json?"+Math.random();
	var request=gm.webSight+gm.MobileService+gm.accessToken+URLManager.getUrl(updata)+param;
	if(!reuqestsCache[request]){
		reuqestsCache[request]=true;
	}else{
		return;
	}
	console.log(request)
	//the ajax {} can't move to be an object?	
	$.ajax({
	    type:"get",
		dataType:"json",
	    url:isDebug?globalSightPath:request,
	   // async:false,
	    cache:false,
	    timeout:18000, 
	    data:updata,
	    crossDomain: true,
	    success:function(msg){
	    	var needBreak=GSErrorMsgManager.receiveMsg(msg)
			if(needBreak){
				$.mobile.changePage ("login.html",{
					transition: 'slidedown',
					reverse: false
				});
				return;
			};
			param="";
			updata={};	
							
			pageData=msg;
			URLManager.fixRequest();
	       if(callbackfunc && typeof (callbackfunc) === 'function') {
	       	//may we need change vieews first
	       		
	       		callbackfunc();
	       		
	        }
	    },
	    beforeSend: function(){

		// Handle the beforeSend event
		},
		error:function( jqXHR, textStatus,  errorThrown ){
			//Possible causes:
			//1.Access-Control-Allow-Origin
			//2.the msg is not a json******************************
			// alert(globalSightPath)
			GSErrorMsgManager.parsererror(textStatus);
		},
		complete:function(){
			reuqestsCache[request]=null;
			//$.mobile.hidePageLoadingMsg();	
			if(callcompletefunc && typeof (callcompletefunc) === 'function') {
	       	//may we need change vieews first
	       		callcompletefunc();
	       		callcompletefunc=null;
	            // $.mobile.hidePageLoadingMsg();
	        }
			//$.mobile.hidePageLoadingMsg();
			//alert(globalSightPath)
		}
	 });
}

var callcompletefunc=null;
function pullUpAction(){
	$pullup.addClass("loading")
	$pullup.find(".pullDownLabel").text("going to next page");
	$thelist.append($pullup);
	var theli=$thelist.find(".listli:visible").length;
	if(theli==10){		
		pageNumber++;
	}	
	callbackfunc=pushData;
	useAjax();
	 myScroll.scrollToElement('li:nth-child(1)', 2000);
	 myScroll.refresh();
}

function pullDownAction(){
	if(pageNumber>1){
		$pullup.find(".pullDownLabel").text("pull down go to pre page");
		$thelist.before($pullup);
		pageNumber--;
		callbackfunc=pushData;
		useAjax();
		myScroll.scrollToElement('li:nth-child(1)', 200);
		myScroll.refresh();
	}else{
		$pullup.find(".pullDownLabel").text("there is no pre page");
		$thelist.before($pullup);
		myScroll.refresh();
	}
}
