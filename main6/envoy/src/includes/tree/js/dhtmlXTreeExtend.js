function compareStrDistinct(mainArr,valueArr,sChar)
	{		
		var result="";
		for (i = 0; i < valueArr.length; i++)
		{
			var isAdd = true;
			for(j = 0; j < mainArr.length; j++)
			{
				if (mainArr[j] == valueArr[i])
				{		
					isAdd = false;
					break;
				}
			}
			if (isAdd)
			{
				result = result + valueArr[i] + sChar;
			}
		}
		return result;
	}
function compareStrSame(mainArr,valueArr,sChar)
	{		
		var result="";
		for (i = 0; i < valueArr.length; i++)
		{
			for(j = 0; j < mainArr.length; j++)
			{
				if (mainArr[j] == valueArr[i])
				{		
					result = result + valueArr[i] + sChar;
					break;
				}
			}
		}
		return result;
	}

function splitStr(str,sChar)
	{
		toId=str.split(sChar);
		return toId;	
	}
dhtmlXTreeObject.prototype.getAllCheckedLeaf=function()
{
		var checked = this.getAllChecked();
		var leaves = this.getAllLeafs();
		var commas = ",";
		var checkedArr = splitStr(checked,commas);
		var leafArr = splitStr(leaves,commas);
		var result = compareStrSame(checkedArr,leafArr,commas);
		return result;
}

dhtmlXTreeObject.prototype.getAllUcCheckedLeaf=function()
{
		var unchecked = this.getAllUnchecked();
		var leaves = this.getAllLeafs();
		var commas = ",";
		var uncheckedArr = splitStr(unchecked,commas);
		var leafArr = splitStr(leaves,commas);
		var result = compareStrSame(uncheckedArr,leafArr,commas);
		return result;
}

