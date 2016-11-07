/**
 * 
 */
package com.globalsight.everest.dqf;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import jodd.props.Props;
import jodd.props.PropsUtil;
import jodd.util.ClassLoaderUtil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bsh.This;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskException;
import com.globalsight.everest.taskmanager.TaskManagerLocal;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.workflow.WorkflowHelper;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowManagerException;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;
import com.globalsight.util.ClassUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

/**
 * @author Administrator
 *
 */
public class DQFInJobDetailTest
{
    private static Long wfId = 1L;
    private static WorkflowImpl workflow = null;
    private WorkflowManagerLocal handler = new WorkflowManagerLocal();
    private static Props props = new Props();
    private static final String PROPERTIES_FILE = "properties/dqf.properties";
    
    @BeforeClass
    public static void before() {
        try
        {
            props.load(ClassLoaderUtil.getResourceAsStream(PROPERTIES_FILE));
            
            wfId = props.getLongValue("wfId");
            if (wfId == null || wfId < 0)
                wfId = 1L;
            
            //Get workflow with wfId, if specified ID does not exist, then id will be increased one by one.
            while (true) {
                try
                {
                    workflow = (WorkflowImpl) new WorkflowManagerLocal().getWorkflowById(wfId);
                    if (workflow != null)
                        break;
                    wfId++;
                }
                catch (Exception e)
                {
                    System.out.println("Can NOT get workflow with ID [" + wfId + "]");
                }
            }
            System.out.println("Get workflow successfully with ID [" + wfId + "]");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @AfterClass
    public static void after() {
        props = null;
    }
    
    @Test
    public void testEnableDQF() throws RemoteException
    {
        int showType = workflow.getScorecardShowType();
        assertEquals(showType > 1 && showType < 6, workflow.enableDQF());
    }
    
    @Test
    public void testRequiredDQF()
    {
        int showType = workflow.getScorecardShowType();
        assertEquals(showType == 3 || showType == 5, workflow.requiredDQF());
    }

    @Test
    public void testEnableScorecard()
    {
        int showType = workflow.getScorecardShowType();
        assertEquals(showType > -1 && showType < 4, workflow.enableScorecard());
    }

    @Test
    public void testRequiredScorecard()
    {
        int showType = workflow.getScorecardShowType();
        assertEquals(showType == 1 || showType == 3, workflow.requiredScorecard());
    }

    @Test
    public void testHasDQFStored() throws RemoteException
    {
        assertEquals(StringUtil.isNotEmpty(workflow.getDQFComment()), workflow.hasDQFStored());
    }

    @Test
    public void testHasScorecardStored()
    {
        assertEquals(StringUtil.isNotEmpty(workflow.getScorecardComment()),
                workflow.hasScorecardStored());
    }

    @Test
    public void testShowScorecardTab()
    {
        TaskManagerLocal taskManager = new TaskManagerLocal();
        boolean isTranslateTask = false;
        try
        {
            Collection tasks = taskManager.getCurrentTasks(wfId);
            if (tasks != null)
            {
                for (Iterator it = tasks.iterator(); it.hasNext();)
                {
                    Task task = (Task) it.next();
                    isTranslateTask = !(task.isType(Task.TYPE_REVIEW) || task
                            .isType(Task.TYPE_REVIEW_EDITABLE));
                }
                if (isTranslateTask)
                {
                    assertEquals(false,
                            workflow.getScorecardShowType() > -1
                                    && workflow.getScorecardShowType() < 6 && isTranslateTask);
                }
                else
                {
                    assertEquals(true,
                            workflow.getScorecardShowType() > -1
                                    && workflow.getScorecardShowType() < 6 && !isTranslateTask);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
