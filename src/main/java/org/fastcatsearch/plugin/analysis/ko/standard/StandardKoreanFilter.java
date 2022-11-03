package org.fastcatsearch.plugin.analysis.ko.standard;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.core.TypeTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.FeatureAttribute;
import org.apache.lucene.analysis.tokenattributes.FeatureAttribute.FeatureType;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.StopwordAttribute;
import org.apache.lucene.analysis.tokenattributes.SynonymAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.dictionary.SetDictionary;
import org.fastcatsearch.ir.dictionary.SynonymDictionary;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.fastcatsearch.plugin.analysis.ko.standard.tokenattributes.PosTagAttribute;

public class StandardKoreanFilter extends TokenFilter {

	private static String DICT_SYNONYM = "synonym";
	private static String DICT_STOP = "stop";

	private AbstractKoreanExtractor extractor;
	private CharTermAttribute charTermAttribute;
	private OffsetAttribute offsetAttribute;
	private TypeAttribute typeAttribute;
	private PositionIncrementAttribute positionIncrementAttribute;

	private SynonymDictionary synonymDictionary;
	private SetDictionary stopDictionary;

	private SynonymAttribute synonymAttribute; // 유사어 속성.
	private StopwordAttribute stopwordAttribute; // 불용어 속성.

	private CharsRefTermAttribute charsRefTermAttribute;
	private PosTagAttribute posTagAttribute;
	private FeatureAttribute featureAttribute;

	private Entry entry;
	private int positionIncrementBase;
	private int baseOffset;

	private int positionIncrementOffset; // 들어온 포지션 위치와 분석되어 나누어진후의 포지션 위치는 달라야
											// 하므로 더해주는 크기를 유지한다.
	private int termIncrementCount; // 한글분석시 여러개의 텀으로 나누어지기 때문에, 증가된 텀갯수만큼 뒤의 텀
									// position에 더해주어야한다.

	private CommonDictionary<TagProb, PreResult<CharVector>> dictionary;

	private int meaningfulUNKHangulLength = 12;

