package com.globalsight.restful;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.BuildVersion;
import com.globalsight.util.ServerUtil;

/**
 * A helper class for logging webservice within GlobalSight. The goal is to
 * allows us to see everything happening at any moment.
 */
public class RestWebServiceLog
{
    private static final Logger log = Logger.getLogger(RestWebServiceLog.class);

    // Log a start-up event so we know when GlobalSight has been restarted.
    // There is no corresponding end event.
    static
    {
        Map<Object, Object> webServiceArgs = new HashMap<Object, Object>();
        webServiceArgs.put("version", ServerUtil.getVersion());
        webServiceArgs.put("buildDate", BuildVersion.BUILD_DATE);
        start(RestWebServiceLog.class, "<clinit>", webServiceArgs);
    }

    @SuppressWarnings("rawtypes")
    public static Start start(Class caller, String method, Map<Object, Object> args)
    {
        try
        {
            String activity = caller.getName() + "." + method;
            JSONObject json = new JSONObject();
            for (Map.Entry<Object, Object> e : args.entrySet())
            {
                json.put(e.getKey().toString(), e.getValue() == null ? JSONObject.NULL : e
                        .getValue().toString());
            }

            log.info(" -> " + activity + json);
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
        return start(caller, method, new HashMap<Object, Object>());
    }

    private static void end(String activity, long dt)
    {
        log.info(" <- " + activity + " (" + String.format("%.3f", dt / 1000f) + "s)");
    }

    /**
     * A helper to simplify logging end events.
     * 
     * @see WebServicesLog#start.
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
            RestWebServiceLog.end(activity, System.currentTimeMillis() - t0);
        }
    }

    private static class FalseStart implements Start
    {
        public void end()
        {
        }
    }
}
