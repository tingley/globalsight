// Compatibility functions for Internet Explorer's Javascript versions < 5.5.
//
// Array functions from Wrox Professional Javascript book.
//
// See http://www.p2p.wrox.com/archive/javascript/2002-05/2.asp.

if(!Array.prototype.push)
  Array.prototype.push=Array_push;

function Array_push(items)
{
  for(var x=0;x<arguments.length;x++)
    this[this.length]=arguments[x];
  return this.length;
}

if(!Array.prototype.pop)
  Array.prototype.pop=Array_pop;

function Array_pop()
{
  var old=this[this.length-1];
  delete this[this.length-1];
  this.length--;
  return old;
}

if(!Array.prototype.splice)
  Array.prototype.splice=Array_splice;

function Array_splice(start,deleteCount,items)
{
  var results = new Array();
  //copy items to delete and store in return array
  for(var x=0; x<deleteCount; x++)
    results[x] = this[x + start];
  //shift all remaining elements down
  for(x=start; x<this.length - deleteCount; x++)
    this[x] = this[x + deleteCount];
  //remove elements from the end
  this.length -= deleteCount;
  if(arguments.length>2)
  {
    //make room for the provided items to be inserted
    for(x=this.length-1; x>= start; x--)
      this[x + deleteCount] = this[x];
    //insert the provided items
    for(x=0; x<deleteCount; x++)
      this[x + start] = arguments[x + 2];
  }
  return results;
}

if(!Array.prototype.shift)
  Array.prototype.shift=Array_shift;

function Array_shift()
{
  var old=this[0];
  for(var x=0;x<this.length-1;x++)
    this[x]=this[x+1];
  delete this[this.length-1];
  this.length--;
  return old;
}

if(!Array.prototype.unshift||Array(6,6,6,6).unshift(4)!=5)
  Array.prototype.unshift=Array_unshift;

function Array_unshift(items)
{
  //move items up the chain
  for(var x=this.length-1;x>=0;x--)
    this[x+arguments.length]=this[x];
  for(x=0;x<arguments.length;x++)
    this[x]=arguments[x];

  return this.length;
}

if (!String.prototype.startsWith)
  String.prototype.startsWith = String_startsWith;

function String_startsWith(s)
{
    return this.indexOf(s) == 0;
}

function getDomString(node) {
	   if (typeof(XMLSerializer) !== 'undefined') {
	      var serializer = new XMLSerializer();
	      return serializer.serializeToString(node);
	   } else if (node.xml) {
	      return node.xml;
	   }
}

function LTrim(s)
{
   var whitespace = " \t\n\r";

   if (whitespace.indexOf(s.charAt(0)) != -1) {
      // We have a string with leading blank(s)...

      var j=0, i = s.length;

      // Iterate from the far left of string until we
      // don't have any more whitespace...
      while (j < i && whitespace.indexOf(s.charAt(j)) != -1)
         j++;

      // Get the substring from the first non-whitespace
      // character to the end of the string...
      s = s.substring(j, i);
   }

   return s;
}

function RTrim(s)
{
   // We don't want to trip JUST spaces, but also tabs,
   // line feeds, etc.  Add anything else you want to
   // "trim" here in Whitespace
   var whitespace = " \t\n\r";

   if (whitespace.indexOf(s.charAt(s.length-1)) != -1) {
      // We have a string with trailing blank(s)...

      var i = s.length - 1;       // Get length of string

      // Iterate from the far right of string until we
      // don't have any more whitespace...
      while (i >= 0 && whitespace.indexOf(s.charAt(i)) != -1)
         i--;


      // Get the substring from the front of the string to
      // where the last non-whitespace character is...
      s = s.substring(0, i+1);
   }

   return s;
}

function Trim(str)
{
   return RTrim(LTrim(str));
}


function allTrim(s)
{
	if (s != null)
	{
		// Left and right trim
		s = Trim(s);
		// Remove in-line "\t", "\r", "\n".
		for (var i = 0; i < s.length; i++)
		{
			var space = "\t\n\r";
			if (space.indexOf(s.charAt(i)) != -1)
			{
				var m = i;
				if (m == s.length - 2)
				{
					s = s.substring(0, m) + "  " + s[m + 1];
				}
				else
				{
					s = s.substring(0, m) + "  " + s.substring(m + 1, s.length);
				}
			}
		}
	}

	return s;
}

function getUTF8Len(p_string)
{
    var result = 0;
    var c;

    for (var i = 0, max = p_string.length; i < max; i++)
    {
        c = p_string.charAt(i);

        if (c >= 0x0000 && c <= 0x007F)
        {
            result++;
        }
        else if (c > 0x07FF)
        {
            result += 3;
        }
        else
        {
            result += 2;
        }
    }

    return result;
}

function truncateUTF8Len(p_string, p_maxLen)
{
    var result = p_string;
    var len = 0;
    var c;

    for (var i = 0, max = p_string.length; i < max; i++)
    {
        c = p_string.charAt(i);

        if (c >= 0x0000 && c <= 0x007F)
        {
            len++;
        }
        else if (c > 0x07FF)
        {
            len += 3;
        }
        else
        {
            len += 2;
        }

        if (len >= p_maxLen)
        {
            result = result.substring(0, i);
            break;
        }
    }

    return result;
}

// Convert Jquery XML Object to XML String
function xmlObjToString(xmlObj) {
	var xmlString = undefined;
	if (window.ActiveXObject) {
		xmlString = xmlObj[0].xml;
	}

	if (xmlString === undefined) {
		var oSerializer = new XMLSerializer();
		xmlString = oSerializer.serializeToString(xmlObj[0]);
	}

	return xmlString;
}