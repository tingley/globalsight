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

package com.globalsight.restful.tm;

import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FORMAT;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FROM_WORLDSERVER;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.SID;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TRANSLATABLE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TYPE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.UPDATED_BY_PROJECT;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.sql.Connection;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.edit.offline.page.TmxUtil;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.importer.ImportOptions;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmManagerLocal;
import com.globalsight.everest.tm.exporter.ExportManager;
import com.globalsight.everest.tm.exporter.ExportUtil;
import com.globalsight.everest.tm.exporter.TmxWriter;
import com.globalsight.everest.tm.importer.ImportManager;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.everest.util.comparator.ProjectTMComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.tm.corpus.OverridableLeverageOptions;
import com.globalsight.exporter.IExportManager;
import com.globalsight.importer.IImportManager;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.AbstractTmTuv;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeveragedTuv;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.segmenttm.TMidTUid;
import com.globalsight.ling.tm3.core.BaseTm;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;
import com.globalsight.ling.tm3.integration.GSDataFactory;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.ling.tm3.integration.segmenttm.TM3Util;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.restful.RestResource;
import com.globalsight.restful.RestWebServiceException;
import com.globalsight.restful.RestWebServiceLog;
import com.globalsight.restful.RestWebServiceUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.Assert;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;
import com.globalsight.util.Replacer;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.webservices.WebServiceException;

@Path("/companies/{companyName}/tms")
public class TmResource extends RestResource
{
    public static final String GET_TMS = "getTms";

    public static final String UPLOAD_TMX_FILE = "uploadTmxFile";
    public static final String IMPORT_TMX_FILE = "importTmxFile";

    public static final String EXPORT_TM = "exportTM";
    public static final String GET_TM_EXPORT_FILE = "getTmExportFile";

    public static final String CREATE_TU = "createTu";
    public static final String GET_TU = "getTu";
    public static final String GET_TUS = "getTus";
    public static final String EDIT_TUS = "editTus";
    public static final String DELETE_TUS = "deleteTus";

    public static final String FULL_TEXT_SEARCH = "fullTextSearch";
    public static final String LEVERAGE_SEGMENT = "leverageSegment";

    private static String ERROR_EXPORT_FILE_NAME = "You cannot have \\, /, :, ;, ,,.,*, ?,!,$,#,@,[,],{,},(,),^,+,=,~, |, \',\", &lt;, &gt;, % or &amp; in the Export File Name.";
    private static String ERROR_EXPORT_PROJECT_NAMES = "You cannot have \\, /, :, ;, .,*, ?,!,$,#,@,[,],{,},(,),^,+,=,~, |, \',\", &lt;, &gt;, % or &amp; in the Project Name.";

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Tuv content need to be encoded in JSON returning.
    private static Pattern xml2JsonPattern = Pattern.compile("<seg>(.*?)</seg>");

    private final static String ENTRY_XML = "\r\n\t<entry>"
            + "\r\n\t\t<tm id={0}>{1}</tm>"
            + "\r\n\t\t<percentage>{2}%</percentage>"
            + "\r\n\t\t<sid>{3}</sid>"
            + "\r\n\t\t<source>"
            + "\r\n\t\t\t<locale>{4}</locale>"
            + "\r\n\t\t\t<segment>{5}</segment>"
            + "\r\n\t\t</source>"
            + "\r\n\t\t<target>"
            + "\r\n\t\t\t<locale>{6}</locale>"
            + "\r\n\t\t\t<segment>{7}</segment>"
            + "\r\n\t\t</target>"
            + "\r\n\t</entry>";

    private final static String NULL_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "\r\n<entries>\r\n\t<entry>\r\n\t\t"
            + "<percentage>0%</percentage>\r\n\t</entry>\r\n</entries>";

    private static final Logger logger = Logger.getLogger(TmResource.class);

    /**
     * Get all project TM information.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name. Required.
     * 
     * @return All TM data in JSON.
     * 
     * @throws RestWebServiceException
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTms(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromRequest(authorization);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restStart = RestWebServiceLog.start(TmResource.class, GET_TMS, restArgs);

            checkPermission(userName, Permission.TM_VIEW);

            List<GetTmsResponse> tmsResponse = new ArrayList<GetTmsResponse>();
            Company company = ServerProxy.getJobHandler().getCompany(p_companyName);
            long companyId = company.getId();
            List<ProjectTM> tms = getTMs(userName, company);
            for (ProjectTM tm : tms)
            {
                if (!tm.isActive() || tm.getCompanyId() != companyId)
                    continue;

                GetTmsResponse res = new GetTmsResponse();
                res.setId(tm.getId());
                res.setName(tm.getName());
                res.setDescription(tm.getDescription());
                res.setDomain(tm.getDomain());
                res.setOrganization(tm.getOrganization());
                res.setCompanyId(tm.getCompanyId());
                res.setCreationUser(tm.getCreationUser());
                tmsResponse.add(res);
            }
            return Response.status(200).entity(tmsResponse).build();
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(GET_TMS, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
    }

    // From "TmMainHandler::List<ProjectTM> getTMs(String userId, String cond)"
    @SuppressWarnings("rawtypes")
    private List<ProjectTM> getTMs(String userName, Company currentCompany) throws Exception
    {
        User user = ServerProxy.getUserManager().getUserByName(userName);
        String userId = user.getUserId();
        long currentCompanyId = currentCompany.getId();
        boolean enableTMAccessControl = currentCompany.getEnableTMAccessControl();

        boolean isSuperPM = UserUtil.isSuperPM(userId);
        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperLP = UserUtil.isSuperLP(userId);

        ProjectHandler projectHandler;
        Collection<ProjectTM> allTMs = null;
        Set<Long> tmsIds = new HashSet<Long>();
        try
        {
            projectHandler = ServerProxy.getProjectHandler();
            allTMs = projectHandler.getAllProjectTMs();
            for (ProjectTM ptm : allTMs)
            {
                tmsIds.add(ptm.getIdAsLong());
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }

        List<ProjectTM> tms = new ArrayList<ProjectTM>();
        if (1 == currentCompanyId)
        {
            if (isSuperLP)
            {
                Set<Long> companyIds = new HashSet<Long>();
                // Get all the companies the super translator worked for
                List<Project> projectList = ServerProxy.getProjectHandler().getProjectsByUser(
                        userId);
                for (Project pj : projectList)
                {
                    companyIds.add(pj.getCompanyId());
                }
                for (ProjectTM tm : allTMs)
                {
                    if (companyIds.contains(tm.getCompanyId()))
                    {
                        tms.add(tm);
                    }
                }
            }
            else
            {
                // Super admin
                tms.addAll(allTMs);
            }
        }
        else
        {
            if (enableTMAccessControl && !isAdmin)
            {
                ProjectTMTBUsers projectTMTBUsers = new ProjectTMTBUsers();
                List tmIdList = projectTMTBUsers.getTList(userId, "TM");
                Iterator it = tmIdList.iterator();
                while (it.hasNext())
                {
                    ProjectTM tm = ServerProxy.getProjectHandler().getProjectTMById(
                            ((BigInteger) it.next()).longValue(), false);
                    if (isSuperPM)
                    {
                        if (tm.getCompanyId() == currentCompanyId
                                && tmsIds.contains(tm.getIdAsLong()))
                        {
                            tms.add(tm);
                        }
                    }
                    else
                    {
                        if (tmsIds.contains(tm.getIdAsLong()))
                            tms.add(tm);
                    }
                }
            }
            else
            {
                tms.addAll(allTMs);
            }
        }

        SortUtil.sort(tms, new ProjectTMComparator(Locale.getDefault()));
        return tms;
    }

    /**
     * Create a new TU.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/tus
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name. Required.
     * @param p_tmId
     *            -- TM ID to export. Required.
     * @param p_sourceLocale
     *            -- the source locale. Required.
     * @param p_sourceSegment
     *            -- the source string. Required.
     * @param p_targetLocale
     *            -- the target locale. Required.
     * @param p_targetSegment
     *            -- the target string. Required.
     * @param p_sid
     *            -- the SID. Optional.
     * @param p_escapeString
     *            -- Is convert all the escapable characters into their string
     *            (escaped) equivalents or not, available values: true|false.
     *            Optional.
     * 
     * @return A success message
     * 
     * @throws RestWebServiceException
     */
    @POST
    @Path("/{tmId}/tus")
    public Response createTu(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @PathParam("tmId") String p_tmId,
            @QueryParam("sourceLocale") String p_sourceLocale,
            @QueryParam("sourceSegment") String p_sourceSegment,
            @QueryParam("targetLocale") String p_targetLocale,
            @QueryParam("targetSegment") String p_targetSegment,
            @QueryParam("sid") String p_sid,
            @QueryParam("escapeString") String p_escapeString) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromRequest(authorization);
            String escapeString = checkEscapeString(p_escapeString);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("tmId", p_tmId);
            restArgs.put("sid", p_sid);
            restArgs.put("sourceLocale", p_sourceLocale);
            restArgs.put("sourceSegment", p_sourceSegment);
            restArgs.put("targetLocale", p_targetLocale);
            restArgs.put("targetSegment", p_targetSegment);
            restArgs.put("escapeString", escapeString);
            restStart = RestWebServiceLog.start(TmResource.class, CREATE_TU, restArgs);

            checkPermission(userName, Permission.TM_ADD_ENTRY);

            ProjectTM tm = checkTmId(p_tmId, p_companyName);

            String sid = clearSid(p_sid);

            boolean escape = Boolean.parseBoolean(escapeString);
            if (StringUtil.isEmpty(p_sourceSegment))
                throw new RestWebServiceException("Empty source segment");
            if (StringUtil.isEmpty(p_targetSegment))
                throw new RestWebServiceException("Empty target segment");
            p_sourceSegment = wrapSegment(p_sourceSegment, escape);
            p_targetSegment = wrapSegment(p_targetSegment, escape);
            if (!escape)
            {
                p_sourceSegment = repairSegment(p_sourceSegment);
                p_targetSegment = repairSegment(p_targetSegment);
            }

            SegmentTmTu tu = new SegmentTmTu();
            tu.setTranslatable();
            tu.setFormat("plaintext");
            tu.setType("text");

            SegmentTmTuv sourceTuv = new SegmentTmTuv();
            SegmentTmTuv targetTuv = new SegmentTmTuv();
            sourceTuv.setTu(tu);
            targetTuv.setTu(tu);

