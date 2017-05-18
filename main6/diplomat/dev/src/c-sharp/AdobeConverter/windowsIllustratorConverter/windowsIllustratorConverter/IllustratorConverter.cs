using System;
using System.Collections.Generic;
using System.Text;
using Illustrator;
using GlobalSight.Common;
using System.Runtime.InteropServices;
using System.Threading;

namespace windowsIllustratorConverter
{
    class IllustratorConverter : Converter
    {
        public enum ConversionType { IMPORT, EXPORT };

        private Logger m_log = Logger.GetLogger();
        private ConversionType m_conversionType = ConversionType.IMPORT;
        private IllustratorApp m_app = IllustratorApp.getInstance();
        private Illustrator.Document m_doc = null;

        private XmlProcess m_xmlProcess = null;

        private string m_cs2FileName = null;
        private string m_xmlFileName = null;
        private string error = null;

        public void OpenFile(string p_fileName, ConversionType p_conversionType)
        {
            m_conversionType = p_conversionType;
            if (p_conversionType == ConversionType.IMPORT)
            {
                m_cs2FileName = p_fileName;
            }
            else
            {
                m_cs2FileName = p_fileName.Substring(0, p_fileName.LastIndexOf(".")) + ".ai";
                m_xmlFileName = p_fileName;
            }
        }

        public string getErrorInfo()
        {
            return error;
        }

        private void CS2Converter(string a, string b)
        {
            int i = init();
            if (i == 1)
            {
                return;
            }
            ReadLayer();
            CloseFile();
        }

        private void ReadLayer()
        {
            Layers layers = m_doc.Layers;

            for (int layIndex = 1; layIndex <= layers.Count; layIndex++)
            {
                try
                {
                    Layer layer = layers[layIndex];
                    string layerName = layer.Name.Trim().ToLower();
                    if (layerName.Equals("translation") 
                        || layerName.Equals("translate") 
                        || layerName.Equals("for translation"))
                    {
                        m_xmlProcess.ProcessLayer(layIndex);
                        TextFrames textFrames = layer.TextFrames;
                        ReadTextFrames(textFrames);
                        error = "0";
                        // Sometimes some text is in GroupItems
                        GroupItems groupItems = layer.GroupItems;
                        ReadGroupItems(groupItems);
                        
                    }
                }
                catch (Exception e)
                {
                    m_log.Log(e.ToString());
                    CloseFile();
                    m_app.restartApp();
                    throw new Exception();
                }

            }
            if (error == null)
            {
                error = "There is no layer needed to be translated ";
            }
        }

        private void ReadGroupItems(GroupItems groupItems)
        {
            for (int groupItemIndex = 1; groupItemIndex <= groupItems.Count; groupItemIndex++)
            {
                GroupItem groupItem = groupItems[groupItemIndex];
                m_xmlProcess.ProcessGroupItem(groupItemIndex);
                TextFrames textFrames = groupItem.TextFrames;
                ReadTextFrames(textFrames);
                error = "0";

            }
        }

        private void ReadTextFrames(TextFrames textFrames)
        {
            for (int textFrameIndex = 1; textFrameIndex <= textFrames.Count; textFrameIndex++)
            {
                TextFrame textFrame = textFrames[textFrameIndex];
                m_xmlProcess.ProcessTextFrame(textFrameIndex);
                TextRange textRange = textFrame.TextRange;
                m_xmlProcess.ProcessTextRange(1);
                ReadParagraphs(textRange);
            }
        }

        private void ReadParagraphs(TextRange textRange)
        {
            if (textRange.Contents.Trim() == "")
            {
                textRange.Contents = m_xmlProcess.ProcessParagraph(-1, "");
            }
            else
            {   
                Paragraphs paragraphs = textRange.Paragraphs;
                for (int i = 1; i <= paragraphs.Count; i++)
                {
                    try
                    {
                        paragraphs[i].Contents = m_xmlProcess.ProcessParagraph(i, paragraphs[i].Contents);
                    }
                    // Illustrator's bug would cause an exception, 
                    // just catch it preventing job creating from failure
                    catch (Exception e)
                    {
                      
                    }                  
                    
                }
            }     
        }

        private void CloseFile()
        {
            CloseIllustratorDoc();
            CloseXml();
        }

        private void CloseXml()
        {
            m_xmlProcess.CloseXml();
        }


        private void OpenIllustratorDoc()
        {
            m_doc = m_app.open(m_cs2FileName);
        }

        private void CloseIllustratorDoc()
        {
            m_doc.Save();
            m_doc.Close(AiSaveOptions.aiSaveChanges);
        }

        private void CreateXmlReader()
        {
            m_xmlProcess = new XmlReader(m_xmlFileName);
        }

        private void CreateXmlWriter()
        {
            string xmlFileName = m_cs2FileName.Substring(0, m_cs2FileName.LastIndexOf(".")) + ".xml";
            m_xmlProcess = new XmlWriter(xmlFileName);
        }

        private int init()
        {
            try
            {
                OpenIllustratorDoc();
            }
            catch (Exception e)
            {
                m_log.Log(e.ToString());
                error = "The specified file could not be opened because it could not be found or it is an unknown format.";
                return 1;
            }

            if (m_conversionType == ConversionType.IMPORT)
            {
                CreateXmlWriter();
            }
            else
            {
                CreateXmlReader();
            }
            m_xmlProcess.Init();
            return 0;

        }


        #region Converter Members

        public void Convert(string p_filename, string p_srcLanguage)
        {
            CS2Converter(p_filename, p_srcLanguage);
        }

        public string GetFileExtensionToWatch()
        {
            throw new Exception("The method or operation is not implemented.");
        }

        #endregion
    }
}
