package com.globalsight.machineTranslation.iptranslator.request;

public class TerminateRequest extends Request
{

    public TerminateRequest(String key, String[] translationEngineIds)
    {
        super(key);
        this.translationEngineIds = translationEngineIds;
    }

    public TerminateRequest()
    {
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String[] translationEngineIds;

    public String[] getTranslationEngineIds()
    {
        return translationEngineIds;
    }

    public void setTranslationEngineIds(String[] translationEngineIds)
    {
        this.translationEngineIds = translationEngineIds;
    }
}
