package com.globalsight.machineTranslation.iptranslator.request;

import java.util.Arrays;

public class TranslationRequest extends Request
{
    private static final long serialVersionUID = 1L;

    private String inputType;
    private String input[];
    private String from;
    private String to;

    @Override
    public String toString()
    {
        return "TranslationRequest [text=" + Arrays.toString(input)
                + ", xliff=" + ", from=" + from + ", to=" + to + ", inputType:"
                + inputType + " ]";
    }

    public String getInputType()
    {
        return inputType;
    }

    public void setInputType(String inputType)
    {
        this.inputType = inputType;
    }

    public String[] getInput()
    {
        return input;
    }

    public void setInput(String[] input)
    {
        this.input = input;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }
}
