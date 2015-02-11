//-- lib.js: Core JS library for www.oracle.com
//-- last updated: 7/18/00
//-- Library file MUST be loaded before all else

var orainfo_exists = false, otnnm_exists = false
var langjsLoad  = false
var dash 		= '<img src="/admin/images/dash.gif" border=0 width=150 height=5>'
var r_arrow 	= '<img src="/admin/images/r_arrow.gif" border=0 width=10 height=10>'
var d_arrow 	= '<img src="/admin/images/d_arrow.gif" border=0 width=10 height=10>'
var tl_img		= '<img src="/admin/images/tl_img.gif" border=0 width=4 height=18>'
var bl_img		= '<img src="/admin/images/bl_img.gif" border=0 width=4 height=6>'
var tr_img		= '<img src="/admin/images/tr_img.gif" border=0 width=4 height=18>'
var br_img		= '<img src="/admin/images/br_img.gif" border=0 width=4 height=6>'
var l_crnr 		= '<img src="/admin/images/rc_lft.gif" width=4 height=20>'
var r_crnr 		= '<img src="/admin/images/rc_rt.gif" width=4 height=20>'
var see_image 	= '<img src="/admin/images/see.gif" width=100 height=25 border=0>'
var try_image 	= '<img src="/admin/images/try.gif" width=100 height=25 border=0>'
var buy_image 	= '<img src="/admin/images/buy.gif" width=93 height=35 border=0>'
var block 		= ''

//-- Determine Frame status
var isFramed = false
if (parent.frames.length != 0) var isFramed = true
//alert(location + ": " + isFramed)

var user_info = new Array()
var otn_info  = new Array()

//-- Utility function defs
var min = (60 * 1000)
var hour = (60 * min)
var day = (24 * hour)
var year = (365 * day)

//-- Portlet Style defs
var PLAIN = 3, WWW = 2

// Campaign Style Defs
var INLINE = 2, WRAPPED = 4

//-- User Info (Cookie) defs
var FNAME=0, LNAME=1, TITLE=2, EMAIL=3, UID=4
var ROLE=5, Q2=6, Q3=7, Q4=8, Q5=9, OWNER=10
var OTN_UID=0, OTN_IP=1, OTN_LVL=2
var OPP_LVL=3, OPP_PIN=4

//-- Hostname internal/external
if (location.hostname.indexOf("us.oracle.com") != -1) {
	var ora_host 	= "http://www3-stage.us.oracle.com"
	var otn_host 	= "http://larva.us.oracle.com:88"
	var odp_host 	= "http://concrete.us.oracle.com:7200"
	var auth_host 	= "http://larva.us.oracle.com:2500"
	var apps_host 	= "http://www3-stage.oracle.com/appsnet"
	var bol_host 	= "http://businessonline-stage.us.oracle.com"
	var search_dad  = "/ctxsys-imedstg-dad/plsql/";
} else {
	var ora_host 	= "http://www.oracle.com"
	var otn_host 	= "http://technet.oracle.com"
	var odp_host 	= "http://odp.oracle.com"
	var auth_host 	= "http://technet.oracle.com:2500"
	var apps_host 	= "http://appsnet.oracle.com"
	var bol_host 	= "http://www.businessonline.com"
	var search_dad  = "/pls/intermedia/";
}

//-- Function Library
function stretch(w, h) {
	return '<img src="/admin/images/stretch.gif" width=' + w + ' height=' + h + ' BORDER=0>'
}

function goWin(url, type, w, h, scroll) {
	if (!type || type == "") type = 1
	if (type == 1) top.location = "./" + url
	else if (type == 2) window.open(url, "smallWin", "toolbar=0,location=0,directories=0,status=0,menubar=0,resizable=1,scrollbars=" + scroll + ",width=" + w + ",height=" + h + ",top=0,screenY=0,left=0,screenX=0")
	else if (type == 3) window.open(url, "fullWin")
}

function loader(){}
function unloader(){}

function fix_action(host, form, action){
		form.action = host + action
        return true
}

function getName(user){
	if ((user[FNAME] && user[FNAME] != "") || (user[LNAME] && user[LNAME] != "")) var foo = user[FNAME] + " " + user[LNAME]
	return foo
}

function getArg(arg_name, str) {
	var value = "", tmpstr = ""
	if (!str) str = location.search.substring(1)
	if (!str) return value
	else {
		var tmparray = str.split("&")
		for (i=0; i<tmparray.length; i++) {
			tmpstr = tmparray[i].toUpperCase()
			if (tmpstr.indexOf(arg_name.toUpperCase() + "=") != -1) {
				var tmp2array = tmparray[i].split("=")
				value = tmp2array[1]
			}
		}
	}
	return value
}

//-- Cookie Functions
function readInfoCookie() {
   	var j=0, i=0
   	var str = getCookie("ORAINFO")
	if (str == null || str == "") return false
	user_info = str.split("VS1:")
	orainfo_exists = true
	return true
}

