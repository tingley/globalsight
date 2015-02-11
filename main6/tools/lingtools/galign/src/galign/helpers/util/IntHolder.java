/*
 * Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package galign.helpers.util;

/**
 * A wrapper object for an int. In contrast to Integer, this object is
 * mutable and the int value can be modified.
 */
public class IntHolder
{
    /**
     * The int value. A public field for lazy programmers.
     */
    public int value;

    public IntHolder()
    {
        value = 0;
    }

    public IntHolder(int p_value)
    {
        value = p_value;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int p_value)
    {
        value = p_value;
    }

    /**
     * The post-increment ++ operator: increments the value by one,
     * and returns the old value.
     */
    public int inc()
    {
        return value++;
    }

    /**
     * The post-decrement -- operator: decrements the value by one,
     * and returns the old value.
     */
    public int dec()
    {
        return value--;
    }
}
