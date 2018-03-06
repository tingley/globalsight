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
package com.globalsight.connector.blaise;

/**
 * Constants for Blaise feature
 */
public interface BlaiseConstants
{
    String BLAISE_TYPE_GRAPHIC = "com.cognitran.publication.model.media.Graphic";
    String GS_TYPE_GRAPHIC = "Graphic";
    String BLAISE_TYPE_STANDALONE = "com.cognitran.publication.model.standalone.StandalonePublication";
    String GS_TYPE_STANDALONE = "Standalone";
    String BLAISE_TYPE_PROCEDURE = "com.cognitran.publication.model.composite.Procedure";
    String GS_TYPE_PROCEDURE = "Procedure";
    String BLAISE_TYPE_CONTROLLED_CONTENT = "com.cognitran.publication.model.controlled.ControlledContent";
    String GS_TYPE_CONTROLLED_CONTENT = "Controlled content";
    String BLAISE_TYPE_TRANSLATABLE_OBJECT = "com.cognitran.translation.model.TranslatableObjectsDocument";
    String GS_TYPE_TRANSLATABLE_OBJECT = "Translatable object";
    String BLAISE_TYPE_PARTS_CATALOG = "com.cognitran.translation.model.PartsCatalog";
    String GS_TYPE_PARTS_CATALOG = "Parts Catalog";

    String DASH = " - ";
    String HARLEY = "Harley";
    String FALCON_TARGET_VALUE = "Falcon Target Value";
    String FILENAME_PREFIX = "Blaise ID ";
    String FILENAME_EXTENSION = ".xlf";
    String ENCODING = "UTF-8";

    String USAGE_ISHEET = "HD_ISHEET";
    String USAGE_OWNER_MANUAL = "HD_OWNER_MANUAL";
    String USAGE_SERVICE_MANUAL = "HD_SERVICE_MANUAL";
    String USAGE_PDI_MANUAL = "HD_PDI_MANUAL";
    String USAGE_EDM_MANUAL = "HD_EDM_MANUAL";
    String USAGE_HDU_WORKBOOK = "HD_HDU_Workbook";
}
