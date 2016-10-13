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
package com.globalsight.cxe.adapter.documentum;

import com.documentum.fc.common.DfException;
import com.documentum.operations.IDfOperation;
import com.documentum.operations.IDfOperationError;
import com.documentum.operations.IDfOperationMonitor;
import com.documentum.operations.IDfOperationNode;
import com.documentum.operations.IDfOperationStep;

/**
 * The <code>Progress</code> class describes the progress of DCTM operations. 
 * 
 */
public class Progress implements IDfOperationMonitor
{
    public int progressReport( IDfOperation op, int iPercentOpDone,
                               IDfOperationStep step, int iPercentStepDone,
                               IDfOperationNode node ) throws DfException {
        return IDfOperationMonitor.CONTINUE;
    }

    public int reportError( IDfOperationError error ) throws DfException {
        return IDfOperationMonitor.CONTINUE;
    }

    public int getYesNoAnswer( IDfOperationError Question ) throws DfException {
        return IDfOperationMonitor.YES;
    }
}