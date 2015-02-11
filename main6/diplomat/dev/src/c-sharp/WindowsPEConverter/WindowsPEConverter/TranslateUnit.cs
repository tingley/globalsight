using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GlobalSight.WinPEConverter
{
    public class TranslateUnit
    {
        private string _number;
        private string _id;
        private string _index;
        private string _subIndex;
        private string _srcString;
        private string _tgtString;
        private string _category;
        private string _type;

        public TranslateUnit()
        {
        }

        public TranslateUnit(string number, string id, string index, string subIndex)
        {
            _number = number;
            _id = id;
            _index = index;
            _subIndex = subIndex;
        }

        public TranslateUnit(string number, string id, string index, string subIndex, string srcString)
        {
            _number = number;
            _id = id;
            _index = index;
            _subIndex = subIndex;
            _srcString = srcString;
        }

        public TranslateUnit(string number, string id, string index, string subIndex, string srcString, string tgtString)
            : this(number, id, index, subIndex, srcString)
        {
            _tgtString = tgtString;
        }

        public String Number
        {
            set
            {
                _number = value;
            }
            get
            {
                return _number;
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

        public String Index
        {
            set
            {
                _index = value;
            }
            get
            {
                return _index;
            }
        }

        public String SubIndex
        {
            set
            {
                _subIndex = value;
            }
            get
            {
                return _subIndex;
            }
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

        public String Type
        {
            set
            {
                _type = value;
            }
            get
            {
                return _type == null ? "" : _type;
            }
        }
    }

    public class TranslateUnitUtil
    {
        public static TranslateUnit GetTranslateUnit(List<TranslateUnit> units, string category, string id)
        {
            foreach (TranslateUnit tu in units)
            {
                if (tu.Category.Equals(category) && tu.Id.Equals(id))
                {
                    return tu;
                }
            }

            return null;
        }

        public static TranslateUnit GetTranslateUnit(List<TranslateUnit> units, string category, string categoryIndex, string subIndex)
        {
            foreach (TranslateUnit tu in units)
            {
                if (tu.Category.Equals(category) && tu.Index.Equals(categoryIndex) && tu.SubIndex.Equals(subIndex))
                {
                    return tu;
                }
            }

            return null;
        }
    }
}
