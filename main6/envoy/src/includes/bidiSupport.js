var LRE = "\u202A";
var PDF = "\u202C";

function addBidiMarker(s)
{
  var result = "";
  var c, i = 0, inPtag = 0, len = s.length;

  while (i < len)
  {
    c = s.charAt(i);

    if (c == "[")
    {
      if (i + 1 < len && s.charAt(i + 1) == "[")
      {
	result = result + "[[";
	inPtag = 0;
        ++i;
      }
      else
      {
        result = result + "[" + LRE;
        inPtag = 1;
      }
    }
    else if (c == "]")
    {
      if (inPtag)
      {
        result = result + PDF;
      }

      result = result + "]";
      inPtag = 0;
    }
    else
    {
      result = result + c;
    }

    ++i;
  }

  return result;
}

function removeBidiMarker(s)
{
  var result = "";
  var c;

  for (i = 0; i < s.length; ++i)
  {
    c = s.charAt(i);

    if (c != LRE && c != PDF)
    {
      result = result + c;
    }
  }

  return result;
}
