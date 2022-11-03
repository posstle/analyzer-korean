package org.fastcatsearch.plugin.analysis.ko.standard;

import org.apache.lucene.analysis.core.TypeTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.fastcatsearch.ir.common.IRException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by swsong on 2015. 3. 23..
 */
public class TokenizerTest {

	@Test
	public void testTokenizer() throws IRException, IOException {
		String text = "토큰 테스트";
		TypeTokenizer tok = new TypeTokenizer(new StringReader(text));
		tok.reset();
		CharTermAttribute termAttribute = tok.getAttribute(CharTermAttribute.class);
		while(tok.incrementToken()) {
			String str = termAttribute.toString();
			System.out.println(str);
		}
	}
}
