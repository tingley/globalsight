
package debex.data;

import com.globalsight.ling.docproc.IFormatNames;

import java.util.Vector;

public class FileTypes
    implements IFormatNames
{
    static public String[] s_formatNames =
    {
        FORMAT_HTML,
        FORMAT_JAVASCRIPT,
        FORMAT_JAVA,
        FORMAT_CSS,
        FORMAT_CSS_STYLE,
        FORMAT_XML,
        FORMAT_XSL,
        FORMAT_JAVAPROP,
        FORMAT_JAVAPROP_HTML,
        FORMAT_JAVAPROP_MSG,
        FORMAT_PLAINTEXT,
        FORMAT_VBSCRIPT,
        FORMAT_CFSCRIPT,
        FORMAT_CF,
        FORMAT_JHTML,
        FORMAT_ASP,
        FORMAT_JSP,
        FORMAT_CPP,
        FORMAT_RTF,
        FORMAT_SGML,

        FORMAT_EXCEL_HTML,
        FORMAT_WORD_HTML,
        FORMAT_POWERPOINT_HTML,

        FORMAT_EBAY_SGML,
        FORMAT_EBAY_PRJ,
    };

    public static Vector getFormatNames()
    {
        Vector result = new Vector();

        for (int i = 0; i < s_formatNames.length; i++)
        {
            result.add(s_formatNames[i]);
        }

        return result;
    }
}
