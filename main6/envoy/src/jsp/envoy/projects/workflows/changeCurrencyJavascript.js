
var aCurrency = new Array();

function parseCurrency()
{	
	var nodes, name, value, i;
    
    if(window.navigator.userAgent.indexOf("MSIE")>0)
    {
       var dom = oCurrency.XMLDocument;
       nodes = dom.selectNodes("/currencyOptions/currency");
       
       for (i = 0; i < nodes.length; ++i)
       {
        node = nodes.item(i);

        if(node.selectSingleNode("name"))
        {
            name = node.selectSingleNode("name").text;
        }
        if(node.selectSingleNode("value"))
        {
            value = node.selectSingleNode("value").text;
        }
        
        aCurrency[aCurrency.length] = new Currency(name, value);
      }

    }
    else
    {      
      nodes  = document.getElementsByTagName("currency");
      for (i = 0; i < nodes.length; ++i)
      {       
        name = nodes[i].firstChild.firstChild;
        value = nodes[i].lastChild.firstChild;

        aCurrency[aCurrency.length] = new Currency(name, value);
      }
    }
}

function doOnLoad()
{
   parseCurrency();
}

function Currency(name, value)
{
	this.name = name;
	this.value = value;
}

function Properties(curr)
{
	this.currentCurrency = curr;
	this.currencyOptions = aCurrency;
}

function changeCurrency(c, url)
{
	if (typeof(c) == "undefined")
	{
		c = "USD"
	}
	var oProperties = new Properties(c);

	var Curr = window.showModalDialog(
	  url, oProperties,
	  "dialogHeight:250px; dialogWidth:400px; center:yes; " +
	  "resizable:no; status:no;");

	 if (Curr != null)
	 {
		 reSetQuoteApprovedDateDefault();
		 oForm.idCurrency.value = Curr;
		 oForm.submit();
	 }
}

function reSetQuoteApprovedDateDefault()
{
    var d = new Date();
  	var quoteEditDateDefaultValue = "0000";
	oForm.quoteApprovedDate.value = quoteEditDateDefaultValue;
	oForm.dateChanged.value = d;
}
