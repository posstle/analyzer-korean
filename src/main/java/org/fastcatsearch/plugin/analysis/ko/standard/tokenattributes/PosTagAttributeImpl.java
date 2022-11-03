package org.fastcatsearch.plugin.analysis.ko.standard.tokenattributes;

import org.apache.lucene.util.AttributeImpl;
import org.fastcatsearch.plugin.analysis.ko.standard.PosTag;


public class PosTagAttributeImpl extends AttributeImpl implements PosTagAttribute, Cloneable {

	private PosTag posTag;

	@Override
	public void setPosTag(PosTag posTag) {
		this.posTag = posTag;
	}

	@Override
	public PosTag posTag() {
		return posTag;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyTo(AttributeImpl target) {
		// TODO Auto-generated method stub
		
	}

}
