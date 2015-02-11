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

package com.globalsight.everest.tuv;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class represents the system known types of translation units the
 * original GXML was specified as.
 */
public class TuType implements Serializable
{
    private static final Logger CATEGORY = Logger.getLogger(TuType.class);

    private String m_name = null;
    private int m_value = -1;

    // static TuTypes
    public static final TuType UNSPECIFIED = new TuType("unspecified", 0);

    // JavaScript
    public static final TuType STRING = new TuType("string", 1);

    // HTML
    public static final TuType TEXT = new TuType("text", 2); // All normal text
                                                             // to be
                                                             // translated.
    public static final TuType ABBR = new TuType("abbr", 3); // HTML4: <TD
                                                             // abbr="...">=
    public static final TuType ACCESS_KEY = new TuType("accesskey", 4); // IE:
                                                                        // <LABEL
                                                                        // for="button"
                                                                        // accesskey="s">
    public static final TuType ALT = new TuType("alt", 5); // <IMG alt="...">
    public static final TuType CHAR = new TuType("char", 6); // HTML4: <TD
                                                             // char="...">
    public static final TuType LABEL = new TuType("label", 7); // <OPTION
                                                               // label="...">
    public static final TuType PROMPT = new TuType("prompt", 8); // <ISINDEX
                                                                 // prompt="...">
    public static final TuType STANDBY = new TuType("standby", 9); // <OBJECT
                                                                   // standby="...">
    public static final TuType SUMMARY = new TuType("summary", 10); // <TABLE
                                                                    // summary="...">
    public static final TuType TITLE = new TuType("title", 11); // <XXX
                                                                // title="...">
    public static final TuType VALUE = new TuType("value", 12); // <PARAM
                                                                // value="...">
    public static final TuType LANG = new TuType("lang", 13); // <BODY
                                                              // lang="...">
    public static final TuType URL_A = new TuType("url-a", 14);
    public static final TuType URL_ANIMATION = new TuType("url-animation", 15);
    public static final TuType URL_APPLET = new TuType("url-applet", 16);
    public static final TuType URL_APPLET_CODEBASE = new TuType(
            "url-applet-codebase", 17);
    public static final TuType URL_AREA = new TuType("url-area", 18);
    public static final TuType URL_AUDIO = new TuType("url-audio", 19);
    public static final TuType URL_BASE = new TuType("url-base", 20);
    public static final TuType URL_BGSOUND = new TuType("url-bgsound", 21);
    public static final TuType URL_BLOCK_QUOTE = new TuType("url-blockquote",
            22);
    public static final TuType URL_BODY = new TuType("url-body", 23);
    public static final TuType URL_DEL = new TuType("url-del", 24);
    public static final TuType URL_EMBED = new TuType("url-embed", 25);
    public static final TuType URL_FORM = new TuType("url-form", 26);
    public static final TuType URL_FRAME = new TuType("url-frame", 27);
    public static final TuType URL_FRAME_LONG_DESC = new TuType(
            "url-frame-longdesc", 28);
    public static final TuType URL_HEAD = new TuType("url-head", 29);
    public static final TuType URL_IFRAME = new TuType("url-iframe", 30);
    public static final TuType URL_IFRAME_LONG_DESC = new TuType(
            "url-iframe-longdesc", 31);
    public static final TuType URL_IMG = new TuType("url-img", 32);
    public static final TuType URL_IMG_LONG_DESC = new TuType(
            "url-img-longdesc", 33);
    public static final TuType URL_IMG_USEMAP = new TuType("url-img-usemap", 34);
    public static final TuType URL_INPUT = new TuType("url-input", 35);
    public static final TuType URL_INPUT_USEMAP = new TuType(
            "url-input-usemap", 36);
    public static final TuType URL_INS = new TuType("url-ins", 37);
    public static final TuType URL_LAYER = new TuType("url-layer", 38);
    public static final TuType URL_LINK = new TuType("url-link", 39);
    public static final TuType URL_MEDIA = new TuType("url-media", 40);
    public static final TuType URL_Q = new TuType("url-q", 41);
    public static final TuType URL_OBJECT_DATA = new TuType("url-object-data",
            42);
    public static final TuType URL_OBJECT_CLASSID = new TuType(
            "url-object-classid", 43);
    public static final TuType URL_OBJECT_CODEBASE = new TuType(
            "url-object-codebase", 44);
    public static final TuType URLOBJECT_USEMAP = new TuType(
            "url-object-usemap", 45);
    public static final TuType URL_SCRIPT = new TuType("url-script", 46);
    public static final TuType URL_STYLE = new TuType("url-style", 47);
    public static final TuType URL_TABLE = new TuType("url-table", 48);
    public static final TuType URL_TD = new TuType("url-td", 49);
    public static final TuType URL_TH = new TuType("url-th", 50);
    public static final TuType URL_VIDEO = new TuType("url-video", 51);
    public static final TuType URL_XML = new TuType("url-xml", 52);
    public static final TuType IMG_HEIGTH = new TuType("img-height", 53);
    public static final TuType IMG_WIDTH = new TuType("img-width", 54);

