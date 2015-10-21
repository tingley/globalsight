package com.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.action.AddFileAction;
import com.action.RemoveActionListener;
import com.demo.Hotfix;
import com.demo.HotfixTreeNode;
import com.listener.DataListener;
import com.listener.ProcessListener;
import com.util.HelpUtil;
import com.util.Resource;
import com.util.ServerUtil;

public class MainUI implements DataListener, ProcessListener {

	private static Logger log = Logger.getLogger(Hotfix.class);

	private JFrame frmGlobalsightPatchInstaller;
	private final Action action = new SwingAction();
	private final Action action_1 = new SwingAction_1();
	private JLabel vName = new JLabel("");
	private JLabel vSequence = new JLabel("");
	private JLabel vDate = new JLabel("");
	private JLabel vInstalled = new JLabel("");
	private JLabel vDependency = new JLabel("");
	private JButton bInstall = new JButton("Install");
	private JButton bUninstall = new JButton("Uninstall");
	private JProgressBar progressBar = new JProgressBar();
	private JLabel message = new JLabel(" ");
	public static JTree tree = null;
	private final Action action_2 = new SwingAction_2();
	private JLabel lb_server = new JLabel("");
	private final Action action_3 = new SwingAction_3();
	private JTextPane textPane = new JTextPane();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

//		try {
//		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//		        if ("Nimbus".equals(info.getName())) {
//		            UIManager.setLookAndFeel(info.getClassName());
//		            break;
//		        }
//		    }
//		} catch (Exception e) {
//		    // If Nimbus is not available, you can set the GUI to another look and feel.
//		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					ServerUtil.initPath();

