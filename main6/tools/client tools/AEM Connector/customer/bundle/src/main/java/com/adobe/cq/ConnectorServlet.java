package com.adobe.cq;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServlet(paths = "/bin/connector", methods = "GET", metatype = true)
public class ConnectorServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 2598426539166789515L;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServerException,
			IOException {

		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		Map<String, String> result = this.getCustomerService()
				.getGlobalsightData();

		try {
			JSONObject obj = new JSONObject();

			if (result != null) {
				Set<Entry<String, String>> set = result.entrySet();

				for (Entry<String, String> entry : set) {
					String key = entry.getKey();
					String value = entry.getValue();
					obj.put(key, value);
				}
			}

			String ip = getServerIpAddr();
			
			if (ip != null) {
				obj.put("ipAddress", ip);
			}

			log.info("request globalsight connector information: " + obj.toString());
			response.getWriter().write(obj.toString());
		} catch (JSONException e) {
			log.error("JSON Exception:", e);
		}
	}

	private String getServerIpAddr() {
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
			log.info("The server IP address : " + ip.getHostAddress());
			return ip.getHostAddress();
		} catch (UnknownHostException e) {
			log.error("get server ipAddress error: ", e);
		}
		return null;
	}

	private CustomerService getCustomerService() {
		BundleContext ctx = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
		ServiceReference serviceReference = ctx
				.getServiceReference(CustomerService.class.getName());
		return CustomerService.class.cast(ctx.getService(serviceReference));
	}
	
}
