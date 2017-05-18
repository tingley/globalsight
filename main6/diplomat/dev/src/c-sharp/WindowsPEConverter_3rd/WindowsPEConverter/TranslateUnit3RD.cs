using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GlobalSight.WinPEConverter
{
    public class TranslateUnit3RD
    {
        private string _category;
        private string _id;
        private int _lineNumber = -1;
        private string _srcString;
        private string _tgtString;
        

        public TranslateUnit3RD()
        {
        }

        public TranslateUnit3RD(int lineNumber, string category, string srcString, string tgtString)
        {
            _lineNumber = lineNumber;
            _category = category;
            _srcString = srcString;
            _tgtString = tgtString;
        }

        public String SourceContent
        {
            set
            {
                _srcString = value;
            }
            get
            {
                return _srcString;
            }
        }

        public String TargetContent
        {
            set
            {
                _tgtString = value;
            }
            get
            {
                return _tgtString;
            }
        }

        public String Category
        {
            set
            {
                _category = value;
            }
            get
            {
                return _category == null ? "" : _category;
            }
        }

        public int LineNumber
        {
            set
            {
                _lineNumber = value;
            }
            get
            {
                return _lineNumber;
            }
        }

        public String Id
        {
            set
            {
                _id = value;
            }
            get
            {
                return _id;
            }
        }
    }

    public class TranslateUnitUtil3RD
    {
        public static TranslateUnit3RD GetTranslateUnit(List<TranslateUnit3RD> units, string category, int lineNumber)
        {
            foreach (TranslateUnit3RD tu in units)
            {
                if (tu.Category.Equals(category) && (tu.LineNumber == lineNumber))
                {
                    return tu;
                }
            }

            return null;
        }
    }

    public enum TranslateUnitType
    {
        MenuType,
        StringType,
        DialogType,
        VersionType
    }
}
