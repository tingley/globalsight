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

package com.globalsight.ling.tm2.leverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.everest.projecthandler.ProjectTmTuT;
import com.globalsight.everest.projecthandler.ProjectTmTuvT;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;

/**
 * The format of tu is defferent between globalsight and worldserve. To improve
 * the availability of imported-worldserve tu, GS will replace placeholders to
 * make the format look like globalsight format.
 * <p>
 * Only tu come from storage tm can be repaired.
 * <p>
 * To qualify for this type of repairing, a TM match must meet the following
 * criteria:
 * <p>
 * 1. The TM match must be identified as a WorldServer TM entry (in the new
 * column).
 * <p>
 * 2. The match must be exact except for the differences between
 * placeholders/tags. In other words, if you removed the tags/placeholders from
 * the source content and the TM match, they would be identical.
 * <p>
 * 3. The TM match source segment placeholders must be in the same positions as
 * the tags in the source content segment.
 */
public class PlaceholderHander
{
    private SegmentTmTuv sourceTuv;
    private SegmentTmTu tu;
    
    // The id of storage TM setted in tm profile.
    private long saveTmId;

    // Tags of sourceTuv.
    private List<String> sourceTags = null;

    // Tags of tu.sourceTuv.
    private List<String> srcTags = null;

    // The relationship of placeholders between source tuv and matched tuv 
    private Map<String, String> replaceRules = null;

    // Regex of <ph>...</ph> and <ph/>
    private final static String PH_REGEX = "(<ph[^>]*?x=\"\\d*\"[^>/]*?>[^<]*?</ph>)|(<ph[^>]*?x=\"\\d*\"[^/>]*?/>)";

    // Regex of <bpt>...</bpt>, <ept>...</ept>, <bpt/> and <ept/>.
    private final static String PT_REGEX = "(<.pt[^>]*?i=\"\\d*\"[^>/]*?>[^<]*?</.pt>)|(<.pt[^>]*?i=\"\\d*\"[^/>]*?/>)";

    /**
     * Constructor.
     * 
     * @param sourceTuv
     *            The tuv that need to leverage.
     * @param tu
     *            The matched tu. Please make sure the score is 100.
     */
    public PlaceholderHander(SegmentTmTuv sourceTuv, SegmentTmTu tu, long saveTmId)
    {
        Assert.assertNotNull(sourceTuv, "source tuv");
        Assert.assertNotNull(tu, "matched tu");

        this.sourceTuv = sourceTuv;
        this.tu = tu;
        this.saveTmId = saveTmId;
    }

    /**
     * Repairs matched tu if it can be repaired.
     */
    public void repair()
    {
        if (repairable())
        {
            buildReplaceRule();
            String segment = tu.getSourceTuv().getSegment();
            repair(tu.getTuvs());
            updateDatabase(segment);
        }
    }

    /**
     * Updates tu imported from worldserver.
     * 
     * @param segment
     *            The segment that havn't been repaired.
     */
    private void updateDatabase(String segment)
    {
        // Only tu from storage tm can be repaired.
        if (saveTmId == tu.getTmId())
        {
            ProjectTmTuT pTu = HibernateUtil.get(ProjectTmTuT.class, tu.getId());
            if (pTu != null
                    && segment.equals(pTu.getSourceTuv().getSegmentString()))
            {
                repair(pTu.getTuvs());
            }
        }
    }

    /**
     * Try to figure out the tu can be repaired or not. The score of the
     * leveraged tu is 100, so only need to check the size and position of
     * placeholders.
     * 
     * @return
     */
    private boolean repairable()
    {
        if (!tu.isFromWorldServer())
        {
            return false;
        }

        // get exact match string
        String sourceString = sourceTuv.getSegmentNoTopTag();
        String matchString = tu.getSourceTuv().getSegmentNoTopTag();

        // normalize whitespace
        sourceString = Text.normalizeWhiteSpaceForTm(" " + sourceString + " ");
        matchString = Text.normalizeWhiteSpaceForTm(" " + matchString + " ");

        List<String> sTags = parse(sourceString, PT_REGEX);
        List<String> mTags = parse(matchString, PH_REGEX);

        for (String tag : sTags)
        {
            sourceString = sourceString.replace(tag, "*");
        }

        for (String tag : mTags)
        {
            matchString = matchString.replace(tag, "*");
        }

        if (!sourceString.equalsIgnoreCase(matchString))
        {
            return false;
        }

        return true;
    }

    /**
     * The target tuv can be repaired only if no placeholder is add or lost.
     * 
     * @param trgtags
     *            The placeholders that included in target tuv.
     * @return ture or false.
     */
    private boolean repairable(List<String> trgtags)
    {
        return srcTags.size() == trgtags.size() && trgtags.containsAll(srcTags);
    }

    /**
     * Repairs all target segments.
     * 
     * <p>
     * For each segment, picks up all placeholders and compare with placeholders
     * which were picked up from source segment. If the target segment is
     * repairable, replace placeholders with source segment placeholders
     * according to the order.
     * 
     * @param trgtags
     */
    private void repair(List<BaseTmTuv> tuvs)
    {
        for (BaseTmTuv tuv : tuvs)
        {
            List<String> tags = parse(tuv, PH_REGEX);
            if (repairable(tags))
            {
                String segment = tuv.getSegment();
                for (String tag : tags)
                {
                    segment = segment.replace(tag, replaceRules.get(tag));
                }
                tuv.setSegment(segment);
            }
        }
    }

    /**
     * Repairs all tuvs.
     * 
     * <p>
     * For each segment, picks up all placeholders and compare with placeholders
     * which were picked up from source segment. If the target segment is
     * repairable, replace placeholders with source segment placeholders
     * according to the order.
     * 
     * @param trgtags
     */
    private void repair(Set<ProjectTmTuvT> tuvs)
    {
        for (ProjectTmTuvT tuv : tuvs)
        {
            List<String> tags = parse(tuv.getSegmentString(), PH_REGEX);
            if (repairable(tags))
            {
                String segment = tuv.getSegmentString();
                for (String tag : tags)
                {
                    segment = segment.replace(tag, replaceRules.get(tag));
                }
                tuv.setSegmentString(segment);

                HibernateUtil.saveOrUpdate(tuv);

                ProjectTmTuT tu = tuv.getTu();
                if (tu.isFromWorldServer())
                {
                    tu.setFromWorldServer(false);
                    HibernateUtil.saveOrUpdate(tu);
                }
            }
        }
    }

    /**
     * Gets all placeholders included in the tuv.
     * 
     * @param tuv
     * @param regex
     * @return
     */
    private List<String> parse(BaseTmTuv tuv, String regex)
    {
        return parse(tuv.getSegment(), regex);
    }

    /**
     * Gets all placeholders included in the tuv.
     * 
     * @param tuvSegment
     * @param regex
     * @return
     */
    private List<String> parse(String tuvSegment, String regex)
    {
        List<String> tags = new ArrayList<String>();

        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(tuvSegment);

        while (match.find())
        {
            tags.add(match.group());
        }

        return tags;
    }

    /**
     * Gets all placeholders included in the source tuv.
     */
    private void buildReplaceRule()
    {
        sourceTags = parse(sourceTuv, PT_REGEX);
        srcTags = parse(tu.getSourceTuv(), PH_REGEX);

        Assert.assertTrue(sourceTags.size() == srcTags.size(),
                "The matched tu can not be repaired");

        replaceRules = new HashMap<String, String>();

        for (int i = 0; i < sourceTags.size(); i++)
        {
            replaceRules.put(srcTags.get(i), sourceTags.get(i));
        }
    }
}
