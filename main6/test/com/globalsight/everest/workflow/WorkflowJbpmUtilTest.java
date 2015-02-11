package com.globalsight.everest.workflow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.node.Decision;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.junit.Before;
import org.junit.Test;

public class WorkflowJbpmUtilTest
{
    WorkflowJbpmUtil m_instance = new WorkflowJbpmUtil();
    String m_userId = "JUniter";
    boolean m_isDebug = false;
    
    String translationName = "Translation1_1000";
    String reviewName = "review_dtp1_1000";
    
    // Translation and Review ProcessInstance Map
    Map<String, MockProcessInstance> piMapTrans;
    Map<String, MockProcessInstance> piMapReview;
    
    @Before
    public void before()
    {
//        m_isDebug = true;
        
        String type;
        MockProcessInstance pi = new MockProcessInstance();
        Calendar cal;
        
        // Set value for piMapTrans
        piMapTrans = new HashMap<String, MockProcessInstance>();
        type = WorkflowConstants.TASK_TYPE_NEW;
        TaskInstance translation = new TaskInstance(translationName);
        translation.setDescription("Translation1");
        translation.setActorId(m_userId);
        cal = Calendar.getInstance();
        translation.setCreate(cal.getTime());
        pi.saveOrUpdateTaskInstance(translation, type);
        piMapTrans.put(type, pi.clone());
        
        type = WorkflowConstants.TASK_TYPE_ACC;
        translation.start();
        pi.saveOrUpdateTaskInstance(translation, type);
        piMapTrans.put(type, pi.clone());
        
        type = WorkflowConstants.TASK_TYPE_COM;
        translation.end();
        pi.saveOrUpdateTaskInstance(translation, type);
        piMapTrans.put(type, pi.clone());
        
        // Set value for piMapReview
        piMapReview = new HashMap<String, MockProcessInstance>();
        type = WorkflowConstants.TASK_TYPE_NEW;
        TaskInstance review = new TaskInstance(reviewName);
        review.setDescription("Review1");
        review.setActorId(m_userId);
        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        review.setCreate(cal.getTime());
        pi.saveOrUpdateTaskInstance(review, WorkflowConstants.TASK_TYPE_NEW);
        piMapReview.put(type, pi.clone());
        
        type = WorkflowConstants.TASK_TYPE_ACC;
        review.start();
        pi.saveOrUpdateTaskInstance(review, type);
        piMapReview.put(type, pi.clone());
        
        type = WorkflowConstants.TASK_TYPE_COM;
        review.end();
        pi.saveOrUpdateTaskInstance(review, type);
        
        TaskInstance translation2 = new TaskInstance("T2");
        translation2.setDescription("Translation2");
        translation2.setActorId(m_userId);
        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 2);
        translation2.setCreate(cal.getTime());
        pi.saveOrUpdateTaskInstance(translation2, type);
        piMapReview.put(type, pi.clone());
    }
    
    @SuppressWarnings("static-access")
    @Test
    public void testGetActivityNameWithArrowName() throws InterruptedException
    {
        printMsg("\nStart test method for [getActivityNameWithArrowName]-----");
        
        String activityName;
        String expected, msg;
        MockProcessInstance pi;
        String type;
        String suffix = "_1000";        
        Node start = new Node("Start");
        TaskNode node = new TaskNode("node_1_Translation1_1000");
        Transition trans1 = new Transition("Action1");
        trans1.setFrom(start);
        node.addArrivingTransition(trans1); 
        type = WorkflowConstants.TASK_TYPE_NEW;
        pi = piMapTrans.get(type);        
        activityName = m_instance.getActivityNameWithArrowName(node, suffix, pi, type);
        expected = "Translation1(Action1)";
        msg = "\nExpected:\t"+expected+"\nActural:\t" + activityName;
        printMsg(msg);
        Assert.assertTrue(msg, expected.equals(activityName));
        
        Decision dec = new Decision();
        Transition trans2 = new Transition("Action2");
        TaskNode review = new TaskNode("node_2_review_dtp1_1000");
        Transition trans3 = new Transition("Action3");
        trans3.setFrom(review);
        dec.addArrivingTransition(trans3);
        trans2.setFrom(dec);
        node.addArrivingTransition(trans2);
        pi = piMapReview.get(WorkflowConstants.TASK_TYPE_COM);        
        activityName = m_instance.getActivityNameWithArrowName(node, suffix, pi, type);
        expected = "Translation1(Action2)";
        msg = "Expected:\t"+expected+"\nActural:\t" + activityName;
        printMsg(msg);
        Assert.assertTrue(msg, expected.equals(activityName));
    }
    
    @SuppressWarnings("static-access")
    @Test
    public void testGetLastTaskInstance()
    {
        printMsg("\nStart test method for [getLastTaskInstance]---------");
        
        MockProcessInstance pi = new MockProcessInstance();
        String type;
        TaskInstance ti;
        
        type = WorkflowConstants.TASK_TYPE_NEW;
        pi = piMapTrans.get(type);
        ti = m_instance.getLastTaskInstance(pi, type);
        printMsg(ti);
        Assert.assertSame("Start", ti.getName());
        
        type = WorkflowConstants.TASK_TYPE_ACC;
        pi = piMapTrans.get(type);
        ti = m_instance.getLastTaskInstance(pi, type);
        printMsg(ti);
        Assert.assertSame("Start", ti.getName());
        
        type = WorkflowConstants.TASK_TYPE_COM;
        pi = piMapTrans.get(type);
        ti = m_instance.getLastTaskInstance(pi, type);
        printMsg(ti);
        Assert.assertSame("Start", ti.getName());
        
        type = WorkflowConstants.TASK_TYPE_NEW;
        pi = piMapReview.get(type);
        printTaskInstance(pi);
        ti = m_instance.getLastTaskInstance(pi, type);
        printMsg(ti);
        Assert.assertSame(translationName, ti.getName());
        
        type = WorkflowConstants.TASK_TYPE_ACC;
        pi = piMapReview.get(type);
        ti = m_instance.getLastTaskInstance(pi, type);
        printMsg(ti);
        Assert.assertSame(translationName, ti.getName());
        
        type = WorkflowConstants.TASK_TYPE_COM;
        pi = piMapReview.get(type);
        ti = m_instance.getLastTaskInstance(pi, type);
        printMsg(ti);
        Assert.assertSame(translationName, ti.getName());
        
        type = WorkflowConstants.TASK_TYPE_NEW;
        pi = piMapReview.get(WorkflowConstants.TASK_TYPE_COM);
        ti = m_instance.getLastTaskInstance(pi, type);
        printMsg(ti);
        Assert.assertSame(reviewName, ti.getName());
    }
    
    @SuppressWarnings("unchecked")
    public void printTaskInstance(MockProcessInstance p_pi)
    {
        if (!m_isDebug)
        {
            return;
        }
        
        Collection<TaskInstance> tiSet = p_pi.getTaskMgmtInstance().getTaskInstances();
        List<TaskInstance> tiList = new ArrayList<TaskInstance>(tiSet);
        Collections.sort(tiList, new java.util.Comparator<TaskInstance>()
        {
            /**
             * Sort the object into descending order.
             */
            @Override
            public int compare(TaskInstance ti1, TaskInstance ti2)
            {
                Date tiDate = ti1.getCreate();
                if (tiDate == null || tiDate.before(ti2.getCreate()))
                {
                    return 1;
                }
                else if (tiDate.after(ti2.getCreate()))
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        });
        
        for(TaskInstance ti : tiList)
        {
            printMsg(ti);
        }
        System.out.println("Finish method printTaskInstance");
    }
    
    public void printMsg(Object obj)
    {
        if (!m_isDebug)
        {
            return;
        }
        
        if (obj == null)
        {
            System.out.println("The Input Object is NULL.");
        }
        else if (obj instanceof TaskInstance)
        {
            TaskInstance ti = (TaskInstance) obj;
            System.out.println(ti.getName() + " " + ti.getDescription() + "\t"
                    + "[Create:" + ti.getCreate() + "]\t" 
                    + "[Start:" + ti.getStart() + "]\t" 
                    + "[End:" + ti.getEnd() + "]\t");
        }
        else 
        {
            System.out.println(obj.toString());
        }
    }
}
