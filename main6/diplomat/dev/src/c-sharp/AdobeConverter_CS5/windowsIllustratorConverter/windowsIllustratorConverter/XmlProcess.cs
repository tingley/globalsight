using System;
using System.Collections.Generic;
using System.Text;

namespace windowsIllustratorConverter
{
    public interface XmlProcess
    {
        void OpenXml();
        void Init();
        void ProcessLayer(int p_layerIndex);
        void ProcessTextFrame(int p_textFrameIndex);
        string ProcessTextRangeContent(int p_content, string p_textRangeContent);
        void CloseXml();
    }
}
