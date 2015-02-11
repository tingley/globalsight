using System;
using System.Collections.Generic;
using System.Text;
using Illustrator;
using GlobalSight.Common;
using System.Runtime.InteropServices;

namespace windowsIllustratorConverter
{
    class IllustratorConverter : Converter
    {
        public enum ConversionType { IMPORT, EXPORT };

        private ConversionType m_conversionType = ConversionType.IMPORT;
        private IllustratorApp m_app = IllustratorApp.getInstance();
        private Illustrator.Document m_doc = null;

        //private XmlReader m_xmlReader = null;
        //private XmlWriter m_xmlWriter = null;
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
            //new TestFile().doJob();
        }

        private void ReadLayer()
        {
            Layers layers = m_doc.Layers;

            for (int layIndex = 1; layIndex <= layers.Count; layIndex++)
            {
                try
                {
                    Console.WriteLine(layIndex);
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
                    }
                }
                catch (Exception e)
                {
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

        private void ReadTextFrames(TextFrames textFrames)
        {
            for (int textFrameIndex = 1; textFrameIndex <= textFrames.Count; textFrameIndex++)
            {
                TextFrame textFrame = textFrames[textFrameIndex];
                //if (textFrame.Contents == "") continue;
                m_xmlProcess.ProcessTextFrame(textFrameIndex);
                //TextRanges textRanges = textFrame.TextRanges;
                TextRange textRange = textFrame.TextRange;
                ReadRangeContents(textRange);
            }
        }

        private void ReadRangeContents(TextRange textRange)
        {
            string result = "";
            string contents = textRange.Contents;


            textRange.Contents = m_xmlProcess.ProcessTextRangeContent(1, contents);
            if (contents == "")
            {
                return;
            }
            CharacterAttributes ca = textRange.CharacterAttributes;
            TextFont tf = ca.TextFont;
            string fontFamily = tf.Family;
            Double d = ca.Size;
            result = result + " contents:" + contents + " fontFamily:" + fontFamily + " size:" + d + "\r\n";
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
            //Console.WriteLine("Open an Illustrator file !!!");
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
            catch (Exception ex)
            {
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
