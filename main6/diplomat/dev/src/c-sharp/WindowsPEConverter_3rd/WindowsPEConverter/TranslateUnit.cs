using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GlobalSight.WinPEConverter
{
    public class TranslateUnit
    {
        private string _category;
        private int _lineNumber = -1;
        private string _srcString;
        private string _tgtString;
        

        public TranslateUnit()
        {
        }

        public TranslateUnit(int lineNumber, string category, string srcString, string tgtString)
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
    }

    public class TranslateUnitUtil
    {
        public static TranslateUnit GetTranslateUnit(List<TranslateUnit> units, string category, int lineNumber)
        {
            foreach (TranslateUnit tu in units)
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
