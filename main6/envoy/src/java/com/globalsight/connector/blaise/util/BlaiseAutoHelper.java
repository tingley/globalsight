package com.globalsight.connector.blaise.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.JsonUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import org.apache.log4j.Logger;

import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/2/9.
 */
public class BlaiseAutoHelper
{
    private static Logger logger = Logger.getLogger(BlaiseAutoHelper.class);
    private static boolean running = false;
    private static BlaiseAutoHelper autoHelper = new BlaiseAutoHelper();

    private BlaiseAutoHelper()
    {
    }

    public static BlaiseAutoHelper getInstance()
    {
        if (autoHelper == null)
            autoHelper = new BlaiseAutoHelper();
        return autoHelper;
    }

    public void runAutomatic(BlaiseConnector bc)
    {
        if (running || bc == null)
            return;

        BlaiseHelper helper = new BlaiseHelper(bc);
        running = true;
        helper.groupInboxEntries();
        running = false;
    }

    public String getJobAttributes(long fpId)
    {
        try
        {
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager().getFileProfileById(fpId, false);
            if (fp == null)
                return "";
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(fp.getL10nProfileId());
            Project project = lp.getProject();
            AttributeSet attributeSet = project.getAttributeSet();
            List<Attribute> attributes = attributeSet.getAttributeAsList();
            JSONObject object = new JSONObject();
            object.put("setName", attributeSet.getName());
            object.put("setId", attributeSet.getId());
            if (attributes != null && attributes.size()> 0)
            {
                JSONArray array = new JSONArray();
                JSONObject o = null;
                for (Attribute attribute : attributes) {
                    o.put("required", attribute.isRequired());
                    o.put("name", attribute.getName());
                    o.put("displayName", attribute.getDisplayName());
                    o.put("type", attribute.getType());
                    o.put("condition", attribute.getCondition());
                    Set<AttributeSet> values = attribute.getAttributeSets();
                    String tmp = "";
                    if (values != null) {
                        if ("choice list".equals(attribute.getType())) {
                        }
                    }
                }
            }
            String data =  JsonUtil.toJson(attributes);
            logger.info("json data == " + data);
            return data;
        } catch (Exception e) {
            logger.error("Error found when invoking getJobAttributes().", e);
            return "";
        }
    }

    public ArrayList<FileProfileImpl> getAllXliff12FileProfile(long companyId, String userId)
            throws RemoteException, NamingException
    {
        ArrayList<FileProfileImpl> fileProfileListOfUser = new ArrayList<FileProfileImpl>();
        List<String> extensionList = new ArrayList<String>();
        extensionList.add("xlf");
        extensionList.add("xliff");
        List<FileProfileImpl> fileProfileListOfCompany = (List) ServerProxy
                .getFileProfilePersistenceManager().getFileProfilesByExtension(
                        extensionList, Long.valueOf(companyId));
        if (fileProfileListOfCompany == null)
            return new ArrayList<>();

        SortUtil.sort(fileProfileListOfCompany, new Comparator<Object>()
        {
            public int compare(Object arg0, Object arg1)
            {
                FileProfileImpl a0 = (FileProfileImpl) arg0;
                FileProfileImpl a1 = (FileProfileImpl) arg1;
                return a0.getName().compareToIgnoreCase(a1.getName());
            }
        });

        User user = ServerProxy.getUserManager().getUser(userId);

        List projectsOfCurrentUser = ServerProxy.getProjectHandler()
                .getProjectsByUser(user.getUserId());

        for (FileProfileImpl fp : fileProfileListOfCompany)
        {
            Project fpProj = getProject(fp);
            // get the project and check if it is in the group of user's
            // projects.
            if (projectsOfCurrentUser.contains(fpProj)
                    && fp.getKnownFormatTypeId() == 39)// xliff 1.2
            {
                fileProfileListOfUser.add(fp);
            }
        }

        return fileProfileListOfUser;
    }

    private Project getProject(FileProfile p_fp)
    {
        Project p = null;
        try
        {
            long l10nProfileId = p_fp.getL10nProfileId();
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(
                    l10nProfileId);
            p = lp.getProject();
        }
        catch (Exception e)
        {
            logger.error(
                    "Failed to get the project that file profile "
                            + p_fp.toString() + " is associated with.", e);
        }
        return p;
    }

}
