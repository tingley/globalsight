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


package com.globalsight.diplomat.servlet.config;

public class GSA_LanguagePair
{
    private int m_id = 0;
    private int m_sourceLanguage = 0;
    private int m_targetLanguage = 0;
    private float m_cost = 0;
    
    public GSA_LanguagePair (int p_id, int p_sourceLanguage, int p_targetLanguage,
        float p_cost)
    {
        m_id = p_id;
        m_sourceLanguage = p_sourceLanguage;
        m_targetLanguage = p_targetLanguage;
        m_cost = p_cost;
    }
    
    public int getID() { return m_id; }
    public int getSourceLanguage() { return m_sourceLanguage; }
    public int getTargetLanguage() { return m_targetLanguage; }
    public float getCost() { return m_cost; }
}