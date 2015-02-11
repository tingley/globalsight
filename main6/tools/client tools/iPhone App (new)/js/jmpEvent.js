var JQM={
	A:"pagebeforechange",
	B:"pagebeforeload",
	C:"pagebeforecreate",
	D:"pagecreate",
	E:"pageinit",
	F:"pageload"	
	
}


$(document).bind(JQM.A,function(e,data){
	var toPage=data.toPage;
	var options=data.options;
	e.preventDefault();
	
})
