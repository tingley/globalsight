package com.globalsight.ling.tm3.core;

import java.util.Date;

/**
 * Essentially, the combination of a locale and a content value, plus
 * associated metadata.  This may serve the purpose of either a source
 * segment or a target translation.  TUV are always owned by a TU.
 * 
 * Access to the value is provided using the TM3TuvData interface, which
 * should be provided by client code along with a TM3DataFactory 
 * implementation capable of creating new TM3TuvData instances from the 
 * serialized (DB) form.
 */
public class TM3Tuv<T extends TM3Data> {
    private Long id;
    private TM3Locale locale;
    private String serializedData;
    private Long fingerprint;
    private TM3Tu<T> tu;
    private TM3Event firstEvent, latestEvent;

    private String creationUser;
    private Date creationDate;
    private String modifyUser;
    private Date modifyDate;
    private Date lastUsageDate = null;
    private long jobId = -1;
    private String jobName = null;
    private long previousHash = -1;
    private long nextHash= -1;
    private String sid = null;
    
    private T data; // Transient
    private TM3EventLog eventLog;
    private TuStorage<T> storage;

    TM3Tuv() { }

    TM3Tuv(TM3Locale locale, T data, TM3Event creationEvent,
            String creationUser, Date creationDate, String modifyUser,
			Date modifyDate, Date lastUsageDate, long jobId, String jobName,
			long previousHash, long nextHash, String sid)
    {
        this.locale = locale;
        setContent(data);
        this.firstEvent = creationEvent;
        this.latestEvent = creationEvent;
        this.creationUser = creationUser;
        this.creationDate = creationDate;
        this.modifyUser = modifyUser;
        this.modifyDate = modifyDate;
        this.lastUsageDate = lastUsageDate;
        this.jobId = jobId;
        this.jobName = jobName;
        this.previousHash = previousHash;
        this.nextHash = nextHash;
        this.sid = sid;
    }

    public Long getId() {
        return id;
    }
    
    void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Return the TU to which this TUV belongs.
     * @return Owning TU
     */
    public TM3Tu<T> getTu() {
        return tu;
    }
    
    void setTu(TM3Tu<T> tu) {
        this.tu = tu;
    }
    
    public boolean isSource() {
        // This could also just be a flag
        return equals(tu.getSourceTuv());
    }
    
    public long getFingerprint() {
        if (fingerprint == null)
        {
            calculateFingerprint();
        }
        return fingerprint;
    }

    private void calculateFingerprint() {
        fingerprint = data.getFingerprint();
    }

    protected void setFingerprint(long fingerprint) {
        this.fingerprint = fingerprint;
    }

    public TM3Locale getLocale() {
        return locale;
    }

    void setLocale(TM3Locale locale) {
        this.locale = locale;
    }
    
    public T getContent() {
        if (data == null) {
            TM3DataFactory<T> factory = getTu().getTm().getDataFactory();
            data = factory.fromSerializedForm(locale, serializedData);
        }
        return data;
    }
    
    public void setContent(T data) {
        this.data = data;
        // Update the fields that are persisted into the DB
        this.serializedData = data.getSerializedForm();
        // Compute the fingerprint lazily
        this.fingerprint = null;
    }

    public TM3Event getFirstEvent() {
        return firstEvent;
    }
    
    void setFirstEvent(TM3Event firstEvent) {
        this.firstEvent = firstEvent;
    }    
    
    /**
     * Returns the most recent event affecting this TUV.  
     * Note that this value can not be set directly; it will be
     * updated automatically to the event passed in calls like
     * {@link TM3Tm#save()} or {@link TM3Tm#modifyTu(TM3Tu, TM3Event)}.
     * @return most recent event affecting this tuv
     */
    public TM3Event getLatestEvent() {
        return latestEvent;
    }
    
    void setLatestEvent(TM3Event latestEvent) {
        this.latestEvent = latestEvent;
    }
    
    protected TuStorage<T> getStorage() {
        return storage;
    }
    
    void setStorage(TuStorage<T> storage) {
        this.storage = storage;
    }
    
    // For persistence
    String getSerializedForm() {
        return serializedData;
    }
    
    void setSerializedForm(String serializedForm) {
        this.serializedData = serializedForm;
    }
   
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TM3Tuv<?>)) {
            return false;
        }
        TM3Tuv<?> tuv = (TM3Tuv<?>)o;
        return getTu().equals(tuv.getTu()) &&
               getLocale().equals(tuv.getLocale()) &&
               getSerializedForm().equals(tuv.getSerializedForm());
    }
    
    @Override
    public String toString() {
        return getContent() + "(" + getLocale().toString() + ")"; 
    }

    public String getCreationUser()
    {
        return creationUser;
    }

    public void setCreationUser(String creationUser)
    {
        this.creationUser = creationUser;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getModifyUser()
    {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser)
    {
        this.modifyUser = modifyUser;
    }

    public Date getModifyDate()
    {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate)
    {
        this.modifyDate = modifyDate;
    }

	public Date getLastUsageDate() {
		return lastUsageDate;
	}

	public void setLastUsageDate(Date lastUsageDate) {
		this.lastUsageDate = lastUsageDate;
	}

	public long getJobId() {
		return jobId;
	}

	public void setJobId(long jobId) {
		this.jobId = jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public long getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(long previousHash) {
		this.previousHash = previousHash;
	}

	public long getNextHash() {
		return nextHash;
	}

	public void setNextHash(long nextHash) {
		this.nextHash = nextHash;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}
}
