package org.fastcatsearch.plugin.analysis.ko.standard;

import org.apache.lucene.analysis.core.TypeTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.fastcatsearch.ir.common.IRException;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.List;


public class KoreanExtractorTest extends DictionaryTestBase {
	
	@Test
	public void testSpeed() throws IRException {
		
		StandardKoreanExtractor extractor = new StandardKoreanExtractor(koreanDictionary, 32);
		URL url = getClass().getResource("/org/fastcatsearch/plugin/analysis/ko/large_korean_text.txt");
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
					
//					logger.debug(">> {}", line);
					TypeTokenizer tok = new TypeTokenizer(new StringReader(line));
					tok.reset();
					CharTermAttribute termAttribute = tok.getAttribute(CharTermAttribute.class);
					while(tok.incrementToken()){
						char[] source = termAttribute.buffer();
						if(extractor.setInput(source, termAttribute.length()) == -1){
							continue;
						}
						Entry bestEntry = extractor.extract();
						printEntrySplit(bestEntry, source);
						logger.debug(">{}", bestEntry.getChainedString(source));
//						logger.debug("======{}======", termAttribute.toString());
//						logger.debug("best>> {}", bestEntry.getChainedString(source));
//						List<Entry> resultList = extractor.getAllResult();
//						for (Entry e : resultList) {
//							logger.debug(">{}", e.getChainedString(source));
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

	private void printEntrySplit(Entry entry, char[] source){
		int k = 0;
		while (entry != null) {
			if(entry.posTag() == PosTag.J){
				entry = entry.next();
				continue;
			}
			if (k > 0) {
				System.out.print("/");
			}
			System.out.print(entry.toWord(source));

			entry = entry.next();
			k++;
		}
		System.out.println();
	}
	@Test
	public void test() throws IRException, IOException {
//		HashSetDictionary koreanDictionary = new HashSetDictionary(100);
//		koreanDictionary.put(new CharVector("한국"));
//		koreanDictionary.put(new CharVector("대학"));
		StandardKoreanExtractor extractor = new StandardKoreanExtractor(koreanDictionary, 32);
//		extractor.setKoreanDic(koreanDictionary);
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
		text = "맞춰져";
		text = "저는";
		text = "진청바지가";
		text = "조선의마왕 즐거운인생";
		text = "쌀가공 발매트 학생용";
		
		TypeTokenizer tok = new TypeTokenizer(new StringReader(text));
		tok.reset();
		CharTermAttribute termAttribute = tok.getAttribute(CharTermAttribute.class);
		while(tok.incrementToken()){
			String str = termAttribute.toString();
			if(extractor.setInput(str.toCharArray(), str.length()) == -1){
				continue;
			}

			char[] source = termAttribute.buffer();
			extractor.setInput(source, termAttribute.length());
			Entry bestEntry = extractor.extract();
			
			logger.debug("======{}======", str);
			extractor.showTabular();
			logger.debug("best>> {}", bestEntry.getChainedString(source));
			List<Entry> resultList = extractor.getAllResult();
			for (Entry e : resultList) {
				logger.debug(">{}", e.getChainedString(source));
				
			}
		}

	}

	@Test
	public void testMultitype() throws IRException {
		StandardKoreanExtractor extractor = new StandardKoreanExtractor(koreanDictionary, 32);
		
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
		
		StandardKoreanExtractor extractor = new StandardKoreanExtractor(koreanDictionary, 32);
		String text = "보내다 보낸다 보냄을 보냅니다. 보낼 수 있다. 됐으면 한다. 했습니다. 하였었습니다. 하였습니다. ";
		TypeTokenizer tok = new TypeTokenizer(new StringReader(text));
		tok.reset();
		CharTermAttribute termAttribute = tok.getAttribute(CharTermAttribute.class);
		while(tok.incrementToken()){
			String str = termAttribute.toString();
			if(extractor.setInput(str.toCharArray(), str.length()) == -1){
				continue;
			}

			char[] source = termAttribute.buffer();
			extractor.setInput(source, termAttribute.length());
			Entry bestEntry = extractor.extract();
			
			logger.debug("======{}======", str);
			extractor.showTabular();
			logger.debug("best>> {}", bestEntry.getChainedString(source));
			
		}

	}
}
