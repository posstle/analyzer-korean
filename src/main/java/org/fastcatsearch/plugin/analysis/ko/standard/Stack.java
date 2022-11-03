package org.fastcatsearch.plugin.analysis.ko.standard;


/*
 * 향후 backtracking에 사용될수 있으나 현재는 사용되지 않음.
 * 
 * 
 * */
public class Stack {
	private TrackingMark[] elements;
	private int top;

	public Stack() {
		elements = new TrackingMark[128];
		for (int i = 0; i < elements.length; i++) {
			elements[i] = new TrackingMark();
		}
	}

	public void push(int pos, PosTag posTag) {
		elements[top++].set(pos, posTag);
	}

	public int size() {
		return top;
	}

	public TrackingMark pop() {
		if (top == 0) {
			return null;
		}
		return elements[--top];
	}
}

class TrackingMark {
	private int pos;
	private PosTag posTag;

	public TrackingMark() {
	}

	public void set(int pos, PosTag posTag) {
		this.pos = pos;
		this.posTag = posTag;
	}

	public TrackingMark(int pos, PosTag posTag) {
		this.pos = pos;
		this.posTag = posTag;
	}

	public int pos() {
		return pos;
	}

	public PosTag posTag() {
		return posTag;
	}
}
