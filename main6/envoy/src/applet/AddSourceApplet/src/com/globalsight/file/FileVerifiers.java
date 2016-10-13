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
package com.globalsight.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.globalsight.util.SortUtil;

public class FileVerifiers
{
    private List<Verifier> verifiers = new ArrayList<Verifier>();

    public void addVerifier(Verifier verifier)
    {
        verifiers.add(verifier);
    }

    public List<File> validate(List<File> files)
    {
        List<File> fs = new ArrayList<File>();
        Map<String, List<String>> map = new HashMap<String, List<String>>();

        for (File file : files)
        {
            boolean failed = false;
            for (Verifier verifier : verifiers)
            {
                String msg = verifier.validate(file);
                if (msg != null)
                {
                    List<String> paths = map.get(msg);
                    if (paths == null)
                    {
                        paths = new ArrayList<String>();
                        map.put(msg, paths);
                    }

                    paths.add(file.getPath());
                    failed = true;
                    break;
                }
            }

            if (!failed)
            {
                fs.add(file);
            }
        }

        if (map.size() > 0)
        {
            List<String> keys = new ArrayList<String>();
            keys.addAll(map.keySet());
            SortUtil.sort(keys);

            StringBuffer msg = new StringBuffer(
                    "<html><div style=\"padding-right: 50px;\">");
            for (String key : keys)
            {
                msg.append(key).append("<ul>");

                for (String path : map.get(key))
                {
                    msg.append("<li>").append(path).append("</li>");
                }

                msg.append("</ul>");
            }

            msg.append("</div></html>");

            JOptionPane.showMessageDialog(null, new JLabel(msg.toString()));
        }

        return fs;
    }
}
