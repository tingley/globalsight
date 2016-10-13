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
package com.globalsight.everest.edit.offline.xliff.xliff20.document;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for stateType.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="stateType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="initial"/>
 *     &lt;enumeration value="translated"/>
 *     &lt;enumeration value="reviewed"/>
 *     &lt;enumeration value="final"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "stateType")
@XmlEnum
public enum StateType
{

    @XmlEnumValue("initial")
    INITIAL("initial"), @XmlEnumValue("translated")
    TRANSLATED("translated"), @XmlEnumValue("reviewed")
    REVIEWED("reviewed"), @XmlEnumValue("final")
    FINAL("final");
    private final String value;

    StateType(String v)
    {
        value = v;
    }

    public String value()
    {
        return value;
    }

    public static StateType fromValue(String v)
    {
        for (StateType c : StateType.values())
        {
            if (c.value.equals(v))
            {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
