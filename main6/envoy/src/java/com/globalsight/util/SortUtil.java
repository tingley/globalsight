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

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import com.globalsight.everest.util.comparator.CachingStringComparator;

/**
 * Uses jdk 1.6's sort methods to replace Collections.sort() for resolving the
 * "Comparison method violates its general contract" error.
 */
public class SortUtil
{
    public static <T> void sort(List<T> list)
    {
        sort(list, null);
    }

    public static <T> void sort(List<T> list, Comparator<? super T> c)
    {
        Object[] a = list.toArray();
        sort(a, (Comparator) c);
        ListIterator i = list.listIterator();
        for (int j = 0; j < a.length; j++)
        {
            i.next();
            i.set(a[j]);
        }
        if (c instanceof CachingStringComparator)
        {
            // Release any memory held by cached CollationKeys
            ((CachingStringComparator) c).clearCollationKeyCache();
        }
    }

    public static <T> void sort(T[] a, Comparator<? super T> c)
    {
        T[] aux = (T[]) a.clone();
        if (c == null)
            mergeSort(aux, a, 0, a.length, 0);
        else
            mergeSort(aux, a, 0, a.length, 0, c);
    }

    /**
     * Src is the source array that starts at index 0 Dest is the (possibly
     * larger) array destination with a possible offset low is the index in dest
     * to start sorting high is the end index in dest to end sorting off is the
     * offset into src corresponding to low in dest
     */
    private static void mergeSort(Object[] src, Object[] dest, int low,
            int high, int off, Comparator c)
    {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < 7)
        {
            for (int i = low; i < high; i++)
                for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; j--)
                    swap(dest, j, j - 1);
            return;
        }

        // Recursively sort halves of dest into src
        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off, c);
        mergeSort(dest, src, mid, high, -off, c);

        // If list is already sorted, just copy from src to dest. This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (c.compare(src[mid - 1], src[mid]) <= 0)
        {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = destLow, p = low, q = mid; i < destHigh; i++)
        {
            if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    /**
     * Src is the source array that starts at index 0 Dest is the (possibly
     * larger) array destination with a possible offset low is the index in dest
     * to start sorting high is the end index in dest to end sorting off is the
     * offset to generate corresponding low, high in src
     */
    private static void mergeSort(Object[] src, Object[] dest, int low,
            int high, int off)
    {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < 7)
        {
            for (int i = low; i < high; i++)
                for (int j = i; j > low
                        && ((Comparable) dest[j - 1]).compareTo(dest[j]) > 0; j--)
                    swap(dest, j, j - 1);
            return;
        }

        // Recursively sort halves of dest into src
        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off);
        mergeSort(dest, src, mid, high, -off);

        // If list is already sorted, just copy from src to dest. This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0)
        {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = destLow, p = low, q = mid; i < destHigh; i++)
        {
            if (q >= high || p < mid
                    && ((Comparable) src[p]).compareTo(src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(Object[] x, int a, int b)
    {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}
