package org.fastcatsearch.plugin.analysis.ko.standard;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.fastcatsearch.ir.analysis.AnalyzerFactory;
import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.TagCharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;


public class StandardKoreanAnalyzerFactory implements AnalyzerFactory {

	private CommonDictionary<TagProb, PreResult<CharVector>> dictionary;
	
	public StandardKoreanAnalyzerFactory(CommonDictionary<TagProb, PreResult<CharVector>> dictionary){
		this.dictionary = dictionary;
	}
	
	@Override
	public void init() {
	}

	@Override
	public Analyzer create() {
		return new StandardKoreanAnalyzer(dictionary);
	}


	@Override
	public Class<? extends Analyzer> getAnalyzerClass() {
		return StandardKoreanAnalyzer.class;
	}

}
