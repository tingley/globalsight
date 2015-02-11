package com.globalsight.machineTranslation.iptranslator.request;

/**
 * monitor function request object
 * 
 */
public class MonitorRequest extends Request
{

    public MonitorRequest(String key, String[] translationEngineIds)
    {
        super(key);
        this.translationEngineIds = translationEngineIds;
    }

    public MonitorRequest()
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
