package com.globalsight.everest.comment;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.tuv.TuvImpl;

public class IssueEditionRelation extends PersistentObject
{
	private static final long serialVersionUID = -1275637165851849002L;

	private long originalTuId ;
	private long originalTuvId;
	private String originalIssueHistoryId;
	private TuvImpl tuv;
	
	public IssueEditionRelation()
	{		
	}
	
	public IssueEditionRelation(long p_originalTuId, long p_originalTuvId, 
	        TuvImpl p_newTuv, String p_originalIssueHistoryId)
	{		
		this.originalTuId = p_originalTuId;
		this.originalTuvId = p_originalTuvId;
		this.originalIssueHistoryId = p_originalIssueHistoryId;
		this.tuv = p_newTuv;
	}
	
	public long getOriginalTuId() 
	{
		return originalTuId;
	}
	
	public void setOriginalTuId(long originalTuId) 
	{
		this.originalTuId = originalTuId;
	}
	
	public long getOriginalTuvId() 
	{
		return originalTuvId;
	}
	
	public void setOriginalTuvId(long originalTuvId) 
	{
		this.originalTuvId = originalTuvId;
	}
	
	public String getOriginalIssueHistoryId() 
	{
		return originalIssueHistoryId;
	}
	
	public void setOriginalIssueHistoryId(String originalIssueHistoryId) 
	{
		this.originalIssueHistoryId = originalIssueHistoryId;
	}
	
    public TuvImpl getTuv() {
        return tuv;
    }
    
    public void setTuv(TuvImpl tuvimpl) {
        tuv = tuvimpl;
    }

}
