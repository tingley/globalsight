function newId()
{
	//var newid = (new Date()).getTime();
	var newid = (new Date()).getTime() + "" + Math.round(Math.random()*100);
	return newid;
}

function onValueTypeChange()
{
	var obj = document.getElementById("valueType").value;
	if (obj == "VALUE_INPUT")
	{
		document.getElementById("inputValueField").style.display = "";
	}
	else
	{
		document.getElementById("inputValueField").style.display = "none";
		document.getElementById("valueData").value = "";
	}
}

function doAddAttribute()
{
	var attname = document.getElementById("attname").value;
	if (attname == "")
	{
		return;
	}
	var valueType = document.getElementById("valueType").value;
	var valueData = document.getElementById("valueData").value;
	if ("VALUE_INPUT" == valueType && valueData == "")
	{
		alert("Input Value is empty.");
		document.getElementById("valueData").focus();
		return;
	}
	var operator = document.getElementById("operator").value;
	var andOr = document.getElementById("andOr").value;

//	arrayAvailableAttnames.removeData(attname);

	var tmpAtt = new Object();
	tmpAtt.itemid = newId();
	tmpAtt.attributename = attname;
	tmpAtt.operator = operator;
	tmpAtt.valueType = valueType;
	tmpAtt.valueData = valueData;
	tmpAtt.order = ++maxOrder;
	tmpAtt.andOr = andOr;
	arrayTMPAtts[arrayTMPAtts.length] = tmpAtt;
	
	initAttbutesUI();
}

function removeTMPAttribute(itemid)
{
	var removedAttrOrder = 0;
	for (var i = 0; i < arrayTMPAtts.length; i++)
	{
		var tmpatt = arrayTMPAtts[i];
		
		if (tmpatt.itemid == itemid)
		{
			removedAttrOrder = tmpatt.order;
//			arrayAvailableAttnames.appendUniqueObj(tmpatt.attributename);
			arrayTMPAtts.splice(i, 1);
			break;
		}
	}

    // reset the orders from 1
	for (var i = 0; i < arrayTMPAtts.length; i++)
	{
		var tmpatt = arrayTMPAtts[i];
		if (tmpatt.order > removedAttrOrder)
		{
			tmpatt.order = tmpatt.order - 1;
			arrayTMPAtts[i] = tmpatt;
		}
	}

	maxOrder--;

	initAttbutesUI();
}

function setTMPAttributes()
{
    var tmpattstr = new StringBuffer("");
    if (arrayTMPAtts != null && arrayTMPAtts.length > 0)
    {
    	for (var i = 0; i < arrayTMPAtts.length; i++)
    	{
    		var tmpatt = arrayTMPAtts[i];
    		if (tmpatt.valueType)
    		{
	    		tmpattstr.append(tmpatt.attributename);
	    		tmpattstr.append(":");
	    		tmpattstr.append(tmpatt.operator);
	    		tmpattstr.append(":");
	    		tmpattstr.append(tmpatt.valueType);
	    		tmpattstr.append(":");
	    		tmpattstr.append(tmpatt.valueData);
	    		tmpattstr.append(":");
	    		tmpattstr.append(tmpatt.order);
	    		tmpattstr.append(":");
	    		tmpattstr.append(tmpatt.andOr);
	    		tmpattstr.append(",");
    		}
    	}
    }

    // store attributes in "tmpAttributes" hidden object
    document.getElementById("tmpAttributes").value = tmpattstr.toString();
}

function initAttbutesUI()
{
	var objAttnameSelect = document.getElementById("attname");
	var divAtts = document.getElementById("divAtts");
	
	if (objAttnameSelect && divAtts)
	{
		objAttnameSelect.options.length = 0;
		for(var i = 0; i < arrayAvailableAttnames.length; i++)
		{
			var attname = arrayAvailableAttnames[i];
			var varItem = new Option(attname, attname);
			objAttnameSelect.options.add(varItem);
		}

		var ccc = new StringBuffer("<table style='width:100%'>");
		ccc.append("<tr class='thead_tr'>");
		ccc.append("  <td class='thead_td'>And/Or</td>");
		ccc.append("  <td class='thead_td'>Attribute Internal Name</td>");
		ccc.append("  <td class='thead_td'>Operator</td>");
		ccc.append("  <td class='thead_td'>Value</td>");
		ccc.append("  <td class='thead_td'>Order</td>");
		ccc.append("  <td class='thead_td'>Delete</td>");
		ccc.append("</tr>");
		for(var i = 0; i < arrayTMPAtts.length; i++)
		{
			var tmpatt = arrayTMPAtts[i];
			if (tmpatt.valueType)
			{
				var backgroundColor = "#C7CEE0";
				if(i % 2 == 0)
				{
					backgroundColor = "#DFE3EE";
				}
				ccc.append("<tr style='background-color:");
				ccc.append(backgroundColor);
				ccc.append("'>");
				ccc.append("  <td class='standardText'>");
				if (tmpatt.order == 1) {
					ccc.append("");
				} else {
					ccc.append(tmpatt.andOr);
				}
				ccc.append("  </td>");
				ccc.append("  <td class='standardText'>");
				ccc.append(tmpatt.attributename)
				ccc.append("  </td>");
				ccc.append("  <td class='standardText'>");
				ccc.append(tmpatt.operator);
				ccc.append("  </td>");
				ccc.append("  <td class='standardText'>");
				var inputValueData = tmpatt.valueData;
				if (inputValueData != "" && tmpatt.valueType == "VALUE_INPUT")
				{
					inputValueData = inputValueData.replace('<', '&#60;').replace('>', '&#62;');
				}
				ccc.append(tmpatt.valueType == "VALUE_INPUT" ? "[Input Value] " + inputValueData : "From Job Attribute of same name");
				ccc.append("  </td>");
				ccc.append("  <td class='standardText'>");
				ccc.append(tmpatt.order);
				ccc.append("  </td>");
				ccc.append("  <td class='standardText' align='center'><a href='#tuvAttSub' onclick='removeTMPAttribute(\"");
				ccc.append(tmpatt.itemid);
				ccc.append("\")'>X</a></td>");
				ccc.append("</tr>");
			}
		}
		ccc.append("</table>");

		divAtts.innerHTML = ccc.toString();
	}
}