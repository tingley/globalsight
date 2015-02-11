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
package com.globalsight.everest.aligner;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.foundation.User;

import com.globalsight.ling.aligner.io.AlignmentProjectFileAccessor;
import com.globalsight.ling.aligner.AlignmentProject;


import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * Implementation of AlignerManager
 */
public class AlignerManagerLocal implements AlignerManager
{
    static private final Logger CATEGORY = Logger
            .getLogger(AlignerManagerLocal.class);

    static private boolean s_isInstalled = false;

    /**
     * Flag indicating that the Corpus Aligner module is enabled per licence
     * agreement.
     */
    static public boolean isInstalled()
    {
        // ALI-2284-140957270
        String expectedKey = "ALI-" + "GS".hashCode()
                + "corpusaligner".hashCode();

        s_isInstalled = SystemConfiguration
                .isKeyValid(SystemConfigParamNames.CORPUS_ALIGNER_INSTALL_KEY);

        return s_isInstalled;
    }

    /**
     * Align source and target files specified in p_alignerPackageOptions. This
     * method returns immediately and the alignment process is done in back
     * ground. The resulting alignment package (.alp) is written in a designated
     * directory. The method throws AlignerManagerException only when critical
     * error occurs. When some of the file pair has some extraction or any othe
     * errors, they are recorded but the rest of the process continues. The
     * errors can be retrieved by getErrorMessages() method.
     * 
     * @param p_alignerPackageOptions
     *            user specified alignment options
     * @param p_user
     *            user who invoked the operation
     */
    public void batchAlign(AlignerPackageOptions p_alignerPackageOptions,
            User p_user) throws AlignerManagerException
    {
        BatchAlignThread batchAlignThread = new BatchAlignThread(
                p_alignerPackageOptions, p_user);
        batchAlignThread.start();
    }

    /**
     * Returns all alignment packages currently available. AlignmentStatus
     * contains a package name and packages's status. Package name is a base
     * name of the alignment package file.
     * 
     * @return List of AlignmentStatus objects
     */
    public List getAllPackages() throws AlignerManagerException
    {
        List packages;

        try
        {
            packages = new ArrayList(AlignmentProjectFileAccessor.read());
        }
        catch (Exception ex)
        {
            throw new AlignerManagerException(ex);
        }

        return packages;
    }

    /**
     * Returns error messages occured during the batch alignment.
     * 
     * @param p_packageName
     *            Name of a package
     * @return List of AlignmentError objects
     */
    public List getErrorMessages(String p_packageName)
            throws AlignerManagerException
    {
        List errorMessages = null;

        try
        {
            AlignmentStatus status = AlignmentProjectFileAccessor
                    .getProjectStatus(p_packageName);

            if (status != null)
            {
                errorMessages = status.getErrorMessages();
            }
        }
        catch (Exception ex)
        {
            throw new AlignerManagerException(ex);
        }

        return errorMessages;
    }

    /**
     * Delete a specified alignment package.
     * 
     * @param p_packageName
     *            Name of a package
     */
    public void deletePackage(String p_packageName)
            throws AlignerManagerException
    {
        try
        {
            // Delete the project registration first.
            AlignmentProjectFileAccessor.deleteProject(p_packageName);

            // Delete the project files last.
            deleteProjectTmpDirectory(p_packageName);

            File projectFile = AlignmentProject.getProjectFile(p_packageName);
            projectFile.delete();
        }
        catch (Exception ex)
        {
            throw new AlignerManagerException(ex);
        }
    }

    /**
     * Upload an alignment package and process it to save the aligned segments
     * to a TM. The method reads an alignment package from the specified file.
     * 
     * @param p_alignerPackageUploadOptions
     *            User scpecified upload option
     * @param p_packageFile
     *            file name of the package
     * @param p_user
     *            user who invoked the operation
     */
    public void uploadPackage(
            AlignerPackageUploadOptions p_alignerPackageUploadOptions,
            String p_packageFile, User p_user) throws AlignerManagerException
    {
        UploadThread uploadThread = new UploadThread(
                p_alignerPackageUploadOptions, p_packageFile, p_user);
        uploadThread.start();
    }

    private void deleteProjectTmpDirectory(String p_projectName)
    {
        File projectTmpDir = AlignmentProject
                .getProjectTmpDirectory(p_projectName);

        File[] files = projectTmpDir.listFiles();

        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                files[i].delete();
            }
        }

        projectTmpDir.delete();
    }
}
