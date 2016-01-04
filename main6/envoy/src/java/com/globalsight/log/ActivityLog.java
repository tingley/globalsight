package com.globalsight.log;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.BuildVersion;
import com.globalsight.util.ServerUtil;

/**
 * A helper class for logging activity within GlobalSight.  The goal is to
 * allows us to see everything happening at any moment.
 */
public class ActivityLog
{
    // our normal log
    private static final Logger log = Logger.getLogger(ActivityLog.class);

    // the activity log
    private static final Logger activityLog =
        Logger.getLogger(ActivityLog.class.getName() + ".entry");

    // Log a start-up event so we know when GlobalSight has been restarted.
    // There is no corresponding end event.
    static
    {
        Map<Object,Object> activityArgs = new HashMap<Object,Object>();
        // ServerUtil is more likely to have the correct version
        activityArgs.put("version", ServerUtil.getVersion());
        activityArgs.put("buildDate", BuildVersion.BUILD_DATE);
        start(ActivityLog.class, "<clinit>", activityArgs);
    }

    /**
     * Log the start of an activity.  The activity is represented by the class
     * and method of the caller.  The args contain information about the
     * activity.  The keys and values will be converted to strings with
     * toString.  Be sure they are machine-readable and unambiguous.  Values
     * may be null.  For a given method, the args should always use the same
     * keys, to simplify automated processing.  (Think of them as keyword
     * arguments.)  If you feel you need dynamically named args, enhance this
     * code to allow values to have complex types instead.  For the same
     * reasons, try not to overload a named arg with context-dependent meaning;
     * instead, use differently named args.
     *
     * This method returns a {@link Start} object that is used to log the end
     * of the activity.  You must log an end for every start, even if there is
     * an exception, so put the end in a finally block.  Typical use:
     *
     * <pre>
     * {@code
     * ActivityLog.Start activityStart = null;
     * try {
     *     ...
     *     Map<Object,Object> activityArgs = new HashMap<Object,Object>();
     *     activityArgs.put(...);
     *     activityStart = ActivityLog.start(
     *         MyClass.class, "methodName", activityArgs);
     *     ...
     * } catch (...) {
     *     ...
     * } finally {
     *     if (activityStart != null)
     *     {
     *         activityStart.end();
     *     }       
     * }
     * }
     * </pre>
     *
     * Pass the class in which the call literally resides, not the possible
     * sub-class of the current instance, so it is easy to correlate activity
     * logs with the source.  If there is another relevant class, put it in the
     * args.
     *
     * The method name may be contrived to resolve ambiguity.
     *
     * This method will not throw an exception or return null.
     */
	@SuppressWarnings("rawtypes")
	public static Start start(Class caller, String method,
			Map<Object, Object> args)
    {
        try
        {
            String activity = caller.getName() + "." + method;
            JSONObject json = new JSONObject();
			for (Map.Entry<Object, Object> e : args.entrySet())
			{
				json.put(e.getKey().toString(),
						e.getValue() == null ? JSONObject.NULL : e.getValue()
								.toString());
			}
            activityLog.info("-> " + activity + json);
            return new RealStart(activity);
        }
        catch (Exception e)
        {
            // not much could go wrong, but just in case
            log.error("Problem logging start event", e);
            // give them back a dummy instead of returning null
            return new FalseStart();
        }
    }

    /**
     * Call {@link start(Class, String, Map)} with an empty args map.
     */
    @SuppressWarnings("rawtypes")
	public static Start start(Class caller, String method)
    {
        return start(caller, method, new HashMap<Object,Object>());
    }

    private static void end(String activity, long dt)
    {
        activityLog.info("<- " + activity + " (" +
            String.format("%.3f", dt / 1000f) + "s)");
    }

    /**
     * A helper to simplify logging end events.
     * @see ActivityLog#start.
     */
    public static interface Start
    {
        /**
         * Log the end of an activity.
         *
         * This method will not throw an exception.
         */
        void end();
    }

    private static class RealStart implements Start
    {
        private final String activity;
        private final long t0;

        private RealStart(String activity)
        {
            this.activity = activity;
            t0 = System.currentTimeMillis();
        }

        public void end()
        {
            ActivityLog.end(activity, System.currentTimeMillis() - t0);
        }
    }

    private static class FalseStart implements Start
    {
        public void end() {}
    }
}
