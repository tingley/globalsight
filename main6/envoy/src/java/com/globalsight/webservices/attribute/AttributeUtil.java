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

package com.globalsight.webservices.attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeClone;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.SelectOption;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;

public class AttributeUtil
{
    static private final Logger s_logger = Logger
            .getLogger(AttributeUtil.class);

    public static Attribute getAttributeByInternalName(String internaName)
    {
        String hql = "from Attribute a where a.name = :name";
        Map map = new HashMap();
        map.put("name", internaName);
        return (Attribute) HibernateUtil.getFirst(hql, map);
    }

    public static JobAttribute createJobAttribute(JobAttributeVo ob)
    {
        JobAttribute att = null;
        if (ob != null)
        {
            att = new JobAttribute();

            JobAttributeVo vo = (JobAttributeVo) ob;
            Attribute attribute = getAttributeByInternalName(vo
                    .getInternalName());
            Assert.assertFalse(attribute == null,
                    "can not get attribute with internal name: "
                            + vo.getInternalName());
            att.setAttribute(attribute.getCloneAttribute());

            if (Attribute.TYPE_CHOICE_LIST.equals(attribute.getType()))
            {
                ListJobAttributeVo listVo = (ListJobAttributeVo) vo;
                att.setValue(listVo.getSelectedOptions(), false);
            }
            else if (Attribute.TYPE_INTEGER.equals(attribute.getType()))
            {
                IntJobAttributeVo intVo = (IntJobAttributeVo) vo;
                att.setValue(intVo.getValue());
            }
            else if (Attribute.TYPE_FLOAT.equals(attribute.getType()))
            {
                FloatJobAttributeVo floatVo = (FloatJobAttributeVo) vo;
                att.setValue(floatVo.getValue());
            }
            else if (Attribute.TYPE_TEXT.equals(attribute.getType()))
            {
                TextJobAttributeVo textVo = (TextJobAttributeVo) vo;
                att.setValue(textVo.getValue());
            }
            else if (Attribute.TYPE_DATE.equals(attribute.getType()))
            {
                DateJobAttributeVo dateVo = (DateJobAttributeVo) vo;
                att.setValue(dateVo.getValue());
            }
        }

        return att;
    }

    public static JobAttributeVo getAttributeVo(AttributeClone attribute)
    {
        JobAttributeVo vo = null;
        if (attribute != null)
        {
            if (Attribute.TYPE_CHOICE_LIST.equals(attribute.getType()))
            {
                ListCondition condition = (ListCondition) attribute
                        .getCondition();
                ListJobAttributeVo listVo = new ListJobAttributeVo();
                vo = listVo;

                listVo.setMultiple(condition.isMultiple());
                List<SelectOption> options = condition.getSortedAllOptions();
                for (SelectOption option : options)
                {
                    listVo.addOptions(option.getValue());
                }
            }
            else if (Attribute.TYPE_INTEGER.equals(attribute.getType()))
            {
                IntCondition conditon = (IntCondition) attribute.getCondition();
                IntJobAttributeVo intVo = new IntJobAttributeVo();
                vo = intVo;
                intVo.setMax(conditon.getMax());
                intVo.setMin(conditon.getMin());
            }
            else if (Attribute.TYPE_FLOAT.equals(attribute.getType()))
            {
                FloatCondition conditon = (FloatCondition) attribute
                        .getCondition();
                FloatJobAttributeVo floatVo = new FloatJobAttributeVo();
                vo = floatVo;
                floatVo.setMax(conditon.getMax());
                floatVo.setMin(conditon.getMin());
            }
            else if (Attribute.TYPE_TEXT.equals(attribute.getType()))
            {
                TextCondition conditon = (TextCondition) attribute
                        .getCondition();
                TextJobAttributeVo textVo = new TextJobAttributeVo();
                vo = textVo;
                textVo.setLength(conditon.getLength());
            }
            else if (Attribute.TYPE_DATE.equals(attribute.getType()))
            {
                DateJobAttributeVo dateVo = new DateJobAttributeVo();
                vo = dateVo;
            }
            else if (Attribute.TYPE_FILE.equals(attribute.getType()))
            {
                FileJobAttributeVo fileVo = new FileJobAttributeVo();
                vo = fileVo;
            }
            else
            {
                vo = new JobAttributeVo();
            }

            vo.setType(attribute.getType());
            vo.setInternalName(attribute.getName());
            vo.setDisplayName(attribute.getDisplayName());
            vo.setRequired(attribute.isRequired());
            vo.setFromSuperCompany(CompanyWrapper.SUPER_COMPANY_ID
                    .equals(attribute.getCompanyId()));
        }

        return vo;
    }

    public static JobAttributeVo getJobAttributeVo(JobAttribute jobAtt)
    {
        JobAttributeVo vo = null;

        if (jobAtt != null)
        {
            AttributeClone attribute = jobAtt.getAttribute();
            vo = getAttributeVo(attribute);

            if (Attribute.TYPE_CHOICE_LIST.equals(jobAtt.getType()))
            {
                ListJobAttributeVo listVo = (ListJobAttributeVo) vo;
                for (SelectOption option : jobAtt.getOptionValues())
                {
                    listVo.addSelectedOptions(option.getValue());
                }
            }
            else if (Attribute.TYPE_INTEGER.equals(jobAtt.getType()))
            {
                IntJobAttributeVo intVo = (IntJobAttributeVo) vo;
                intVo.setValue(jobAtt.getIntegerValue());
            }
            else if (Attribute.TYPE_FLOAT.equals(jobAtt.getType()))
            {
                FloatJobAttributeVo floatVo = (FloatJobAttributeVo) vo;
                floatVo.setValue(jobAtt.getFloatValue());
            }
            else if (Attribute.TYPE_TEXT.equals(jobAtt.getType()))
            {
                TextJobAttributeVo textVo = (TextJobAttributeVo) vo;
                textVo.setValue(jobAtt.getStringValue());
            }
            else if (Attribute.TYPE_DATE.equals(jobAtt.getType()))
            {
                DateJobAttributeVo dateVo = (DateJobAttributeVo) vo;
                dateVo.setValue(jobAtt.getDateValue());
            }
            else if (Attribute.TYPE_FILE.equals(jobAtt.getType()))
            {
                FileJobAttributeVo fileVo = (FileJobAttributeVo) vo;
                for (String file : jobAtt.getDisplayFiles())
                {
                    fileVo.addFile(file);
                }
            }
        }

        return vo;
    }
}