/*
 * Created on Jun 23, 2003
 *
 */
package org.getopt.luke;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import org.apache.lucene.analysis.*;
//import org.apache.lucene.analysis.de.GermanAnalyzer;
//import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import com.globalsight.ling.lucene.analysis.ar.ArabicGlossAnalyzer;
import com.globalsight.ling.lucene.analysis.ar.ArabicStemAnalyzer;
import com.globalsight.ling.lucene.analysis.cjk.CJKAnalyzer;
import com.globalsight.ling.lucene.analysis.cn.ChineseAnalyzer;
import com.globalsight.ling.lucene.analysis.cz.CzechAnalyzer;
import com.globalsight.ling.lucene.analysis.da.DanishAnalyzer;
import com.globalsight.ling.lucene.analysis.de.GermanAnalyzer;
import com.globalsight.ling.lucene.analysis.es.SpanishAnalyzer;
import com.globalsight.ling.lucene.analysis.fi.FinnishAnalyzer;
import com.globalsight.ling.lucene.analysis.fr.FrenchAnalyzer;
import com.globalsight.ling.lucene.analysis.it.ItalianAnalyzer;
import com.globalsight.ling.lucene.analysis.nl.DutchAnalyzer;
import com.globalsight.ling.lucene.analysis.no.NorwegianAnalyzer;
import com.globalsight.ling.lucene.analysis.pl.PolishAnalyzer;
import com.globalsight.ling.lucene.analysis.pt.PortugueseAnalyzer;
import com.globalsight.ling.lucene.analysis.pt_br.BrazilianAnalyzer;
import com.globalsight.ling.lucene.analysis.ru.RussianAnalyzer;
import com.globalsight.ling.lucene.analysis.sv.SwedishAnalyzer;

import com.globalsight.ling.lucene.analysis.ngram.NgramAnalyzer;
import com.globalsight.ling.lucene.analysis.ngram.NgramNoPunctuationAnalyzer;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;

import thinlet.FrameLauncher;
import thinlet.Thinlet;

/**
 * This class allows you to browse a <a href="jakarta.apache.org/lucene">Lucene</a>
 * index in several ways - by document, by term, by query, and by most frequent terms.
 *
 * @author Andrzej Bialecki <ab@getopt.org>
 *
 */
