/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
//===============================================================
// package : com.tetrasix.majix.rtf
// class : com.tetrasix.majix.RtfAbstractStylesheet
//===============================================================
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at
// http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is TetraSys code.
//
// The Initial Developer of the Original Code is TetraSys..
// Portions created by TetraSys are
// Copyright (C) 1998-2000 TetraSys. All Rights Reserved.
//
// Contributor(s):
//===============================================================
package com.globalsight.ling.rtf;


import java.io.IOException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

//import com.jclark.xml.output.*;

public class RtfAbstractStylesheet
    implements Cloneable
{
    private Vector    _styles;
    private Hashtable _map;
    private String    _default;

    public RtfAbstractStylesheet()
    {
        _styles = new Vector(100);
        _map = new Hashtable(100);
    }

    public void clear()
    {
        _styles.clear();
        _map.clear();
        _default = null;
    }

    public Object clone()
    {
        RtfAbstractStylesheet newssheet = new RtfAbstractStylesheet();
        newssheet._map    = (Hashtable) _map.clone();
        newssheet._styles = (Vector)    _styles.clone();
        return newssheet;
    }

    public void defineParagraphStyle(String name, String description)
    {
        AbstractStyle style = new AbstractParagraphStyle(name, description);
        _styles.addElement(style);
        _map.put(name, style);
    }

    public void defineCharacterStyle(String name, String description)
    {
        AbstractStyle style = new AbstractCharacterStyle(name, description);
        _styles.addElement(style);
        _map.put(name, style);
    }

    public String getDescription(String name)
    {
        AbstractStyle style = (AbstractStyle)_map.get(name);

        if (style != null)
        {
            return style.getDescription();
        }
        else
        {
            return null;
        }
    }

    public String getNameFromDescription(String desc)
    {
        int nbStyles = _styles.size();

        for (int ii = 0; ii < nbStyles; ii++)
        {
            AbstractStyle style = (AbstractStyle) _styles.elementAt(ii);

            if (style.getDescription().equals(desc))
            {
                return style.getName();
            }
        }

        return null;
    }

    public String getNameFromDescription(String desc,
      AbstractTranslator translator)
    {
        int nbStyles = _styles.size();

        for (int ii = 0; ii < nbStyles; ii++)
        {
            AbstractStyle style = (AbstractStyle) _styles.elementAt(ii);

            if (translator.doit(style.getDescription()).equals(desc))
            {
                return style.getName();
            }
        }

        return null;
    }

    public boolean exists(String name)
    {
        return _map.get(name) != null;
    }

    public Enumeration getStyleNames()
    {
        return new AbstractStyleEnumeration(true, true);
    }

    public Enumeration getParagraphStyleNames()
    {
        return new AbstractStyleEnumeration(true , false);
    }

    public Enumeration getCharacterStyleNames()
    {
        return new AbstractStyleEnumeration(false, true);
    }

    // TODOXXX
    public void setDefaultStyle(String name)
    {
        if (exists(name))
        {
            _default = name;
        }
        else
        {
            System.out.println("No such style : " + name);
        }
    }

    public String getDefaultStyle()
    {
        return (_default != null) ? _default : "";
    }

    /*
    public void save(XMLWriter writer) throws IOException
    {
        writer.write("\n");
        writer.startElement("abstractstylesheet");

        writer.write("\n");
        writer.startElement("pstyles");

        for (Enumeration e = getParagraphStyleNames(); e.hasMoreElements(); ) {
            String name = e.nextElement().toString();
            writer.write("\n  ");
            writer.startElement("astyle");
            writer.attribute("name", name);
            writer.attribute("description", getDescription(name));
            writer.endElement("astyle");
        }

        writer.write("\n");
        writer.endElement("pstyles");

        writer.write("\n");
        writer.startElement("cstyles");

        for (Enumeration e = getCharacterStyleNames(); e.hasMoreElements(); ) {
            String name = e.nextElement().toString();
            writer.write("\n  ");
            writer.startElement("astyle");
            writer.attribute("name", name);
            writer.attribute("description", getDescription(name));
            writer.endElement("astyle");
        }

        writer.write("\n");
        writer.endElement("cstyles");

        writer.write("\n");
        writer.endElement("abstractstylesheet");
    }
    */

    //
    // Local Classes
    //

    private abstract class AbstractStyle
    {
        private String _styleName;
        private String _description;

        AbstractStyle(String name, String description)
        {
            _styleName = name;
            _description = description;
        }

        String getName()
        {
            return _styleName;
        }

        String getDescription()
        {
            return _description;
        }
    }

    private class AbstractParagraphStyle
        extends AbstractStyle
    {
        AbstractParagraphStyle(String name, String description)
        {
            super(name, description);
        }
    }

    private class AbstractCharacterStyle
        extends AbstractStyle
    {
        AbstractCharacterStyle(String name, String description)
        {
            super(name, description);
        }
    }

    private class AbstractStyleEnumeration
        implements Enumeration
    {
        boolean _with_para;
        boolean _with_chars;
        int     _position;

        AbstractStyleEnumeration(boolean with_para, boolean with_chars)
        {
            _with_para  = with_para;
            _with_chars = with_chars;
            _position = 0;
        }

        public boolean hasMoreElements()
        {
            while (_position < _styles.size())
            {
                AbstractStyle style = (AbstractStyle)_styles.elementAt(_position);

                if (! _with_para && (style instanceof AbstractParagraphStyle))
                {
                    _position++;
                }
                else if (! _with_chars && (style instanceof AbstractCharacterStyle))
                {
                    _position++;
                }
                else
                {
                    return true;
                }
            }

            return false;
        }

        public Object nextElement()
        {
            AbstractStyle style = (AbstractStyle) _styles.elementAt(_position);

            _position++;

            return style.getName();
        }

        public Object nextStyle()
        {
            AbstractStyle style = (AbstractStyle) _styles.elementAt(_position);

            _position++;

            return style;
        }
    }
}
