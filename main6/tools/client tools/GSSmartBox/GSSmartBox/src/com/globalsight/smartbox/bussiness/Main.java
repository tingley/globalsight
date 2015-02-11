package com.globalsight.smartbox.bussiness;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import com.globalsight.smartbox.bo.CompanyConfiguration;
import com.globalsight.smartbox.bussiness.polling.InboxPolling;
import com.globalsight.smartbox.bussiness.polling.JobCreatePolling;
import com.globalsight.smartbox.bussiness.polling.JobDownloadPolling;
import com.globalsight.smartbox.bussiness.polling.Polling;
import com.globalsight.smartbox.bussiness.xo.SmartConfiguration;
import com.globalsight.smartbox.util.LogUtil;

/**
 * Main Entry for GSSmartBox
 * 
 * @author Leon
 * 
 */
public class Main implements WrapperListener {
	private static SmartConfiguration config;
	private static Polling inboxPolling;
	private static Polling jobCreatePolling;
	private static Polling jobDownloadPolling;

	private static final String VERSION = "1.1";
	private static final String GS_VERSION = "9.0";

	// Main entry for GSSmartBox
	public static void main(String args[]) {
		LogUtil.info("*********************GSSmartBox service starting...*********************");
		LogUtil.info("*********************GSSmartBox Version: " + VERSION + "       *********************");
		LogUtil.info("GSSmartBox init...");
		config = new SmartConfiguration();
		CompanyConfiguration cpConfig = config.init();
		if (cpConfig == null) {
			LogUtil.FAILEDLOG.error("Init error, please check the configuration and restart service...");
			return;
		}
		LogUtil.info("GSSmartBox init successfully...");
		LogUtil.info("GSSmartBox ProcessCase is " + cpConfig.getProcessCase() + "...");

		inboxPolling = new InboxPolling(cpConfig);
		inboxPolling.start();
		jobCreatePolling = new JobCreatePolling(cpConfig);
		jobCreatePolling.start();
		jobDownloadPolling = new JobDownloadPolling(cpConfig);
		jobDownloadPolling.start();

		WrapperManager.start(new Main(), args);
	}

	@Override
	public void controlEvent(int arg0) {
		System.out.println("Control Event...");
	}

	@Override
	public Integer start(String[] arg0) {
		System.out.println("GSSmartBox Starting...");
		return null;
	}

	@Override
	public int stop(int exitCode) {
		System.out.println("GSSmartBox Stoping...");
		inboxPolling.stop();
		jobCreatePolling.stop();
		jobDownloadPolling.stop();
		LogUtil.info("*********************GSSmartBox Service Stopping...*********************");

		return exitCode;
	}
}
