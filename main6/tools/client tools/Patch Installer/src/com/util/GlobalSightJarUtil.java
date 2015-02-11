package com.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

public class GlobalSightJarUtil {
	private static Logger log = Logger.getLogger(GlobalSightJarUtil.class);

	public static String PATH = null;

	public static void backFile(String classPath, String hotfixPath)
			throws Exception {
		if (PATH == null) {
			String path = ServerUtil.getPath()
					+ "/jboss/server/standalone/deployments/globalsight.ear/globalsight.jar";
			File tempJar = unzip(path);
			if (tempJar != null) {
				PATH = tempJar.getAbsolutePath() + "/";
			}
		}

		if (PATH != null) {
			classPath = classPath.replace(".class", "");
			File of = new File(PATH + classPath);
			File f = of.getParentFile();
			if (f.exists()) {
				String name = of.getName();
				for (File f1 : f.listFiles()) {
					if (f1.getName().equals(name + ".class")
							|| f1.getName().startsWith(name + "$")) {
						String filePath = getPath(f1);
						String realPath = hotfixPath
								+ "/jboss/server/standalone/deployments/globalsight.ear/lib/classes"
								+ filePath.substring(filePath.indexOf("/com/"));
						FileUtil.copyFile(f1, new File(realPath));
					}
				}
			}
		}
	}

	public static void deleteTempFile() {
		if (PATH != null) {
			File f = new File(PATH);
			FileUtil.deleteFile(f);
		}
	}

	private static String getPath(File file) {
		return file.getPath().replace("\\", "/");
	}

	private static File unzip(String srcZipFile) {

		File tmp = null;

		try {

			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(srcZipFile));

			ZipInputStream zis = new ZipInputStream(bis);

			BufferedOutputStream bos = null;

			ZipEntry entry = null;
			tmp = File.createTempFile("temp", "");
			tmp.delete();

			while ((entry = zis.getNextEntry()) != null) {

				String entryName = entry.getName();
				String path = FileUtil.TEMP_DIRECTORY + "/" + tmp.getName()
						+ "/" + entryName;

				if (entry.isDirectory()) {
					File f = new File(path);
					f.mkdirs();
				} else {
					File f = new File(path);
					f.getParentFile().mkdirs();

					bos = new BufferedOutputStream(new FileOutputStream(path));

					int b = 0;

					while ((b = zis.read()) != -1) {

						bos.write(b);
					}

					bos.flush();
					bos.close();
				}
			}
			zis.close();

		} catch (Exception e) {
			log.error(e);
		}
		return tmp;
	}
}
