Array.prototype.contains = function(oneItem)
{
	var index = dojo.indexOf(this, oneItem);
	return index != -1;
}

Array.prototype.appendUniqueObj = function(oneItem)
{
	if (!this.contains(oneItem))
	{
		this[this.length] = oneItem;
	}
}

Array.prototype.removeData = function(oneItem)
{
	var index = dojo.indexOf(this, oneItem);
	
	if (index != -1)
	{
		return this.splice(index, 1);
	}
	
	return this;
}

Array.prototype.alertMe = function()
{
	var s = this.join(", ");
	alert(s);
}