//-- lang.js: Country/language specific JS data file
//-- last updated: 7/18/00

readInfoCookie()
var name = getName(user_info)
var toprow = new Array(10)
for (var i = 0; i < toprow.length; i++ ) toprow[i] = new item(i);

var tab = new Array(15)
for (var i = 0; i < tab.length; i++ ) tab[i] = new item(i);

function item(Id){
  this.id = Id, this.label = '', this.url = '', this.image = '', this.target = '_top'
}
///-- DON'T TOUCH ABOVE THIS LINE

var strings = new Object()
strings.language_root  	= ''
strings.print_label    	= "Printer View"
strings.mail_label     	= "Tell a Friend"
strings.mail_URL		= "/admin/account/mail.html"
strings.rate_label     	= "Rate This Page"
strings.rate_URL		= "/admin/account/rate.html"
strings.salesrep_label  = "Contact a Sales rep"
strings.salesrep_URL	= "/admin/account/sales.html"
strings.subscribe_label	= "Subscribe"
strings.subscribe_URL	= "/subscribe/subscribe_smallwindow.html"
strings.signin_label	= "Click Here"
strings.signin_URL		= "/admin/account/index.html"
strings.signout_label	= "Sign Out"
strings.ident_label    	= "Welcome " + name
strings.mem1_label     	= "If you are not " + name + ", "
strings.mem2_label     	= " to register for a free Oracle Web account"

toprow[1].label = 'Products A-Z'
toprow[1].url = '/products/index.html'
toprow[1].image = '/admin/images/headers/products.gif'
toprow[2].label = 'Oracle Store'
toprow[2].url = 'http://oraclestore.oracle.com/'
toprow[2].image = '/admin/images/headers/shopcart.gif'
toprow[3].label = 'Downloads'
toprow[3].url = 'http://technet.oracle.com/software/'
toprow[3].image = '/admin/images/headers/download.gif'
toprow[4].label = 'My Profile'
toprow[4].url = '/admin/account/index.html'
toprow[4].image = '/admin/images/headers/profile.gif'
toprow[5].label = 'Search'
toprow[5].url = 'javascript:document.SearchForm.submit();'
toprow[5].image = '/admin/images/headers/maglass.gif'
toprow[6].label = ''
toprow[6].target = 'content'
toprow[6].url = ora_host + search_dad + 'search2'
toprow[6].image = '[Search Text]'
TopMaxVal = 6;

tab[1].label = 'Database'
tab[1].url = '/database/index.html'
tab[2].label = 'Applications'
tab[2].url = '/applications/index.html'
tab[3].label = 'Tools'
tab[3].url = '/tools/index.html'
tab[4].label = 'Support'
tab[4].url = '/support/index.html'
tab[5].label = 'Education'
tab[5].url = 'http://education.oracle.com/'
tab[6].label = 'Consulting'
tab[6].url = '/consulting/index.html'
tab[7].label = 'Employment'
tab[7].url = '/corporate/employment/index.html'
tab[8].label = 'Partners'
tab[8].url = '/partners/index.html'
tab[9].label = 'About Oracle'
tab[9].url = '/corporate/index.html'
TabMaxVal = 9;

var langjsLoad = true
//alert("Language File Loaded")

