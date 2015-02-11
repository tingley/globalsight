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

/**
 * 
 */
package com.globalsight.everest.edit.offline.page;

import com.globalsight.everest.edit.offline.page.terminology.GsTermHelp;
import com.globalsight.everest.edit.offline.page.terminology.HtmlTermHelp;
import com.globalsight.everest.edit.offline.page.terminology.TbxTermHelp;
import com.globalsight.everest.edit.offline.page.terminology.TradocTermHelp;
import com.globalsight.everest.edit.offline.page.terminology.TxtTermHelp;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;

/**
 * <code>TermHelpFactory</code> is a factory class, creating instance of
 * <code>TermHelp</code> according to the format argement.
 * 
 * <p>
 * The default instance is <code>GsTermHelp</code>, so if can not get
 * approprate instance, <code>GsTermHelp</code> will be return.
 */
public class TermHelpFactory
{
    public static int HTML = 0;
    public static int GLOBALSIGHT = 1;
    public static int TRADOC = 2;
    public static int TBX = 3;
    public static int TXT = 4;

    /**
     * Gets a instance of <tt>TermHelp</tt> according to the <tt>format</tt>
     * argement.
     * <p>
     * The format argement can {@link TermHelpFactory#HTML},
     * {@link TermHelpFactory#GLOBALSIGHT} or
     * {@link TermHelpFactory#GLOBALSIGHT}.
     * 
     * @param format
     *            The format argement.
     * @return The instance of <tt>TermHelp</tt>
     */
    public static TerminologyHelp newInstance(int format)
    {
        if (format == HTML)
            return new HtmlTermHelp();
        else if (format == TRADOC)
            return new TradocTermHelp();
        else if (format == TBX)
            return new TbxTermHelp();
        else if (format == TXT)
            return new TxtTermHelp();

        return new GsTermHelp();
    }

    /**
     * Gets a instance of <tt>TermHelp</tt> according to the <tt>format</tt>
     * argement.
     * <p>
     * The format argement can be {@link OfflineConstants#TERM_HTML},
     * TermHelpFactory.{@link OfflineConstants#TERM_TRADOS} or
     * {@link OfflineConstants#TERM_GLOBALSIGHT}.
     * 
     * @param format
     *            The format argement.
     * @return The instance of <tt>TermHelp</tt>
     */
    public static TerminologyHelp newInstance(String format)
    {
        if (OfflineConstants.TERM_HTML.equals(format))
            return new HtmlTermHelp();
        else if (OfflineConstants.TERM_TRADOS.equals(format))
            return new TradocTermHelp();
        else if (OfflineConstants.TERM_TBX.equals(format))
            return new TbxTermHelp();
        else if (OfflineConstants.TERM_TXT.equals(format))
            return new TxtTermHelp();

        return new GsTermHelp();
    }
}
