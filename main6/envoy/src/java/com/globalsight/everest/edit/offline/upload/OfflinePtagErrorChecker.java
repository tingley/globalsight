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


package com.globalsight.everest.edit.offline.upload;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.page.PageData;
import com.globalsight.everest.edit.offline.page.UploadIssue;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoErrorChecker;
import com.globalsight.ling.tw.PseudoParserException;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.SegmentUtil2;

public class OfflinePtagErrorChecker
{
    static private final Logger CATEGORY =
        Logger.getLogger(
            OfflinePtagErrorChecker.class);

    private PtagErrorPageWriter m_errWriter = null;
    private ResourceBundle m_messages = null;

    private int TRADOS_REGX_SOURCE_PAREN = 1;
    private int TRADOS_REGX_TARGET_PAREN = 2;

    // Compile Regex used to detect and auto-clean Trados segments
    // A trados segment has the following form:
    //
    // {0> sourceText <}nn{> targetText <0}
    //
    // Where:
    //      {0>     is the begin marker
    //      <}nn{>  is the middle marker and "nn" is some number
    //      <0}     is the end marker
    //
    // This basic aexpresion will match:
    //     ^{0>(.*)<}\d+{>(.*)<0}$
    //
    // below we add an allowance for white space within the marker syntax
    // and use escapes where necessary.
    static private final String tradosSegStart  = "\\{\\s*0\\s*>";
    static private final String tradosSegSource = "(.*)";
    static private final String tradosSegMid    = "<\\s*\\}\\s*\\d+\\s*\\{\\s*>";
    static private final String tradosSegTarget = "(.*)";
    static private final String tradosSegEnd    = "<\\s*0\\s*\\}";
    static private final REProgram RE_TRADOS_MARKUP = createProgram(
        tradosSegStart + tradosSegSource + tradosSegMid + tradosSegTarget + tradosSegEnd);
    static private final RE RE_SEGMENT_START = new RE(createProgram(tradosSegStart), RE.MATCH_SINGLELINE);
    static private final RE RE_SEGMENT_MID = new RE(createProgram(tradosSegMid), RE.MATCH_SINGLELINE);
    static private final RE RE_SEGMENT_END = new RE(createProgram(tradosSegEnd), RE.MATCH_SINGLELINE);
    
