/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.globalsight.webservices;

import javax.jws.WebService;

@WebService(endpointInterface = "com.globalsight.webservices.WebService4AEM", serviceName = "WebService4AEM", portName = "WebService4AEMPort")
public class WebService4AEMImpl extends Ambassador implements WebService4AEM {
	@Override
	public String uploadFileForInitial(WrapHashMap map)
			throws WebServiceException {
		return this.uploadFileForInitial(map.getInputData());
	}
}
