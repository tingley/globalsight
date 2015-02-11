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

import galign.Setup;

/**
 * This class implements a simple worker thread that keeps sticking
 * around as daemon thread waiting for Runnables to execute.
 */
public class PoolThread
    extends Thread
{
    private Object lock = new Object();
    private Runnable target = null;

    public void run()
    {
        Runnable currTarget;

        while (true)
        {
            synchronized (lock)
            {
                while (target == null)
                {
                    try
                    {
                        lock.wait();
                    }
                    catch (InterruptedException e)
                    {
                        if (Setup.DEBUG)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                currTarget = target;
            }

            try
            {
                target.run();
            }
            catch (Throwable t)
            {
                if (Setup.DEBUG)
                {
                    t.printStackTrace();
                }
            }

            synchronized (lock)
            {
                if (currTarget == target)
                {
                    target = null;
                }
            }
        }
    }

    public void execute(Runnable r)
    {
        synchronized (lock)
        {
            target = r;
            lock.notifyAll();
        }
    }
}
