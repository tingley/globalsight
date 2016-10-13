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

package galign.helpers;

/**
 * This interface decouples UI controller classes from worker threads.
 * A controller class starts a worker thread passing it Callback
 * instances for each possible outcome (onSuccess, onFailure, etc).
 * The worker thread calls the callbacks on the UI threads (using a
 * Runnable instance).
 */
public interface Callback
{
    public void call();
}