					MainUI window = new MainUI();
					window.frmGlobalsightPatchInstaller.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainUI() {
		PatchTreeModel.addDataListener(this);
		setHelpLocation();
		initialize();
		setLocation();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmGlobalsightPatchInstaller = new JFrame();

		new DropTarget(frmGlobalsightPatchInstaller, DnDConstants.ACTION_COPY,
				new PatchDropTarget());

		frmGlobalsightPatchInstaller
				.setFont(new Font("Calibri", Font.PLAIN, 12));
		frmGlobalsightPatchInstaller
				.setTitle("GlobalSight Patch Installer - Version: 8.6.4");
		frmGlobalsightPatchInstaller
				.setIconImage(Toolkit.getDefaultToolkit().getImage(
						MainUI.class.getResource("/gif/GlobalSight_Icon.jpg")));
		frmGlobalsightPatchInstaller.setBounds(100, 100, 800, 600);
		frmGlobalsightPatchInstaller
				.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.1);
		frmGlobalsightPatchInstaller.getContentPane().add(splitPane,
				BorderLayout.CENTER);
		PatchTreeModel p = new PatchTreeModel(null);
		tree = new JTree(PatchTreeModel.getRootTreeNode());
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					Component c = e.getComponent();
					if (c.equals(tree)) {
						// TreePath path = tree.getPathForLocation(e.getX(),
						// e.getY());
						TreePath[] patchs = tree.getSelectionPaths();
						List<Hotfix> hs = new ArrayList<Hotfix>();
						for (TreePath path : patchs) {
							Object ob = path.getLastPathComponent();
							if (ob instanceof HotfixTreeNode) {
								HotfixTreeNode t = (HotfixTreeNode) ob;

								Object o = t.getUserObject();
								if (o instanceof Hotfix) {
									Hotfix h = (Hotfix) o;
									if (!h.getInstalled()) {
										hs.add(h);
									}
								}
							}
						}

						if (hs.size() > 0) {
							JPopupMenu popupMenu1 = new JPopupMenu();
							JMenuItem del = new JMenuItem("Delete");
							del.addActionListener(new RemoveActionListener(hs));

							popupMenu1.add(del);
							popupMenu1.show(tree, e.getX(), e.getY());
						}
					}
				}
			}
		});
		TreeNode node = (TreeNode) tree.getModel().getRoot();
		expandAll(tree, new TreePath(node));
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath tp = e.getNewLeadSelectionPath();
				if (tp == null) {
					PatchTreeModel.setSelectedHotfix(null);
					return;
				}

				Object ob = tp.getLastPathComponent();
				if (ob instanceof HotfixTreeNode) {
					HotfixTreeNode t = (HotfixTreeNode) ob;
					Object o = t.getUserObject();
					if (o instanceof Hotfix) {
						Hotfix h = (Hotfix) o;
						PatchTreeModel.setSelectedHotfix(h);
					} else {
						PatchTreeModel.setSelectedHotfix(null);
					}
				} else {
					PatchTreeModel.setSelectedHotfix(null);
				}
			}
		});

		tree.setBorder(new EmptyBorder(10, 10, 10, 10));
		splitPane.setLeftComponent(tree);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(0, 10, 0, 10));
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		panel.setAlignmentX(50);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(10, 0, 10, 0));
		panel.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new GridLayout(0, 2, 5, 5));

		JLabel lblNewLabel_1 = new JLabel("Name:");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
		panel_2.add(lblNewLabel_1);
		panel_2.add(vName);

		JLabel lblNewLabel_5 = new JLabel("Sequence:");
		lblNewLabel_5.setHorizontalAlignment(SwingConstants.LEFT);
		panel_2.add(lblNewLabel_5);
		panel_2.add(vSequence);

		JLabel lblNewLabel_7 = new JLabel("Installed:");
		lblNewLabel_7.setHorizontalAlignment(SwingConstants.LEFT);
		panel_2.add(lblNewLabel_7);
		panel_2.add(vInstalled);

		JLabel lblNewLabel_9 = new JLabel("Date:");
		lblNewLabel_9.setHorizontalAlignment(SwingConstants.LEFT);
		panel_2.add(lblNewLabel_9);

		panel_2.add(vDate);

		JLabel lblNewLabel_8 = new JLabel("Dependency:");
		lblNewLabel_8.setHorizontalAlignment(SwingConstants.LEFT);
		panel_2.add(lblNewLabel_8);
		panel_2.add(vDependency);

		JLabel label = new JLabel("");
		panel_2.add(label);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new EmptyBorder(10, 0, 10, 0));
		panel.add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));

		JPanel panel_4 = new JPanel();
		panel_3.add(panel_4, BorderLayout.EAST);
		panel_4.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		panel_4.add(bInstall);
		panel_4.add(bUninstall);

		panel_3.add(message, BorderLayout.NORTH);

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new EmptyBorder(7, 0, 7, 0));
		panel_3.add(panel_5, BorderLayout.CENTER);
		panel_5.setLayout(new BorderLayout(0, 0));
		progressBar.setStringPainted(true);

		progressBar.setMaximum(1000000);
		panel_5.add(progressBar);
		progressBar.setSize(200, 25);

		JPanel panel_6 = new JPanel();
		panel.add(panel_6, BorderLayout.CENTER);
		panel_6.setLayout(new BorderLayout(0, 0));
		// textArea.setLineWrap(true);
		// textArea.setWrapStyleWord(true);
		// panel_6.add(textArea);
		// textArea.setEditable(false);

		panel.add(textPane, BorderLayout.CENTER);

		bUninstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uninstall();
			}
		});

		bInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				install();
			}
		});

		JPanel panel_1 = new JPanel();
		frmGlobalsightPatchInstaller.getContentPane().add(panel_1,
				BorderLayout.NORTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel lblNewLabel = new JLabel("GlobalSight Server:");
		lblNewLabel.setFont(new Font("Calibri", Font.PLAIN, 12));
		panel_1.add(lblNewLabel);

		lb_server.setText(ServerUtil.getPath());
		panel_1.add(lb_server);

		JMenuBar menuBar = new JMenuBar();
		menuBar.setMargin(new Insets(5, 5, 5, 5));
		frmGlobalsightPatchInstaller.setJMenuBar(menuBar);

		// JMenu mnNewMenu = new JMenu(Resource.get("menu_file"));
		JMenu mnNewMenu = new JMenu("  File  ");
		mnNewMenu.setSize(200, 30);
		menuBar.add(mnNewMenu);

		JMenuItem mntmNewMenuItem = new JMenuItem("");
		mntmNewMenuItem.setAction(action);
		mntmNewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				InputEvent.ALT_MASK));
		mnNewMenu.add(mntmNewMenuItem);

		JMenuItem mntmChangeGlobalsightServer = new JMenuItem("");
		mntmChangeGlobalsightServer.setAction(action_1);
		mntmChangeGlobalsightServer.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S, InputEvent.ALT_MASK));
		mnNewMenu.add(mntmChangeGlobalsightServer);

		mnNewMenu.add(new JSeparator());
		JMenuItem mntmExit = new JMenuItem("");
		mntmExit.setAction(action_2);
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				InputEvent.ALT_MASK));
		mnNewMenu.add(mntmExit);

		JMenu mnHelp = new JMenu("  Help  ");
		menuBar.add(mnHelp);

		JMenuItem mntmAboutGlobalsightPatcher = new JMenuItem(
				"About GlobalSight Patch Installer");
		mntmAboutGlobalsightPatcher.setAction(action_3);
		HelpUtil util = new HelpUtil();

		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Help Contents");
		mntmNewMenuItem_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
				InputEvent.ALT_MASK));
		util.addHelp(mntmNewMenuItem_1);

		mntmNewMenuItem_1.setIcon(new ImageIcon(MainUI.class
				.getResource("/gif/help.gif")));
		mnHelp.add(mntmNewMenuItem_1);
		mnHelp.add(mntmAboutGlobalsightPatcher);

		updateUI();
	}

	@Override
	public void run() {
		this.progressBar.setValue(0);
		this.message.setText(" ");
		updateUI();
	}

	private void updateUI() {
		Hotfix h = PatchTreeModel.getSelectedHotfix();
		if (h != null) {
			vName.setText(h.getName());
			vSequence.setText(h.getSequence());
			textPane.setText(h.getDescription());
			vDependency.setText(h.getDependHotfixAsString());

			if (h.getInstalled()) {
				vInstalled.setForeground(vName.getForeground());
				vInstalled.setText("Yes");
			} else {
				vInstalled.setForeground(Color.red);
				vInstalled.setText("No");
			}

			vDate.setText(h.getDate().toString());
			bInstall.setEnabled(!h.getInstalled());
			bUninstall.setEnabled(h.getInstalled());
		} else {
			vName.setText("");
			vSequence.setText("");
			textPane.setText("");
			vDependency.setText("");
			vInstalled.setForeground(vName.getForeground());
			vInstalled.setText("");
			vDate.setText("");
			bInstall.setEnabled(false);
			bUninstall.setEnabled(false);
		}
	}

	public void install() {
		Hotfix h = PatchTreeModel.getSelectedHotfix();
		if (h != null) {
			if (!h.checkVersion()) {
				JOptionPane.showMessageDialog(
						null,
						MessageFormat.format(
								Resource.get("msg.version.wrong.install"),
								h.getVersion(), ServerUtil.getVersion()));
				return;
			}

			String hs = h.checkDepend();

			if (hs.length() > 0) {
				JOptionPane.showMessageDialog(null,
						Resource.get("msg.install.first") + hs);

				return;
			}

			if (h.hasSqlFiles()
					&& JOptionPane.showConfirmDialog(null,
							Resource.get("confirm.install.sql")) != JOptionPane.OK_OPTION) {
				return;
			}

			bInstall.setEnabled(false);
			bUninstall.setEnabled(false);

			h.addProcessListener(this);
			Thread th = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Hotfix h = PatchTreeModel.getSelectedHotfix();
						h.install();
						if (h.getInstalled()) {
							PatchTreeModel.getNewPatches().remove(h);
							PatchTreeModel.getInstalledPatches().add(h);

							PatchTreeModel.updateTreeNode();
							tree.updateUI();
						}

					} catch (Exception e) {
						updateUI();
						log.error(e);
					}
				}
			});
			th.start();
		}
	}

	public void uninstall() {
		Hotfix h = PatchTreeModel.getSelectedHotfix();
		if (h != null) {

			if (h.hasSqlFiles()) {
				JOptionPane.showMessageDialog(null,
						Resource.get("msg.uninstall.sql"));
				return;
			}

			if (!h.checkVersion()) {
				JOptionPane.showMessageDialog(
						null,
						MessageFormat.format(
								Resource.get("msg.version.wrong.uninstall"),
								h.getVersion(), ServerUtil.getVersion()));
				return;
			}

			String hs = h.checkRedepend(PatchTreeModel.getAllHotfix());
			if (hs.length() > 0) {
				JOptionPane.showMessageDialog(null,
						Resource.get("msg.uninstall.first") + hs);
				return;
			}

			bInstall.setEnabled(false);
			bUninstall.setEnabled(false);

			h.addProcessListener(this);
			Thread th = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Hotfix h = PatchTreeModel.getSelectedHotfix();
						h.rollback();
						if (!h.getInstalled()) {
							PatchTreeModel.getNewPatches().add(h);
							PatchTreeModel.getInstalledPatches().remove(h);
							PatchTreeModel.updateTreeNode();
							tree.updateUI();
						}
					} catch (Exception e) {
						updateUI();
						log.error(e);
					}
				}
			});
			th.start();
		}
	}

	private void expandAll(JTree tree, TreePath parent) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();

		if (node.getChildCount() > 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path);
			}
		}

		tree.expandPath(parent);
	}

	/**
	 * Lets the dialog showed in the center of screen.
	 */
	private void setLocation() {
		frmGlobalsightPatchInstaller.setLocationRelativeTo(null);
		frmGlobalsightPatchInstaller.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
	}

	private void setHelpLocation() {
		// Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// Point location = new Point((screen.width - 600) / 2,
		// (screen.height - 600) / 2);
		//
		// try {
		// URL url = MainUI.class.getResource("/help/Master.hs");
		// File f = new File(url.getFile());
		// String content = FileUtil.readFile(f,"ISO-8859-1");
		// content = content.replace("<location x=\"200\" y=\"200\" />",
		// "<location x=\"" + location.x + "\" y=\"" + (location.y - 20) +
		// "\" />");
		// FileUtil.writeFile(f, content, "ISO-8859-1");
		// } catch (IOException e) {
		// log.error(e);
		// }
	}

	/**
	 * Let user confirm again while closing the dialog.
	 */
	private void onClose() {
		if (JOptionPane.showConfirmDialog(null, Resource.get("confirm.exit"),
				Resource.get("title.exit"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

			System.exit(0);
		} else {
			frmGlobalsightPatchInstaller
					.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}
	}

	private class SwingAction_2 extends AbstractAction {
		private static final long serialVersionUID = -7024297004087008637L;

		public SwingAction_2() {
			putValue(NAME, "Exit");
			putValue(SHORT_DESCRIPTION, "Exit GlobalSight Patch Installer");
		}

		public void actionPerformed(ActionEvent e) {
			onClose();
		}
	}

	private class SwingAction extends AbstractAction {

		private static final long serialVersionUID = 6081148923103197129L;

		public SwingAction() {
			putValue(
					SMALL_ICON,
					new ImageIcon(MainUI.class.getResource("/gif/addPatch.gif")));
			putValue(NAME, "Add Patch...");
			putValue(SHORT_DESCRIPTION, "Add a new patch");
		}

		public void actionPerformed(ActionEvent e) {
			if (ServerUtil.getPath() == null)
			{
				JOptionPane.showMessageDialog(null,
						Resource.get("msg.no.server"));
				return;
			}
			AddFileAction add = new AddFileAction();
			add.run();
		}
	}

	private class SwingAction_1 extends AbstractAction {
		private static final long serialVersionUID = -4556061548210459055L;

		public SwingAction_1() {
			putValue(NAME, "Set GlobalSight Server");
			putValue(SHORT_DESCRIPTION, "Set GlobalSight server path");
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogTitle("Set GlobalSight Server");
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			String path = ServerUtil.getPath();
			if (path != null) {
				jfc.setSelectedFile(new File(path));
			}

			int result = jfc.showOpenDialog(frmGlobalsightPatchInstaller);
			File file = null;
			if (JFileChooser.APPROVE_OPTION == result) {
				file = jfc.getSelectedFile();

				if (ServerUtil.isServerPath(file.getAbsolutePath())) {
					ServerUtil.setPath(file.getAbsolutePath());
					lb_server.setText(ServerUtil.getPath());
					tree.updateUI();
				} else {
					JOptionPane.showMessageDialog(null,
							Resource.get("msg.invalid.server"));
				}
			}
		}
	}

	@Override
	public void processChanged(int n, String message) {
		this.progressBar.setValue(n);
		this.message.setText(message);

		if (n == progressBar.getMaximum()) {
			updateUI();
		}
	}

	private class SwingAction_3 extends AbstractAction {
		public SwingAction_3() {
			putValue(NAME, "About GlobalSight Patch Installer");
			putValue(SHORT_DESCRIPTION, "About GlobalSight Patch Installer");
		}

		public void actionPerformed(ActionEvent e) {
			AboutDialog d = new AboutDialog();
			d.setVisible(true);
		}
	}
}
