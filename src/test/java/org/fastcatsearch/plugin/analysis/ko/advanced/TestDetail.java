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
		System.out.println("동사 + 전성 어미 테스트");
		String text[] = new String[]{"됐음","했음"};
		String result[] = new String[]{"됐:V + 음:E","했:V + 음:E"};
		HashMap<String, String> analPair = new HashMap<String, String>();
		
		for ( int i = 0 ;i < text.length ; i ++ )
			analPair.put(text[i], result[i]);
		
		for ( String str : text )
		{			
			assertTrue(getAnalysisResult(str).equals((String)analPair.get(str)));
		}
		System.out.println("동사 + 전성 어미 테스트 완료 ");
	}
	
	@Test
	public void testV_BI() throws IRException, IOException {
		System.out.println("동사 어근 ㅂ 불규칙 테스트 ");
//		String text = "됐음 했음 도운다 도왔다 보내다 보낸다 보냄을 보냅니다. 보낼 수 있다. 됐으면 한다 했습니다 하였었습니다 하였습니다 ";
		
		String text[] = new String[]{"도운다", "도왔다", "고운", "곱게", "고와서"};
		String result[] = new String[]{"도운:V + 다:E","도:V + 왔:E + 다:E", "고:V + 운:E", "곱:V + 게:E", "고와:V + 서:E"};
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
		System.out.println("동사 어근 ㅂ 불규칙 테스트 완료");
	}
	
	
	
	@Test
	public void testV_DI() throws IRException, IOException {
		
		System.out.println("동사 어근 ㄷ 불규칙 테스트 ");
//		String text = "됐음 했음 도운다 도왔다 보내다 보낸다 보냄을 보냅니다. 보낼 수 있다. 됐으면 한다 했습니다 하였었습니다 하였습니다 ";
		
		String text[] = new String[]{"깨닫고","깨달아야","깨닫지"};
		String result[] = new String[]{"깨닫:V + 고:E", "깨달:V + 아야:E", "깨닫:V + 지:E"};
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
		System.out.println("동사 어근 ㄷ 불규칙 테스트 완료");
	}
	
	@Test
	public void testV_SI() throws IRException, IOException {
		System.out.println("동사 어근 ㅅ 불규칙 테스트 ");
//		String text = "됐음 했음 도운다 도왔다 보내다 보낸다 보냄을 보냅니다. 보낼 수 있다. 됐으면 한다 했습니다 하였었습니다 하였습니다 ";
		
		String text[] = new String[]{"그어", "나아"};
		String result[] = new String[]{"그:V + 어:E", "나:V + 아:E"};
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
		System.out.println("동사 어근 ㅅ 불규칙 테스트 완료");
	}
	
	@Test
	public void testV_RI() throws IRException, IOException {
		System.out.println("동사 어근 ㄹ 불규칙 테스트 ");
//		String text = "됐음 했음 도운다 도왔다 보내다 보낸다 보냄을 보냅니다. 보낼 수 있다. 됐으면 한다 했습니다 하였었습니다 하였습니다 ";
		
		String text[] = new String[]{"드셨습니까", "드시오", "드십니다" };
		String result[] = new String[]{"드:V + 셨:E + 습니까:E", "드:V + 시오:E", "드:V + 십니다:E"};
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
		System.out.println("동사 어근 ㄹ 불규칙 테스트 완료");
	}
	
	@Test
	public void testV_HI() throws IRException, IOException {
		System.out.println("동사 어근 ㅎ 불규칙 테스트 ");
//		String text = "됐음 했음 도운다 도왔다 보내다 보낸다 보냄을 보냅니다. 보낼 수 있다. 됐으면 한다 했습니다 하였었습니다 하였습니다 ";
		
		String text[] = new String[]{"까만"};
		String result[] = new String[]{"까맣:V + 만:E"};
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
		System.out.println("동사 어근 ㅎ 불규칙 테스트 완료");
	}
	
	
	@Test
	public void test() throws IRException, IOException {
		System.out.println("예외 케이스 테스트 ");
//		String text = "됐음 했음 도운다 도왔다 보내다 보낸다 보냄을 보냅니다. 보낼 수 있다. 됐으면 한다 했습니다 하였었습니다 하였습니다 ";
		
		String text[] = new String[]{"고와서", "고운", "곱게"};
		String result[] = new String[]{"고와:V + 서:E", "고:V + 운:E", "곱:V + 게:E"};
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
		System.out.println("동사 + 전성 어미 테스트 완료 ");
	}
}
