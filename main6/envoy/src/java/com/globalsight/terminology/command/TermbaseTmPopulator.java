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

package com.globalsight.terminology.command;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.TermbaseManager;
import com.globalsight.terminology.java.*;
import com.globalsight.terminology.util.Sortkey;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.UTC;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;

public class TermbaseTmPopulator implements ITermbaseTmPopulator
{

    private static final Logger CATEGORY = Logger
            .getLogger(TermbaseManager.class);
    private ArrayList<String> termImgType = new ArrayList<String>();
    
    public TermbaseTmPopulator() {
        termImgType.add("jpg");
        termImgType.add("gif");
        termImgType.add("png");
        termImgType.add("bmp");
        termImgType.add("tif");
        termImgType.add("tiff");
        termImgType.add("jpeg");
        termImgType.add("jpe");
        termImgType.add("jfif");
        termImgType.add("dib");
    }

    @Override
    public void populateTermbase(long TBId, List tuvs, String creator)
    {
        // if the source term has same content in termbase, make the target
        // overwrite the term of the concept
        // and don't create a new concept.
        if (tuvs.size() > 0)
        {
            BaseTmTuv tuvSource = (BaseTmTuv) tuvs.get(0);
            BaseTmTuv tuvTarget = (BaseTmTuv) tuvs.get(1);
            String sourceLanguage = tuvSource.getLocale().getDisplayLanguage(
                    Locale.US);
            String targetLanguage = tuvTarget.getLocale().getDisplayLanguage(
                    Locale.US);

            try
            {
                String sourceTerm = getTermTextByTuv(tuvSource);
                sourceTerm = TbUtil.FixTermIllegalChar(sourceTerm);
                StringBuffer hql = new StringBuffer();
                hql.append("select tt.tbLanguage.concept from TbTerm tt");
                hql.append(" where tt.termContent='").append(sourceTerm);
                hql.append("' and tt.tbLanguage.name='").append(sourceLanguage);
                hql.append("' and tt.tbid=").append(TBId);

                Iterator ite = HibernateUtil.search(hql.toString()).iterator();

                if (!ite.hasNext())
                {
                    TbConcept tc = getTbConceptByTuv(TBId, tuvs, creator);

                    if (tc != null)
                    {
                        HibernateUtil.save(tc);
                    }
                    
                    TbTerm imageTerm = getTbTermOfTargetTuv(tuvTarget, tc);
                    renameImage(tuvTarget, imageTerm);
                }

                while (ite.hasNext())
                {
                    TbConcept tc = (TbConcept) ite.next();
                    Iterator ite2 = tc.getLanguages().iterator();
                    boolean isHaveLan = false;

                    while (ite2.hasNext())
                    {
                        TbLanguage tl = (TbLanguage) ite2.next();

                        if (tl.getName() != null
                                && tl.getName().trim().equalsIgnoreCase(
                                        targetLanguage))
                        {
                            isHaveLan = true;
//                            tl.getTerms().clear();
                            TbTerm targetTbTerm = getTbTermByTuv(tuvTarget,
                                    creator, tl);
                            tl.getTerms().add(targetTbTerm);

                            break;
                        }
                    }

                    if (!isHaveLan)
                    {
                        TbLanguage tl = getTbLanguageByTuv(tuvTarget, creator,tc);
                        tc.getLanguages().add(tl);
                    }

                    HibernateUtil.saveOrUpdate(tc);
                    TbTerm imageTerm = getTbTermOfTargetTuv(tuvTarget, tc);
                    renameImage(tuvTarget, imageTerm);
                }
            }
            catch (Exception e)
            {
                CATEGORY.error("populate termbase error", e);
            } 
        }
    }
    
    private TbTerm getTbTermOfTargetTuv(BaseTmTuv tuv, TbConcept tc)
    {
        Iterator<TbLanguage> ite = tc.getLanguages().iterator();
        String language = tuv.getLocale().getDisplayLanguage(Locale.US);

        while (ite.hasNext())
        {
            TbLanguage tl = ite.next();
            if (tl.getName().equals(language))
            {
                Iterator<TbTerm> ite2 = tl.getTerms().iterator();
                while (ite2.hasNext())
                {
                    TbTerm tt = ite2.next();
                    if (tt.getTermContent() != null
                            && tt.getTermContent().trim().equals(
                                    getTermTextByTuv(tuv).trim()))
                    {
                        return tt;
                    }
                }
            }
        }

        return null;
    }

