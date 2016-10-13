package org.getopt.luke.plugins;

import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.getopt.luke.LukePlugin;

public class AnalyzerToolPlugin extends LukePlugin {

    /** Default constructor. Initialize analyzers list. */
    public AnalyzerToolPlugin() throws Exception {
    }

    public String getXULName() {
        return "/xml/at-plugin.xml";
    }
    
    public String getPluginName() {
        return "AnalyzerTool";
    }
    
    public String getPluginInfo() {
        return "Tool for analyzing analyzers, by Mark Harwood";
    }
    
    public String getPluginHome() {
        return "mailto:mharwood@apache.org";
    }
    
    /** Overriden to populate the drop down even if no index is open. */
    public void setMyUi(Object ui) {
        super.setMyUi(ui);
        try {
            init();
        } catch (Exception e) {e.printStackTrace();};
    }
    
    public boolean init() throws Exception {
        Object combobox = app.find(myUi, "analyzers");
        app.removeAll(combobox);
        String firstClass = "";
        Class[] analyzers = app.getAnalyzers();
        for (int i = 0; i < analyzers.length; i++) {
            Object choice = app.create("choice");
            app.setString(choice, "text", analyzers[i].getName());
            if (i == 0) {
                firstClass = analyzers[i].getName();
            }
            app.add(combobox, choice);
        }
        app.setInteger(combobox, "selected", 0);
        app.setString(combobox, "text", firstClass);
        return true;
    }

    public void analyze() {
        showError("");
        try {
            Object combobox = app.find(myUi, "analyzers");
            Object resultsList = app.find(myUi, "resultsList");
            Object inputText = app.find(myUi, "inputText");
            Object outputText = app.find(myUi, "outputText");
            String classname = app.getString(combobox, "text");
            Class clazz = Class.forName(classname);
            Analyzer analyzer = null;
            try {
                analyzer = (Analyzer) clazz.newInstance();

            } catch (Throwable t) {
                showError("Couldn't instantiate analyzer - public zero-argument constructor required");
                return;
            }
            app.setString(outputText, "text", app.getString(inputText,
                    "text"));
            TokenStream ts = analyzer.tokenStream(new StringReader(app
                    .getString(inputText, "text")));
            Token token = ts.next();
            app.removeAll(resultsList);

            while (token != null) {
                Object row = app.create("item");
                app.setString(row, "text", token.termText());
                app.add(resultsList, row);
                app.putProperty(row, "data", token);
                token = ts.next();
            }
        } catch (Throwable t) {
            showError("Error analyzing:" + t.getMessage());
        }
    }

    public void tokenChange() {
        Object list = app.find("resultsList");
        Object selectedNode = app.getSelectedItem(list);
        if (selectedNode == null) { return; }
        Token token = (Token) app.getProperty(selectedNode, "data");
        Object outputText = app.find("outputText");
        app.setInteger(outputText, "start", 0);
        app.setInteger(outputText, "end", token.endOffset());
        app.setInteger(outputText, "start", token.startOffset());
        app.requestFocus(outputText);
    }

    public void showError(String message) {
        Object errorLabel = app.find("errorLabel");
        app.setString(errorLabel, "text", message);
    }

}