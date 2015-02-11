using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;

namespace IllustratorConvert
{
    class XmlWriter : XmlProcess
    {
        private string m_cs2XmlFileName = null;
        private string m_documentName = null;

        private const string CS2_DOCUMENT = "Document";
        private const string CS2_LAYER = "Layer";
        private const string CS2_TEXT_FRAME = "TextFrame";
        private const string CS2_TEXT_RANGE = "TextRange";
        private const string CS2_ITME_ID = "ItemId";
        private const string CS2_TEXT_RANGE_CONTENT = "TextContent";
        private const string CS2_FILE_NAME = "FileName";

        private XmlTextWriter tw = null;



        private bool isLayerOpened = false;
        private bool isTextFrameOpened = false;
        private bool isTextRangeOpened = false;



        public XmlWriter(string p_cs2XmlFileName)
        {
            isLayerOpened = false;
            isTextFrameOpened = false;
            isTextRangeOpened = false;
            m_cs2XmlFileName = p_cs2XmlFileName;
            m_documentName = p_cs2XmlFileName.Substring(0, p_cs2XmlFileName.LastIndexOf("."));

            tw = new XmlTextWriter(m_cs2XmlFileName, System.Text.Encoding.GetEncoding("UTF-8"));
            tw.Formatting = Formatting.Indented;
            tw.WriteStartDocument();
            tw.WriteStartElement("BODY");
        }

        private void WriteDocument()
        {
            tw.WriteStartElement(CS2_DOCUMENT);
            tw.WriteAttributeString(CS2_FILE_NAME, m_documentName);
        }
        private void WriteLayer(int layerItemId)
        {
            EndLayer();
            tw.WriteStartElement(CS2_LAYER);
            tw.WriteAttributeString(CS2_ITME_ID, layerItemId.ToString());
            isLayerOpened = true;
        }
        private void WriteTextFrame(int textFrameItmeId)
        {
            EndTextFrame();
            tw.WriteStartElement(CS2_TEXT_FRAME);
            tw.WriteAttributeString(CS2_ITME_ID, textFrameItmeId.ToString());
            isTextFrameOpened = true;
        }
        private void WriteTextRange(int textRangeItemId, string textRangeContent)
        {
            EndTextRange();
            tw.WriteStartElement(CS2_TEXT_RANGE);
            tw.WriteAttributeString(CS2_TEXT_RANGE, textRangeItemId.ToString());
            tw.WriteElementString(CS2_TEXT_RANGE_CONTENT, textRangeContent);
            isTextRangeOpened = true;

        }
        private void EndTextRange()
        {
            tw.Flush();
            if (isTextRangeOpened)
            {
                tw.WriteEndElement();
                isTextRangeOpened = false;
            }
        }
        private void EndTextFrame()
        {
            tw.Flush();
            if (isTextFrameOpened)
            {
                EndTextRange();
                tw.WriteEndElement();
                isTextFrameOpened = false;
            }
        }
        private void EndLayer()
        {
            tw.Flush();
            if (isLayerOpened)
            {
                EndTextFrame();
                tw.WriteEndElement();
                isLayerOpened = false;
            }
        }

        private void CloseXmlFile()
        {
            EndLayer();
            tw.WriteEndElement();

            tw.WriteEndDocument();
            tw.Flush();
            tw.Close();
            //System.Console.WriteLine("done");
        }


        #region XmlProcess Members

        public void OpenXml()
        {
            if (tw == null)
                throw new Exception("the xml is not openned.");
        }

        public void Init()
        {
            WriteDocument();
        }

        public void ProcessLayer(int p_layerIndex)
        {
            WriteLayer(p_layerIndex);
        }

        public void ProcessTextFrame(int p_layerIndex)
        {
            WriteTextFrame(p_layerIndex);
        }


        public string ProcessTextRangeContent(int p_layerIndex, string p_textRangeContent)
        {
            WriteTextRange(p_layerIndex, p_textRangeContent);
            return p_textRangeContent;
        }

        public void CloseXml()
        {
            CloseXmlFile();
        }

        #endregion
    }
}
