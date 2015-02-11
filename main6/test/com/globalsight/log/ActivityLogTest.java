package com.globalsight.log;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;

public class ActivityLogTest {
    @Test
    public void testActivityLog() {
        // There isn't much to test here without inspecting the logs, but at
        // least we can check that no exceptions are thrown.
        ActivityLog.Start start;
        Map<Object,Object> activityArgs;

        start = ActivityLog.start(ActivityLogTest.class, "test1");
        start.end();

        activityArgs = new HashMap<Object,Object>();
        activityArgs.put("key", "val");
        start = ActivityLog.start(ActivityLogTest.class, "test1", activityArgs);
        start.end();

        activityArgs = new HashMap<Object,Object>();
        activityArgs.put("key", null);
        start = ActivityLog.start(ActivityLogTest.class, "test1", activityArgs);
        start.end();

        // get perverse to cause internal exceptions
        start = ActivityLog.start(ActivityLogTest.class, "test1", null);
        start.end();

        activityArgs = new HashMap<Object,Object>();
        activityArgs.put(null, "val");
        start = ActivityLog.start(ActivityLogTest.class, "test1", activityArgs);
        start.end();
    }
}
