
package debex.data;

import java.util.Locale;
import java.util.Vector;

public class Locales
{
    static public Locale[] s_locales =
        {
        Locale.US,
        new Locale("ar", "AE"),
        new Locale("ar", "BH"),
        new Locale("ar", "DZ"),
        new Locale("ar", "EG"),
        new Locale("ar", "IQ"),
        new Locale("ar", "JO"),
        new Locale("ar", "KW"),
        new Locale("ar", "LB"),
        new Locale("ar", "LY"),
        new Locale("ar", "MA"),
        new Locale("ar", "OM"),
        new Locale("ar", "QA"),
        new Locale("ar", "SA"),
        new Locale("ar", "SD"),
        new Locale("ar", "SY"),
        new Locale("ar", "TN"),
        new Locale("ar", "YE"),
        new Locale("be", "BY"),
        new Locale("bg", "BG"),
        new Locale("ca", "ES"),
        new Locale("cs", "CZ"),
        new Locale("da", "DK"),
        new Locale("de", "AT"),
        new Locale("de", "CH"),
        new Locale("de", "DE"),
        new Locale("de", "LU"),
        new Locale("el", "GR"),
        new Locale("en", "AU"),
        new Locale("en", "CA"),
        new Locale("en", "GB"),
        new Locale("en", "IE"),
        new Locale("en", "IN"),
        new Locale("en", "NZ"),
        new Locale("en", "ZA"),
        new Locale("es", "AR"),
        new Locale("es", "BO"),
        new Locale("es", "CL"),
        new Locale("es", "CO"),
        new Locale("es", "CR"),
        new Locale("es", "DO"),
        new Locale("es", "EC"),
        new Locale("es", "ES"),
        new Locale("es", "GT"),
        new Locale("es", "HN"),
        new Locale("es", "MX"),
        new Locale("es", "NI"),
        new Locale("es", "PA"),
        new Locale("es", "PE"),
        new Locale("es", "PR"),
        new Locale("es", "PY"),
        new Locale("es", "SV"),
        new Locale("es", "UY"),
        new Locale("es", "VE"),
        new Locale("et", "EE"),
        new Locale("fi", "FI"),
        new Locale("fr", "BE"),
        new Locale("fr", "CA"),
        new Locale("fr", "CH"),
        new Locale("fr", "FR"),
        new Locale("fr", "LU"),
        new Locale("he", "IL"),
        new Locale("hi", "IN"),
        new Locale("hr", "HR"),
        new Locale("hu", "HU"),
        new Locale("is", "IS"),
        new Locale("it", "CH"),
        new Locale("it", "IT"),
        new Locale("ja", "JP"),
        new Locale("ko", "KR"),
        new Locale("lt", "LT"),
        new Locale("lv", "LV"),
        new Locale("mk", "MK"),
        new Locale("nl", "BE"),
        new Locale("nl", "NL"),
        new Locale("no", "NO"),
        new Locale("pl", "PL"),
        new Locale("pt", "BR"),
        new Locale("pt", "PT"),
        new Locale("ro", "RO"),
        new Locale("ru", "RU"),
        new Locale("sh", "YU"),
        new Locale("sk", "SK"),
        new Locale("sl", "SI"),
        new Locale("sq", "AL"),
        new Locale("sr", "YU"),
        new Locale("sv", "SE"),
        new Locale("th", "TH"),
        new Locale("th", "TH"),
        new Locale("tr", "TR"),
        new Locale("uk", "UA"),
        new Locale("zh", "CN"),
        new Locale("zh", "HK"),
        new Locale("zh", "TW"),
        };

    public static Vector getLocaleNames()
    {
        Vector result = new Vector();

        for (int i = 0; i < s_locales.length; i++)
        {
            result.add(s_locales[i]);
        }

        return result;
    }
}
