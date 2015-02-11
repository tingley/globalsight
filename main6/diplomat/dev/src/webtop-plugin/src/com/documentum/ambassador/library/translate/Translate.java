/*
 * Copyright (c) 2004-2005, GlobalSight Corporation. All rights reserved. THIS
 * DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF GLOBALSIGHT
 * CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT IN CONFIDENCE.
 * INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED OR DISCLOSED IN WHOLE OR
 * IN PART EXCEPT AS PERMITTED BY WRITTEN AGREEMENT SIGNED BY AN OFFICER OF
 * GLOBALSIGHT CORPORATION.
 * 
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER SECTIONS 104
 * AND 408 OF TITLE 17 OF THE UNITED STATES CODE. UNAUTHORIZED USE, COPYING OR
 * OTHER REPRODUCTION IS PROHIBITED BY LAW.
 */
package com.documentum.ambassador.library.translate;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import com.documentum.ambassador.util.FileProfile;
import com.documentum.ambassador.util.SysobjAttributes;
import com.documentum.ambassador.webservice.AmbassadorConfiguration;
import com.documentum.ambassador.webservice.AmbassadorWebServiceClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.form.control.databound.TableResultSet;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.control.action.ActionControl;
import com.documentum.web.formext.control.action.ActionMultiselect;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.web.formext.docbase.ObjectCacheUtil;
import com.documentum.web.formext.session.DocbaseUtils;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.web.util.DfcUtils;

/**
 * @ see <code>Component</code>
 */
public class Translate extends Component {
    public final static String CTRL_DROP_LIST_NAME = "file_profile_list";
    
    public final static String DATA_DROP_LIST_ID = "file_profile_id";
    
    public final static String DATA_DROP_LIST_LABEL = "file_profile_label";
    
    public final static String CTRL_TEXT_NAME = "attribute_object_name";
    
    public final static String CTRL_DATAGRID_NAME = "target_locale_checkbox_list";
    
    public final static String LOCALE_SET_CODE = "target_language_code";
    
    public final static String LOCALES = "target_language";
    
    public final static String CTRL_MULTI_SELECT_NAME = "multiTargetLanguages";
    
    public final static String PARAM_TARGET_LOCALE = "targetLocale";
    
    private final static String DATE_FORMAT = "yyyyMMdd";
    
    private final static String SQL_SELECT = "select";

    private final static String SQL_FROM = "from";

    private final static String SQL_WHERE = "where";

    private final static String Field_OBJECT_ID = "r_object_id";

    private static String SINGLE_QUOTES = "'";
    
    private static String SPACE = " ";
    
	private static String UNDER_SCORE = "_";

    private static String COMMA = ",";
    
    private static String EQUALS_SIGN = "=";
    
    private static char CHECK_ON = '1';
    
    private static String ERR_MSG_FAILED_READ_QUERY = "Failed to read query.";
    
    private static String ERR_MSG_FAILED_CLOSE_COLLECTION = "Failed to close collection.";

	private String m_strObjectId = null;

	private String m_strObjectName = null;

	private String m_strObjectVersion = null;

	private String m_strFileProfiles = null;

	private String m_strDocbaseName = null;

	private String m_strFolderPath = null;

    private String m_strAttrTargetLanguage = null;

    private String m_strJobName = null;

	private Map m_hashTargetLocales = null;

    private String m_strObjectExtension = null;
    
    private String m_strObjectSourceLocale = null;
    
    private final static String DEFAULT_SOURCE_LOCALE = "en_US";

	public void onInit(ArgumentList args) {
		super.onInit(args);

		initValue(args);
        initDefaultJobName();
        initFileProfileList();
        initDynamicCheckBox();
	}

    /**
     * Use fileprifile List as the data source of the dropdownlist control.
     * 
     * @param fileProfileList
     */
    private void setFileProfileData(List fileProfileList) {
        
        int fileProfileCount = 0;
        if( fileProfileList != null ) {
            fileProfileCount = fileProfileList.size();
        }
        String[][] idAndLabel = null;
        if (fileProfileCount != 0) {

            idAndLabel =  new String[fileProfileCount][2];
            for (int i = 0; i < fileProfileCount; i++) {
                // fileprofile object includes id and name attributes.
                idAndLabel[i][0] = ((FileProfile) fileProfileList.get(i))
                        .getId();
                idAndLabel[i][1] = ((FileProfile) fileProfileList.get(i))
                        .getName();
            }
        }
              
        updateDropdownListData(idAndLabel);
    }
    
