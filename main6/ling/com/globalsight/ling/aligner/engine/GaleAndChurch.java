/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.ling.aligner.engine;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm.LingManagerException;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

public class GaleAndChurch
    implements SegmentAlignmentScorer
{
    private static final Logger c_logger =
        Logger.getLogger(
            GaleAndChurch.class);

    private final static int BIG_DISTANCE = 2500;
    
    // average number of target characters per source character for
    // the source-target pair
    private double m_charDist;
    

    /**
     * Set source and target locales.
     *
     * @param p_sourceLocale Source locale
     * @param p_targetLocale Target locale
     */
    public void setLocales(
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
        throws LingManagerException
    {
        m_charDist = getCharacterDistribution(p_sourceLocale, p_targetLocale);

        c_logger.debug("Character Distribution = " + m_charDist);
    }
    

    /**
     * Calculate the cost of substitution of source segment by target
     * segment.
     *
     * @param p_sourceTuv Source TUV. Source is in X sequence in the DP map.
     * @param p_targetTuv Target TUV. Target is in Y sequence in the DP map.
     * @return cost of the substitution
     */
    public int substitutionScore(BaseTmTuv p_sourceTuv, BaseTmTuv p_targetTuv)
        throws LingManagerException
    {
        int sourceLength = p_sourceTuv.getFuzzyIndexFormat().length();
        int targetLength = p_targetTuv.getFuzzyIndexFormat().length();
        
        return match(sourceLength, targetLength);
    }
    

    /**
     * Calculate the cost of deletion of source segment.
     *
     * @param p_sourceTuv Source TUV. Source is in X sequence in the DP map.
     * @return cost of the deletion
     */
    public int deletionScore(BaseTmTuv p_sourceTuv)
        throws LingManagerException
    {
        int sourceLength = p_sourceTuv.getFuzzyIndexFormat().length();

        return match(sourceLength, 0);
    }
    

    /**
     * Calculate the cost of insertion of target segment.
     *
     * @param p_targetTuv Target TUV. Target is in Y sequence in the DP map.
     * @return cost of the insertion
     */
    public int insertionScore(BaseTmTuv p_targetTuv)
        throws LingManagerException
    {
        int targetLength = p_targetTuv.getFuzzyIndexFormat().length();

        return match(0, targetLength);
    }
    

    /**
     * Calculate the cost of contracting two source segments to one
     * target segment.
     *
     * @param p_sourceTuv1 Source TUV1. Source is in X sequence in the DP map.
     * @param p_sourceTuv2 Source TUV2. Source is in X sequence in the DP map.
     * @param p_targetTuv Target TUV. Target is in Y sequence in the DP map.
     * @return cost of the contraction
     */
    public int contractionScore(BaseTmTuv p_sourceTuv1,
        BaseTmTuv p_sourceTuv2, BaseTmTuv p_targetTuv)
        throws LingManagerException
    {
        int sourceLength1 = p_sourceTuv1.getFuzzyIndexFormat().length();
        int sourceLength2 = p_sourceTuv2.getFuzzyIndexFormat().length();
        int targetLength = p_targetTuv.getFuzzyIndexFormat().length();
        
        return match(sourceLength1 + sourceLength2, targetLength);
    }
    

    /**
     * Calculate the cost of expanding one source segment to two
     * target segments.
     *
     * @param p_sourceTuv Source TUV. Source is in X sequence in the DP map.
     * @param p_targetTuv1 Target TUV1. Target is in Y sequence in the DP map.
     * @param p_targetTuv2 Target TUV2. Target is in Y sequence in the DP map.
     * @return cost of the expansion
     */
    public int expansionScore(BaseTmTuv p_sourceTuv,
        BaseTmTuv p_targetTuv1, BaseTmTuv p_targetTuv2)
        throws LingManagerException
    {
        int sourceLength = p_sourceTuv.getFuzzyIndexFormat().length();
        int targetLength1 = p_targetTuv1.getFuzzyIndexFormat().length();
        int targetLength2 = p_targetTuv2.getFuzzyIndexFormat().length();

        return match(sourceLength, targetLength1 + targetLength2);
    }
    

    /**
     * Calculate the cost of melding of two source segments to two
     * target segments.
     *
     * @param p_sourceTuv1 Source TUV1. Source is in X sequence in the DP map.
     * @param p_sourceTuv2 Source TUV2. Source is in X sequence in the DP map.
     * @param p_targetTuv1 Target TUV1. Target is in Y sequence in the DP map.
     * @param p_targetTuv2 Target TUV2. Target is in Y sequence in the DP map.
     * @return cost of the melding
     */
    public int meldingScore(BaseTmTuv p_sourceTuv1, BaseTmTuv p_sourceTuv2,
        BaseTmTuv p_targetTuv1, BaseTmTuv p_targetTuv2)
        throws LingManagerException
    {
        int sourceLength1 = p_sourceTuv1.getFuzzyIndexFormat().length();
        int sourceLength2 = p_sourceTuv2.getFuzzyIndexFormat().length();
        int targetLength1 = p_targetTuv1.getFuzzyIndexFormat().length();
        int targetLength2 = p_targetTuv2.getFuzzyIndexFormat().length();

        return match(sourceLength1 + sourceLength2,
            targetLength1 + targetLength2);
    }

        
    /**
     * Returns the area under a normal distribution from -inf to z
     * standard deviations
     */
    private double pnorm(double z)
    {
        double t, pd;
        t = 1/(1 + 0.2316419 * z);
        pd = 1 - 0.3989423 * Math.exp(-z * z/2) * 
            ((((1.330274429 * t - 1.821255978) * t 
                + 1.781477937) * t - 0.356563782) * t + 0.319381530) * t;
        /* see Gradsteyn & Rhyzik, 26.2.17 p932 */
        return(pd);
    }


    /**
     * Return -100 * log probability that an source sentence of length
     * len1 is a translation of a foreign sentence of length len2.
     * The probability is based on two parameters, the mean and
     * variance of number of foreign characters per source character.
     *
     * Gale and Church hardcoded foreign_chars_per_eng_char as 1. It
     * apparently works OK for European language alignment. We take
     * the coefficient as a parameter so that non European languages
     * can be aligned as well.
     * */

    private int match(int len1, int len2)
    {
        /* variance per english character */
        /* May need tweak for the other languages */
        double var_per_eng_char = 6.8 ;	
  
        if(len1 == 0 && len2 == 0)
            return(0);

        double mean = (len1 + len2/m_charDist) / 2;
        double z = (m_charDist * len1 - len2)
            / Math.sqrt(var_per_eng_char * mean);

        /* Need to deal with both sides of the normal distribution */
        if(z < 0)
            z = -z;

        double pd = 2 * (1 - pnorm(z));

        if(pd > 0)
            return((int)(-100 * Math.log(pd)));
        else
            return(BIG_DISTANCE);
    }


    private double getCharacterDistribution(
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
        throws LingManagerException
    {
        double charDist = 1;
        
        try
        {
            double srcCharDist = getCharDistProperty(p_sourceLocale);
            double trgCharDist = getCharDistProperty(p_targetLocale);

            charDist = trgCharDist / srcCharDist;
        }
        catch(Exception e)
        {
            throw new LingManagerException(e);
        }
        
        return charDist;
    }


    private double getCharDistProperty(GlobalSightLocale p_locale)
        throws Exception
    {
        String lang = p_locale.getLanguageCode();
        String locale = p_locale.toString();
        
        ResourceBundle res
            = ResourceBundle.getBundle("properties/aligner/CharDist");

        String charDistStr = null;
        try
        {
            charDistStr = res.getString(lang);
        }
        catch(MissingResourceException e)
        {
            try
            {
                charDistStr = res.getString(locale);
            }
            catch(MissingResourceException e2)
            {
                charDistStr = "1";
            }
        }

        return Double.parseDouble(charDistStr);
    }
    
}
    
