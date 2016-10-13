using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using Ricciolo.StylesExplorer.MarkupReflection;
using Mono.Cecil;
using ILSpy.BamlDecompiler;
using System.IO;

namespace GlobalSight.WinPEConverter
{
    public class BamlUtil
    {
        public static XDocument LoadIntoDocument(IAssemblyResolver resolver, AssemblyDefinition asm, Stream stream)
        {
            XDocument xamlDocument;
            using (XmlBamlReader reader = new XmlBamlReader(stream, new CecilTypeResolver(resolver, asm)))
                xamlDocument = XDocument.Load(reader);
            ConvertToEmptyElements(xamlDocument.Root);
            MoveNamespacesToRoot(xamlDocument);
            return xamlDocument;
        }

        static void MoveNamespacesToRoot(XDocument xamlDocument)
        {
            var additionalXmlns = new List<XAttribute> {
				new XAttribute("xmlns", XmlBamlReader.DefaultWPFNamespace),
				new XAttribute(XName.Get("x", XNamespace.Xmlns.NamespaceName), XmlBamlReader.XWPFNamespace)
			};

            foreach (var element in xamlDocument.Root.DescendantsAndSelf())
            {
                if (element.Name.NamespaceName != XmlBamlReader.DefaultWPFNamespace && !additionalXmlns.Any(ka => ka.Value == element.Name.NamespaceName))
                {
                    string newPrefix = new string(element.Name.LocalName.Where(c => char.IsUpper(c)).ToArray()).ToLowerInvariant();
                    int current = additionalXmlns.Count(ka => ka.Name.Namespace == XNamespace.Xmlns && ka.Name.LocalName.TrimEnd(ch => char.IsNumber(ch)) == newPrefix);
                    if (current > 0)
                        newPrefix += (current + 1).ToString();
                    XName defaultXmlns = XName.Get(newPrefix, XNamespace.Xmlns.NamespaceName);
                    if (element.Name.NamespaceName != XmlBamlReader.DefaultWPFNamespace)
                        additionalXmlns.Add(new XAttribute(defaultXmlns, element.Name.NamespaceName));
                }
            }

            foreach (var xmlns in additionalXmlns.Except(xamlDocument.Root.Attributes()))
            {
                xamlDocument.Root.Add(xmlns);
            }
        }

        static void ConvertToEmptyElements(XElement element)
        {
            foreach (var el in element.Elements())
            {
                if (!el.IsEmpty && !el.HasElements && el.Value == "")
                {
                    el.RemoveNodes();
                    continue;
                }
                ConvertToEmptyElements(el);
            }
        }

        public static string GetButtonId(XElement button)
        {
            string result = "Uid";
            XAttribute att = XmlUtil.SelectAttributeByName(button, "Uid", "x:Uid");

            if (att == null)
            {
                result = "Name";
                att = XmlUtil.SelectAttributeByName(button, "Name", "Name");
            }

            if (att == null)
            {
                throw new Exception("Cannot find ID for button : " + button.ToString());
            }
            else
            {
                result = GetTUID(result, att.Value);
            }

            return result;
        }

        public static string GetTUID(string atName, string atValue)
        {
            //return "[" + atName + "]" + atValue;
            return atValue;
        }
    }
}