    /**
     * Use current data result to update dropdownlist control.
     * 
     * @param strDataSource
     */
    private void updateDropdownListData(String[][] strDataSource) {
        if (strDataSource == null) {
            strDataSource = new String[1][2];
        }
        
        DataDropDownList ddlFileProfiles = (DataDropDownList) getControl(
                CTRL_DROP_LIST_NAME, DataDropDownList.class);
        
        if (ddlFileProfiles != null) {           
            TableResultSet tableSet = new TableResultSet(strDataSource,
                    new String[] { DATA_DROP_LIST_ID, DATA_DROP_LIST_LABEL });
            ddlFileProfiles.getDataProvider().setScrollableResultSet(tableSet);
        }        
    }
 
    /**
     * Initialize all member variables.
     * 
     * @param args
     * @return true if all initial values are initialized successfully, or
     *         return false.
     */
	private boolean initValue(ArgumentList args) {
		try {
            m_strObjectId = args.get(SysobjAttributes.DM_OBJECT_ID);

			IDfSession dfSession = this.getDfSession();
			IDfSysObject sysObject = (IDfSysObject) ObjectCacheUtil.getObject(
					dfSession, m_strObjectId);
			m_strDocbaseName = DocbaseUtils.getDocbaseNameFromId(m_strObjectId);
			m_strFolderPath = FolderUtil.getPrimaryFolderPath(m_strObjectId,
					FolderUtil.isFolderType(m_strObjectId));
			m_strObjectName = sysObject.getObjectName();
            m_strObjectVersion = sysObject
                    .getString(SysobjAttributes.DM_OBJECT_VERSION_LABEL);
            
            // The default value of "language code" attribute is "",
            // so use the value "en_US" as its source locale.
            // If the value is not "", 
            // use it as source locale.
            String strLanguageCode = sysObject.getString(
                    SysobjAttributes.DM_OBJECT_LANGUAGE_CODE);
            if(strLanguageCode == null || strLanguageCode.length() == 0) {
                m_strObjectSourceLocale = DEFAULT_SOURCE_LOCALE;
            }
            else {
                m_strObjectSourceLocale = strLanguageCode;
            }
            
            AmbassadorConfiguration cfg = AmbassadorConfiguration.getInstance();
            String targetLanguagesLabel = cfg
                    .getPropertyValue(AmbassadorConfiguration.DM_OBJECT_TARGET_LANGUANGES);
            m_strAttrTargetLanguage = sysObject.getString(targetLanguagesLabel);
            m_strObjectExtension = sysObject.getFormat().getDOSExtension();

			return true;
		} catch (Exception e) {
			throw new WrapperRuntimeException(e);
		}
	}

    /**
     * 
     * The job name will be generated by the plug-in automatically. The naming
     * convention would be : [Date Time]_[File Path Name]_[fileversion]
     *  
     */
    private void initDefaultJobName() {
		StringBuffer jobNameSb = new StringBuffer();
		// add the current date time into default job name.
        SimpleDateFormat sdFormat = new SimpleDateFormat(DATE_FORMAT);
        jobNameSb.append(sdFormat.format(new Date()));
        jobNameSb.append(UNDER_SCORE);
        jobNameSb.append(this.m_strObjectVersion);
        jobNameSb.append(UNDER_SCORE);
		// add file path name into default job name.
        jobNameSb.append(this.m_strObjectName);

        Text txtJobName = (Text) getControl(CTRL_TEXT_NAME, Text.class);
		if (txtJobName != null) {
            txtJobName.setValue(jobNameSb.toString());
		}
	}

