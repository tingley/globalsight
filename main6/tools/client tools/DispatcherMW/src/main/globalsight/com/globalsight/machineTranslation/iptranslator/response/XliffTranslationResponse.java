package com.globalsight.machineTranslation.iptranslator.response;

import java.util.Arrays;
import java.util.HashMap;

public class XliffTranslationResponse
{
    /**
     * The key is the xliff files index; The key of value is "trans-unit" id,
     * and the value of value is "0" or "-1"(0 means successful; -1 means
     * failure).
     */
    private HashMap<Integer, HashMap<String, Integer>> xliff_status;

    /**
     * Xliff file strings.IPTranslator supports translate multiple xliff files
     * in one invoking.
     */
    private String xliff[];

    /**
     * Total word count of all xliff files.
     */
    private Long wordCount;

    public XliffTranslationResponse()
    {
    }

    public HashMap<Integer, HashMap<String, Integer>> getXliff_status()
    {
        return xliff_status;
    }

    public void setXliff_status(
            HashMap<Integer, HashMap<String, Integer>> xliff_status)
    {
        this.xliff_status = xliff_status;
    }

    public String[] getXliff()
    {
        return xliff;
    }

    public void setXliff(String[] output)
    {
        this.xliff = output;
    }

    public Long getWordCount()
    {
        return wordCount;
    }

    public void setWordCount(Long wordCount)
    {
        this.wordCount = wordCount;
    }

    @Override
    public String toString()
    {
        return "TranslationResponse [output=" + Arrays.toString(xliff)
                + ", xliff_status=" + getXliff_status() + "]";
    }

    public void print()
    {
        if (getXliff_status() != null)
        {
            for (int i = 0; i < xliff.length; i++)
            {
                System.out.println("Translation: " + xliff[i] + "\nstatus: "
                        + getXliff_status().get(i));
            }
        }
    }
}
