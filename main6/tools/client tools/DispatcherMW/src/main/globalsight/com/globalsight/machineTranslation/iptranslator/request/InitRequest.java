package com.globalsight.machineTranslation.iptranslator.request;

/**
 * init function request object
 * 
 */
public class InitRequest extends Request
{

    public InitRequest(String key, String from, String to, int maxIdleTime,
            boolean optimized)
    {
        super(key);
        this.from = from;
        this.to = to;
        this.setMaxIdleTime(maxIdleTime);
        this.optimized = optimized;
    }

    public InitRequest()
    {
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

    public boolean isOptimized()
    {
        return optimized;
    }

    public void setOptimized(boolean optimized)
    {
        this.optimized = optimized;
    }

    public int getMaxIdleTime()
    {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime)
    {
        this.maxIdleTime = maxIdleTime;
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private String from;
    private String to;
    private int maxIdleTime;
    private boolean optimized;
}
