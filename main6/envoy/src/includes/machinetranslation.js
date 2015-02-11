/**
 * Machine Translation parameters.
 *
 * We use a singleton object with public member methods to control the
 * Javascript namespace.
 */
function MT_Parameters()
{
    var m_engines =
        [
                { engine: "babelfish",
                  displayname: "Babelfish",
                  description: "Babelfish (Altavista)",
                  languages: [
                          { source: "en", target: "de" },
                          { source: "en", target: "el" },
                          { source: "en", target: "es" },
                          { source: "en", target: "fr" },
                          { source: "en", target: "it" },
                          { source: "en", target: "ja" },
                          { source: "en", target: "ko" },
                          { source: "en", target: "nl" },
                          { source: "en", target: "pt" },
                          { source: "en", target: "ru" },
                          { source: "en", target: "zh-cn" }, // spelled "zh"
                          { source: "en", target: "zh-tw" }, // spelled "zt"

                          { source: "de", target: "en" },
                          { source: "el", target: "en" },
                          { source: "es", target: "en" },
                          { source: "fr", target: "en" },
                          { source: "it", target: "en" },
                          { source: "ja", target: "en" },
                          { source: "ko", target: "en" },
                          { source: "nl", target: "en" },
                          { source: "pt", target: "en" },
                          { source: "ru", target: "en" },
                          { source: "zh-cn", target: "en" }, // spelled "zh"
                          { source: "zh-tw", target: "en" }, // spelled "zt"

                          { source: "fr", target: "de" },
                          { source: "fr", target: "el" },
                          { source: "fr", target: "es" },
                          { source: "fr", target: "it" },
                          { source: "fr", target: "nl" },
                          { source: "fr", target: "pt" },

                          { source: "de", target: "fr" },
                          { source: "el", target: "fr" },
                          { source: "es", target: "fr" },
                          { source: "it", target: "fr" },
                          { source: "nl", target: "fr" },
                          { source: "pt", target: "fr" }
                      ]
                },
                { engine: "freetranslation",
                  displayname: "FreeTranslation",
                  description: "SDL FreeTranslation",
                  languages: [
                          { source: "en", target: "zh-cn" },
                          { source: "en", target: "zh-tw" },

                          { source: "en", target: "de" },
                          { source: "en", target: "es" },
                          { source: "en", target: "fr" },
                          { source: "en", target: "it" },
                          { source: "en", target: "nl" },
                          { source: "en", target: "no" },
                          { source: "en", target: "pt" },
                          { source: "en", target: "ru" },

                          { source: "de", target: "en" },
                          { source: "es", target: "en" },
                          { source: "fr", target: "en" },
                          { source: "it", target: "en" },
                          { source: "nl", target: "en" },
                          { source: "pt", target: "en" },
                          { source: "ru", target: "en" }
                      ]
                },
                { engine: "systran",
                  displayname: "Systran",
                  description: "Systran",
                  languages: [
                          { source: "en", target: "de" },
                          { source: "en", target: "el" },
                          { source: "en", target: "es" },
                          { source: "en", target: "fr" },
                          { source: "en", target: "it" },
                          { source: "en", target: "ja" },
                          { source: "en", target: "ko" },
                          { source: "en", target: "pt" },
                          { source: "en", target: "ru" },
                          { source: "en", target: "zh-cn" }, //? "zh"
                          { source: "en", target: "zh-tw" }, //? "zt"

                          { source: "de", target: "en" },
                          { source: "el", target: "en" },
                          { source: "es", target: "en" },
                          { source: "fr", target: "en" },
                          { source: "it", target: "en" },
                          { source: "ja", target: "en" },
                          { source: "ko", target: "en" },
                          { source: "pt", target: "en" },
                          { source: "ru", target: "en" },
                          { source: "zh-cn", target: "en" }, //? "zh"
                          { source: "zh-tw", target: "en" }, //? "zt"

                          { source: "fr", target: "de" },
                          { source: "fr", target: "el" },
                          { source: "fr", target: "es" },
                          { source: "fr", target: "it" },
                          { source: "fr", target: "nl" },
                          { source: "fr", target: "pt" },

                          { source: "de", target: "fr" },
                          { source: "el", target: "fr" },
                          { source: "es", target: "fr" },
                          { source: "it", target: "fr" },
                          { source: "nl", target: "fr" },
                          { source: "pt", target: "fr" }
                          ]
                }
                /*,
                { engine: "google",
                  displayname: "Google",
                  description: "Google Language Tools",
                  languages: [
                          { source: "en", target: "de" },
                          { source: "en", target: "es" },
                          { source: "en", target: "fr" },
                          { source: "en", target: "it" },
                          { source: "en", target: "pt" },

                          { source: "de", target: "en" },
                          { source: "es", target: "en" },
                          { source: "fr", target: "en" },
                          { source: "it", target: "en" },
                          { source: "pt", target: "en" },

                          { source: "de", target: "fr" },
                          { source: "fr", target: "de" }
                      ]
                }
                */
        ];

    /**
     * Returns an array of engine names and display values. The array
     * is flat: [name, disp, name, disp, ...]
     */
    this.getEngines = function ()
    {
        var result = new Array();

        for (var i = 0; i < m_engines.length; i++)
        {
            var engine = m_engines[i];

            result.push(engine.engine);
            result.push(engine.displayname);
        }

        return result;
    }

    /**
     * Private method to pull an engine descriptor out of the array.
     */
    function getEngineDescriptor(p_engine)
    {
        for (var i = 0; i < m_engines.length; i++)
        {
            var engine = m_engines[i];

            if (engine.engine == p_engine)
            {
                return engine;
            }
        }

        return null;
    }

    /**
     * Returns the system dictionaries for a given target locale.
     */
    this.isLanguageSupported = function (p_engine, p_source, p_target)
    {
        var desc = getEngineDescriptor(p_engine);

        if (!desc)
        {
            return false;
        }

        if (p_source.indexOf("zh") == -1)
        {
            p_source = p_source.substring(0, 2);
        }
        if (p_target.indexOf("zh") == -1)
        {
            p_target = p_target.substring(0, 2);
        }

        var languages = desc.languages;
        for (var i = 0; i < languages.length; i++)
        {
            var source = languages[i].source;
            var target = languages[i].target;

            if (source == p_source && target == p_target)
            {
                return true;
            }
        }

        return false;
    }
}
