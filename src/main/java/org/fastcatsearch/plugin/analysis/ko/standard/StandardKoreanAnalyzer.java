package org.fastcatsearch.plugin.analysis.ko.standard;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.TypeTokenizer;
import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StandardKoreanAnalyzer extends Analyzer {
	private static Logger logger = LoggerFactory.getLogger(StandardKoreanAnalyzer.class);
	
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 25;
	private CommonDictionary<TagProb, PreResult<CharVector>> koreanDictionary;
	
	public StandardKoreanAnalyzer(CommonDictionary<TagProb, PreResult<CharVector>> koreanDictionary) {
		super(new GlobalReuseStrategy());
		this.koreanDictionary = koreanDictionary;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		//공백으로 분리하여 입력된다.
		final TypeTokenizer tokenizer = new TypeTokenizer(reader);
//		try {
//			tokenizer.reset();
//		} catch (IOException e) {
//			logger.error("tokenizer reset error",e );
//		}
		//분리된 어절을 하나씩 처리한다.
		AbstractKoreanExtractor extractor = new StandardKoreanExtractor(koreanDictionary, DEFAULT_MAX_TOKEN_LENGTH);
		TokenStream filter = new StandardKoreanFilter(tokenizer, extractor);
		
		return new TokenStreamComponents(tokenizer, filter) {
			@Override
			protected void setReader(final Reader reader) throws IOException {
//				logger.debug("TokenStreamComponents setReader");
//				src.setMaxTokenLength(DEFAULT_MAX_TOKEN_LENGTH);
				super.setReader(reader);
				sink.reset();
			}
		};
	}

}
