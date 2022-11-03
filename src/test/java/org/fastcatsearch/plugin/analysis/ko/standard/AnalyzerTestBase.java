package org.fastcatsearch.plugin.analysis.ko.standard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.StopwordAttribute;
import org.apache.lucene.analysis.tokenattributes.SynonymAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.tokenattributes.PosTagAttribute;


public class AnalyzerTestBase {

	public void testSpeed(Analyzer analyzer, File file, boolean isDebug) throws IRException {
		
		int i = 0;
		long start = System.currentTimeMillis();
		long lap = start;

		String fieldName = "text";
		
		
		int COUNT = 2;
		for (int k = 0; k < COUNT; k++) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				BufferedReader sourceReader = new BufferedReader(new InputStreamReader(is, "utf-8"));

				String line = null;
				while ((line = sourceReader.readLine()) != null) {
					line = line.trim();
					if (line.length() == 0)
						continue;
					
					TokenStream tokenStream = analyzer.tokenStream(fieldName, (new StringReader(line)));
					CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
					OffsetAttribute offsetAtt = tokenStream.getAttribute(OffsetAttribute.class);
					CharsRefTermAttribute refTermAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
					PosTagAttribute posTagAttribute = tokenStream.getAttribute(PosTagAttribute.class);
					PositionIncrementAttribute positionIncrementAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
					TypeAttribute typeAttribute = tokenStream.getAttribute(TypeAttribute.class);
					SynonymAttribute synonymAttribute = tokenStream.getAttribute(SynonymAttribute.class);
					StopwordAttribute stopwordAttribute = tokenStream.getAttribute(StopwordAttribute.class);
					
					tokenStream.reset();
					if(isDebug){
						System.out.println(">>>>" + line);
					}
					while (tokenStream.incrementToken()) {
						if(refTermAttribute != null){
							if(isDebug){
								String str = termAttribute.toString();
								String str2 = refTermAttribute.toString();
								System.out.println((stopwordAttribute.isStopword()?"[불용어]":"") + str2 +"/"+ posTagAttribute.posTag().name() 
									+ " " + typeAttribute.type()
									+ " " + positionIncrementAttribute.getPositionIncrement()
									+" [ " + offsetAtt.startOffset() + " ~ " + offsetAtt.endOffset() + " ] "+str);
								List<Object> synonymObj = synonymAttribute.getSynonyms();
								if(synonymObj != null) {
									String synonymStr = "";
									for(Object obj : synonymObj){
										if(obj instanceof CharVector) {
											synonymStr += obj.toString()+", ";
										} else if(obj instanceof List) {
											@SuppressWarnings("unchecked")
											List<CharVector> synonyms = (List<CharVector>)obj;
											String extracted = "";
											for(CharVector synonym : synonyms){
												extracted += synonym+" ";
											}
											synonymStr += extracted + ", ";
										}
									}
									System.out.println("유사어 >> " + synonymStr);
								}
							}
						}
					}
					i++;
					if ((i % 10000) == 0) {
						System.out.println(i + " th " + (System.currentTimeMillis() - lap) + "ms");
						lap = System.currentTimeMillis();
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ignore) {
					}
				}
			}
		}

		long elapsedTime = System.currentTimeMillis() - start;
		double docPerSec = i * 1000 / elapsedTime ;
		System.out.println("DONE " + i + " lines time = " + elapsedTime + "ms");
		double mbps = file.length() * COUNT * 1000.0 / elapsedTime / 1024 / 1024;
		System.out.println("Extraction Speed : " + mbps + "MBp/s, "+ docPerSec+" docs/s");
	}
}
