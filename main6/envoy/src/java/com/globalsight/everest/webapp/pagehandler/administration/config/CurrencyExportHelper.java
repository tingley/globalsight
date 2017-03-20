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

import com.globalsight.everest.costing.Currency;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports currency.
 */
public class CurrencyExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("Currency");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = CURRENCY_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets currency info.
     */
    public static File exportCurrency(File currencyPropertyFile, String id)
    {
        try
        {
            Currency currency = HibernateUtil.get(Currency.class, Long.parseLong(id));
            StringBuffer buffer = new StringBuffer();
            if (currency != null)
            {
                buffer.append("##Currency.").append(currency.getCompanyId()).append(".")
                        .append(currency.getId()).append(".begin").append(NEW_LINE);
                buffer.append("Currency.").append(currency.getId()).append(".ID = ")
                        .append(currency.getId()).append(NEW_LINE);
                buffer.append("Currency.").append(currency.getId()).append(".CURRENCY_ID = ")
                        .append(currency.getIsoCurrency().getId()).append(NEW_LINE);
                buffer.append("Currency.").append(currency.getId()).append(".CONVERSION_FACTOR = ")
                        .append(currency.getConversionFactor()).append(NEW_LINE);
                buffer.append("Currency.").append(currency.getId()).append(".COMPANY_ID = ")
                        .append(currency.getCompanyId()).append(NEW_LINE);
                buffer.append("Currency.").append(currency.getId()).append(".IS_ACTIVE = ")
                        .append(currency.isActive()).append(NEW_LINE);
                buffer.append("##Currency.").append(currency.getCompanyId()).append(".")
                        .append(currency.getId()).append(".end").append(NEW_LINE).append(NEW_LINE);

                writeToFile(currencyPropertyFile, buffer.toString().getBytes());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return currencyPropertyFile;
    }

    private static void writeToFile(File currencyPropertyFile, byte[] bytes)
    {
        currencyPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(currencyPropertyFile, true);
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
