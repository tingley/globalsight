package com.demo;

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import com.listener.ProcessListener;
import com.ui.MainUI;
import com.ui.PatchTreeModel;
import com.util.Assert;
import com.util.CmdUtil;
import com.util.FileUtil;
import com.util.GlobalSightJarUtil;
import com.util.InstallUtil;
import com.util.Resource;
import com.util.ServerUtil;
import com.util.XmlUtil;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;

@XmlRootElement
public class Hotfix {

	private static Logger log = Logger.getLogger(Hotfix.class);

	private String name;
	private String description;
	private String dependency;
	private List<String> filePath = new ArrayList<String>();
	private boolean installed = false;
	private String sequence;
	private String date;
	private String version;
	private Set<ProcessListener> listeners = new HashSet<ProcessListener>();

	private List<File> filesNeedCheck = new ArrayList<File>();
	private List<File> allFiles = new ArrayList<File>();
	private static List<String> ignoreFiles = new ArrayList<String>();
	static {
		ignoreFiles.add("system.xml");
	}

	private String NEW_ROOT = "/new/GlobalSight";
	private String OLD_ROOT = "/ori/GlobalSight";

	private String NEW_ROOT_2 = "/new";
	private String OLD_ROOT_2 = "/ori";

	private int process = 0;
	private String msg = "";

	private String root = "";
	private static final String STATISTIC = "statistic";
	private static final String BACKUP = "back";
	private static final String COPY = "copy";
	private static final String UPDATE_JAR = "jar";

	private static final String EAR_PATH = "/jboss/server/standalone/deployments/globalsight.ear";
	private static final String LIB_PATH = EAR_PATH + "/lib";
	private static final String GLOBLASIGHT_JAR = LIB_PATH + "/globalsight.jar";
	private static final String CLASS_PATH = LIB_PATH + "/classes";
	private boolean hasClass = false;
	private boolean hasTemp = false;

	private static Map<String, Integer> RATES = new HashMap<String, Integer>();
	static {
		RATES.put(STATISTIC, 200000);
		RATES.put(BACKUP, 300000);
		RATES.put(COPY, 300000);
		RATES.put(UPDATE_JAR, 200000);
	}

	public Hotfix() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<String> getFilePath() {
		return filePath;
	}

	public void setFilePath(List<String> filePath) {
		this.filePath = filePath;
	}

