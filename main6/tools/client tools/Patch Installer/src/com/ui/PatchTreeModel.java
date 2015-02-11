package com.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.demo.Hotfix;
import com.demo.HotfixTreeNode;
import com.listener.DataListener;
import com.util.ServerUtil;
import com.util.XmlUtil;

public class PatchTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 4439872399832152113L;

	private static Set<Hotfix> newPatches = new HashSet<Hotfix>();
	private static Set<Hotfix> installedPatches = new HashSet<Hotfix>();

	private static Hotfix selectedHotfix = null;
	private static DefaultMutableTreeNode rootNode = null;
	private static DefaultMutableTreeNode unInstalled = null;
	private static DefaultMutableTreeNode installed = null;

	private static List<DataListener> listerner = new ArrayList<DataListener>();

	public PatchTreeModel(TreeNode node) {
		super(node);
	}

	public static void addDataListener(DataListener l) {
		listerner.add(l);
	}

	public static void fireDataChanged() {
		for (DataListener l : listerner) {
			l.run();
		}
	}

	public static Hotfix getHotfixByName(String name) {
		for (Hotfix h : newPatches) {
			if (name.equals(h.getName())) {
				return h;
			}
		}

		for (Hotfix h : installedPatches) {
			if (name.equals(h.getName())) {
				return h;
			}
		}

		return null;
	}

	public static void updateTreeNode() {
		if (unInstalled == null)
			return;

		unInstalled.removeAllChildren();
		installed.removeAllChildren();

		unInstalled.setUserObject("New Patches (" + newPatches.size() + ")");
		installed.setUserObject("Installed Patches (" + installedPatches.size()
				+ ")");

		ArrayList<Hotfix> fs = new ArrayList<Hotfix>();
		fs.addAll(newPatches);
		Collections.sort(fs, Hotfix.getComparator());
		for (Hotfix h : fs) {
			HotfixTreeNode newNode = new HotfixTreeNode();
			newNode.setUserObject(h);
			unInstalled.add(newNode);
		}

		fs = new ArrayList<Hotfix>();
		fs.addAll(installedPatches);
		Collections.sort(fs, Hotfix.getComparator());
		for (Hotfix h : fs) {
			HotfixTreeNode newNode = new HotfixTreeNode();
			newNode.setUserObject(h);
			installed.add(newNode);
		}
	}

	public static TreeNode getRootTreeNode() {
		if (rootNode == null) {
			if (ServerUtil.getPath() != null) {
				loadHotfix();
			}
			rootNode = new DefaultMutableTreeNode();
			rootNode.setUserObject("GlobalSight Patches");

			unInstalled = new HotfixTreeNode();
			unInstalled
					.setUserObject("New Patches (" + newPatches.size() + ")");

			installed = new HotfixTreeNode();
			installed.setUserObject("Installed Patches ("
					+ installedPatches.size() + ")");

			rootNode.add(unInstalled);
			rootNode.add(installed);

			updateTreeNode();
		}

		return rootNode;
	}

	public static Hotfix getSelectedHotfix() {
		return selectedHotfix;
	}

	public static void setSelectedHotfix(Hotfix selectedHotfix) {
		PatchTreeModel.selectedHotfix = selectedHotfix;
		fireDataChanged();
	}

	public static Set<Hotfix> getNewPatches() {
		return newPatches;
	}

	public static void setNewPatches(Set<Hotfix> newPatches) {
		PatchTreeModel.newPatches = newPatches;
	}

	public static Set<Hotfix> getInstalledPatches() {
		return installedPatches;
	}

	public static List<Hotfix> getAllHotfix() {
		List<Hotfix> hs = new ArrayList<Hotfix>();
		hs.addAll(newPatches);
		hs.addAll(installedPatches);
		return hs;
	}

	public static void setInstalledPatches(Set<Hotfix> installedPatches) {
		PatchTreeModel.installedPatches = installedPatches;
	}

	public static void loadHotfix() {
		installedPatches.clear();
		newPatches.clear();

		File r = new File(ServerUtil.getPath() + "/hotfix");
		File[] fs = r.listFiles();
		if (fs != null) {
			for (File f : fs) {
				String path = f.getAbsolutePath() + "/config.xml";
				File xml = new File(path);
				if (xml.exists()) {
					Hotfix h = XmlUtil.load(Hotfix.class, path);
					if (h.getInstalled()) {
						installedPatches.add(h);
					} else {
						newPatches.add(h);
					}
				}
			}
		}

		updateTreeNode();
	}
}
