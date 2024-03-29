/*
 * Copyright (C) 2012 Aaron W. Johnson
 * 
 * All rights reserved.  Licensing yet to be determined.
 */

package org.arpodwot.heatmaps.documents.output;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.arpodwot.heatmaps.highlighting.color.BlueHighlighter;
import org.arpodwot.heatmaps.highlighting.color.HighlightColorCSSGenerator;
import org.arpodwot.heatmaps.highlighting.color.YellowHighlighter;


public class HighlightedHTMLDocumentGenerator implements OutputDocumentGenerator {

	@Override
	public void writeToFile (
			double[] highlightData,
			String originalText,
			String filePath)
					throws Exception 
	{
		String htmlFilePath = filePath+".html";
		Pattern linePattern = Pattern.compile("^.+$", Pattern.MULTILINE);
		Matcher lineMatcher = linePattern.matcher(originalText);
		Pattern tokenPattern = Pattern.compile("[a-z]+", Pattern.CASE_INSENSITIVE);
		Matcher tokenMatcher = tokenPattern.matcher(originalText);

		HighlightColorCSSGenerator cssGen = new BlueHighlighter(0.1, 0.9);
		StringBuilder outputText = new StringBuilder();
		int currentToken = 0;
		
		// take it one line at a time
		int start = 0;
		while (lineMatcher.find(start)){
			int lineStart = lineMatcher.start();
			int lineEnd = lineMatcher.end();
			
			// find all tokens in this line
			ArrayList<Integer> tokenStarts = new ArrayList<Integer>();
			int tmpStart = lineStart;
			while (tmpStart < lineEnd){
				if (tokenMatcher.find(tmpStart) && tokenMatcher.start() < lineEnd){
					tokenStarts.add(tokenMatcher.start());
					tmpStart = tokenMatcher.end();
				} else {
					break;
				}
			}
			
			// add this line to the output
			outputText.append("<div class=\"paragraph\">");
			for (int i = 0; i < tokenStarts.size(); i++){
				int subSeqStart;
				int subSeqEnd;
				
				if (i == 0)
					subSeqStart = lineStart; 
				else
					subSeqStart = tokenStarts.get(i);
				
				if (i == tokenStarts.size() - 1)
					subSeqEnd = lineEnd;
				else
					subSeqEnd = tokenStarts.get(i+1);
				
				// get the highlighting color
				String highlightCSS = cssGen.generateHighlightCSS(highlightData[currentToken]); 
				
				outputText.append("<span style=\""+highlightCSS+"\">");
				outputText.append(originalText.substring(subSeqStart, subSeqEnd));
				outputText.append("</span>");
				currentToken++;
			}
			outputText.append("</div>\n");
			
			// move to the next line
			start = lineMatcher.end();
		}
			
		// write out the file
		FileWriter fw = new FileWriter(htmlFilePath);
		fw.write(outputText.toString());
		fw.close();
	}
}
