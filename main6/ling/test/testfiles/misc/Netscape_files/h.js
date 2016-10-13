var WinHeight;
function Pup() {
  if ((parseFloat(navigator.appVersion)>=5)&&((navigator.appName=="Netscape")||(navigator.appName=="Mozilla")))
  {
    WinHeight='370';
  }
  else
  {
    WinHeight='330';
  }
        document.cookie='c=1;';
        if (document.cookie) {
                if ((parseFloat(navigator.appVersion) < 4.06 ) && (document.cookie.indexOf("VeriSign=Warned") == -1) && (navigator.appName.substring(0,8) == "Netscape")) {
                        then = new Date();then.setTime(then.getTime() + 259200000);
                        document.cookie = 'VeriSign=Warned; expires=' + then.toGMTString() + '; domain=.netscape.com;';
                        win=window.open("http://www.netscape.com/misc/snf/popup_cert1.html",'VeriSign','width=355,height=173');
                        if (!win.opener) win.opener=self;
                        if (win.focus) win.focus();
                } else {
                        r=new Date();r=r.getSeconds() % 6;
                        c = new Array("sta1","com19");
                        if (c[r] && document.cookie.indexOf("|"+c[r]) == -1) {
                                win=window.open("http://www.netscape.com/misc/snf/popup_"+c[r]+".html",'tCw','toolbar=no,directories=no,width=330,height='+WinHeight);
                                if (!win.opener) win.opener=self;
                                if (win.focus) win.focus();
                        }
                }
        }
}