    // CSS
    public static final TuType CSS_BACKGROUND_COLOR = new TuType(
            "css-background-color", 55);
    public static final TuType CSS_BACKGROUND_IMAGE = new TuType(
            "css-background-image", 56);
    public static final TuType CSS_BORDER = new TuType("css-border", 57);
    public static final TuType CSS_BORDER_COLOR = new TuType(
            "css-border-color", 58);
    public static final TuType CSS_COLOR = new TuType("css-color", 59);
    public static final TuType CSS_FONT = new TuType("css-font", 60);
    public static final TuType CSS_FONT_FAMILY = new TuType("css-font-family",
            61);
    public static final TuType CSS_FONT_SIZE = new TuType("css-font-size", 62);
    public static final TuType CSS_FONT_STYLE = new TuType("css-font-style", 63);
    public static final TuType CSS_VARIANT = new TuType("css-font-variant", 64);
    public static final TuType CSS_FONT_WEIGHT = new TuType("css-font-weight",
            65);
    public static final TuType CSS_LINE_SPACING = new TuType(
            "css-letter-spacing", 66);
    public static final TuType CSS_MARGIN = new TuType("css-margin", 67);
    public static final TuType CSS_MARGIN_BOTTOM = new TuType(
            "css-margin-bottom", 68);
    public static final TuType CSS_MARGIN_LEFT = new TuType("css-margin-left",
            69);
    public static final TuType CSS_MARGIN_RIGHT = new TuType(
            "css-margin-right", 70);
    public static final TuType CSS_MARGN_TOP = new TuType("css-margin-top", 71);
    public static final TuType CSS_PADDING = new TuType("css-padding", 72);
    public static final TuType CSS_PADDING_BOTTOM = new TuType(
            "css-padding-bottom", 73);
    public static final TuType CSS_PADDING_LEFT = new TuType(
            "css-padding-left", 74);
    public static final TuType CSS_PADDING_RIGHT = new TuType(
            "css-padding-right", 75);
    public static final TuType CSS_PADDING_TOP = new TuType("css-padding-top",
            76);
    public static final TuType CSS_TEXT_ALIGN = new TuType("css-text-align", 77);
    public static final TuType CSS_TEXT_DECORATION = new TuType(
            "css-text-decoration", 78);
    public static final TuType CSS_TEXT_INDENT = new TuType("css-text-indent",
            79);
    public static final TuType CSS_TEXT_TRANSFORM = new TuType(
            "css-text-transform", 80);
    public static final TuType CSS_WORD_SPACING = new TuType(
            "css-word-spacing", 81);
    public static final TuType CSS_BORDER_BOTTOM_COLOR = new TuType(
            "css-border-bottom-color", 82);
    public static final TuType CSS_BORDER_LEFT_COLOR = new TuType(
            "css-border-left-color", 83);
    public static final TuType CSS_BORDER_RIGHT_COLOR = new TuType(
            "css-border-right-color", 84);
    public static final TuType CSS_BORDER_TOP_COLOR = new TuType(
            "css-border-top-color", 85);
    public static final TuType CSS_CONTENT = new TuType("css-content", 86);
    public static final TuType CSS_COUNTER_INCREMENT = new TuType(
            "css-counter-increment", 87);
    public static final TuType CSS_COUNTER_RESET = new TuType(
            "css-counter-reset", 88);
    public static final TuType CSS_CUE = new TuType("css-cue", 89);
    public static final TuType CSS_CUE_AFTER = new TuType("css-cue-after", 90);
    public static final TuType CSS_CUE_BEFORE = new TuType("css-cue-before", 91);
    public static final TuType CSS_CURSOR = new TuType("css-cursor", 92);
    public static final TuType CSS_DIRECTION = new TuType("css-direction", 93);
    public static final TuType CSS_FONT_SIZE_ADJUST = new TuType(
            "css-font-size-adjust", 94);
    public static final TuType CSS_FONT_STRETCH = new TuType(
            "css-font-stretch", 95);
    public static final TuType CSS_ORPHANS = new TuType("css-orphans", 96);
    public static final TuType CSS_OUTLINE = new TuType("css-outline", 97);
    public static final TuType CSS_OUTLINE_COLOR = new TuType(
            "css-outline-color", 98);
    public static final TuType CSS_OUTLINE_STYLE = new TuType(
            "css-outline-style", 99);
    public static final TuType CSS_OUTLINE_WIDTH = new TuType(
            "css-outline-width", 100);
    public static final TuType CSS_OVERFLOW = new TuType("css-overflow", 101);
    public static final TuType CSS_PAGE_BREAK_AFTER = new TuType(
            "css-page-break-after", 102);
    public static final TuType CSS_PAGE_BREAK_BEFORE = new TuType(
            "css-page-break-before", 103);
    public static final TuType CSS_PAGE_BREAK_INSIDE = new TuType(
            "css-page-break-inside", 104);
    public static final TuType CSS_PAUSE = new TuType("css-pause", 105);
    public static final TuType CSS_PAUSE_AFTER = new TuType("css-pause-after",
            106);
    public static final TuType CSS_PAUSE_BEFORE = new TuType(
            "css-pause-before", 107);
    public static final TuType CSS_PITCH = new TuType("css-pitch", 108);
    public static final TuType CSS_PITCH_RANGE = new TuType("css-pitch-range",
            109);
    public static final TuType CSS_QUOTES = new TuType("css-quotes", 110);
    public static final TuType CSS_RICHNESS = new TuType("css-richness", 111);
    public static final TuType CSS_SPEAK = new TuType("css-speak", 112);
    public static final TuType CSS_SPEAK_DATE = new TuType("css-speak-date",
            113);
    public static final TuType CSS_SPEAK_HEADER = new TuType(
            "css-speak-header", 114);
    public static final TuType CSS_SPEAK_PUNCTUATION = new TuType(
            "css-speak-punctuation", 115);
    public static final TuType CSS_SPEAK_TIME = new TuType("css-speak-time",
            116);
    public static final TuType CSS_SPEAK_RATE = new TuType("css-speech-rate",
            117);
    public static final TuType CSS_STRESS = new TuType("css-stress", 118);
    public static final TuType CSS_UNICODE_BIDI = new TuType(
            "css-unicode-bidi", 119);
    public static final TuType CSS_VOICE_FAMILY = new TuType(
            "css-voice-family", 120);
    public static final TuType CSS_VOLUME = new TuType("css-volume", 121);
    public static final TuType CSS_WIDOWS = new TuType("css-widows", 122);
    public static final TuType CSS_BEHAVIOR = new TuType("css-behavior", 123);
    public static final TuType CSS_FILTER = new TuType("css-filter", 124);
    public static final TuType CSS_IME_MODE = new TuType("css-ime-mode", 125);
    public static final TuType CSS_OVERFLOW_X = new TuType("css-overflow-x",
            126);
    public static final TuType CSS_OVERFLOW_Y = new TuType("css-overflow-y",
            127);
    public static final TuType CSS_RUBY_ALIGN = new TuType("css-ruby-align",
            128);
    public static final TuType CSS_RUBY_OVERHANG = new TuType(
            "css-ruby-overhang", 129);
    public static final TuType CSS_RUBY_POSITION = new TuType(
            "css-ruby-position", 130);
    public static final TuType CSS_TEXT_AUTOSPACE = new TuType(
            "css-text-autospace", 131);
    public static final TuType CSS_TEXT_JUSTIFY = new TuType(
            "css-text-justify", 132);
    public static final TuType CSS_WORD_BREAK = new TuType("css-word-break",
            133);

