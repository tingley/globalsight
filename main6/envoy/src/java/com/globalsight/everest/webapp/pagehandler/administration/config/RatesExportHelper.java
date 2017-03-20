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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.globalsight.everest.costing.Rate;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports rates.
 */
public class RatesExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("Rates");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = RATE_FILE_NAME + userName + "_" + sdf.format(new Date()) + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets rates info.
     */
    public static File exportRates(File ratePropertyFile, String id)
    {
        try
        {
            Rate rate = HibernateUtil.get(Rate.class, Long.parseLong(id));
            StringBuffer buffer = new StringBuffer();
            if (rate != null)
            {
                buffer.append("##Rate.").append(rate.getActivity().getCompanyId()).append(".")
                        .append(rate.getId()).append(".begin").append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".ID = ").append(rate.getId())
                        .append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".NAME = ")
                        .append(rate.getName()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".CURRENCY_CONV_ISO_CODE = ")
                        .append(rate.getCurrency().getIsoCode()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".TYPE = ")
                        .append(rate.getType()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".ACTIVITY_NAME = ")
                        .append(rate.getActivity().getDisplayName()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".LOCALE_PAIR_NAME = ")
                        .append(rate.getLocalePair().getSource().toString() + "->"
                                + rate.getLocalePair().getTarget().toString())
                        .append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".EXACT_CONTEXT_RATE = ")
                        .append(rate.getContextMatchRate()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".EXACT_SEGMENT_TM_RATE = ")
                        .append(rate.getSegmentTmRate()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".FUZZY_LOW_RATE = ")
                        .append(rate.getLowFuzzyMatchRate()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".FUZZY_MED_RATE = ")
                        .append(rate.getMedFuzzyMatchRate()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".FUZZY_MED_HI_RATE = ")
                        .append(rate.getMedHiFuzzyMatchRate()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".FUZZY_HI_RATE = ")
                        .append(rate.getHiFuzzyMatchRate()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".EXACT_CONTEXT_RATE_PER = ")
                        .append(rate.getContextMatchRatePer()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".EXACT_SEGMENT_TM_RATE_PER = ")
                        .append(rate.getSegmentTmRatePer()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".FUZZY_LOW_RATE_PER = ")
                        .append(rate.getLowFuzzyMatchRatePer()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".FUZZY_MED_RATE_PER = ")
                        .append(rate.getMedFuzzyMatchRatePer()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".FUZZY_MED_HI_RATE_PER = ")
                        .append(rate.getMedHiFuzzyMatchRatePer()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".FUZZY_HI_RATE_PER = ")
                        .append(rate.getHiFuzzyMatchRatePer()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".NO_MATCH_RATE = ")
                        .append(rate.getNoMatchRate()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".REPETITION_RATE = ")
                        .append(rate.getRepetitionRate()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".IN_CONTEXT_MATCH_RATE = ")
                        .append(rate.getInContextMatchRate()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".IN_CONTEXT_MATCH_RATE_PER = ")
                        .append(rate.getInContextMatchRatePer()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".REPETITION_RATE_PER = ")
                        .append(rate.getRepetitionRatePer()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".UNIT_RATE = ")
                        .append(rate.getUnitRate()).append(NEW_LINE);
                buffer.append("Rate.").append(rate.getId()).append(".IS_ACTIVE = ")
                        .append(rate.isActive()).append(NEW_LINE);
                buffer.append("##Rate.").append(rate.getActivity().getCompanyId()).append(".")
                        .append(rate.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);

                writeToFile(ratePropertyFile, buffer.toString().getBytes());

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ratePropertyFile;
    }

    private static void writeToFile(File ratePropertyFile, byte[] bytes)
    {
        ratePropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(ratePropertyFile, true);
            fos.write(bytes);
        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {

            }
        }
    }
}
