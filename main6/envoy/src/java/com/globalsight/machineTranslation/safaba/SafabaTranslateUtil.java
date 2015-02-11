package com.globalsight.machineTranslation.safaba;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.globalsight.machineTranslation.MTHelper;

import safaba.api.SafabaSecureClient;

public class SafabaTranslateUtil
{
    private static final Logger logger = Logger.getLogger(SafabaTranslateUtil.class);

    // Timeout in milliseconds.
    // If not given in the SafabaSecureClient constructor,
    // default is 0 indicating no timeout
    private static final int timeout = 0;

    /**
     * Use Safaba MT to translate, and return a single string value of translation.
     * 
     * @param hostName Safaba host name.
     * @param port Safaba server port.
     * @param companyName The company name -- welocalize
     * @param password Safaba password for company
     * @param safabaClient Safaba client name
     * @param langPair Locale pairs. The pair should in the pattern like "ENUS-DEDE"
     * @param text The text to be translated.
     * @param waitTime How many seconds the server will wait, until timeout.
     * @return The translated string.
     * @throws Exception e
     */
    public static String translate(final String hostName, final int port,
            final String companyName, final String password,
            final String safabaClient, final String langPair,
            final String text, int waitTime)
            throws Exception
    {
        ExecutorService exe = Executors.newSingleThreadExecutor();
        logger.info("Single source segment: " + text);
        FutureTask<String> futureTask = new FutureTask<String>(
                new Callable<String>()
                {
                    public String call()
                    {
                        SafabaSecureClient client = new SafabaSecureClient(
                                hostName, port, companyName, password, timeout);
                        try
                        {
                            String translation = client.translate(safabaClient,
                                    langPair, text);
                            return translation;
                        }
                        catch (Exception e)
                        {
                            logger.error("Translation failed with error: " + e.getMessage());
                            if (logger.isDebugEnabled())
                            {
                                logger.error(e);
                            }
                            return null;
                        }
                    }
                });
        exe.execute(futureTask);
        try
        {
            String result = futureTask.get(waitTime, TimeUnit.SECONDS);
            logger.info("Single translated segment: " + result);
            return result;
        }
        catch (Exception e)
        {
            futureTask.cancel(true);
            throw e;
        }
        finally
        {
            exe.shutdown();
        }
    }
    
    /**
     * Use Safaba MT to translate a list, and return a list of translation.
     * 
     * @param hostName Safaba host name.
     * @param port Safaba server port.
     * @param companyName The company name -- welocalize
     * @param password Safaba password for company
     * @param safabaClient Safaba client name
     * @param langPair Locale pairs. The pair should in the pattern like "ENUS-DEDE"
     * @param textList The text list to be translated.
     * @param waitTime How many seconds the server will wait, until timeout.
     * @return The translated string list.
     * @throws Exception e
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String[] batchTranslate(final String hostName, final int port,
            final String companyName, final String password,
            final String safabaClient, final String langPair,
            final String[] textList, int waitTime)
            throws Exception
    {
        if (MTHelper.isLogDetailedInfo(SafabaProxy.ENGINE_SAFABA))
        {
            for (int i = 0; i < textList.length; i++)
            {
                logger.info("Source segment[" + i + "] :: " + textList[i]);
            }
        }
        ExecutorService exe = Executors.newSingleThreadExecutor();
        FutureTask<List> futureTask = new FutureTask<List>(
                new Callable<List>()
                {
                    public List<String> call()
                    {
                        List<String> segments = Arrays.asList(textList);
                        
                        SafabaSecureClient client = new SafabaSecureClient(
                                hostName, port, companyName, password, timeout);
                        try
                        {
                            return client.translate(safabaClient,
                                    langPair, segments);
                        }
                        catch (Exception e)
                        {
                            logger.error("Translation failed with error: " + e.getMessage());
                            if (logger.isDebugEnabled())
                            {
                                logger.error(e);
                            }
                            return null;
                        }
                    }
                });
        exe.execute(futureTask);
        try
        {
            List<String> back = futureTask.get(waitTime, TimeUnit.SECONDS);
            if (back != null)
            {
                String[] result = new String[back.size()];
                if (MTHelper.isLogDetailedInfo(SafabaProxy.ENGINE_SAFABA))
                {
                    for (int i = 0; i < back.size(); i++)
                    {
                        logger.info("Translated segment[" + i + "] :: " + back.get(i));
                    }
                }
                return back.toArray(result);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            futureTask.cancel(true);
            throw e;
        }
        finally
        {
            exe.shutdown();
        }
    }
    
    /**
     * Get locale pairs. 
     * @param source
     * @param target
     * @return In the pattern like, "ENUS-DEDE"
     */
    public static String getLocalePairs(Locale source, Locale target)
    {
        String part1 = source.getLanguage() + source.getCountry();
        String part2 = target.getLanguage() + target.getCountry();
        return part1 + "-" + part2;
    }

}
