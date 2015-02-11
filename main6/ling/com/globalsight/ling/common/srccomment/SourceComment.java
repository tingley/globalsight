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

package com.globalsight.ling.common.srccomment;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.segmentationhelper.SegmentationRule;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.SegmentNode;

public class SourceComment
{
    public static String srcComment_title = "Comment from source file";
    public static String srcComment_priority = "Medium";
    public static String srcComment_status = "open";
    public static String srcComment_category = "Source Comment";

    private static String phSrcCmtEndKey = "\"></ph>";
    private static String phSrcCmtEndKey2 = "\"/>";
    private static String phSrcCmtStartKey = "<ph type=\"srcComment\"";

    private static String phSrcCmtValueEndKey = "\"></ph>";
    private static String phSrcCmtValueStartKey = "<ph type=\"srcComment\" value=\"";

    private static XmlEntities m_xmlEncoder = new XmlEntities();

    // //////////////////////////////////////////////
    // static methods
    // //////////////////////////////////////////////

    public static String getSrcCommentValue(String segment)
    {
        String result = null;
        if (segment == null || !segment.contains(phSrcCmtStartKey))
        {
            return result;
        }

        StringIndex si = StringIndex.getValueBetween(new StringBuffer(segment), 0,
                phSrcCmtValueStartKey, phSrcCmtValueEndKey);
        if (si != null)
        {
            result = si.value;
        }

        return result;
    }

    public static String removeSrcCommentNode(String segment)
    {
        String result = segment;
        if (segment == null || !segment.contains(phSrcCmtStartKey))
        {
            return result;
        }

        StringIndex si = StringIndex.getValueBetween(new StringBuffer(segment), 0,
                phSrcCmtStartKey, phSrcCmtEndKey);
        while (si != null)
        {
            result = result.substring(0, si.start - phSrcCmtStartKey.length())
                    + result.substring(si.end + phSrcCmtEndKey.length());
            si = StringIndex.getValueBetween(new StringBuffer(result), 0, phSrcCmtStartKey,
                    phSrcCmtEndKey);
        }

        if (result.contains(phSrcCmtStartKey))
        {
            si = StringIndex.getValueBetween(new StringBuffer(result), 0, phSrcCmtStartKey,
                    phSrcCmtEndKey2);
            while (si != null)
            {
                result = result.substring(0, si.start - phSrcCmtStartKey.length())
                        + result.substring(si.end + phSrcCmtEndKey2.length());
                si = StringIndex.getValueBetween(new StringBuffer(result), 0, phSrcCmtStartKey,
                        phSrcCmtEndKey2);
            }
        }

        return result;
    }

    public static List<SegmentNode> handleSrcComment(List<SegmentNode> p_segments)
    {
        if (p_segments == null || p_segments.size() == 0)
        {
            return new ArrayList<SegmentNode>();
        }

        ArrayList<SegmentNode> result = new ArrayList<SegmentNode>();
        String nextSrcCmt = null;
        for (SegmentNode oriSeg : p_segments)
        {
            String oriText = oriSeg.getSegment();
            String srcCmt = getSrcCommentValue(oriText);

            if (srcCmt == null && nextSrcCmt != null)
            {
                srcCmt = nextSrcCmt;
            }

            nextSrcCmt = getNextSrcComentValue(oriText);

            oriText = removeSrcCommentNode(oriText);
            oriSeg.setSegment(oriText);
            SegmentNode sn = new SegmentNode(oriSeg);
            srcCmt = srcCmt == null ? null : m_xmlEncoder.encodeStringBasic(srcCmt);
            sn.setSrcComment(srcCmt);

            result.add(sn);
        }

        return result;
    }

    private static String getNextSrcComentValue(String segment)
    {
        String result = null;
        if (segment == null || !segment.contains(phSrcCmtStartKey)
                || !segment.endsWith(phSrcCmtEndKey))
        {
            return result;
        }

        int index = segment.lastIndexOf(phSrcCmtStartKey);
        String subSegment = segment.substring(index);

        StringIndex si = StringIndex.getValueBetween(new StringBuffer(subSegment), 0,
                phSrcCmtValueStartKey, phSrcCmtValueEndKey);
        if (si != null)
        {
            result = si.value;
        }

        return result;
    }

    public static IssueImpl createSourceComment(TargetPage tPage, Tu tu, Tuv tuv, String jobUid)
    {
        String uid = jobUid == null ? tuv.getCreatedUser() : jobUid;
        String key = CommentHelper.makeLogicalKey(tPage.getId(), tu.getId(), tuv.getId(), 0);
        IssueImpl issue = new IssueImpl(Issue.TYPE_SEGMENT, tuv.getId(),
                SourceComment.srcComment_title, SourceComment.srcComment_priority,
                SourceComment.srcComment_status, SourceComment.srcComment_category, uid,
                tuv.getSrcComment(), key);
        issue.setShare(false);
        issue.setOverwrite(false);
        return issue;
    }
}
