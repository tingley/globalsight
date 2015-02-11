/**
* To use:
* var stringbuffer = new StringBuffer("s");
* stringbuffer.append("t");
* alert(stringbuffer.toString());
*/

function StringBuffer(str)
{
	this.str = str;
}

StringBuffer.prototype.append = function(str)
{
	 this.str = this.str.concat(str);
	 return this.str;
}

StringBuffer.prototype.toString = function()
{
	return this.str.toString();
}

StringBuffer.prototype.indexOf = function(s)
{	
	return this.str.indexOf(s);
}

StringBuffer.prototype.deleteStr = function(s)
{
	this.str = this.str.replace(s, "");
	return this.str;
}

StringBuffer.prototype.replace = function(source, target)
{
	this.str = this.str.replace(source, target);
	return this.str;
}

StringBuffer.prototype.trim = function()
{
	this.str = this.str.replace(/(^\s*)|(\s*$)/g, "");
	return this.str;
}

StringBuffer.prototype.ltrim = function()
{
	this.str = this.str.replace(/(^\s*)/g, "");
	return this.str;
}

StringBuffer.prototype.rtrim = function()
{
	this.str = this.str.replace(/(\s*$)/g, "");
	return this.str;
} 
