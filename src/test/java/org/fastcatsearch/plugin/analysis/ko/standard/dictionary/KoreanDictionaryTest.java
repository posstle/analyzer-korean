package org.fastcatsearch.plugin.analysis.ko.standard.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.DictionaryTestBase;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KoreanDictionaryTest extends DictionaryTestBase {
	private static Logger logger = LoggerFactory.getLogger(KoreanDictionaryTest.class);

	@Test
	public void testLoad() throws IOException {

	}

	@Test
	public void test() throws IOException {
		String line = null;
		ArrayList<CharVector> list = new ArrayList<CharVector>();
		int l = 0;
		String resource = "00.N.nng.txt";
		InputStream is = getClass().getResourceAsStream(resource);
		BufferedReader dr = new BufferedReader(new InputStreamReader(is));
		while ((line = dr.readLine()) != null) {
			line = line.trim();

			if (line.length() == 0)
				continue;

			CharVector key = new CharVector(line);
			list.add(key);
			// if(l == 52800)
			// break;

			l++;
		}
		System.out.println("Lines " + l);

		int count = 0;
		long st = System.currentTimeMillis();
		for (int k = 0; k < 10; k++) {
			for (int i = 0; i < list.size(); i++) {
				CharVector key = list.get(i);
				List<TagProb> typeList = koreanDictionary.find(key);
				count++;
				System.out.println(">>" + key + " : " + typeList);
			}
		}
		logger.debug("Map search {} time = {}", count, (System.currentTimeMillis() - st) + "ms");
	}

//	@Test
//	public void testStatus() {
//		koreanDictionary.checkUseStatus();
//	}

	@Test
	public void testSearchDic() throws IOException {
		String key = "하";
		List<TagProb> list = koreanDictionary.find(new CharVector(key));
		logger.debug("{} >> {}", key, list);
	}
	
	@Test
	public void testProb() {
		
		TagProbDictionaryBuilder builder = new TagProbDictionaryBuilder();
		builder.addEntry("급\tM\t-10.0");
		builder.addEntry("급\tV\t-9.0");
		CommonDictionary<TagProb, PreResult<CharVector>> koreanDic = new CommonDictionary<TagProb, PreResult<CharVector>>(new TagProbDictionary(builder.map(), true));
		String word = "급";
		CharVector ch = new CharVector(word);
		
		List<TagProb> list = koreanDic.find(ch);
		if ( list != null )
		{
			for ( TagProb element : list )
			{
				System.out.println(element);
			}
		}
		else
			System.out.println("there is no element");
	}
	
	@Test
	public void testCompiledDictionary(){
		File systemDictFile = new File("dictionary/binary/system.dict");
		TagProbDictionary tagProbDictionary = new TagProbDictionary(systemDictFile, true);
		Map<CharVector, List<TagProb>> map = tagProbDictionary.getUnmodifiableDictionary();
		List<TagProb> result = map.get(new CharVector("미꾸라지"));
		System.out.println(result);
	}
}
