//                              -*- Mode: Java -*-
//
// Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//

package debex.helpers.threadpool;

import debex.Setup;

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
