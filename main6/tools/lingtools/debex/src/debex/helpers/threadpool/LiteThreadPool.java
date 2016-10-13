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
