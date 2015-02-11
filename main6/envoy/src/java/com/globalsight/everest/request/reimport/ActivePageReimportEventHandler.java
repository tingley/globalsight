/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.request.reimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.request.DelayedImportRequestDescriptorModifier;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.scheduling.EventHandler;
import com.globalsight.scheduling.EventHandlerException;
import com.globalsight.scheduling.EventInfo;
import com.globalsight.scheduling.EventSchedulerHelper;
import com.globalsight.scheduling.FluxEventMap;
import com.globalsight.scheduling.KeyFlowContext;


/**
 * Executes the actual active page reimport when triggered by Quartz
 */
public class ActivePageReimportEventHandler extends EventHandler
{
    private static Logger s_logger = Logger
            .getLogger(ActivePageReimportEventHandler.class.getName());

    // This is a hack to get around a flux 6.1 bug with one-time timers
    // Apparently one-time (repeat count==1) timers do not work and end
    // up repeating anyway a couple of times. So this map keeps track of
    // jobs that have been export-sourced and ignores them. The obvious
    // corner case is that this list is lost on a restart, so it's possible
    // to have the repeat appear after a restart....but that should just
    // result in some harmless log messages
    // No need now
    private static ArrayList<Long> s_handledEvents = new ArrayList<Long>();

    /**
     * Constructs an ActivePageReimportEventHandler
     */
    public ActivePageReimportEventHandler()
    {
        super();
    }

    /**
     * Handles executing a timed dispatch when invoked by Quartz.
     * 
     * @param p_flowContext
     * @exception EventHandlerException
     */
    public void eventFired(KeyFlowContext p_flowContext)
            throws EventHandlerException
    {
        try
        {
            EventInfo myEventInfo = (EventInfo) p_flowContext.getKey();
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Got eventinfo " + myEventInfo);                
            }
            HashMap map = myEventInfo.getMap();
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Got hashmap " + map);                
            }
            Long id = (Long) map.get("delayedImportRequestId");
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Got delayed import req " + id);                
            }
            if (s_handledEvents.contains(id))
            {
                s_logger.info("Ignoring repeated reimport event for id " + id);
                return;
            }
            s_handledEvents.add(id);
            unschedule(map);

            DelayedImportRequest dir = null;
            String sql = DelayedImportRequestDescriptorModifier.DELAYED_IMPORT_REQ_BY_REQ_ID_SQL;
            Map params = new HashMap();
            params.put("delayedImportRequestId", id);
            List requests = HibernateUtil.searchWithSql(sql, params,
                    DelayedImportRequest.class);
            if (requests != null && requests.size() > 0)
            {
                dir = (DelayedImportRequest) requests.get(0);
            }

            s_logger.debug("Got delayed import req from TL " + dir.getId());

            s_logger.info("Executing reimport for request " + id);
            execute(dir);
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Executed task at " + new java.util.Date());                
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to execute delayed reimport.", e);
        }
    }

    /**
     * Starts the reimport
     */
    public void execute(DelayedImportRequest p_delayedRequest)
    {
        try
        {
            s_logger.info("Executing delayed import task.");
            ActivePageReimporter.getInstance().startImport(p_delayedRequest);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to delay reimport:", e);
        }
    }

    /**
     * Unschedules event without throwing any exceptions
     * 
     * @param p_map
     *            map of event info
     */
    private void unschedule(HashMap p_map)
    {
        try
        {
            // now unschedule the event
            FluxEventMap fem = EventSchedulerHelper.findFluxEventMap(p_map);
            ServerProxy.getEventScheduler().unschedule(fem);
            s_logger.debug("Unscheduled event.");
        }
        catch (Exception e)
        {
            s_logger.error("Error unscheduling event.", e);
        }
    }

}