    /**
     * Initialize the dropdownlist to show file profiles.
     */
    private void initFileProfileList() {

        String[][] idAndLabel = null;
        int fileProfileCount = 0;
        String fileProfiles[] = null;
        if (m_strFileProfiles == null || m_strFileProfiles.length() == 0) {
            idAndLabel = new String[1][2];
        } else {
            fileProfiles = m_strFileProfiles.split(COMMA);
            fileProfileCount = fileProfiles.length;
            idAndLabel =  new String[fileProfileCount][2];
        }
			for (int i = 0; i < fileProfileCount; i++) {
				// file profile string looks like as : "1102=html file profile"
            String eachFileProfile[] = fileProfiles[i].split(EQUALS_SIGN);
            // becuse the file profile string should have two parts.
            if (eachFileProfile.length < 2) {
                break;
            }
				idAndLabel[i][0] = eachFileProfile[0].trim();
				idAndLabel[i][1] = eachFileProfile[1].trim();
			}
        
        updateDropdownListData(idAndLabel);
	}

    /**
     * Initialize dynamic check boxes to show target locales.
     */
    private void initDynamicCheckBox() {
		getAvailableLanguages();
		
        Datagrid checkboxGrid = (Datagrid) getControl(CTRL_DATAGRID_NAME,
                Datagrid.class);
		
		if (m_hashTargetLocales != null) {
			String[][] strTargetLocales = new String[m_hashTargetLocales.size()][2];
			for (int i = 0; i < m_hashTargetLocales.size(); ++i) {
				String strHashLocales = m_hashTargetLocales.toString();
                strHashLocales = strHashLocales.substring(1, strHashLocales
                        .length() - 1);
                String strTmp[] = strHashLocales.split(COMMA);
				for (int j = 0; j < strTmp.length; ++j) {
                    String strEachLanguag[] = strTmp[j].split(EQUALS_SIGN);
					strTargetLocales[j][0] = strEachLanguag[0].trim();
					strTargetLocales[j][1] = strEachLanguag[1].trim();
				}
			}
            TableResultSet tsr = new TableResultSet(strTargetLocales,
                    new String[] { LOCALE_SET_CODE, LOCALES });
			checkboxGrid.getDataProvider().setScrollableResultSet(tsr);
		}
	}

    /**
     * According to current object, get all related target locales.
     */
	private void getAvailableLanguages() {
        StringBuffer bufDql = new StringBuffer();
        StringBuffer bufDqlSub = new StringBuffer();
        AmbassadorConfiguration cfg = AmbassadorConfiguration.getInstance();
        String fieldCountryLocales = cfg
                .getPropertyValue(AmbassadorConfiguration.FIELD_COUNTRY_LOCALES);
        String tablePreselectLanguage = cfg
                .getPropertyValue(AmbassadorConfiguration.TABLE_PRESELECT_LANGUAGE);
        String fieldLocaleSet = cfg
                .getPropertyValue(AmbassadorConfiguration.FIELD_LOCALE_SET_CODE);
        String fieldCountryLanguage = cfg
                .getPropertyValue(AmbassadorConfiguration.FIELD_COUNTRY_LANGUANGE);
        String tableCountryList = cfg
                .getPropertyValue(AmbassadorConfiguration.TABLE_COUNTRY_LIST);
        String fieldLocale = cfg
                .getPropertyValue(AmbassadorConfiguration.FIELD_LOCALE);
        // select r_object_id, country_code from ref_translation_preselect where
        // code = '
        // SELECT R_OBJECT_ID, locales FROM REF_TRANSLATION_PRESELECT WHERE
        // locale_set_code ='
        bufDql.append(SQL_SELECT).append(SPACE);
        bufDql.append(Field_OBJECT_ID).append(COMMA).append(SPACE);
        bufDql.append(fieldCountryLocales);
        bufDql.append(SPACE);
        bufDql.append(SQL_FROM).append(SPACE);
        bufDql.append(tablePreselectLanguage);
        bufDql.append(SPACE);
        bufDql.append(SQL_WHERE).append(SPACE);
        bufDql.append(fieldLocaleSet);
        bufDql.append(SPACE);
        bufDql.append(EQUALS_SIGN).append(SPACE).append(SINGLE_QUOTES);
        bufDql.append(m_strAttrTargetLanguage);
        bufDql.append(SINGLE_QUOTES);
		IDfSessionManager sessionManager = SessionManagerHttpBinding
				.getSessionManager();
		IDfSession dfSession = null;
		IDfCollection coll = null;

		try {
			dfSession = sessionManager.getSession(m_strDocbaseName);
			IDfQuery query = DfcUtils.getClientX().getQuery();
			query.setDQL(bufDql.toString());
			coll = query.execute(dfSession, IDfQuery.READ_QUERY);

			if (coll.next() == true) {
                String strCountryCode = coll.getAllRepeatingStrings(
                        fieldCountryLocales, COMMA);
                String strCountries[] = strCountryCode.split(COMMA);
				coll.close();
				for (int i = 0; i < strCountries.length; ++i) {
					bufDql.delete(0, bufDql.length());
                    // select country_name from ref_country_list where code= '
                    // select country_language from ref_country_list where
                    // locale='
                    bufDql.append(SQL_SELECT).append(SPACE);
                    bufDql.append(fieldCountryLanguage);
                    bufDql.append(SPACE);
                    bufDql.append(SQL_FROM).append(SPACE);
                    bufDql.append(tableCountryList);
                    bufDql.append(SPACE);
                    bufDql.append(SQL_WHERE).append(SPACE);
                    bufDql.append(fieldLocale);
                    bufDql.append(SPACE);
                    bufDql.append(EQUALS_SIGN).append(SPACE).append(
                            SINGLE_QUOTES);
					bufDql.append(strCountries[i]);
                    bufDql.append(SINGLE_QUOTES);
					query.setDQL(bufDql.toString());
					coll = query.execute(dfSession, IDfQuery.READ_QUERY);
					if (coll.next()) {
                        String countryName = coll
                                .getString(fieldCountryLanguage);
						if (m_hashTargetLocales == null) {
							m_hashTargetLocales = new HashMap();
						}
						m_hashTargetLocales.put(strCountries[i], countryName);
					}
					coll.close();
					coll = null;
				}
			}
		} catch (DfException dfe) {

            throw new WrapperRuntimeException(ERR_MSG_FAILED_READ_QUERY, dfe);
		} finally {
			try {
				if (coll != null) {
					coll.close();
				}
			} catch (DfException dfe2) {
                throw new WrapperRuntimeException(
                        ERR_MSG_FAILED_CLOSE_COLLECTION, dfe2);
			} finally {
				if (dfSession != null) {
					sessionManager.release(dfSession);
				}
			}
		}
	}
    
