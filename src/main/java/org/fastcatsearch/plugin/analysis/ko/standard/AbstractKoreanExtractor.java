package org.fastcatsearch.plugin.analysis.ko.standard;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 한국어 특성상 뒤에서부터 연결을 하는 extractor.
 * */
public abstract class AbstractKoreanExtractor {
	protected static Logger logger = LoggerFactory.getLogger(AbstractKoreanExtractor.class);

	private static final AnalyzeExceedException Analyze_Exceed_Exception = new AnalyzeExceedException();
	protected CommonDictionary<TagProb, PreResult<CharVector>> koreanDic;

	private PosTagProbEntry[][] tabular;
	private int[] status;
	private int[][] connectionTable;
	
	private ArrayDeque<Entry> queue = new ArrayDeque<Entry>(8);
	private static final int QUEUE_MAX = 20;
	private static final int RESULT_MAX = 20;
	private List<Entry> result = new ArrayList<Entry>();

	protected char[] source;
	protected int length;

	public AbstractKoreanExtractor(CommonDictionary<TagProb, PreResult<CharVector>> koreanDic, int tabularSize) {
		tabular = new PosTagProbEntry[tabularSize][];
		// tabular 파싱 초기화.
		for (int row = 0; row < tabular.length; row++) {
			tabular[row] = new PosTagProbEntry[row + 2];
		}
		status = new int[tabularSize];
		this.koreanDic = koreanDic;
		initConnectionTable();
	}

	public CommonDictionary<TagProb, PreResult<CharVector>> dictionary(){
		return koreanDic;
	}
	
	private void initConnectionTable() {
		connectionTable = new int[PosTag.values().length][];
		
		for (int i = 0; i < connectionTable.length; i++) {
			connectionTable[i] = new int[connectionTable.length];
		}
		loadConnectionInfo();
	}
	private boolean isConnectable(TagProb head, int row, int column, Entry tail){
		if(connectionTable[head.posTag().ordinal()][tail.posTag().ordinal()] == 1){
			if(isConnectableByRule(head, row, column, tail)){
				return true;
			}else{
				return false;
			}
			
		}
//		return connectionTable[last.posTag().ordinal()][next.posTag().ordinal()] == 1;
		return false;
		
	}

	protected void setConnectable(PosTag prev, PosTag... nextList) {
		for (int i = 0; i < nextList.length; i++) {
			PosTag next = nextList[i];
			connectionTable[prev.ordinal()][next.ordinal()] = 1;
		}
	}

	public void setKoreanDic(CommonDictionary<TagProb, PreResult<CharVector>> koreanDic) {
		this.koreanDic = koreanDic;
	}

	
	
	/////////////////////////////////////////
	// Abstract Method
	/*
	 * 두 PosTag간의 tag기반 접속문법검사테이블 설정.
	 * */
	protected abstract void loadConnectionInfo();
	/*
	 * 불규칙변형을 탐색하기위해 원형으로 복원하여 탐색해보도록 수정하는 로직.
	 * */
	protected abstract List<TagProb> modifyIrregularVerb(CharVector cv);
	/*
	 * 두 PosTag간의 룰기반 접속문법검사
	 * */
	protected abstract boolean isConnectableByRule(TagProb tagProb, int row, int column, Entry tail);
	/*
	 * 두 엔트리를 접속시 합치거나 이어붙이는 로직을 구현한다.
	 * */
	protected abstract Entry modifyAndConnect(TagProb tagProb, int row, int column, Entry tail);
	/*
	 * Best 결과를 뽑을때 사용하는 비교로직.
	 * */
	protected abstract boolean isBetterThan(Entry entry, Entry bestEntry);
	/////////////////////////////////////////
	
	
	public void showTabular(){
		for (int row = 0; row < length; row++) {
			PosTagProbEntry[] el = tabular[row];
			int count = status[row];
			StringBuilder sb= new StringBuilder();
			sb.append("{ ");
			sb.append(count);
			sb.append(" }");
			sb.append(" | ");
			for (int column = 1; column <= row + 1; column++) {
				PosTagProbEntry e = el[column];
				sb.append(new String(source, row - column + 1, column));
				sb.append("[");
				while(e != null){
					sb.append(e.get());
					e = e.next();
				}
				sb.append("]");
				sb.append(" | ");
			}
			logger.debug("{}", sb);
			
		}
		
	}
	
	/*
	 * 음절을 조합하여 사전에서 찾아준다.
	 * 찾은 단어와 tag는 table에 저장한다. 
	 * */
	private void doSegment() {

		for (int row = length - 1; row >= 0; row--) {

			for (int column = row + 1; column >= 1; column--) {
				CharVector cv = new CharVector(source, row - column + 1, column);
//				logger.debug("{}", cv);
				List<TagProb> tagList = koreanDic.find(cv);

				//불규칙변형등은 차후 connect 시 처리한다.
//				if(tagList == null){
//					//동사는 뒤에 접속할만한 후보가 존재할때만 검사.
////					if(cv.length > 1 && status[row + column] > 0){
//					//하다. 가다도 체크한다.
//					if(status[row + column] > 0){
//						tagList = modifyIrregularVerb(cv);
//					}
//				}
				
				if (tagList != null){
					PosTagProbEntry chainedEntry = null;
					for (int i = 0; i < tagList.size(); i++) {
						TagProb tagProb = tagList.get(i);
						if(i == 0){
							chainedEntry = new PosTagProbEntry(tagProb);
							tabular[row][column] = chainedEntry;
						}else{
							chainedEntry = chainedEntry.next(tagProb);
						}
					}
					status[row]++;
				}
				
			}
		}
	}


