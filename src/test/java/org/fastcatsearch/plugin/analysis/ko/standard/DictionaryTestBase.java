package org.fastcatsearch.plugin.analysis.ko.standard;

import java.io.File;

import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.dictionary.SetDictionary;
import org.fastcatsearch.ir.dictionary.SynonymDictionary;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.TagCharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.TagProbDictionary;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.TagProbDictionaryBuilder;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DictionaryTestBase {
	protected Logger logger = LoggerFactory.getLogger(DictionaryTestBase.class);
	
	protected CommonDictionary<TagProb, PreResult<CharVector>> koreanDictionary;
	
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
		
//		File userFile = new File(dictionaryDir, "90.User.txt");
		File synonymFile = new File(dictionaryDir, "91.Synonym.txt");
		File stopFile = new File(dictionaryDir, "92.Stop.txt");
		
		TagProbDictionaryBuilder builder = new TagProbDictionaryBuilder(probDictionarySourceFile, usableTags, dictionarySourceList);
		builder.loadDictionary();
		boolean isIgnoreCase = true;
		SetDictionary userDictionary = new SetDictionary(isIgnoreCase);
//		userDictionary.loadSource(userFile);
		
		SetDictionary stopDictionary = new SetDictionary(isIgnoreCase);
		stopDictionary.loadSource(stopFile);
		
		SynonymDictionary synonymDictionary = new SynonymDictionary(isIgnoreCase);
		synonymDictionary.loadSource(synonymFile);
		
		TagProbDictionary dictionary = new TagProbDictionary(builder.map(), true);
		dictionary.appendNounEntry(userDictionary.getUnmodifiableSet());
		dictionary.appendNounEntry(stopDictionary.getUnmodifiableSet());
		dictionary.appendNounEntry(synonymDictionary.getWordSet());
		
		koreanDictionary = new CommonDictionary<TagProb, PreResult<CharVector>>(dictionary);
		koreanDictionary.addDictionary("STOP", stopDictionary.getUnmodifiableSet());
		koreanDictionary.addDictionary("SYNONYM", synonymDictionary.getUnmodifiableMap());

	}

}
