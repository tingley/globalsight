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
        void ProcessGroupItem(int p_groupItemIndex);
        void ProcessTextFrame(int p_textFrameIndex);
        void ProcessTextRange(int p_textRangeIndex);
        string ProcessParagraph(int p_paragraphIndex, string p_paragraph);
        void CloseXml();
    }
}