	private void makeResult() {
		int headRow = -1;
		for (int row = length - 1; row >= 0; row--) {
			if (status[row] > 0) {
				// 분석결과가 존재하는지.
				headRow = row;
				break;
			}
		}
		
		try{
			
			if (headRow == -1) {
				// 통째 미등록어.
				addResult(new Entry(length - 1, length, TagProb.UNK));
				return;
			}

		
			//최초 char부터의 단어매칭이 없다면.
			//예를들어 "대한민국"분석시 "대한"만 사전에 있어서 "민국"은 결과가 없을경우.
			if (headRow < length - 1) {
				// 뒷부분을 미등록어로 처리한다. "대한(N)+민국(UNK)" 이 된다.
				Entry tail = new Entry(length - 1, length - 1 - headRow, TagProb.UNK);
				connectAllTo(headRow, tail);
			} else {
				connectAllTo(headRow, null);
			}
	
			Entry tail = null;
			while ((tail = queue.pollFirst()) != null) {
				int connectRow = tail.row() - tail.column();
				if (status[connectRow] > 0) {
					connectAllTo(connectRow, tail);
				} else {
					connectTo(null, connectRow, -1, tail);
				}
			}
		}catch(AnalyzeExceedException e){
			//분석을 중단하고 탈출한다.
		}

	}
	

	private int connectAllTo(int headRow, Entry tail) throws AnalyzeExceedException {
		PosTagProbEntry[] rowData = tabular[headRow];
		int found = 0;
		//최장길이부터 찾는다.
		for (int headColumn = headRow + 1; headColumn  > 0; headColumn--) {
			if (rowData[headColumn] != null) {
				PosTagProbEntry tagEntry = rowData[headColumn];
				connectTo(tagEntry, headRow, headColumn, tail);
				found++;
			}

			// 갯수만큼 다 찾았으면 일찍 종료한다.
			if (found >= status[headRow]) {
				break;
			}
			
		}
		return found;
	}
	private int connectTo(PosTagProbEntry headTagEntry, int headRow, int headColumn, Entry tail) throws AnalyzeExceedException {
		
		if(tail == null){
			//처음 
			int found = 0;
			while(headTagEntry != null){
				Entry headEntry = new Entry(headRow, headColumn, headTagEntry.get());
				if (headEntry.row() - headEntry.column() < 0) {
					addResult(headEntry);
				} else {
					addQueue(headEntry);
				}
				headTagEntry = headTagEntry.next;
				found++;
			}
			//바로리턴.
			return found;
		}
		
		if(headTagEntry == null){
			//해당 row의 모든 column을 확인해본다.
			for (int column = 1; column <= headRow + 1; column++) {
				int row2 = headRow - column;
				//head앞에 결합가능한 것이 있다면 현재 head를 미등록처 처리하고 링크로 이어준다.
				//앞쪽에 결합가능한것이 없으면 현재 head는 버린다.
				//row2 < 0 는 어절의 처음에 도달한것임.
				if (row2 < 0 || status[row2] > 0) {
					Entry unkEntry = new Entry(headRow, column, TagProb.UNK);
					
					if(isConnectable(unkEntry.tagProb(), headRow, column, tail)){
						Entry newTail = modifyAndConnect(unkEntry.tagProb(), headRow, column, tail);
						
						if (newTail.row() - newTail.column() < 0) {
							addResult(newTail);
						} else {
							addQueue(newTail);
						}
					}
				}
			}
			return 1;
		}
		int found = 0;
		while(headTagEntry != null){
			if(isConnectable(headTagEntry.get(), headRow, headColumn, tail)){
//				logger.debug("MAC {} >> {}, {}", tail.getChainedString(source), new String(source, headRow, headColumn), tail);
				Entry newTail = modifyAndConnect(headTagEntry.get(), headRow, headColumn, tail);
				
				if (newTail.row() - newTail.column() < 0) {
					addResult(newTail);
				} else {
					addQueue(newTail);
				}
				
				found++;
			}
			headTagEntry = headTagEntry.next;
		}
		return found;
	}

	//
	protected void addQueue(Entry entry) throws AnalyzeExceedException{
		//결과로 넣음.
//		logger.debug("#Queue >> {}", entry.getChainedString(source));
		queue.add(entry);
		if(queue.size() >= QUEUE_MAX){
			throw Analyze_Exceed_Exception;
		}
	}
	
	protected void addResult(Entry entry) throws AnalyzeExceedException {
		//결과로 넣음.
//		logger.debug("#Result >> {}", entry.getChainedString(source));
		result.add(entry);
		if(result.size() >= RESULT_MAX){
			throw Analyze_Exceed_Exception;
		}
	}

	private void resetTabular(){
		for (int row = 0; row < length; row++) {
			status[row] = 0; 
			for (int column = 1; column < tabular[row].length; column++) {
				tabular[row][column] = null;
			}
		}
	}
	public int setInput(char[] buffer, int length) {
		if (length >= tabular.length) {
//			logger.error("input string too large. length={}, {}", length, new String(buffer, 0, length));
			return -1;
		}
		this.source = buffer;
		queue.clear();
		result.clear();
		// tabluar초기화.
		this.length = length;
		resetTabular();
		return length;
	}
	

	public Entry extract() {
		doSegment();
//		showTabular();
		makeResult();
		return getBestResult();
	}
	public List<Entry> getAllResult() {
		return result;
	}
	
	public Entry getBestResult() {
		Entry bestEntry = null;
		for (int k = 0; k < result.size(); k++) {
			Entry entry = result.get(k);
			
			if(bestEntry == null){
				bestEntry = entry;
			}else{
				if(isBetterThan(entry, bestEntry)){
					bestEntry = entry;
				}
			}
		}
		
		if(bestEntry == null){
			//통째 미등록어.
			bestEntry = new Entry(length - 1, length, TagProb.UNK);
		}
		return bestEntry;
	}

	
}
