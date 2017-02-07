package com.globalsight.connector.blaise;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Yan 2017/1/24
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

    String DASH = " - ";
    String HARLEY = "Harley";
    String FALCON_TARGET_VALUE = "Falcon Target Value";
    String FILENAME_PREFIX = "Blaise ID ";
    String FILENAME_EXTENSION = ".xlf";
    String ENCODING = "UTF-8";

    String USAGE_TYPE_HDU = "HD_HDU_MANUAL";
    String USAGE_TYPE_EDM = "HD_ISHEET";
}
