package com.globalsight.terminology;

import java.util.ArrayList;
import java.util.Locale;

import com.globalsight.ling.common.LocaleCreater;
import com.globalsight.terminology.termleverager.TermLeverageOptions;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.terminology.termleverager.TermLeverager;
import com.globalsight.terminology.termleverager.TermLeveragerException;
import com.globalsight.util.edit.EditUtil;

public class TermLeveragingSearch extends AbstractTermSearch
{
    private String flag = new String();
    /**
     * For debugging the term leverager: calls the leverager on the given query,
     * which should be a segment, and returns the source terms found.
     */
    public Hitlist getHitListResults(String p_language, String p_locale,
            String p_query, int p_maxHits, int begin) throws TermbaseException
    {
        Hitlist result = new Hitlist();

        if (p_query.length() == 0)
        {
            return result;
        }

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("term leverage in " + p_language + " ("
                        + p_locale + ") for '" + p_query + "'");
            }

            ArrayList matches = leverageTerms(p_query, p_language, p_locale);

            for (int i = 0, max = matches.size(); i < max; i++)
            {
                TermLeverageResult.MatchRecord match = 
                    (TermLeverageResult.MatchRecord) matches.get(i);

                result.add(match.getMatchedSourceTerm(), match.getConceptId(),
                        match.getMatchedSourceTermId(), match.getScore(), match
                                .getSourceDescXML());
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error("cannot leverage terms", ex);
        }

        return result;
    }
    
    public String getXmlResults(String srcLan, String trgLan, String p_query,
            int maxHits, int begin) throws TermbaseException
    {
        if (flag != null && !flag.equals(""))
        {
            return flag;
        }
        else
        {
            Hitlist result = getHitListResults(srcLan, trgLan, p_query,
                    maxHits, begin);
            return result.getXml();
        }
    }
    
    private ArrayList leverageTerms(String p_segment, String p_language,
            String p_locale) throws TermbaseException, TermLeveragerException
    {
        Locale locale = LocaleCreater.makeLocale(p_locale);
        TermLeverageOptions options = new TermLeverageOptions();

        // fuzzy threshold set by object constructor - use defaults.
        // options.setFuzzyThreshold(50);

        options.addTermBase(m_name);
        options.setLoadTargetTerms(true);
        options.setSaveToDatabase(false);
        options.setSourcePageLocale(locale);
        options.addSourcePageLocale2LangName(p_language);

        // need to add a target language != source language, pick any
        // and don't care which it is - this is only a "feature" to
        // debug the leverager.
        String targetLang = "";
        String targetLocl = "";

        ArrayList langs = m_definition.getLanguages();
        for (int i = 0, max = langs.size(); i < max; i++)
        {
            Definition.Language lang = (Definition.Language) langs.get(i);

            if (lang.getName().equals(p_language))
            {
                continue;
            }

            targetLang = lang.getName();
            targetLocl = lang.getLocale();

            break;
        }

        Locale targetLocale = LocaleCreater.makeLocale(targetLocl);

        options.addTargetPageLocale2LangName(targetLocale, targetLang);
        options.addLangName2Locale(targetLang, targetLocale);

        TermLeverager tl = new TermLeverager();

        p_segment = "<segment>" + EditUtil.encodeXmlEntities(p_segment)
                + "</segment>";

        try
        {
            TermLeverageResult result = tl.leverageTerms(p_segment, options);
            return result.getAllMatchRecords();
        }
        catch (java.rmi.RemoteException ignore)
        {
            // sigh
        }

        return null;
    }
}
