package com.globalsight.everest.webapp.applet.createjob;

import java.io.File;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.JLabel;

import netscape.javascript.JSObject;

import com.globalsight.everest.webapp.applet.common.EnvoyJApplet;

public class UploadAttachment extends EnvoyJApplet
{
    private static final long serialVersionUID = 1L;
    private JLabel label;
    private JSObject win;
    private String baseFolder = "";
    
    static 
    { 
        Locale.setDefault(Locale.ENGLISH); 
    }
    
    public void init()
    {
        win = JSObject.getWindow(this);
        label = new JLabel();
        add(label);
    }
    
    public void openChooser(final String userName, final String password,
            final String companyIdWorkingFor)
    {
        try
        {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

                @Override
                public Object run()
                {
                    MyJFileChooser chooser = new MyJFileChooser(baseFolder);
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setApproveButtonText("Select");
                    chooser.setApproveButtonToolTipText("Select Attachment");
                    int state = chooser.showOpenDialog(label);
                    if (state == JFileChooser.APPROVE_OPTION)
                    {
                        File file = chooser.getSelectedFile();
                        baseFolder = file.getParent();
                        if (file.canRead() && !file.isHidden())
                        {
                            performUpload(file, userName, password, companyIdWorkingFor);
                        }
                    }
                    
                    return null;
                }
                
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void performUpload(File file, String userName, String password,
            String companyIdWorkingFor)
    {
        if (file.length() > 35 * 1024 * 1024)
        {
            win.eval("alert(\"The attachment should be less than 35M.\")");
            return;
        }
        CreateJobUtil.runJavaScript(win, "addAttachment", new Object[] { file.getName() });

        URL url = this.getCodeBase();
        String hostName = url.getHost();
        int port = url.getPort();
        String protocol = url.getProtocol();
        boolean enableHttps = protocol.contains("s") ? true : false;

        String tmpFolder = getParameter("folderName");
        String savingPath = "GlobalSight" + File.separator + "CommentReference"
                + File.separator + "tmp" + File.separator + tmpFolder
                + File.separator + file.getName();

        UploadAttachmentThread uat = new UploadAttachmentThread(hostName,
                String.valueOf(port), userName, password, enableHttps,
                companyIdWorkingFor, file, savingPath, win);
        uat.start();
    }
    
    public void destroy() 
    {
        super.destroy();
    }
}