public class Luke
    extends Thinlet
    implements ClipboardOwner
{
    private FSDirectory dir = null;
    private String pName = null;
    private IndexReader ir = null;
    private Collection fn = null;
    private String[] idxFields = null;
    private ArrayList plugins = new ArrayList();
    private Object error = null;
    private Object statmsg = null;
    private Analyzer stdAnalyzer = new StandardAnalyzer();
    private Analyzer analyzer = null;
    private QueryParser qp = null;
    private boolean readOnly = false;
    private boolean useCompound = false;
    private int numTerms = 0;
    private Class[] analyzers = null;

    private Class[] defaultAnalyzers =
        {
        NgramAnalyzer.class,
        StandardAnalyzer.class,

        NgramNoPunctuationAnalyzer.class,
        SimpleAnalyzer.class,
        StopAnalyzer.class,
        WhitespaceAnalyzer.class,

        ArabicGlossAnalyzer.class,
        ArabicStemAnalyzer.class,
        CJKAnalyzer.class,
        ChineseAnalyzer.class,
        CzechAnalyzer.class,
        DanishAnalyzer.class,
        GermanAnalyzer.class,
        SpanishAnalyzer.class,
        FinnishAnalyzer.class,
        FrenchAnalyzer.class,
        ItalianAnalyzer.class,
        DutchAnalyzer.class,
        NorwegianAnalyzer.class,
        PolishAnalyzer.class,
        PortugueseAnalyzer.class,
        BrazilianAnalyzer.class,
        RussianAnalyzer.class,
        SwedishAnalyzer.class,
        };

    private static final String MSG_NOINDEX =
        "FAILED: No index, or index is closed. Reopen it.";
    private static final String MSG_READONLY = "FAILED: Read-Only index.";

    public Luke()
    {
        super();
        Prefs.load();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {};
        setColors(0xece9d8, 0x000000, 0xf5f4f0, 0x919b9a, 0xb0b0b0, 0xededed, 0xb9b9b9, 0xff899a, 0xc5c5dd);
        addComponent(this, "/xml/luke.xml", null, null);
        error = addComponent(null, "/xml/error.xml", null, null);
        statmsg = find("statmsg");

        /* CvdL: too expensive in a large classpath

        // populate analyzers
        try
        {
            analyzers =
                ClassFinder.getInstantiableSubclasses(Analyzer.class);
            if (analyzers == null || analyzers.length == 0)
                analyzers = defaultAnalyzers;
            Object cbType = find("cbType");

            populateAnalyzers(cbType);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        */

        // CvdL: use the hardcoded list
        analyzers = defaultAnalyzers;
        Object cbType = find("cbType");
        populateAnalyzers(cbType);

        loadPlugins();
        actionOpen();
    }

    public void populateAnalyzers(Object combo)
    {
        removeAll(combo);

        String[] aNames = new String[analyzers.length];
        for (int i = 0; i < analyzers.length; i++)
        {
            aNames[i] = analyzers[i].getName();
        }

        Arrays.sort(aNames);

        for (int i = 0; i < aNames.length; i++) {
            Object choice = create("choice");
            setString(choice, "text", aNames[i]);
            add(combo, choice);
        }
    }

    public Class[] getAnalyzers() {
        return analyzers;
    }

    private void loadPlugins()
    {
        ArrayList pluginClasses = new ArrayList();

        /* CvdL: too expensive in a large classpath

        // try to find all plugins
        try
        {
            Class classes[] =
                ClassFinder.getInstantiableSubclasses(LukePlugin.class);

            if (classes != null && classes.length > 0)
            {
                pluginClasses.addAll(Arrays.asList(classes));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // load plugins declared in the ".plugins" file
        try
        {
            InputStream is = getClass().getResourceAsStream("/.plugins");

            if (is != null)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = br.readLine()) != null)
                {
                    if (line.startsWith("#")) continue;
                    if (line.trim().equals("")) continue;

                    try
                    {
                        Class clazz = Class.forName(line);
                        if (clazz.getSuperclass().equals(LukePlugin.class)
                            && !pluginClasses.contains(clazz))
                        {
                            pluginClasses.add(clazz);
                        }
                    }
                    catch (Exception x)
                    {
                        //
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        */

        // CvdL: Hardcode the one plugin that we know exists.

        pluginClasses.add(org.getopt.luke.plugins.AnalyzerToolPlugin.class);

        try
        {
            for (int i = 0; i < pluginClasses.size(); i++)
            {
                try
                {
                    LukePlugin plugin =
                        (LukePlugin)((Class)pluginClasses.get(i)).
                        getConstructor(new Class[0]).
                        newInstance(new Object[0]);

                    String xul = plugin.getXULName();
                    if (xul == null) continue;
                    Object ui = parse(xul, plugin);
                    plugin.setApplication(this);
                    plugin.setMyUi(ui);
                    plugins.add(plugin);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (plugins.size() == 0) return;

        initPlugins();
    }

    public void addPluginTab(Object tabs, LukePlugin plugin) {
        Object tab = create("tab");
        setColor(tab, "foreground", new Color(0x006000));
        setString(tab, "text", plugin.getPluginName());
        setFont(tab, getFont().deriveFont(Font.BOLD));
        add(tabs, tab);
        Object panel = create("panel");
        setInteger(panel, "gap", 2);
        setInteger(panel, "weightx", 1);
        setInteger(panel, "weighty", 1);
        setChoice(panel, "halign", "fill");
        setChoice(panel, "valign", "fill");
        setInteger(panel, "columns", 1);
        add(tab, panel);
        Object infobar = create("panel");
        setInteger(infobar, "gap", 8);
        setInteger(infobar, "top", 2);
        setInteger(infobar, "bottom", 2);
        setInteger(infobar, "weightx", 1);
        setChoice(infobar, "halign", "fill");
        setColor(infobar, "background", new Color(0xc0f0c0));
        add(panel, infobar);
        Object label = create("label");
        setString(label, "text", plugin.getPluginInfo());
        add(infobar, label);
        Object link = create("button");
        setChoice(link, "type", "link");
        setString(link, "text", plugin.getPluginHome());
        putProperty(link, "url", plugin.getPluginHome());
        setMethod(link, "action", "goUrl(this)", infobar, this);
        add(infobar, link);
        add(panel, create("separator"));
        add(panel, plugin.getMyUi());
    }

    private void showStatus(final String msg) {
        Thread thr = new Thread() {
                public void run() {
                    setString(statmsg, "text", msg);
                    try {
                        sleep(5000);
                    } catch (Exception e) {};
                    setString(statmsg, "text", "");
                }
            };
        thr.start();
    }

    public Object addComponent(Object parent, String compView, String handlerStr, Object[] argv) {
        Object res = null;
        Object handler = null;
        try {
            if (handlerStr != null) {
                if (argv == null) {
                    handler = Class.forName(handlerStr)
                        .getConstructor(new Class[] { Thinlet.class })
                        .newInstance(new Object[] { this });
                } else {
                    handler = Class.forName(handlerStr)
                        .getConstructor(new Class[] { Thinlet.class, Object[].class})
                        .newInstance(new Object[] { this, argv});
                }
            }
            if (handler != null) {
                res = parse(compView, handler);
            } else
                res = parse(compView);
            if (parent != null) {
                if (parent instanceof Thinlet)
                    add(res);
                else
                    add(parent, res);
            }
            return res;
        } catch (Exception exc) {
            exc.printStackTrace();
            errorMsg(exc.getMessage());
            return null;
        }
    }

    public void errorMsg(String msg) {
        System.out.println(msg);
        Object fMsg = find(error, "msg");
        setString(fMsg, "text", msg);
        add(error);
    }

    public void actionOpen() {
        addComponent(this, "/xml/lukeinit.xml", null, null);
    }

    public void openBrowse(Object path) {
        JFileChooser fd = new JFileChooser();
        fd.setDialogType(JFileChooser.OPEN_DIALOG);
        fd.setDialogTitle("Select Index directory");
        fd.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fd.setFileHidingEnabled(false);
        int res = fd.showOpenDialog(this);
        File iDir = null;
        if (res == JFileChooser.APPROVE_OPTION) iDir = fd.getSelectedFile();
        if (iDir != null && iDir.exists()) {
            if (!iDir.isDirectory()) iDir = iDir.getParentFile();
            setString(path, "text", iDir.toString());
        }
    }
    public void setupInit(Object dialog) {
        Object path = find(dialog, "path");
        syncMRU(path);
    }

    public void openOk(Object dialog) {
        Object path = find(dialog, "path");
        pName = getString(path, "text").trim();

        boolean force = getBoolean(find(dialog, "force"), "selected");
        if (pName == null || pName.trim().equals("") || !IndexReader.indexExists(pName)) {
            errorMsg("Invalid path, or not a Lucene index.");
            return;
        }
        readOnly = getBoolean(find(dialog, "ro"), "selected");
        remove(dialog);
        removeAll();
        addComponent(this, "/xml/luke.xml", null, null);
        if (dir != null) {
            try {
                if (ir != null) ir.close();
            } catch(Exception e) {};
            try {
                dir.close();
            } catch(Exception e) {};
        }
        try {
            Prefs.addToMruList(pName);
            syncMRU(path);

            dir = FSDirectory.getDirectory(pName, false);
            if (IndexReader.isLocked(dir)) {
                if (readOnly) {
                    showStatus("Index is locked and Read-Only. Open for read-write and 'Force unlock'.");
                    dir.close();
                    return;
                }
                if (force) {
                    IndexReader.unlock(dir);
                } else {
                    showStatus("Index is locked. Try 'Force unlock' when opening.");
                    dir.close();
                    return;
                }
            }
            showFiles(dir);
            Object fileSize = find("iFileSize");
            BigInteger totalFileSize = calcTotalFileSize(pName, dir);
            setString(fileSize, "text", normalizeSize(totalFileSize) + normalizeUnit(totalFileSize));

            System.out.println(dir);
            ir = IndexReader.open(dir);
            initOverview();
            initPlugins();
            showStatus("Index successfully open.");
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg(e.getMessage());
            return;
        }
    }

    private void initPlugins() {
        Object pluginsTabs = find("pluginsTabs");
        removeAll(pluginsTabs);
        for (int i = 0; i < plugins.size(); i++) {
            LukePlugin plugin = (LukePlugin)plugins.get(i);
            addPluginTab(pluginsTabs, plugin);
            plugin.setDirectory(dir);
            plugin.setIndexReader(ir);
            try {
                plugin.init();
            } catch (Exception e) {
                e.printStackTrace();
                showStatus("PLUGIN ERROR: " + e.getMessage());
            }
        }
    }

    private void initOverview() {
        try {
            Object cbType = find("cbType");
            populateAnalyzers(cbType);
            // XXX this needs to be somewhere else
            Object cbUseCompound = find("cbUseCompound");
            setBoolean(cbUseCompound, "selected", Prefs.getBoolean(Prefs.P_USE_COMPOUND, true));

            Object pOver = find("pOver");
            Object iName = find("idx");
            setString(iName, "text", pName + (readOnly? " (R)" : ""));
            iName = find(pOver, "iName");
            setString(iName, "text", pName + (readOnly? " (Read-Only)" : ""));
            Object iDocs = find(pOver, "iDocs");
            String numdocs = String.valueOf(ir.numDocs());
            setString(iDocs, "text", numdocs);
            iDocs = find("iDocs1");
            setString(iDocs, "text", String.valueOf(ir.numDocs() - 1));
            Object iFields = find(pOver, "iFields");
            fn = ir.getFieldNames();
            idxFields = new String[fn.size()];
            setString(iFields, "text", String.valueOf(fn.size()));
            Object fList = find(pOver, "fList");
            Object defFld = find("defFld");
            Object fCombo = find("fCombo");
            java.util.Iterator it = fn.iterator();
            int i = 0;
            removeAll(fList);
            removeAll(fCombo);

            while (it.hasNext()) {
                Object item = create("item");
                add(fList, item);
                String name = (String)it.next();
                setString(item, "text", "<" + name + ">");
                putProperty(item, "fName", name);
                item = create("choice");
                add(defFld, item);
                setString(item, "text", name);
                item = create("choice");
                add(fCombo, item);
                setString(item, "text", name);
                putProperty(item, "fName", name);
                idxFields[i++] = name;
            }
            setString(defFld, "text", idxFields[0]);
            Object iTerms = find(pOver, "iTerms");
            TermEnum te = ir.terms();
            numTerms = 0;
            while (te.next()) numTerms++;
            te.close();
            setString(iTerms, "text", String.valueOf(numTerms));
            Object iMod = find(pOver, "iMod");
            setString(iMod, "text", new java.util.Date(IndexReader.lastModified(dir)).toString());
            Object iVer = find(pOver, "iVer");
            setString(iVer, "text", String.valueOf(ir.getCurrentVersion(dir)));
            Object iDel = find(pOver, "iDel");
            setString(iDel, "text", ir.hasDeletions()? "Yes" : "No");
            setString(find("defFld"), "text", idxFields[0]);
            // Remove columns
            Object header = get(find("sTable"), "header");
            Object col = get(header, ":comp");  // rank
            col = get(col, ":next");            // id
            Object tail = col;
            Object next = get(col, ":next");
            while (next != null) {
                set (col, ":next", null);
                col = next;
                next = get(col, ":next");
            }
            for (int j = 0; j < idxFields.length; j++) {
                Object c = create("column");
                setString(c, "text", idxFields[j]);
                set(tail, ":next", c);
                tail = c;
            }
            actionTopTerms(find("nTerms"));
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg(e.getMessage());
        }
    }

    private String SEP_CHAR = System.getProperty("file.separator");
    public void showFiles(FSDirectory dir) throws Exception {
        String[] files = dir.list();
        Object filesTable = find("filesTable");
        removeAll( filesTable );
        for(int i = 0; i < files.length; i++) {
            String filename;
            if(pName.endsWith(SEP_CHAR)) {
                filename = pName;
            } else {
                filename = pName + SEP_CHAR;
            }
            File file = new File(filename + files[i]);
            Object row = create("row");
            Object nameCell = create("cell");
            setString(nameCell, "text", files[i]);
            add(row, nameCell);
            Object sizeCell = create("cell");
            setString(sizeCell, "text", normalizeSize(file.length()));
            add(row, sizeCell);
            Object unitCell = create("cell");
            setString(unitCell, "text", normalizeUnit(file.length()));
            add(row, unitCell);
            add(filesTable, row);
        }
    }

    private void syncMRU(Object path) {
        removeAll(path);
        for (Iterator iter = Prefs.getMruList().iterator(); iter.hasNext(); ) {
            String element = (String) iter.next();
            Object choice = create("choice");
            setString(choice, "text", element);
            add(path, choice);
        }
    }

    private String normalizeUnit(long len) {
        if(len == 1) {
            return " byte";
        } else if(len < 1024) {
            return " bytes";
        } else if(len < 51200000) {
            return " kB";
        } else {
            return " MB";
        }
    }

    private String normalizeUnit(BigInteger len) {
        if(len.compareTo(new BigInteger("1")) == 0)  {
            return " byte";
        } else if(len.compareTo(new BigInteger("1024")) == -1) {
            return " bytes";
        } else if(len.compareTo(new BigInteger("51200000")) == -1) {
            return " kB";
        } else {
            return " MB";
        }
    }

    private String normalizeSize(BigInteger len) {
        if(len.compareTo(new BigInteger("1")) == 0)  {
            return len.toString();
        } else if(len.compareTo(new BigInteger("1024")) == -1) {
            return len.toString();
        } else if(len.compareTo(new BigInteger("51200000")) == -1) {
            return len.divide(new BigInteger("1024")).toString();
        } else {
            return len.divide(new BigInteger("102400")).toString();
        }
    }

    private BigInteger calcTotalFileSize(String path, FSDirectory fsdir) {
        BigInteger totalFileSize = new BigInteger("0");
        try {
            String[] files;
            files = fsdir.list();
            for(int i = 0; i < files.length; i++) {
                String filename;
                if(pName.endsWith(SEP_CHAR)) {
                    filename = pName;
                } else {
                    filename = pName + SEP_CHAR;
                }

                File file = new File(filename + files[i]);
                totalFileSize = totalFileSize.add(new BigInteger(file.length() + ""));
            }
        } catch (IOException e) {
            // swallow .. degrade gracefully
        }

        return totalFileSize;

    }
    private String normalizeSize(long len) {
        if(len == 1) {
            return len + "";
        } else if(len < 1024) {
            return len + "";
        } else if(len < 51200000) {
            return (len / 1024) + "";
        } else {
            return (len / 102400) + "";
        }
    }



    public void actionTopTerms(Object nTerms) {
        String sndoc = getString(nTerms, "text");
        int ndoc = 50;
        try {
            ndoc = Integer.parseInt(sndoc);
        } catch (Exception e) {};
        System.out.println("nTerms=" + ndoc);
        Object[] fields = getSelectedItems(find("fList"));
        String[] flds = null;
        if (fields == null || fields.length == 0) {
            flds = idxFields;
        } else {
            flds = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                flds[i] = (String)getProperty(fields[i], "fName");
                System.out.println(" - " + flds[i]);
            }
        }
        try {
            TermInfo[] tis = HighFreqTerms.getHighFreqTerms(dir, null, ndoc, flds);
            Object table = find("tTable");
            removeAll(table);
            if (tis == null || tis.length == 0) {
                System.out.println("tis=" + tis + " (no results)");
                Object row = create("row");
                Object cell = create("cell");
                add(row, cell);
                cell = create("cell");
                add(row, cell);
                cell = create("cell");
                add(row, cell);
                cell = create("cell");
                setBoolean(cell, "enabled", false);
                setString(cell, "text", "No Results");
                add(row, cell);
                add(table, row);
                return;
            }
            for (int i = 0; i < tis.length; i++) {
                Object row = create("row");
                add(table, row);
                putProperty(row, "term", tis[i].term);
                Object cell = create("cell");
                setChoice(cell, "alignment", "right");
                setString(cell, "text", String.valueOf(i + 1));
                add(row, cell);
                cell = create("cell");
                setChoice(cell, "alignment", "right");
                setString(cell, "text", String.valueOf(tis[i].docFreq) + "  ");
                add(row, cell);
                cell = create("cell");
                setString(cell, "text", " <" + tis[i].term.field() + "> ");
                add(row, cell);
                cell = create("cell");
                setString(cell, "text", "  " + tis[i].term.text());
                add(row, cell);
            }
        } catch(Exception e) {
            e.printStackTrace();
            errorMsg(e.getMessage());
        }
    }

    public void browseTermDocs(Object tTable) {
        Object row = getSelectedItem(tTable);
        if (row == null) return;
        Term t = (Term)getProperty(row, "term");
        if (t == null) return;
        Object tabpane = find("maintpane");
        setInteger(tabpane, "selected", 1);
        _showTerm(find("fCombo"), find("fText"), t);
        repaint();

    }

    public void showTermDocs(Object tTable) {
        Object row = getSelectedItem(tTable);
        if (row == null) return;
        Term t = (Term)getProperty(row, "term");
        if (t == null) return;
        Object tabpane = find("maintpane");
        setInteger(tabpane, "selected", 2);
        Object qField = find("qField");
        setString(qField, "text", t.field() + ":" + t.text());
        search(qField);
        repaint();

    }

    public void actionUseCompound(Object check) {
        useCompound = getBoolean(check, "selected");
        Prefs.setProperty(Prefs.P_USE_COMPOUND, Boolean.toString(useCompound));
    }

    public void actionUndelete() {
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        if (readOnly) {
            showStatus(MSG_READONLY);
            return;
        }
        try {
            ir.undeleteAll();
            initOverview();
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg(e.getMessage());
        }
    }

    public void actionOptimize() {
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        if (readOnly) {
            showStatus(MSG_READONLY);
            return;
        }
        try {
            ir.close();
            IndexWriter iw = new IndexWriter(dir, new org.apache.lucene.analysis.WhitespaceAnalyzer(), false);
            iw.setUseCompoundFile(useCompound);
            BigInteger startSize = calcTotalFileSize(pName, dir);
            long startTime = System.currentTimeMillis();
            iw.optimize();
            long endTime = System.currentTimeMillis();
            BigInteger endSize = calcTotalFileSize(pName, dir);
            BigInteger deltaSize = startSize.subtract( endSize );
            String sign = deltaSize.compareTo(new BigInteger("0")) < 0 ? " Increased " : " Reduced ";
            String sizeMsg = sign + normalizeSize(deltaSize.abs()) + normalizeUnit(deltaSize.abs());
            String timeMsg = String.valueOf(endTime - startTime) + " ms";
            showStatus(sizeMsg + " in " + timeMsg);
            showFiles(dir);
            iw.close();
            ir = IndexReader.open(dir);
            initOverview();
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
    }

    public void showPrevDoc(Object docNum) {
        _showDoc(docNum, -1);
    }

    public void showNextDoc(Object docNum) {
        _showDoc(docNum, +1);
    }

    public void showDoc(Object docNum) {
        _showDoc(docNum, 0);
    }

    private void _showDoc(Object docNum, int incr) {
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        String num = getString(docNum, "text");
        if (num.trim().equals("")) num = String.valueOf(-incr);
        try {
            int iNum = Integer.parseInt(num);
            iNum += incr;
            if (iNum < 0 || iNum >= ir.numDocs()) {
                showStatus("Document number outside valid range.");
                return;
            }
            Document doc = null;
            if (!ir.isDeleted(iNum)) doc = ir.document(iNum);
            else showStatus("This document has been deleted and is not available.");
            _showDocFields(iNum, doc);
        } catch(Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
    }

    public void actionReconstruct(Object docTable) {
        final Integer DocNum = (Integer)getProperty(docTable, "docNum");
        if (DocNum == null) {
            showStatus("FAILED: need to select doc. first!");
            return;
        }
        final Document document = (Document)getProperty(docTable, "doc");
        final Object progress = addComponent(this, "/xml/progress.xml", null, null);
        setString(find(progress, "msg"), "text", "Collecting terms...");
        setInteger(find(progress, "bar"), "maximum", 100);
        Thread thr = new Thread() {
                public void run() {
                    int docNum = DocNum.intValue();
                    TreeMap doc = new TreeMap();
                    // get stored fields
                    Vector sf = new Vector();
                    for (int i = 0; i < idxFields.length; i++) {
                        Field[] f = document.getFields(idxFields[i]);
                        if (f == null || !f[0].isStored()) continue;
                        StringBuffer sb = new StringBuffer();
                        for (int k = 0; k < f.length; k++) {
                            if (k > 0) sb.append('\n');
                            sb.append(f[k].stringValue());
                        }
                        Field field = new Field(idxFields[i], sb.toString(), f[0].isStored(), f[0].isIndexed(), f[0].isTokenized(), f[0].isTermVectorStored());
                        field.setBoost(f[0].getBoost());
                        doc.put(idxFields[i], field);
                        sf.add(idxFields[i]);
                    }
                    String term = null;
                    GrowableStringArray terms = null;
                    try {
                        int i = 0;
                        int delta = numTerms / 100;
                        Object bar = find(progress, "bar");
                        TermEnum te = ir.terms();
                        TermPositions tp = ir.termPositions();
                        while (te.next()) {
                            if ((i++ % delta) == 0) {
                                setInteger(bar, "value", i / delta);
                            }
                            // skip stored fields
                            if (sf.contains(te.term().field())) continue;
                            tp.seek(te.term());
                            if (!tp.skipTo(docNum) || tp.doc() != docNum) {
                                // this term is not found in the doc
                                continue;
                            }
                            term = te.term().text();
                            terms = (GrowableStringArray)doc.get(te.term().field());
                            if (terms == null) {
                                terms = new GrowableStringArray();
                                doc.put(te.term().field(), terms);
                            }
                            for (int k = 0; k < tp.freq(); k++) {
                                int pos = tp.nextPosition();
                                terms.set(pos, term);
                            }
                        }
                        Object dialog = addComponent(null, "/xml/editdoc.xml", null, null);
                        putProperty(dialog, "docNum", new Integer(docNum));
                        Object cbAnalyzers = find(dialog, "cbAnalyzers");
                        populateAnalyzers(cbAnalyzers);
                        setInteger(cbAnalyzers, "selected", 0);
                        Object editTabs = find(dialog, "editTabs");
                        setString(find(dialog, "docNum"), "text", "Fields of Doc #: " + docNum);
                        for (Iterator it = doc.keySet().iterator(); it.hasNext();) {
                            String key = (String)it.next();
                            Object tab = create("tab");
                            setString(tab, "text", key);
                            setFont(tab, getFont().deriveFont(Font.BOLD));
                            add(editTabs, tab);
                            Object editfield = addComponent(tab, "/xml/editfield.xml", null, null);
                            Object t = doc.get(key);
                            Object fType = find(editfield, "fType");
                            Object fText = find(editfield, "fText");
                            Object fBoost = find(editfield, "fBoost");
                            Object cbStored = find(editfield, "cbStored");
                            Object cbIndexed = find(editfield, "cbIndexed");
                            Object cbTokenized = find(editfield, "cbTokenized");
                            Object cbTVF = find(editfield, "cbTVF");
                            if (t instanceof Field) {
                                Field f = (Field)t;
                                setString(fType, "text", "Original stored field content");
                                setString(fText, "text", f.stringValue());
                                setString(fBoost, "text", String.valueOf(f.getBoost()));
                                setBoolean(cbStored, "selected", f.isStored());
                                setBoolean(cbIndexed, "selected", f.isIndexed());
                                setBoolean(cbTokenized, "selected", f.isTokenized());
                                setBoolean(cbTVF, "selected", f.isTermVectorStored());
                            } else {
                                setString(fType, "text", "RESTORED content - check for errors!");
                                setColor(fType, "foreground", Color.red);
                                setBoolean(cbIndexed, "selected", true);
                                setString(fBoost, "text", String.valueOf(document.getBoost()));
                                terms = (GrowableStringArray)doc.get(key);
                                if (terms == null) continue;
                                StringBuffer sb = new StringBuffer();
                                String sNull = "null";
                                int k = 0, m = 0;
                                for (int j = 0; j < terms.size(); j++) {
                                    if (terms.get(j) == null) k++;
                                    else {
                                        if (sb.length() > 0) sb.append(' ');
                                        if (k > 0) {
                                            sb.append(sNull + "_" + k + " ");
                                            k = 0;
                                            m++;
                                        }
                                        sb.append(terms.get(j));
                                        m++;
                                        if (m % 10 == 0) sb.append('\n');
                                    }
                                }
                                setString(fText, "text", sb.toString());
                            }
                        }
                        add(dialog);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showStatus(e.getMessage());
                    }
                    remove(progress);
                }
            };
        thr.start();
    }

    public void actionEditAdd(Object editdoc) {
        Document doc = new Document();
        Object cbAnalyzers = find(editdoc, "cbAnalyzers");
        Analyzer a = new StandardAnalyzer();
        try {
            String clazz = getString(cbAnalyzers, "text");
            a = (Analyzer)Class.forName(clazz).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("FAILED: using StandardAnalyzer");
        }
        Object editTabs = find(editdoc, "editTabs");
        Object[] tabs = getItems(editTabs);
        for (int i = 0; i < tabs.length; i++) {
            String name = getString(tabs[i], "text");
            if (name.trim().equals("")) continue;
            Object fBoost = find(tabs[i], "fBoost");
            Object fText = find(tabs[i], "fText");
            Object cbStored = find(tabs[i], "cbStored");
            Object cbIndexed = find(tabs[i], "cbIndexed");
            Object cbTokenized = find(tabs[i], "cbTokenized");
            Object cbTVF = find(tabs[i], "cbTVF");
            String text = getString(fText, "text");
            Field f = new Field(name, text,
                getBoolean(cbStored, "selected"),
                getBoolean(cbIndexed, "selected"),
                getBoolean(cbTokenized, "selected"),
                getBoolean(cbTVF, "selected"));
            String boostS = getString(fBoost, "text").trim();
            if (!boostS.equals("") && !boostS.equals("1.0")) {
                float boost = 1.0f;
                try {
                    boost = Float.parseFloat(boostS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                f.setBoost(boost);
            }
            doc.add(f);
        }
        IndexWriter writer = null;
        try {
            ir.close();
            writer = new IndexWriter(dir, a, false);
            writer.addDocument(doc);
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg("FAILED: " + e.getMessage());
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                ir = IndexReader.open(dir);
            } catch (Exception e) {
                e.printStackTrace();
            }
            remove(editdoc);
            initOverview();
            initPlugins();
        }
    }

    public void actionEditReplace(Object editdoc) {
        actionEditAdd(editdoc);
        Integer DocNum = (Integer)getProperty(editdoc, "docNum");
        if (DocNum == null) return;
        try {
            ir.delete(DocNum.intValue());
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("ERROR deleting: " + e.getMessage());
            return;
        }
    }

    public void actionEditAddField(Object editdoc) {
        String name = getString(find(editdoc, "fNewName"), "text");
        if (name.trim().equals("")) {
            showStatus("FAILED: Field name is required.");
            return;
        }
        name = name.trim();
        Object editTabs = find(editdoc, "editTabs");
        Object tab = create("tab");
        setString(tab, "text", name);
        setFont(tab, getFont().deriveFont(Font.BOLD));
        add(editTabs, tab);
        Object editfield = addComponent(tab, "/xml/editfield.xml", null, null);
        repaint(editTabs);
    }

    public void actionEditDeleteField(Object editfield) {
        Object tab = getParent(editfield);
        remove(tab);
    }

    private void _showDocFields(int docid, Document doc) {
        Object table = find("docTable");
        setString(find("docNum"), "text", String.valueOf(docid));
        setString(find("docNum1"), "text", String.valueOf(docid));
        removeAll(table);
        putProperty(table, "doc", doc);
        putProperty(table, "docNum", new Integer(docid));
        if (doc == null) return;
        for (int i = 0; i < idxFields.length; i++) {
            Field[] fields = doc.getFields(idxFields[i]);
            if (fields == null) {
                addFieldRow(table, idxFields[i], null);
                continue;
            }
            for (int j = 0; j < fields.length; j++) {
                addFieldRow(table, idxFields[i], fields[j]);
            }
        }
        doLayout(table);
    }

    private void addFieldRow(Object table, String fName, Field f) {
        Object row = create("row");
        add(table, row);
        putProperty(row, "field", f);
        putProperty(row, "fName", fName);
        Object cell = create("cell");
        setString(cell, "text", "<" + fName + ">");
        add(row, cell);
        cell = create("cell");
        if (f != null && f.isIndexed()) setString(cell, "text", "+");
        add(row, cell);
        cell = create("cell");
        if (f != null && f.isTokenized()) setString(cell, "text", "+");
        add(row, cell);
        cell = create("cell");
        if (f != null && f.isStored()) setString(cell, "text", "+");
        add(row, cell);
        cell = create("cell");
        if (f != null && f.isTermVectorStored()) setString(cell, "text", "+");
        add(row, cell);
        cell = create("cell");
        if (f != null) setString(cell, "text", String.valueOf(f.getBoost()));
        add(row, cell);
        cell = create("cell");
        if (f != null) setString(cell, "text", f.stringValue());
        else {
            setString(cell, "text", "<not available>");
            setBoolean(cell, "enabled", false);
        }
        add(row, cell);
    }

    public void showTV(Object table) {
        Object row = getSelectedItem(table);
        if (row == null) return;
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        Integer DocId = (Integer)getProperty(table, "docNum");
        if (DocId == null) {
            showStatus("Missing Doc. Id.");
            return;
        }
        try {
            String fName = (String)getProperty(row, "fName");
            TermFreqVector tfv = ir.getTermFreqVector(DocId.intValue(), fName);
            if (tfv == null) {
                showStatus("Term Vector not available.");
                return;
            }
            Object dialog = addComponent(null, "/xml/vector.xml", null, null);
            setString(find(dialog, "fld"), "text", fName);
            Object vTable = find(dialog, "vTable");
            IntPair[] tvs = new IntPair[tfv.size()];
            String[] terms = tfv.getTerms();
            int[] freqs = tfv.getTermFrequencies();
            for (int i = 0; i < terms.length; i++) {
                IntPair ip = new IntPair(freqs[i], terms[i]);
                tvs[i] = ip;
            }
            Arrays.sort(tvs, new IntPair.PairComparator(false, true));
            for (int i = 0; i < tvs.length; i++) {
                Object r = create("row");
                add(vTable, r);
                Object cell = create("cell");
                setString(cell, "text", String.valueOf(tvs[i].cnt));
                add(r, cell);
                cell = create("cell");
                setString(cell, "text", tvs[i].text);
                add(r, cell);
            }
            add(dialog);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
    }

    public void clipCopyFields(Object table) {
        Object[] rows = getSelectedItems(table);
        if (rows == null || rows.length == 0) return;
        Document doc = (Document)getProperty(table, "doc");
        if (doc == null) return;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < rows.length; i++) {
            Field f = (Field)getProperty(rows[i], "field");
            if (f == null) continue;
            if (i > 0) sb.append('\n');
            sb.append(f.toString());
        }
        StringSelection sel = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, this);
    }

    public void clipCopyDoc(Object table) {
        Document doc = (Document)getProperty(table, "doc");
        if (doc == null) return;
        StringBuffer sb = new StringBuffer();
        Object[] rows = getItems(table);
        for (int i = 0; i < rows.length; i++) {
            Field f = (Field)getProperty(rows[i], "field");
            if (f == null) continue;
            if (i > 0) sb.append('\n');
            sb.append(f.toString());
        }
        StringSelection sel = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, this);
    }

    public void showFirstTerm(Object fCombo, Object fText) {
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        try {
            TermEnum te = ir.terms();
            te.next();
            Term t = te.term();
            _showTerm(fCombo, fText, t);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
    }

    public void showNextTerm(Object fCombo, Object fText) {
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        try {
            String text = getString(fText, "text");
            String fld = getString(fCombo, "text");
            TermEnum te = null;
            if (text == null || text.trim().equals("")) te = ir.terms();
            else te = ir.terms(new Term(fld, text));
            te.next();
            Term t = te.term();
            _showTerm(fCombo, fText, t);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
    }

    public void showTerm(Object fCombo, Object fText) {
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        try {
            String text = getString(fText, "text");
            String fld = getString(fCombo, "text");
            if (text == null || text.trim().equals("")) return;
            Term t = new Term(fld, text);
            _showTerm(fCombo, fText, t);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
    }

    private void _showTerm(Object fCombo, Object fText, Term t) {
        if (t == null) {
            showStatus("No terms?!");
            return;
        }
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        Object[] choices = getItems(fCombo);
        for (int i = 0; i < choices.length; i++) {
            if (t.field().equals(getString(choices[i], "text"))) {
                setInteger(fCombo, "selected", i);
                break;
            }
        }
        setString(fText, "text", t.text());
        putProperty(fText, "term", t);
        putProperty(fText, "td", null);
        setString(find("tdNum"), "text", "?");
        setString(find("tFreq"), "text", "?");
        Object dFreq = find("dFreq");
        try {
            int freq = ir.docFreq(t);
            setString(dFreq, "text", String.valueOf(freq));
            dFreq = find("tdMax");
            setString(dFreq, "text", String.valueOf(freq));
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
            setString(dFreq, "text", "?");
        }
    }

    public void showFirstTermDoc(Object fText) {
        Term t = (Term)getProperty(fText, "term");
        if (t == null) return;
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        try {
            TermDocs td = ir.termDocs(t);
            td.next();
            setString(find("tdNum"), "text", "1");
            putProperty(fText, "td", td);
            _showTermDoc(fText, td);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
    }

    public void showNextTermDoc(Object fText) {
        Term t = (Term)getProperty(fText, "term");
        if (t == null) return;
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        try {
            TermDocs td = (TermDocs)getProperty(fText, "td");
            if (td == null) {
                showFirstTermDoc(fText);
                return;
            }
            if (!td.next()) return;
            Object tdNum = find("tdNum");
            String sCnt = getString(tdNum, "text");
            int cnt = 1;
            try {
                cnt = Integer.parseInt(sCnt);
            } catch(Exception e) {};
            setString(tdNum, "text", String.valueOf(cnt + 1));
            _showTermDoc(fText, td);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
    }

    public void showAllTermDoc(Object fText) {
        Term t = (Term)getProperty(fText, "term");
        if (t == null) return;
        if (ir == null) {
            showStatus("MSG_NOINDEX");
            return;
        }
        Object tabpane = find("maintpane");
        setInteger(tabpane, "selected", 2);
        Object qField = find("qField");
        setString(qField, "text", t.field() + ":" + t.text());
        Object qFieldParsed = find("qFieldParsed");
        Query q = new TermQuery(t);
        setString(qFieldParsed, "text", q.toString());
        IndexSearcher is = null;
        try {
            is = new IndexSearcher(dir);
            Object sTable = find("sTable");
            removeAll(sTable);
            _search(q, is, sTable);
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg(e.getMessage());
        } finally {
            if (is != null) try {is.close();} catch (Exception e1) {};
        }
    }

    private QueryParser createQueryParser() {
        String sAnal = getString(find("cbType"), "text");
        if (sAnal.trim().equals("")) {
            sAnal = "org.apache.lucene.analysis.standard.StandardAnalyzer";
            setString(find("cbType"), "text", sAnal);
        }
        try {
            analyzer = (Analyzer)Class.forName(sAnal).getConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg("Analyzer '" + sAnal + "' error: " + e.getMessage() + ". Using StandardAnalyzer.");
            analyzer = stdAnalyzer;
        }
        String defField = getString(find("defFld"), "text");
        if (defField == null || defField.trim().equals("")) {
            defField = idxFields[0];
            setString(find("defFld"), "text", defField);
        }
        return new QueryParser(defField, analyzer);
    }

    public void showParsed() {
        QueryParser qp = createQueryParser();
        Object qField = find("qField");
        Object qFieldParsed = find("qFieldParsed");
        String queryS = getString(qField, "text");
        if (queryS.trim().equals("")) {
            setBoolean(qFieldParsed, "enabled", false);
            setString(qFieldParsed, "text", "<Empty query>");
            return;
        }
        setBoolean(qFieldParsed, "enabled", true);
        try {
            Query q = qp.parse(queryS);
            setString(qFieldParsed, "text", q.toString());
        } catch (Throwable t) {
            setString(qFieldParsed, "text", t.getMessage());
        }
    }

    public void search(Object qField) {
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        String queryS = getString(qField, "text");
        if (queryS.trim().equals("")) {
            showStatus("FAILED: Empty query.");
            return;
        }
        qp = createQueryParser();
        Object sTable = find("sTable");
        removeAll(sTable);
        IndexSearcher is = null;
        try {
            Query q = qp.parse(queryS);
            is = new IndexSearcher(dir);
            Object qFieldParsed = find("qFieldParsed");
            setString(qFieldParsed, "text", q.toString());
            _search(q, is, sTable);
        } catch (Throwable e) {
            e.printStackTrace();
            errorMsg(e.getMessage());
        } finally {
            if (is != null) try {is.close();} catch (Exception e1) {};
        }
    }

    private void _search(Query q, IndexSearcher is, Object sTable) throws Exception {
        long startTime = System.currentTimeMillis();
        Hits hits = is.search(q);
        long endTime = System.currentTimeMillis();
        showStatus( endTime - startTime + " ms");
        if (hits == null || hits.length() == 0) {
            Object row = create("row");
            Object cell = create("cell");
            add(sTable, row);
            add(row, cell);
            cell = create("cell");
            add(row, cell);
            cell = create("cell");
            setString(cell, "text", "No Results");
            setBoolean(cell, "enabled", false);
            add(row, cell);
            setString(find("resNum"), "text", "0");
            return;
        }

        setString(find("resNum"), "text", String.valueOf(hits.length()));
        for (int i = 0; i < hits.length(); i++) {
            Object row = create("row");
            Object cell = create("cell");
            add(sTable, row);
            setString(cell, "text", String.valueOf((double)Math.round((float)1000 * hits.score(i)) / 10.0));
            add(row, cell);
            cell = create("cell");
            setString(cell, "text", String.valueOf(hits.id(i)));
            add(row, cell);
            Document doc = hits.doc(i);
            putProperty(row, "docid", new Integer(hits.id(i)));
            for (int j = 0; j < idxFields.length; j++) {
                cell = create("cell");
                setString(cell, "text", doc.get(idxFields[j]));
                add(row, cell);
            }
        }
        putProperty(sTable, "query", q);
    }

    public void explainResult(Object sTable) {
        Object row = getSelectedItem(sTable);
        if (row == null) return;
        Integer docid = (Integer)getProperty(row, "docid");
        if (docid == null) return;
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        Query q = (Query)getProperty(sTable, "query");
        if (q == null) return;
        try {
            IndexSearcher is = new IndexSearcher(dir);
            Explanation expl = is.explain(q, docid.intValue());
            Object dialog = addComponent(null, "/xml/explain.xml", null, null);
            Object eTree = find(dialog, "eTree");
            addNode(eTree, expl);
            //setBoolean(eTree, "expand", true);
            add(dialog);
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg(e.getMessage());
        }
    }

    private DecimalFormat df = new DecimalFormat("0.0000");
    private void addNode(Object tree, Explanation expl) {
        Object node = create("node");
        setString(node, "text", df.format((double)expl.getValue()) + "  " + expl.getDescription());
        add(tree, node);
        if (getClass(tree) == "tree") {
            setFont(node, getFont().deriveFont(Font.BOLD));
        }
        Explanation[] kids = expl.getDetails();
        if (kids != null && kids.length > 0) {
            for (int i = 0; i < kids.length; i++) {
                addNode(node, kids[i]);
            }
        }
    }

    public void gotoDoc(Object sTable) {
        Object row = getSelectedItem(sTable);
        if (row == null) return;
        Integer docid = (Integer)getProperty(row, "docid");
        if (docid == null) return;
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        Document doc = null;
        try {
            doc = ir.document(docid.intValue());
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
            return;
        }
        _showDocFields(docid.intValue(), doc);
        Object tabpane = find("maintpane");
        setInteger(tabpane, "selected", 1);
        repaint();
    }

    private void _showTermDoc(Object fText, TermDocs td) {
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        try {
            Document doc = ir.document(td.doc());
            setString(find("docNum"), "text", String.valueOf(td.doc()));
            setString(find("tFreq"), "text", String.valueOf(td.freq()));
            _showDocFields(td.doc(), doc);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
    }

    public void deleteTermDoc(Object fText) {
        Term t = (Term)getProperty(fText, "term");
        if (t == null) return;
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        if (readOnly) {
            showStatus(MSG_READONLY);
            return;
        }
        try {
            showNextTerm(find("fCombo"), fText);
            ir.delete(t);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
        initOverview();
    }

    public void deleteDoc(Object docNum) {
        int docid = 0;
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        if (readOnly) {
            showStatus(MSG_READONLY);
            return;
        }
        try {
            docid = Integer.parseInt(getString(docNum, "text"));
            showNextDoc(docNum);
            ir.delete(docid);
            initOverview();
        } catch (Exception e) {
            showStatus(e.getMessage());
            e.printStackTrace();
        }

    }

    public void deleteDocList(Object sTable) {
        Object[] rows = getSelectedItems(sTable);
        if (rows == null || rows.length == 0) return;
        if (ir == null) {
            showStatus(MSG_NOINDEX);
            return;
        }
        if (readOnly) {
            showStatus(MSG_READONLY);
            return;
        }
        for (int i = 0; i < rows.length; i++) {
            Integer docId = (Integer)getProperty(rows[i], "docid");
            if (docId == null) continue;
            try {
                ir.delete(docId.intValue());
            } catch (Exception e) {
                continue;
            }
            remove(rows[i]);
        }
        initOverview();
    }

    public void actionAbout() {
        Object about = addComponent(this, "/xml/about.xml", null, null);
        requestFocus(find(about, "bOk"));
    }

    public boolean destroy() {
        if (ir != null) try {ir.close();} catch (Exception e) {};
        if (dir != null) try {dir.close();} catch (Exception e) {};
        try {Prefs.save();} catch (Exception e) {};
        return super.destroy();
    }

    public void actionExit() {
        destroy();
        System.exit(0);
    }

    public void goUrl(Object url) {
        String u = (String)getProperty(url, "url");
        if (u == null) return;
        try {
            BrowserLauncher.openURL(u);
        } catch (Exception e) {
            e.printStackTrace();
            showStatus(e.getMessage());
        }
    }

    public static void main(String[] args) {
        FrameLauncher f = new FrameLauncher("Luke - Lucene Index Toolbox, v 0.5 (2004-06-25)", new Luke(), 650, 450);
        f.setIconImage(Toolkit.getDefaultToolkit().createImage(Luke.class.getResource("/img/luke.gif")));
    }

    /* (non-Javadoc)
     * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
     */
    public void lostOwnership(Clipboard arg0, Transferable arg1) {

    }

}

class GrowableStringArray {
    public int INITIAL_SIZE = 20;
    private int size = 0;
    private String[] array = null;

    public int size() {
        return size;
    }

    public void set(int index, String value) {
        if (array == null) array = new String[INITIAL_SIZE];
        if (array.length < index + 1) {
            String[] newArray = new String[index + INITIAL_SIZE];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }
        if (index > size - 1) size = index + 1;
        array[index] = value;
    }

    public String get(int index) {
        return array[index];
    }
}
