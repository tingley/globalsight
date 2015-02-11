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
package test.globalsight.ling.tw;

/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

import com.globalsight.ling.tw.HtmlTableWriter;
import java.util.Hashtable;
import java.util.Locale;
import junit.framework.*;


/**
 * Insert the type's description here.
 * Creation date: (11/21/2000 10:34:28 AM)
 * @author: 
 */
public class HtmlTableWriterTest extends TestCase 
{              
    
            Hashtable m_hVerbose_EN = new Hashtable();
    Hashtable m_hCompact_EN = new Hashtable();
    String m_sortedTableCompact = "";
    String m_sortedRowsCompact = "";
    String[] m_sortedKeysCompact = null;
    String m_sortedTableVerbose = "";
    String m_sortedRowsVerbose = "";
    String[] m_sortedKeysVerbose = null;

    /**
    */
    public HtmlTableWriterTest(String p_name)
    {
        super(p_name);
    }

    /**
    */
    public void setUp()
    {
        // make test hash
        m_hVerbose_EN = new Hashtable();
        m_hVerbose_EN.put("[link1]", "Start a link");
        m_hVerbose_EN.put("[/link1]", "End a link");
        m_hVerbose_EN.put("[linebreak2]", "Start a line break");
        m_hVerbose_EN.put("[bold]", "Start a bold");
        m_hVerbose_EN.put("[/bold]", "End a bold");
        m_hVerbose_EN.put("[underline]", "Start an underline");
        m_hVerbose_EN.put("[/underline]", "End an underline");
        m_hVerbose_EN.put("[italic]", "Start an italic");
        m_hVerbose_EN.put("[/italic]", "End an italic");
        m_hVerbose_EN.put("[tab6]", "a tab");
        m_hVerbose_EN.put("[formfeed7]", "a FF");
    
        m_hCompact_EN = new Hashtable();
        m_hCompact_EN.put("[l1]", "Start a link");
        m_hCompact_EN.put("[/l1]", "End a link");
        m_hCompact_EN.put("[lb2]", "Start a line break");
        m_hCompact_EN.put("[b]", "Start a bold");
        m_hCompact_EN.put("[/b]", "End a bold");
        m_hCompact_EN.put("[u]", "Start an underline");
        m_hCompact_EN.put("[/u]", "End an underline");
        m_hCompact_EN.put("[i]", "Start an italic");
        m_hCompact_EN.put("[/i]", "End an italic");
        m_hCompact_EN.put("[t6]", "a tab");
        m_hCompact_EN.put("[ff7]", "a FF");
    
        m_sortedTableCompact = "<table border=\"\"><tr><td>[b]</td><td>Start a bold</td></tr><tr><td>[/b]</td><td>End a bold</td></tr><tr><td>[ff7]</td><td>a FF</td></tr><tr><td>[i]</td><td>Start an italic</td></tr><tr><td>[/i]</td><td>End an italic</td></tr><tr><td>[l1]</td><td>Start a link</td></tr><tr><td>[/l1]</td><td>End a link</td></tr><tr><td>[lb2]</td><td>Start a line break</td></tr><tr><td>[t6]</td><td>a tab</td></tr><tr><td>[u]</td><td>Start an underline</td></tr><tr><td>[/u]</td><td>End an underline</td></tr></table>";
        m_sortedRowsCompact = "<tr><td>[b]</td><td>Start a bold</td></tr><tr><td>[/b]</td><td>End a bold</td></tr><tr><td>[ff7]</td><td>a FF</td></tr><tr><td>[i]</td><td>Start an italic</td></tr><tr><td>[/i]</td><td>End an italic</td></tr><tr><td>[l1]</td><td>Start a link</td></tr><tr><td>[/l1]</td><td>End a link</td></tr><tr><td>[lb2]</td><td>Start a line break</td></tr><tr><td>[t6]</td><td>a tab</td></tr><tr><td>[u]</td><td>Start an underline</td></tr><tr><td>[/u]</td><td>End an underline</td></tr>";
        m_sortedKeysCompact = new String[]{"[b]","[/b]","[ff7]","[i]","[/i]","[l1]","[/l1]","[lb2]","[t6]","[u]","[/u]"};
            
        m_sortedTableVerbose = "<table border=\"\"><tr><td>[bold]</td><td>Start a bold</td></tr><tr><td>[/bold]</td><td>End a bold</td></tr><tr><td>[formfeed7]</td><td>a FF</td></tr><tr><td>[italic]</td><td>Start an italic</td></tr><tr><td>[/italic]</td><td>End an italic</td></tr><tr><td>[linebreak2]</td><td>Start a line break</td></tr><tr><td>[link1]</td><td>Start a link</td></tr><tr><td>[/link1]</td><td>End a link</td></tr><tr><td>[tab6]</td><td>a tab</td></tr><tr><td>[underline]</td><td>Start an underline</td></tr><tr><td>[/underline]</td><td>End an underline</td></tr></table>";
        m_sortedRowsVerbose = "<tr><td>[bold]</td><td>Start a bold</td></tr><tr><td>[/bold]</td><td>End a bold</td></tr><tr><td>[formfeed7]</td><td>a FF</td></tr><tr><td>[italic]</td><td>Start an italic</td></tr><tr><td>[/italic]</td><td>End an italic</td></tr><tr><td>[linebreak2]</td><td>Start a line break</td></tr><tr><td>[link1]</td><td>Start a link</td></tr><tr><td>[/link1]</td><td>End a link</td></tr><tr><td>[tab6]</td><td>a tab</td></tr><tr><td>[underline]</td><td>Start an underline</td></tr><tr><td>[/underline]</td><td>End an underline</td></tr>";
        m_sortedKeysVerbose = new String[]{"[bold]","[/bold]","[formfeed7]","[italic]","[/italic]","[linebreak2]","[link1]","[/link1]","[tab6]","[underline]","[/underline]"};
    }

