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
package com.globalsight.machineTranslation.mstranslator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.datacontract.schemas._2004._07.Microsoft_MT_Web_Service_V2.TranslateArrayResponse;

public class MSTranslator
{
    /**
     * Sometimes the translation is hold on. So it need to be run in an thread and will be close f it doesn't respond for 1 minutes.
     * 
     * @param config
     * @param segments
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static TranslateArrayResponse[] translator(MSTranslateConfig config, String[] segments)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        ExecutorService es = Executors.newSingleThreadExecutor();
        MSTask task = new MSTask(config, segments);
        FutureTask<TranslateArrayResponse[]> futureTask = new FutureTask<>(task);
        es.submit(futureTask);
        es.shutdown();

        return futureTask.get(1, TimeUnit.MINUTES);
    }
}
