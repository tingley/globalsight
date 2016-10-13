package com.globalsight.machineTranslation.iptranslator.response;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class TextTranslationResponse
{
    private String text[];
    private int text_status[];
    private Long wordCount;

    public TextTranslationResponse()
    {
    }

    public TextTranslationResponse(
            LinkedHashMap<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, Integer>> textTranslations,
            Long wordCount)
    {

        if (textTranslations != null)
        {

            for (Map.Entry<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, Integer>> entry : textTranslations
                    .entrySet())
            {
                LinkedHashMap<Integer, String> translations = entry.getKey();
                LinkedHashMap<Integer, Integer> status = entry.getValue();

                text = new String[translations.size()];
                text_status = new int[translations.size()];

                for (Map.Entry<Integer, Integer> statusEntry : status
                        .entrySet())
                {
                    int index = statusEntry.getKey();
                    int statusValue = statusEntry.getValue();

                    String translation = translations.get(index);

                    text_status[index] = statusValue;
                    text[index] = translation;
                }

            }
        }

        this.wordCount = wordCount;
    }

    public void print()
    {
        if (text != null)
        {
            for (int i = 0; i < text.length; i++)
            {
                System.out.println("Translation: " + text[i] + "\nstatus: "
                        + text_status[i]);
            }
        }
    }

    public String printToFile()
    {
        try
        {
            File f = File.createTempFile("response", ".xml");
            if (text != null)
            {
                FileUtils.writeLines(f, Arrays.asList(text));
            }
            System.out.println(f.getAbsolutePath());
            return f.getAbsolutePath();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String printToFile(String fileName)
    {
        try
        {
            File f = new File(fileName + ".es");
            if (text != null)
            {
                FileUtils.writeLines(f, Arrays.asList(text));
            }
            return f.getAbsolutePath();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString()
    {
        return "TranslationResponse [" + ", output=" + Arrays.toString(text)
                + ", text_status=" + ", output_status=" + text_status + "]";
    }

    public String[] getText()
    {
        return text;
    }

    public void setText(String[] text)
    {
        this.text = text;
    }

    public int[] getText_status()
    {
        return text_status;
    }

    public void setText_status(int[] text_status)
    {
        this.text_status = text_status;
    }

    public Long getWordCount()
    {
        return wordCount;
    }

    public void setWordCount(Long wordCount)
    {
        this.wordCount = wordCount;
    }
}
