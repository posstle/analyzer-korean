package org.fastcatsearch.plugin.analysis.ko.standard;

import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class EntryBak implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(EntryBak.class);
	private int row;
	private int column;
	private TagProb tagProb;
	private EntryBak next; //next는 분석시 사용되지 않으며, best로 뽑힌뒤 사용할때 getChainingHead를 호출할때 연결된다.
	private EntryBak prev;
	private double score; //prev와 연결될때 이전 score에 합산하여 유지된다. 최종 score이다.

	public EntryBak(int row, int column, TagProb tagProb) {
		this.row = row;
		this.column = column;
		this.tagProb = tagProb;
		this.score += tagProb.prob();
	}

	public TagProb tagProb(){
		return tagProb;
	}
	
	public PosTag posTag(){
		return tagProb.posTag();
	}
	public void tagProb(TagProb tagProb){
		this.tagProb = tagProb;
		this.score += tagProb.prob();
	}

	public int row(){
		return row;
	}
	public int column(){
		return column;
	}
	public void column(int column){
		this.column = column;
	}
	public double totalScore() {
		return score;
	}
	
	public int entryCount() {
		EntryBak prev = this;
		int count = 0;
		while (prev != null) {
			count++;
			prev = prev.prev();
		}
		return count;
	}
	
	public int charSize(){
		return column;
	}
	public EntryBak next() {
		return next;
	}
	public EntryBak prev() {
		return prev;
	}
	public EntryBak next(EntryBak next) {
		next.prev = this;
		next.score += this.score;
		return next;
	}
	public EntryBak getChainingHead(){
		EntryBak curr = this;
		EntryBak prev = this.prev;
		while (prev != null) {
			prev.next = curr;
			curr = prev;
			prev = prev.prev;
		}
		return curr;
	}
	
	public String toShortString(char[] source){
		return new String(source, row, column) + ":" + tagProb.toShortString();
		
	}
	
	public String getChainedShortString(char[] source){
		logger.debug("getChainedShortString");
		if ( prev == null )
			return toShortString(source);
		else
			return prev.getChainedShortString(source) + " + " + toShortString(source);
	}
	
	public String getChainedString() {
		if (prev == null) {
			return toString();
		} else {
			return prev.getChainedString() + " + " + toString();
		}
	}

	public String getChainedString(char[] source) {
		if (prev == null) {
			return toDetailString(source);
		} else {
			return prev.getChainedString(source) + " + " + toDetailString(source);
		}
	}

	@Override
	public EntryBak clone() {
		EntryBak entry = null;
		try {
			entry = (EntryBak) super.clone();
		} catch (CloneNotSupportedException e) {
			logger.error("", e);
		}
		return entry;
	}

	@Override
	public String toString() {
		return "(" + row + "," + column + "):" + tagProb + ":" + score;
	}

	public String toDetailString(char[] source) {
		return new String(source, row, column)+ "(" + row + "," + column + "):" + tagProb + ":" + score;
	}

}
