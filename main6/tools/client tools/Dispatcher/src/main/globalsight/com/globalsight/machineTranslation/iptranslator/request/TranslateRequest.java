package com.globalsight.machineTranslation.iptranslator.request;

/**
 * translate function request object
 * 
 */
public class TranslateRequest extends Request
{

    public TranslateRequest(String key, String translationEngineId,
            String text[], String xliff[])
    {
        super(key);
        this.text = text;
        this.xliff = xliff;
        this.translationEngineId = translationEngineId;
    }

    public TranslateRequest()
    {
    }

    public String getTranslationEngineId()
    {
        return translationEngineId;
    }

    public void setTranslationEngineId(String translationEngineId)
    {
        this.translationEngineId = translationEngineId;
    }

    public String[] getText()
    {
        return text;
    }

    public void setText(String text[])
    {
        this.text = text;
    }

    public String[] getXliff()
    {
        return xliff;
    }

    public void setXliff(String xliff[])
    {
        this.xliff = xliff;
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private String text[];
    private String xliff[];
    private String translationEngineId;
}
