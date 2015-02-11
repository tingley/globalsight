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
package com.globalsight.machineTranslation.promt.pts9;

public interface PTSXLIFFTranslatorSoap extends java.rmi.Remote
{
    public java.lang.String translateFormattedText(int dirId,
            java.lang.String tplId, java.lang.String strText,
            java.lang.String fileType) throws java.rmi.RemoteException;

    public void translateFormattedText2(int iDirId, java.lang.String strTplId,
            java.lang.String strSourceText, java.lang.String strFileType,
            javax.xml.rpc.holders.StringHolder translateFormattedText2Result,
            javax.xml.rpc.holders.StringHolder strError)
            throws java.rmi.RemoteException;

    public java.lang.Object[] directions() throws java.rmi.RemoteException;

    public java.lang.Object[] templates(int iDirID)
            throws java.rmi.RemoteException;
}
