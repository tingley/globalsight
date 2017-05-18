using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
namespace IllustratorConvert
{
    class XmlReader : XmlProcess
    {

        private XmlTextReader tr = null;

        private const string CS2_DOCUMENT = "Document";
        private const string CS2_LAYER = "Layer";
        private const string CS2_TEXT_FRAME = "TextFrame";
        private const string CS2_TEXT_RANGE = "TextRange";
        private const string CS2_ITME_ID = "ItemId";
        private const string CS2_TEXT_RANGE_CONTENT = "TextContent";
        private const string CS2_FILE_NAME = "FileName";

        private bool stopRead = false;

        public XmlReader(string xmlFileName)
        {
            tr = new XmlTextReader(xmlFileName);

        }

        private bool check(string typeName, int id)
        {
            //int x = int.Parse(tr.GetAttribute(CS2_ITME_ID));
            if (tr.LocalName.Equals(typeName) && id.ToString().Equals(tr.GetAttribute(CS2_ITME_ID)))
            {
                for (int i = 0; i < tr.AttributeCount; i++)
                {
                    string attr = tr.GetAttribute(i);
                    string att2 = tr.GetAttribute(CS2_ITME_ID);
                }
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

        public bool ReadTextFrame(int textFrameItemId)
        {
            return move(CS2_TEXT_FRAME, textFrameItemId);
        }

        public bool ReadTextRange()
        {
            //move(CS2_TEXT_RANGE, layerItemId);
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

        public string ReadTextRangeContent()
        {
            while (tr.Read())
            {
                if (tr.NodeType == XmlNodeType.Element)
                {
                    if (tr.LocalName.Equals(CS2_TEXT_RANGE_CONTENT))
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

        public void ProcessTextFrame(int p_textFrameIndex)
        {
            ReadTextFrame(p_textFrameIndex);
        }

        public string ProcessTextRangeContent(int p_layerIndex, string p_textRangeContent)
        {
            ReadTextRange();
            return ReadTextRangeContent();
        }

        public void CloseXml()
        {
            CloseXmlFile();
        }

        #endregion
    }
}
