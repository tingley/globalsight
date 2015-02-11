var ToolBar_Supported = ToolBar_Supported ;
if (ToolBar_Supported != null && ToolBar_Supported == true)
{
	//To Turn on/off Frame support, set Frame_Supported = true/false.
	Frame_Supported = false;

	// Customize default ICP menu color - bgColor, fontColor, mouseoverColor
	setDefaultICPMenuColor("#6699CC", "white", "red");

	// Customize toolbar background color
	setToolbarBGColor("white");

	// display ICP Banner
	setICPBanner("/library/toolbar/images/banner.gif","/isapi/gomscom.asp?target=/","microsoft.com Home") ;
	
	//***** Add ICP menus *****
	//Home
	addICPMenu("HomeMenu", "Home", "","/isapi/gomscom.asp?target=/");
	addICPSubMenu("HomeMenu","microsoft.com Home","/isapi/gomscom.asp?target=/");
	
	//Events & Training
	addICPMenu("EventsMenu", "Events/Training", "","http://events.microsoft.com/isapi/events/default.asp");
	addICPSubMenu("EventsMenu","Events","http://events.microsoft.com/isapi/events/default.asp");
	addICPSubMenu("EventsMenu","Microsoft Press ","http://mspress.microsoft.com/");
	addICPSubMenu("EventsMenu","Online Seminars","/isapi/gomscom.asp?target=/seminar/1033/");
	addICPSubMenu("EventsMenu","Training & Certification","/isapi/gomscom.asp?target=/train_cert/");
 

	//Subscribe
	addICPMenu("SubscribeMenu", "Subscribe", "","/isapi/goregwiz.asp?target=/regsys/pic.asp?sec=0");
	addICPSubMenu("SubscribeMenu","Newsletters","/isapi/goregwiz.asp?target=/regsys/pic.asp?sec=0");
	addICPSubMenu("SubscribeMenu","Free E-Mail Account","http://www.hotmail.com/");
	addICPSubMenu("SubscribeMenu","Messenger Service","http://messenger.msn.com/");
	addICPSubMenu("SubscribeMenu","Manage Your Profile","/isapi/goregwiz.asp?target=/regsys/pic.asp");
	addICPSubMenu("SubscribeMenu","Privacy, Security &amp; Safety","/isapi/gomscom.asp?target=/info/privacy_security.htm");
	
	//About Microsoft
	addICPMenu("MicrosoftMenu", "About Microsoft", "","/isapi/gomscom.asp?target=/mscorp/");
	addICPSubMenu("MicrosoftMenu","Inside Our Site","/isapi/gomscom.asp?target=/backstage/");
	addICPSubMenu("MicrosoftMenu","Company Overview","/isapi/gomscom.asp?target=/mscorp/");
	addICPSubMenu("MicrosoftMenu","Jobs","/isapi/gomscom.asp?target=/jobs/");
	addICPSubMenu("MicrosoftMenu","Piracy","/isapi/gomscom.asp?target=/piracy/");
	addICPSubMenu("MicrosoftMenu","Press News","/isapi/gomscom.asp?target=/presspass/");
	addICPSubMenu("MicrosoftMenu","Investor Relations","/isapi/gomscom.asp?target=/msft/");

	//International
	addICPMenu("InternationalMenu", "US/Worldwide", "","/isapi/gomscom.asp?target=/worldwide/");
	addICPSubMenu("InternationalMenu","Int'l Web Sites & Offices","/isapi/gomscom.asp?target=/worldwide/");
	addICPSubMenu("InternationalMenu","US Web Sites & Offices","/isapi/gomscom.asp?target=/usa/");
	addICPSubMenu("InternationalMenu","MSN Worldwide","http://www.msn.com/wwcon/intl_map.asp");

	//Downloads
	addICPMenu("DownloadMenu", "Downloads", "","/isapi/gomscom.asp?target=/downloads/");
	addICPSubMenu("DownloadMenu","Download Center","/isapi/gomscom.asp?target=/downloads/");
	addICPSubMenu("DownloadMenu","Office Update","http://officeupdate.microsoft.com/");
	addICPSubMenu("DownloadMenu","Windows Update","http://windowsupdate.microsoft.com/");

	//Contact Us
	addICPMenu("ContactMenu", "Contact Us", "","/isapi/goregwiz.asp?target=/contactus/contactus.asp");
	addICPSubMenu("ContactMenu","Contact microsoft.com","/isapi/goregwiz.asp?target=/contactus/contactus.asp");
	addICPSubMenu("ContactMenu","Product Support","/isapi/gosupport.asp?target=/directory/");
	addICPSubMenu("ContactMenu","MSN Support","http://www.msn.com/help/contact.asp");
	addICPSubMenu("ContactMenu","Hotmail Support","http://www.msn.com/help/contact.asp");

	//MSN.COM
	addICPMenu("MSNMenu", "MSN.com", "","http://msn.com/");
	addICPSubMenu("MSNMenu","Entertainment","http://go.msn.com/ZZS/0/D.asp");
	addICPSubMenu("MSNMenu","Free E-mail","http://go.msn.com/ZZS/0/E.asp");
	addICPSubMenu("MSNMenu","Free Games","http://go.msn.com/ZZS/0/B.asp");
	addICPSubMenu("MSNMenu","Free Home Pages","http://go.msn.com/ZZS/0/2.asp");
	addICPSubMenu("MSNMenu","Greeting Cards","http://go.msn.com/ZZS/0/C.asp");
	addICPSubMenu("MSNMenu","Headlines","http://go.msn.com/zzs/0/7.asp");
	addICPSubMenu("MSNMenu","Internet Access","http://go.msn.com/ZZS/0/6.asp");
	addICPSubMenu("MSNMenu","Money","http://go.msn.com/zzs/0/8.asp");
	addICPSubMenu("MSNMenu","People & Chat","http://go.msn.com/ZZS/0/F.asp");
	addICPSubMenu("MSNMenu","Shopping","http://go.msn.com/ZZS/0/9.asp");
	addICPSubMenu("MSNMenu","Today on MSN","http://go.msn.com/ZZS/0/G.asp");
	addICPSubMenu("MSNMenu","More...","http://go.msn.com/ZZS/0/H.asp");
}
