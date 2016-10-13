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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.globalsight.cxe.adapter.msoffice.excel.Cell;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.filterconfiguration.MSOffice2010Filter;
import com.globalsight.ling.docproc.extractor.msoffice2010.XmlUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SortUtil;

public class ExcelFileManager
{
    static private final Logger logger = Logger
            .getLogger(ExcelFileManager.class);
    private XmlUtil util = new XmlUtil();

    public void mergeSortSegments(String dir)
    {
        List<File> fs = FileUtil.getAllFiles(new File(dir, "xl/worksheets"),
                new FileFilter()
                {

                    @Override
                    public boolean accept(File pathname)
                    {
                        String name = pathname.getName();
                        return name.startsWith("sortsheet")
                                && name.endsWith(".xml");
                    }
                });

        Map<String, Node> siNodes = new HashMap<String, Node>();

        for (File f : fs)
        {
            List<Node> cs = new ArrayList<Node>();
            List<Node> oddHeader = new ArrayList<Node>();
            List<Node> oddFooter = new ArrayList<Node>();
            try
            {
                Document d = util.getDocument(f);
                List<Node> ns = util.getChildNodes(d.getDocumentElement());

                for (Node n : ns)
                {

                    if (n.getNodeType() == Node.TEXT_NODE)
                        continue;

                    String name = n.getNodeName();
                    if ("si".equals(name))
                    {
                        String index = util.getAttribute(n, "siIndex")
                                .getNodeValue();
                        siNodes.put(index, n);
                    }
                    else if ("c".equals(name))
                    {
                        cs.add(n);
                    }
                    else if ("oddHeader".equals(name))
                    {
                        oddHeader.add(n);
                    }
                    else if ("oddFooter".equals(name))
                    {
                        oddFooter.add(n);
                    }
                }

                String path = f.getParent() + "/" + f.getName().substring(4);
                Document od = util.getDocument(new File(path));
                updateC(cs, od);
                updateHeaderOrFooter(oddHeader, util.getNodes(od, "oddHeader"));
                updateHeaderOrFooter(oddFooter, util.getNodes(od, "oddFooter"));

                util.saveToFileNoFormat(od, path);
            }
            catch (Exception e)
            {
                logger.error(e);
            }

            f.delete();
        }

        try
        {
            updateSi(siNodes, dir);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    private void updateC(List<Node> cs, Document d)
    {
        if (cs.size() == 0)
            return;

        List<Node> ocs = util.getNodes(d, "c");

        for (Node c : cs)
        {
            for (Node oc : ocs)
            {
                if (isSameAttribute(c, oc))
                {
                    Node p = oc.getParentNode();
                    Node cloneC = p.getOwnerDocument().importNode(c, true);
                    p.insertBefore(cloneC, oc);
                    p.removeChild(oc);
                    ocs.remove(oc);
                    break;
                }
            }
        }
    }

    private void updateSi(Map<String, Node> siNodes, String dir)
            throws Exception
    {
        if (siNodes.size() == 0)
            return;

        File f = new File(dir, "xl/sharedStrings.xml");
        if (!f.exists())
            return;

        Document d = util.getDocument(f);
        List<Node> ocs = util.getNodes(d, "si");

        for (String key : siNodes.keySet())
        {
            int index = Integer.parseInt(key);
            Node oc = ocs.get(index);
            Node c = siNodes.get(key);
            Node p = oc.getParentNode();
            Node cloneC = p.getOwnerDocument().importNode(c, true);
            Element e = (Element) cloneC;
            e.removeAttribute("siIndex");
            e.removeAttribute("c");
            e.removeAttribute("r");
            p.insertBefore(cloneC, oc);
            p.removeChild(oc);
        }

        util.saveToFileNoFormat(d, f.getAbsolutePath());
    }

    private void updateHeaderOrFooter(List<Node> os, List<Node> ns)
    {
        if (os.size() == 0)
            return;

        if (os.size() != ns.size())
            return;

        for (int i = 0; i < os.size(); i++)
        {
            Node oc = os.get(i);
            Node c = ns.get(i);

            Node p = oc.getParentNode();
            Node cloneC = p.getOwnerDocument().importNode(c, true);
            p.insertBefore(cloneC, oc);
            p.removeChild(oc);
        }
    }

    private boolean isSameAttribute(Node n1, Node n2)
    {
        NamedNodeMap attrs1 = n1.getAttributes();
        NamedNodeMap attrs2 = n2.getAttributes();

        if (attrs1.getLength() != attrs2.getLength())
            return false;

        for (int i = 0; i < attrs1.getLength(); ++i)
        {
            Node att = attrs1.item(i);
            String attname = att.getNodeName();
            String value = att.getNodeValue();

            boolean found = false;
            for (int j = 0; j < attrs1.getLength(); ++j)
            {
                Node att2 = attrs2.item(i);
                String attname2 = att2.getNodeName();
                String value2 = att2.getNodeValue();

                if (attname.equals(attname2) && value.equals(value2))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
                return false;
        }

        return true;
    }

    private Comparator getComparatorByR()
    {
        return new Comparator()
        {

            @Override
            public int compare(Object o1, Object o2)
            {
                Cell c1 = (Cell) o1;
                Cell c2 = (Cell) o2;

                String r1 = c1.getR();
                String r2 = c2.getR();

                if (r1.length() != r2.length())
                {
                    return r1.length() - r2.length();
                }

                if (!r1.equals(r2))
                {
                    return r1.compareTo(r2);
                }

                String cc1 = c1.getC();
                String cc2 = c2.getC();

                if (cc1.length() != cc2.length())
                {
                    return cc1.length() - cc2.length();
                }

                if (!cc1.equals(cc2))
                {
                    return cc1.compareTo(cc2);
                }

                return 0;
            }
        };
    }

    private Comparator getComparatorByC()
    {
        return new Comparator()
        {

            @Override
            public int compare(Object o1, Object o2)
            {
                Cell c1 = (Cell) o1;
                Cell c2 = (Cell) o2;

                String cc1 = c1.getC();
                String cc2 = c2.getC();

                if (cc1.length() != cc2.length())
                {
                    return cc1.length() - cc2.length();
                }

                if (!cc1.equals(cc2))
                {
                    return cc1.compareTo(cc2);
                }

                String r1 = c1.getR();
                String r2 = c2.getR();

                if (r1.length() != r2.length())
                {
                    return r1.length() - r2.length();
                }

                if (!r1.equals(r2))
                {
                    return r1.compareTo(r2);
                }

                return 0;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public void sortSegments(String dir, List<String> hIds, String excelOrder,
            HashMap<String, String> hideCellMap,
            Set<String> p_excelInternalTextCellStyles)
    {
        XmlUtil util = new XmlUtil();
        List<File> fs = FileUtil.getAllFiles(new File(dir, "xl/worksheets"),
                new FileFilter()
                {

                    @Override
                    public boolean accept(File pathname)
                    {
                        String name = pathname.getName();
                        return name.startsWith("sheet")
                                && name.endsWith(".xml");
                    }
                });

        File sharedStrings = new File(dir, "xl/sharedStrings.xml");
        if (!sharedStrings.exists())
        {
            return;
        }

        Document doc = null;
        try
        {
            doc = util.getDocument(sharedStrings);
        }
        catch (Exception e1)
        {
            try
            {
                String content = FileUtil.readFile(sharedStrings, "UTF-8");

                while (content.charAt(0) != '<')
                {
                    content = content.substring(1);
                }
                FileUtil.writeFile(sharedStrings, content, "UTF-8");
                doc = util.getDocument(sharedStrings);
            }
            catch (Exception e2)
            {
                logger.error(e1);
                return;
            }
        }

        List<Node> sis = util.getNodes(doc, "si");

        for (File f : fs)
        {
            String fileName = FileUtils
                    .getPrefix(FileUtils.getBaseName(f.getName()));
            String value = hideCellMap.get(fileName);
            ArrayList<String> hide = new ArrayList<String>();
            if (value != null)
            {
                hide.addAll(MSOffice2010Filter.toList(value));
            }

            List<Cell> cs = new ArrayList<Cell>();
            Document d = null;
            
            try
            {
                d = util.getDocument(f);
            }
            catch (Exception e1)
            {
                try
                {
                    String content = FileUtil.readFile(f, "UTF-8");

                    while (content.charAt(0) != '<')
                    {
                        content = content.substring(1);
                    }
                    FileUtil.writeFile(f, content, "UTF-8");
                    d = util.getDocument(f);
                }
                catch (Exception e2)
                {
                    logger.error(e1);
                    return;
                }
            }
            
            try
            {
                List<Node> vs = util.getNodes(d, "v");
                for (Node v : vs)
                {
                    Node n = v.getParentNode();
                    if (!"c".equals(n.getNodeName()))
                        continue;

                    Node r = n.getParentNode();
                    if (!"row".equals(r.getNodeName()))
                        continue;

                    Node cr = util.getAttribute(n, "r");
                    String vc = cr.getNodeValue();
                    if (hide.contains(vc))
                        continue;

                    Node ar = util.getAttribute(r, "r");
                    String vr = ar.getNodeValue();
                    vc = vc.substring(0, vc.length() - vr.length());
                    Cell c = new Cell();

                    c.setR(vr);
                    c.setC(vc);

                    Node t = util.getAttribute(n, "t");
                    if (t != null && "s".equals(t.getNodeValue()))
                    {
                        c.setFromSharedString(true);
                        String sid = v.getTextContent();
                        if (hIds.contains(sid))
                            continue;

                        c.setSsId(sid);
                        c.setNode(sis.get(Integer.parseInt(sid)));
                    }
                    else
                    {
                        c.setNode(n);
                    }

                    cs.add(c);
                }
            }
            catch (Exception e)
            {
                logger.error(e);
            }

            if (cs.size() == 0)
                continue;

            Comparator comparator = null;
            if ("c".equals(excelOrder))
            {
                comparator = getComparatorByC();
            }
            else
            {
                comparator = getComparatorByR();
            }

            SortUtil.sort(cs, comparator);

            try
            {

                Document save = util.newDocument();
                Element root = save.createElement("worksheet");
                save.appendChild(root);
                for (Cell c : cs)
                {
                    Node n = c.getNode();
                    Element cn = (Element) save.importNode(n, true);
                    if (c.isFromSharedString())
                    {
                        String ssid = c.getSsId();
                        cn.setAttribute("siIndex", ssid);
                        cn.setAttribute("r", c.getR());
                        cn.setAttribute("c", c.getC());
                        if (p_excelInternalTextCellStyles.contains(ssid))
                        {
                            cn.setAttribute("isInternalTextCellStyles", "1");
                        }
                    }

                    root.appendChild(cn);
                }

                List<Node> oddHeader = util.getNodes(doc, "oddHeader");
                for (Node h : oddHeader)
                {
                    Element cn = (Element) save.importNode(h, true);
                    root.appendChild(cn);
                }

                List<Node> oddFooter = util.getNodes(doc, "oddFooter");
                for (Node h : oddFooter)
                {
                    Element cn = (Element) save.importNode(h, true);
                    root.appendChild(cn);
                }

                String p = f.getParent();
                String name = f.getName();
                name = "sort" + name;
                util.saveToFileNoFormat(save, p + "/" + name);
            }
            catch (Exception e)
            {
                logger.error(e);
            }
        }
    }

    public void sortComments(List<String> comments, String order)
    {
        XmlUtil util = new XmlUtil();
        for (String cPath : comments)
        {
            File f = new File(cPath);
            List<Cell> cells = new ArrayList<Cell>();
            Document doc = null;
            try
            {
                doc = util.getDocument(f);
                List<Node> cNodes = util.getNodes(doc, "comment");
                for (Node cNode : cNodes)
                {
                    Node ref = util.getAttribute(cNode, "ref");
                    String value = ref.getNodeValue();
                    // <comment ref = "AA1234"
                    int index = 0;
                    for (int i = 0; i < value.length(); i++)
                    {
                        char c = value.charAt(i);
                        if ("1234567890".indexOf(c) > -1)
                        {
                            index = i;
                            break;
                        }
                    }
                    String colum = value.substring(0, index);
                    String row = value.substring(index);
                    Cell c = new Cell();
                    c.setR(row);
                    c.setC(colum);
                    c.setNode(cNode);

                    cells.add(c);
                }
            }
            catch (Exception e)
            {
                logger.error(e);
            }

            if (cells.size() == 0)
                continue;

            Comparator comparator = null;
            if ("c".equals(order))
            {
                comparator = getComparatorByC();
            }
            else
            {
                comparator = getComparatorByR();
            }

            SortUtil.sort(cells, comparator);

            Node clNode = util.getNode(doc, "commentList");
            List<Node> cNodes = util.getChildNodes(clNode);

            int size = cNodes.size();
            for (int i = 0; i < size; i++)
            {
                clNode.removeChild(cNodes.get(i));
            }

            for (int i = 0; i < size; i++)
            {
                Cell c = cells.get(i);
                clNode.appendChild(c.getNode());
            }
            util.saveToFileNoFormat(doc, cPath);
        }
    }
}
