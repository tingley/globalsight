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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.page.pageexport.ExportingPage;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.page.SourcePage;

public class ExportUtil 
{
	private static HashMap<Long, HashMap<String, HashMap<String, Integer>>> EXPORTING_BATCHS =
	            new HashMap<Long, HashMap<String, HashMap<String, Integer>>>();

    private synchronized static ExportStatus getExportStatus(Long exportBatchId,
            String path, boolean cutNum, String locale)
	{
		ExportStatus status = new ExportStatus();
		path = path.replace("\\", "/");
		HashMap<String, HashMap<String, Integer>> batchs = EXPORTING_BATCHS
                .get(exportBatchId);
		if (batchs == null)
		{
			status.setStart(true);
            batchs = new HashMap<String, HashMap<String, Integer>>();
			EXPORTING_BATCHS.put(exportBatchId, batchs);

            ExportBatchEvent event = HibernateUtil.get(ExportBatchEvent.class,
                    exportBatchId);
			List<ExportingPage> pages = event.getExportingPages();
			
			for (ExportingPage page : pages)
			{
				TargetPage p = (TargetPage) page.getPage();
				SourcePage sp = p.getSourcePage();
				String sPath = p.getSourcePage().getExternalPageId();
				sPath = sPath.replace("\\", "/");
				sPath = sPath.substring(sPath.indexOf("/") + 1);
				
				String tLocale = p.getGlobalSightLocale().toString();
				if (sp.isPassoloPage())
				{
					File f = sp.getFile();
					String rp = f.getAbsolutePath().replace("\\", "/");
					String x = sPath.substring(0, sPath.indexOf("/"));
					sPath = rp.substring(rp.indexOf(x));
					tLocale = "all";
				}
				
				HashMap<String, Integer> locales = batchs.get(sPath);
				 if (locales == null)
				 {
					 locales = new HashMap<String, Integer>();
					 batchs.put(sPath, locales);
				 }
				 
				Integer pageCount = locales.get(tLocale);
				if (pageCount == null)
				{
					pageCount = 0;
				}
				
				pageCount ++;
				locales.put(tLocale, pageCount);
			}
		}
		
		Integer pageCount = batchs.get(path).get(locale);
		
		if (pageCount == 1)
		{
			status.setEnd(true);
		}
		
		if (cutNum)
		{
			batchs.get(path).put(locale, pageCount - 1);
		}
		
		return status;
	}
	
    public static boolean isFirstFileAndAllFileSelected(String exportBatchId,
            String path, int num, String locale)
    {
        path = path.replace("\\", "/");
        boolean isFirstFile = getExportStatus(Long.parseLong(exportBatchId),
                path, false, locale).isStart();
        if (isFirstFile)
        {
        	HashMap<String, Integer> batchs = EXPORTING_BATCHS.get(
                    Long.parseLong(exportBatchId)).get(path);
            int all = batchs.get(locale);
            if (all == num)
            {
                return true;
            }
        }
        return false;
    }
	
    public static boolean isLastFile(String exportBatchId, String path,
            String locale)
    {
        return getExportStatus(Long.parseLong(exportBatchId), path, true,
                locale).isEnd();
    }
}