    /**
    * Insert the method's description here.
    * Creation date: (8/16/2000 10:40:43 AM)
    */
    public static Test suite()
    {
        return new TestSuite(HtmlTableWriterTest.class);
    }

    public void testSortedKeysVerbose()
    {
        HtmlTableWriter tw = new HtmlTableWriter();
        String s[] = null;
        
        s = tw.getSortedPtagKeys(m_hVerbose_EN);
        for( int i=0; i < m_sortedKeysVerbose.length; i++ )
        {
            assertEquals("Verbose sorted keys failed", m_sortedKeysVerbose[i], (s[i]) ) ;			
        }
    
        s = tw.getSortedPtagKeys(m_hVerbose_EN, Locale.US);
        for( int i=0; i < m_sortedKeysVerbose.length; i++ )
        {
            assertEquals("Verbose sorted keys failed", m_sortedKeysVerbose[i], (s[i]) ) ;			
        }
    
    }

    public void testSortedKeysCompact()
    {
        
        HtmlTableWriter tw = new HtmlTableWriter();
        String s[] = null;
        
        s = tw.getSortedPtagKeys(m_hCompact_EN);
        for( int i=0; i < m_sortedKeysCompact.length; i++ )
        {
            assertEquals("Compact sorted keys failed", m_sortedKeysCompact[i], (s[i]) ) ;			
        }
    
        s = tw.getSortedPtagKeys(m_hCompact_EN, Locale.US);
        for( int i=0; i < m_sortedKeysCompact.length; i++ )
        {
            assertEquals("Compact sorted keys failed", m_sortedKeysCompact[i], (s[i]) ) ;			
        }
    
    
    }

    public void testSortedRowsVerbose()
    {
        HtmlTableWriter tw = new HtmlTableWriter();
        String s = tw.getSortedHtmlRows(m_hVerbose_EN);
        assertEquals("Compact rows failed", m_sortedRowsVerbose, (s) );
    }

    public void testSortedTableVerbose()
    {
        HtmlTableWriter tw = new HtmlTableWriter();
        String s = tw.getSortedHtmlTable(m_hVerbose_EN);
        assertEquals("Verbose table failed", m_sortedTableVerbose, (s) );
    }

    public void testSortedRowsCompact()
    {
        HtmlTableWriter tw = new HtmlTableWriter();
        String s = tw.getSortedHtmlRows(m_hCompact_EN);
        assertEquals("Compact rows failed", m_sortedRowsCompact, (s) );
    }

    public void testSortedTableCompact()
    {
        HtmlTableWriter tw = new HtmlTableWriter();
        String s = tw.getSortedHtmlTable(m_hCompact_EN);
        assertEquals("Compact table failed", m_sortedTableCompact, (s) );
    }
	
}
