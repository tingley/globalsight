/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
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

package com.globalsight.everest.foundation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.foundation.DispatchCriteria;
import com.globalsight.everest.foundation.PeriodOfTime;
import com.globalsight.everest.foundation.VolumeOfData;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;

import junit.framework.TestCase;

public class BasicL10nProfileTest extends TestCase
{
    private static Set ids = new HashSet();
    private static Long defaultId = new Long(10412);

    private List getIds()
    {
        ArrayList idList = new ArrayList();
        if (ids.size() == 0)
        {
            idList.add(defaultId);
        }
        else
        {
            idList = new ArrayList(ids);
        }
        return idList;
    }

    public void testSave() throws Exception
    {
        GlobalSightLocale locale = new GlobalSightLocale();
        locale.setId(1);
        ProjectImpl p = new ProjectImpl();
        p.setId(1);
        DispatchCriteria d = new DispatchCriteria();
        d.setCondition(1);
        PeriodOfTime t = new PeriodOfTime();
        VolumeOfData v = new VolumeOfData();
        v.setVolumeContext(1);
        d.setTimer(t);
        d.setVolume(v);

        BasicL10nProfile impl = new BasicL10nProfile();
        impl.setName("name");
        impl.setPriority(3);
        impl.setAutoDispatch(false);
        impl.setTimestamp(new Timestamp(new Date().getTime()));
        impl.setRunScript(false);
        impl.setTmChoice(3);
        impl.setIsActive(true);
        impl.setCompanyId("12");

        impl.setSourceLocale(locale);
        impl.setProject(p);

        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testSaveNoNull() throws Exception
    {
        GlobalSightLocale locale = new GlobalSightLocale();
        locale.setId(1);
        ProjectImpl p = new ProjectImpl();
        p.setId(1);
        DispatchCriteria d = new DispatchCriteria();
        d.setCondition(1);
        PeriodOfTime t = new PeriodOfTime();
        VolumeOfData v = new VolumeOfData();
        v.setVolumeContext(1);
        d.setTimer(t);
        d.setVolume(v);
        TranslationMemoryProfile m = new TranslationMemoryProfile();
        m.setTuTypes("s");

        BasicL10nProfile impl = new BasicL10nProfile();
        impl.setName("name");
        impl.setPriority(3);
        impl.setAutoDispatch(false);
        impl.setTimestamp(new Timestamp(new Date().getTime()));
        impl.setRunScript(false);
        impl.setTmChoice(3);
        impl.setIsActive(true);
        impl.setCompanyId("121");
        impl.setDescription("d");
        impl.setJobCreationScriptName("s");
        impl.setIsExactMatchEditing(false);

        impl.setSourceLocale(locale);
        impl.setProject(p);
        impl.setDispatchCriteria(d);
        impl.setTmProfile(m);

        HibernateUtil.save(impl);

        ids.add(new Long(impl.getId()));
        System.out.println("Save id: " + impl.getId());
    }

    public void testGet() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            Long id = (Long) ids.get(i);
            System.out.println("id: " + id);
            BasicL10nProfile object = (BasicL10nProfile) HibernateUtil.get(
                    BasicL10nProfile.class, id);
            System.out.println(object.getTimestamp());
        }
    }

    public void testDelete() throws Exception
    {
        List ids = getIds();

        for (int i = 0; i < ids.size(); i++)
        {
            BasicL10nProfile impl = new BasicL10nProfile();

            GlobalSightLocale locale = new GlobalSightLocale();
            locale.setId(1);
            ProjectImpl p = new ProjectImpl();
            p.setId(1);
            DispatchCriteria d = new DispatchCriteria();
            d.setCondition(1);
            PeriodOfTime t = new PeriodOfTime();
            VolumeOfData v = new VolumeOfData();
            v.setVolumeContext(1);
            d.setTimer(t);
            d.setVolume(v);

            impl.setName("name");
            impl.setPriority(3);
            impl.setAutoDispatch(false);
            impl.setTimestamp(new Timestamp(new Date().getTime()));
            impl.setRunScript(false);
            impl.setTmChoice(3);
            impl.setIsActive(true);
            impl.setCompanyId("s");

            impl.setSourceLocale(locale);
            impl.setProject(p);

            Long id = (Long) ids.get(i);
            impl.setId(id.longValue());

            System.out.println("Delete id: " + id);
            HibernateUtil.delete(impl);
        }
    }
}