    // TODO CvdL: this should be renamed in the CSS extractor to
    // url-style-import or so. Defined in ExtractionHandler.java.
    public static final TuType CSS_STYLE_URL = new TuType("style-url", 134);

    // JHTML
    public static final TuType URL_DROPLET = new TuType("url-droplet", 135);

    // CFM
    public static final TuType URL_CFGRID = new TuType("url-cfgrid", 136);
    public static final TuType URL_CFFORM = new TuType("url-cfform", 137);
    public static final TuType URL_CFHTTP = new TuType("url-cfhttp", 138);
    public static final TuType URL_CFLOCATION = new TuType("url-cflocation",
            139);

    // JHTML's include comment
    public static final TuType SSI_INCLUDE = new TuType("ssi-include", 140);

    public static final TuType META_CONTENT = new TuType("meta-content", 161);
    public static final TuType CHARSET = new TuType("charset", 162);

    public static final int All_TUTYPES_SIZE = 143;

    public static final TuType[] ALL_TUTYPES = new TuType[All_TUTYPES_SIZE];
    public static final Map ALL_TUTYPES_BY_VALUE = new HashMap(All_TUTYPES_SIZE);
    public static final Map ALL_TUTYPES_BY_NAME = new HashMap(All_TUTYPES_SIZE);