    /**
     * Update current JSP state.
     */
    public void updatePageState(String strSelection) {
        HashSet localeSet = new HashSet();         
        
        // save state of checkboxes
        Control ctrlMultiSelect = getControl(CTRL_MULTI_SELECT_NAME,
                ActionMultiselect.class);
        if (strSelection != null && strSelection.length() != 0
                && ctrlMultiSelect != null
                && ctrlMultiSelect instanceof ActionMultiselect) {
            ActionMultiselect ctrlActionMultiselect = (ActionMultiselect) ctrlMultiSelect;
            ArgumentList ActionArgs[] = ctrlActionMultiselect
                    .getMultiselectItemArgs();
            for (int i = 0; i < ActionArgs.length; ++i) {
                char ch = strSelection.charAt(i);
                if (ch == CHECK_ON) {
                    ctrlActionMultiselect.selectMultiselectItem(ActionArgs[i]);
                    localeSet.add(ActionArgs[i].get(PARAM_TARGET_LOCALE));
                }
            }
        }
        
        // update file profile dropdown list.
        // Filter file profiles by object extension and selected target locales.
        // For example: hs.add("zh_CN");
        AmbassadorWebServiceClient ambWSC = 
            AmbassadorWebServiceClient.getInstance();

        List fileProfilesList = ambWSC.getFileProfileByFilter(
                  this.m_strObjectExtension, 
                  this.m_strObjectSourceLocale, 
                  localeSet);
        setFileProfileData(fileProfilesList);

        // save state of job name
        Control ctrlJobName = getControl(CTRL_TEXT_NAME, Text.class);
        if (ctrlJobName != null && ctrlJobName instanceof Text) {
            Text txtJobName = (Text) ctrlJobName;
            m_strJobName = txtJobName.getValue();
            txtJobName.setValue(m_strJobName);
        }

    }

    /**
     * 
     * @see com.documentum.web.formext.component.Component#onaction(
     *          com.documentum.web.formext.control.action.ActionControl, 
     *          com.documentum.web.common.ArgumentList)
     */
    public void onaction(ActionControl actionControl, ArgumentList args) {
        String strCurSelection = args.get(ActionMultiselect.ARGUMENT_SELECTION);

        updatePageState(strCurSelection);
    }
}