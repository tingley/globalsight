//-- frame.js: frame builder for www.oracle.com
//-- last updated: 4/25/00


//-- Some default values
var header_size   = 156
var path 		  = location.pathname
var srch 		  = location.search
var def_content   = "content.html"
var def_header	  = '00'

//-- Build directory paths and file locations
var content 	= srch.substring(1, srch.length)
if (content == "") content = def_content

var header		  = top.header
if (!header) header = def_header

if (langjsLoad) var header_path = "/admin/headers/header.html"
else {
	readInfoCookie()
	var name = getName(user_info)
	var header_path	 = "/admin/headers/" + top.header + ".html"
	var header_size = 125
}


//-- Build the FRAMESET
	var block = "";
	block += '<FRAMESET ROWS="' + header_size + ', *"  FRAMEBORDER=0 FRAMESPACING=0 BORDER=0>\n';
	block += '     <FRAME SRC="' + header_path + '" NAME=header SCROLLING=NO NORESIZE MARGINWIDTH=0 MARGINHEIGHT=0>\n';
	block += '     <FRAME SRC="' + content + '" NAME=content MARGINWIDTH=5 MARGINHEIGHT=0 SCROLLING=AUTO>\n'
	block += '</FRAMESET>\n'
	document.write(block);
	document.close();
