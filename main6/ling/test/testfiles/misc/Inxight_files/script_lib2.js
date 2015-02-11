// check for platform and apply the appropriate stylesheet
	if (navigator.appVersion.indexOf("Mac") != -1) {
	    document.write('\<link rel=\"stylesheet\"href=\"/styles/mac.css\"\>');
	}
	else {
	    document.write('\<link rel=\"stylesheet\"href=\"/styles/win.css\"\>');
	}
// end apply stylesheet

// image rollovers and status bar content
	function navProperties(imgSrc, status)
	{
		if (document.images)
			{
				this.image = new Image();
				this.image.src = imgSrc;
				this.status = status;
			}
	}

	if (document.images)
	{
		defaultStatus = 'Inxight Software, Inc.';
		
		rollovers = new Array();
		
		rollovers.wb_off = new navProperties( '/images/nav_wb_off.gif', defaultStatus );
		rollovers.wb_on = new navProperties( '/images/nav_wb_on.gif', 'products for web builders - web masters, site developers and portal builders' );
		
		rollovers.sp_off = new navProperties( '/images/nav_sp_off.gif', defaultStatus );
		rollovers.sp_on = new navProperties( '/images/nav_sp_on.gif', 'products for software producers - OEM and corporate application developers' );
		
		rollovers.eu_off = new navProperties( '/images/nav_eu_off.gif', defaultStatus );
		rollovers.eu_on = new navProperties( '/images/nav_eu_on.gif', 'products for end users - cutting-edge visual navigation and exploration tools' );
		
		rollovers.about_off = new navProperties( '/images/nav_about_off.gif', defaultStatus );
		rollovers.about_on = new navProperties( '/images/nav_about_on.gif', 'about inxight - customers, management team and contact information' );
		
		rollovers.programs_off = new navProperties( '/images/nav_programs_off.gif', defaultStatus );
		rollovers.programs_on = new navProperties( '/images/nav_programs_on.gif', 'programs - partner program, featured partner, partner press, online application, ECHO' );
		
		rollovers.news_off = new navProperties( '/images/nav_news_off.gif', defaultStatus );
		rollovers.news_on = new navProperties( '/images/nav_news_on.gif', 'news &amp; events - press releases, events, in the news, research papers and press kit' );
		
		rollovers.jobs_off = new navProperties( '/images/nav_jobs_off.gif', defaultStatus );
		rollovers.jobs_on = new navProperties( '/images/nav_jobs_on.gif', 'employment - what we offer, apply online, sales, engineering, operations, finance and marketing' );
		
		rollovers.contact_off = new navProperties( '/images/nav_contact_off.gif', defaultStatus );
		rollovers.contact_on = new navProperties( '/images/nav_contact_on.gif', 'contact inxight - address, email, phone, fax and directions' );
		
		rollovers.map_off = new navProperties( '/images/nav_map_off.gif', defaultStatus );
		rollovers.map_on = new navProperties( '/images/nav_map_on.gif', 'site map - explore this site using hyperbolic tree technology' );
	}

	function swap(which, state) 
	{
		if (document.images)
			{
				document[which].src = rollovers[which + '_' + state].image.src;
				window.status = rollovers[which + '_' + state].status;
				return true;
			}
	}
// end image rollovers

// SLS Demo links

		var vers=navigator.appVersion
		var MacMSIE=false;

		if (((vers.indexOf("MSIE"))!=-1) && (vers.indexOf("ac")!=-1))
        	MacMSIE=true;

		function launchMap(mac,non_mac) 
		{
    		window.name="HT";
        	if (MacMSIE == true)
  				window.open(mac, "New_Title", "resizable=0,scrollbars=0,toolbar=0,location=0,menubar=0,status=1,width=550,height=550");
        	else
  				window.open(non_mac, "New_Title", "resizable=1,scrollbars=0,toolbar=0,location=0,menubar=0,status=1,width=550,height=550");
		}
// end SLS demo links
