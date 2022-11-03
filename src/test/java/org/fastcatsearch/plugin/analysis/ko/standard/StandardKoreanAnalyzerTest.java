package org.fastcatsearch.plugin.analysis.ko.standard;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.tokenattributes.*;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.tokenattributes.PosTagAttribute;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;


public class StandardKoreanAnalyzerTest extends DictionaryTestBase{
	
	
	@Test
	public void testSpeed() throws IRException {
		AnalyzerTestBase base = new AnalyzerTestBase();
		AnalyzerOption option = new AnalyzerOption();
		option.useStopword(true);
		option.useSynonym(true);
		StandardKoreanAnalyzer analyzer = new StandardKoreanAnalyzer(koreanDictionary);
		
//		URL url = getClass().getResource("/org/fastcatsearch/plugin/analysis/ko/large_korean_text.txt");
//		File file = new File(url.getFile());
		File file = new File("/Users/swsong/TEST_HOME/danawa1022/node1/collections/community_etc/out.txt");
		
		base.testSpeed(analyzer, file, true);
	}

	@Test
	public void testSingle() throws IOException{
		AnalyzerOption option = new AnalyzerOption();
		option.useStopword(true);
		option.useSynonym(true);
		
		String fieldName = "text";
		String fieldData = "하지만 분명한 것은 컴퓨터에 대해 큰 관심을 갖고 있고, 앞으로도 컴퓨터분야의 길을 가고 싶다는 것은 확고합니다. Yonsei 대학교에서의 수업을 통해 컴퓨터의 여러 분야에 대해 더 배워보고 정해가고 싶습니다.";
		fieldData = "메이온의 사랑이 담긴";
		fieldData = "아버지 기로기로기로기로기로기기로기로기로기로기로기 어머니1234gb";
		fieldData =  "캬캬캬캬캬캬캬캬캬캬캬캬캬캬캬";
		fieldData = "정신을 잃고 깨어나보니 성별이 다른 여자아이 몸으로 숲속에서 깨어나게 되었다. 몸 주인의 이름은 엘레미아 폰 갈레아. 그렇게 여자아이 몸으로 숲속 생활을 시작하게 되는데... 나는 귀족이었다. 노래했다	";
		//		fieldData = "분야에&nbsp;대한&nbsp;학부가&nbsp;9개&nbsp;영역으로&nbsp;세분화되어&nbsp;있는&nbsp;점이&nbsp;이&nbsp;학과에&nbsp;대한&nbsp;신뢰와";
//		fieldData = "맞춰져";
//		fieldData = "무료배송 실리콘 케이스 APPLE 정품";
		fieldData = "미꾸라지약관으로조선";
		fieldData ="같은꿈을꾸다조선의마왕";
		fieldData = "쌀가공";
		StandardKoreanAnalyzer analyzer = new StandardKoreanAnalyzer(koreanDictionary);
		TokenStream tokenStream = analyzer.tokenStream(fieldName, (new StringReader(fieldData)), option);
		CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		OffsetAttribute offsetAtt = tokenStream.getAttribute(OffsetAttribute.class);
		CharsRefTermAttribute refTermAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
		PosTagAttribute posTagAttribute = tokenStream.getAttribute(PosTagAttribute.class);
		TypeAttribute typeAttribute = tokenStream.getAttribute(TypeAttribute.class);
		PositionIncrementAttribute positionIncrementAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
		SynonymAttribute synonymAttribute = tokenStream.getAttribute(SynonymAttribute.class);
		StopwordAttribute stopwordAttribute = tokenStream.getAttribute(StopwordAttribute.class);
		FeatureAttribute featureAttribute = tokenStream.getAttribute(FeatureAttribute.class);
		
		tokenStream.reset();
		System.out.println(">>>>" + fieldData);
		while (tokenStream.incrementToken()) {
			String str = termAttribute.toString();
			String str2 = refTermAttribute.toString();
			System.out.println((stopwordAttribute.isStopword()?"[불용어]":"") + str2 +"/"+ posTagAttribute.posTag().name() + " [" + featureAttribute.type() + "] " + typeAttribute.type()
					+" " + positionIncrementAttribute.getPositionIncrement() + " [ " + offsetAtt.startOffset() + " ~ " + offsetAtt.endOffset() + " ] "+str);
			List<Object> synonymObj = synonymAttribute.getSynonyms();
			
			if(synonymObj != null){
				String synonymStr = "";
				for (Object obj : synonymObj) {
					if(obj instanceof CharVector) {
						synonymStr += obj.toString()+", ";
					} else if(obj instanceof List) {
						String extracted = "";
						@SuppressWarnings("unchecked")
						List<CharVector> synonyms = (List<CharVector>) obj;
						for(CharVector synonym : synonyms){
							extracted += synonym+" ";
						}
						synonymStr += extracted+", ";
					}
				}
				System.out.println("유사어 >> " + synonymStr);
			}
		}
	}
}