	public boolean getInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public Hotfix(String name, String version, String description,
			String sequence) {
		super();
		this.name = name;
		this.version = version;
		this.description = description;
		this.sequence = sequence;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private List<String> getDepends() {
		List<String> ds = new ArrayList<String>();

		if (dependency == null)
			return ds;

		dependency = dependency.trim();
		String[] ss = dependency.split(",");
		for (String s : ss) {
			s = s.trim();
			if (s.length() > 0) {
				ds.add(s);
			}
		}

		return ds;
	}
	
	public void remove()
	{
		File f = new File(getRootPath());
		if (f.exists())
		{
			FileUtil.deleteFile(f);
			PatchTreeModel.loadHotfix();
			MainUI.tree.updateUI();
			PatchTreeModel.setSelectedHotfix(null);
		}
	}

	public String getDependHotfixAsString() {

		List<String> ds = getDepends();

		if (ds.size() == 0)
			return "N/A";

		String s = "";

		for (String h : ds) {
			if (s.length() > 0) {
				s += ", ";
			}

			s += h;
		}

		return s;
	}

	private void updateJar() throws Exception {
		log.info("Updating jar");

		updateProcess(0, "Updating globalsight.jar");

		File f = new File(getPath() + CLASS_PATH + "/com");
		if (f.exists()) {
			if (ServerUtil.isInLinux()) {
				String[] cmd = { "sh", "script/linux/updateJar.sh",
						ServerUtil.getPath(), getPath() + CLASS_PATH };
				CmdUtil.run(cmd, false);
			} else {
				String[] cmd = { "cmd", "/c", "jar", "uvf",
						ServerUtil.getPath() + GLOBLASIGHT_JAR, "-C",
						getPath() + CLASS_PATH, "com" };
				CmdUtil.run(cmd);
			}
		}

		updateProcess(getProgress(UPDATE_JAR));
		log.info("Updating jar finished");
	}

	private String getPath() {

		return getRootPath() + root;
	}

	public boolean hasClassFile() {
		return hasClass;
	}

	private String getOldRoot() {
		if (NEW_ROOT_2.equals(getNewRoot())) {
			return OLD_ROOT_2;
		}

		return OLD_ROOT;
	}

	private String getNewRoot() {
		File aRoot = new File(getRootPath() + NEW_ROOT);
		if (!aRoot.exists()) {
			return NEW_ROOT_2;
		}

		return NEW_ROOT;
	}

	private void backup() throws Exception {
		File aRoot = new File(getRootPath() + OLD_ROOT_2);
		if (aRoot.exists())
			return;

		updateProcess(0, "Backing up...");

		List<File> copyFiles = new ArrayList<File>();
		List<File> classFiles = new ArrayList<File>();

		for (File file : allFiles) {
			if (!file.getName().endsWith(".class") || file.getAbsolutePath().contains("globalsightServices.war")) {
				copyFiles.add(file);
			} else {
				classFiles.add(file);
			}
		}

		int size = copyFiles.size() + classFiles.size();
		log.info("File size: " + size);
		int processTotle = getProgress(BACKUP);

		if (size == 0) {
			updateProcess(processTotle);
			return;
		}

		int rate = processTotle / size;
		int lose = processTotle - rate * size;

		for (int i = 0; i < copyFiles.size(); i++) {
			File f = copyFiles.get(i);
			updateProcess(rate, MessageFormat.format(
					Resource.get("process.backup.files"), i + 1, size,
					f.getName()));
			backupFile(f);
		}

		for (int i = 0; i < classFiles.size(); i++) {
			File f = classFiles.get(i);
			updateProcess(rate, MessageFormat.format(
					Resource.get("process.backup.files"), copyFiles.size() + i
							+ 1, size, f.getName()));
			backupClassFile(f);
		}

		GlobalSightJarUtil.deleteTempFile();
		updateProcess(lose);
		log.info("Copying files finished");
	}

	/**
	 * Copies files from patch to server.
	 * <p>
	 * Note: if a file will not copy to server if the name has been included in
	 * <code>ignoreFiles</<code>,
	 * 
	 * @throws Exception
	 * 
	 * @throws Exception
	 *             A exception will be throw out if copy file failed.
	 */
	public void copy() throws Exception {
		log.info("Start copying files");

		List<File> copyFiles = new ArrayList<File>();
		for (File file : allFiles) {
			if (!file.getName().endsWith(".class") || file.getPath().contains("globalsightServices.war")) {
				copyFiles.add(file);
			} else {
				hasClass = true;
			}
		}

		int size = copyFiles.size();
		log.info("File size: " + size);

		int processTotle = getProgress(COPY);

		if (size == 0) {
			updateProcess(processTotle);
			return;
		}
		int rate = processTotle / size;
		int lose = processTotle - rate * size;

		for (int i = 0; i < size; i++) {
			File f = copyFiles.get(i);
			copyFile(f);
			updateProcess(rate, MessageFormat.format(
					Resource.get("process.update.files"), i + 1, size,
					f.getName()));
		}

		updateProcess(lose);
		log.info("Copying files finished");
	}

	private Integer getProgress(String key) {
		Assert.assertNotNull(key, "Press key");

		return RATES.get(key);
	}

	private void copyFile(File file) throws Exception {
		String path = file.getCanonicalPath().replace("\\", "/");
		String prefixPath = new File(getPath()).getCanonicalPath().replace(
				"\\", "/");
		path = path.replace(prefixPath, "");
		File targetFile = new File(ServerUtil.getPath(), path);

		FileUtil.copyFile(file, targetFile);
	}

	private void backupFile(File file) throws Exception {
		String path = file.getCanonicalPath().replace("\\", "/");
		String prefixPath = new File(getRootPath() + getNewRoot()).getCanonicalPath().replace(
				"\\", "/");
		path = path.replace(prefixPath, "");
		File targetFile = new File(ServerUtil.getPath(), path);

		if (targetFile.exists())
		{
			FileUtil.copyFile(targetFile, new File(getRootPath() + getOldRoot()
					+ path));
		}
	}

	private void backupClassFile(File file) throws Exception {
		String path = file.getCanonicalPath().replace("\\", "/");
		path = path.substring(path.indexOf("/com/"));
		GlobalSightJarUtil.backFile(path, getRootPath() + getOldRoot());
	}

	public List<File> getAllFiles() {
		if (allFiles.size() == 0) {
			ListAllFiles(true);
		}

		return allFiles;
	}

	private boolean checkClassPath() {
		if (hasClass) {
			File f = new File(getPath() + CLASS_PATH + "/com");
			if (!f.exists()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Lists all files included in patch, and find sql files.
	 */
	public void ListAllFiles(boolean isInstall) {
		hasClass = false;
		hasTemp = false;
		
		log.info("Listing all files in patch");
		updateProcess(1000);

		root = isInstall ? getNewRoot() : getOldRoot();
		File aRoot = new File(getRootPath() + root);
		// not copy to server
		if (!aRoot.exists()) {
			return;
		}

		filesNeedCheck.add(aRoot);
		allFiles = new ArrayList<File>();

		while (!filesNeedCheck.isEmpty()) {
			File f = filesNeedCheck.remove(0);
			for (File cf : f.listFiles()) {
				if (isIgnoreFile(cf)) {
					continue;
				}

				if (cf.isDirectory()) {
					filesNeedCheck.add(cf);
				} else {
					allFiles.add(cf);
					if (cf.getName().endsWith(".class")) {
						hasClass = true;
					}
					if (cf.getName().endsWith(".template")) {
						hasTemp = true;
					}
				}
			}
		}

		updateProcess(getProgress(STATISTIC));
		log.info("Listing file finished");
	}

	private boolean isIgnoreFile(File f) {
		return ignoreFiles.contains(f.getName());
	}

	public boolean checkVersion() {
		log.info("Checking version...");
		String version = ServerUtil.getVersion();
		log.info("Server version: " + version);
		log.info("Patch version: " + this.version);
		return this.version.equals(version);
	}

	public String checkDepend() {

		ArrayList<Hotfix> fs = new ArrayList<Hotfix>();
		fs.addAll(getAllDepend(false));
		Collections.sort(fs, getComparator());

		return changeToString(fs);
	}

	private String changeToString(ArrayList<Hotfix> fs) {
		String s = "";

		int i = 1;

		for (Hotfix h : fs) {
			if (s.length() > 0) {
				s += "\n";
			}

			s += "    " + i + ". " + h.getName();
			i++;
		}

		return s;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String checkRedepend(List<Hotfix> hs) {
		ArrayList<Hotfix> fl = new ArrayList<Hotfix>();
		HashSet<Hotfix> fs = new HashSet<Hotfix>();
		for (Hotfix h : hs) {
			if (h.installed) {
				HashSet<Hotfix> dh = h.getAllDepend(true);
				if (dh.contains(this)) {
					fs.add(h);
				}
			}
		}

		fl.addAll(fs);
		Collections.sort(fl, getComparator());

		return changeToString(fl);
	}

	private HashSet<Hotfix> getAllDepend(boolean installed) {

		List<String> ds = getDepends();

		HashSet<Hotfix> fs = new HashSet<Hotfix>();

		for (String s : ds) {
			if (s == null || s.trim().length() == 0)
				continue;

			Hotfix h = PatchTreeModel.getHotfixByName(s.trim());
			if (h == null) {
				h = new Hotfix();
				h.setName(s);
			}

			if (installed == h.installed) {
				fs.add(h);
				fs.addAll(h.getAllDepend(installed));
			}
		}

		return fs;
	}

	private void initProcess() {
		process = 0;
		fireProcessChanged();
	}

	private void endProcess() {
		process = 1000000;
		msg = Resource.get("process.done");
		fireProcessChanged();
	}

	public boolean install() throws Exception {
		msg = "Installing...";
		initProcess();
		ListAllFiles(true);
		if (!checkClassPath()) {
			JOptionPane.showMessageDialog(null,
					Resource.get("msg.invalid.patch"));
			return false;
		}
		backup();
		copy();
		updateJar();
		InstallUtil u = new InstallUtil();
		if (hasTemp) {
			u.parseAllTemplates();
		}
		updateDb();
		installed = true;
		saveConfig();
		endProcess();
		return true;
	}
	
	public boolean hasSqlFiles()
	{
		File[] fs = getSqlFiles();
		return fs != null && fs.length > 0;
	}

	private File[] getSqlFiles()
	{
		File f = new File(getRootPath());
		if (f.exists()) {
			File[] fs = f.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return pathname.getName().toLowerCase().endsWith(".sql");
				}
			});
			
			return fs;
		}
		
		return null;
	}
	
	private void updateDb() {
		DbUtil db = DbUtilFactory.getDbUtil();
		File[] fs = getSqlFiles();
		if (fs != null)
		{
			for (File sf : fs) {
				try {
					String msg = MessageFormat.format(Resource.get("process.sql"),
                            sf.getName());
                    log.info(msg);
                    updateProcess(0, msg);
                    db.execSqlFile(sf);
                    log.info("   " + sf.getName() + " : successful");
					
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null,
							MessageFormat.format(Resource
		                            .get("msg.executeSql"), sf.getAbsolutePath(), e
		                            .getMessage()));
					
					log.error(e);
				}
			}
		}
	}

	public boolean rollback() throws Exception {
		msg = "Uninstalling...";
		initProcess();
		ListAllFiles(false);
		if (!checkClassPath()) {
			JOptionPane.showMessageDialog(null,
					Resource.get("msg.invalid.patch"));
			return false;
		}
		copy();
		updateJar();
		if (hasTemp) {
			InstallUtil u = new InstallUtil();
			u.parseAllTemplates();
		}
		installed = false;
		saveConfig();
		endProcess();
		return true;
	}

	private void saveConfig() {
		File f = new File(getRootPath());
		if (f.exists()) {
			XmlUtil.save(this, getRootPath() + "/config.xml");
		}
	}

	private void updateProcess(int n) {
		process += n;
		fireProcessChanged();
	}

	private void updateProcess(int n, String msg) {
		process += n;
		this.msg = msg;
		fireProcessChanged();
	}

	public void addProcessListener(ProcessListener p) {
		listeners.add(p);
	}

	public void fireProcessChanged() {
		for (ProcessListener p : listeners) {
			p.processChanged(process, msg);
		}
	}

	public String getRootPath() {
		return ServerUtil.getPath() + "/hotfix/" + name;
	}

	public static Comparator<Hotfix> getComparator() {
		return new Comparator<Hotfix>() {

			@Override
			public int compare(Hotfix o1, Hotfix o2) {
				
				String bv1 = o1.getVersion();
				String bv2 = o2.getVersion();
				if (!bv1.equals(bv2))
				{
					return bv1.compareTo(bv2);
				}

				String v1 = o1.getSequence();
				String v2 = o2.getSequence();

				if (v1 == null || v2 == null)
					return -1;

				String[] vv1 = v1.split("\\.");
				String[] vv2 = v2.split("\\.");

				int n1 = Integer.parseInt(vv1[vv1.length - 1]);
				int n2 = Integer.parseInt(vv2[vv2.length - 1]);

				return n1 - n2;
			}
		};
	}

	public static boolean isHotfix(String path) {
		File f = new File(path + "/config.xml");
		if (f.exists()) {
			Hotfix h = XmlUtil.load(Hotfix.class, path + "/config.xml");
			if (h != null && h.getName() != null && h.getName().length() > 0) {
				return true;
			}
		}
		return false;
	}

	public String getDependency() {
		return dependency;
	}

	@Override
	public String toString() {
		return name + " (" + version + ")";
	}

	public void setDependency(String dependency) {
		this.dependency = dependency;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hotfix other = (Hotfix) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
