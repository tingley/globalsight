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


// java imports
import java.util.Collection;

//
// globalsight imports
//
import com.globalsight.everest.tuv.Tu;


/**
 * The LeverageGroup interface represents a group of
 * translation units that are to be leveraged together.
 * The group are related in a way that a leverage hit
 * within the group is of higher quality than a leverage
 * hit outside the group.
 */
public interface LeverageGroup
{

    /**
     * Get LeverageGroup unique identifier.
     * @return unique identifier.
     */
    public long getLeverageGroupId();


    /**
     * Get LeverageGroup unique identifier.
     * @return unique identifier.
     */
    public long getId();


    /**
     * Return the persistent object's id as a Long object.
     * <p>
     * This is a convenience method that simply wraps the id as an object, so
     * that, for example, the idAsLong can be used as a Hashtable key.
     *
     * @return the unique identifier as a Long object.
     */
    public Long getIdAsLong();

    /**
     * Add a Tu to the LeverageGoup.
     * @param p_tu Tu to add.
     */
    public void addTu(Tu p_tu);

    /**
     * Get a collection of Tus for this leverage group.
     * @return A collection of Tus.
     */
    Collection<Tu> getTus();
    Collection<Tu> getTus(boolean p_loadFromDb);
}
