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
package com.globalsight.cxe.adapter.idml;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

import com.globalsight.cxe.adapter.openoffice.StringIndex;

class TextFrameObj
{
    int pageNum = 0;
    String parentStory = null;
    String itemTransform = null;
    double pointX = 0;
    double pointY = 0;
    boolean isHidden = false;

    double[] pathPointA = new double[2];
    double[] pathPointB = new double[2];
    double[] pathPointC = new double[2];
    double[] pathPointD = new double[2];

    boolean pathPointASet = false;
    boolean pathPointBSet = false;
    boolean pathPointCSet = false;
    boolean pathPointDSet = false;

    protected static TextFrameObj createInstance(String DOMVersion,
            String textFrameTag, String storyId, String itemTransform,
            String content, Matcher mTextFrame, List<String> overrideList)
    {
        boolean isVersion8 = false;
        try
        {
            if (DOMVersion != null)
            {
                double vv = Double.parseDouble(DOMVersion);

                isVersion8 = (vv >= 8.0);
            }
        }
        catch (Exception ex)
        {
            // ignore
        }

        // get Self id
        String selfId = null;
        try
        {
            StringIndex si = StringIndex.getValueBetween(textFrameTag, 0,
                    "Self=\"", "\"");
            if (si != null)
            {
                selfId = si.value;
            }
        }
        catch (Exception ex)
        {
            // ignore
        }

        TextFrameObj textFrame = new TextFrameObj();
        textFrame.parentStory = storyId;
        textFrame.itemTransform = itemTransform;
        String[] arrTransform = itemTransform.trim().split(" ");

        if (arrTransform.length == 6)
        {
            textFrame.pointX = Double.parseDouble(arrTransform[4].trim());
            textFrame.pointY = Double.parseDouble(arrTransform[5].trim());

            double pA = Double.parseDouble(arrTransform[0].trim());
            double pB = Double.parseDouble(arrTransform[1].trim());
            double pC = Double.parseDouble(arrTransform[2].trim());
            double pD = Double.parseDouble(arrTransform[3].trim());
            double[] sideLength = new double[2];

            try
            {
                int end = mTextFrame.end();
                int textFrameEnd = content.indexOf("</TextFrame>", end);
                StringIndex si = StringIndex.getValueBetween(content, end,
                        " Anchor=\"", "\"");
                int count = 1;
                while (si != null && count <= 4 && si.end < textFrameEnd)
                {

                    String[] point = si.value.split(" ");
                    if (point != null && point.length == 2)
                    {
                        double p0 = Double.parseDouble(point[0]);
                        double p1 = Double.parseDouble(point[1]);
                        if (count == 1)
                        {
                            textFrame.pathPointA[0] = p0;
                            textFrame.pathPointA[1] = p1;
                            textFrame.pathPointASet = true;
                        }
                        else if (count == 2)
                        {
                            textFrame.pathPointB[0] = p0;
                            textFrame.pathPointB[1] = p1;
                            textFrame.pathPointBSet = true;
                        }
                        else if (count == 3)
                        {
                            textFrame.pathPointC[0] = p0;
                            textFrame.pathPointC[1] = p1;
                            textFrame.pathPointCSet = true;
                        }
                        else if (count == 4)
                        {
                            textFrame.pathPointD[0] = p0;
                            textFrame.pathPointD[1] = p1;
                            textFrame.pathPointDSet = true;
                        }
                    }

                    si = StringIndex.getValueBetween(content, si.end,
                            " Anchor=\"", "\"");
                    count++;
                }

                sideLength[0] = textFrame.pathPointC[0]
                        - textFrame.pathPointA[0];
                sideLength[1] = textFrame.pathPointC[1]
                        - textFrame.pathPointA[1];
            }
            catch (Exception ex)
            {
                // ignore
            }

            // 1 0 0 1 : No spread rotation
            if (pA == 1 && pB == 0 && pC == 0 && pD == 1)
            {
                if (textFrame.pathPointASet)
                {
                    textFrame.pointX = textFrame.pointX
                            + textFrame.pathPointA[0];
                    textFrame.pointY = textFrame.pointY
                            + textFrame.pathPointA[1];
                }
            }
            else if (sideLength != null && sideLength.length == 2)
            {
                // 0 -1 1 0 : 90 degree counterclockwise spread rotation
                if (pA == 0 && pB == -1 && pC == 1 && pD == 0)
                {
                    textFrame.pointX = textFrame.pointX + sideLength[0];
                    if (textFrame.pathPointASet)
                    {
                        textFrame.pointY = textFrame.pathPointA[1];
                    }
                }
                // 0 1 -1 0 : 90 degree clockwise spread rotation
                else if (pA == 0 && pB == 1 && pC == -1 && pD == 0)
                {
                    textFrame.pointX = textFrame.pointX + sideLength[0];
                    textFrame.pointY = textFrame.pointY - sideLength[0];
                }
                // -1 0 -0 -1 : 180 degree spread rotation
                else if (pA == -1 && pB == 0 && pC == 0 && pD == -1)
                {
                    textFrame.pointX = textFrame.pointX + sideLength[0];
                    if (textFrame.pathPointASet)
                    {
                        textFrame.pointY = textFrame.pathPointA[1];
                    }
                }
            }

            // override list
            if (selfId != null && overrideList != null
                    && overrideList.contains(selfId))
            {
                textFrame.pointY = Double.MIN_VALUE;
            }

            /*
             * if (isVersion8 && !textFrameTag.contains("FillTint=\"-1\"")) {
             * textFrame.pointY = textFrame.pathPointA[1]; }
             */
        }
        return textFrame;
    }

    protected TextFrameObj()
    {
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (obj instanceof TextFrameObj)
        {
            TextFrameObj tfo = (TextFrameObj) obj;

            if (this.parentStory == null && tfo.parentStory == null)
            {
                return true;
            }
            else if (this.parentStory == null || tfo.parentStory == null)
            {
                return false;
            }
            else
            {
                return this.parentStory.equals(tfo.parentStory);
            }
        }

        return false;
    }

    @Override
    public String toString()
    {
        String f = MessageFormat.format("{0}({3} {1} {2})", parentStory,
                pointX, pointY, itemTransform);
        return f;
    }

    @Override
    public int hashCode()
    {
        return (parentStory != null) ? parentStory.hashCode() : super
                .hashCode();
    }
}