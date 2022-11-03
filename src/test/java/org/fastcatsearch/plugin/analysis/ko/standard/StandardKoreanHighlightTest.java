package org.fastcatsearch.plugin.analysis.ko.standard;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.StopwordAttribute;
import org.apache.lucene.analysis.tokenattributes.SynonymAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.fastcatsearch.ir.query.Term.Option;
import org.fastcatsearch.ir.summary.BasicHighlightAndSummary;
import org.fastcatsearch.ir.summary.TokenizedTermScorer;
import org.fastcatsearch.plugin.analysis.ko.standard.StandardKoreanAnalyzer;
import org.fastcatsearch.plugin.analysis.ko.standard.tokenattributes.PosTagAttribute;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;


public class StandardKoreanHighlightTest extends DictionaryTestBase {
	
	@Before
	public void init() {
		String LOG_LEVEL = System.getProperty("LOG_LEVEL");
		if(LOG_LEVEL==null || "".equals(LOG_LEVEL)) {
			LOG_LEVEL = "DEBUG";
		}
		
		((ch.qos.logback.classic.Logger)
			LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)
			).setLevel(Level.toLevel("DEBUG"));
		
		((ch.qos.logback.classic.Logger)
			LoggerFactory.getLogger(BasicHighlightAndSummary.class)
			).setLevel(Level.toLevel(LOG_LEVEL));
		
		((ch.qos.logback.classic.Logger)
			LoggerFactory.getLogger(TokenizedTermScorer.class)
			).setLevel(Level.toLevel(LOG_LEVEL));
		
