<!--
  var appVer = navigator.appVersion;
  var msie = appVer.indexOf("MSIE ");
  var msieWin31 = (appVer.indexOf("Windows 3.1") >= 0), 
          isMac = (appVer.indexOf("Macintosh") >= 0);
  var ver = 0;
  if (msie >= 0)
    ver = parseFloat(appVer.substring(msie+5, appVer.indexOf (";", msie)));
  else
    ver = parseInt(appVer);

  path = "./imation_files/error.htm";

  if( msie>=0 && ( (isMac && ver>=5)||(!isMac && ver>=4) ) )
    window.location.replace('./imation_files/slide0001.htm'+document.location.hash);
  else
    {
    if (!msieWin31 && ((msie >= 0 && ver >= 3.02) || (msie < 0 && ver >= 3 )))
      window.location.replace( path );
    else
      window.location.href = path;
    }    
//-->
