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
package com.globalsight.cxe.adapter.msoffice;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultText;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.everest.page.pageexport.style.StyleFactory;
import com.globalsight.everest.page.pageexport.style.StyleUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.XmlParser;

public class WordRepairer extends OfficeRepairer
{
    // static private final Logger logger = Logger
    // .getLogger(WordRepairer.class);

    public WordRepairer(String path)
    {
        super(path);
    }

    @Override
    protected boolean accept()
    {
        String docPath = path + "/word/document.xml";
        File f = new File(docPath);
        return f.exists();
    }

    private List<File> getAllFiles()
    {
        File root = new File(path + "/word");

        return FileUtil.getAllFiles(root, new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                if (pathname.isFile())
                {
                    String name = pathname.getName();
                    if (name.endsWith(".xml") || name.endsWith(".xml.rels"))
                        return true;
                }

                return false;
            }
        });
    }

    @Override
    protected void repair() throws Exception
    {
        List<File> fs = getAllFiles();

        for (File f1 : fs)
        {
            try
            {
                repairDocFiles(f1);
            }
            catch (Exception e)
            {
                // ignore it.
                // logger.error(e);
            }
        }
    }

    private static void repairDocFiles(File f) throws Exception
    {
        if (!f.exists())
            return;

        StyleUtil util = StyleFactory.getStyleUtil(StyleFactory.DOCX);
        util.updateBeforeExport(f.getAbsolutePath());

        String content = FileUtil.readFile(f, "utf-8");

        XmlParser parser = new XmlParser();
        parser.setErrorHandler(new ErrorHandler()
        {
            @Override
            public void warning(SAXParseException arg0) throws SAXException
            {
                // Do nothing.
            }

            @Override
            public void fatalError(SAXParseException arg0) throws SAXException
            {
                return;
            }

            @Override
            public void error(SAXParseException e) throws SAXException
            {
                String s = e.getMessage();
                if (s.matches("Attribute .*? was already specified for element[\\s\\S]*"))
                    return;

                throw new SAXException("XML parse error at\n  line "
                        + e.getLineNumber() + "\n  column "
                        + e.getColumnNumber() + "\n  Message:" + e.getMessage());
            }
        });

        org.dom4j.Document document = parser.parseXml(content);
        Element element = document.getRootElement();

        forHyperlinkInWr(element);
        forHyperlinkInWt(element);
        forWtNotInWr(element);
        forTextInWr(element);
        forWrInWr(element);
        forTextInWp(element);
        forNodesInWt(element);

        Writer fileWriter = new OutputStreamWriter(new FileOutputStream(f),
                "UTF-8");
        XMLWriter xmlWriter = new XMLWriter(fileWriter);
        xmlWriter.write(document);
        xmlWriter.close();

        if (content.contains("</mc:AlternateContent>"))
        {
            forAlternateContent(f);
        }
    }

    private static void forAlternateContent(File f) throws Exception
    {
        String content = FileUtil.readFile(f, "utf-8");
        int startIndex = 0;
        String startTag = "<mc:AlternateContent>";
        String endTag = "</mc:AlternateContent>";
        String startTag_2 = "<w:txbxContent>";
        String endTag_2 = "</w:txbxContent>";
        StringBuffer allContent = new StringBuffer(content);
        StringBuffer newContent = new StringBuffer(content.length());

        StringIndex si = StringIndex.getValueBetween(allContent, startIndex,
                startTag, endTag);
        while (si != null)
        {
            StringBuffer sub = new StringBuffer(si.value);
            int s = si.start;
            int e = si.end;
            String pre = allContent.substring(0, s);
            String after = allContent.substring(e);
            newContent.append(pre);

            // handle <w:txbxContent> and </w:txbxContent>
            int tmpindex = 0;
            StringIndex tmpsi = StringIndex.getValueBetween(sub, tmpindex,
                    startTag_2, endTag_2);
            if (tmpsi != null)
            {
                int s1 = tmpsi.start;
                int e1 = tmpsi.end;
                tmpindex = tmpsi.end;

                tmpsi = StringIndex.getValueBetween(sub, tmpindex, startTag_2,
                        endTag_2);

                if (tmpsi != null)
                {
                    String c2 = tmpsi.value;

                    sub.replace(s1, e1, c2);
                }
            }
            newContent.append(sub);

            allContent = new StringBuffer(after);
            si = StringIndex.getValueBetween(allContent, startIndex, startTag,
                    endTag);
        }

        newContent.append(allContent.toString());

        FileUtil.writeFile(f, newContent.toString(), "utf-8");
    }

    @SuppressWarnings("unchecked")
    private static void forHyperlinkInWr(Element element)
    {
        List<Node> ts = element.selectNodes("//w:r/w:hyperlink/w:r");

        for (Node t : ts)
        {
            Element hyperlink = t.getParent();

            if (hyperlink == null)
                continue;

            Element wr = hyperlink.getParent();
            if (wr == null)
                continue;

            Element wrParent = wr.getParent();
            if (wrParent == null)
                continue;

            boolean beforeWt = false;

            List<?> els = wr.content();

            for (Object el : els)
            {
                if (el instanceof Element)
                {
                    Element elm = (Element) el;
                    if ("t".equals(elm.getName()))
                    {
                        beforeWt = false;
                        break;
                    }
                    else if (hyperlink.equals(elm))
                    {
                        beforeWt = true;
                        break;
                    }
                }
            }

            wr.remove(hyperlink);

            @SuppressWarnings("rawtypes")
            List es = wrParent.elements();
            int index = es.indexOf(wr);
            index = beforeWt ? index : index + 1;

            hyperlink.setParent(wrParent);
            es.add(index, hyperlink);
        }
    }

    /**
     * For GBS-3085.
     */
    @SuppressWarnings("unchecked")
    private static void forHyperlinkInWt(Element element)
    {
        List<Node> ts = element.selectNodes("//w:r/w:t/w:hyperlink/w:r");

        boolean find = false;

        for (Node t : ts)
        {
            find = true;
            Element hyperlink = t.getParent();

            if (hyperlink == null)
                continue;

            Element wt = hyperlink.getParent();
            if (wt == null)
                continue;

            Element wr = wt.getParent();
            if (wr == null)
                continue;

            Element wrParent = wr.getParent();
            if (wrParent == null)
                continue;

            List wtNodes = wt.content();
            List removedNodes = new ArrayList();

            int index = wtNodes.indexOf(hyperlink);
            for (int i = wtNodes.size() - 1; i >= index; i--)
            {
                removedNodes.add(0, wtNodes.remove(i));
            }

            Element cloneWr = (Element) wr.clone();
            List es = wrParent.content();
            index = es.indexOf(wr);

            wrParent.remove(cloneWr);
            es.add(index, cloneWr);
            es.add(index + 1, hyperlink);

            wt.clearContent();
            List wts = wt.content();
            for (int i = 1; i < removedNodes.size(); i++)
            {
                wts.add(removedNodes.get(i));
            }
        }

        if (find)
        {
            forHyperlinkInWt(element);
        }
    }

    private static void forNodesInWt(Element element)
    {
        @SuppressWarnings("unchecked")
        List<Element> wts = element.selectNodes("//w:t");

        for (Element wt : wts)
        {
            @SuppressWarnings("unchecked")
            List<Element> es = wt.elements();

            if (!wt.isTextOnly())
            {
                String text = wt.getStringValue();
                for (Element e : es)
                {
                    wt.remove(e);
                }

                wt.setText(text);
            }
        }
    }

    private static void forWrInWr(Element element)
    {
        @SuppressWarnings("unchecked")
        List<Node> ts = element.selectNodes("//w:r/w:r");

        for (Node t : ts)
        {
            Element wr = t.getParent();

            if (wr == null)
                continue;

            List<?> els = wr.content();

            StringBuffer sb = new StringBuffer();
            Element wt = null;
            List<Element> wrs = new ArrayList<Element>();

            for (Object el : els)
            {
                if (el instanceof Element)
                {
                    Element elm = (Element) el;
                    if ("t".equals(elm.getName()))
                    {
                        wt = elm;
                        sb.append(elm.getStringValue());
                    }
                    else if ("r".equals(elm.getName()))
                    {
                        sb.append(elm.getStringValue());
                        wrs.add(elm);
                    }
                }
            }

            if (wt == null)
            {
                wt = wr.addElement("w:t");
                wt.addAttribute("xml:space", "preserve");
            }

            wt.setText(sb.toString());

            for (Element w : wrs)
            {
                wr.remove(w);
            }
        }
    }

    private static void forWtNotInWr(Element element)
    {
        @SuppressWarnings("unchecked")
        List<Element> wts = element.selectNodes("//w:t");

        for (Element wt : wts)
        {
            Element parent = wt.getParent();
            if (parent == null || "r".equals(parent.getName()))
                continue;

            @SuppressWarnings("unchecked")
            List<Element> es = parent.elements();

            int wtIndex = -1;
            for (Element e : es)
            {
                wtIndex++;
                if (wt.equals(e))
                {
                    break;
                }
            }

            for (int i = 1; i < es.size(); i++)
            {
                int prefix = wtIndex - i;
                int suffix = wtIndex + i;

                if (prefix < 0 && suffix > es.size() - 1)
                {
                    break;
                }

                if (prefix > -1)
                {
                    Element prefixElement = es.get(prefix);
                    if ("r".equals(prefixElement.getName()))
                    {
                        @SuppressWarnings("unchecked")
                        List<Element> preWts = prefixElement.elements("t");

                        if (preWts.size() > 0)
                        {
                            String text = wt.getStringValue();
                            Element preWt = preWts.get(preWts.size() - 1);
                            preWt.setText(preWt.getStringValue() + text);
                            parent.remove(wt);
                            break;
                        }
                    }
                }

                if (suffix < es.size())
                {
                    Element sufixElement = es.get(prefix);
                    if ("r".equals(sufixElement.getName()))
                    {
                        @SuppressWarnings("unchecked")
                        List<Element> sufWts = sufixElement.elements("t");

                        if (sufWts.size() > 0)
                        {
                            String text = wt.getStringValue();
                            Element sufWt = sufWts.get(0);
                            sufWt.setText(text + sufWt.getStringValue());
                            parent.remove(wt);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void forTextInWr(Element element)
    {
        @SuppressWarnings("unchecked")
        List<Node> ts = element.selectNodes("//w:r/text()");

        for (Node t : ts)
        {
            if (t.getText().matches("[\n\r]*"))
            {
                continue;
            }

            Element wr = t.getParent();

            if (wr == null)
            {
                continue;
            }

            List<?> els = wr.content();

            StringBuffer sb = new StringBuffer();
            Element wt = null;
            List<DefaultText> texts = new ArrayList<DefaultText>();

            for (Object el : els)
            {
                if (el instanceof DefaultText)
                {
                    DefaultText text = (DefaultText) el;
                    texts.add(text);
                    sb.append(text.getStringValue());
                }
                else if (el instanceof Element)
                {
                    Element elm = (Element) el;
                    if ("t".equals(elm.getName()))
                    {
                        wt = elm;
                        sb.append(elm.getStringValue());
                    }
                }
            }

            if (wt == null)
            {
                wt = wr.addElement("w:t");
                wt.addAttribute("xml:space", "preserve");
            }

            wt.setText(sb.toString());

            for (DefaultText text : texts)
            {
                wr.remove(text);
            }
        }
    }

    private static void forTextInWp(Element element)
    {
        @SuppressWarnings("unchecked")
        List<Node> ts = element.selectNodes("//w:p/text()");

        for (Node t : ts)
        {
            String c = t.getText();
            if (c.matches("[\n\r]*"))
            {
                continue;
            }

            Element wp = t.getParent();
            Element wr = DocumentHelper.createElement("w:r");
            wp.content().add(wp.indexOf(t), wr);
            Element wt = wr.addElement("w:t");
            wt.setText(t.getText());
            wp.remove(t);
        }
    }
}
