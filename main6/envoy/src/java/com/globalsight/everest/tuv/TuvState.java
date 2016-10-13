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

package com.globalsight.everest.tuv;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.TuvException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

/**
 * Represents the state of a Tuv.
 */
public final class TuvState implements Serializable
{
    private static final long serialVersionUID = 4923628543358721762L;

    private static final Logger CATEGORY = Logger.getLogger(TuvState.class
            .getName());

    private String m_name;
    private int m_value;

    // static TuvStates
    public static final TuvState UNSPECIFIED =
        new TuvState("UNSPECIFIED", 0);
    public static final TuvState NOT_LOCALIZED =
        new TuvState("NOT_LOCALIZED", 1);
    public static final TuvState LOCALIZED =
        new TuvState("LOCALIZED", 2);
    public static final TuvState COMPLETE =
        new TuvState("COMPLETE", 3);
    public static final TuvState OUT_OF_DATE =
        new TuvState("OUT_OF_DATE", 4);
    public static final TuvState LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED =
        new TuvState("LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED", 5);
    public static final TuvState EXACT_MATCH_LOCALIZED =
        new TuvState("EXACT_MATCH_LOCALIZED", 6);
    public static final TuvState ALIGNMENT_LOCALIZED =
        new TuvState("ALIGNMENT_LOCALIZED", 7);
    public static final TuvState UNVERIFIED_EXACT_MATCH =
        new TuvState("UNVERIFIED_EXACT_MATCH", 8);
    public static final TuvState APPROVED = new TuvState("APPROVED", 9);
    public static final TuvState DO_NOT_TRANSLATE = new TuvState("DO_NOT_TRANSLATE", 10);

    public static final HashMap<Integer, TuvState> ALL_TUV_STATES_BY_VALUE = new HashMap<Integer, TuvState>(10);
    public static final HashMap<String, TuvState> ALL_TUV_STATES_BY_NAME = new HashMap<String, TuvState>(10);

    static
    {
        ALL_TUV_STATES_BY_VALUE.put(
            new Integer(UNSPECIFIED.getValue()), UNSPECIFIED);
        ALL_TUV_STATES_BY_VALUE.put(
            new Integer(NOT_LOCALIZED.getValue()), NOT_LOCALIZED);
        ALL_TUV_STATES_BY_VALUE.put(
            new Integer(LOCALIZED.getValue()), LOCALIZED);
        ALL_TUV_STATES_BY_VALUE.put(
            new Integer(COMPLETE.getValue()), COMPLETE);
        ALL_TUV_STATES_BY_VALUE.put(
            new Integer(OUT_OF_DATE.getValue()), OUT_OF_DATE);
        ALL_TUV_STATES_BY_VALUE.put(
            new Integer(LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED.getValue()),
            LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED);
        ALL_TUV_STATES_BY_VALUE.put(
            new Integer(EXACT_MATCH_LOCALIZED.getValue()), EXACT_MATCH_LOCALIZED);
        ALL_TUV_STATES_BY_VALUE.put(
            new Integer(ALIGNMENT_LOCALIZED.getValue()), ALIGNMENT_LOCALIZED);
        ALL_TUV_STATES_BY_VALUE.put(
            new Integer(UNVERIFIED_EXACT_MATCH.getValue()),
            UNVERIFIED_EXACT_MATCH);
        ALL_TUV_STATES_BY_VALUE.put(new Integer(APPROVED.getValue()), APPROVED);
        ALL_TUV_STATES_BY_VALUE.put(new Integer(DO_NOT_TRANSLATE.getValue()),
                DO_NOT_TRANSLATE);

        ALL_TUV_STATES_BY_NAME.put(UNSPECIFIED.getName(), UNSPECIFIED);
        ALL_TUV_STATES_BY_NAME.put(NOT_LOCALIZED.getName(), NOT_LOCALIZED);
        ALL_TUV_STATES_BY_NAME.put(LOCALIZED.getName(), LOCALIZED);
        ALL_TUV_STATES_BY_NAME.put(COMPLETE.getName(), COMPLETE);
        ALL_TUV_STATES_BY_NAME.put(OUT_OF_DATE.getName(), OUT_OF_DATE);
        ALL_TUV_STATES_BY_NAME.put(LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED.getName(),
            LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED);
        ALL_TUV_STATES_BY_NAME.put(EXACT_MATCH_LOCALIZED.getName(),
            EXACT_MATCH_LOCALIZED);
        ALL_TUV_STATES_BY_NAME.put(ALIGNMENT_LOCALIZED.getName(),
            ALIGNMENT_LOCALIZED);
        ALL_TUV_STATES_BY_NAME.put(UNVERIFIED_EXACT_MATCH.getName(),
            UNVERIFIED_EXACT_MATCH);
        ALL_TUV_STATES_BY_NAME.put(APPROVED.getName(), APPROVED);
        ALL_TUV_STATES_BY_NAME
                .put(DO_NOT_TRANSLATE.getName(), DO_NOT_TRANSLATE);

        // self check that the two HashMaps contain the same values
        try
        {
            consistencyTest();
        }
        catch (TuvException te)
        {
        }
    }

