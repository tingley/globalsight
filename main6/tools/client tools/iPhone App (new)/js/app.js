/**
 * @author mark
 */


var jqm={
 	A:"pageinit",
 	B:"pagahide",
 	C:"pageinit,pagecreate,pageshow,pagebeforeshow",
	D:"pageinit,pagecreate"//if cached pagecreate only run one time
 }

var pages=[{id:views.A,event:jqm.C}
			,{id:views.B,event:jqm.C}
			,{id:views.C,event:jqm.C}
			,{id:views.D,event:jqm.C}
			,{id:views.E,event:jqm.C}];

gm.run(pages);

//$('#avlist').live(pagecreate,function(){
//	alert(1)
//});
//$('#'+views.E).live(jqm.A,gm.controllers[id][e]);