    private TbConcept getTbConceptByTuv(long TBId, List tuvs, String creator)
    {
        String domain = "*unknown*";
        String project = "*unknown*";
        String status = "proposed";
        StringBuffer xml = new StringBuffer();

        xml = xml.append("<transacGrp><transac type=\"origination\">");
        xml = xml.append(((BaseTmTuv) tuvs.get(0)).getCreationUser()).append(
                "</transac><date>");
        xml = xml.append(UTC.valueOf((new Date()))).append(
                "</date></transacGrp>");
        TbConcept tc = new TbConcept();
        tc.setDomain(domain);
        tc.setStatus(status);
        tc.setProject(project);
        tc.setXml(xml.toString());
        tc.setCreationBy(creator);
        java.util.Date date = new java.util.Date();
        Timestamp ts = new Timestamp(date.getTime());
        tc.setCreationDate(ts);
        Termbase tb = HibernateUtil.get(Termbase.class, TBId);

        if (tb != null)
        {
            tc.setTermbase(tb);

            for (int i = 0; i < tuvs.size(); i++)
            {
                BaseTmTuv tuv = (BaseTmTuv) tuvs.get(i);
                TbLanguage tl = getTbLanguageByTuv(tuv, creator, tc);
                tc.getLanguages().add(tl);
            }
        }

        return tc;
    }

    private TbLanguage getTbLanguageByTuv(BaseTmTuv tuv, String creator,
            TbConcept tc)
    {
        String language = tuv.getLocale().getDisplayLanguage(Locale.US);
        TbLanguage tlan = new TbLanguage();
        tlan.setTbid(tc.getTermbase().getId());
        tlan.setConcept(tc);
        tlan.setLocal(tuv.getLocale().getLanguage());
        tlan.setName(language);
        tlan.setXml("");
        TbTerm tt = getTbTermByTuv(tuv, creator, tlan);
        tlan.getTerms().add(tt);

        return tlan;
    }

    private TbTerm getTbTermByTuv(BaseTmTuv tuv, String creator, TbLanguage tl)
    {
        String language = tuv.getLocale().getDisplayLanguage(Locale.US);
        String termText = getTermTextByTuv(tuv);

        String termType = "*unknown*";
        String termStatus = "*unknown*";
        String sortKey = SqlUtil.toHex(Sortkey.getSortkey(termText, tuv
                .getLocale().getLanguage()), 2000);

        java.util.Date date = new java.util.Date();
        Timestamp ts = new Timestamp(date.getTime());
        TbTerm tbTerm = new TbTerm();
        tbTerm.setTbid(tl.getTbid());
        tbTerm.setCreationBy(creator);
        tbTerm.setCreationDate(ts);

        tbTerm.setModifyBy(creator);
        tbTerm.setModifyDate(ts);
        tbTerm.setSortKey(sortKey);
        tbTerm.setStatus(termStatus);

        tbTerm.setTermContent(termText);
        tbTerm.setType(termType);
        tbTerm.setXml("");
        tbTerm.setLanguage(language);
        tbTerm.setTbLanguage(tl);
        tbTerm.setTbConcept(tl.getConcept());

        return tbTerm;
    }

    private String getTermTextByTuv(BaseTmTuv tuv)
    {
        GxmlFragmentReader reader = null;
        String sourceTerm = new String();

        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();
            GxmlElement m_gxmlElement = reader.parseFragment(tuv.getSegment());
            sourceTerm = m_gxmlElement.getTextValue();
        }
        catch (Exception e)
        {
            throw new RuntimeException("generate term by tuv error: ", e);
        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }

        return sourceTerm;
    }

    private void renameImage(BaseTmTuv tuvTarget, TbTerm targetTerm)
    {
        String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg";

        try
        {
            for(int i = 0; i < termImgType.size(); i++) {
                StringBuffer fileName = new StringBuffer();
                fileName.append("tuv_");
                fileName.append(Long.toString(tuvTarget.getId()));
                fileName.append(".").append(termImgType.get(i));

                File file = new File(termImgPath, fileName.toString());
                
                if (file.exists())
                {
                    String newFileName = "tb_" + targetTerm.getId() + "." + termImgType.get(i);
                    File newFile = new File(termImgPath, newFileName);
                    FileUploadHelper.renameFile(file, newFile, true);
                    file.delete();
                }
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        /*
        File parentFilePath = new File(termImgPath.toString());
        File[] files = parentFilePath.listFiles();

        if (files != null && files.length > 0)
        {
            for (int j = 0; j < files.length; j++)
            {
                File file = files[j];
                String fileName = file.getName();

                if (fileName.lastIndexOf(".") > 0)
                {
                    String tempName = fileName.substring(0, fileName
                            .lastIndexOf("."));
                    String suffix = fileName.substring(fileName
                            .lastIndexOf("."), fileName.length());

                    String nowImgName = "tuv_"
                            + Long.toString(tuvTarget.getId());

                    if (tempName.equals(nowImgName))
                    {
                        String newFileName = "tb_" + targetTerm.getId()
                                + suffix;
                        File newFile = new File(termImgPath, newFileName);

                        try
                        {
                            FileUploadHelper.renameFile(file, newFile, true);
                        }
                        catch (Exception e)
                        {

                        }
                    }
                }
            }
        }
        */
    }
}
