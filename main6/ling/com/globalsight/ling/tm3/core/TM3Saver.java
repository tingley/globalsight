package com.globalsight.ling.tm3.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An object that allows complex save requests to be sent to the TM
 * in a somewhat orderly way.
 * <p>
 * A sample invocation might look like this:
 * <pre>
 *   TM3Saver<T> saver = tm.createSaver();
 *   for (<i>some condition</i>) {
 *      saver.tu(srcContent, srcLocale, event)
 *           .attr(attr1, value1)
 *           .attr(attr2, value2)
 *           .tuv(frenchContent, frenchLocale, event)
 *           .tuv(germanContent, germanLocale, event);
 *   }
 *   saver.save(TM3SaveMode.MERGE);
 * </pre>
 */
public abstract class TM3Saver<T extends TM3Data>
{
    List<Tu> tus = new ArrayList<Tu>();
    
    TM3Saver()
    {
    }
    
    /**
     * Add a new TU to this save operation, identified by its 
     * source TUV.  Additional calls may be made to the returned
     * Tu object to add targets TUV, attribute values, etc.
     * <p>
     * Note that nothing will be saved to the TM until a call to
     * {{@link #save(TM3SaveMode)} is made. 
     * @param content source content
     * @param locale source locale
     * @param event source tuv event
     * @return
     */
	public Tu tu(T content, TM3Locale locale, TM3Event event,
			String creationUser, Date creationDate, String modifyUser,
			Date modifyDate, Date lastUsageDate, long jobId, String jobName,
			long previousHash, long nextHash, String sid)
    {
		Tu tu = new Tu(content, locale, event, creationUser, creationDate,
				modifyUser, modifyDate, lastUsageDate, jobId, jobName,
				previousHash, nextHash, sid);
        tus.add(tu);
        return tu;
    }
    
    /**
     * Update the TM based on the contents of this saver.  This will
     * flush all TU and TUV to the database.
     * @param mode Save mode
     * @return
     * @throws TM3Exception
     */
    public abstract List<TM3Tu<T>> save(TM3SaveMode mode, boolean indexTarget)
            throws TM3Exception;
    
    /**
     * Representation of an unsaved TU, created by a call to 
     * {@link TM3Saver#tu(TM3Data, TM3Locale, TM3Event)}.  Method calls
     * on this object can add target TUV data or attributes.
     */
    public class Tu
    {
        Tuv srcTuv;
        List<Tuv> targets = new ArrayList<Tuv>();
        Map<TM3Attribute, Object> attrs = new HashMap<TM3Attribute, Object>();

		Tu(T content, TM3Locale locale, TM3Event event, String creationUser,
                Date creationDate, String modifyUser, Date modifyDate,
				Date lastUsageDate, long jobId, String jobName,
				long previousHash, long nextHash, String sid)
        {
			srcTuv = new Tuv(content, locale, event, creationUser,
					creationDate, modifyUser, modifyDate, lastUsageDate, jobId,
					jobName, previousHash, nextHash, sid);
        }

        /**
         * Add a single attribute/value pair to this TU.
         * @param attr attribute
         * @param value value
         * @return this
         */
        public Tu attr(TM3Attribute attr, Object value) {
            attrs.put(attr, value);
            return this;
        }

        /**
         * Add multiple attribute/value pairs to this TU. 
         * @param pairs attribute/value pairs
         * @return this
         */
        public Tu attrs(Map<TM3Attribute, Object> pairs) {
            if (pairs != null) {
                attrs.putAll(pairs);
            }
            return this;
        }

        /**
         * Add a single target TUV to this TU.
         * @param content target content
         * @param locale target locale
         * @param event target TUV event
         * @return this
         */
		public Tu target(T content, TM3Locale locale, TM3Event event,
				String creationUser, Date creationDate, String modifyUser,
				Date modifyDate, Date lastUsageDate, long jobId,
				String jobName, long previousHash, long nextHash, String sid)
        {
			targets.add(new Tuv(content, locale, event, creationUser,
					creationDate, modifyUser, modifyDate, lastUsageDate, jobId,
					jobName, previousHash, nextHash, sid));
            return this;
        }
        
        /**
         * Update the TM based on the contents of this saver.  This will
         * flush all TU and TUV to the database.  This method is provided
         * for convenience and is equivalent to calling 
         * <tt>saver.save(mode)</tt>.
         * @param mode Save mode
         * @return
         * @throws TM3Exception
         */
        public List<TM3Tu<T>> save(TM3SaveMode mode, boolean indexTarget)
        {
            return TM3Saver.this.save(mode, indexTarget);
        }
    }
    
    public class Tuv
    {
        T content;
        TM3Locale locale;
        TM3Event event;
        String creationUser = null;
        Date creationDate = null;
        String modifyUser = null;
        Date modifyDate = null;
        Date lastUsageDate = null;
        long jobId = -1;
        String jobName = null;
        long previousHash = -1;
        long nextHash = -1;
        String sid = null;

		Tuv(T content, TM3Locale locale, TM3Event event, String creationUser,
				Date creationDate, String modifyUser, Date modifyDate,
				Date lastUsageDate, long jobId, String jobName,
				long previousHash, long nextHash, String sid)
        {
            this.content = content;
            this.locale = locale;
            this.event = event;
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

        public T getContent()
        {
            return content;
        }

        public TM3Locale getLocale()
        {
            return locale;
        }

        public TM3Event getEvent()
        {
            return event;
        }

        public String getCreationUser()
        {
            return creationUser;
        }

        public Date getCreationDate()
        {
            return creationDate;
        }

        public String getModifyUser()
        {
            return modifyUser;
        }

        public Date getModifyDate()
        {
            return modifyDate;
        }

        public Date getLastUsageDate()
        {
        	return lastUsageDate;
        }

        public long getJobId()
        {
        	return jobId;
        }

        public String getJobName()
        {
        	return jobName;
        }

        public long getPreviousHash()
        {
        	return previousHash;
        }

        public long getNextHash()
        {
        	return nextHash;
        }

        public String getSid()
        {
        	return sid;
        }
    }
}
