<%@ page contentType="text/css; charset=UTF-8" errorPage="/envoy/common/error.jsp" session="false"%>
<%
response.setHeader("Cache-Control", "public");
%>
<jsp:useBean id="skin" class="com.globalsight.everest.webapp.javabean.SkinBean" scope="application"/>
.webfx-menu, .webfx-menu * {
	/*
	Set the box sizing to content box
	in the future when IE6 supports box-sizing
	there will be an issue to fix the sizes

	There is probably an issue with IE5 mac now
	because IE5 uses content-box but the script
	assumes all versions of IE uses border-box.

	At the time of this writing mozilla did not support
	box-sizing for absolute positioned element.

	Opera only supports content-box
	*/
	box-sizing:			content-box;
	-moz-box-sizing:	content-box;
}

.webfx-menu {
	position:			absolute;
	z-index:			100;
	visibility:			hidden;
	width:				100px;
	border:				1px solid <%=skin.getProperty("skin.menu.bgColor")%>; 
	padding:			1px;
	color:			<%=skin.getProperty("skin.menu.color")%>;
	background:	    <%=skin.getProperty("skin.menu.bgColor")%>;
	
	filter:				progid:DXImageTransform.Microsoft.Shadow(color="#777777", Direction=135, Strength=4)
						alpha(Opacity=100);
	-moz-opacity:		1.0;
}

.webfx-menu-empty {
	display:			block;
	border:				1px solid white;
	padding:			2px 5px 2px 5px;
	font-family:	Arial, Helvetica, sans-serif;
	font-size:		8pt;
	color:			<%=skin.getProperty("skin.menu.color")%>;
}

.webfx-menu a {
	display:			block;
	width:				expression(constExpression(ieBox ? "100%": "auto"));	/* should be ignored by mz and op */
	height:				expression(constExpression(ie7 ? "13px" : "1px"));
	overflow:			visible;
	padding:			2px 0px 2px 5px;
	font-family:	Arial, Helvetica, sans-serif;
	font-size:		8pt;
	text-decoration:	none;
	vertical-align:		center;
	color:			<%=skin.getProperty("skin.menu.color")%>;
	border:				1px solid <%=skin.getProperty("skin.menu.bgColor")%>; 
}

.webfx-menu a:visited,
.webfx-menu a:visited:hover {
	color:			<%=skin.getProperty("skin.menu.color")%>;
}

.webfx-menu a:hover {
	color:			<%=skin.getProperty("skin.menu.color")%>;
	background:		HighLight; 

	border:			1px solid <%=skin.getProperty("skin.menu.bgColor")%>;
}

.webfx-menu a .arrow {
	float:			right;
	border:			0;
	width:			3px;
	margin-right:	3px;
	margin-top:		4px;
}

/* separtor */
.webfx-menu div {
	height:			0;
	height:			expression(constExpression(ieBox ? "2px" : "0"));
	border-top:		1px solid <%=skin.getProperty("skin.menu.bgColor")%>;
	border-bottom:	1px solid rgb(234,242,255);
	overflow:		hidden;
	margin:			2px 0px 2px 0px;
	font-size:		0mm;
}

.webfx-menu-bar {
    background:	    <%=skin.getProperty("skin.menu.bgColor")%>;
    color:			<%=skin.getProperty("skin.menu.color")%>;

	padding:		2px;
	
	font-family:	Arial, Helvetica, sans-serif;
	font-size:		8pt;
	font-weight:    bold;
	
	/* IE5.0 has the wierdest box model for inline elements */
	padding:		expression(constExpression(ie50 ? "0px" : "2px"));
}

.webfx-menu-bar a,
.webfx-menu-bar a:visited {
	border:				1px solid <%=skin.getProperty("skin.menu.bgColor")%>; 
	padding:			1px 5px 1px 5px;
	color:			<%=skin.getProperty("skin.menu.color")%>;
	text-decoration:	none;

	/* IE5.0 Does not paint borders and padding on inline elements without a height/width */
	height:		expression(constExpression(ie50 ? "17px" : "auto"));
}

.webfx-menu-bar a:hover {
	color:			<%=skin.getProperty("skin.menu.color")%>;
	background:		rgb(120,172,255);
	border-left:	1px solid rgb(234,242,255);
	border-right:	1px solid rgb(0,66,174);
	border-top:		1px solid rgb(234,242,255);
	border-bottom:	1px solid rgb(0,66,174);
}

.webfx-menu-bar a .arrow {
	border:			0;
	float:			none;
}

.webfx-menu-bar a:active, .webfx-menu-bar a:focus {
	-moz-outline:	none;
	outline:		none;
	ie-dummy:		expression(this.hideFocus=true);
	border-left:	1px solid rgb(0,66,174);
	border-right:	1px solid rgb(234,242,255);
	border-top:		1px solid rgb(0,66,174);
	border-bottom:	1px solid rgb(234,242,255);
}