    static
    {
        ALL_TUTYPES_BY_VALUE.put(new Integer(UNSPECIFIED.getValue()),
                UNSPECIFIED);
        ALL_TUTYPES_BY_VALUE.put(new Integer(STRING.getValue()), STRING);
        ALL_TUTYPES_BY_VALUE.put(new Integer(TEXT.getValue()), TEXT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(ABBR.getValue()), ABBR);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(ACCESS_KEY.getValue()), ACCESS_KEY);
        ALL_TUTYPES_BY_VALUE.put(new Integer(ALT.getValue()), ALT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CHAR.getValue()), CHAR);
        ALL_TUTYPES_BY_VALUE.put(new Integer(LABEL.getValue()), LABEL);
        ALL_TUTYPES_BY_VALUE.put(new Integer(PROMPT.getValue()), PROMPT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(STANDBY.getValue()), STANDBY);
        ALL_TUTYPES_BY_VALUE.put(new Integer(SUMMARY.getValue()), SUMMARY);
        ALL_TUTYPES_BY_VALUE.put(new Integer(TITLE.getValue()), TITLE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(VALUE.getValue()), VALUE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(LANG.getValue()), LANG);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_A.getValue()), URL_A);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_ANIMATION.getValue()),
                URL_ANIMATION);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(URL_APPLET.getValue()), URL_APPLET);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_APPLET_CODEBASE.getValue()),
                URL_APPLET_CODEBASE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_AREA.getValue()), URL_AREA);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_AUDIO.getValue()), URL_AUDIO);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_BASE.getValue()), URL_BASE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_BGSOUND.getValue()),
                URL_BGSOUND);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_BLOCK_QUOTE.getValue()),
                URL_BLOCK_QUOTE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_BODY.getValue()), URL_BODY);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_DEL.getValue()), URL_DEL);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_EMBED.getValue()), URL_EMBED);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_FORM.getValue()), URL_FORM);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_FRAME.getValue()), URL_FRAME);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_FRAME_LONG_DESC.getValue()),
                URL_FRAME_LONG_DESC);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_HEAD.getValue()), URL_HEAD);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(URL_IFRAME.getValue()), URL_IFRAME);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_IFRAME_LONG_DESC.getValue()),
                URL_IFRAME_LONG_DESC);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_IMG.getValue()), URL_IMG);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_IMG_LONG_DESC.getValue()),
                URL_IMG_LONG_DESC);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_IMG_USEMAP.getValue()),
                URL_IMG_USEMAP);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_INPUT.getValue()), URL_INPUT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_INPUT_USEMAP.getValue()),
                URL_INPUT_USEMAP);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_INS.getValue()), URL_INS);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_LAYER.getValue()), URL_LAYER);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_LINK.getValue()), URL_LINK);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_MEDIA.getValue()), URL_MEDIA);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_Q.getValue()), URL_Q);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_OBJECT_DATA.getValue()),
                URL_OBJECT_DATA);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_OBJECT_CLASSID.getValue()),
                URL_OBJECT_CLASSID);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_OBJECT_CODEBASE.getValue()),
                URL_OBJECT_CODEBASE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URLOBJECT_USEMAP.getValue()),
                URLOBJECT_USEMAP);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(URL_SCRIPT.getValue()), URL_SCRIPT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_STYLE.getValue()), URL_STYLE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_TABLE.getValue()), URL_TABLE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_TD.getValue()), URL_TD);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_TH.getValue()), URL_TH);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_VIDEO.getValue()), URL_VIDEO);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_XML.getValue()), URL_XML);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(IMG_HEIGTH.getValue()), IMG_HEIGTH);
        ALL_TUTYPES_BY_VALUE.put(new Integer(IMG_WIDTH.getValue()), IMG_WIDTH);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_BACKGROUND_COLOR.getValue()),
                CSS_BACKGROUND_COLOR);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_BACKGROUND_IMAGE.getValue()),
                CSS_BACKGROUND_IMAGE);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(CSS_BORDER.getValue()), CSS_BORDER);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_BORDER_COLOR.getValue()),
                CSS_BORDER_COLOR);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_COLOR.getValue()), CSS_COLOR);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_FONT.getValue()), CSS_FONT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_FONT_FAMILY.getValue()),
                CSS_FONT_FAMILY);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_FONT_SIZE.getValue()),
                CSS_FONT_SIZE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_FONT_STYLE.getValue()),
                CSS_FONT_STYLE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_VARIANT.getValue()),
                CSS_VARIANT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_FONT_WEIGHT.getValue()),
                CSS_FONT_WEIGHT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_LINE_SPACING.getValue()),
                CSS_LINE_SPACING);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(CSS_MARGIN.getValue()), CSS_MARGIN);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_MARGIN_BOTTOM.getValue()),
                CSS_MARGIN_BOTTOM);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_MARGIN_LEFT.getValue()),
                CSS_MARGIN_LEFT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_MARGIN_RIGHT.getValue()),
                CSS_MARGIN_RIGHT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_MARGN_TOP.getValue()),
                CSS_MARGN_TOP);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PADDING.getValue()),
                CSS_PADDING);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PADDING_BOTTOM.getValue()),
                CSS_PADDING_BOTTOM);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PADDING_LEFT.getValue()),
                CSS_PADDING_LEFT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PADDING_RIGHT.getValue()),
                CSS_PADDING_RIGHT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PADDING_TOP.getValue()),
                CSS_PADDING_TOP);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_TEXT_ALIGN.getValue()),
                CSS_TEXT_ALIGN);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_TEXT_DECORATION.getValue()),
                CSS_TEXT_DECORATION);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_TEXT_INDENT.getValue()),
                CSS_TEXT_INDENT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_TEXT_TRANSFORM.getValue()),
                CSS_TEXT_TRANSFORM);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_WORD_SPACING.getValue()),
                CSS_WORD_SPACING);
        ALL_TUTYPES_BY_VALUE.put(
                new Integer(CSS_BORDER_BOTTOM_COLOR.getValue()),
                CSS_BORDER_BOTTOM_COLOR);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_BORDER_LEFT_COLOR.getValue()),
                CSS_BORDER_LEFT_COLOR);
        ALL_TUTYPES_BY_VALUE.put(
                new Integer(CSS_BORDER_RIGHT_COLOR.getValue()),
                CSS_BORDER_RIGHT_COLOR);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_BORDER_TOP_COLOR.getValue()),
                CSS_BORDER_TOP_COLOR);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_CONTENT.getValue()),
                CSS_CONTENT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_COUNTER_INCREMENT.getValue()),
                CSS_COUNTER_INCREMENT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_COUNTER_RESET.getValue()),
                CSS_COUNTER_RESET);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_CUE.getValue()), CSS_CUE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_CUE_AFTER.getValue()),
                CSS_CUE_AFTER);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_CUE_BEFORE.getValue()),
                CSS_CUE_BEFORE);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(CSS_CURSOR.getValue()), CSS_CURSOR);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_DIRECTION.getValue()),
                CSS_DIRECTION);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_FONT_SIZE_ADJUST.getValue()),
                CSS_FONT_SIZE_ADJUST);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_FONT_STRETCH.getValue()),
                CSS_FONT_STRETCH);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_ORPHANS.getValue()),
                CSS_ORPHANS);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_OUTLINE.getValue()),
                CSS_OUTLINE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_OUTLINE_COLOR.getValue()),
                CSS_OUTLINE_COLOR);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_OUTLINE_STYLE.getValue()),
                CSS_OUTLINE_STYLE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_OUTLINE_WIDTH.getValue()),
                CSS_OUTLINE_WIDTH);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_OVERFLOW.getValue()),
                CSS_OVERFLOW);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PAGE_BREAK_AFTER.getValue()),
                CSS_PAGE_BREAK_AFTER);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PAGE_BREAK_BEFORE.getValue()),
                CSS_PAGE_BREAK_BEFORE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PAGE_BREAK_INSIDE.getValue()),
                CSS_PAGE_BREAK_INSIDE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PAUSE.getValue()), CSS_PAUSE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PAUSE_AFTER.getValue()),
                CSS_PAUSE_AFTER);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PAUSE_BEFORE.getValue()),
                CSS_PAUSE_BEFORE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PITCH.getValue()), CSS_PITCH);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_PITCH_RANGE.getValue()),
                CSS_PITCH_RANGE);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(CSS_QUOTES.getValue()), CSS_QUOTES);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_RICHNESS.getValue()),
                CSS_RICHNESS);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_SPEAK.getValue()), CSS_SPEAK);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_SPEAK_DATE.getValue()),
                CSS_SPEAK_DATE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_SPEAK_HEADER.getValue()),
                CSS_SPEAK_HEADER);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_SPEAK_PUNCTUATION.getValue()),
                CSS_SPEAK_PUNCTUATION);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_SPEAK_TIME.getValue()),
                CSS_SPEAK_TIME);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_SPEAK_RATE.getValue()),
                CSS_SPEAK_RATE);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(CSS_STRESS.getValue()), CSS_STRESS);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_UNICODE_BIDI.getValue()),
                CSS_UNICODE_BIDI);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_VOICE_FAMILY.getValue()),
                CSS_VOICE_FAMILY);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(CSS_VOLUME.getValue()), CSS_VOLUME);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(CSS_WIDOWS.getValue()), CSS_WIDOWS);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_BEHAVIOR.getValue()),
                CSS_BEHAVIOR);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(CSS_FILTER.getValue()), CSS_FILTER);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_IME_MODE.getValue()),
                CSS_IME_MODE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_OVERFLOW_X.getValue()),
                CSS_OVERFLOW_X);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_OVERFLOW_Y.getValue()),
                CSS_OVERFLOW_Y);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_RUBY_ALIGN.getValue()),
                CSS_RUBY_ALIGN);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_RUBY_OVERHANG.getValue()),
                CSS_RUBY_OVERHANG);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_RUBY_POSITION.getValue()),
                CSS_RUBY_POSITION);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_TEXT_AUTOSPACE.getValue()),
                CSS_TEXT_AUTOSPACE);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_TEXT_JUSTIFY.getValue()),
                CSS_TEXT_JUSTIFY);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_WORD_BREAK.getValue()),
                CSS_WORD_BREAK);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CSS_STYLE_URL.getValue()),
                CSS_STYLE_URL);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_DROPLET.getValue()),
                URL_DROPLET);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(URL_CFGRID.getValue()), URL_CFGRID);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(URL_CFFORM.getValue()), URL_CFFORM);
        ALL_TUTYPES_BY_VALUE
                .put(new Integer(URL_CFHTTP.getValue()), URL_CFHTTP);
        ALL_TUTYPES_BY_VALUE.put(new Integer(URL_CFLOCATION.getValue()),
                URL_CFLOCATION);

        ALL_TUTYPES_BY_VALUE.put(new Integer(SSI_INCLUDE.getValue()),
                SSI_INCLUDE);

        ALL_TUTYPES_BY_VALUE.put(new Integer(META_CONTENT.getValue()),
                META_CONTENT);
        ALL_TUTYPES_BY_VALUE.put(new Integer(CHARSET.getValue()), CHARSET);

        // ************************************************

        ALL_TUTYPES_BY_NAME.put(UNSPECIFIED.getName(), UNSPECIFIED);
        ALL_TUTYPES_BY_NAME.put(STRING.getName(), STRING);
        ALL_TUTYPES_BY_NAME.put(TEXT.getName(), TEXT);
        ALL_TUTYPES_BY_NAME.put(ABBR.getName(), ABBR);
        ALL_TUTYPES_BY_NAME.put(ACCESS_KEY.getName(), ACCESS_KEY);
        ALL_TUTYPES_BY_NAME.put(ALT.getName(), ALT);
        ALL_TUTYPES_BY_NAME.put(CHAR.getName(), CHAR);
        ALL_TUTYPES_BY_NAME.put(LABEL.getName(), LABEL);
        ALL_TUTYPES_BY_NAME.put(PROMPT.getName(), PROMPT);
        ALL_TUTYPES_BY_NAME.put(STANDBY.getName(), STANDBY);
        ALL_TUTYPES_BY_NAME.put(SUMMARY.getName(), SUMMARY);
        ALL_TUTYPES_BY_NAME.put(TITLE.getName(), TITLE);
        ALL_TUTYPES_BY_NAME.put(VALUE.getName(), VALUE);
        ALL_TUTYPES_BY_NAME.put(LANG.getName(), LANG);
        ALL_TUTYPES_BY_NAME.put(URL_A.getName(), URL_A);
        ALL_TUTYPES_BY_NAME.put(URL_ANIMATION.getName(), URL_ANIMATION);
        ALL_TUTYPES_BY_NAME.put(URL_APPLET.getName(), URL_APPLET);
        ALL_TUTYPES_BY_NAME.put(URL_APPLET_CODEBASE.getName(),
                URL_APPLET_CODEBASE);
        ALL_TUTYPES_BY_NAME.put(URL_AREA.getName(), URL_AREA);
        ALL_TUTYPES_BY_NAME.put(URL_AUDIO.getName(), URL_AUDIO);
        ALL_TUTYPES_BY_NAME.put(URL_BASE.getName(), URL_BASE);
        ALL_TUTYPES_BY_NAME.put(URL_BGSOUND.getName(), URL_BGSOUND);
        ALL_TUTYPES_BY_NAME.put(URL_BLOCK_QUOTE.getName(), URL_BLOCK_QUOTE);
        ALL_TUTYPES_BY_NAME.put(URL_BODY.getName(), URL_BODY);
        ALL_TUTYPES_BY_NAME.put(URL_DEL.getName(), URL_DEL);
        ALL_TUTYPES_BY_NAME.put(URL_EMBED.getName(), URL_EMBED);
        ALL_TUTYPES_BY_NAME.put(URL_FORM.getName(), URL_FORM);
        ALL_TUTYPES_BY_NAME.put(URL_FRAME.getName(), URL_FRAME);
        ALL_TUTYPES_BY_NAME.put(URL_FRAME_LONG_DESC.getName(),
                URL_FRAME_LONG_DESC);
        ALL_TUTYPES_BY_NAME.put(URL_HEAD.getName(), URL_HEAD);
        ALL_TUTYPES_BY_NAME.put(URL_IFRAME.getName(), URL_IFRAME);
        ALL_TUTYPES_BY_NAME.put(URL_IFRAME_LONG_DESC.getName(),
                URL_IFRAME_LONG_DESC);
        ALL_TUTYPES_BY_NAME.put(URL_IMG.getName(), URL_IMG);
        ALL_TUTYPES_BY_NAME.put(URL_IMG_LONG_DESC.getName(), URL_IMG_LONG_DESC);
        ALL_TUTYPES_BY_NAME.put(URL_IMG_USEMAP.getName(), URL_IMG_USEMAP);
        ALL_TUTYPES_BY_NAME.put(URL_INPUT.getName(), URL_INPUT);
        ALL_TUTYPES_BY_NAME.put(URL_INPUT_USEMAP.getName(), URL_INPUT_USEMAP);
        ALL_TUTYPES_BY_NAME.put(URL_INS.getName(), URL_INS);
        ALL_TUTYPES_BY_NAME.put(URL_LAYER.getName(), URL_LAYER);
        ALL_TUTYPES_BY_NAME.put(URL_LINK.getName(), URL_LINK);
        ALL_TUTYPES_BY_NAME.put(URL_MEDIA.getName(), URL_MEDIA);
        ALL_TUTYPES_BY_NAME.put(URL_Q.getName(), URL_Q);
        ALL_TUTYPES_BY_NAME.put(URL_OBJECT_DATA.getName(), URL_OBJECT_DATA);
        ALL_TUTYPES_BY_NAME.put(URL_OBJECT_CLASSID.getName(),
                URL_OBJECT_CLASSID);
        ALL_TUTYPES_BY_NAME.put(URL_OBJECT_CODEBASE.getName(),
                URL_OBJECT_CODEBASE);
        ALL_TUTYPES_BY_NAME.put(URLOBJECT_USEMAP.getName(), URLOBJECT_USEMAP);
        ALL_TUTYPES_BY_NAME.put(URL_SCRIPT.getName(), URL_SCRIPT);
        ALL_TUTYPES_BY_NAME.put(URL_STYLE.getName(), URL_STYLE);
        ALL_TUTYPES_BY_NAME.put(URL_TABLE.getName(), URL_TABLE);
        ALL_TUTYPES_BY_NAME.put(URL_TD.getName(), URL_TD);
        ALL_TUTYPES_BY_NAME.put(URL_TH.getName(), URL_TH);
        ALL_TUTYPES_BY_NAME.put(URL_VIDEO.getName(), URL_VIDEO);
        ALL_TUTYPES_BY_NAME.put(URL_XML.getName(), URL_XML);
        ALL_TUTYPES_BY_NAME.put(IMG_HEIGTH.getName(), IMG_HEIGTH);
        ALL_TUTYPES_BY_NAME.put(IMG_WIDTH.getName(), IMG_WIDTH);
        ALL_TUTYPES_BY_NAME.put(CSS_BACKGROUND_COLOR.getName(),
                CSS_BACKGROUND_COLOR);
        ALL_TUTYPES_BY_NAME.put(CSS_BACKGROUND_IMAGE.getName(),
                CSS_BACKGROUND_IMAGE);
        ALL_TUTYPES_BY_NAME.put(CSS_BORDER.getName(), CSS_BORDER);
        ALL_TUTYPES_BY_NAME.put(CSS_BORDER_COLOR.getName(), CSS_BORDER_COLOR);
        ALL_TUTYPES_BY_NAME.put(CSS_COLOR.getName(), CSS_COLOR);
        ALL_TUTYPES_BY_NAME.put(CSS_FONT.getName(), CSS_FONT);
        ALL_TUTYPES_BY_NAME.put(CSS_FONT_FAMILY.getName(), CSS_FONT_FAMILY);
        ALL_TUTYPES_BY_NAME.put(CSS_FONT_SIZE.getName(), CSS_FONT_SIZE);
        ALL_TUTYPES_BY_NAME.put(CSS_FONT_STYLE.getName(), CSS_FONT_STYLE);
        ALL_TUTYPES_BY_NAME.put(CSS_VARIANT.getName(), CSS_VARIANT);
        ALL_TUTYPES_BY_NAME.put(CSS_FONT_WEIGHT.getName(), CSS_FONT_WEIGHT);
        ALL_TUTYPES_BY_NAME.put(CSS_LINE_SPACING.getName(), CSS_LINE_SPACING);
        ALL_TUTYPES_BY_NAME.put(CSS_MARGIN.getName(), CSS_MARGIN);
        ALL_TUTYPES_BY_NAME.put(CSS_MARGIN_BOTTOM.getName(), CSS_MARGIN_BOTTOM);
        ALL_TUTYPES_BY_NAME.put(CSS_MARGIN_LEFT.getName(), CSS_MARGIN_LEFT);
        ALL_TUTYPES_BY_NAME.put(CSS_MARGIN_RIGHT.getName(), CSS_MARGIN_RIGHT);
        ALL_TUTYPES_BY_NAME.put(CSS_MARGN_TOP.getName(), CSS_MARGN_TOP);
        ALL_TUTYPES_BY_NAME.put(CSS_PADDING.getName(), CSS_PADDING);
        ALL_TUTYPES_BY_NAME.put(CSS_PADDING_BOTTOM.getName(),
                CSS_PADDING_BOTTOM);
        ALL_TUTYPES_BY_NAME.put(CSS_PADDING_LEFT.getName(), CSS_PADDING_LEFT);
        ALL_TUTYPES_BY_NAME.put(CSS_PADDING_RIGHT.getName(), CSS_PADDING_RIGHT);
        ALL_TUTYPES_BY_NAME.put(CSS_PADDING_TOP.getName(), CSS_PADDING_TOP);
        ALL_TUTYPES_BY_NAME.put(CSS_TEXT_ALIGN.getName(), CSS_TEXT_ALIGN);
        ALL_TUTYPES_BY_NAME.put(CSS_TEXT_DECORATION.getName(),
                CSS_TEXT_DECORATION);
        ALL_TUTYPES_BY_NAME.put(CSS_TEXT_INDENT.getName(), CSS_TEXT_INDENT);
        ALL_TUTYPES_BY_NAME.put(CSS_TEXT_TRANSFORM.getName(),
                CSS_TEXT_TRANSFORM);
        ALL_TUTYPES_BY_NAME.put(CSS_WORD_SPACING.getName(), CSS_WORD_SPACING);
        ALL_TUTYPES_BY_NAME.put(CSS_BORDER_BOTTOM_COLOR.getName(),
                CSS_BORDER_BOTTOM_COLOR);
        ALL_TUTYPES_BY_NAME.put(CSS_BORDER_LEFT_COLOR.getName(),
                CSS_BORDER_LEFT_COLOR);
        ALL_TUTYPES_BY_NAME.put(CSS_BORDER_RIGHT_COLOR.getName(),
                CSS_BORDER_RIGHT_COLOR);
        ALL_TUTYPES_BY_NAME.put(CSS_BORDER_TOP_COLOR.getName(),
                CSS_BORDER_TOP_COLOR);
        ALL_TUTYPES_BY_NAME.put(CSS_CONTENT.getName(), CSS_CONTENT);
        ALL_TUTYPES_BY_NAME.put(CSS_COUNTER_INCREMENT.getName(),
                CSS_COUNTER_INCREMENT);
        ALL_TUTYPES_BY_NAME.put(CSS_COUNTER_RESET.getName(), CSS_COUNTER_RESET);
        ALL_TUTYPES_BY_NAME.put(CSS_CUE.getName(), CSS_CUE);
        ALL_TUTYPES_BY_NAME.put(CSS_CUE_AFTER.getName(), CSS_CUE_AFTER);
        ALL_TUTYPES_BY_NAME.put(CSS_CUE_BEFORE.getName(), CSS_CUE_BEFORE);
        ALL_TUTYPES_BY_NAME.put(CSS_CURSOR.getName(), CSS_CURSOR);
        ALL_TUTYPES_BY_NAME.put(CSS_DIRECTION.getName(), CSS_DIRECTION);
        ALL_TUTYPES_BY_NAME.put(CSS_FONT_SIZE_ADJUST.getName(),
                CSS_FONT_SIZE_ADJUST);
        ALL_TUTYPES_BY_NAME.put(CSS_FONT_STRETCH.getName(), CSS_FONT_STRETCH);
        ALL_TUTYPES_BY_NAME.put(CSS_ORPHANS.getName(), CSS_ORPHANS);
        ALL_TUTYPES_BY_NAME.put(CSS_OUTLINE.getName(), CSS_OUTLINE);
        ALL_TUTYPES_BY_NAME.put(CSS_OUTLINE_COLOR.getName(), CSS_OUTLINE_COLOR);
        ALL_TUTYPES_BY_NAME.put(CSS_OUTLINE_STYLE.getName(), CSS_OUTLINE_STYLE);
        ALL_TUTYPES_BY_NAME.put(CSS_OUTLINE_WIDTH.getName(), CSS_OUTLINE_WIDTH);
        ALL_TUTYPES_BY_NAME.put(CSS_OVERFLOW.getName(), CSS_OVERFLOW);
        ALL_TUTYPES_BY_NAME.put(CSS_PAGE_BREAK_AFTER.getName(),
                CSS_PAGE_BREAK_AFTER);
        ALL_TUTYPES_BY_NAME.put(CSS_PAGE_BREAK_BEFORE.getName(),
                CSS_PAGE_BREAK_BEFORE);
        ALL_TUTYPES_BY_NAME.put(CSS_PAGE_BREAK_INSIDE.getName(),
                CSS_PAGE_BREAK_INSIDE);
        ALL_TUTYPES_BY_NAME.put(CSS_PAUSE.getName(), CSS_PAUSE);
        ALL_TUTYPES_BY_NAME.put(CSS_PAUSE_AFTER.getName(), CSS_PAUSE_AFTER);
        ALL_TUTYPES_BY_NAME.put(CSS_PAUSE_BEFORE.getName(), CSS_PAUSE_BEFORE);
        ALL_TUTYPES_BY_NAME.put(CSS_PITCH.getName(), CSS_PITCH);
        ALL_TUTYPES_BY_NAME.put(CSS_PITCH_RANGE.getName(), CSS_PITCH_RANGE);
        ALL_TUTYPES_BY_NAME.put(CSS_QUOTES.getName(), CSS_QUOTES);
        ALL_TUTYPES_BY_NAME.put(CSS_RICHNESS.getName(), CSS_RICHNESS);
        ALL_TUTYPES_BY_NAME.put(CSS_SPEAK.getName(), CSS_SPEAK);
        ALL_TUTYPES_BY_NAME.put(CSS_SPEAK_DATE.getName(), CSS_SPEAK_DATE);
        ALL_TUTYPES_BY_NAME.put(CSS_SPEAK_HEADER.getName(), CSS_SPEAK_HEADER);
        ALL_TUTYPES_BY_NAME.put(CSS_SPEAK_PUNCTUATION.getName(),
                CSS_SPEAK_PUNCTUATION);
        ALL_TUTYPES_BY_NAME.put(CSS_SPEAK_TIME.getName(), CSS_SPEAK_TIME);
        ALL_TUTYPES_BY_NAME.put(CSS_SPEAK_RATE.getName(), CSS_SPEAK_RATE);
        ALL_TUTYPES_BY_NAME.put(CSS_STRESS.getName(), CSS_STRESS);
        ALL_TUTYPES_BY_NAME.put(CSS_UNICODE_BIDI.getName(), CSS_UNICODE_BIDI);
        ALL_TUTYPES_BY_NAME.put(CSS_VOICE_FAMILY.getName(), CSS_VOICE_FAMILY);
        ALL_TUTYPES_BY_NAME.put(CSS_VOLUME.getName(), CSS_VOLUME);
        ALL_TUTYPES_BY_NAME.put(CSS_WIDOWS.getName(), CSS_WIDOWS);
        ALL_TUTYPES_BY_NAME.put(CSS_BEHAVIOR.getName(), CSS_BEHAVIOR);
        ALL_TUTYPES_BY_NAME.put(CSS_FILTER.getName(), CSS_FILTER);
        ALL_TUTYPES_BY_NAME.put(CSS_IME_MODE.getName(), CSS_IME_MODE);
        ALL_TUTYPES_BY_NAME.put(CSS_OVERFLOW_X.getName(), CSS_OVERFLOW_X);
        ALL_TUTYPES_BY_NAME.put(CSS_OVERFLOW_Y.getName(), CSS_OVERFLOW_Y);
        ALL_TUTYPES_BY_NAME.put(CSS_RUBY_ALIGN.getName(), CSS_RUBY_ALIGN);
        ALL_TUTYPES_BY_NAME.put(CSS_RUBY_OVERHANG.getName(), CSS_RUBY_OVERHANG);
        ALL_TUTYPES_BY_NAME.put(CSS_RUBY_POSITION.getName(), CSS_RUBY_POSITION);
        ALL_TUTYPES_BY_NAME.put(CSS_TEXT_AUTOSPACE.getName(),
                CSS_TEXT_AUTOSPACE);
        ALL_TUTYPES_BY_NAME.put(CSS_TEXT_JUSTIFY.getName(), CSS_TEXT_JUSTIFY);
        ALL_TUTYPES_BY_NAME.put(CSS_WORD_BREAK.getName(), CSS_WORD_BREAK);
        ALL_TUTYPES_BY_NAME.put(CSS_STYLE_URL.getName(), CSS_STYLE_URL);
        ALL_TUTYPES_BY_NAME.put(URL_DROPLET.getName(), URL_DROPLET);
        ALL_TUTYPES_BY_NAME.put(URL_CFGRID.getName(), URL_CFGRID);
        ALL_TUTYPES_BY_NAME.put(URL_CFFORM.getName(), URL_CFFORM);
        ALL_TUTYPES_BY_NAME.put(URL_CFHTTP.getName(), URL_CFHTTP);
        ALL_TUTYPES_BY_NAME.put(URL_CFLOCATION.getName(), URL_CFLOCATION);

        ALL_TUTYPES_BY_NAME.put(SSI_INCLUDE.getName(), SSI_INCLUDE);

        ALL_TUTYPES_BY_NAME.put(META_CONTENT.getName(), META_CONTENT);
        ALL_TUTYPES_BY_NAME.put(CHARSET.getName(), CHARSET);

        // self check that the two HashMaps contain the same values
        try
        {
            consistencyTest();
        }
        catch (TuvException te)
        {
        }
    }

    private static Map m_customTuTypesByValue = new HashMap(6);
    private static Map m_customTuTypesByName = new HashMap(6);

    /**
     * Returns the TuType name.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Returns the TuType value.
     */
    public int getValue()
    {
        return m_value;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString()
    {
        return "TuType " + m_name + " (" + Integer.toString(m_value) + ")";
    }

    /**
     * Convert the String to a TuType.
     * 
     * @return the TuType.
     * @throws TuvException
     *             if the name is not a valid TuType name.
     */
    public static TuType valueOf(String p_name) throws TuvException
    {
        TuType tuType = (TuType) ALL_TUTYPES_BY_NAME.get(p_name);

        if (tuType != null)
        {
            return tuType;
        }

        tuType = (TuType) m_customTuTypesByName.get(p_name);

        if (tuType != null)
        {
            return tuType;
        }

        TuvException te = new TuvException(p_name
                + " is not a known TuType name");

        // CATEGORY.warn(te.getMessage(), te);

        throw te;
    }

    /**
     * Convert the int value to a TuType.
     * 
     * @return the TuType.
     * @throws TuvException
     *             if the int value is not a valid TuType value.
     */
    public static TuType valueOf(int p_value) throws TuvException
    {
        Integer key = new Integer(p_value);

        TuType tuType = (TuType) ALL_TUTYPES_BY_VALUE.get(key);

        if (tuType != null)
        {
            return tuType;
        }

        tuType = (TuType) m_customTuTypesByValue.get(key);

        if (tuType != null)
        {
            return tuType;
        }

        TuvException te = new TuvException(key + " is not a known TuType value");

        // CATEGORY.warn(te.getMessage(), te);

        throw te;
    }

    /**
     * Convert the String to the TuType value.
     * 
     * @return the TuType value.
     * @throws TuvException
     *             if the name is not a valid TuType name.
     */
    public static int toInt(String p_name) throws TuvException
    {
        return valueOf(p_name).getValue();
    }

    /**
     * Convert the int value to a TuType name.
     * 
     * @return the TuType name.
     * @throws TuvException
     *             if the int value is not a valid TuType value.
     */
    public static String toString(int p_value) throws TuvException
    {
        return valueOf(p_value).getName();
    }

    /**
     * Test that the two HashMaps are consistent.
     * 
     * @throws TuvException
     *             if they are not consistent.
     */
    public static void consistencyTest() throws TuvException
    {
        TuType tuType = null;
        Collection values = null;
        Iterator valuesIt = null;

        values = ALL_TUTYPES_BY_VALUE.values();
        valuesIt = values.iterator();

        while (valuesIt.hasNext())
        {
            tuType = (TuType) valuesIt.next();

            if (tuType != valueOf(tuType.getValue()))
            {
                CATEGORY.error(tuType.toString()
                        + " not in ALL_TUTYPES_BY_VALUE correctly");

                throw new TuvException(tuType.toString()
                        + " not in ALL_TUTYPES_BY_VALUE correctly");
            }

            if (tuType != valueOf(tuType.getName()))
            {
                CATEGORY.error(tuType.toString()
                        + " not in ALL_TUTYPES_BY_NAME correctly");

                throw new TuvException(tuType.toString()
                        + " not in ALL_TUTYPES_BY_NAME correctly");
            }
        }

        values = ALL_TUTYPES_BY_NAME.values();
        valuesIt = values.iterator();

        while (valuesIt.hasNext())
        {
            tuType = (TuType) valuesIt.next();

            if (tuType != valueOf(tuType.getValue()))
            {
                CATEGORY.error(tuType.toString()
                        + " not in ALL_TUTYPES_BY_VALUE correctly");

                throw new TuvException(tuType.toString()
                        + " not in ALL_TUTYPES_BY_VALUE correctly");
            }

            if (tuType != valueOf(tuType.getName()))
            {
                CATEGORY.error(tuType.toString()
                        + " not in ALL_TUTYPES_BY_NAME correctly");

                throw new TuvException(tuType.toString()
                        + " not in ALL_TUTYPES_BY_NAME correctly");
            }
        }

        if (ALL_TUTYPES_BY_NAME.size() != All_TUTYPES_SIZE)
        {
            String message = "ALL_TUTYPES_BY_NAME.size() "
                    + ALL_TUTYPES_BY_NAME.size() + " != All_TUTYPES_SIZE "
                    + All_TUTYPES_SIZE;

            CATEGORY.error(message);

            throw new TuvException(message);
        }

        if (ALL_TUTYPES_BY_VALUE.size() != All_TUTYPES_SIZE)
        {
            String message = "ALL_TUTYPES_BY_VALUE.size() "
                    + ALL_TUTYPES_BY_VALUE.size() + " != All_TUTYPES_SIZE "
                    + All_TUTYPES_SIZE;

            CATEGORY.error(message);

            throw new TuvException(message);
        }
    }

    /**
     * Compare TuTypes for equality.
     * 
     * @return true if they are equal.
     */
    public boolean equals(Object p_tuType)
    {
        if (p_tuType instanceof TuType)
        {
            return (m_value == ((TuType) p_tuType).m_value);
        }
        return false;
    }

    public int hashCode()
    {
        return m_value;
    }

    private TuType()
    {
    }

    protected TuType(String p_name, int p_value)
    {
        m_name = p_name;
        m_value = p_value;
    }

    /**
     * <p>
     * Construct a CustomTuType from a name. Cannot be a TuType that is
     * previously known. Cannot be a CustomTuType that has been previously
     * constructed. If not, returns a CustomTuType (@see CustomTuType) that
     * already exists, or returns a new CustomTuType;
     * </p>
     * 
     * <p>
     * First use TuType valueOf(String p_name) to obtain an existing TuType. If
     * a TuvException is thrown, then construct one with this constructor.
     * </p>
     * 
     * @return TuType
     */
    protected TuType(String p_name) throws TuvException
    {
        TuType tuType = null;

        try
        {
            // throw an exception if already defined
            tuType = valueOf(p_name);
        }
        catch (TuvException te)
        {
            // normal case, it's not yet defined
            m_name = p_name;
            m_value = All_TUTYPES_SIZE + m_customTuTypesByValue.size() + 1;
            m_customTuTypesByValue.put(new Integer(m_value), this);
            m_customTuTypesByName.put(getName(), this);

            CATEGORY.warn("Constructed custom TuType " + toString());

            return;
        }

        TuvException te = new TuvException(p_name + " already defined as "
                + tuType.toString());

        CATEGORY.error(te.getMessage(), te);

        throw te;
    }
}
