<%@ page contentType="text/css; charset=UTF-8" errorPage="/envoy/common/error.jsp" session="false"%>
<%
response.setHeader("Cache-Control", "public");
%>
<jsp:useBean id="skin" class="com.globalsight.everest.webapp.javabean.SkinBean" scope="application"/>
/* menu body */

.menu-body {
	background-color:	<%=skin.getProperty("skin.menu.bgColor")%>;
	color:				<%=skin.getProperty("skin.menu.color")%>;
	margin:				0;
	padding:			0;
	overflow:			hidden;
	border:				0;
	cursor:				default;
}

.menu-body .outer-border {
	border:			1px solid;
	border-color:	        ThreeDLightShadow ThreeDDarkShadow
				ThreeDDarkShadow ThreeDLightShadow;
}

.menu-body .inner-border {
	border:			1px solid;
	border-color:	        ThreeDHighLight ThreeDShadow
				ThreeDShadow ThreeDHighLight;
	padding:		1px;
	width:			100%;
	height:			100%;
}

/* end menu body */

/*****************************************************************************/

/* menu items */

.menu-body td {
        font: menu;
}

.menu-body .hover {
	background-color:	HighLight;
	color:			white;
}

.menu-body td.empty-icon-cell {
	padding:	0px 2px 0px 2px;
	border:		0;
}

.menu-body td.empty-icon-cell span {
	width:	16px;
}

.menu-body td.icon-cell {
	padding:	0px 2px 0px 2px;
	border:		0;
}


.menu-body td.icon-cell img {
	width:	16px;
	height:	16px;
	margin:	0;
}

.menu-body td.label-cell {
	width:		100%;
	padding:	0px 5px 0px 5px;
}

.menu-body td.shortcut-cell {
	padding:	0px 5px 0px 5px;
}

.menu-body td.arrow-cell {
	width:			20px;
	padding:		0px 2px 0px 0px;
	font-family:	Webdings;
	font-size:		80%;
}

/* end menu items */

/*****************************************************************************/

/* disabled items */

.menu-body .disabled .disabled-container {
	color:				GrayText;
}

.menu-body .disabled .icon-cell .disabled-container,
.menu-body .disabled-hover .icon-cell .disabled-container {
	background:			GrayText;
	filter:				progid:DXImageTransform.Microsoft.Chroma(Color=#010101)
						DropShadow(color=ButtonHighlight, offx=1, offy=1);
	width:				100%;
	height:				100%;
}

.menu-body .disabled-hover .icon-cell .disabled-container {
	filter:				progid:DXImageTransform.Microsoft.Chroma(Color=#010101);
}

.menu-body .disabled .icon-cell .disabled-container .disabled-container,
.menu-body .disabled-hover .icon-cell .disabled-container .disabled-container {
	background:			Transparent;
	filter:				Mask(Color=#010101);
}
.menu-body .disabled-hover td {
	background-color:	Highlight;
	color:				GrayText;
}
/* end disabled items */

/*****************************************************************************/

/* separator */

.menu-body td.separator {
	font-size:	0.001mm;
	padding:	4px 10px 4px 10px;
}

.menu-body .separator-line {
	overflow:		hidden;
	border-top:		1px solid ThreeDShadow;
	border-bottom:	1px solid ThreeDHighlight;
	height:			2px;
}

/* end separator */

/*****************************************************************************/

/* Scroll buttons */

.menu-body #scroll-up-item td,
.menu-body #scroll-down-item td {
	font-family:	Webdings !important;
	text-align:		center;
	padding:		10px;
}
.menu-body #scroll-up-item,
.menu-body #scroll-down-item {
	width:		100%;
}

.menu-body #scroll-up-item td,
.menu-body #scroll-down-item td {
	font-family:	Webdings;
	text-align:		center;
	padding:		0px 5px 0px 5px;
	font-size:		10px;
}

/* end scroll buttons */

/*****************************************************************************/

/* radio and check box items */

.menu-body .check-box,
.menu-body .radio-button {
	width:			16px;
	text-align:		center;
	vertical-align:	middle;
}

.menu-body .checked .check-box {
	font-family:	Marlett;
	font-size:	150%;
}

.menu-body .checked .radio-button {
	font-family:	Marlett;
	font-size:		66%;
}

/* end radio and check box items */

/*****************************************************************************/

/* Menu Bar */

.menu-bar {
	background:		<%=skin.getProperty("skin.menu.bgColor")%>;
	cursor:			default;
	padding:		1px;
}

.menu-bar .menu-button {
	background:	<%=skin.getProperty("skin.menu.bgColor")%>;
	color:		<%=skin.getProperty("skin.menu.color")%>;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 8pt;
    font-weight: bold;
	padding:	3px 7px 3px 7px;
	border:		0;
	margin:		0;
	display:	inline-block;
	white-space:	nowrap;
	cursor:			default;
}

.menu-bar .menu-button.active {
	padding:		3px 5px 1px 7px;
	border:			1px solid;
	border-color:	ButtonShadow ButtonHighlight
					ButtonHighlight ButtonShadow;
}

.menu-bar .menu-button.hover {
	padding:		2px 6px 2px 6px;
	border:			1px solid;
	border-color:	ButtonHighlight ButtonShadow
					ButtonShadow ButtonHighlight;
}

/* End Menu Bar */
