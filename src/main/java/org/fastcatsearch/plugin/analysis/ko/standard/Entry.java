package org.fastcatsearch.plugin.analysis.ko.standard;

import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Entry implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(Entry.class);
	private int row;
	private int column;
	private TagProb tagProb;
	private Entry next; //다음 엔트리.
	private double score; //최종 score이다. head가 next이후의 score의 합산을 가지고 있게된다.
	private boolean extracted;
	private int offset;
	
	public Entry(int row, int column, TagProb tagProb) {
		this(row, column, tagProb, (double) 0);
	}
	public Entry(int row, int column, TagProb tagProb, double scoreAdd) {
		this.row = row;
		this.column = column;
		this.tagProb = tagProb;
		this.score += (tagProb.prob() + scoreAdd);
	}
	
	public Entry(int row, int column, TagProb tagProb, int offset) {
		this.row = row;
		this.column = column;
		this.tagProb = tagProb;
		this.score += tagProb.prob();
		this.offset = offset;
	}

	public TagProb tagProb(){
		return tagProb;
	}
	
	public PosTag posTag(){
		return tagProb.posTag();
	}
	public void tagProb(TagProb tagProb){
		//이전것을 빼고.
		this.score -= this.tagProb.prob();
		this.tagProb = tagProb;
		this.score += tagProb.prob();
	}

	public int offset(){
		return offset + row - column + 1;
	}
	public int row(){
		return row;
	}
	public void row(int row){
		this.row = row;
	}
	public int column(){
		return column;
	}
	public void column(int column){
		this.column = column;
	}
	public void addScore(int score) {
		this.score += score;
	}
	
	public double totalScore() {
		return score;
	}
	
	public int entryCount() {
		Entry nextEntry = this;
		int count = 0;
		while (nextEntry != null) {
			count++;
			nextEntry = nextEntry.next;
		}
		return count;
	}
	
	public int charSize(){
		return column;
	}
	public Entry next() {
		return next;
	}

	public Entry last() {
		Entry l = this;
		while(true){
			if(l.next() == null){
				return l;
			}else{
				l = l.next();
			}
		}
	}
	
	public void setNext(Entry next) {
		this.next = next;
	}
	public Entry next(Entry next) {
//		next.prev = this;
//		next.score += this.score;
//		return next;
		this.next = next;
		if(next != null){
			this.score += next.score;
		}
		return this;
	}
//	public Entry getChainingHead(){
//		Entry curr = this;
//		Entry prev = this.prev;
//		while (prev != null) {
//			prev.next = curr;
//			curr = prev;
//			prev = prev.prev;
//		}
//		return curr;
//	}
	
	public boolean isExtracted() {
		return extracted;
	}
	
	public void setExtracted(boolean extracted) {
		this.extracted = extracted;
	}
	
	
	public String getChainedString() {
		if (next == null) {
			return toString();
		} else {
			return toString() + " + " + next.getChainedString();
		}
	}

	public String getChainedShortString(char[] source){
		if ( next == null )
			return toShortString(source);
		else
			return toShortString(source) + " + " + next.getChainedShortString(source);
	}
	
	public String getChainedString(char[] source) {
		if (next == null) {
			return toDetailString(source);
		} else {
			return toDetailString(source) + " + " + next.getChainedString(source);
		}
	}

	@Override
	public Entry clone() {
		Entry entry = null;
		try {
			entry = (Entry) super.clone();
		} catch (CloneNotSupportedException e) {
			logger.error("", e);
		}
		return entry;
	}

	@Override
	public String toString() {
		return "(" + (row + offset) + "," + column + "):" + tagProb + ":" + score;
	}

	public String toWord(char[] source){
		return new String(source, row + offset - column + 1, column);
		
	}
	
	public String toShortString(char[] source){
		return new String(source, row + offset - column + 1, column) + ":" + tagProb.toShortString();
		
	}
	
	public String toDetailString(char[] source) {
//		logger.debug("toDetailString [{},{}] {}", row, column, tagProb);
		try{
		return new String(source, row + offset - column + 1, column)+ "(" + (row + offset) + "," + column + "):" + tagProb + ":" + score;
		}catch(Exception e){
			logger.debug("{} ({},{})", new String(source), row + offset - column + 1, column);
			throw new RuntimeException();
		}
	}

}