    private static REProgram createProgram(String p_pattern)
    {
        REProgram pattern = null;

        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException ex)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(ex.getMessage());
        }

        return pattern;
    }

    // resource keys
    static private final String RESKEY_BAD_HEADER = "BadHeader";


    /**
     * Maximum size of a segment in UTF-8 chars.  Together these
     * values can be used to enable a gxml length check for our db.
     * A length of zero disables the check.
     */
    static private final int m_maxLengthGxml = 0; // disabled
    static private final String m_gxmlEncoding = "UTF8";

    /**
     * Maximum size of native-content.  Together these values can be
     * used to enable a native content length check for the clients db.
     * A length of zero disables the check.
     */
    static private final int m_maxLengthNativeContent = 0; // disabled
    static private final String m_nativeContentEncoding = "UTF8";

    /**
     * Constructor.
     */
    public OfflinePtagErrorChecker(PtagErrorPageWriter p_errWriter)
        throws AmbassadorDwUpException
    {
        m_messages = ResourceBundle.getBundle(
            "com.globalsight.everest.edit.offline.upload.OfflinePtagErrorChecker",
            p_errWriter.getLocale());

        m_errWriter = p_errWriter;
    }
    
    /**
     * Offline report file error checker.
     */
    public String checkAndSave(Map p_idSegmentMap, boolean p_adjustWS, long p_localeId, String p_userId)
    throws Exception
    {

    		TuvManager tuvManager = ServerProxy.getTuvManager();
    		List tuvs = new ArrayList();
    		Set tuIds = p_idSegmentMap.keySet();
    		Iterator tuIdIterator = tuIds.iterator();
    		long tuId = -1;
    		Long tuIdLong = null;
    		String segment = null;
    		PseudoData pTagData = new PseudoData();
    		pTagData.setMode(PseudoConstants.PSEUDO_COMPACT);
    		PseudoErrorChecker errorChecker = new PseudoErrorChecker();
    		errorChecker.setStyles(SegmentUtil2.getTAGS());
    		boolean hasError = false;
    		while (tuIdIterator.hasNext())
    		{
    			tuIdLong = (Long)tuIdIterator.next();
    			tuId = tuIdLong.longValue();
    			Tuv tuv = tuvManager.getTuvForSegmentEditor(tuId, p_localeId);
    		    if (tuv.getDataType().toLowerCase().equals("html"))
    	        {
    	           // sepecial treatment for html
    	           pTagData.setAddables(tuv.getTu().getTuType().equals("text") ?
    	               tuv.getDataType() : tuv.getTu().getTuType());
    	        }
    	        else
    	        {
    	           // all other non-html formats
    	           pTagData.setAddables(tuv.getDataType());
    	        }
    		    TmxPseudo.tmx2Pseudo(
                        tuv.getGxmlExcludeTopTags(), pTagData);
    		    
    			segment = (String)p_idSegmentMap.get(tuIdLong);
    			
    			String errMsg = null;
    			if (segment != null && segment != "")
    			{
    				if (!pTagData.getPTagSourceString().equals(segment))
    				{
    					pTagData.setPTagTargetString(segment);
    					pTagData.setDataType(tuv.getDataType());
					    if ((errMsg = errorChecker.check(pTagData, "",
		                        m_maxLengthGxml, m_gxmlEncoding,
		                        m_maxLengthNativeContent, m_nativeContentEncoding)) != null)
					    {
					    	hasError = true;
	                        m_errWriter.addSegmentErrorMsg(String.valueOf(tuId), errMsg.trim());
					    }
    					tuv.setGxmlExcludeTopTags(TmxPseudo.pseudo2Tmx(pTagData));
    					tuvs.add(tuv);
    				}
    				
    			}
    		}
    		
		    if (hasError)
	        {
	            return m_errWriter.buildReportErroPage().toString();       
	        }
		    else
		    {
		    	Iterator tuvIterator = tuvs.iterator();
	    		Tuv tuvToUpdate = null;
	    		while(tuvIterator.hasNext())
	    		{
	    			tuvToUpdate = (Tuv)tuvIterator.next();
	    			tuvToUpdate.setLastModifiedUser(p_userId);
	    			tuvManager.updateTuv(tuvToUpdate);
	    		}
		    }
    		
		   return null;
    }
    /**
     * Offline error checker.
     *
     * Compares an offline page to an internal offline reference page.
     * In doing so this method compares the ptags in the uploaded
     * target segments to the ptags in the reference source segments.
     *
     * If successful, each segment in the upload page is converted
     * back to gxml.
     *
     * @param p_referencePage - the base file containing the original
     * English text.
     * @param p_uploadPage - the upload file containing the translated text.
     * @return On error, we return an error message. If there are no
     * errors we return null.
     */
    public String check(ArrayList<PageData> p_referencePages,
        OfflinePageData p_uploadPage, boolean p_adjustWS)
    {
        PseudoData pTagData = null;
        TmxPseudo convertor = null;
        PseudoErrorChecker errorChecker = null;
        String errMsg = null;
        boolean hasErr = false;
        int uploadPagePtagDisplayMode;
        List errorList = new ArrayList();

        // Create PTag resources
        pTagData = new PseudoData();
        pTagData.setLocale(m_errWriter.getLocale());
        pTagData.setDataType(p_uploadPage.getDocumentFormat());

        convertor = new TmxPseudo();
        errorChecker = new PseudoErrorChecker();
        errorChecker.setStyles(SegmentUtil2.getTAGS());
        // Confirm/get the ptag display mode.
        if (p_uploadPage.getPlaceholderFormat().equals(
            AmbassadorDwUpConstants.TAG_TYPE_PTAGV))
        {
            uploadPagePtagDisplayMode = PseudoConstants.PSEUDO_VERBOSE;
        }
        else if (p_uploadPage.getPlaceholderFormat().equals(
            AmbassadorDwUpConstants.TAG_TYPE_PTAGC))
        {
            uploadPagePtagDisplayMode = PseudoConstants.PSEUDO_COMPACT;
        }
        else
        {
            // InvalidTagModeError
            String label = AmbassadorDwUpConstants.LABEL_CURRENT_FMT;

            String args[] = {label,
                             AmbassadorDwUpConstants.TAG_TYPE_PTAGV,
                             AmbassadorDwUpConstants.TAG_TYPE_PTAGC };

            errMsg = MessageFormat.format(
                m_messages.getString("InvalidTagModeError"), args);

            m_errWriter.addFileErrorMsg(errMsg);

            return m_errWriter.buildPage().toString();
        }

        // ==========================
        // compare pages, segment-by-segment, driven by the reference page
        // ==========================
        OfflineSegmentData refSeg = null;
        OfflineSegmentData uploadSeg = null;
        OfflinePageData p_referencePage = null;
        for (int ii = 0; ii < p_referencePages.size(); ii++) {
        	p_referencePage = p_referencePages.get(ii).getOfflinePageData();
	        for (ListIterator it = p_referencePage.getSegmentIterator(); it.hasNext(); )
	        {
	            refSeg = (OfflineSegmentData)it.next();
	
	            uploadSeg = p_uploadPage.getSegmentByDisplayId(
	                refSeg.getDisplaySegmentID());
	
	            // Detect SegmentMissingError.
	            // Note: SegmentAddError is detected way below - it is the last check.
	            if (uploadSeg == null)
	            {
	                hasErr = true;
	                String args[] = { refSeg.getDisplaySegmentID(),
	                                  String.valueOf(p_referencePage.getTotalNumOfSegments()) };
	                errMsg = MessageFormat.format(
	                    m_messages.getString("SegmentMissingError"), args);
	
	                m_errWriter.addSegmentErrorMsg(refSeg, errMsg);
	
	                return m_errWriter.buildPage().toString();
	            }
	
	            uploadSeg.setReferenceSegmentFound(true);
	            String tempUploadTargetDisplayText = uploadSeg.getDisplayTargetText();
	
	            try
	            {
	                // flag protected segments
	                if (UploadPageSaver.confirmUploadProtection(uploadSeg))
	                {
	                    // Note: we allow multiple downloads (same
	                    // user-same files) and we also allow the user
	                    // to choose to un-protect exact matches on
	                    // the download screen (when this feature is
	                    // enabled for them in the l10n profile).
	
	                    // The end result of all these download
	                    // choices is currently recorded in the given
	                    // instance of the offline file via the
	                    // Matchtype string. So this string is used
	                    // upon upload to help determine the
	                    // protection status requested during
	                    // download.  Then we set the following
	                    // values.
	                    refSeg.setWriteAsProtectedSegment(true);
	                }
	                else
	                {
	                    refSeg.setWriteAsProtectedSegment(false);
	
	                    // detect uncleaned Trados segments,
	                    RE tradosMarkup = new RE(RE_TRADOS_MARKUP, RE.MATCH_SINGLELINE);
	                    if (tradosMarkup.match(tempUploadTargetDisplayText))
	                    {
	                        tempUploadTargetDisplayText =
	                            tradosMarkup.getParen(TRADOS_REGX_TARGET_PAREN);
	
	                        if (CATEGORY.isDebugEnabled())
	                        {
	                            CATEGORY.debug("\n\nDetected trados markup in segment " +
	                                uploadSeg.getDisplaySegmentID() +
	                                " and extracted the following target text: \n\n" +
	                                tempUploadTargetDisplayText);
	                        }
	                    }
	                    else 
	                    {
	                        // The display text not match the format "{0> sourceText
	                        // <}n{> targetText <0}", and include "{0>", "<}n{>" or "<0}", we
	                        // think there are some targets are missing.
	                        
	                        boolean isInclude = false;
	                        List res = new ArrayList();
	                        res.add(RE_SEGMENT_START);
	                        res.add(RE_SEGMENT_MID);
	                        res.add(RE_SEGMENT_END);
	                        
	                        for (int i = 0; i < res.size(); i++)
	                        {
	                            RE re = (RE)res.get(i);
	                            isInclude = re.match(tempUploadTargetDisplayText);
	                            if (isInclude)
	                            {
	                                break;
	                            }
	                        }
	                        
	                        if (isInclude) 
	                        {
	                            String args[] = {"<,},0,{,>,<,0,}"};
	                            errMsg = MessageFormat.format(
	                                    m_messages.getString("TagsMissError"), args);
	                            
	                            m_errWriter.addSegmentErrorMsg(uploadSeg, errMsg);
	                            
	                            return m_errWriter.buildPage().toString();
	                        }
	                    }
	
	                    // Configure the PTag data object - this must
	                    // be done prior to using the object with the
	                    // conversion methods.  Set placeholder mode
	                    // as determined by the upload header.
	                    pTagData.setMode(uploadPagePtagDisplayMode);
	
	                    // - set addables as determined by reference segment
	                    if (refSeg.getDisplaySegmentFormat().toLowerCase().equals("html"))
	                    {
	                        // sepecial treatment for html
	                        pTagData.setAddables(refSeg.getSegmentType().equals("text") ?
	                            refSeg.getDisplaySegmentFormat() : refSeg.getSegmentType());
	                    }
	                    else
	                    {
	                        // all other non-html formats
	                        pTagData.setAddables(refSeg.getDisplaySegmentFormat());
	                    }
	
	                    // Compare previous ptag target with new one.
	
	                    // Note: implementation of split/merge
	                    // requires subflows to always be re-joined
	                    // with targets.
	
	                    convertor.tmx2Pseudo(
	                        refSeg.getDisplayTargetText(), pTagData);
	                    String refTarget = pTagData.getPTagSourceString(); // intentional
	                    
	
	                    // For GBS-608. I think the method refinePseudoTag is wrong.
	                    // we can't replace all tags just by order.
	                    
	                    // tempUploadTargetDisplayText =
	                    // refinePseudoTag(tempUploadTargetDisplayText, refTarget);
	                    // uploadSeg.setDisplayTargetText(tempUploadTargetDisplayText);
	
	                    if (!refTarget.equals(tempUploadTargetDisplayText))
	                    {
	                        if (CATEGORY.isDebugEnabled())
	                        {
	                            CATEGORY.debug("Segment modified:" +
	                                "\r\nOriginal: " + refTarget +
	                                "\r\nUploaded: " + tempUploadTargetDisplayText);
	                        }
	
	                        uploadSeg.setTargetHasBeenEdited(true);
	                    }
	
	                    // Now load the source and target segments for the
	                    // error checker.  Set source (ptag format set above).
	                    if (false && CATEGORY.isDebugEnabled())
	                    {
	                        CATEGORY.debug("\n\nREF Source GXML set for next Target" +
	                            " ptag-to-Gxml conversion: " +
	                            refSeg.getDisplaySourceText());
	                    }
	
	                    convertor.tmx2Pseudo(refSeg.getDisplaySourceText(), pTagData);
	                    pTagData.setPTagTargetString(tempUploadTargetDisplayText);
	
	                    // Here we do ptag error checking with optional
	                    // checks on the max length of the entire Gxml
	                    // string and the max length of the native
	                    // content.  The former is in regards to our own
	                    // internal storage restrictions (if any) and is
	                    // enabled when m_maxLengthGxml != 0.
	                    // The latter check is intended to be used to
	                    // restrict the native content length with respect
	                    // to the clients target storage requirements.
	                    // Either length check can be diabled by passing 0
	                    // for the length.
	                    if ((errMsg = errorChecker.check(pTagData, "",
	                        m_maxLengthGxml, m_gxmlEncoding,
	                        m_maxLengthNativeContent, m_nativeContentEncoding)) != null)
	                    {
	                        hasErr = true;
	                        m_errWriter.addSegmentErrorMsg(uploadSeg, errMsg.trim());
	                        errorList.add("Segment " + uploadSeg.getDisplaySegmentID() + 
	                        		" error : " + errMsg);
	                        if (false && CATEGORY.isDebugEnabled())
	                        {
	                            CATEGORY.debug("NEW Target GXML as result of " +
	                                "ptag-to-gxml conversion: " +
	                                "aborted due to ptag error.");
	                        }
	                    }
	                    else
	                    {
	                        // If sucessful, set the new GXML string.
	                        // The convertor will encode special
	                        // characters user may have entered.
	                        String newGxml = convertor.pseudo2Tmx(pTagData);
	
	                        if (p_adjustWS)
	                        {
	                            newGxml = EditUtil.adjustWhitespace(newGxml,
	                                refSeg.getDisplaySourceText());
	                        }
	
	                        uploadSeg.setDisplayTargetText(newGxml);
	
	                        if (false && CATEGORY.isDebugEnabled())
	                        {
	                            CATEGORY.debug("NEW Target GXML as result of " +
	                                "ptag-to-gxml conversion: " +
	                                uploadSeg.getDisplayTargetText());
	                        }
	                    }
	                }
	            }
	            catch (PseudoParserException ex)
	            {
	                String args[] = { ex.toString() };
	                errMsg = MessageFormat.format(
	                    m_messages.getString("PtagParseError"), args);
	
	                m_errWriter.addSegmentErrorMsg(uploadSeg, errMsg);
	                return m_errWriter.buildPage().toString();
	            }
	            catch (DiplomatBasicParserException ex)
	            {
	                String args[] = { ex.toString() };
	                errMsg = MessageFormat.format(
	                    m_messages.getString("GXMLParseError"), args);
	
	                m_errWriter.addSegmentErrorMsg(uploadSeg, errMsg);
	                return m_errWriter.buildPage().toString();
	            }
	            catch (UploadPageSaverException ex)
	            {
	                m_errWriter.addSegmentErrorMsg(
	                    refSeg, ex.getMessage(m_errWriter.getLocale()));
	                return m_errWriter.buildPage().toString();
	            }
	            catch (Exception ex)
	            {
	                m_errWriter.addSystemErrorMsg(uploadSeg, ex.toString());
	                return m_errWriter.buildPage().toString();
	            }
	
	            pTagData.reset();
	        }
        }

        // Detect SegmentAddError.
        StringBuffer extraIds = null;
        ListIterator uploadSegIterator = p_uploadPage.getSegmentIterator();
        while (uploadSegIterator.hasNext())
        {
            uploadSeg = (OfflineSegmentData)uploadSegIterator.next();

            if (!uploadSeg.isReferenceSegmentFound())
            {
                hasErr = true;

                if (extraIds == null)
                {
                    extraIds = new StringBuffer();
                }

                extraIds.append("\n" + uploadSeg.getDisplaySegmentID());
            }
        }

        if (extraIds != null)
        {
            String args[] = { extraIds.toString() };

            errMsg = MessageFormat.format(
                m_messages.getString("SegmentAddError"), args);

            m_errWriter.addSegmentErrorMsg(refSeg, errMsg.trim());
        }

        // Finally check uploaded issues. For now, we separate them
        // into new issues and replies to existing issues.
        int iNew = 0, iReplies = 0, iDiscard = 0;
        HashMap issues = p_uploadPage.getUploadedIssuesMap();

        for (Iterator it = issues.values().iterator(); it.hasNext(); )
        {
            UploadIssue issue = (UploadIssue)it.next();

            String issueKey = makeIssueKey(issue);

            if (isNewIssue(p_referencePage, issue, issueKey))
            {
                iNew++;

                p_uploadPage.addUploadedNewIssue(issue);
            }
            else if (isModifiedIssue(p_referencePage, issue, issueKey))
            {
                iDiscard++;

                Issue refIssue = getReferenceIssue(p_referencePage, issueKey);
                p_uploadPage.addUploadedReplyIssue(refIssue, issue);
            }
            else
            {
                // this is an untouched issue, discard.
                iDiscard++;
            }
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Uploaded issues=" + issues.size() +
                ", new=" + iNew + " replies=" + iReplies +
                " discarded=" + iDiscard);
        }

        // Then we fill in the missing TUV ID.
        fixIssueTuvs(p_referencePage, p_uploadPage);

        // Complete the error checking process.
        if (hasErr)
        {
        	String error = null;
        	CATEGORY.error("Error happens with the following segments when uploading them.");
        	CATEGORY.error("File Name = " + m_errWriter.getFileName() + " Task Id = " + p_uploadPage.getTaskId() + 
        			" Page ID = " + p_uploadPage.getPageId() + " Workflow ID = " + p_uploadPage.getWorkflowId());
        	for(int i = 0, len = errorList.size(); i < len; i++ )
        	{
        		error = (String)errorList.get(i);
        		CATEGORY.error(error);
        	}
            return m_errWriter.buildPage().toString();       
        }

        p_uploadPage.setPlaceholderFormat(AmbassadorDwUpConstants.TAG_TYPE_GXML);

        return null;
    }

    /**
     * Refine the segment content. Because some tags was missing during the 
     * inconsistence, it causes the generated pseudo tag inconsistence.
     * 
     * @param str The sentence need to be refined.
     * @param refStr The reference sentence.
     * @return The refined sentence.
     */
    private String refinePseudoTag(String str, String refStr)
	{
		String resultStr = str;
        String regEx = "\\[[^\\[]+\\]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		Matcher m2 = p.matcher(refStr);
		String result, result2;
		while (m.find()) {
			result = str.substring(m.start(), m.end());
			if (m2.find()) {
				result2 = refStr.substring(m2.start(), m2.end());				
				resultStr = resultStr.replace(result, result2);
			}
		}
		return resultStr;
	}

	/**
     * Gets a key for issues stored in the reference page's IssueMap.
     */
    private String makeIssueKey(UploadIssue p_issue)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(p_issue.getTuId());
        sb.append(CommentHelper.SEPARATOR);
        sb.append(p_issue.getSubId());

        return sb.toString();
    }

    /**
     * Checks if the uploaded issue is new.
     */
    private Issue getReferenceIssue(OfflinePageData p_refOpd, String p_key)
    {
        HashMap oldIssues = p_refOpd.getIssuesMap();
        return (Issue)oldIssues.get(p_key);
    }

    /**
     * Checks if the uploaded issue is new.
     */
    private boolean isNewIssue(OfflinePageData p_refOpd,
        UploadIssue p_issue, String p_key)
    {
        Issue issue = getReferenceIssue(p_refOpd, p_key);

        // User has created a new issue.
        if (issue == null)
        {
            return true;
        }

        return false;
    }

    /**
     * Checks if the uploaded issue is a reply to an existing issue,
     * or merely a discardable copy of an existing issue.
     */
    private boolean isModifiedIssue(OfflinePageData p_refOpd,
        UploadIssue p_issue, String p_key)
    {
        Issue issue = getReferenceIssue(p_refOpd, p_key);

        if (issue == null)
        {
            // This is a new issue, not a modified one, caller should
            // have detected this by calling isNewIssue().
            return false;
        }

        String oldComment =
            ((IssueHistory)issue.getHistory().get(0)).getComment();

        if (issue.getTitle().equals(p_issue.getTitle()) &&
            issue.getStatus().equals(p_issue.getStatus()) &&
            issue.getPriority().equals(p_issue.getPriority()) &&
            oldComment.equals(p_issue.getComment()))
        {
            return false;
        }

        return true;
    }

    /**
     * Fix the UploadIssue objects by patching in the TUV ID based on
     * the available TU and SUB ID.
     */
    private void fixIssueTuvs(OfflinePageData p_referencePageData,
        OfflinePageData p_uploadPageData)
    {
        // Collect all uploaded issues in a nice data structure.
        ArrayList issues = new ArrayList();

        issues.addAll(p_uploadPageData.getUploadedNewIssues());
        issues.addAll(p_uploadPageData.getUploadedReplyIssuesMap().values());

        for (int i = 0, max = issues.size(); i < max; i++)
        {
            UploadIssue issue = (UploadIssue)issues.get(i);

            String key = issue.getDisplayId();

            /* Works for para view but not list view. Aaargh.
            String key = String.valueOf(issue.getTuId());
            if (issue.getSubId() != 0)
            {
                key = key + "_" + String.valueOf(issue.getSubId());
            }
            */

            OfflineSegmentData resOsd =
                p_referencePageData.getSegmentByDisplayId(key);

            if (CATEGORY.isDebugEnabled())
            {
                System.out.println("fixIssueTuvs looking for tu " +
                    issue.getTuId() + CommentHelper.SEPARATOR + "?" +
                    CommentHelper.SEPARATOR + issue.getSubId() +
                    " (display id=" + key + ")" +
                    ", found display id " + (resOsd != null ?
                        resOsd.getDisplaySegmentID() : "???") +
                    " tuvid=" + (resOsd != null ? resOsd.getTrgTuvId() :
                        new Long(-1)));
            }

            // resOsd better be an object!
            if (resOsd != null)
            {
                Long tuvId = resOsd.getTrgTuvId();

                issue.setTuvId(tuvId.longValue());
            }
        }
    }
}
