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

package galign.helpers.threadpool;

/**
 * This class provides access to worker threads. It currently provides
 * only 2 workerthreads but can be reimplemented to be a real pool of
 * many threads.
 */
public class LiteThreadPool
{
    private PoolThread thread1;
    private PoolThread thread2;

    public LiteThreadPool()
    {
        thread1 = new PoolThread();
        thread1.setDaemon(true);
        thread1.start();

        thread2 = new PoolThread();
        thread2.setDaemon(true);
        thread2.start();
    }

    public PoolThread getThread1()
    {
        return thread1;
    }

    public PoolThread getThread2()
    {
        return thread2;
    }
}