    /**
     * Return the TuvState name.
     * @returns the TuvState name.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the TuvState value.
     * @returns the TuvState value.
     */
    public int getValue()
    {
        return m_value;
    }

    /**
     * Return a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString()
    {
        return getClass().getName()
            + " m_name=" + m_name
            + " m_value=" + Integer.toString(m_value)
            ;
    }

    /**
     * Convert the String to a TuvState.
     * @returns the TuvState.
     * @throws TuvException if the name is not a valid TuvState name.
     */
    public static TuvState valueOf(String p_name) throws TuvException
    {
        TuvState tuvState =
            (TuvState)ALL_TUV_STATES_BY_NAME.get(p_name);

        if (tuvState != null)
        {
            return tuvState;
        }

        throw new TuvException(p_name + " is not a TuvState name");
    }

    /**
     * Convert the int value to a TuvState.
     * @returns the TuvState, null if not found.
     * @throws TuvException if the value is not a valid TuvState value.
     */
    public static TuvState valueOf(int p_value) throws TuvException
    {
        TuvState tuvState =
            (TuvState)ALL_TUV_STATES_BY_VALUE.get(new Integer(p_value));

        if (tuvState != null)
        {
            return tuvState;
        }

        throw new TuvException(Integer.toString(p_value) +
            " is not a TuvState value");
    }

    /**
     * Convert the String to the TuvState value.
     * @returns the TuvState value, null if not found.
     * @throws TuvException if the name is not a valid TuvState name.
     */
    public static int toInt(String p_name) throws TuvException
    {
        return valueOf(p_name).getValue();
    }

    /**
     * Convert the int value to a TuvState name.
     * @returns the TuvState name.
     * @throws TuvException if the value is not a valid TuvState value.
     */
    public static String toString(int p_value) throws TuvException
    {
        return valueOf(p_value).getName();
    }

    /**
     * Test that the two HashMaps are consistent.
     * @throws TuvException if they are not consistent.
     */
    static void consistencyTest() throws TuvException
    {
        TuvState tuvState = null;
        Collection<TuvState> values = null;
        Iterator<TuvState> valuesIt = null;

        values = ALL_TUV_STATES_BY_VALUE.values();
        valuesIt = values.iterator();

        while (valuesIt.hasNext())
        {
            tuvState = (TuvState)valuesIt.next();

            if (tuvState != valueOf(tuvState.getValue()))
            {
                CATEGORY.error(tuvState.toString()
                    + " not in ALL_TUV_STATES_BY_VALUE correctly");
                throw new TuvException(tuvState.toString()
                    + " not in ALL_TUV_STATES_BY_VALUE correctly");
            }

            if (tuvState != valueOf(tuvState.getName()))
            {
                CATEGORY.error(tuvState.toString()
                    + " not in ALL_TUV_STATES_BY_NAME correctly");
                throw new TuvException(tuvState.toString()
                    + " not in ALL_TUV_STATES_BY_NAME correctly");
            }
        }

        values = ALL_TUV_STATES_BY_NAME.values();
        valuesIt = values.iterator();

        while (valuesIt.hasNext())
        {
            tuvState = (TuvState)valuesIt.next();

            if (tuvState != valueOf(tuvState.getValue()))
            {
                CATEGORY.error(tuvState.toString()
                    + " not in ALL_TUV_STATES_BY_VALUE correctly");
                throw new TuvException(tuvState.toString()
                    + " not in ALL_TUV_STATES_BY_VALUE correctly");
            }

            if (tuvState != valueOf(tuvState.getName()))
            {
                CATEGORY.error(tuvState.toString()
                    + " not in ALL_TUV_STATES_BY_NAME correctly");
                throw new TuvException(tuvState.toString()
                    + " not in ALL_TUV_STATES_BY_NAME correctly");
            }
        }
    }

    /**
     * Compare TuvStates for equality.
     * @returns true if they are equal.
     */
    public boolean equals(TuvState p_TuvState)
    {
        if (m_value == p_TuvState.m_value)
        {
            return true;
        }

        return false;
    }

    private TuvState()
    {
    }

    private TuvState(String p_name, int p_value)
    {
        m_name = p_name;
        m_value = p_value;
    }
}
