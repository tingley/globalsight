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

import com.globalsight.everest.foundation.User;
import java.rmi.RemoteException;
import java.util.List;

/**
 * AlignerManager is responsible for the interaction between the
 * aligner and the UI.
 */
public interface AlignerManager
{
    /**
     * Align source and target files specified in
     * p_alignerPackageOptions. This method returns immediately and
     * the alignment process is done in back ground. The resulting
     * alignment package (.alp) is written in a designated
     * directory. The method throws AlignerManagerException only when
     * critical error occurs. When some of the file pair has some
     * extraction or any othe errors, they are recorded but the rest
     * of the process continues. The errors can be retrieved by
     * getErrorMessages() method.
     *
     * @param p_alignerPackageOptions user specified alignment options
     * @param p_user user who invoked the operation
     */
    public void batchAlign(
        AlignerPackageOptions p_alignerPackageOptions, User p_user)
        throws RemoteException, AlignerManagerException;


    /**
     * Returns all alignment packages currently
     * available. AlignmentStatus contains a package name and
     * packages's status. Package name is a base name of the alignment
     * package file.
     *
     * @return List of AlignmentStatus objects
     */
    public List getAllPackages()
        throws RemoteException, AlignerManagerException;


    /**
     * Returns error messages occured during the batch alignment.
     *
     * @param p_packageName Name of a package
     * @return List of AlignmentError objects
     */
    public List getErrorMessages(String p_packageName)
        throws RemoteException, AlignerManagerException;


    /**
     * Delete a specified alignment package.
     *
     * @param p_packageName Name of a package
     */
    public void deletePackage(String p_packageName)
        throws RemoteException, AlignerManagerException;


    /**
     * Upload an alignment package and process it to save the aligned
     * segments to a TM. The method reads an alignment package from
     * the specified file.
     *
     * @param p_alignerPackageUploadOptions User scpecified upload option
     * @param p_packageFile file name of the package
     * @param p_user user who invoked the operation
     */
    public void uploadPackage(
        AlignerPackageUploadOptions p_alignerPackageUploadOptions,
        String p_packageFile, User p_user)
        throws RemoteException, AlignerManagerException;

}
