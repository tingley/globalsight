package com.globalsight.everest.edit.offline;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.taskmanager.Task;

public class OfflineEditManagerLocalTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    /**
     * Used to start a upload thread.
     * 
     * @author malcolm
     *
     */
    private class UploadThread implements Runnable
    {

        private int i;
        private List<OEMProcessStatus> status;

        public UploadThread(int i, List<OEMProcessStatus> status)
        {
            this.i = i;
            this.status = status;
        }

        @Override
        public void run()
        {
            OfflineEditManagerLocalForTest local = new OfflineEditManagerLocalForTest();
            local.attachListener(status.get(i));
            try
            {
                local.processUploadPage(new File("a.html"), null, null, "a" + i);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Used to simulate multithreaded upload.
     * 
     * @param n
     *            how many thread will be created.
     * @param status
     *            the according status
     * @throws InterruptedException
     */
    private void startThread(int n, List<OEMProcessStatus> status)
            throws InterruptedException
    {
        for (int i = 0; i < n; i++)
        {
            Thread t = new Thread(new UploadThread(i, status));

            t.start();
            Thread.sleep(100);
        }
    }

    private List<OEMProcessStatus> getStatus(int n)
    {
        List<OEMProcessStatus> status = new ArrayList<OEMProcessStatus>();
        for (int i = 0; i < n; i++)
        {
            OEMProcessStatus statu = new OEMProcessStatus();
            status.add(statu);
        }
        return status;
    }

    @Test
    /**
     * Checks there are no waiting forms.
     * 
     * @throws InterruptedException
     */
    public void testThreadRun() throws InterruptedException
    {
        startThread(50, getStatus(50));

        Thread.sleep(15000);
        OfflineEditManagerLocalForTest local = new OfflineEditManagerLocalForTest();
        if (local.WAITING_FORMS.size() > 0)
        {
            fail("There are still some waiting files");
        }

        if (local.RUNNING_FORMS.size() > 0)
        {
            fail("There are still some running files");
        }
    }

    @Test
    /**
     * Checks all files are processed.
     * 
     * @throws InterruptedException
     */
    public void testFilesHandled() throws InterruptedException
    {
        startThread(50, getStatus(50));
        Thread.sleep(15000);

        OfflineEditManagerLocalForTest local = new OfflineEditManagerLocalForTest();
        Vector<String> fs = local.getFileNames();

        for (int i = 0; i < 50; i++)
        {
            if (!fs.contains("a" + i))
            {
                fail("There are some status are not processed");
            }
        }
    }

    @Test
    /**
     * Checks all status are updated.
     * 
     * @throws InterruptedException
     */
    public void testStatus() throws InterruptedException
    {
        List<OEMProcessStatus> status = getStatus(50);
        startThread(50, status);
        Thread.sleep(15000);

        for (OEMProcessStatus s : status)
        {
            if (s.getPercentage() != 100)
            {
                fail("There are some status are not 100");

            }
        }
    }

    class OfflineEditManagerLocalForTest extends OfflineEditManagerLocal
    {
        private Vector<String> fileNames = new Vector<String>();
        
        public Vector<String> getFileNames()
        {
            return fileNames;
        }

        public void setFileNames(Vector<String> fileNames)
        {
            this.fileNames = fileNames;
        }

        @Override
        public String runProcessUploadPage(File p_tmpFile, User p_user,
                Task p_task, String p_fileName) throws AmbassadorDwUpException
        {
            try
            {
                Thread.sleep(1000);
                fileNames.add(p_fileName);
                this.getStatus().setPercentage(100);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            return "";
        }
    }
}
