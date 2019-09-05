package com.globalsight.connector.eloqua.models.form;

import java.util.regex.Matcher;

import com.globalsight.util.Replacer;

public class IdReplacer extends Replacer
{
    private int i;
    
    public IdReplacer(int i)
    {
        super();
        this.i = i;
    }

    @Override
    public String getReplaceString(Matcher m)
    {
        return "\"id\":\"" + i-- + "\"";
    }

}
