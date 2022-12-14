package org.fastcatsearch.plugin.analysis.ko.advanced;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.plugin.analysis.ko.standard.DictionaryTestBase;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestDetail extends DictionaryTestBase {

private AdvStandardKoreanExtractor extractor = null;
private StandardTokenizer tok = null;

private static Logger logger = LoggerFactory.getLogger(TestDetail.class);
	
public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    root.setLevel(level);
//    System.out.println(level.levelStr);
}

	@Before
	public void initTokenizer(){
		extractor = new AdvStandardKoreanExtractor(koreanDictionary, 32);
		tok = new StandardTokenizer(null);
		setLoggingLevel(ch.qos.logback.classic.Level.INFO);
	}
	
	private String getAnalysisResult(String input) throws IOException
	{
		
		tok.setReader(new StringReader(input));
		tok.reset();
		CharTermAttribute termAttribute = tok.getAttribute(CharTermAttribute.class);
		while(tok.incrementToken()){
			String str = termAttribute.toString();
			if(!extractor.setInput(str.toCharArray(), str.length())){
				continue;
			}

			char[] source = termAttribute.buffer();
			extractor.setInput(source, termAttribute.length());
			Entry bestEntry = extractor.extract();
			return bestEntry.getChainedShortString(source);
			
		}
		return "";
	
	}
	
	private String getAnalysisResultDebug(String input) throws IOException
	{
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
	    
		//root.setLevel(level);
		if ( root.getLevel().levelStr.equalsIgnoreCase("info") )
			return getAnalysisResult(input);
	    
		tok.setReader(new StringReader(input));
		tok.reset();
		CharTermAttribute termAttribute = tok.getAttribute(CharTermAttribute.class);
		while(tok.incrementToken()){
			String str = termAttribute.toString();
			if(!extractor.setInput(str.toCharArray(), str.length())){
				continue;
			}

			char[] source = termAttribute.buffer();
			extractor.setInput(source, termAttribute.length());
			Entry bestEntry = extractor.extract();
			
			logger.debug("======{}======", str);
			extractor.showTabular();
			logger.debug("best>> {}", bestEntry.getChainedShortString(source));
			return bestEntry.getChainedShortString(source);
			
		}
		return "";
	
	}
	
	
	@Test
	public void testV_ATM() throws IRException, IOException {
		System.out.println("?????? + ?????? ?????? ?????????");
		String text[] = new String[]{"??????","??????"};
		String result[] = new String[]{"???:V + ???:E","???:V + ???:E"};
		HashMap<String, String> analPair = new HashMap<String, String>();
		
		for ( int i = 0 ;i < text.length ; i ++ )
			analPair.put(text[i], result[i]);
		
		for ( String str : text )
		{			
			assertTrue(getAnalysisResult(str).equals((String)analPair.get(str)));
		}
		System.out.println("?????? + ?????? ?????? ????????? ?????? ");
	}
	
	@Test
	public void testV_BI() throws IRException, IOException {
		System.out.println("?????? ?????? ??? ????????? ????????? ");
//		String text = "?????? ?????? ????????? ????????? ????????? ????????? ????????? ????????????. ?????? ??? ??????. ????????? ?????? ???????????? ?????????????????? ??????????????? ";
		
		String text[] = new String[]{"?????????", "?????????", "??????", "??????", "?????????"};
		String result[] = new String[]{"??????:V + ???:E","???:V + ???:E + ???:E", "???:V + ???:E", "???:V + ???:E", "??????:V + ???:E"};
		HashMap<String, String> analPair = new HashMap<String, String>();
		
		for ( int i = 0 ;i < text.length ; i ++ )
			analPair.put(text[i], result[i]);
		
		for ( String str : text )
		{			
			String anal = getAnalysisResult(str);
			System.out.println("source :" + str);
			
			System.out.println("anal :" + anal);
			System.out.println("corr :" + (String)analPair.get(str));
			assertTrue(anal.equals((String)analPair.get(str)));
			System.out.println();
		}
		System.out.println("?????? ?????? ??? ????????? ????????? ??????");
	}
	
	
	
	@Test
	public void testV_DI() throws IRException, IOException {
		
		System.out.println("?????? ?????? ??? ????????? ????????? ");
//		String text = "?????? ?????? ????????? ????????? ????????? ????????? ????????? ????????????. ?????? ??? ??????. ????????? ?????? ???????????? ?????????????????? ??????????????? ";
		
		String text[] = new String[]{"?????????","????????????","?????????"};
		String result[] = new String[]{"??????:V + ???:E", "??????:V + ??????:E", "??????:V + ???:E"};
		HashMap<String, String> analPair = new HashMap<String, String>();
		
		for ( int i = 0 ;i < text.length ; i ++ )
			analPair.put(text[i], result[i]);
		
		for ( String str : text )
		{			
			String anal = getAnalysisResult(str);
			System.out.println("source :" + str);
			
			System.out.println("anal :" + anal);
			System.out.println("corr :" + (String)analPair.get(str));
			assertTrue(anal.equals((String)analPair.get(str)));
			System.out.println();
		}
		System.out.println("?????? ?????? ??? ????????? ????????? ??????");
	}
	
	@Test
	public void testV_SI() throws IRException, IOException {
		System.out.println("?????? ?????? ??? ????????? ????????? ");
//		String text = "?????? ?????? ????????? ????????? ????????? ????????? ????????? ????????????. ?????? ??? ??????. ????????? ?????? ???????????? ?????????????????? ??????????????? ";
		
		String text[] = new String[]{"??????", "??????"};
		String result[] = new String[]{"???:V + ???:E", "???:V + ???:E"};
		HashMap<String, String> analPair = new HashMap<String, String>();
		
		for ( int i = 0 ;i < text.length ; i ++ )
			analPair.put(text[i], result[i]);
		
		for ( String str : text )
		{			
			String anal = getAnalysisResult(str);
			System.out.println("source :" + str);
			
			System.out.println("anal :" + anal);
			System.out.println("corr :" + (String)analPair.get(str));
			assertTrue(anal.equals((String)analPair.get(str)));
			System.out.println();
		}
		System.out.println("?????? ?????? ??? ????????? ????????? ??????");
	}
	
	@Test
	public void testV_RI() throws IRException, IOException {
		System.out.println("?????? ?????? ??? ????????? ????????? ");
//		String text = "?????? ?????? ????????? ????????? ????????? ????????? ????????? ????????????. ?????? ??? ??????. ????????? ?????? ???????????? ?????????????????? ??????????????? ";
		
		String text[] = new String[]{"???????????????", "?????????", "????????????" };
		String result[] = new String[]{"???:V + ???:E + ?????????:E", "???:V + ??????:E", "???:V + ?????????:E"};
		HashMap<String, String> analPair = new HashMap<String, String>();
		
		for ( int i = 0 ;i < text.length ; i ++ )
			analPair.put(text[i], result[i]);
		
		for ( String str : text )
		{			
			String anal = getAnalysisResult(str);
			System.out.println("source :" + str);
			
			System.out.println("anal :" + anal);
			System.out.println("corr :" + (String)analPair.get(str));
			assertTrue(anal.equals((String)analPair.get(str)));
			System.out.println();
		}
		System.out.println("?????? ?????? ??? ????????? ????????? ??????");
	}
	
	@Test
	public void testV_HI() throws IRException, IOException {
		System.out.println("?????? ?????? ??? ????????? ????????? ");
//		String text = "?????? ?????? ????????? ????????? ????????? ????????? ????????? ????????????. ?????? ??? ??????. ????????? ?????? ???????????? ?????????????????? ??????????????? ";
		
		String text[] = new String[]{"??????"};
		String result[] = new String[]{"??????:V + ???:E"};
		HashMap<String, String> analPair = new HashMap<String, String>();
		
		for ( int i = 0 ;i < text.length ; i ++ )
			analPair.put(text[i], result[i]);
		
		for ( String str : text )
		{			
			String anal = getAnalysisResult(str);
			System.out.println("source :" + str);
			
			System.out.println("anal :" + anal);
			System.out.println("corr :" + (String)analPair.get(str));
			assertTrue(anal.equals((String)analPair.get(str)));
			System.out.println();
		}
		System.out.println("?????? ?????? ??? ????????? ????????? ??????");
	}
	
	
	@Test
	public void test() throws IRException, IOException {
		System.out.println("?????? ????????? ????????? ");
//		String text = "?????? ?????? ????????? ????????? ????????? ????????? ????????? ????????????. ?????? ??? ??????. ????????? ?????? ???????????? ?????????????????? ??????????????? ";
		
		String text[] = new String[]{"?????????", "??????", "??????"};
		String result[] = new String[]{"??????:V + ???:E", "???:V + ???:E", "???:V + ???:E"};
		HashMap<String, String> analPair = new HashMap<String, String>();
		
		for ( int i = 0 ;i < text.length ; i ++ )
			analPair.put(text[i], result[i]);
		
		for ( String str : text )
		{			
			String anal = getAnalysisResult(str);
			System.out.println("source :" + str);
			
			System.out.println("anal :" + anal);
			System.out.println("corr :" + (String)analPair.get(str));
			assertTrue(anal.equals((String)analPair.get(str)));
			System.out.println();
		}
		System.out.println("?????? + ?????? ?????? ????????? ?????? ");
	}
}
