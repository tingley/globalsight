package com.globalsight.ui.attribute;

/**
 * FileTransferHandler.java is used by the 1.4
 * 
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class AttFileTransfer extends TransferHandler
{
    private static final long serialVersionUID = -9010049960773064114L;
    private DataFlavor fileFlavor = DataFlavor.javaFileListFlavor;
    private JList jList;

    public AttFileTransfer(JList jList)
    {
        super();
        this.jList = jList;
    }

    public boolean canImport(JComponent p_jc, DataFlavor[] p_flavors)
    {
        if (hasFileFlavor(p_flavors))
        {
            return true;
        }
        return false;
    }

    public boolean importData(JComponent p_jc, Transferable p_t)
    {
        if (!canImport(p_jc, p_t.getTransferDataFlavors()))
        {
            return false;
        }

        try
        {
            if (hasFileFlavor(p_t.getTransferDataFlavors()))
            {
                List<File> files = (List<File>) p_t.getTransferData(fileFlavor);
                DefaultListModel model = (DefaultListModel) jList.getModel();
                for (File f : files)
                {
                    model.addElement(f.getAbsolutePath());
                }
            }
        }
        catch (UnsupportedFlavorException ufe)
        {
            System.out.println("importData: unsupported data flavor");
        }
        catch (IOException ieo)
        {
            System.out.println("importData: I/O exception");
        }
        return false;
    }

    private boolean hasFileFlavor(DataFlavor[] p_flavors)
    {
        for (int i = 0; i < p_flavors.length; i++)
        {
            if (fileFlavor.equals(p_flavors[i]))
            {
                return true;
            }
        }
        return false;

    }
}