            GlobalSightLocale sourceLocale = getLocaleByName(p_sourceLocale);
            GlobalSightLocale targetLocale = getLocaleByName(p_targetLocale);
            tu.setSourceLocale(sourceLocale);
            sourceTuv.setLocale(sourceLocale);
            targetTuv.setLocale(targetLocale);

            sourceTuv.setCreationUser(Tmx.DEFAULT_USER);
            sourceTuv.setCreationDate(new java.sql.Timestamp(new Date().getTime()));
            sourceTuv.setSegment(p_sourceSegment);
            sourceTuv.setSid(sid);

            targetTuv.setCreationUser(Tmx.DEFAULT_USER);
            targetTuv.setCreationDate(new java.sql.Timestamp(new Date().getTime()));
            targetTuv.setSegment(p_targetSegment);
            targetTuv.setSid(sid);

            tu.addTuv(sourceTuv);
            tu.addTuv(targetTuv);

            List<SegmentTmTu> tus = new ArrayList<SegmentTmTu>();
            tus.add(tu);
            LingServerProxy.getTmCoreManager().saveToSegmentTm(tm, tus, TmCoreManager.SYNC_MERGE,
                    null);
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(CREATE_TU, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return Response.status(200).entity("success").build();
    }

    // "createTu" parameter check
    private String checkEscapeString(String p_escapeString)
    {
        if (StringUtil.isEmpty(p_escapeString))
            return "false";

        p_escapeString = p_escapeString.trim().toLowerCase();
        if (!"true".equals(p_escapeString) && !"false".equals(p_escapeString))
        {
            return "false";
        }

        return p_escapeString;
    }

    private String clearSid(String sid)
    {
        if (sid != null)
        {
            sid = sid.trim();
            if (sid.length() == 0 || "null".equalsIgnoreCase(sid))
            {
                sid = null;
            }
        }

        return sid;
    }

    /**
     * Wraps segment with <code>segment</code> tag.
     * <p>
     * At first, convert all the escapable characters in the given string into
     * their string (escaped) equivalents if escapeString is set to true.
     * Then add <code>"<segment>"</code> to the first and
     * <code>"</segment>"</code> to the end.
     * 
     * @param segment
     * @param escapeString
     * @return
     */
    private String wrapSegment(String segment, boolean escapeString)
    {
        if (segment == null)
        {
            segment = "";
        }

        if (segment.startsWith("<segment") && segment.endsWith("</segment>"))
        {
            segment = GxmlUtil.stripRootTag(segment);
        }
        if (escapeString)
        {
            segment = com.globalsight.diplomat.util.XmlUtil.escapeString(segment);
        }

        return "<segment>" + segment + "</segment>";
    }

    /**
     * Try to repair the segment.
     * <p>
     * Will throw out a WebServiceException if the format is wrong and can not
     * be repaired.
     * <p>
     * 
     * @see SegmentHandler
     * @see #validateSegment(Element, IntHolder)
     * 
     * @param s The segment to be repaired
     * @return The repaired segment
     * 
     * @throws WebServiceException
     */
    private String repairSegment(String s) throws RestWebServiceException
    {
        Assert.assertNotEmpty(s, "segment");
        SAXReader reader = new SAXReader();
        SegmentHandler segmentHandler = new SegmentHandler(s);
        reader.addHandler("/segment", segmentHandler);
        try
        {
            reader.read(new StringReader(s));
            if (segmentHandler.hasError())
            {
                throw new RestWebServiceException(segmentHandler.getError());
            }
            return segmentHandler.getSegment();
        }
        catch (DocumentException e)
        {
            logger.error(e.getMessage(), e);
            throw new RestWebServiceException(e.getMessage());
        }
    }

    /**
     * Parses a segment string, and try to repair it.
     * <p>
     * Remember to check out the error is not null.
     */
    private class SegmentHandler implements ElementHandler
    {
        private String segment = null;
        private String error = null;

        public SegmentHandler(String segment)
        {
            this.segment = segment;
        }

        public String getSegment()
        {
            return segment;
        }

        public boolean hasError()
        {
            return error != null;
        }

        public String getError()
        {
            return error;
        }

        @Override
        public void onEnd(ElementPath path)
        {
            Element element = path.getCurrent();
            element.detach();
            try
            {
                validateSegment(element, new IntHolder(1));
                this.segment = "<segment>" + ImportUtil.getInnerXml(element)
                        + "</segment>";
            }
            catch (Exception e)
            {
                error = e.getMessage();
            }
        }

        @Override
        public void onStart(ElementPath path)
        {

        }
    }

