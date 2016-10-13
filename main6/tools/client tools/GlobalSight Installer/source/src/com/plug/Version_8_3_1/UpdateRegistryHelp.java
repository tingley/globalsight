package com.plug.Version_8_3_1;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.config.properties.Resource;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.CmdUtil;
import com.util.FileUtil;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;

public class UpdateRegistryHelp {
	private static Logger log = Logger.getLogger(UpdateRegistryHelp.class);
	private static String regFile = "a.reg";
	private boolean updateMaxUserPort = false;
	private boolean updateTcpTimedWaitDelay = false;

	public void run() {
//		if (isSystemNeedUpdate() && isDbNeedUPdate()) {
//			try {
//				checkReg();
//				confiremChangeReg();
//				updateReg();
//			} catch (Exception e) {
//				log.error(e);
//			}
//		}
	}

	private void restartSystem() {
		UI ui = UIFactory.getUI();
		ui.showMessage(Resource.get("msg.updateRegistryFinished"));
		System.exit(0);
//		String[] cmd = new String[]{
//				"net", "stop","lmhosts"
//		};
//		
//		try {
//			CmdUtil.run(cmd);
//		} catch (Exception e) {
//			log.info(e.getMessage());
//		}
//		
//		cmd = new String[]{
//				"net", "start","lmhosts"
//		};
//		
//		try {
//			CmdUtil.run(cmd);
//		} catch (Exception e) {
//			log.error(e);
//			UI ui = UIFactory.getUI();
//			ui.infoError("Failed to restart the “TCP/IP NetBIOS Helper” service.\nPlease restart your computer and re-run the \nUpgrade Installer to continue GlobalSight upgrade. \nError Message " + e.getMessage());
//		}
	}

	private void confiremChangeReg() {
		if (updateMaxUserPort || updateTcpTimedWaitDelay) {
			UI ui = UIFactory.getUI();
			String msg = Resource.get("msg.updateRegistry");

			if (updateMaxUserPort) {
				msg += "\n" + "Set MaxUserPort to 65534";
			}

			if (updateTcpTimedWaitDelay) {
				msg += "\n " + "Set TcpTimedWaitDelay to 30";
			}

			ui.confirmContinue(msg);
		}
	}

	private boolean isSystemNeedUpdate() {
		String os = System.getProperty("os.name");
		log.info("System: " + os);

		return os.contains("Windows");
	}

	@SuppressWarnings("rawtypes")
	private boolean isDbNeedUPdate() {
		DbUtil util = DbUtilFactory.getDbUtil();

		try {
			List count = util.query("select count(id) from CONTAINER_ROLE");
			List c = (List) count.get(0);
			Long n = (Long) (c.get(0));
			if (n <= 0) {
				log.info("Need insert roles");
				return true;
			}

			count = util.query("select count(id) from user");
			c = (List) count.get(0);
			n = (Long) (c.get(0));
			if (n > 0) {
				log.info("Need insert users");
				return true;
			}
		} catch (SQLException e1) {
			if (e1.getMessage().indexOf("doesn't exist") > 0)
			{
				log.info("Tables are not exist");
				return true;
			}
			log.error(e1);
		}

		return false;
	}

	private void updateReg() throws Exception {
		if (updateTcpTimedWaitDelay) {
			File f = new File("./script/plug/8.3.1/GlobalSight_8.3.1.1.reg");
			runFile(f);
		}

		if (updateMaxUserPort) {
			File f = new File("./script/plug/8.3.1/GlobalSight_8.3.1.2.reg");
			runFile(f);
		}
		
		if (updateTcpTimedWaitDelay || updateMaxUserPort)
		{
			restartSystem();
		}
	}

	private void runFile(File f) throws Exception {
		log.info("Run " + f.getAbsolutePath());
		String[] cmd = { "regedit", "/s", f.getAbsolutePath() };
		CmdUtil.run(cmd);
	}

	private boolean checkReg() throws Exception {
		log.info("Checking registry");

		String[] cmd = { "regedit", "/e", regFile,
				"HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters" };

		CmdUtil.run(cmd);

		String content = FileUtil.readFile(new File(regFile), "unicode");

		Integer maxUserPort = getValue("\"MaxUserPort\"=dword:(.*)", content);
		log.info("MaxUserPort : " + maxUserPort);
		updateMaxUserPort = maxUserPort == null || maxUserPort < 65534;

		Integer tcpTimedWaitDelay = getValue(
				"\"TcpTimedWaitDelay\"=dword:(.*)", content);
		log.info("TcpTimedWaitDelay : " + tcpTimedWaitDelay);
		updateTcpTimedWaitDelay = tcpTimedWaitDelay == null
				|| tcpTimedWaitDelay > 30;

		FileUtil.deleteFile(new File(regFile));
		return true;
	}

	private static Integer getValue(String regex, String content) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		if (m.find()) {
			String num = m.group(1);
			int n = Integer.parseInt(num, 16);
			return n;
		}

		return null;
	}
}