//-- Cookie Functions
function readOTN_NMCookie() {
   	var j=11, i=0
   	var str_temp = getCookie("OTN_NM")
	if (str_temp == null || str_temp == "") return false
	var str = crunch(0, str_temp)
	otn_info = str.split("VS1:")
	otnnm_exists = true
	return true
}

// Set the ORAINFO cookie
function setORAINFOCookie(form) {
	info_fname = form.firstname.value
	info_lname = form.lastname.value
	info_title = form.title.value
	info_email = form.email.value
	info_username = form.username.value
   	info_q1 = form.question1.value
	info_q2 = form.question2.value
	info_q3 = form.question3.value
   	info_q4 = form.question4.value
	info_q5 = form.question5.value
	pgm_owner = form.pgm_owner.value
	
   	var cookieStr = info_fname + "VS1:" + info_lname + "VS1:" + info_title + "VS1:"; 
	cookieStr += info_email + "VS1:" + info_username + "VS1:";
	cookieStr += info_q1 + "VS1:" + info_q2 + "VS1:" + info_q3 + "VS1:" + info_q4 + "VS1:" + info_q5;
	cookieStr += "VS1:" + pgm_owner

	setCookie("ORAINFO", cookieStr, 30, "day")
}

function setOTN_INFOCookie(form) {
	var info_src = "WEB"
	info_fname = form.firstname.value
	info_lname = form.lastname.value
   	info_email = form.email.value
	info_company = form.company.value
	info_username = form.username.value
	info_country = form.country.value
	
	if (form.src_code0) info_src = form.src_code0.value

   	var cookieStr = info_fname + "VS1:" + info_lname + "VS1:" + info_email + "VS1:"; 
	cookieStr += info_company + "VS1:" + info_country + "VS1:" + info_src + "VS1:" + info_username;

	var encrypted = crunch(1, cookieStr)
	setCookie("OTN_INFO", encrypted, 1, "year")
}

//Set the OTN_NM Ccookie
function setOTN_NMCookie(form) {
	var otn_pgmlvl = "O"
	otn_uid = form.username.value
	otn_ip = form.ip_address.value
   	otn_level = form.member_level.value
	otn_pin = form.pin_code.value
	if (form.pgm_code) otn_pgmlvl = form.pgm_code.value

   	var cookieStr = otn_uid + "VS1:" + otn_ip + "VS1:" + otn_level + "VS1:"; 
	cookieStr += otn_pgmlvl + "VS1:" + otn_pin;

	var encrypted = crunch(1, cookieStr)
	setCookie("OTN_NM", encrypted, 30, "day")
}

function getCookie(name) {
   var arg = name + "="
   var alen = arg.length
   if (document.cookie && document.cookie.length != 0){
   		var cstart = document.cookie.indexOf(arg)
		if (cstart == -1) return null
		else {
   			var tmpStr = document.cookie.substring(cstart, (document.cookie.length+1))
   			var cend = tmpStr.indexOf(";")
			if (cend == -1) cend = tmpStr.length+1
   			tmpStr = unescape(tmpStr.substring(alen, cend))
			return tmpStr
		}
	}
	else return null
}

function crunch(encrypt, cStr) {
//-- 1 for encrypt, 0 for decrypt
	var asciiArray = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~."
	var value = "", ch = "", newStr = ""
	for (i=0; i < cStr.length; i++){
		ch = cStr.charAt(i)
		value = asciiArray.indexOf(ch)
		if (!encrypt) value += 2
		if (encrypt) value -= 2
		if (value < 0) value += 95
		if (value > 94) value -= 95
		newStr += asciiArray.charAt(value)
	}
	return newStr
}

function setCookie(name, value, time, ttype) {
	var exp = new Date()
	var cookieval = name + "=" + value + "; "
	var date = exp.getTime()
	if (time > 0) {
      	if (ttype == "year") exp.setTime(date + (time * year)); 
		else if (ttype == "day") exp.setTime(date + (time * day));
		else if (ttype == "hour") exp.setTime(date + (time * hour));
		
      	cookieval += "expires=" + exp.toGMTString();
	}
      cookieval += "; domain=.oracle.com; path=/";
	document.cookie = cookieval;
}

function signout(url) {
	var exp = new Date();
	exp.setYear(70);
	var exp_str = "expires=" + exp.toGMTString() + "; domain=.oracle.com; path=/;";
	document.cookie = "OTN_NM=;" + exp_str;
	document.cookie = "ORAINFO=;" + exp_str;
	top.location = url;
}

//readInfoCookie()

//-- Get and Set Language based information
//-- This can be moved to lang.js file when ready
var language_root  	= ""
var print_label    	= "Printer View"
var mail_label     	= "Tell a Friend"
var mail_URL		= "/admin/account/mail.html"
var rate_label     	= "Rate this Page"
var rate_URL		= "/admin/account/rate.html"
var salesrep_label  = "Contact a Sales rep"
var salesrep_URL	= "/admin/account/sales.html"
var subscribe_label	= "Subscribe"
var subscribe_URL	= "/subscribe/subscribe_smallwindow.html"

libjsLoad = true
//alert ("Oracle lib File Loaded")
