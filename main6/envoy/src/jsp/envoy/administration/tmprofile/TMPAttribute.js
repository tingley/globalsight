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
	var operator = document.getElementById("operator").value;
	var valueType = document.getElementById("valueType").value;
	var valueData = document.getElementById("valueData").value;
	var penalty = 0;
	
	//alert(attname);
	
	if (attname == "")
	{
		return;
	}
	
	arrayAvailableAttnames.removeData(attname);
	var tmpAtt = new Object();
	tmpAtt.itemid = newId();
	tmpAtt.attributename = attname;
	tmpAtt.operator = operator;
	tmpAtt.valueType = valueType;
	tmpAtt.valueData = valueData;
	tmpAtt.penalty = penalty;
	arrayTMPAtts[arrayTMPAtts.length] = tmpAtt;
	
	initAttbutesUI();
}

function removeTMPAttribute(itemid)
{
	for (var i = 0; i < arrayTMPAtts.length; i++)
	{
		var tmpatt = arrayTMPAtts[i];
		
		if (tmpatt.itemid == itemid)
		{
			arrayAvailableAttnames.appendUniqueObj(tmpatt.attributename);
			arrayTMPAtts.splice(i, 1);
			break;
		}
	}
	
	initAttbutesUI();
}

function checkPenalty(objId, oldValue)
{
	var vvv = document.getElementById(objId).value;
	var useOldValue = false;
	if (isEmptyString(vvv))
	{
		useOldValue = true;
	}
	else if (!isAllDigits(vvv))
	{
		alert(msgAlertDigit);
		useOldValue = true;
	}
	else if (!checkIsVaildPercent(vvv))
	{
		useOldValue = true;
	}
	
	if (useOldValue)
	{
		document.getElementById(objId).value = oldValue;
	}
}

function setTMPAttributes()
{
	var objtmpAttributes = document.getElementById("tmpAttributes");
    var tmpattstr = new StringBuffer("");
    if (arrayTMPAtts != null && arrayTMPAtts.length > 0)
    {
    	for (var i = 0; i < arrayTMPAtts.length; i++)
    	{
    		var tmpatt = arrayTMPAtts[i];
    		if (tmpatt.valueType)
    		{
    			var objPenalty = document.getElementById("attPenalty_" + tmpatt.itemid);
    			var penalty = (objPenalty) ? objPenalty.value : tmpatt.penalty;
    			
	    		tmpattstr.append(tmpatt.attributename);
	    		tmpattstr.append(":");
	    		tmpattstr.append(tmpatt.operator);
	    		tmpattstr.append(":");
	    		tmpattstr.append(tmpatt.valueType);
	    		tmpattstr.append(":");
	    		tmpattstr.append(tmpatt.valueData);
	    		tmpattstr.append(":");
	    		tmpattstr.append(penalty);
	    		tmpattstr.append(",");
    		}
    	}
    }
    objtmpAttributes.value = tmpattstr.toString();
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
	ccc.append("<tr class='thead_tr'><td class='thead_td'>Attribute Internal Name</td><td class='thead_td'>Operator</td><td class='thead_td'>Value</td><td class='thead_td'>Penalty</td><td class='thead_td'>Delete</td></tr>");
	for(var i = 0; i < arrayTMPAtts.length; i++)
	{
		var tmpatt = arrayTMPAtts[i];
		if (tmpatt.valueType)
		{
			var penaltyId = "attPenalty_" + tmpatt.itemid;
			var oldPenalty = document.getElementById(penaltyId) ? document.getElementById(penaltyId).value : tmpatt.penalty;
			var backgroundColor = "#C7CEE0";
			if(i % 2 == 0)
			{
				backgroundColor = "#DFE3EE";
			}
			ccc.append("<tr style='background-color:");
			ccc.append(backgroundColor);
			ccc.append("'><td class='standardText'>");
			ccc.append(tmpatt.attributename);
			ccc.append("</td><td class='standardText'>");
			ccc.append(tmpatt.operator);
			ccc.append("</td><td class='standardText'>");
			ccc.append(tmpatt.valueType == "VALUE_INPUT" ? "[Input Value] " + tmpatt.valueData : "From Job Attribute of same name");
			ccc.append("</td><td class='standardText'><input type='text' SIZE='1' MAXLENGTH='3' onblur='checkPenalty(\"");
			ccc.append(penaltyId);
			ccc.append("\",");
			ccc.append(oldPenalty);
			ccc.append(")' id='");
			ccc.append(penaltyId);
			ccc.append("' value='");
			ccc.append(oldPenalty);
			ccc.append("'/>%</td><td class='standardText' align='center'><a href='#tuvAttSub' onclick='removeTMPAttribute(\"");
			ccc.append(tmpatt.itemid);
			ccc.append("\")'>X</a></td></tr>");
		}
	}
	ccc.append("</table>");
	
	divAtts.innerHTML = ccc.toString();
	}
}