    /**
     * Validates the segment, and try to repair it if the format is wrong.
     * 
     * <p>
     * Will throw out a exception if the format is wrong and can not be
     * repaired.
     * 
     * @param p_seg
     *            The segment string to validate
     * @param p_x_count
     *            The value of x
     * @return Repaired segment
     * 
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private Element validateSegment(Element p_seg, IntHolder p_x_count) throws Exception
    {
        String attr;
        List elems = p_seg.elements();
        for (Iterator it = elems.iterator(); it.hasNext();)
        {
            Element elem = (Element) it.next();
            String name = elem.getName();

            if (name.equals("bpt"))
            {
                attr = elem.attributeValue("x"); // mandatory only in 1.4
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("x", String.valueOf(p_x_count.inc()));
                }

                attr = elem.attributeValue("i"); // mandatory
                if (attr == null || attr.length() == 0)
                {
                    throw new Exception(
                            "A <bpt> tag is lacking the mandatory i attribute.");
                }

                attr = elem.attributeValue("type");
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("type", "text");
                }
            }
            else if (name.equals("ept"))
            {
                attr = elem.attributeValue("i"); // mandatory
                if (attr == null || attr.length() == 0)
                {
                    throw new Exception(
                            "A <ept> tag is lacking the mandatory i attribute.");
                }
            }
            else if (name.equals("it"))
            {
                attr = elem.attributeValue("x"); // mandatory only in 1.4
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("x", String.valueOf(p_x_count.inc()));
                }

                attr = elem.attributeValue("pos"); // mandatory
                if (attr == null || attr.length() == 0)
                {
                    throw new Exception(
                            "A <it> tag is lacking the mandatory pos attribute.");
                }

                attr = elem.attributeValue("type");
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("type", "text");
                }
            }
            else if (name.equals("ph"))
            {
                attr = elem.attributeValue("x"); // mandatory only in 1.4
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("x", String.valueOf(p_x_count.inc()));
                }

                attr = elem.attributeValue("type");
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("type", "text");
                }

                // GXML doesn't care about assoc, just preserve it.
                // attr = elem.attributeValue("assoc");
            }
            else if (name.equals("ut"))
            {
                // TMX level 2 does not allow UT. We can either remove
                // it, or look inside and guess what it may be.
                it.remove();
                continue;
            }

            // Recurse into any subs.
            validateSubs(elem, p_x_count);
        }

        return p_seg;
    }

    /**
     * Validates the sub elements inside a TMX tag. This means adding a <sub
     * locType="..."> attribute.
     * 
     * @param p_elem
     * @param p_x_count
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private void validateSubs(Element p_elem, IntHolder p_x_count) throws Exception
    {
        List subs = p_elem.elements("sub");

        for (int i = 0, max = subs.size(); i < max; i++)
        {
            Element sub = (Element) subs.get(i);
            validateSegment(sub, p_x_count);
        }
    }

    /**
     * Get TU data by ID.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/tus/{id}
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name.
     * @param p_tmId
     *            -- TM ID to export. Required.
     * @param p_tuId
     *            -- the TU id to query. Required.
     * 
     * @return TU XML string
     * 
     * @throws RestWebServiceException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @GET
    @Path("/{tmId}/tus/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getTu(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @PathParam("tmId") String p_tmId,
            @PathParam("id") String p_tuId) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        String tuXml = "";
        try
        {
            String userName = getUserNameFromRequest(authorization);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("tmId", p_tmId);
            restArgs.put("id", p_tuId);
            restStart = RestWebServiceLog.start(TmResource.class, GET_TU, restArgs);

            ProjectTM ptm = checkTmId(p_tmId, p_companyName);
            BaseTm tm = TM3Util.getBaseTm(ptm.getTm3Id());
            TM3Tu tm3Tu = tm.getTu(Long.parseLong(p_tuId));
            if (tm3Tu == null)
            {
                return Response.status(200).entity("Unable to find TU by tuId: " + p_tuId).build();
            }

            if (tm3Tu.getTm().getId() != ptm.getTm3Id())
            {
                throw new RestWebServiceException("The tu Id " + p_tuId
                        + " does not belong to tm: " + p_tmId);
            }

            TM3Attribute typeAttr = TM3Util.getAttr(tm, TYPE);
            TM3Attribute formatAttr = TM3Util.getAttr(tm, FORMAT);
            TM3Attribute sidAttr = TM3Util.getAttr(tm, SID);
            TM3Attribute translatableAttr = TM3Util.getAttr(tm, TRANSLATABLE);
            TM3Attribute fromWsAttr = TM3Util.getAttr(tm, FROM_WORLDSERVER);
            TM3Attribute projectAttr = TM3Util.getAttr(tm, UPDATED_BY_PROJECT);

            SegmentTmTu segmentTmTu = TM3Util.toSegmentTmTu(tm3Tu, ptm.getId(), formatAttr,
                    typeAttr, sidAttr, fromWsAttr, translatableAttr, projectAttr);

            IExportManager exporter = null;
            String options = null;
            exporter = new ExportManager(ptm, new SessionInfo("", ""));
            options = exporter.getExportOptions();
            Document doc = DocumentHelper.parseText(options);
            Element rootElt = doc.getRootElement();
            Iterator fileIter = rootElt.elementIterator("fileOptions");
            while (fileIter.hasNext())
            {
                Element fileEle = (Element) fileIter.next();
                Element fileTypeElem = fileEle.element("fileType");
                fileTypeElem.setText("xml");
            }
            options = doc.asXML().substring(doc.asXML().indexOf("<exportOptions>"));
            exporter.setExportOptions(options);

            Tmx tmx = new Tmx();
            tmx.setSourceLang(Tmx.DEFAULT_SOURCELANG);
            tmx.setDatatype(Tmx.DATATYPE_HTML);

            TmxWriter tmxWriter = new TmxWriter(exporter.getExportOptionsObject(), ptm, tmx);
            tuXml = tmxWriter.getSegmentTmForXml(segmentTmTu);
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(GET_TU, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return Response.status(200).entity(tuXml).build();
    }

    /**
     * Get TU data by specified start ID and offset.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/tus?startId=1000&offset=xx&sourceLocale="en_us"
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name. Required.
     * @param p_tmId
     *            -- TM ID to export. Required.
     * @param p_startId
     *            -- the start TU id (excluded). Optional. Default start with
     *            the first id.
     * @param p_offset
     *            -- TU id offset. Optional. Default 1.
     * @param p_sourceLocale
     *            -- The source locale, like "EN_US"(case-insensitive).
     *            Required.
     * @param p_targetLocale
     *            -- The target locale, like "FR_FR"(case-insensitive).
     *            Optional.
     * 
     * @return TU(s) XML string
     * 
     * @throws RestWebServiceException
     */
    @GET
    @Path("/{tmId}/tus")
    @Produces(MediaType.APPLICATION_XML)
    public Response getTus(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @PathParam("tmId") String p_tmId,
            @QueryParam("startId") @DefaultValue("0") String p_startId,
            @QueryParam("offset") @DefaultValue("1") String p_offset,
            @QueryParam("sourceLocale") String p_sourceLocale,
            @QueryParam("targetLocale") String p_targetLocale) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        String tuXml = "";
        try
        {
            String userName = getUserNameFromRequest(authorization);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("tmId", p_tmId);
            restArgs.put("startId", p_startId);
            restArgs.put("offset", p_offset);
            restArgs.put("sourceLocale", p_sourceLocale);
            restArgs.put("targetLocale", p_targetLocale);
            restStart = RestWebServiceLog.start(TmResource.class, GET_TUS, restArgs);

            checkPermission(userName, Permission.TM_SEARCH);

            ProjectTM tm = checkTmId(p_tmId, p_companyName);

            long startId = checkStartId(p_startId);
            int offset = checkOffset(p_offset);

            if (StringUtil.isEmpty(p_sourceLocale))
            {
                throw new RestWebServiceException("Empty source locale");
            }
            GlobalSightLocale srcGSLocale = getLocaleByName(p_sourceLocale);
            GlobalSightLocale trgGSLocale = null;
            if (p_targetLocale != null && p_targetLocale.length() > 0)
            {
                trgGSLocale = getLocaleByName(p_targetLocale);
            }

            tuXml = nextTm3Tus(tm, srcGSLocale, trgGSLocale, startId, offset);
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(GET_TUS, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return Response.status(200).entity(tuXml).build();
    }

    private long checkStartId(String p_startId) throws RestWebServiceException
    {
        long startId = 0;
        if (StringUtil.isNotEmpty(p_startId))
        {
            try
            {
                startId = Long.parseLong(p_startId);
            }
            catch (NumberFormatException e)
            {
                throw new RestWebServiceException("Invalid startId: " + p_startId);
            }
        }
        return startId;
    }

    private int checkOffset(String p_offset) throws RestWebServiceException
    {
        int offset = 1;
        if (StringUtil.isNotEmpty(p_offset))
        {
            try
            {
                offset = Integer.parseInt(p_offset);
            }
            catch (NumberFormatException e)
            {
                throw new RestWebServiceException("Invalid offset: " + p_offset);
            }
        }

        return offset;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String nextTm3Tus(ProjectTM ptm, GlobalSightLocale srcGSLocale,
            GlobalSightLocale trgGSLocale, long startId, int offset)
            throws RestWebServiceException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();

            String tuTable = "tm3_tu_shared_" + ptm.getCompanyId();
            String tuvTable = "tm3_tuv_shared_" + ptm.getCompanyId();

            StatementBuilder sb = new StatementBuilder();
            if (trgGSLocale != null)
            {
                sb.append("SELECT distinct tuv.tuId FROM ").append(tuvTable).append(" tuv,");
                sb.append(" (SELECT id FROM ").append(tuTable).append(" tu ")
                        .append("WHERE tu.tmid = ? ").addValue(ptm.getTm3Id())
                        .append(" AND tu.srcLocaleId = ? ").addValue(srcGSLocale.getId())
                        .append(" AND tu.id > ? ").addValue(startId)
                        .append(" ORDER BY tu.id LIMIT 0, ")
                        .append(String.valueOf(10 * offset)).append(") tuids ");
                sb.append(" WHERE tuv.tuId = tuids.id");
                sb.append(" AND tuv.localeId = ? ").addValue(trgGSLocale.getId());
                sb.append(" AND tuv.tmId = ? ").addValue(ptm.getTm3Id());
                sb.append(" ORDER BY tuv.tuId ");
                sb.append(" LIMIT 0, ").append(String.valueOf(offset)).append(";");
            }
            else
            {
                sb.append("SELECT id FROM ").append(tuTable)
                        .append(" WHERE tmid = ? ").addValue(ptm.getTm3Id())
                        .append(" AND srcLocaleId = ? ").addValue(srcGSLocale.getId())
                        .append(" AND id > ? ").addValue(startId)
                        .append(" ORDER BY id ")
                        .append("LIMIT 0,").append(offset + ";");
            }
            List<Long> tuIds = SQLUtil.execIdsQuery(conn, sb);
            if (tuIds == null || tuIds.size() == 0)
            {
                return null;
            }

            BaseTm tm = TM3Util.getBaseTm(ptm.getTm3Id());
            List<TM3Tu> tm3Tus = tm.getTu(tuIds);

            TM3Attribute typeAttr = TM3Util.getAttr(tm, TYPE);
            TM3Attribute formatAttr = TM3Util.getAttr(tm, FORMAT);
            TM3Attribute sidAttr = TM3Util.getAttr(tm, SID);
            TM3Attribute translatableAttr = TM3Util.getAttr(tm, TRANSLATABLE);
            TM3Attribute fromWsAttr = TM3Util.getAttr(tm, FROM_WORLDSERVER);
            TM3Attribute projectAttr = TM3Util.getAttr(tm, UPDATED_BY_PROJECT);
            List<SegmentTmTu> segTmTus = new ArrayList<SegmentTmTu>();
            for (TM3Tu tm3Tu : tm3Tus)
            {
                segTmTus.add(TM3Util.toSegmentTmTu(tm3Tu, ptm.getId(), formatAttr, typeAttr,
                        sidAttr, fromWsAttr, translatableAttr, projectAttr));
            }

            List<GlobalSightLocale> targetLocales = null;
            if (trgGSLocale != null)
            {
                targetLocales = new ArrayList<GlobalSightLocale>();
                targetLocales.add(trgGSLocale);
            }

            StringBuffer result = new StringBuffer();
            IExportManager exporter = null;
            String options = null;
            exporter = TmManagerLocal.getProjectTmExporter(ptm.getName());
            options = exporter.getExportOptions();
            Document doc = DocumentHelper.parseText(options);
            Element rootElt = doc.getRootElement();
            Iterator fileIter = rootElt.elementIterator("fileOptions");
            while (fileIter.hasNext())
            {
                Element fileEle = (Element) fileIter.next();
                Element fileTypeElem = fileEle.element("fileType");
                fileTypeElem.setText("xml");
            }

            Iterator filterIter = rootElt.elementIterator("filterOptions");
            while (filterIter.hasNext())
            {
                Element filterEle = (Element) filterIter.next();
                Element language = filterEle.element("language");
                if (trgGSLocale != null)
                {
                    language.setText(trgGSLocale.getLanguage() + "_" + trgGSLocale.getCountry());
                }
            }

            options = doc.asXML().substring(doc.asXML().indexOf("<exportOptions>"));
            exporter.setExportOptions(options);

            Tmx tmx = new Tmx();
            tmx.setSourceLang(Tmx.DEFAULT_SOURCELANG);
            tmx.setDatatype(Tmx.DATATYPE_HTML);

            TmxWriter tmxWriter = new TmxWriter(exporter.getExportOptionsObject(), ptm, tmx);
            for (SegmentTmTu segTmTu : segTmTus)
            {
                result.append(tmxWriter.getSegmentTmForXml(segTmTu));
            }

            return result.toString();
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new RestWebServiceException(e.getMessage());
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    // "getTus"
    @SuppressWarnings("unused")
    private String xml2JsonString(String tuXml)
    {
        XmlEntities xe = new XmlEntities();

        String newTuXml = StringUtil.replaceWithRE(tuXml, xml2JsonPattern, new Replacer()
        {
            @Override
            public String getReplaceString(Matcher m)
            {
                String segContent = m.group(1);
                // Encode TUV content for JSON returning
                String newSegContent = xe.encodeStringBasic(segContent);
                return "<seg>" + newSegContent + "</seg>";
            }
        });

        String tuJson = XmlUtil.xml2JsonString(newTuXml);

        return tuJson;
    }

    /**
     * Edit TUs by TU XML.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/tus
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name. Required.
     * @param p_tmId
     *            -- TM ID to export. Required.
     * 
     * @return A success message
     * 
     * @throws RestWebServiceException
     */
    @PUT
    @Path("/{tmId}/tus")
    @Consumes(MediaType.APPLICATION_XML)
    public Response editTus(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @PathParam("tmId") String p_tmId,
            InputStream inputStream) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromRequest(authorization);
            String tuXml = IOUtils.toString(inputStream, "UTF-8");

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("tmId", p_tmId);
            restArgs.put("tmx", tuXml);
            restStart = RestWebServiceLog.start(TmResource.class, EDIT_TUS, restArgs);

            checkPermission(userName, Permission.TM_EDIT_ENTRY);

            ProjectTM tm = checkTmId(p_tmId, p_companyName);

            SAXReader reader = new SAXReader();
            ElementHandler handler = new ElementHandler()
            {
                public void onStart(ElementPath path)
                {
                }

                public void onEnd(ElementPath path)
                {
                    Element element = path.getCurrent();
                    element.detach();

                    try
                    {
                        normalizeTu(element);
                        validateTu(element);
                        editTm3Tu(element, tm);
                    }
                    catch (Throwable ex)
                    {
                        logger.error(ex);
                        throw new ThreadDeath();
                    }
                }
            };
            reader.addHandler("/tu", handler);
            reader.read(new StringReader(tuXml));
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(EDIT_TUS, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return Response.status(200).entity("success").build();
    }

    /**
     * Normalizes the spelling of the "lang" elements.
     * 
     * @param p_tu
     *            Element
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private void normalizeTu(Element p_tu) throws Exception
    {
        // Header default source lang normalized when header is read.
        // Locales read from m_options were normalized by TmxReader.
        String lang = p_tu.attributeValue(Tmx.SRCLANG);
        if (lang != null)
        {
            lang = ImportUtil.normalizeLocale(lang);
            p_tu.addAttribute(Tmx.SRCLANG, lang);
        }

        // can't use xpath here because xml:lang won't be matched
        List nodes = p_tu.selectNodes("./tuv");
        for (int i = 0, max = nodes.size(); i < max; i++)
        {
            Element elem = (Element) nodes.get(i);

            lang = elem.attributeValue(Tmx.LANG);
            lang = ImportUtil.normalizeLocale(lang);

            elem.addAttribute(Tmx.LANG, lang);
        }
    }

    /**
     * Validates a TU by checking it contains a TUV in a source language that
     * should be imported. Also checks if there are more than 2 TUVs.
     * 
     * @param p_tu
     *            Element
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private void validateTu(Element p_tu) throws Exception
    {
        boolean b_found = false;

        String tuvLang = null;
        String srcLang = p_tu.attributeValue(Tmx.SRCLANG);
        if (srcLang == null)
        {
            srcLang = "en_US";
        }

        // can't use xpath here because xml:lang won't be matched
        List nodes = p_tu.selectNodes("./tuv");
        if (nodes.size() < 2)
        {
            throw new Exception("TU contains less than 2 TUVs (after filtering), ignoring");
        }

        for (int i = 0; i < nodes.size(); i++)
        {
            Element elem = (Element) nodes.get(i);
            tuvLang = elem.attributeValue(Tmx.LANG);
            if (tuvLang.equalsIgnoreCase(srcLang))
            {
                b_found = true;
                break;
            }
        }

        if (!b_found)
        {
            throw new Exception("TU is missing TUV in source language " + srcLang);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void editTm3Tu(Element p_root, ProjectTM ptm) throws Exception
    {
        String tuId = p_root.attributeValue(Tmx.TUID);
        BaseTm tm = TM3Util.getBaseTm(ptm.getTm3Id());
        TM3Tu tm3Tu = tm.getTu(Long.parseLong(tuId));

        TM3Attribute typeAttr = TM3Util.getAttr(tm, TYPE);
        TM3Attribute formatAttr = TM3Util.getAttr(tm, FORMAT);
        TM3Attribute sidAttr = TM3Util.getAttr(tm, SID);
        TM3Attribute translatableAttr = TM3Util.getAttr(tm, TRANSLATABLE);
        TM3Attribute fromWsAttr = TM3Util.getAttr(tm, FROM_WORLDSERVER);
        TM3Attribute projectAttr = TM3Util.getAttr(tm, UPDATED_BY_PROJECT);
        SegmentTmTu tu = TM3Util.toSegmentTmTu(tm3Tu, ptm.getId(), formatAttr, typeAttr, sidAttr,
                fromWsAttr, translatableAttr, projectAttr);

        // Datatype of the TU (html, javascript etc)
        String format = p_root.attributeValue(Tmx.DATATYPE);
        if (format == null || format.length() == 0)
        {
            format = "html";
        }
        tu.setFormat(format.trim());
        // Locale of Source TUV (use default from header)
        String lang = p_root.attributeValue(Tmx.SRCLANG);
        if (lang == null || lang.length() == 0)
        {
            lang = "en_US";
        }
        String locale = ImportUtil.normalizeLocale(lang);
        LocaleManagerLocal manager = new LocaleManagerLocal();
        tu.setSourceLocale(manager.getLocaleByString(locale));
        // Segment type (text, css-color, etc)
        String segmentType = "text";
        Node node = p_root.selectSingleNode(".//prop[@type = '" + Tmx.PROP_SEGMENTTYPE + "']");
        if (node != null)
        {
            segmentType = node.getText();
        }
        tu.setType(segmentType);
        // Sid
        String sid = null;
        node = p_root.selectSingleNode(".//prop[@type= '" + Tmx.PROP_TM_UDA_SID + "']");
        if (node != null)
        {
            sid = node.getText();
            tu.setSID(sid);
        }
        // TUVs
        List nodes = p_root.elements("tuv");
        List<SegmentTmTuv> tuvsToBeUpdated = new ArrayList<SegmentTmTuv>();
        for (int i = 0; i < nodes.size(); i++)
        {
            Element elem = (Element) nodes.get(i);
            SegmentTmTuv tuv = new SegmentTmTuv();
            PageTmTu pageTmTu = new PageTmTu(-1, -1, "plaintext", "text", true);
            tuv.setTu(pageTmTu);
            tuv.setSid(sid);
            TmxUtil.convertFromTmx(elem, tuv);

            Collection splitSegments = tuv.prepareForSegmentTm();

            Iterator itSplit = splitSegments.iterator();
            while (itSplit.hasNext())
            {
                AbstractTmTuv.SegmentAttributes segAtt = (AbstractTmTuv.SegmentAttributes) itSplit
                        .next();
                String segmentString = segAtt.getText();
                tuv.setSegment(segmentString);
            }
            // Check the locale
            List<SegmentTmTuv> savedTuvs = new ArrayList<SegmentTmTuv>();
            for (BaseTmTuv savedTuv : tu.getTuvs())
            {
                if (savedTuv.getLocale().equals(tuv.getLocale()))
                {
                    savedTuvs.add((SegmentTmTuv) savedTuv);
                }
            }

            if (savedTuvs.size() > 1)
            {
                boolean find = false;
                for (SegmentTmTuv savedTuv : savedTuvs)
                {
                    if (savedTuv.getCreationDate().getTime() == tuv.getCreationDate().getTime())
                    {
                        find = true;
                        savedTuv.merge(tuv);
                        tuvsToBeUpdated.add(savedTuv);
                    }
                }
                if (!find)
                {
                    throw new RestWebServiceException("Can not find tuv with tu id: " + tu.getId()
                            + ", locale: " + tuv.getLocale() + ", creation date:"
                            + tuv.getCreationDate());
                }
            }
            else
            {
                SegmentTmTuv savedTuv = savedTuvs.get(0);
                savedTuv.merge(tuv);
                tuvsToBeUpdated.add(savedTuv);
            }
        }

        ptm.getSegmentTmInfo().updateSegmentTmTuvs(ptm, tuvsToBeUpdated);
    }

    /**
     * Delete TU by TU ids.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/tus/{ids}
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name. Required.
     * @param p_tmId
     *            -- TM ID to export. Required.
     * @param p_tuIds
     *            -- tu ids comma-separated, i.e "12,14,15" or "12,14-20,34". Required.
     * 
     * @return A success message
     * 
     * @throws RestWebServiceException
     */
    @DELETE
    @Path("/{tmId}/tus/{ids}")
    public Response deleteTus(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @PathParam("tmId") String p_tmId,
            @PathParam("ids") String p_tuIds) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromRequest(authorization);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("tmId", p_tmId);
            restArgs.put("tuIds", p_tuIds);
            restStart = RestWebServiceLog.start(TmResource.class, DELETE_TUS, restArgs);

            checkPermission(userName, Permission.TM_DELETE);

            ProjectTM ptm = checkTmId(p_tmId, p_companyName);

            List<Long> tuIdList = checkTuIds(p_tuIds);

            TM3Tm<GSTuvData> tm3tm = (new Tm3SegmentTmInfo()).getTM3Tm(ptm.getTm3Id());
            List<TM3Tu<GSTuvData>> tus = tm3tm.getTu(tuIdList);
            List<SegmentTmTu> resultList = new ArrayList<SegmentTmTu>();
            TM3Attribute typeAttr = TM3Util.getAttr(tm3tm, TYPE);
            TM3Attribute formatAttr = TM3Util.getAttr(tm3tm, FORMAT);
            TM3Attribute sidAttr = TM3Util.getAttr(tm3tm, SID);
            TM3Attribute translatableAttr = TM3Util.getAttr(tm3tm, TRANSLATABLE);
            TM3Attribute fromWsAttr = TM3Util.getAttr(tm3tm, FROM_WORLDSERVER);
            TM3Attribute projectAttr = TM3Util.getAttr(tm3tm, UPDATED_BY_PROJECT);
            for (TM3Tu<GSTuvData> tm3tu : tus)
            {
                if (tm3tu.getTm().getId() == ptm.getTm3Id())
                {
                    SegmentTmTu segmentTmTu = TM3Util.toSegmentTmTu(tm3tu, ptm.getId(), formatAttr,
                            typeAttr, sidAttr, fromWsAttr, translatableAttr, projectAttr);
                    resultList.add(segmentTmTu);
                }
//                else
//                {
//                    throw new RestWebServiceException("Tu id (" + tm3tu.getId()
//                            + ") does not belong to current tm.");
//                }
            }
            if (resultList.size() > 0)
            {
                TmCoreManager manager = LingServerProxy.getTmCoreManager();
                manager.deleteSegmentTmTus(ptm, resultList, false);
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(DELETE_TUS, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return Response.status(200).entity("Tus are removed successfully.").build();
    }

    // "deleteTuByTuIds" parameter check
    private List<Long> checkTuIds(String p_tuIds) throws RestWebServiceException
    {
        if (StringUtil.isEmpty(p_tuIds))
        {
            throw new RestWebServiceException("Empty tu Ids");
        }

        // sample "12,14,15" or "12,14-20,34"
        Set<Long> tuIdSet = new HashSet<Long>();
        for (String tuId : p_tuIds.split(","))
        {
            if (StringUtil.isEmpty(tuId))
            {
                continue;
            }

            try
            {
                String[] ids = tuId.split("-");
                if (ids.length == 1)
                {
                    tuIdSet.add(Long.parseLong(ids[0]));
                }
                else if (ids.length == 2)
                {
                    long start = Long.parseLong(ids[0]);
                    long end = Long.parseLong(ids[1]);
                    if (start <= end)
                    {
                        for (long i = start; i <= end; i++)
                        {
                            tuIdSet.add(i);
                        }
                    }
                    else
                    {
                        throw new RestWebServiceException("Invaild tu Id(s): " + p_tuIds);
                    }
                }
                else
                {
                    throw new RestWebServiceException("Invaild tu Id(s): " + p_tuIds);
                }
            }
            catch (NumberFormatException e)
            {
                throw new RestWebServiceException("Invaild tu Id(s): " + p_tuIds);
            }
        }

        List<Long> result = new ArrayList<Long>();
        result.addAll(tuIdSet);
        return result;
    }

    /**
     * Upload TM data file. After upload is finished, use "import" request to
     * import the TM data file into TM. If a file is too large, API allows to
     * upload multiple times.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/upload
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name.
     * @param p_tmId
     *            -- TM ID to export. Required.
     * 
     * @return A success message
     * 
     * @throws RestWebServiceException
     */
    @POST
    @Path("/{tmId}/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadTmxFile(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @PathParam("tmId") String p_tmId,
            MultipartInput p_input) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromRequest(authorization);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("tmId", p_tmId);
            restStart = RestWebServiceLog.start(TmResource.class, UPLOAD_TMX_FILE, restArgs);

            checkPermission(userName, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

            ProjectTM tm = checkTmId(p_tmId, p_companyName);

            StringBuffer pathBuffer = new StringBuffer();
            pathBuffer.append(AmbFileStoragePathUtils.getFileStorageDirPath(tm.getCompanyId()));
            pathBuffer.append(File.separator).append(WebAppConstants.VIRTUALDIR_TOPLEVEL);
            pathBuffer.append(File.separator).append("TmImport");
            pathBuffer.append(File.separator).append(tm.getName());
            pathBuffer.append(File.separator).append("tmp");
            String tmpDir = pathBuffer.toString();

            List<InputPart> inputParts = p_input.getParts();
            for (InputPart inputPart : inputParts)
            {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                String contentDisposition = header.getFirst("Content-Disposition");
                String fileName = getFileNameFromHeaderInfo(contentDisposition);
                if (fileName != null)
                {
                    // convert the uploaded file content to InputStream
                    InputStream inputStream = inputPart.getBody(InputStream.class, null);

                    byte[] bytes = IOUtils.toByteArray(inputStream);

                    File tmxFile = new File(tmpDir, fileName);
                    RestWebServiceUtil.writeFile(tmxFile, bytes);
                }
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(UPLOAD_TMX_FILE, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return Response.status(200).entity("File is uploaded sucessfully.").build();
    }

    // Get file name from content-disposition header info. A sample info is like:
    // "form-data; name="attachment"; filename="tm_100_InContext_Fuzzy_NoMatch.xml"
    private String getFileNameFromHeaderInfo(String contentDisposition)
    {
        if (StringUtil.isEmpty(contentDisposition))
            return null;

        String fileName = null;
        String[] strs = contentDisposition.split(";");
        for (String str : strs)
        {
            if (str != null && str.trim().startsWith("filename="))
            {
                str = str.trim();
                str = str.substring("filename=".length());
                if (str.startsWith("\""))
                {
                    str = str.substring(1);
                }
                if (str.endsWith("\""))
                {
                    str = str.substring(0, str.length() - 1);
                }
                fileName = str;
                break;
            }
        }

        return fileName;
    }

    /**
     * Import TM data file into TM. The TM data file is uploaded via "upload" requests.
     * 
     * Sample URL:
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/import
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name. Required.
     * @param p_tmId
     *            -- TM ID to export. Required.
     * @param p_syncMode
     *            -- option to import data into TM: {merge, overwrite, discard}.
     *            Default "merge". Optional
     * 
     * @return A success message
     * 
     * @throws RestWebServiceException
     */
    @POST
    @Path("/{tmId}/import")
    public Response importTmxFile(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @PathParam("tmId") String p_tmId,
            @QueryParam("syncMode") String p_syncMode) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromRequest(authorization);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("tmId", p_tmId);
            restArgs.put("syncMode", p_syncMode);
            restStart = RestWebServiceLog.start(TmResource.class, IMPORT_TMX_FILE, restArgs);

            checkPermission(userName, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);

            ProjectTM tm = checkTmId(p_tmId, p_companyName);

            com.globalsight.everest.tm.importer.ImportOptions tmImportOptions = initTmImportOptions(p_syncMode);
            IImportManager importer = new ImportManager(tm, new SessionInfo("", ""));

            StringBuffer pathBuffer = new StringBuffer();
            pathBuffer.append(AmbFileStoragePathUtils.getFileStorageDirPath(tm.getCompanyId()));
            pathBuffer.append(File.separator).append(WebAppConstants.VIRTUALDIR_TOPLEVEL);
            pathBuffer.append(File.separator).append("TmImport");
            pathBuffer.append(File.separator).append(tm.getName());
            String tmNameDir = pathBuffer.toString();

            pathBuffer.append(File.separator).append("tmp");
            String tmpDir = pathBuffer.toString();
            tmpDir = tmpDir.replace("\\", "/");

            File tmxFileDir = new File(tmpDir);
            if (tmxFileDir.exists() && tmxFileDir.isDirectory())
            {
                File[] tmxFiles = tmxFileDir.listFiles();
                if (tmxFiles != null && tmxFiles.length > 0)
                {
                    for (int i = 0; i < tmxFiles.length; i++)
                    {
                        File tmxFile = tmxFiles[i];
                        File savedFile = new File(tmNameDir, tmxFile.getName());
                        ImportUtil.createInstance().saveTmFileWithValidation(tmxFile, savedFile);

                        importer.setImportOptions(tmImportOptions.getXml());
                        importer.setImportFile(savedFile.getAbsolutePath(), false);
                        String options = importer.analyzeFile();

                        importer.setImportOptions(options);
                        importer.doImport();

                        // delete tmp TMX files to avoid re-import.
                        tmxFile.delete();
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(IMPORT_TMX_FILE, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return Response.status(200).entity("success").build();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private com.globalsight.everest.tm.importer.ImportOptions initTmImportOptions(String p_syncMode)
    {
        com.globalsight.everest.tm.importer.ImportOptions tmImportOptions = new com.globalsight.everest.tm.importer.ImportOptions();

        // syncMode : default "merge"
        tmImportOptions.setSyncMode(ImportOptions.SYNC_MERGE);
        if (ImportOptions.SYNC_MERGE.equalsIgnoreCase(p_syncMode)
                || ImportOptions.SYNC_OVERWRITE.equalsIgnoreCase(p_syncMode)
                || ImportOptions.SYNC_DISCARD.equalsIgnoreCase(p_syncMode))
        {
            tmImportOptions.setSyncMode(p_syncMode.toLowerCase());
        }

        // default: all -- all
        tmImportOptions.setSelectedSource("all");
        Collection selectedTargets = new ArrayList();
        selectedTargets.add("all");
        tmImportOptions.setSelectedTargets(selectedTargets);

        return tmImportOptions;
    }

    /**
     * Export TM data. This will trigger a TM exporting event and return an
     * identify key. Then user can use another API "getTmExportStatus" with this
     * identify key to check the export status.
     * 
     * Sample URL:
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/export?startDate=yyyyMMdd&exportFormat=TMX1.4b
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name. Required.
     * @param p_tmId
     *            -- TM ID to export. Required.
     * @param p_languages
     *            -- languages to export like "de_DE,fr_FR" or "fr_FR".
     *            Optional. If be empty, export all.
     * @param p_startDate
     *            -- start time in "yyyyMMdd" format, TM data within current day
     *            will be included. Required.
     * @param p_finishDate
     *            -- finish time in "yyyyMMdd" format, TM data within current
     *            day will be included. Optional. If be empty, apply current
     *            time.
     * @param p_projectNames
     *            -- project names to export like
     *            "project_name_01, project_name_02" or "project_name_01".
     *            Optional.
     * @param p_exportFormat
     *            -- export file formats: "GMX" or "TMX1.4b". Required.
     * @param p_exportedFileName
     *            -- exported file name. Optional. If be empty, use GlobalSight
     *            default name like "tm_export_n.tmx" or "tm_export_n.xml".
     * 
     * @return an identify key in string which is used to check TM export status
     *         and retrieve TM data.
     * 
     * @throws RestWebServiceException
     */
    @GET
    @Path("/{tmId}/export")
    public Response exportTM(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @PathParam("tmId") String p_tmId,
            @QueryParam("languages") String p_languages,
            @QueryParam("startDate") String p_startDate,
            @QueryParam("finishDate") String p_finishDate,
            @QueryParam("projectNames") String p_projectNames,
            @QueryParam("exportFormat") String p_exportFormat,
            @QueryParam("exportedFileName") String p_exportedFileName)
            throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        String identifyKey = null;
        try
        {
            String userName = getUserNameFromRequest(authorization);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("tmId", p_tmId);
            restArgs.put("languages", p_languages);
            restArgs.put("startDate", p_startDate);
            restArgs.put("finishDate", p_finishDate);
            restArgs.put("projectNames", p_projectNames);
            restArgs.put("exportFormat", p_exportFormat);
            restArgs.put("exportedFileName", p_exportedFileName);
            restStart = RestWebServiceLog.start(TmResource.class, EXPORT_TM, restArgs);

            ProjectTM tm = checkTmId(p_tmId, p_companyName);

            p_exportedFileName = checkExportedFileName(p_exportedFileName);

            String startDate = checkDate(p_startDate, "creation start date", false);
            String finishDate = checkDate(p_finishDate, "creation finish date", true);
            checkStartFinishDate(startDate, finishDate);

            p_languages = checkLanguages(p_languages);

            checkProjectNames(p_projectNames);

            String fileType = checkExportFormat(p_exportFormat);

            IExportManager exporter = null;
            String options = null;
            try
            {
                exporter = new ExportManager(tm, new SessionInfo("", ""));
                options = exporter.getExportOptions();
            }
            catch (Exception e)
            {
                throw new RestWebServiceException("Invalid tm Id");
            }
            if (options != null)
            {
                String directory = ExportUtil.getExportDirectory();
                identifyKey = RestWebServiceUtil.getRandomFeed();
                directory = directory + "/" + identifyKey + "/" + "inprogress";
                new File(directory).mkdirs();

                options = joinXml(options, startDate, finishDate, fileType, p_languages,
                        p_exportedFileName, p_projectNames);
                try
                {
                    exporter.setExportOptions(options);
                    if (StringUtil.isEmpty(p_exportedFileName))
                    {
                        options = exporter.analyzeTm();
                    }
                    // pass down new options from client
                    exporter.setExportOptions(options);
                    ((com.globalsight.everest.tm.exporter.ExportOptions) exporter
                            .getExportOptionsObject()).setIdentifyKey(identifyKey);
                    exporter.doExport();
                }
                catch (Exception e)
                {
                    ExportUtil.handleTmExportFlagFile(identifyKey, "failed", true);
                }
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(EXPORT_TM, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return Response.ok().entity(identifyKey).build();
    }

    private String formatDate(String strDate)
    {
        String formatDate = null;
        try
        {
            SimpleDateFormat sfm1 = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sfm2 = new SimpleDateFormat("MM/dd/yyyy");
            formatDate = sfm2.format(sfm1.parse(strDate));
        }
        catch (Exception e)
        {
            return "error";
        }
        return formatDate;
    }

    @SuppressWarnings("rawtypes")
    private String joinXml(String xml, String startDate, String finishDate,
            String fileType, String languages, String exportedFileName, String projectNames)
            throws RestWebServiceException
    {
        Document doc = null;
        try
        {
            doc = DocumentHelper.parseText(xml);
            Element rootElt = doc.getRootElement();
            Iterator fileIter = rootElt.elementIterator("fileOptions");
            while (fileIter.hasNext())
            {
                Element fileEle = (Element) fileIter.next();
                if (exportedFileName != null)
                {
                    Element fileNameElem = fileEle.element("fileName");
                    if (fileType.equals("xml"))
                    {
                        fileNameElem.setText(exportedFileName + ".xml");
                    }
                    else if (fileType.equals("tmx2"))
                    {
                        fileNameElem.setText(exportedFileName + ".tmx");
                    }
                }
                Element fileTypeElem = fileEle.element("fileType");
                fileTypeElem.setText(fileType);
                Element fileEncodingElem = fileEle.element("fileEncoding");
                fileEncodingElem.setText("UTF-8");
            }

            Iterator selectIter = rootElt.elementIterator("selectOptions");
            while (selectIter.hasNext())
            {
                Element selectEle = (Element) selectIter.next();
                Element selectModeElem = selectEle.element("selectMode");
                selectModeElem
                        .setText(com.globalsight.everest.tm.exporter.ExportOptions.SELECT_ALL);
            }

            Iterator filterIter = rootElt.elementIterator("filterOptions");
            while (filterIter.hasNext())
            {
                Element filterEle = (Element) filterIter.next();
                Element createdafterElem = filterEle.element("createdafter");
                createdafterElem.setText(startDate);
                Element createdbeforeElem = filterEle.element("createdbefore");
                Element language = filterEle.element("language");
                Element projectName = filterEle.element("projectName");
                if (finishDate == null)
                {
                    Date nowDate = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    String nowDateStr = sdf.format(nowDate);
                    createdbeforeElem.setText(nowDateStr);
                }
                else
                {
                    createdbeforeElem.setText(finishDate);
                }

                if (StringUtil.isNotEmpty(languages))
                {
                    language.setText(languages);
                }

                if (StringUtil.isNotEmpty(projectNames))
                {
                    projectName.setText(projectNames);
                }
            }

            Iterator outputIter = rootElt.elementIterator("outputOptions");
            while (outputIter.hasNext())
            {
                Element outputEle = (Element) outputIter.next();
                Element systemFields = outputEle.element("systemFields");
                systemFields.setText("true");
            }

            String xmlDoc = doc.asXML();
            return xmlDoc.substring(xmlDoc.indexOf("<exportOptions>"));
        }
        catch (DocumentException e)
        {
            throw new RestWebServiceException(e.getMessage());
        }
    }

    // Common parameter check for all TM resource methods
    private ProjectTM checkTmId(String p_tmId, String p_companyName) throws RestWebServiceException
    {
        ProjectTM tm = null;
        try
        {
            tm = ServerProxy.getProjectHandler().getProjectTMById(
                    Long.parseLong(p_tmId.trim()), false);

            Company company = ServerProxy.getJobHandler().getCompany(p_companyName);
            if (company != null && company.getId() != tm.getCompanyId())
            {
                String msg = "TM " + p_tmId + " does not belong to company " + company.getName();
                throw new RestWebServiceException(msg);
            }

            if (tm.getTm3Id() == null)
            {
                throw new RestWebServiceException(
                        "Restful APIs do not support TM2 requests as TM2 had been abandoned.");
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException("Invalid tm ID: " + p_tmId);
        }

        return tm;
    }

    // "exportTM" parameter check
    private String checkExportedFileName(String p_exportedFileName) throws RestWebServiceException
    {
        if (StringUtil.isEmpty(p_exportedFileName))
        {
            return null;
        }
        else
        {
            String specialChars = "~!@#$%^&*()+=[]\\';,./{}|\":<>?";
            for (int i = 0; i < p_exportedFileName.trim().length(); i++)
            {
                char c = p_exportedFileName.trim().charAt(i);
                if (specialChars.indexOf(c) > -1)
                {
                    throw new RestWebServiceException(ERROR_EXPORT_FILE_NAME);
                }
            }
        }
        return p_exportedFileName;
    }

    private String checkDate(String p_date, String paramName, boolean allowEmpty)
            throws RestWebServiceException
    {
        if (StringUtil.isEmpty(p_date))
        {
            if (allowEmpty)
            {
                return null;
            }
            else
            {
                throw new RestWebServiceException("Empty " + paramName);
            }
        }

        String date = formatDate(p_date);
        if (date.equals("error"))
        {
            throw new RestWebServiceException("Invalid " + paramName + ": " + p_date);
        }
        return date;
    }

    // "exportTM" parameter check
    private void checkStartFinishDate(String startDate, String finishDate)
            throws RestWebServiceException
    {
        if (startDate == null || finishDate == null)
            return;

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try
        {
            Date staDate = sdf.parse(startDate);
            Date fshDate = sdf.parse(finishDate);
            if (fshDate.before(staDate))
            {
                throw new RestWebServiceException("Invalid start date and finish date.");
            }
        }
        catch (ParseException e)
        {
            throw new RestWebServiceException("Invalid start date and finish date.");
        }
    }

    // "exportTM" parameter check
    private void checkProjectNames(String p_projectNames) throws RestWebServiceException
    {
        if (StringUtil.isNotEmpty(p_projectNames))
        {
            String specialChars = "~!@#$%^&*()+=[]\\\';./{}|\":<>?";
            for (int i = 0; i < p_projectNames.trim().length(); i++)
            {
                char c = p_projectNames.trim().charAt(i);
                if (specialChars.indexOf(c) > -1)
                {
                    throw new RestWebServiceException(ERROR_EXPORT_PROJECT_NAMES);
                }
            }
        }
    }

    // "exportTM" parameter check
    private String checkExportFormat(String p_exportFormat) throws RestWebServiceException
    {
        String fileType = null;
        if (StringUtil.isEmpty(p_exportFormat)
                || !p_exportFormat.trim().equalsIgnoreCase("GMX")
                && !p_exportFormat.trim().equalsIgnoreCase("TMX1.4b"))
        {
            throw new RestWebServiceException("Invalid export format: " + p_exportFormat);
        }

        if (p_exportFormat.equalsIgnoreCase("GMX"))
        {
            fileType = "xml";
        }
        else if (p_exportFormat.equalsIgnoreCase("TMX1.4b"))
        {
            fileType = "tmx2";
        }
        return fileType;
    }

    // "exportTM" parameter check
    private String checkLanguages(String p_languages) throws RestWebServiceException
    {
        if (StringUtil.isNotEmpty(p_languages))
        {
            String[] languageArr = p_languages.split(",");
            for (String lang : languageArr)
            {
                lang = lang.replace("-", "_");
                GlobalSightLocale locale = GSDataFactory.localeFromCode(lang.trim());
                if (locale == null)
                {
                    throw new RestWebServiceException("Invalid language: " + lang);
                }
            }
            return p_languages.replace("-", "_");
        }

        return null;
    }

    /**
     * Check TM export status and return exported TM data by identify key.
     * 
     * Sample URL:
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmId}/export/{identifyKey}
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name.
     * @param p_identifyKey
     *            -- identifyKey to help locate where the export file is. Required.
     * 
     * @return exported TM data in XML.
     * 
     * @throws RestWebServiceException
     */
    @GET
    @Path("/{tmId}/export/{identifyKey}")
    @Produces({"application/xml;charset=UTF-8"})
    public Response getTmExportFile(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @PathParam("identifyKey") String p_identifyKey)
            throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;

        try
        {
            String userName = getUserNameFromRequest(authorization);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("identifyKey", p_identifyKey);
            restStart = RestWebServiceLog.start(TmResource.class, GET_TM_EXPORT_FILE, restArgs);

            if (StringUtil.isEmpty(p_identifyKey))
                throw new RestWebServiceException("Empty identifyKey");

            String directory = ExportUtil.getExportDirectory();
            directory = directory.replace("\\", "/");
            String failed = directory + "/" + p_identifyKey + "/" + "failed";
            String inprogress = directory + "/" + p_identifyKey + "/" + "inprogress";
            File failedFile = new File(failed);
            File inporgressFile = new File(inprogress);

            if (failedFile.exists())
            {
                return Response.ok().entity("TM export failed").build();
            }
            else if (inporgressFile.exists() && !failedFile.exists())
            {
                return Response.ok().entity("TM is exporting").build();
            }
            else
            {
                try
                {
                    File file = new File(directory + "/" + p_identifyKey);
                    String[] subFileNames = file.list();
                    if (subFileNames != null && subFileNames.length > 0)
                    {
                        for (String fileName : subFileNames)
                        {
                            if (fileName.toLowerCase().endsWith(".xml")
                                    || fileName.toLowerCase().endsWith(".tmx"))
                            {
                                String xmlPath = directory + "/" + p_identifyKey + "/" + fileName;
                                File tmFile = new File(xmlPath);
                                ResponseBuilder response = Response.ok((Object) tmFile);
                                response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                                response.encoding("gzip");
                                return response.build();
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    throw new RestWebServiceException(e.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(GET_TM_EXPORT_FILE, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return null;
    }

    /**
     * Full text search from specified TMs.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tms/{tmIds}/fullTextSearch
     * ?searchText=searchText&sourceLocale=en_US&targetLocale=fr_FR
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name.
     * @param p_tmIds
     *            -- tm IDs comma separated. Required.
     * @param p_searchText
     *            -- Search text. Required.
     * @param p_sourceLocale
     *            -- source locale like "en_US". Required.
     * @param p_targetLocale
     *            -- target locale like "fr_FR". Required.
     * @param p_creationStartDate
     *            -- Tuv creation start date in "yyyyMMdd" format. Optional.
     * @param p_creationFinishDate
     *            -- Tuv creation finish date in "yyyyMMdd" format. Optional.
     * @param p_modifyStartDate
     *            -- Tuv modify start date in "yyyyMMdd" format. Optional.
     * @param p_modifyFinishDate
     *            -- Tuv modify finish date in "yyyyMMdd" format. Optional.
     * 
     * @return searched TM data in XML.
     * 
     * @throws RestWebServiceException
     */
    @SuppressWarnings("rawtypes")
    @GET
    @Path("/{tmIds}/fullTextSearch")
    @Produces(MediaType.APPLICATION_XML)
    public Response fullTextSearch(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @PathParam("tmIds") String p_tmIds,
            @QueryParam("searchText") String p_searchText,
            @QueryParam("sourceLocale") String p_sourceLocale,
            @QueryParam("targetLocale") String p_targetLocale,
            @QueryParam("creationStartDate") String p_creationStartDate,
            @QueryParam("creationFinishDate") String p_creationFinishDate,
            @QueryParam("modifyStartDate") String p_modifyStartDate,
            @QueryParam("modifyFinishDate") String p_modifyFinishDate)
            throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromRequest(authorization);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("tmIds", p_tmIds);
            restArgs.put("searchText", p_searchText);
            restArgs.put("sourceLocale", p_sourceLocale);
            restArgs.put("targetLocale", p_targetLocale);
            restArgs.put("creationStartDate", p_creationStartDate);
            restArgs.put("creationFinishDate", p_creationFinishDate);
            restArgs.put("modifyStartDate", p_modifyStartDate);
            restArgs.put("modifyFinishDate", p_modifyFinishDate);
            restStart = RestWebServiceLog.start(TmResource.class, FULL_TEXT_SEARCH, restArgs);

            checkParamters(userName, p_companyName, p_tmIds, p_searchText, p_sourceLocale,
                    p_targetLocale, p_creationStartDate, p_creationFinishDate, p_modifyStartDate,
                    p_modifyFinishDate);

            LocaleManager lm = ServerProxy.getLocaleManager();
            Date creationStartDate = parseStartDate(p_creationStartDate);
            Date creationFinishDate = parseEndDate(p_creationFinishDate);
            Date modifyStartDate = parseStartDate(p_modifyStartDate);
            Date modifyFinishDate = parseEndDate(p_modifyFinishDate);
            
            boolean searchInSource = true;
            GlobalSightLocale sourceGSL = lm.getLocaleByString(p_sourceLocale);
            GlobalSightLocale targetGSL = lm.getLocaleByString(p_targetLocale);
            // get all selected TMS
            ArrayList<Tm> tmList = new ArrayList<Tm>();
            for (String tmId : p_tmIds.split(","))
            {
                tmList.add(ServerProxy.getProjectHandler().getProjectTMById(
                        Long.parseLong(tmId.trim()), false));
            }
            // do search
            TmCoreManager mgr = LingServerProxy.getTmCoreManager();
            List<TMidTUid> queryResult = mgr.tmConcordanceQuery(tmList, p_searchText,
                    searchInSource ? sourceGSL : targetGSL, searchInSource ? targetGSL : sourceGSL,
                    null);

            /**
             * I try to map java object to XML, but JAXB will output encoded
             * "sourceSegment" and "targetSegment", which is not we wanted(maybe acceptable?)
             */
//            FullTextSearchSegmentList responseEntity = new FullTextSearchSegmentList();
//            responseEntity.setSourceLocale(sourceGSL.getDisplayName());
//            responseEntity.setTargetLocale(targetGSL.getDisplayName());
//
//            List<SegmentTmTu> tus = LingServerProxy.getTmCoreManager().getSegmentsById(queryResult);
//            String sourceSegment = "";
//            String targetSegment = "";
//            String sid = "";
//            String tmName = "";
//            BaseTmTuv trgTuv = null;
//            for (int i = 0, max = tus.size(); i < max; i++)
//            {
//                SegmentTmTu tu = tus.get(i);
//                if (tu == null)
//                {
//                    continue;
//                }
//
//                BaseTmTuv srcTuv = tu.getFirstTuv(sourceGSL);
//                if (!isValidCreationDate(srcTuv, creationStartDate, creationFinishDate))
//                    continue;
//                if (!isValidModifyDate(srcTuv, modifyStartDate, modifyFinishDate))
//                    continue;
//
//                TmxWriter.convertTuvToTmxLevel(tu, (SegmentTmTuv) srcTuv, TmxWriter.TMX_LEVEL_2);
//                sourceSegment = GxmlUtil.stripRootTag(srcTuv.getSegment());
//                sid = srcTuv.getSid() == null ? "N/A" : srcTuv.getSid();
//                sid = EditUtil.encodeXmlEntities(sid);
//                long tmId = srcTuv.getTu().getTmId();
//                tmName = ServerProxy.getProjectHandler().getProjectTMById(tmId, false).getName();
//
//                Collection targetTuvs = tu.getTuvList(targetGSL);
//                for (Iterator it = targetTuvs.iterator(); it.hasNext();)
//                {
//                    trgTuv = (BaseTmTuv) it.next();
//                    TmxWriter.convertTuvToTmxLevel(tu, (SegmentTmTuv) trgTuv, TmxWriter.TMX_LEVEL_2);
//                    targetSegment = GxmlUtil.stripRootTag(trgTuv.getSegment());
//                    responseEntity.addSegment(sourceSegment, targetSegment, sid, tmName);
//                }
//            }
//            return Response.status(200).entity(responseEntity).build();

            StringBuffer xml = new StringBuffer(XML_HEAD);
            xml.append("<segments>\r\n");
            xml.append("\t<sourceLocale>").append(sourceGSL.getDisplayName())
                    .append("</sourceLocale>\r\n");
            xml.append("\t<targetLocale>").append(targetGSL.getDisplayName())
                    .append("</targetLocale>\r\n");

            // Get all TUS by queryResult, then get all needed properties
            List<SegmentTmTu> tus = LingServerProxy.getTmCoreManager().getSegmentsById(queryResult);
            String sourceSegment = "";
            String targetSegment = "";
            String sid = "";
            String tmName = "";
            BaseTmTuv trgTuv = null;
            for (int i = 0, max = tus.size(); i < max; i++)
            {
                SegmentTmTu tu = tus.get(i);
                if (tu == null)
                {
                    continue;
                }

                BaseTmTuv srcTuv = tu.getFirstTuv(sourceGSL);
                if (!isValidCreationDate(srcTuv, creationStartDate, creationFinishDate))
                    continue;
                if (!isValidModifyDate(srcTuv, modifyStartDate, modifyFinishDate))
                    continue;

                TmxWriter.convertTuvToTmxLevel(tu, (SegmentTmTuv) srcTuv, TmxWriter.TMX_LEVEL_2);
                sourceSegment = GxmlUtil.stripRootTag(srcTuv.getSegment());
                sid = srcTuv.getSid() == null ? "N/A" : srcTuv.getSid();
                sid = EditUtil.encodeXmlEntities(sid);
                long tmId = srcTuv.getTu().getTmId();
                tmName = ServerProxy.getProjectHandler().getProjectTMById(tmId, false).getName();

                Collection targetTuvs = tu.getTuvList(targetGSL);
                for (Iterator it = targetTuvs.iterator(); it.hasNext();)
                {
                    trgTuv = (BaseTmTuv) it.next();
                    TmxWriter.convertTuvToTmxLevel(tu, (SegmentTmTuv) trgTuv, TmxWriter.TMX_LEVEL_2);
                    targetSegment = GxmlUtil.stripRootTag(trgTuv.getSegment());

                    xml.append("\t<segment>\r\n");
                    xml.append("\t\t<sourceSegment>").append(sourceSegment).append("</sourceSegment>\r\n");                    
                    xml.append("\t\t<targetSegment>").append(targetSegment).append("</targetSegment>\r\n");
                    xml.append("\t\t<sid>").append(sid).append("</sid>\r\n");
                    xml.append("\t\t<tuId>").append(tu.getId()).append("</tuId>\r\n");
                    xml.append("\t\t<tmName>").append(tmName).append("</tmName>\r\n");
                    xml.append("\t</segment>\r\n");         
                }

                sourceSegment = "";
                targetSegment = "";
                sid = "";
                tmName = "";
                trgTuv = null;
            }
            xml.append("</segments>\r\n");
            return Response.status(200).entity(xml.toString()).build();
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(FULL_TEXT_SEARCH, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
    }

    private void checkParamters(String p_userName, String p_companyName, String p_tmIds,
            String p_searchText, String p_sourceLocale, String p_targetLocale,
            String p_creationStartDate, String p_creationFinishDate, String p_modifyStartDate,
            String p_modifyFinishDate) throws RestWebServiceException
    {
        if (StringUtil.isEmpty(p_tmIds))
            throw new RestWebServiceException("Empty TM IDs");
        for (String tmId : p_tmIds.split(","))
        {
            checkTmId(tmId, p_companyName);
        }

        if (StringUtil.isEmpty(p_searchText))
            throw new RestWebServiceException("Empty search text");

        if (StringUtil.isEmpty(p_sourceLocale))
            throw new RestWebServiceException("Empty source locale");
        GlobalSightLocale sourceLocale = GSDataFactory.localeFromCode(p_sourceLocale);
        if (sourceLocale == null)
            throw new RestWebServiceException("Invaild source locale: " + p_sourceLocale);

        if (StringUtil.isEmpty(p_targetLocale))
            throw new RestWebServiceException("Empty target locale");
        GlobalSightLocale targetLocale = GSDataFactory.localeFromCode(p_targetLocale);
        if (targetLocale == null)
            throw new RestWebServiceException("Invaild target locale: " + p_targetLocale);

        String creationStartDate = checkDate(p_creationStartDate, "creation start date", true);
        String creationFinishDate = checkDate(p_creationFinishDate, "creation finish date", true);
        checkStartFinishDate(creationStartDate, creationFinishDate);

        String modifyStartDate = checkDate(p_modifyStartDate, "modify start date", true);
        String modifyFinishDate = checkDate(p_modifyFinishDate, "modify finish date", true);
        checkStartFinishDate(modifyStartDate, modifyFinishDate);
    }

    private boolean validateDateScope(Date creationDate, Date startDate, Date endDate)
    {
        if (startDate != null && creationDate.before(startDate))
        {
            return false;
        }
        if (endDate != null && creationDate.after(endDate))
        {
            return false;
        }

        return true;
    }

    private Date parseStartDate(String dateStr)
    {
        SimpleDateFormat sfm1 = new SimpleDateFormat("yyyyMMdd HHmmss");
        if (StringUtil.isNotEmpty(dateStr))
        {
            try
            {
                dateStr = dateStr.trim();
                dateStr += " 000000";
                return sfm1.parse(dateStr);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Date parseEndDate(String dateStr)
    {
        SimpleDateFormat sfm1 = new SimpleDateFormat("yyyyMMdd HHmmss");
        if (StringUtil.isNotEmpty(dateStr))
        {
            try
            {
                dateStr = dateStr.trim();
                dateStr += " 235959";
                return sfm1.parse(dateStr);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean isValidCreationDate(BaseTmTuv srcTuv, Date creationStartDate,
            Date creationFinishDate) throws ParseException
    {
        if (creationStartDate != null || creationFinishDate != null)
        {
            Date creationDate = format.parse(format.format(srcTuv.getCreationDate()));
            return validateDateScope(creationDate, creationStartDate, creationFinishDate);
        }
    
        return true;
    }

    private boolean isValidModifyDate(BaseTmTuv srcTuv, Date modifyStartDate, Date modifyFinishDate)
            throws ParseException
    {
        if (modifyStartDate != null || modifyFinishDate != null)
        {
            Date modifyDate = format.parse(format.format(srcTuv.getModifyDate()));
            return validateDateScope(modifyDate, modifyStartDate, modifyFinishDate);
        }
        return true;
    }

    /**
     * Leverage segment.
     * 
     * Sample URL: http://localhost:8080/globalsight/restfulServices/companies/{
     * companyName}/tms/leverage?searchText=searchText&tmProfileName=tmpName&sourceLocale=en_US
     * 
     * @since 8.6.9 release
     * 
     * @param authorization
     *            -- authorization information from request context header.
     * @param p_companyName
     *            -- company name. Required.
     * @param p_tmProfileName
     *            -- Translation memory profile name. Required.
     * @param p_searchText
     *            -- Search text. Required.
     * @param p_sourceLocale
     *            -- source locale like "en_US". Required.
     * @param p_targetLocale
     *            -- target locale like "fr_FR". Optional.
     * @param p_escapeString
     *            -- Is convert all the escapable characters into their string
     *            (escaped) equivalents or not, available values: "true" or "false".
     *            Optional.
     * 
     * @return searched TM data in XML.
     * 
     * @throws RestWebServiceException
     */
    @SuppressWarnings("rawtypes")
    @GET
    @Path("/leverage")
    @Produces(MediaType.APPLICATION_XML)
    public Response leverageSegment(
            @HeaderParam("Authorization") List<String> authorization,
            @PathParam("companyName") String p_companyName,
            @QueryParam("tmProfileName") String p_tmProfileName,
            @QueryParam("searchText") String p_searchText,
            @QueryParam("sourceLocale") String p_sourceLocale,
            @QueryParam("targetLocale") String p_targetLocale,
            @QueryParam("escapeString") String p_escapeString) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromRequest(authorization);
            String escapeString = checkEscapeString(p_escapeString);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyName", p_companyName);
            restArgs.put("tmProfileName", p_tmProfileName);
            restArgs.put("searchText", p_searchText);
            restArgs.put("sourceLocale", p_sourceLocale);
            restArgs.put("escapeString", escapeString);
            restStart = RestWebServiceLog.start(TmResource.class, LEVERAGE_SEGMENT, restArgs);

            TranslationMemoryProfile tmp = checkTmProfileName(p_tmProfileName, p_companyName);

            if (StringUtil.isEmpty(p_sourceLocale))
                throw new RestWebServiceException("Empty source locale");
            GlobalSightLocale sourceLocale = getLocaleByName(p_sourceLocale);

            GlobalSightLocale targetLocale = getLocaleByName(p_targetLocale);

            if (StringUtil.isEmpty(p_searchText))
                throw new RestWebServiceException("Empty search text");

            LeveragingLocales levLocales = new LeveragingLocales();
            ArrayList<GlobalSightLocale> trgLocales = new ArrayList<GlobalSightLocale>();
            if (targetLocale == null)
            {
                Iterator it = ServerProxy.getLocaleManager().getSourceTargetLocalePairs().iterator();
                while (it.hasNext())
                {
                    LocalePair localePair = (LocalePair) it.next();
                    if (localePair.getSource().equals(sourceLocale))
                    {
                        GlobalSightLocale trgLocale = localePair.getTarget();
                        trgLocales.add(trgLocale);
                        levLocales.setLeveragingLocale(trgLocale, null);
                    }
                }
            }
            else
            {
                trgLocales.add(targetLocale);
                levLocales.setLeveragingLocale(targetLocale, null);
            }

            OverridableLeverageOptions levOptions = new OverridableLeverageOptions(tmp, levLocales);
            int threshold = (int) tmp.getFuzzyMatchThreshold();
            levOptions.setMatchThreshold(threshold);
            Set<Long> tmIdsOverride = new HashSet<Long>();
            Vector<LeverageProjectTM> tms = tmp.getProjectTMsToLeverageFrom();
            for (LeverageProjectTM tm : tms)
            {
                tmIdsOverride.add(tm.getProjectTmId());
            }
            levOptions.setTmsToLeverageFrom(tmIdsOverride);

            String segment = wrapSegment(p_searchText, Boolean.valueOf(escapeString));
            PageTmTu tu = new PageTmTu(-1, -1, "plaintext", "text", true);
            PageTmTuv tuv = new PageTmTuv(-1, segment, sourceLocale);
            tuv.setTu(tu);
            tuv.setExactMatchKey();
            tu.addTuv(tuv);

            Iterator<LeverageMatches> itLeverageMatches = LingServerProxy
                    .getTmCoreManager()
                    .leverageSegments(Collections.singletonList(tuv), sourceLocale, trgLocales,
                            levOptions).leverageResultIterator();

            long jobId = -1;
            Map<String, List<LeveragedTuv>> storedTuvs = new HashMap<String, List<LeveragedTuv>>();
            // In fact only ONE levMatches in this iterator.
            while (itLeverageMatches.hasNext())
            {
                LeverageMatches levMatches = (LeverageMatches) itLeverageMatches.next();
                // walk through all target locales in the LeverageMatches
                Iterator itLocales = levMatches.targetLocaleIterator(jobId);
                while (itLocales.hasNext())
                {
                    GlobalSightLocale tLocale = (GlobalSightLocale) itLocales.next();
                    // walk through all matches in the locale
                    Iterator itMatch = levMatches.matchIterator(tLocale, jobId);
                    while (itMatch.hasNext())
                    {
                        LeveragedTuv matchedTuv = (LeveragedTuv) itMatch.next();
                        if (matchedTuv.getScore() < threshold)
                        {
                            continue;
                        }

                        List<LeveragedTuv> tuvs = storedTuvs.get(tLocale.toString());
                        if (tuvs == null)
                        {
                            tuvs = new ArrayList<LeveragedTuv>();
                            storedTuvs.put(tLocale.toString(), tuvs);
                        }

                        storedTuvs.get(tLocale.toString()).add(matchedTuv);
                    }
                }
            }

            Set<String> localeNames = storedTuvs.keySet();
            if (localeNames.size() == 0)
            {
                return Response.status(200).entity(NULL_XML).build();
            }

            StringBuilder returnString = new StringBuilder(XML_HEAD);
            returnString.append("<entries>");
            boolean isTmProcedence = tmp.isTmProcendence();
            try
            {
                String tmName = "";
                for (String name : localeNames)
                {
                    List<LeveragedTuv> matchedTuvs = storedTuvs.get(name);
                    Collections.sort(matchedTuvs,
                            getMatchedTuvComparator(levOptions, isTmProcedence));
                    int size = Math.min(matchedTuvs.size(),
                            (int) tmp.getNumberOfMatchesReturned());
                    for (int i = 0; i < size; i++)
                    {
                        LeveragedTuv matchedTuv = matchedTuvs.get(i);
                        BaseTmTuv sourceTuv = matchedTuv.getSourceTuv();

                        long tmId = sourceTuv.getTu().getTmId();
                        tmName = ServerProxy.getProjectHandler().getProjectTMById(tmId, false)
                                .getName();

                        String strTmId = "\"" + tmId + "\"";
                        String entryXml = MessageFormat.format(ENTRY_XML,
                                strTmId, tmName, matchedTuv.getScore(),
                                sourceTuv.getSid(), sourceTuv.getLocale(),
                                sourceTuv.getSegmentNoTopTag(),
                                matchedTuv.getLocale(),
                                matchedTuv.getSegmentNoTopTag());

                        if (sourceTuv.getSid() == null || sourceTuv.getSid().length() == 0)
                        {
                            entryXml = entryXml.replaceAll("\r\n\t\t<sid>.*?</sid>", "");
                        }

                        returnString.append(entryXml);
                    }
                    // Remained trgLocales have no tm matches better than
                    // threshold
                    for (int i = 0; i < trgLocales.size(); i++)
                    {
                        GlobalSightLocale trgLocale = (GlobalSightLocale) trgLocales.get(i);
                        if (trgLocale.toString().equals(name))
                        {
                            trgLocales.remove(trgLocale);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                logger.error(e);
            }
            returnString.append("\r\n</entries>");
            return Response.status(200).entity(returnString.toString()).build();
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(LEVERAGE_SEGMENT, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
    }

    /**
     * Gets the comparator for matched TUVs
     * 
     * @param leverageOptions
     * @param isTmProcedence
     * @return
     */
    private Comparator<LeveragedTuv> getMatchedTuvComparator(
            final OverridableLeverageOptions leverageOptions,
            final boolean isTmProcedence)
    {
        return new Comparator<LeveragedTuv>()
        {
            @Override
            public int compare(LeveragedTuv tuv1, LeveragedTuv tuv2)
            {
                long tmId1 = tuv1.getTu().getTmId();
                long tmId2 = tuv2.getTu().getTmId();
                int projectIndex1 = getProjectIndex(tmId1);
                int projectIndex2 = getProjectIndex(tmId2);
                float result = 0.0f;
                if (isTmProcedence)
                {
                    result = projectIndex1 - projectIndex2;
                    if (result == 0)
                    {
                        result = tuv2.getScore() - tuv1.getScore();
                    }
                }
                else
                {
                    result = tuv2.getScore() - tuv1.getScore();
                    if (result == 0)
                    {
                        result = projectIndex1 - projectIndex2;
                    }
                }
                return (int) result;
            }

            private int getProjectIndex(long tmId)
            {
                return leverageOptions.getTmIndexsToLeverageFrom().get(tmId);
            }
        };
    }

    private TranslationMemoryProfile checkTmProfileName(String p_tmProfileName, String p_companyName)
            throws Exception
    {
        if (StringUtil.isEmpty(p_tmProfileName))
            throw new RestWebServiceException("Empty TM profile name");

        TranslationMemoryProfile tmp = TMProfileHandlerHelper.getTMProfileByName(p_tmProfileName);
        if (tmp == null)
        {
            String message = "Unable to find translation memory profile: " + p_tmProfileName;
            logger.error(message);
            throw new RestWebServiceException(message);
        }

        Company company = ServerProxy.getJobHandler().getCompany(p_companyName);
        if (company != null && company.getId() != tmp.getCompanyId())
        {
            String msg = "Translation memory profile '" + p_tmProfileName
                    + "' does not belong to company: " + company.getName();
            throw new RestWebServiceException(msg);
        }
        return tmp;
    }

}
