package com.globalsight.ling.common;

import java.util.HashMap;

public class MapOfHtmlEntity 
{
   // private final static HashMap<Character, String> MapOfCharacterEntityName = new HashMap<Character, String>();
    private final static HashMap<String, String> MapOfEntityNumEntityName = new HashMap<String, String>();
    static
    {
        //Reserved Characters in HTML
//        MapOfEntityNumEntityName.put("&#34;", "&quot;");
//        MapOfEntityNumEntityName.put("&#39;", "&apos;");
//        MapOfEntityNumEntityName.put("&#38;", "&amp;");
//        MapOfEntityNumEntityName.put("&#60;", "&lt;");
//        MapOfEntityNumEntityName.put("&#62;", "&gt;");
        
        //ISO 8859-1 Symbols
        MapOfEntityNumEntityName.put("&#160;", "&nbsp;");
        MapOfEntityNumEntityName.put("&#161;", "&iexcl;");
        MapOfEntityNumEntityName.put("&#162;", "&cent;");
        MapOfEntityNumEntityName.put("&#163;", "&pound;");
        MapOfEntityNumEntityName.put("&#164;", "&curren;");
        MapOfEntityNumEntityName.put("&#165;", "&yen;");
        MapOfEntityNumEntityName.put("&#166;", "&brvbar;");
        MapOfEntityNumEntityName.put("&#167;", "&sect;");
        MapOfEntityNumEntityName.put("&#168;", "&uml;");
        MapOfEntityNumEntityName.put("&#169;", "&copy;");
        MapOfEntityNumEntityName.put("&#170;", "&ordf;");
        MapOfEntityNumEntityName.put("&#171;", "&laquo;");
        MapOfEntityNumEntityName.put("&#172;", "&not;");
        MapOfEntityNumEntityName.put("&#173;", "&shy;");
        MapOfEntityNumEntityName.put("&#174;", "&reg;");
        MapOfEntityNumEntityName.put("&#175;", "&macr;");
        MapOfEntityNumEntityName.put("&#176;", "&deg;");
        MapOfEntityNumEntityName.put("&#177;", "&plusmn;");
        MapOfEntityNumEntityName.put("&#178;", "&sup2;");
        MapOfEntityNumEntityName.put("&#179;", "&sup3;");
        MapOfEntityNumEntityName.put("&#180;", "&acute;");
        MapOfEntityNumEntityName.put("&#181;", "&micro;");
        MapOfEntityNumEntityName.put("&#182;", "&para;");
        MapOfEntityNumEntityName.put("&#183;", "&middot;");
        MapOfEntityNumEntityName.put("&#184;", "&cedil;");
        MapOfEntityNumEntityName.put("&#185;", "&sup1;");
        MapOfEntityNumEntityName.put("&#186;", "&ordm;");
        MapOfEntityNumEntityName.put("&#187;", "&raquo;");
        MapOfEntityNumEntityName.put("&#188;", "&frac14;");
        MapOfEntityNumEntityName.put("&#189;", "&frac12;");
        MapOfEntityNumEntityName.put("&#190;", "&frac34;");
        MapOfEntityNumEntityName.put("&#191;", "&iquest;");
        MapOfEntityNumEntityName.put("&#215;", "&times;");
        MapOfEntityNumEntityName.put("&#247;", "&divide;");
        
        //ISO 8859-1 Characters
        MapOfEntityNumEntityName.put("&#192;", "&Agrave;");
        MapOfEntityNumEntityName.put("&#193;", "&Aacute;");
        MapOfEntityNumEntityName.put("&#194;", "&Acirc;");
        MapOfEntityNumEntityName.put("&#195;", "&Atilde;");
        MapOfEntityNumEntityName.put("&#196;", "&Auml;");
        MapOfEntityNumEntityName.put("&#197;", "&Aring;");
        MapOfEntityNumEntityName.put("&#198;", "&AElig;");
        MapOfEntityNumEntityName.put("&#199;", "&Ccedil;");
        MapOfEntityNumEntityName.put("&#200;", "&Egrave;");
        MapOfEntityNumEntityName.put("&#201;", "&Eacute;");
        MapOfEntityNumEntityName.put("&#202;", "&Ecirc;");
        MapOfEntityNumEntityName.put("&#203;", "&Euml;");
        MapOfEntityNumEntityName.put("&#204;", "&Igrave;");
        MapOfEntityNumEntityName.put("&#205;", "&Iacute;");
        MapOfEntityNumEntityName.put("&#206;", "&Icirc;");
        MapOfEntityNumEntityName.put("&#207;", "&Iuml;");
        MapOfEntityNumEntityName.put("&#208;", "&ETH;");
        MapOfEntityNumEntityName.put("&#209;", "&Ntilde;");
        MapOfEntityNumEntityName.put("&#210;", "&Ograve;");
        MapOfEntityNumEntityName.put("&#211;", "&Oacute;");
        MapOfEntityNumEntityName.put("&#212;", "&Ocirc;");
        MapOfEntityNumEntityName.put("&#213;", "&Otilde;");
        MapOfEntityNumEntityName.put("&#214;", "&Ouml;");
        MapOfEntityNumEntityName.put("&#216;", "&Oslash;");
        MapOfEntityNumEntityName.put("&#217;", "&Ugrave;");
        MapOfEntityNumEntityName.put("&#218;", "&Uacute;");
        MapOfEntityNumEntityName.put("&#220;", "&Uuml;");
        MapOfEntityNumEntityName.put("&#221;", "&Yacute;");
        MapOfEntityNumEntityName.put("&#222;", "&THORN;");
        MapOfEntityNumEntityName.put("&#223;", "&szlig;");
        MapOfEntityNumEntityName.put("&#224;", "&agrave;");
        MapOfEntityNumEntityName.put("&#225;", "&aacute;");
        MapOfEntityNumEntityName.put("&#226;", "&acirc;");
        MapOfEntityNumEntityName.put("&#227;", "&atilde;");
        MapOfEntityNumEntityName.put("&#228;", "&auml;");
        MapOfEntityNumEntityName.put("&#229;", "&aring;");
        MapOfEntityNumEntityName.put("&#230;", "&aelig;");
        MapOfEntityNumEntityName.put("&#231;", "&ccedil;");
        MapOfEntityNumEntityName.put("&#232;", "&egrave;");
        MapOfEntityNumEntityName.put("&#233;", "&eacute;");
        MapOfEntityNumEntityName.put("&#234;", "&ecirc;");
        MapOfEntityNumEntityName.put("&#235;", "&euml;");
        MapOfEntityNumEntityName.put("&#236;", "&igrave;");
        MapOfEntityNumEntityName.put("&#237;", "&iacute;");
        MapOfEntityNumEntityName.put("&#238;", "&icirc;");
        MapOfEntityNumEntityName.put("&#239;", "&iuml;");
        MapOfEntityNumEntityName.put("&#240;", "&eth;");
        MapOfEntityNumEntityName.put("&#241;", "&ntilde;");
        MapOfEntityNumEntityName.put("&#242;", "&ograve;");
        MapOfEntityNumEntityName.put("&#243;", "&oacute;");
        MapOfEntityNumEntityName.put("&#244;", "&ocirc;");
        MapOfEntityNumEntityName.put("&#245;", "&otilde;");
        MapOfEntityNumEntityName.put("&#246;", "&ouml;");
        MapOfEntityNumEntityName.put("&#248;", "&oslash;");
        MapOfEntityNumEntityName.put("&#249;", "&ugrave;");
        MapOfEntityNumEntityName.put("&#250;", "&uacute;");
        MapOfEntityNumEntityName.put("&#251;", "&ucirc;");
        MapOfEntityNumEntityName.put("&#252;", "&uuml;");
        MapOfEntityNumEntityName.put("&#253;", "&yacute;");
        MapOfEntityNumEntityName.put("&#254;", "&thorn;");
        MapOfEntityNumEntityName.put("&#255;", "&yuml;");
    }
//    static
//    {
//        //Reserved Characters in HTML
//        MapOfCharacterEntityName.put('&',"&amp;");
//        MapOfCharacterEntityName.put('\"', "&quot;");
//        MapOfCharacterEntityName.put('\'', "&apos;");
//        MapOfCharacterEntityName.put('<', "&lt;");
//        MapOfCharacterEntityName.put('>', "&gt;");
//        
//        //ISO 8859-1 Symbols
//        //HtmlEntity.put(' ',"&nbsp;");
//        MapOfCharacterEntityName.put('¡',"&iexcl;");
//        MapOfCharacterEntityName.put('¢',"&cent;");
//        MapOfCharacterEntityName.put('£',"&pound;");
//        MapOfCharacterEntityName.put('¤',"&curren;");
//        MapOfCharacterEntityName.put('¥',"&yen;");
//        MapOfCharacterEntityName.put('¦',"&brvbar;");
//        MapOfCharacterEntityName.put('§',"&sect;");
//        MapOfCharacterEntityName.put('¨',"&uml;");
//        MapOfCharacterEntityName.put('©',"&copy;");
//        MapOfCharacterEntityName.put('ª',"&ordf;");
//        MapOfCharacterEntityName.put('«',"&laquo;");
//        MapOfCharacterEntityName.put('¬',"&not;");
//        MapOfCharacterEntityName.put('®',"&reg;");
//        MapOfCharacterEntityName.put('¯',"&macr;");
//        MapOfCharacterEntityName.put('°',"&deg;");
//        MapOfCharacterEntityName.put('±',"&plusmn;");
//        MapOfCharacterEntityName.put('²',"&sup2;");
//        MapOfCharacterEntityName.put('³',"&sup3;");
//        MapOfCharacterEntityName.put('´',"&acute;");
//        MapOfCharacterEntityName.put('µ',"&micro;");
//        MapOfCharacterEntityName.put('¶',"&para;");
//        MapOfCharacterEntityName.put('·',"&middot;");
//        MapOfCharacterEntityName.put('¸',"&cedil;");
//        MapOfCharacterEntityName.put('¹',"&sup1;");
//        MapOfCharacterEntityName.put('º',"&ordm;");
//        MapOfCharacterEntityName.put('»',"&raquo;");
//        MapOfCharacterEntityName.put('¼',"&frac14;");
//        MapOfCharacterEntityName.put('½',"&frac12;");
//        MapOfCharacterEntityName.put('¾',"&frac34;");
//        MapOfCharacterEntityName.put('¿',"&iquest;");
//        MapOfCharacterEntityName.put('×',"&times;");
//        MapOfCharacterEntityName.put('÷',"&divide;");
//        
//        //ISO 8859-1 Characters
//        MapOfCharacterEntityName.put('À',"&Agrave;");
//        MapOfCharacterEntityName.put('Á',"&Aacute;");
//        MapOfCharacterEntityName.put('Â',"&Acirc;");
//        MapOfCharacterEntityName.put('Ã',"&Atilde;");
//        MapOfCharacterEntityName.put('Ä',"&Auml;");
//        MapOfCharacterEntityName.put('Å', "&Aring;");
//        MapOfCharacterEntityName.put('Æ', "&AElig;");
//        MapOfCharacterEntityName.put('Ç', "&Ccedil;");
//        MapOfCharacterEntityName.put('È', "&Egrave;");
//        MapOfCharacterEntityName.put('É', "&Eacute;");
//        MapOfCharacterEntityName.put('Ê', "&Ecirc;");
//        MapOfCharacterEntityName.put('Ë', "&Euml;");
//        MapOfCharacterEntityName.put('Ì', "&Igrave;");
//        MapOfCharacterEntityName.put('Í', "&Iacute;");
//        MapOfCharacterEntityName.put('Î', "&Icirc;");
//        MapOfCharacterEntityName.put('Ï', "&Iuml;");
//        MapOfCharacterEntityName.put('Ð', "&ETH;");
//        MapOfCharacterEntityName.put('Ñ', "&Ntilde;");
//        MapOfCharacterEntityName.put('Ò', "&Ograve;");
//        MapOfCharacterEntityName.put('Ó', "&Oacute;");
//        MapOfCharacterEntityName.put('Ô', "&Ocirc;");
//        MapOfCharacterEntityName.put('Õ', "&Otilde;");
//        MapOfCharacterEntityName.put('Ö', "&Ouml;");
//        MapOfCharacterEntityName.put('Ø', "&Oslash;");
//        MapOfCharacterEntityName.put('Ù', "&Ugrave;");
//        MapOfCharacterEntityName.put('Ú', "&Uacute;");
//        MapOfCharacterEntityName.put('Û', "&Ucirc;");
//        MapOfCharacterEntityName.put('Ü', "&Uuml;");
//        MapOfCharacterEntityName.put('Ý', "&Yacute;");
//        MapOfCharacterEntityName.put('Þ', "&THORN;");
//        MapOfCharacterEntityName.put('ß', "&szlig;");
//        MapOfCharacterEntityName.put('à', "&agrave;");
//        MapOfCharacterEntityName.put('á', "&aacute;");
//        MapOfCharacterEntityName.put('â', "&acirc;");
//        MapOfCharacterEntityName.put('ã', "&atilde;");
//        MapOfCharacterEntityName.put('ä', "&auml;");
//        MapOfCharacterEntityName.put('å', "&aring;");
//        MapOfCharacterEntityName.put('æ', "&aelig;");
//        MapOfCharacterEntityName.put('ç', "&ccedil;");
//        MapOfCharacterEntityName.put('è', "&egrave;");
//        MapOfCharacterEntityName.put('é', "&eacute;");
//        MapOfCharacterEntityName.put('ê', "&ecirc;");
//        MapOfCharacterEntityName.put('ë', "&euml;");
//        MapOfCharacterEntityName.put('ì', "&igrave;");
//        MapOfCharacterEntityName.put('í', "&iacute;");
//        MapOfCharacterEntityName.put('î', "&icirc;");
//        MapOfCharacterEntityName.put('ï', "&iuml;");
//        MapOfCharacterEntityName.put('ð', "&eth;");
//        MapOfCharacterEntityName.put('ñ', "&ntilde;");
//        MapOfCharacterEntityName.put('ò', "&ograve;");
//        MapOfCharacterEntityName.put('ó', "&oacute;");
//        MapOfCharacterEntityName.put('ô', "&ocirc;");
//        MapOfCharacterEntityName.put('õ', "&otilde;");
//        MapOfCharacterEntityName.put('ö', "&ouml;");
//        MapOfCharacterEntityName.put('ø', "&oslash;");
//        MapOfCharacterEntityName.put('ù', "&ugrave;");
//        MapOfCharacterEntityName.put('ú', "&uacute;");
//        MapOfCharacterEntityName.put('û', "&ucirc;");
//        MapOfCharacterEntityName.put('ü', "&uuml;");
//        MapOfCharacterEntityName.put('ý', "&yacute;");
//        MapOfCharacterEntityName.put('þ', "&thorn;");
//        MapOfCharacterEntityName.put('ÿ', "&yuml;");
//    } 
    
