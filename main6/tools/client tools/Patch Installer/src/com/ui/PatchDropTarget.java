package com.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;

import javax.swing.JOptionPane;

import com.action.AddFileAction;
import com.util.Resource;
import com.util.ServerUtil;

public class PatchDropTarget extends DropTargetAdapter {

	@Override
	public void drop(DropTargetDropEvent dtde) {
		if (ServerUtil.getPath() == null)
		{
			JOptionPane.showMessageDialog(null,
					Resource.get("msg.no.server"));
			return;
		}
		
		AddFileAction action = new AddFileAction();
		
		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		Transferable transferable = dtde.getTransferable();
		DataFlavor[] flavors = transferable.getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			DataFlavor d = flavors[i];
			try {
				if (d.equals(DataFlavor.javaFileListFlavor)) {
					java.util.List fileList = (java.util.List) transferable
							.getTransferData(d);
					for (Object f : fileList) {
						action.addPatch(((File) f).getAbsolutePath());
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			dtde.dropComplete(true);
		}

	}

}
