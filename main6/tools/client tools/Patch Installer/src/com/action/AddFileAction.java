package com.action;

import java.awt.FileDialog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import com.demo.Hotfix;
import com.ui.MainUI;
import com.ui.PatchTreeModel;
import com.util.FileUtil;
import com.util.Resource;
import com.util.ServerUtil;

public class AddFileAction {
	private static Logger log = Logger.getLogger(AddFileAction.class);
	
	public void run() {

		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Add GlobalSight Patch");
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "*.zip";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().matches(".*\\.zip");
			}
		});
		int result = jfc.showOpenDialog(null);
		File file = null;
		if (JFileChooser.APPROVE_OPTION == result) {
			file = jfc.getSelectedFile();
			addPatch(file.getAbsolutePath());
		}
	}
	
	public void addPatch(String path)
	{
		String error = unzip(path);
		if (error != null) {
			JOptionPane.showMessageDialog(null, error);
		}
		PatchTreeModel.loadHotfix();
		MainUI.tree.updateUI();
	}

	private String unzip(String srcZipFile) {

		File tmp = null;
		List<String> uninstallPatchs = new ArrayList<String>();

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

			File r = new File(FileUtil.TEMP_DIRECTORY + "/" + tmp.getName());
			File[] fs = r.listFiles();

			for (File f : fs) {
				if (f.isDirectory()) {
					if (Hotfix.isHotfix(f.getAbsolutePath())) {
						try {

							File root = new File(ServerUtil.getPath()
									+ "/hotfix/" + f.getName());

							Hotfix h = PatchTreeModel.getHotfixByName(f
									.getName());
							if (h != null && h.getInstalled()) {
								uninstallPatchs.add(f.getName());
								continue;
							}

							if (root.exists()) {
								FileUtil.deleteFile(root);
							}

							FileUtil.copyAllFile(f,
									new File(ServerUtil.getPath() + "/hotfix/"
											+ f.getName()));
						} catch (Exception e) {
							log.error(e);
						}
					} else {
						log.error(Resource.get("log.invalid.patch") + ": "
								+ f.getAbsolutePath());
					}
				}
			}

		} catch (Exception e) {

			log.error(e);e.printStackTrace();
			return Resource.get("msg.invalid.patch");
		} finally {
			if (tmp != null)
				FileUtil.deleteFile(new File(FileUtil.TEMP_DIRECTORY + "/"
						+ tmp.getName()));
		}

		if (uninstallPatchs.size() > 0) {
			String s = Resource.get("msg.patch.exist");

			int i = 0;
			for (String e : uninstallPatchs) {
				i++;
				s += "\n    " + i + ". " + e;
			}

			return s;
		}

		return null;

	}
}