    public static String getHtmlEntityName(char ch)
    {
        StringBuilder buf = new StringBuilder();
        String s = MapOfEntityNumEntityName.get(toEntityNumber(ch));
        if (s == null)
        {
            if (ch > 0x7F)
            {
                int intValue = ch;
                buf.append("&#");
                buf.append(intValue);
                buf.append(';');
            }
            else
            {
                buf.append(ch);
            }
        }
        else
        {
            buf.append(s);
        }
        return buf.toString();
    }
    
    public static String toEntityNumber(char ch)
    {
        StringBuilder buf = new StringBuilder();
        if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9')
        {
          // safe
          buf.append(ch);
        }
        else if (Character.isWhitespace(ch))
        {
          // paranoid version: whitespaces are unsafe - escape
          // conversion of (int)ch is naive
          buf.append("&#").append((int) ch).append(";");
        }
        else if (Character.isISOControl(ch))
        {
          // paranoid version:isISOControl which are not isWhitespace removed !
          // do nothing do not include in output !
        }
        else if (Character.isHighSurrogate(ch))
        {
             buf.append("&#").append(ch).append(";");
        }
        else if(Character.isLowSurrogate(ch))
        {

        }
        else
        {
          if (Character.isDefined(ch))
          {
            // paranoid version
            // the rest is unsafe, including <127 control chars
            buf.append("&#").append((int) ch).append(";");
          }
        }
        return buf.toString();
    }

    public static String escapeHtmlFull(String s)
    {
        StringBuilder b = new StringBuilder(s.length());
         for (int i = 0; i < s.length(); i++)
         {
           char ch = s.charAt(i);
           b.append(MapOfHtmlEntity.getHtmlEntityName(ch));
         }
         return b.toString();
    }
}