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
package com.globalsight.ling.lucene.analysis;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.BitSet;

/**
 * Allows multiple {@link Filter}s to be chained.
 *
 * Logical operations such as <b>NOT</b> and <b>XOR</b> are applied
 * between filters. One operation can be used for all filters, or a
 * specific operation can be declared for each filter.  </p> <p> Order
 * in which filters are called depends on the position of the filter
 * in the chain. It's probably more efficient to place the most
 * restrictive filters /least computationally-intensive filters first.
 *
 * @author <a href="mailto:kelvint@apache.org">Kelvin Tan</a>
 * @deprecated can not find where this class is used.
 */
public class ChainedFilter
    extends Filter
{
    /**
     * {@link BitSet#or}.
     */
    public static final int OR = 0;

    /**
     * {@link BitSet#and}.
     */
    public static final int AND = 1;

    /**
     * {@link BitSet#andNot}.
     */
    public static final int ANDNOT = 2;

    /**
     * {@link BitSet#xor}.
     */
    public static final int XOR = 3;

    /**
     * Logical operation when none is declared. Defaults to
     * {@link BitSet#or}.
     */
    public static int DEFAULT = OR;

    /** The filter chain */
    private Filter[] chain = null;

    private int[] logicArray;

    private int logic = -1;

    /**
     * Ctor.
     * @param chain The chain of filters
     */
    public ChainedFilter(Filter[] chain)
    {
        this.chain = chain;
    }

    /**
     * Ctor.
     * @param chain The chain of filters
     * @param logicArray Logical operations to apply between filters
     */
    public ChainedFilter(Filter[] chain, int[] logicArray)
    {
        this.chain = chain;
        this.logicArray = logicArray;
    }

    /**
     * Ctor.
     * @param chain The chain of filters
     * @param logic Logicial operation to apply to ALL filters
     */
    public ChainedFilter(Filter[] chain, int logic)
    {
        this.chain = chain;
        this.logic = logic;
    }

    /**
     * {@link Filter#bits}.
     */
    public BitSet bits(IndexReader reader)
        throws IOException
    {
        if (logic != -1)
            return bits(reader, logic);
        else if (logicArray != null)
            return bits(reader, logicArray);
        else
            return bits(reader, DEFAULT);
    }

    /**
     * Delegates to each filter in the chain.
     * @param reader IndexReader
     * @param logic Logical operation
     * @return BitSet
     */
    private BitSet bits(IndexReader reader, int logic)
        throws IOException
    {
        BitSet result;
        int i = 0;

        // First AND operation takes place against a completely false
        // bitset and will always return zero results. Thanks to
        // Daniel Armbrust for pointing this out and suggesting workaround.
        if (logic == AND)
        {
            result = new BitSet();
            //result = (BitSet)chain[i].bits(reader).clone();
            ++i;
        }
        else
        {
            result = new BitSet(reader.maxDoc());
        }

        for (; i < chain.length; i++)
        {
            doChain(result, reader, logic, chain[i]);
        }
        return result;
    }

    /**
     * Delegates to each filter in the chain.
     * @param reader IndexReader
     * @param logic Logical operation
     * @return BitSet
     */
    private BitSet bits(IndexReader reader, int[] logic)
        throws IOException
    {
        if (logic.length != chain.length)
        {
            throw new IllegalArgumentException(
                "Invalid number of elements in logic array");
        }

        BitSet result;
        int i = 0;

        // First AND operation takes place against a completely false
        // bitset and will always return zero results. Thanks to
        // Daniel Armbrust for pointing this out and suggesting workaround.
        if (logic[0] == AND)
        {
            result = new BitSet();
            //result = (BitSet)chain[i].bits(reader).clone();
            ++i;
        }
        else
        {
            result = new BitSet(reader.maxDoc());
        }

        for (; i < chain.length; i++)
        {
            doChain(result, reader, logic[i], chain[i]);
        }

        return result;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("ChainedFilter: [");
        for (int i = 0; i < chain.length; i++)
        {
            sb.append(chain[i]);
            sb.append(' ');
        }
        sb.append(']');

        return sb.toString();
    }

    private void doChain(BitSet result, IndexReader reader,
        int logic, Filter filter)
        throws IOException
    {
        /*
        switch (logic)
        {
        case OR:
            result.or(filter.bits(reader));
            break;
        case AND:
            result.and(filter.bits(reader));
            break;
        case ANDNOT:
            result.andNot(filter.bits(reader));
            break;
        case XOR:
            result.xor(filter.bits(reader));
            break;
        default:
            doChain(result, reader, DEFAULT, filter);
            break;
        }
        */
    }

    @Override
    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs)
            throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