	public StandardKoreanFilter(TokenStream input, AbstractKoreanExtractor koreanExtractor) {
		super(input);
		extractor = koreanExtractor;
		charTermAttribute = getAttribute(CharTermAttribute.class);
		offsetAttribute = getAttribute(OffsetAttribute.class);
		typeAttribute = getAttribute(TypeAttribute.class);
		positionIncrementAttribute = getAttribute(PositionIncrementAttribute.class);

		charsRefTermAttribute = addAttribute(CharsRefTermAttribute.class);
		posTagAttribute = addAttribute(PosTagAttribute.class);

		synonymAttribute = addAttribute(SynonymAttribute.class);
		stopwordAttribute = addAttribute(StopwordAttribute.class);
		featureAttribute = addAttribute(FeatureAttribute.class);

		dictionary = extractor.dictionary();

		synonymDictionary = (SynonymDictionary) dictionary.getDictionary(DICT_SYNONYM);
		stopDictionary = (SetDictionary) dictionary.getDictionary(DICT_STOP);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (entry == null) {
			
			boolean hasNext = false;
			while((hasNext = input.incrementToken())) {
				if( TypeTokenizer.SYMBOL == typeAttribute.type()) {
					continue;
				}
				break;
			}
		
			if (!hasNext) {
				return false;
			}
			
			
			// 한글이 들어있는 것만 분석.
			if (typeAttribute.type() == TypeTokenizer.HANGUL) {
				char[] buffer = charTermAttribute.buffer();
				int length = charTermAttribute.length();

				if(extractor.setInput(buffer, length) != -1){
					entry = extractor.extract();
				}else{
					entry = null;
				}
				charsRefTermAttribute.setBuffer(buffer, 0, length);

				baseOffset = offsetAttribute.startOffset();
				termIncrementCount = -1;
			}

		}

		// 상위에서 분석된 텀의 포지션을 가져온다.
		positionIncrementBase = positionIncrementAttribute.getPositionIncrement();

		// keyword는 유사어,금지어 검색용도이다.
		CharVector keyword = null;
		if (entry != null) {
			if (analyzerOption.useStopword() || analyzerOption.useSynonym()) {
				keyword = new CharVector(charsRefTermAttribute.charsRef().chars, entry.offset(), entry.column());
			}

			// 한글분석 엔트리존재.
			charsRefTermAttribute.setOffset(entry.offset(), entry.column());
			PosTag posTag = entry.posTag();
			posTagAttribute.setPosTag(posTag);
			if (posTag == PosTag.N || posTag == PosTag.ALPHA || posTag == PosTag.UNK) { //한글은 UNK도 MAIN으로..
				featureAttribute.setType(FeatureType.MAIN);
			} else if (posTag == PosTag.J || posTag == PosTag.E) {
				featureAttribute.setType(FeatureType.APPEND);
			} else {
				featureAttribute.setType(FeatureType.ADDITION);
			}

			offsetAttribute.setOffset(baseOffset + entry.offset(), baseOffset + entry.offset() + entry.column());

			entry = entry.next();
			termIncrementCount++;
		} else {
			// 한글분석안된것은 그대로.
			char[] buffer = charTermAttribute.buffer();
			int length = charTermAttribute.length();
			if (analyzerOption.useStopword() || analyzerOption.useSynonym()) {
				keyword = new CharVector(buffer, 0, length);
			}

			charsRefTermAttribute.setBuffer(buffer, 0, length);
			posTagAttribute.setPosTag(PosTag.UNK);
			
			
			if (typeAttribute.type() == TypeTokenizer.HANGUL) {
				if(length <= meaningfulUNKHangulLength ){
					featureAttribute.setType(FeatureType.ADDITION);
				}else{
					featureAttribute.setType(FeatureType.NULL);
					//없애준다.
					charsRefTermAttribute.setOffset(0, 0);
					charTermAttribute.setLength(0);
				}
			} else if (typeAttribute.type() == TypeTokenizer.ALPHA || typeAttribute.type() == TypeTokenizer.NUMBER) {
				//영문은 모두 main
				featureAttribute.setType(FeatureType.MAIN);
			} else {
				//나머지는 부가단어.
				featureAttribute.setType(FeatureType.ADDITION);
			}
			termIncrementCount = -1;
			// offsetAttribute 은 변경없음.
		}

		if (keyword != null) {
			boolean isStopword = false;
			if (analyzerOption.useStopword()) {
				isStopword = checkStopword(keyword);
			}
			// 불용어가 아닐때만 유사어 확장을 한다.
			if (!isStopword && analyzerOption.useSynonym()) {
				checkSynonym(keyword);
			}
		}

		/*
		 * positionIncrementAttribute 값 재조정.
		 */
		if (termIncrementCount > 0) {
			// 분리된 것이 2개이상일때는 더해준다. 단 positionIncrementOffset은 이전 텀에서 미리 더해주었으므로
			// 여기서는 증가분만 더해준다.
			positionIncrementAttribute.setPositionIncrement(positionIncrementBase + 1);
			positionIncrementOffset++;
		} else {
			// 분리가 없거나 분리된 것이 1개일 때는 그대로 사용.
			positionIncrementAttribute.setPositionIncrement(positionIncrementBase + positionIncrementOffset);
		}

		return true;
	}

	public void checkSynonym(CharVector keyword) {
		if (synonymDictionary == null) {
			return;
		}
		CharVector[] synonymKeyword = synonymDictionary.map().get(keyword);
		if (synonymKeyword != null) {
			// logger.debug("유사어 발견 >> {} >> {}", keyword, synonymKeyword);

			synonymAttribute.setSynonyms(Arrays.asList(synonymKeyword));

			// 유사어가 불용어일때는 처리하지 않는다.
			// 반드시 관리자가 유사어에 불용어를 넣지 않도록 해야한다.
			// 여기서 불용어를 발견해도 불용어셋팅을 해주기가 애매하다. 메인단어가 불용어인지, 확장이 불용어인지 알수없기때문에.

		} else {
			// clear value
			synonymAttribute.setSynonyms(null);
		}
	}

	public boolean checkStopword(CharVector keyword) {
		if (stopDictionary == null) {
			return false;
		}

		if (stopDictionary.set().contains(keyword)) {
			// logger.debug("불용어 발견 >> {}", keyword);
			stopwordAttribute.setStopword(true);
			return true;
		} else {
			// clear value
			stopwordAttribute.setStopword(false);
		}
		return false;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		positionIncrementOffset = 0;
		termIncrementCount = 0;
		entry = null;
	}

}
