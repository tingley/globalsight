using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;

namespace windowsIllustratorConverter
{
    class XmlWriter : XmlProcess
    {
        private string m_cs2XmlFileName = null;
        private string m_documentName = null;

        private const string CS2_BODY = "Body";
        private const string CS2_DOCUMENT = "Document";
        private const string CS2_LAYER = "Layer";
        private const string CS2_GROUPITEM = "GroupItem";
        private const string CS2_TEXT_FRAME = "TextFrame";
        private const string CS2_TEXT_RANGE = "TextRange";
        private const string CS2_ITEM_ID = "ItemId";
        private const string CS2_PARAGRAPH = "Paragraph";
        private const string CS2_CONTENT = "Content";
        private const string CS2_FILE_NAME = "FileName";

        private XmlTextWriter tw = null;

        private bool isLayerOpened = false;
        private bool isTextFrameOpened = false;
        private bool isTextRangeOpened = false;
        private bool isParagraphOpened = false;
        private bool isGroupItemOpened = false;
        private bool isFirstTimeGroupItemWritten = true;


        public XmlWriter(string p_cs2XmlFileName)
        {
            isLayerOpened = false;
            isTextFrameOpened = false;
            isTextRangeOpened = false;
            isParagraphOpened = false;
            isGroupItemOpened = false;
            isFirstTimeGroupItemWritten = true;
            m_cs2XmlFileName = p_cs2XmlFileName;
            m_documentName = p_cs2XmlFileName.Substring(0, p_cs2XmlFileName.LastIndexOf("."));

            tw = new XmlTextWriter(m_cs2XmlFileName, System.Text.Encoding.GetEncoding("UTF-8"));
            tw.Formatting = Formatting.Indented;
            tw.WriteStartDocument();
            tw.WriteStartElement(CS2_BODY);
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
            tw.WriteAttributeString(CS2_ITEM_ID, layerItemId.ToString());
            isLayerOpened = true;
        }

        private void WriteGroupItem(int groupItemId)
        {
            EndGroupItem();
            tw.WriteStartElement(CS2_GROUPITEM);
            tw.WriteAttributeString(CS2_ITEM_ID, groupItemId.ToString());
            isGroupItemOpened = true;
 
        }

        private void WriteTextFrame(int textFrameItemId)
        {
            EndTextFrame();
            tw.WriteStartElement(CS2_TEXT_FRAME);
            tw.WriteAttributeString(CS2_ITEM_ID, textFrameItemId.ToString());
            isTextFrameOpened = true;
        }

        private void WriteTextRange(int p_textRangeIndex)
        {
            EndTextRange();
            tw.WriteStartElement(CS2_TEXT_RANGE);
            tw.WriteAttributeString(CS2_ITEM_ID, p_textRangeIndex.ToString());
            isTextRangeOpened = true;
        }

        private void WriteParagraph(int paragraphItemId, string paragraph)
        {
            if (paragraphItemId == -1)
            {
                tw.WriteElementString(CS2_CONTENT, paragraph);
            }
            else
            {
                EndParagraph();
                tw.WriteStartElement(CS2_PARAGRAPH);
                tw.WriteAttributeString(CS2_ITEM_ID, paragraphItemId.ToString());
                tw.WriteElementString(CS2_CONTENT, paragraph);
                isParagraphOpened = true;
            }
        }

        private void EndParagraph()
        {
            tw.Flush();
            if (isParagraphOpened)
            {
                tw.WriteEndElement();
                isParagraphOpened = false;
            }
        }

        private void EndTextRange()
        {
            tw.Flush();
            if (isTextRangeOpened)
            {
                EndParagraph();
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
                EndGroupItem();
                tw.WriteEndElement();
                isLayerOpened = false;
            }
        }

        private void EndGroupItem()
        {
            tw.Flush();
            // Before starting write groupItem tag firstly close the TextFrame tag
            // which should not be contained in groupItem tag.
            if (isFirstTimeGroupItemWritten)
            {
                EndTextFrame();
                isFirstTimeGroupItemWritten = false;
            }
            if (isGroupItemOpened)
            {
                EndTextFrame();
                tw.WriteEndElement();
                isGroupItemOpened = false;
            }
        }

        private void CloseXmlFile()
        {
            EndLayer();
            tw.WriteEndElement();

            tw.WriteEndDocument();
            tw.Flush();
            tw.Close();
        }


        #region XmlProcess Members

        public void OpenXml()
        {
            if (tw == null)
                throw new Exception("the xml is not opened.");
        }

        public void Init()
        {
            WriteDocument();
        }

        public void ProcessLayer(int p_layerIndex)
        {
            WriteLayer(p_layerIndex);
        }

        public void ProcessGroupItem(int p_groupItemIndex)
        {
            WriteGroupItem(p_groupItemIndex);
        }

        public void ProcessTextFrame(int p_textFrameIndex)
        {
            WriteTextFrame(p_textFrameIndex);
        }

        public void ProcessTextRange(int p_textRangeIndex)
        {
            WriteTextRange(p_textRangeIndex);
        }

        public string ProcessParagraph(int p_paragraphIndex, string p_paragraph)
        {
            WriteParagraph(p_paragraphIndex, p_paragraph);
            return p_paragraph;
        }

        public void CloseXml()
        {
            CloseXmlFile();
        }

        #endregion
    }
}
