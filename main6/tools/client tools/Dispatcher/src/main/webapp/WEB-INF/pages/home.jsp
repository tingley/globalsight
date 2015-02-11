<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>Dispatcher Main Page</title>
<link rel="shortcut icon" href="./images/favicon_globalsight.PNG"/>
<link rel="stylesheet" href="./resources/css/style.css" />
<link rel="stylesheet" href="./resources/jquery/jQueryUI.redmond.css" />
<!--[if lt IE 9]>
<script src="./resources/js/html5shiv.js"></script>
<![endif]-->
<script type="text/javascript" src="./resources/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="./resources/jquery/jquery-ui-1.8.18.custom.min.js"></script>
</head>
<body>
<!-- Form Element -->
<FORM NAME="dForm" METHOD="POST" ACTION="">
<input type="hidden" name="tmProfileID">
</FORM>

	<DIV>
		<header>
			<div class="leftIcon"><img src="./images/welocalizeLogo.png" alt="Logo"></div>
			<nav>
				<ul id="menu">
					<li><a href="./">Home</a></li>
					<li><a href="./mtProfiles/main.htm">MT Profile</a></li>
					<li><a href="./mtpLanguages/main.htm">Languages</a></li>
					<li><a href="./onlineTest/main.htm">Test</a></li>
				</ul>
				<div class="clear"></div>
			</nav>
		</header>

		<div id="content" style="height:200px;">
			<p/>
			Welcome to Dispatcher! <p/>
			Dispatcher will allow you to connect your live chat or forum application to machine translation for instant communication in numerous languages. <p/>
			On the <b>MT Profile</b> page, indicate the name of the MT Engine profile, URL and account options for all MT engines that will be used. <p/>
			On the <b>Languages</b> page, create locales and associate them with MT engines in the MT Profile tab.<p/>
			On the <b>Test</b> page, translate text with specified language.<p/><p/>
		</div>
		
		<%@ include file="/WEB-INF/pages/footer.jspIncl" %>
	</DIV>
</body>
</html>