package org.fastcatsearch.plugin.analysis.ko.advanced;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.PosTag;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class AdvAbstractKoreanExtractor {
	protected static Logger logger = LoggerFactory.getLogger(AdvAbstractKoreanExtractor.class);

	protected CommonDictionary<TagProb, PreResult<CharVector>> koreanDic;

	private PosTagProbEntry[][] tabular;
	private int[] status;
	private int[][] connectionTable;
	
	private  ArrayDeque<Entry> queue = new ArrayDeque<Entry>(8);

	private List<Entry> result = new ArrayList<Entry>();

	protected char[] source;
	protected int length;

	public AdvAbstractKoreanExtractor(CommonDictionary<TagProb, PreResult<CharVector>> koreanDic, int tabularSize) {
		tabular = new PosTagProbEntry[tabularSize][];
		// tabular 파싱 초기화.
		for (int i = 0; i < tabular.length; i++) {
			tabular[i] = new PosTagProbEntry[tabular.length - i];
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
	
	private boolean isConnectable(Entry tail, TagProb next, int column){
		
		if(connectionTable[tail.posTag().ordinal()][next.posTag().ordinal()] == 1){
			if(isConnectableByRule(tail, next, column)){
				return true;
			}else{
				return false;
			}
			
		}
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
	protected abstract void modifyIrregularVerb(CharVector cv, int row, int column);
	/*
	 * 두 PosTag간의 룰기반 접속문법검사
	 * */
	protected abstract boolean isConnectableByRule(Entry tail, TagProb next, int column);
	/*
	 * 두 엔트리를 접속시 합치거나 이어붙이는 로직을 구현한다.
	 * */
	protected abstract Entry modifyAndConnect(Entry tail, int row, int column, TagProb tagProb);
	/*
	 * Best 결과를 뽑을때 사용하는 비교로직.
	 * */
	protected abstract boolean isBetterThan(Entry entry, Entry bestEntry);
	/**
	 * 동사를 처리 하는 부분. 
	 * */
	protected abstract void verbProcess();
	/////////////////////////////////////////
	
	
	
	public void showTabular(){
		for (int i = 0; i < length; i++) {
			PosTagProbEntry[] el = tabular[i];
			int count = status[i];
			StringBuilder sb= new StringBuilder();
			sb.append("{ ");
			sb.append(count);
			sb.append(" }");
			sb.append(" | ");
			for (int j = 1; j < length - i + 1; j++) {
				PosTagProbEntry e = el[j];
				sb.append(new String(source, i, j));
				sb.append("[" + i + ","+ j+"]");
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

			for (int column = 1; column <= length - row; column++) {
				int e = row + column;
//				String term = str.substring(row, e);
				CharVector cv = new CharVector(source, row, column);
				List<TagProb> tagList = koreanDic.find(cv);

				if(tagList == null){
					//동사는 뒤에 접속할만한 후보가 존재할때만 검사.
//					if(cv.length > 1 && status[row + column] > 0){
					//하다. 가다도 체크한다.
//					if(status[row + column] > 0){
//						modifyIrregularVerb(cv, row, column);
//					}
				}else
					setMatrix(tagList, row, column);
			}
		}
	}
	
	protected boolean checkEomi(int row)
	{
		
		for ( int i = 0 ; i < tabular[row].length ; i++ ) {
			PosTagProbEntry prob = tabular[row][i];
			if ( prob == null )
				continue;
			if ( prob.posContains(PosTag.E) || prob.posContains(PosTag.EP))
				{
//				logger.debug("row {} has Eomi", row);
				return true;
				}
		}
//		logger.debug("row {} doesn't has Eomi", row);
		return false;		
	}
	
	protected boolean checkEP(int row)
	{
		
		for ( int i = 0 ; i < tabular[row].length ; i++ ) {
			PosTagProbEntry prob = tabular[row][i];
			if ( prob == null )
				continue;
			if ( prob.posContains(PosTag.EP))
				{
				logger.debug("row {} has EP", row);
				return true;
				}
		}
		logger.debug("row {} doesn't has EP", row);
		return false;		
	}
	
	protected void setMatrix(List<TagProb> tagList, int row, int column)
	{
		if (tagList != null){
			PosTagProbEntry chainedEntry = null;//new PosTagProbEntry(tagList.get(0));

			if ( row == 0 && column == 1) {
				boolean nounExists = false;
				boolean xpnExists = false;
				
				for ( int i = 0 ; i < tagList.size() ; i ++ ) {
					TagProb tagProb = tagList.get(i);
					if ( tagProb.posTag() == PosTag.N )
						nounExists = true;
					else if ( tagProb.posTag() == PosTag.XPN )
						xpnExists = true;
				}
				
				//1음절 가장 처음 형태소가 접두사(XPN)와 명사(N)두개가 있다면 명사(N)을 삭제한다.
				if ( xpnExists == true && nounExists == true ) {
					List<TagProb> tempList = new ArrayList<TagProb>();
					for ( int i = 0 ; i < tagList.size() ; i ++ ) {
						TagProb tagProb = tagList.get(i);
						if ( tagProb.posTag() != PosTag.N )
							tempList.add(tagProb);
					}
					tagList.clear();
					tagList.addAll(tempList);
				}
			}
			logger.debug("set data row:{}, column:{}", row, column);
			for (int i = 0; i < tagList.size(); i++) {
				TagProb tagProb = tagList.get(i);
				if(i == 0){
					if ( tabular[row][column] == null ) {
						chainedEntry = new PosTagProbEntry(tagProb);
						tabular[row][column] = chainedEntry;
						}
					else {
						chainedEntry = tabular[row][column];
						chainedEntry = chainedEntry.next(tagProb);
					}
				}else{
					chainedEntry = chainedEntry.next(tagProb);
				}
			}
			status[row]++;
		}
	}
	
	protected void resetMatrix(List<TagProb> tagList, int row, int column)
	{
		if (tagList != null){
//			logger.debug("dic>{}, {}", cv, tagList);
			PosTagProbEntry chainedEntry = null;//new PosTagProbEntry(tagList.get(0));
//			tabular[row][column] = chainedEntry;
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

	private void removeTagCascade(int row, int column, PosTag e) {
		for (int r = row; r < length; r++) {
			for (int c = column - 1; c > 0; c--) {
				removeTag(r, c, e);
			}
		}
	}

	private void removeTag(int r, int c, PosTag posTag) {
		if(tabular[r][c] == null){
			return;
		}
//		logger.debug("removeTag =[{},{}]", r,c);
		PosTagProbEntry posTagEntry = tabular[r][c];
		PosTagProbEntry prevTagEntry = null;
		while(posTagEntry != null){
			if(posTagEntry.get().posTag() == posTag){
				//remove
//				logger.debug("removed!! =[{},{}]", r,c);
				if(posTagEntry.next() != null){
					if(prevTagEntry != null){
						prevTagEntry.next = posTagEntry.next();
					}else{
						tabular[r][c] = posTagEntry.next();
					}
				}else{
					if(prevTagEntry != null){
						prevTagEntry.next = null;
					}else{
						tabular[r][c] = null;
						status[r]--;
					}
				}
			}
			prevTagEntry = posTagEntry;
			posTagEntry = posTagEntry.next();
		}
	}

	private void makeResult() {
		int headRow = -1;
		for (int row = 0; row < length; row++) {
			if (status[row] > 0) {
				// 분석결과가 존재하는지.
				headRow = row;
				break;
			}
		}
//		logger.debug("### {}, {}, {}", new Object[]{str, length, headRow});
		if (headRow == -1) {
			// 통째 미등록어.
			// logger.debug("#UNK >> {}", string);
			addResult(new Entry(0, length, TagProb.UNK));
			return;
		}

		//최초 char부터의 단어매칭이 없다면.
		//예를들어 "한국어"분석시 "국어"만 사전에 있어서 "한"은 결과가 없을경우.
		if (headRow > 0) {
			// 처음부분을 미등록어로 처리한다. "한(UNK)"+..가 된다.
			Entry head = new Entry(0, headRow, TagProb.UNK);
			connectAllTo(head, headRow);
		} else {
			connectAllTo(null, headRow);
		}

		Entry tail = null;
		while ((tail = queue.pollFirst()) != null) {
			// 마지막 원소를 찾는다.
			// TODO 성능튜닝.
//			Entry last = head;
//			while (tail.next() != null) {
//				tail = tail.next();
//			}
//			logger.debug("head >> {}", head.getChainedString(source));
//			logger.debug("last >> {}", last.getChainedString(source));
			int connectRow = tail.row() + tail.column();

			if (status[connectRow] > 0) {
				connectAllTo(tail, connectRow);
			} else {
				connectTo(tail, null, connectRow, -1);
			}
		}

	}

	private int connectAllTo(Entry tail, int row){
		PosTagProbEntry[] rowData = tabular[row];
		int found = 0;
//		for (int column = 1; column <= length - row; column++) {
		//최장길이부터 찾는다.
		for (int column = length - row; column  > 0; column--) {
			if (rowData[column] != null) {
//				logger.debug("OK2 {} + {}", tail==null?null:tail.getChainedString(source), new String(source, row, column));
				PosTagProbEntry tagEntry = rowData[column];
				connectTo(tail, tagEntry, row, column);
				found++;
			}

			// 갯수만큼 다 찾았으면 일찍 종료한다.
			if (found >= status[row]) {
				break;
			}
			
		}
		return found;
	}
	private int connectTo(Entry tail, PosTagProbEntry tagEntry, int row, int column) {
		
		if(tail == null){
			//처음 
			int found = 0;
			while(tagEntry != null){
				Entry newEntry = new Entry(row, column, tagEntry.get());
				//형태소 분석의 처음에 올 품사들에 대한 제약. 
				if ( connectionTable[PosTag.SOE.ordinal()][newEntry.posTag().ordinal()] == 1 ) {
					if (row + column == length) {
//						logger.debug("NULL + {}", newEntry.getChainedString(source));
						addResult(newEntry);
					} else {
//						logger.debug("NULL + {}", newEntry.getChainedString(source));
						addQueue(newEntry);
					}
				}
				tagEntry = tagEntry.next;
				found++;
				
			}
			//바로리턴.
			return found;
		}
		
		if(tagEntry == null){
			// 연결되는 엔트리를 찾지 못햇다면 뒤쪽에 사용할만한 연결엔트리를 찾는다. 
			// 그것과의 사이에 있는 단어는 미등록어 처리한다.
			
//			Entry last = head;
//			while (last.next() != null) {
//				last = last.next();
//			}
			
			boolean linked = false;
			for (int row2 = row; row2 < length; row2++) {
				if (status[row2] > 0) {
					//미등록어에 미등록어를 붙여줄때는 통합한다.
					if(tail.posTag() == PosTag.UNK){
						tail = tail.clone();
						tail.column(tail.column() + row2 - row);
					}else{
						Entry unkEntry = new Entry(row, row2 - row, TagProb.UNK);
						tail = tail.next(unkEntry);
					}
					addQueue(tail);
//					logger.debug("No Head1 >> {}", head.getChainedString(str));
					// 처음하나만 사용하고 끝낸다.
					linked = true;
					break;

				}
			}

			// 뒤쪽에 분석결과가 없으면 뒤쪽은 통째로 미등록어.
			if (!linked) {
				Entry unkEntry = new Entry(row, length - row, TagProb.UNK);
				tail = tail.next(unkEntry);
//				logger.debug("No Head2 >> {}", head.getChainedString(str));
				addResult(tail);
			}
			//바로리턴.
			return 1;
		}
		
		int found = 0;
		while(tagEntry != null){
			
			if(isConnectable(tail, tagEntry.get(), column)){
//				logger.debug("MAC {} >> {}, {}", new Object[]{tail.getChainedString(source), new String(source, row, column), tagEntry});
				Entry newTail = modifyAndConnect(tail, row, column, tagEntry.get());
				
				if (row + column == length) {
					addResult(newTail);
//					logger.debug("addresult {}", newHead.getChainedString(str));
				} else {
					addQueue(newTail);
//					logger.debug("addresult {}", newHead.getChainedString(str));
				}
				
				found++;
			}
			tagEntry = tagEntry.next;
		}
		return found;
	}

	protected void addQueue(Entry entry) {
		//결과로 넣음.
//		logger.debug("#Queue >> {}", entry.getChainedString(source));
		queue.add(entry);
	}
	
	protected void addResult(Entry entry) {
		//결과로 넣음.
//		logger.debug("#Result >> {}", entry.getChainedString(source));
		result.add(entry);
	}

	private void resetTabular(){
		for (int row = 0; row < length; row++) {
			status[row] = 0; 
			for (int column = 1; column < tabular[row].length; column++) {
				tabular[row][column] = null;
			}
		}
	}
	public boolean setInput(char[] buffer, int length) {
		if (length >= tabular.length - 1) {
			logger.error("input string too large. length={}, {}", length, new String(buffer, 0, length));
			return false;
		}
		this.source = buffer;
		queue.clear();
		result.clear();
		// tabluar초기화.
		this.length = length;
		resetTabular();
		return true;
	}
	

	public Entry extract() {
		doSegment();
//		showTabular();//TEST
		verbProcess();
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
			bestEntry = new Entry(0, length, TagProb.UNK);
		}
		
		return bestEntry;
	}

	
}
