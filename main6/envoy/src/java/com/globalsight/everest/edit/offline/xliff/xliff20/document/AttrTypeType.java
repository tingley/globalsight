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
 * Java class for attrType_type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="attrType_type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="fmt"/>
 *     &lt;enumeration value="ui"/>
 *     &lt;enumeration value="quote"/>
 *     &lt;enumeration value="link"/>
 *     &lt;enumeration value="image"/>
 *     &lt;enumeration value="other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "attrType_type")
@XmlEnum
public enum AttrTypeType
{

    @XmlEnumValue("fmt")
    FMT("fmt"), @XmlEnumValue("ui")
    UI("ui"), @XmlEnumValue("quote")
    QUOTE("quote"), @XmlEnumValue("link")
    LINK("link"), @XmlEnumValue("image")
    IMAGE("image"), @XmlEnumValue("other")
    OTHER("other");
    private final String value;

    AttrTypeType(String v)
    {
        value = v;
    }

    public String value()
    {
        return value;
    }

    public static AttrTypeType fromValue(String v)
    {
        for (AttrTypeType c : AttrTypeType.values())
        {
            if (c.value.equals(v))
            {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