		((ch.qos.logback.classic.Logger)
			LoggerFactory.getLogger(SimpleHTMLFormatter.class)
			).setLevel(Level.toLevel(LOG_LEVEL));
	}
	
	public void testSingle(String fieldData, String queryTerm, int length, int fragments) throws Exception {
		
		BasicHighlightAndSummary highlighter = new BasicHighlightAndSummary();
			
		String fieldName = "text";
		
		AnalyzerOption option = new AnalyzerOption();
		StandardKoreanAnalyzer analyzer = new StandardKoreanAnalyzer(koreanDictionary);
		TokenStream tokenStream = analyzer.tokenStream(fieldName, (new StringReader(queryTerm)), option);
		CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		OffsetAttribute offsetAtt = tokenStream.getAttribute(OffsetAttribute.class);
		CharsRefTermAttribute refTermAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
		PosTagAttribute posTagAttribute = tokenStream.getAttribute(PosTagAttribute.class);
		TypeAttribute typeAttribute = tokenStream.getAttribute(TypeAttribute.class);
		PositionIncrementAttribute positionIncrementAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
		SynonymAttribute synonymAttribute = tokenStream.getAttribute(SynonymAttribute.class);
		StopwordAttribute stopwordAttribute = tokenStream.getAttribute(StopwordAttribute.class);
		
		
		String query = "";
		List<String>terms = new ArrayList<String>();
		
		tokenStream.reset();
		System.out.println(">>>>" + queryTerm);
		while (tokenStream.incrementToken()) {
			String str = refTermAttribute.toString();
			System.out.println("query terms : ["+str+"]");
			terms.add(str);
		}
			
		System.out.println("HL>>"+highlighter.highlight(fieldName, analyzer, analyzer, fieldData, queryTerm, new String[]{"[","]"}, length, fragments, new Option()));
	}
	
	@Test 
	public void testSingle() throws Exception {
		String fieldData = "일이라면 언제나 가장 열성적으로 저를 지지해주는 후원자이기도 하셨습니다. 어린 시절부터 저는 집안의 장남으로서 동생과 부모님을 돌보며 자신의 것만을 고집하기보다는 다른 사람들과 화합하는 방법들을 자연스럽게 터득하며 성장하였습니다. 이러한 저의 가정환경은 훗날 제가 책임감이 강하고 성실한 사람으로 발전하";
		String queryTerm = "어린 시절부터 장남으로서 후원 동생을 돌보며 자신의 것만을 고집하기보다는 다른 사람들과 화합하는 방법들을 자연스럽게 터득하며 성장하였습니다. ";
		queryTerm = "언제나 고집하기보다는 성실한.";
		
		fieldData = "그대가 올 때쯤이면.. 그대가 올 때쯤이면 난 이미 그대 곁을 스쳐지나가고 있을 것입니다 내가 그대를 기다린만큼 그대도 나를 기다려야 할 것입니다 항상 그대만 바라보고 항상 그대만 생각하고 항상 그대만 감싸줬는데 그대는 언제나 나보다는 그녀가 먼저 였습니다 그런 그대를 바라보며 녹초가 되어버린 내 맘은 이제 잃을것도 버릴것도 미련같은것도 증오같은것도 없이 그저 허망한 느낌 단지 그것 뿐이었습니다 이제 그대가 기다릴 차례입니다 내가 그 동안 힘들었던것만큼 그대도 지금 힘들어합니다 이제 그대가 기다리고 있습니다 애타게 나를 바라보며 말입니다 하지만 그런 그대를 바라보며 내가 느끼는 느낌은 그저 허망함과 안쓰러움 뿐이었습니다 이제 그대가 내 곁으로 다가올 때쯤이면 난 이미 그대곁을 떠나고 없을것입니다...";
		queryTerm = "나를 기다려야 할 것입니다 그대만 감싸줬는데 ";
		
		
		fieldData = "타거스(대표 이혁준)는 미국 국방성의 내구성 표준 기준 테스트를 통과한 스마트기기 케이스 ‘세이프 포트 러기드 맥스 프로‘ 시리즈를 출시한다고 15일 밝혔다.\n" + 
				"타거스의 신제품 케이스는 강화 폴리 카보이트 소재를 사용해 외부충격 흡수 기능을 강화했고 자체 액정 보호 쉴드를 장착하여 스크래치를 방지하는 것이 특징이다. 또한 실리콘 플러그로 이어폰 및 USB 포트를 개폐하도록 설계해 먼지나 오염물질이 포트에 침투하는 것을 방지했다.\n" + 
				"타거스는 충격방지 케이스와 함께 레저 활동 시 활용도를 높일 수 있도록 자전거에 연결해 사용할 수 있는 바이크 마운트도 출시했다.\n" + 
				"신제품 충격 방지 케이스는 미국 국방성이 군용 제품에 적용하는 내구성 표준 기준(MIL-STD 810F-516.5) 테스트를 거쳤다. 미국 국방성의 내구성 표준 기준은 1.2미터 높이에서 콘크리트에 떨어뜨렸을 때의 충격 발생 상황과 시간 당 198mm의 우천 상황, 먼지와 진동 발생 등의 상황에서 기기가 정상적으로 작동 하는지 테스트 한다.\n" + 
				"아이폰5와 아이패드 미니, 갤럭시S4용 제품이 출시됐으며, 가격은 종류에 따라4만5천원~ 8만9천원이다.\n" + 
				"이혁준 한국타거스의 대표이사는 “새로 구매한 스마트폰을 떨어트려 화면 또는 기기가 손상되었을 때 받는 충격은 기기뿐 아니라 사용자에게도 크게 다가온다. 타거스의 세이프포트 시리즈 케이스는 사용자에게 안정감과 손상으로 인해 발생되는 추가 비용을 절감해 준다\"고 말했다.";
		
		queryTerm = "강화 폴리 카보이트 바이크 마운트 세이프포트";
		testSingle(fieldData, queryTerm, 100, 3);
//		String str1 = "Hello this is a piece of text that is very long and contains too much preamble and the meat is really here which says kennedy has been shot";
//		String str2 = "hello this is a piece of text contains much been shot";
//		str1 = "감사노트도 적어보고..그러다가 세월에, 여건에 휩쓸려 잊고 살았는데..또 우연히 네빌 고다드의 5일간의 강의 라는 책을 읽고 믿음으로 걸어라를 읽고 드뎌 사람들이 ..";
//		str2 = "네빌 고다드의 5일간의 강의 라는 책";
	}
}
