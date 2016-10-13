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

package com.globalsight.util;

import com.globalsight.util.ReaderResult;

import java.util.ArrayList;
import java.util.Stack;

/**
 * A synchronized producer-consumer queue of a certain length (default 100).
 *
 * Objects to be put in the queue are also cached in a pool so object
 * allocation is minimized.
 *
 * @see http://java.sun.com/docs/books/tutorial/essential/threads/synchronization.html
 */
public class ReaderResultQueue
{
    //
    // Private Members
    //

    /** Max number of objects that can be pushed */
    private int m_maxSize = 100;
    private int m_size = 0;

    /** Queue of ReaderResult objects */
    private ArrayList m_queue = new ArrayList(m_maxSize);

    /**
     * Indicates the producer has stopped producing and the consumer
     * can thus stop consuming.
     */
    private boolean m_producerDone = false;

    /**
     * Indicates the consumer has stopped consuming and the producer
     * can thus stop producing.
     */
    private boolean m_consumerDone = false;

    private Stack m_results;

    //
    // Constructor
    //
    public ReaderResultQueue(int p_maxSize)
    {
        m_maxSize = p_maxSize;
        m_queue.ensureCapacity(m_maxSize);

        m_results = new Stack();

        for (int i = 0; i < m_maxSize; ++i)
        {
            m_results.push(new ReaderResult());
        }
    }

    //
    // Public Methods
    //

    public synchronized ReaderResult get()
    {
        ReaderResult result;

        while (m_size == 0 && !m_producerDone)
        {
            try
            {
                // wait for Producer to put value
                wait();
            }
            catch (InterruptedException e)
            {
            }
        }

        // Drain remaining results
        if (m_size > 0)
        {
            result = (ReaderResult)m_queue.remove(0);
            --m_size;

            // notify Producer that value can be pushed
            notifyAll();
        }
        else
        {
            // m_done is true, return null pointer
            result = null;
        }

        return result;
    }

    /**
     * Put a new value in the queue. Returns TRUE if consumer has
     * stopped running and production should stop.
     */
    public synchronized boolean put(ReaderResult p_value)
    {
        while (m_size == m_maxSize && !m_consumerDone)
        {
            try
            {
                // wait for Consumer to get value
                wait();
            }
            catch (InterruptedException e)
            {
            }
        }

        if (m_consumerDone)
        {
            return true;
        }

        m_queue.add(p_value);
        ++m_size;

        notifyAll();

        return false;
    }


    /**
     * For the producer only: notifies consumer that production has
     * closed shop.
     */
    public synchronized void producerDone()
    {
        m_producerDone = true;
        notifyAll();
    }

    /**
     * For the consumer only: notifies producer that consumption has
     * closed shop.
     */
    public synchronized void consumerDone()
    {
        m_consumerDone = true;
        notifyAll();
    }

    public synchronized ReaderResult hireResult()
    {
        if (m_results.empty())
        {
            return new ReaderResult();
        }

        return (ReaderResult)m_results.pop();
    }

    public synchronized void fireResult(ReaderResult p_result)
    {
        p_result.clear();
        m_results.push(p_result);
    }
}
