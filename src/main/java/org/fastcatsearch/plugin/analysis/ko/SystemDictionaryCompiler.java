package org.fastcatsearch.plugin.analysis.ko;

import org.fastcatsearch.plugin.analysis.ko.standard.PosTag;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.TagProbDictionaryBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class SystemDictionaryCompiler {
	
	public static void main(String[] args) throws IOException {

		File outFile = new File("dictionary/binary/system.dict");

		File probDictionarySourceFile = new File("dictionary/source/0.lnpr_morp.txt");

		PosTag[] usableTags = null; //모든 태그 사용.

		File[] dictionarySourceList = new File[] {
			new File("dictionary/source/00.Extension.txt")
			, new File("dictionary/source/00.N.nng.txt")
			, new File("dictionary/source/01.N.nnp.txt")
			, new File("dictionary/source/02.N.nnb.txt")
			, new File("dictionary/source/03.N.nr.txt")
			, new File("dictionary/source/04.N.np.txt")
			, new File("dictionary/source/10.V.verb.txt")
			, new File("dictionary/source/11.V.vx.txt")
			, new File("dictionary/source/20.M.mm.txt")
			, new File("dictionary/source/21.M.ma.txt")
			, new File("dictionary/source/30.IC.txt")
			, new File("dictionary/source/40.XPN.x.txt")
			, new File("dictionary/source/50.J.josa.txt")
			, new File("dictionary/source/51.E.eomi.txt")
		};

        outFile.getParentFile().mkdirs();
        if(!outFile.exists()) {
            outFile.createNewFile();
        }

		OutputStream out = new FileOutputStream(outFile);
		TagProbDictionaryBuilder builder = new TagProbDictionaryBuilder(probDictionarySourceFile, usableTags, dictionarySourceList);
		builder.loadDictionary();
		builder.writeTo(out);
		out.close();
		
		System.out.println("바이너리 사전을 생성하였습니다. "+outFile.getAbsolutePath()+" ("+outFile.length()+"B)");
	}
}
