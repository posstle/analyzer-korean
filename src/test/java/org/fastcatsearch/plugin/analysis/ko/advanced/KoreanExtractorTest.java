package org.fastcatsearch.plugin.analysis.ko.advanced;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.List;

import org.apache.lucene.analysis.core.TypeTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.PosTag;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.TagProbDictionary;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.TagProbDictionaryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KoreanExtractorTest {
	private static Logger logger = LoggerFactory.getLogger(KoreanExtractorTest.class);
	
	CommonDictionary<TagProb, PreResult<CharVector>> koreanDic;
	
	@Before
	public void initDic(){
		File dictionaryDir = new File("dictionary/source/");
		
		File probDictionarySourceFile = new File(dictionaryDir, "0.lnpr_morp.txt");
		PosTag[] usableTags = null;
		File[] dictionarySourceList = new File[]{
				new File(dictionaryDir, "00.Extension.txt")
				,new File(dictionaryDir, "00.N.nng.txt")
				,new File(dictionaryDir, "01.N.nnp.txt")
				,new File(dictionaryDir, "02.N.nnb.txt")
				,new File(dictionaryDir, "03.N.nr.txt")
				,new File(dictionaryDir, "04.N.np.txt")
				,new File(dictionaryDir, "10.V.verb.txt")
				,new File(dictionaryDir, "11.V.vx.txt")
				,new File(dictionaryDir, "20.M.mm.txt")
				,new File(dictionaryDir, "21.M.ma.txt")
				,new File(dictionaryDir, "30.IC.txt")
				,new File(dictionaryDir, "40.XPN.x.txt")
				,new File(dictionaryDir, "50.J.josa.txt")
				,new File(dictionaryDir, "51.E.eomi.txt")
		};
		TagProbDictionaryBuilder builder = new TagProbDictionaryBuilder(probDictionarySourceFile, usableTags, dictionarySourceList);
		builder.loadDictionary();
		koreanDic = new CommonDictionary<TagProb, PreResult<CharVector>>(new TagProbDictionary(builder.map(), true));
	}
	
	public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
	    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
	    root.setLevel(level);
//	    System.out.println(level.levelStr);
	}
	
	@Test
	public void testSpeed() throws IRException {
		setLoggingLevel(ch.qos.logback.classic.Level.INFO);
		AdvStandardKoreanExtractor extractor = new AdvStandardKoreanExtractor(koreanDic, 32);
		URL url = getClass().getResource("/com/fastcatsearch/ir/analysis/ko/large_korean_text.txt");
		File file = new File(url.getFile());
		BufferedReader dr = null;
		
		for (int k = 0; k < 5; k++) {

			int i = 0;
			try {
				dr = new BufferedReader( new InputStreamReader(new FileInputStream(file), "utf-8"));
			} catch (Exception e) {
				return;
			}

			String line = "";

			long start = System.currentTimeMillis();
			long lap = start;
			try {
				while ((line = dr.readLine()) != null) {
					line = line.trim();

					if (line.length() == 0)
						continue;
					
					TypeTokenizer tok = new TypeTokenizer(new StringReader(line));
					tok.reset();
					CharTermAttribute termAttribute = tok.getAttribute(CharTermAttribute.class);
					while(tok.incrementToken()){
						char[] source = termAttribute.buffer();
						if(!extractor.setInput(source, termAttribute.length())){
							continue;
						}
						Entry bestEntry = extractor.extract();
						
//						logger.info("======{}======", termAttribute.toString());
//						logger.info("best>> {}", bestEntry.getChainedString(source));
//						List<Entry> resultList = extractor.getAllResult();
//						for (Entry entry : resultList) {
//							logger.info(">{}", entry.getChainedString(source));
//						}
					}

					i++;
					if ((i % 10000) == 0) {
						System.out.println(i + " th "+(System.currentTimeMillis() - lap)+"ms");
						lap = System.currentTimeMillis();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			long elapsedTime = System.currentTimeMillis() - start;
			double lps = i * 1000 / elapsedTime;
			System.out.println("DONE "+i+" lines time = "+elapsedTime+"ms, docs/s="+ lps);
			double mbps = file.length() / (elapsedTime / 1000.0) / 1024 / 1024;
			System.out.println((k + 1) + "th LineByLine index Extraction Speed : " + mbps + "MBp/s");
		}
	}

	@Test
	public void test() throws IRException, IOException {
//		HashSetDictionary koreanDic = new HashSetDictionary(100);
//		koreanDic.put(new CharVector("한국"));
//		koreanDic.put(new CharVector("대학"));
		
		AdvStandardKoreanExtractor extractor = new AdvStandardKoreanExtractor(koreanDic, 32);
//		extractor.setKoreanDic(koreanDic);
		
		String text = null; 
		text = "공학부에서 들어가";
		text = "도맡아 생각했습니다 ";// 시달려야만 올렸습니다 보고서를 사랑하기 계속해서 
		text = "시간이었을지라도";
		text = "이명박스럽다";
		text = "신소재공학과의 선배들과의 불안감이라고";
		text = "유복하다면 잘한다는";
		text = "문화적 이야기에 제대로 한놈";
		text = "신석기시대부터 청동기시대에 이르기까지 우리 조상들이 온갖 동물과 물고기들과 함께 고래잡이와 사냥모습을 병풍처럼 펼쳐진 매끈한 바위 벽에 그려 놓았다";
		text = "하버드대학교";
		text = "[옐로페이 결제 시 전상품 5%할인] [위즈위드] [프라다] [S S신상품] Open Toe Slingback Sandal 10  구매시 28100포인트 적립 500억원 규모";
		text = "컴퓨터";
		text = "하지만 분명한 것은 컴퓨터에 대해 큰 관심을 갖고 있고, 앞으로도 컴퓨터분야의 길을 가고 싶다는 것은 확고합니다. 연세대학교에서의 수업을 통해 컴퓨터의 여러 분야에 대해 더 배워보고 정해가고 싶습니다.";
		text = "중요해지는 끼쳤습니다 참가해서";
		text = "한나라당 하버드 했습니다 하였습니다 하셨습니다 합니다 했다 했니 주었습니다 주었다 졌습니다 되었습니다 됬다 인가요 스럽다";
		text = "하여서 되어서 주어서 되서 되야 가서 가야 해서 해야 줘야 줘서 봐서 봐야 줘야해서 가줘야 패줘야";
		text = "너희를 자유케 이겨내고";
		text = "12자리 태양열 계산기 (GA-837S) (5개 묶음)";
		text = "연세대학교의";
		TypeTokenizer tok = new TypeTokenizer(new StringReader(text));
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
			List<Entry> resultList = extractor.getAllResult();
			for (Entry e : resultList) {
				logger.debug(">{}", e.getChainedShortString(source));
				
			}
		}

	}

	@Test
	public void testMultitype() throws IRException {
		AdvStandardKoreanExtractor extractor = new AdvStandardKoreanExtractor(koreanDic, 32);
		
		String text = null; 
		text = "옐로페이 결제 시 전상품 5 할인 S S신상품 Open Toe 10 구매시 28,100포인트 적립 500억원 규모";
		text = "LG전자 27MA53D 27인치 IPS패널 TV모니터 D-Sub HDMI2조 MHL호환  AV 컴포넌트 컴포지트  PIP기능 ";
		String[] textList = text.split(" ");

		for (int i = 0; i < textList.length; i++) {

			String str = textList[i];
			char[] source = str.toCharArray();
			extractor.setInput(source, source.length);
			Entry bestEntry = extractor.extract();
			
			logger.debug("======{}======", str);
//			extractor.showTabular();
			logger.debug("best>> {}", bestEntry.getChainedString(source));
			List<Entry> resultList = extractor.getAllResult();
			for (Entry e : resultList) {
				logger.debug(">{}", e.getChainedString(source));
				
			}
		}

	}
	
	@Test
	public void testShort() throws IRException, IOException {
		
		String text = "됐음 했음 도운다 도왔다 보내다 보낸다 보냄을 보냅니다. 보낼 수 있다. 됐으면 한다 했습니다 하였었습니다 하였습니다 ";
//				"한다  ";//"보낸다 했었습니다 도왔다 도운다 ";
		AdvStandardKoreanExtractor extractor = new AdvStandardKoreanExtractor(koreanDic, 32);
		TypeTokenizer tok = new TypeTokenizer(new StringReader(text));
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
			
		}

	}
	
	
	
	
}
