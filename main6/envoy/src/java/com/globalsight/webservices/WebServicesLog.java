package com.globalsight.webservices;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.RPCParam;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.BuildVersion;
import com.globalsight.util.ServerUtil;

/**
 * A helper class for logging webservice within GlobalSight. The goal is to
 * allows us to see everything happening at any moment.
 */
public class WebServicesLog
{
    // our normal log
    private static final Logger log = Logger.getLogger(WebServicesLog.class);

    // the webservice log
    private static final Logger webServiceLog =
        Logger.getLogger(WebServicesLog.class.getName() + ".entry");

    // Log a start-up event so we know when GlobalSight has been restarted.
    // There is no corresponding end event.
    static
    {
        Map<Object,Object> webServiceArgs = new HashMap<Object,Object>();
        // ServerUtil is more likely to have the correct version
        webServiceArgs.put("version", ServerUtil.getVersion());
        webServiceArgs.put("buildDate", BuildVersion.BUILD_DATE);
        start(WebServicesLog.class, "<clinit>", webServiceArgs);
    }

	@SuppressWarnings("rawtypes")
	public static Start start(Class caller, String method,
			Map<Object, Object> args)
	{
        try
        {
        	String activity = caller.getName() + "." + method;
        	JSONObject json = new JSONObject();
        	for (Map.Entry<Object,Object> e : args.entrySet())
        	{
				json.put(e.getKey().toString(),
						e.getValue() == null ? JSONObject.NULL : e.getValue()
								.toString());
        	}

        	webServiceLog.info(" -> " + activity + json);
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
		webServiceLog.info(" <- " + activity + " ("
				+ String.format("%.3f", dt / 1000f) + "s)");
    }

    /**
     * A helper to simplify logging end events.
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
            WebServicesLog.end(activity, System.currentTimeMillis() - t0);
        }
    }

    private static class FalseStart implements Start
    {
        public void end() {}
    }

    /**
     * A helper class (along with {@link AxisResponseHandler}) to log start and
     * end events for Web Service calls.  It is configured in
     * server-config.wsdd.
     */
    public static class AxisRequestHandler extends BasicHandler
    {
		private static final long serialVersionUID = -2297990446660258917L;

		private static final Logger log = Logger
				.getLogger(AxisRequestHandler.class);

		@SuppressWarnings("rawtypes")
		public void invoke(MessageContext ctx) throws AxisFault
		{
            // Cribbed from org.apache.axis.providers.java.RPCProvider, but I
            // didn't copy the full logic, so swallow the error if it fails.
            // (If we throw an exception, it gets propagated to the client.)
            try
            {
                Map<Object,Object> activityArgs = new HashMap<Object,Object>();
                RPCElement rpc = (RPCElement) ctx.getRequestMessage().
                    getSOAPEnvelope().getBodyElements().get(0);
                JavaServiceDesc serv = (JavaServiceDesc)
                    ctx.getService().getServiceDescription();
                activityArgs.put("method",
                    serv.getImplClass().getName() + "." + rpc.getMethodName());

                // Ambassador encodes the access token in various way.  Try
                // them all.  If the user isn't logged for some call, find out
                // how the access token is passed for that call and add it.
                String accessToken = null;
                RPCParam accessTokenParam = rpc.getParam("p_accessToken");
                if (accessTokenParam == null)
                {
                    accessTokenParam = rpc.getParam("accessToken");
                }
                if (accessTokenParam != null)
                {
                    accessToken = (String) accessTokenParam.getObjectValue();
                }
                else
                {
                    RPCParam argsParam = rpc.getParam("args");
                    if (argsParam == null)
                    {
                        argsParam = rpc.getParam("p_args");
                    }
                    if (argsParam != null)
                    {
                        Map args = (Map) argsParam.getObjectValue();
                        accessToken = (String) args.get("accessToken");
                    }
                }
                activityArgs.put("user", accessToken == null ? null :
                    AbstractWebService.getUsernameFromSession(accessToken));

				ctx.setProperty(AxisRequestHandler.class.getName() + ".start",
						start(AxisRequestHandler.class, "invoke", activityArgs));
            }
            catch (Exception e)
            {
                log.warn("Failed to log activity start", e);
            }
        }
    }

    /**
     * A helper class (along with {@link AxisRequestHandler}) to log start and
     * end events for Web Service calls.  It is configured in
     * server-config.wsdd.
     */
    public static class AxisResponseHandler extends BasicHandler
    {
		private static final long serialVersionUID = -8535978221379077840L;

		public void invoke(MessageContext ctx)
		{
			WebServicesLog.Start start = (WebServicesLog.Start) ctx
					.getProperty(AxisRequestHandler.class.getName() + ".start");
            // Check in case the request handler failed.
            if (start != null)
            {
                start.end();
            }
        }
    }
}
