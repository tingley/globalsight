/**
 * 
 */
package com.globalsight.everest.dqf;

import jodd.props.Props;
import jodd.util.ClassLoaderUtil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskManagerLocal;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;

import static org.junit.Assert.assertEquals;

/**
 * @author Administrator
 *
 */
public class DQFInTaskTest
{
    private static Long taskId = 1L;
    private static Props props = new Props();
    private static final String PROPERTIES_FILE = "properties/dqf.properties";
    private static TaskManagerLocal taskManager = new TaskManagerLocal();
    private static Task task = null;
    
    @BeforeClass
    public static void before() {
        try
        {
            props.load(ClassLoaderUtil.getResourceAsStream(PROPERTIES_FILE));
            
            taskId = props.getLongValue("taskId");
            if (taskId == null || taskId < 0)
                taskId = 1L;
            
            //Get task with taskId, if specified ID does not exist, then id will be increased one by one.
            while (true)
            {
                try
                {
                    task = taskManager.getTask(taskId);
                    // Find a task which is
                    // 1.Accepted task
                    // 2.Review only or review editable task
                    if (task != null
                            && task.getState() != Task.STATE_ACCEPTED
                            && (task.isType(Task.TYPE_REVIEW) || task
                                    .isType(Task.TYPE_REVIEW_EDITABLE)))
                        break;
                    taskId++;
                }
                catch (Exception e)
                {
                    System.out.println("Can NOT get task with ID [" + taskId + "]");
                }
            }
            System.out.println("Select task ID is " + taskId);
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
    public void testIfTaskCanComplete()
    {
        task.setType(Task.TYPE_TRANSLATE);
        assertEquals(true, task.isType(Task.TYPE_TRANSLATE));

        Workflow wf = task.getWorkflow();

        // Update data in memory to assert
        task.setType(Task.TYPE_REVIEW);
        
        //Scorecard required
        wf.setScorecardShowType(1);
        wf.setScorecardComment(null);
        assertEquals(false, wf.requiredScorecard() && wf.hasScorecardStored());
        wf.setScorecardComment("junit test comment");
        assertEquals(true, wf.requiredScorecard() && wf.hasScorecardStored());

        //DQF + Scorecard required
        wf.setScorecardShowType(3);
        wf.setDQFComment("junit test comment");
        wf.setFluencyScore("fluency");
        wf.setAdequacyScore("adequacy");
        assertEquals(true, wf.requiredDQF() && wf.hasDQFStored());
        
        wf.setFluencyScore(null);
        assertEquals(false, wf.requiredDQF() && wf.hasDQFStored());

        wf.setScorecardComment("junit test comment");
        assertEquals(true, wf.requiredScorecard() && wf.hasScorecardStored());
        
        wf.setScorecardComment(null);
        assertEquals(false, wf.requiredScorecard() && wf.hasScorecardStored());
        
        //DQF required
        wf.setFluencyScore("fluency");
        wf.setAdequacyScore("adequacy");
        wf.setDQFComment(null);
        assertEquals(false, wf.requiredDQF() && wf.hasDQFStored());
        
        wf.setDQFComment("junit test comment");
        assertEquals(true, wf.requiredDQF() && wf.hasDQFStored());
    }
}
