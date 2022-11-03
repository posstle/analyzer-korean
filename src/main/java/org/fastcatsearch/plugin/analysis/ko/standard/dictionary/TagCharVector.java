package org.fastcatsearch.plugin.analysis.ko.standard.dictionary;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.PosTag;

public class TagCharVector extends CharVector {

	private static final long serialVersionUID = 3390839647907820033L;
	
	private PosTag posTag;
	
	public TagCharVector(String word, PosTag posTag){
		super(word);
		this.posTag = posTag;
	}
	
	public TagCharVector(char[] array, int start, int length, PosTag posTag, boolean isIgnoreCase){
		super(array, start, length, isIgnoreCase);
		this.posTag = posTag;
	}

	public PosTag getPosTag() {
		return posTag;
	}

	public void setPosTag(PosTag posTag) {
		this.posTag = posTag;
	}
	
}
