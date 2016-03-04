using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.IO;
using System.Xml.Linq;
using System.Xml.XPath;

namespace GlobalSight.WinPEConverter
{
    public class XmlUtil
    {
        public static string OutputTranslateUnits(List<TranslateUnit> units)
        {
            StringBuilder sb = new StringBuilder();
            // head
            sb.Append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
            sb.Append("<GlobalSightPE version=\"8.6.8\">");
            // body
            if (units != null && units.Count != 0)
            {
                foreach (TranslateUnit unit in units)
                {
                    sb.Append("<TranslateUnit lineNumber=\"");
                    sb.Append(unit.LineNumber);
                    sb.Append("\" category=\"");
                    sb.Append(unit.Category);
                    sb.Append("\">");
                    sb.Append(Escape(unit.SourceContent));
                    sb.Append("</TranslateUnit>");
                }
            }

            // end
            sb.Append("</GlobalSightPE>");

            return sb.ToString();
        }

        public static string Escape(string xmlcontent)
        {
            if (xmlcontent == null)
            {
                return xmlcontent;
            }

            return xmlcontent.Replace("&", "&amp;").Replace("<", "&lt;").Replace(">", "&gt;").Replace("\"", "&quot;").Replace("'", "&apos;");
        }

        public static List<TranslateUnit> ParseTranslateUnits(string xmlunits)
        {
            List<TranslateUnit> result = new List<TranslateUnit>();

            if (xmlunits == null || !xmlunits.Contains("<GlobalSightPE "))
            {
                return result;
            }

            XmlDocument doc = new XmlDocument();
            doc.LoadXml(xmlunits);

            XmlNodeList nodes = doc.GetElementsByTagName("TranslateUnit");
            if (nodes != null && nodes.Count > 0)
            {
                foreach (XmlNode node in nodes)
                {
                    int lineNumber = Int32.Parse(node.Attributes["lineNumber"].Value);
                    string category = node.Attributes["category"].Value;
                    string content = node.InnerText;

                    TranslateUnit u = new TranslateUnit(lineNumber, category, content, content);
                    result.Add(u);
                }
            }

            return result;
        }

        public static void WriteXml(string xml, string filepath, string encoding)
        {
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.Encoding = Encoding.GetEncoding(encoding);
            settings.OmitXmlDeclaration = false;
            settings.Indent = true;

            using (XmlWriter w = XmlWriter.Create(filepath, settings))
            {
                XmlDocument doc = new XmlDocument();
                doc.LoadXml(xml);
                doc.WriteTo(w);
            }
        }

        public static IEnumerable<XElement> SelectElementsByName(XElement xroot, string elementName)
        {
            return xroot.XPathSelectElements(".//*[local-name()='" + elementName + "']");
        }

        public static XAttribute SelectAttributeByName(XElement el, string attName, string attNameWithNS)
        {
            IEnumerable<XAttribute> xas = el.Attributes();
            foreach (XAttribute ax in xas)
            {
                if (ax.Name.LocalName.Equals(attName) || ax.Name.LocalName.Equals(attNameWithNS))
                {
                    return ax;
                }
            }

            return null;
        }

        public static string ReadFile(string filepath, string encoding)
        {
            return File.ReadAllText(filepath, Encoding.GetEncoding(encoding));
        }

        public static bool TryWriteTus(List<TranslateUnit> _tus)
        {
            if (_tus == null || _tus.Count == 0)
            {
                return true;
            }

            string xml = OutputTranslateUnits(_tus);
            string tempfile = Path.GetTempFileName();

            try
            {
                WriteXml(xml, tempfile, "UTF-8");
                return true;
            }
            catch
            {
                return false;
            }
        }
    }
}
