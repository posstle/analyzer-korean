package org.fastcatsearch.plugin.analysis.ko.standard.tokenattributes;

import org.apache.lucene.util.Attribute;
import org.fastcatsearch.plugin.analysis.ko.standard.PosTag;


public interface PosTagAttribute extends Attribute {

	public void setPosTag(PosTag posTag);

	public PosTag posTag();

}
