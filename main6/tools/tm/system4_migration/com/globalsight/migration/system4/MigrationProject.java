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

package com.globalsight.migration.system4;

import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectHandlerLocal;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.TopLinkPersistence;
import com.globalsight.everest.persistence.project.ProjectQueryNames;

import java.util.Iterator;
import java.util.Vector;
import java.util.Collection;

/**
 * This class is responsible for creating a Migration Project.
 */
public class MigrationProject
{
    private static final String PROJECT_NAME = "System 3 Migration";
    
    /**
     * get a migration project. If it doesn't exist yet, the method
     * creates a new one.
     * @param p_user a project manager
     * @return a migration project
     */
    public static Project get(User p_user)
        throws Exception
    {
        Project migrationProject = getProjectByName(PROJECT_NAME);
        if(migrationProject == null)
        {
            migrationProject = new ProjectImpl();
            migrationProject.setName(PROJECT_NAME);
            migrationProject.setDescription("Migration from System 3 to System 4");
            migrationProject.setProjectManager(p_user);
            Tm tm = MigrationTm.get("dummy");
            migrationProject.setTranslationMemory(tm);
            
            ProjectHandler projectHandler = new ProjectHandlerLocal();
            projectHandler.addProject(migrationProject);
        }
        return migrationProject;
    }


    private static Project getProjectByName(String p_name)
        throws Exception
    {
        Vector queryArgs = new Vector();
        queryArgs.add(p_name);
        Project project = null;

        PersistenceService persistence = PersistenceService.getInstance();
        Collection result =
            persistence.executeNamedQuery(ProjectQueryNames.PROJECT_BY_NAME,
                                          queryArgs,
                                          TopLinkPersistence.MAKE_EDITABLE);
        if (result == null || result.isEmpty())
        {
            return null;
        }
        return (Project)result.iterator().next();
    }

}
