/**
 * Spell-checking parameters for XDE (www.xde.net) and GlobalSight.
 *
 * We use singleton objects with public member methods to control the
 * Javascript namespace.
 */

// GlobalSight
function SC_GSA_Parameters()
{
    var dictionaries = [
            { lang: "ar",    dict: "ar" },
            { lang: "ca",    dict: "ca" },
            { lang: "cs",    dict: "cs" },
            { lang: "da",    dict: "da" },
            { lang: "de",    dict: "de" },
            { lang: "de_CH", dict: "de_ch" },
            { lang: "el",    dict: "el" },
            { lang: "en",    dict: "en_us" },
            { lang: "en_AU", dict: "en_gb" },
            { lang: "en_CA", dict: "en_gb" },
            { lang: "en_GB", dict: "en_gb" },
            { lang: "en_IE", dict: "en_gb" },
            { lang: "en_IN", dict: "en_gb" },
            { lang: "en_NZ", dict: "en_gb" },
            { lang: "en_US", dict: "en_us" },
            { lang: "en_ZA", dict: "en_gb" },
            { lang: "eo",    dict: "eo" },
            { lang: "es",    dict: "es" },
            { lang: "et",    dict: "et" },
            { lang: "fi",    dict: "fi" },
            { lang: "fo",    dict: "fo" },
            { lang: "fr",    dict: "fr" },
            { lang: "hu",    dict: "hu" },
            { lang: "it",    dict: "it" },
            { lang: "mt",    dict: "mt" },
            { lang: "nl",    dict: "nl" },
            { lang: "pl",    dict: "pl" },
            { lang: "pt",    dict: "pt" },
            { lang: "pt_BR", dict: "pt_br" },
            { lang: "ru",    dict: "ru" },
            { lang: "sk",    dict: "sk" },
            { lang: "sl",    dict: "sl" },
            { lang: "sv",    dict: "sv" },
            { lang: "uk",    dict: "uk" }, 
            { lang: "af", dict: "afrikaan" },
            { lang: "hr", dict: "croatian" },
            { lang: "ja", dict: "japaneseroman;japanesesanscript" },
            { lang: "sw", dict: "swahili" },
            { lang: "tr", dict: "turkish;turkey2002" },
            { lang: "zh", dict: "chinese" },
            { lang: "XX", dict: "XX" }
            ];

    /**
     * Returns true if the given target locale is supported by the
     * GlobalSight spell-check engine.
     */
    this.isLanguageSupported = function (p_targetLocale)
    {
        if (this.getSystemDict(p_targetLocale))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns the name of the GS custom dictionary. Customdicts are
     * stored in applications/spellchecker/dicts (next to system dicts).
     */
    this.getCustomDict = function (p_userId, p_targetLocale)
    {
        return p_userId + "_" + p_targetLocale;
    }

    /**
     * Returns the system dictionary for a given target locale.
     * GlobalSight can only check one system dictionary and one
     * custom dictionary at a time.
     */
    this.getSystemDict = function (p_targetLocale)
    {
        var locale = p_targetLocale;

        // Search for a specific dictionary
        for (var i=0;i<dictionaries.length;i++)
        {
            var dict = dictionaries[i];

            if (dict.lang == locale)
            {
                return dict.dict;
            }
        }

        var lang = p_targetLocale.substring(0, 2);

        // Search for a base dictionary
        for (var i=0;i<dictionaries.length;i++)
        {
            var dict = dictionaries[i];

            if (dict.lang == lang)
            {
                return dict.dict;
            }
        }

        return "";
    }
}


// XDE
function SC_XDE_Parameters()
{
    var dictionaries =
        {
        primarydicts: [
                { lang: "af", dict: "afrikaan" },
                //{ lang: "ar", dict: "arabic" },
                //{ lang: "cs", dict: "czech;czech2002" },
                //{ lang: "da", dict: "danish;danish2002" },
                //{ lang: "de", dict: "german" },
                //{ lang: "en", dict: "usenglish;usenglish2002" },
                //{ lang: "es", dict: "spanish;spanish2002" },
                //{ lang: "et", dict: "estonian;estonian2002" },
                //{ lang: "fi", dict: "finnish;finnish2002" },
                //{ lang: "fr", dict: "french;french2002" },
                //{ lang: "el", dict: "greek;greek2002" },
                { lang: "hr", dict: "croatian" },
                //{ lang: "hu", dict: "hungarian;hungarian2002" },
                //{ lang: "it", dict: "italian;italian2002" },
                { lang: "ja", dict: "japaneseroman;japanesesanscript" },
                //{ lang: "nl", dict: "dutch;dutch2002" },
                //{ lang: "no", dict: "norwegian;norwegian2002" },
                //{ lang: "pl", dict: "polish" },
                //{ lang: "pt", dict: "portuguese" },
                //{ lang: "ru", dict: "russian" },
                //{ lang: "sv", dict: "swedish" },
                { lang: "sw", dict: "swahili" },
                { lang: "tr", dict: "turkish;turkey2002" },
                //{ lang: "uk", dict: "ukrainian" },
                { lang: "zh", dict: "chinese" },
                { lang: "XX", dict: "XX" }
            ],
        specificdicts: [
            //{ lang: "de_CH", chain: null, dict: "swiss2002" },
            //{ lang: "en_AU", chain: "en_GB", dict: "australian" },
            //{ lang: "en_CA", chain: "en_GB", dict: "canadian" },
            //{ lang: "en_GB", chain: null, dict: "ukenglish;ukenglish2002" },
            //{ lang: "en_IE", chain: "en_GB", dict: null },
            //{ lang: "en_IN", chain: "en_GB", dict: null },
            //{ lang: "en_NZ", chain: "en_GB", dict: null },
            //{ lang: "en_US", chain: null, dict:
            //  "usenglish;usenglish2002;usenglishfirstname;usenglishlastname" },
            //{ lang: "en_ZA", chain: "en_GB", dict: "africansouth" },
            //{ lang: "es_AR", chain: "es", dict: "argentina" },
            //{ lang: "es_BO", chain: "es", dict: "bolivian;bolivian2002" },
            //{ lang: "es_CL", chain: "es", dict: "chilean" },
            //{ lang: "es_CO", chain: "es", dict: "colombian;colombian2002" },
            //{ lang: "es_EC", chain: "es", dict: "ecuadoran" },
            //{ lang: "es_MX", chain: "es", dict: "mexican" },
            //{ lang: "es_PY", chain: "es", dict: "paraguayan" },
            //{ lang: "es_PE", chain: "es", dict: "peruvian" },
            //{ lang: "es_VE", chain: "es", dict: "venezuelan" },
              { lang: "fr_CA", chain: "fr", dict: "canadianfrench" },
            //{ lang: "pt_BR", chain: "pt", dict: "brazilianportuguese" },
              { lang: "XX_XX", chain: null, dict: "XX" }
            ]
        };

    /**
     * Returns a UI locale for the spell checker window.
     */
    this.getUiLanguage = function (p_uiLocale)
    {
        var lang = p_uiLocale.substring(0, 2);

        if      (lang == "en") return "english";
        else if (lang == "es") return "spanish";
        else if (lang == "fr") return "french";
        else if (lang == "nl") return "dutch";

        return "english";
    }

    /**
     * Returns true if the given target locale is supported by the
     * spell-check engine.
     */
    this.isLanguageSupported = function (p_targetLocale)
    {
        if (this.getSystemDict(p_targetLocale))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns the name of the custom dictionary. Customdicts are
     * stored in applications/xdespellchecker/customdicts.
     */
    this.getCustomDict = function (p_userId, p_targetLocale)
    {
        return p_userId + "_" + p_targetLocale;
    }

    /**
     * Returns the system dictionaries for a given target locale.
     */
    this.getSystemDict = function (p_targetLocale)
    {
        // select dictionaries
        var res1 = null, res2 = null;
        var result = "";
        var chain = false;
        var lang = p_targetLocale.substring(0, 2);
        var locale = p_targetLocale;

        // Search for a specific dictionary and chain to a base language
        for (var i in dictionaries.specificdicts)
        {
            var dict = dictionaries.specificdicts[i];

            if (dict.lang == locale)
            {
                res1 = dict.dict;
                chain = dict.chain;
                break;
            }
        }

        // Found chain to base language, search specific dictionaries again
        if (chain && chain.length == 5)
        {
            for (var i in dictionaries.specificdicts)
            {
                var dict = dictionaries.specificdicts[i];

                if (dict.lang == chain)
                {
                    res2 = dict.dict;
                    break;
                }
            }
        }

        // No specific language or a chain to a primary base language
        if (!res1 && !res2 || chain && chain.length == 2)
        {
            for (var i in dictionaries.primarydicts)
            {
                var dict = dictionaries.primarydicts[i];

                if (dict.lang == lang)
                {
                    res2 = dict.dict;
                    break;
                }
            }
        }

        if (res1 && !res2)
        {
            result = res1;
        }
        else if (res1 && res2)
        {
            result = res1 + ";" + res2;
        }
        else if (res2)
        {
            result = res2;
        }

        return result;
    }
}
