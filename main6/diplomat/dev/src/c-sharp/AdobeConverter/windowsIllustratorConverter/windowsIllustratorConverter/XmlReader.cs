using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
namespace windowsIllustratorConverter
{
    class XmlReader : XmlProcess
    {

        private XmlTextReader tr = null;

        private const string CS2_DOCUMENT = "Document";
        private const string CS2_LAYER = "Layer";
        private const string CS2_GROUPITEM = "GroupItem";
        private const string CS2_TEXT_FRAME = "TextFrame";
        private const string CS2_TEXT_RANGE = "TextRange";
        private const string CS2_ITEM_ID = "ItemId";
        private const string CS2_PARAGRAPH = "Paragraph";
        private const string CS2_CONTENT = "Content";
        private const string CS2_FILE_NAME = "FileName";

        private bool stopRead = false;

        public XmlReader(string xmlFileName)
        {
            tr = new XmlTextReader(xmlFileName);

        }

        private bool check(string typeName, int id)
        {
            if (tr.LocalName.Equals(typeName) && id.ToString().Equals(tr.GetAttribute(CS2_ITEM_ID)))
            {
                return true;
            }
            else
            {
                stopRead = true;
                return false;
            }
        }

        private bool move(string typeName, int id)
        {
            if (stopRead)
            {
                stopRead = !check(typeName, id);
                return !stopRead;
            }
            while (tr.Read())
            {
                if (tr.LocalName.Equals(typeName) && tr.AttributeCount > 0)
                {
                    return check(typeName, id);
                }
            }
            return false;

        }

        public bool ReadLayer(int layerItemId)
        {
            return move(CS2_LAYER, layerItemId);
        }

        public bool ReadGroupItem(int groupItemId)
        {
            return move(CS2_GROUPITEM, groupItemId);
        }

        public bool ReadTextFrame(int textFrameItemId)
        {
            return move(CS2_TEXT_FRAME, textFrameItemId);
        }

        public bool ReadTextRange()
        {
            while (tr.Read())
            {
                if (tr.NodeType == XmlNodeType.Element)
                {
                    if (tr.LocalName.Equals(CS2_TEXT_RANGE))
                    {
                        return true;
                    }

                }
            }
            return false;
        }

        public bool ReadParagraph(int paragraphItemId)
        {
            if (paragraphItemId == -1)
            {
                return true;
            }
            return move(CS2_PARAGRAPH, paragraphItemId);
        }

        private string ReadParagraphContent(string p_paragraph)
        {
            while (tr.Read())
            {
                if (tr.NodeType == XmlNodeType.Element)
                {
                    if (tr.LocalName.Equals(CS2_CONTENT))
                    {
                        return tr.ReadString();
                    }

                }
            }
            return null;
        }

        public void CloseXmlFile()
        {
            tr.Close();
        }


        #region XmlProcess Members

        public void OpenXml()
        {
            OpenXml();
        }

        public void Init()
        {

        }

        public void ProcessLayer(int p_layerIndex)
        {
            ReadLayer(p_layerIndex);
        }

        public void ProcessGroupItem(int p_groupItemIndex)
        {
            ReadGroupItem(p_groupItemIndex);
        }

        public void ProcessTextFrame(int p_textFrameIndex)
        {
            ReadTextFrame(p_textFrameIndex);
        }

        public void ProcessTextRange(int p_textRangeIndex)
        {
            ReadTextRange();
        }

        public string ProcessParagraph(int p_paragraphIndex, string p_paragraph)
        {
            ReadParagraph(p_paragraphIndex);
            return ReadParagraphContent(p_paragraph);
        }

        public void CloseXml()
        {
            CloseXmlFile();
        }

        #endregion
    }
}
