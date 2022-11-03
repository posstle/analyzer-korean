package org.fastcatsearch.plugin.analysis.ko.standard;

import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.common.MorphUtil;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;

import java.util.List;


public class StandardKoreanExtractor extends AbstractKoreanExtractor {
	
	public StandardKoreanExtractor(CommonDictionary<TagProb, PreResult<CharVector>> koreanDic){
		super(koreanDic, 12);
	}
	//tabularSize는 한번에 처리할수 있는 어절길이를 의미한다.
	public StandardKoreanExtractor(CommonDictionary<TagProb, PreResult<CharVector>> koreanDic, int tabularSize) {
		super(koreanDic, tabularSize);
	}

	@Override
	protected void loadConnectionInfo(){
		/*
		 * 접속가능항목을 입력한다.
		 * */
		setConnectable(PosTag.SOE, PosTag.XPN, PosTag.UNK, PosTag.N, PosTag.V, PosTag.IC, PosTag.M);
		//어절의 처음은 UNK, N, V, IC, M, XPN만이 올 수 있다. 
		setConnectable(PosTag.XPN, PosTag.N, PosTag.UNK);
		//XPN(접두사는 바로 뒤에 N(명사), UNK(명사추정)만 올수 있다. 
		
		setConnectable(PosTag.UNK, PosTag.UNK, PosTag.N, /*PosTag.V, PosTag.M, PosTag.IC, PosTag.J, PosTag.E,*/ PosTag.X);
		setConnectable(PosTag.N, PosTag.UNK, PosTag.N, PosTag.V /*PosTag.M, PosTag.IC,*/ , PosTag.J /*PosTag.E,*/, PosTag.X);
		setConnectable(PosTag.V, /*PosTag.UNK PosTag.N, PosTag.V,  PosTag.M, PosTag.IC, PosTag.J,*/ PosTag.E /*, PosTag.X*/);
		//부사 및 관형사.
		setConnectable(PosTag.M, /*PosTag.UNK, PosTag.N,*/ PosTag.V, /*PosTag.M, PosTag.IC, PosTag.J,*/ PosTag.E /*, PosTag.X*/);
//		setConnectable(PosTag.IC, /*PosTag.UNK, PosTag.N, PosTag.V, PosTag.M, PosTag.IC, PosTag.J, PosTag.E , PosTag.X*/);
		setConnectable(PosTag.J /*, PosTag.UNK*/, PosTag.N, PosTag.V /*, PosTag.M, PosTag.IC, PosTag.J, PosTag.E, PosTag.X*/);
		//setConnectable(PosTag.J /*PosTag.UNK, PosTag.N, PosTag.V, PosTag.M, PosTag.IC, PosTag.J, PosTag.E, PosTag.X*/);
		setConnectable(PosTag.E /*, PosTag.UNK*/, PosTag.N /*, PosTag.V, PosTag.M, PosTag.IC,*/ ,PosTag.J, PosTag.E /*, PosTag.X*/);
		setConnectable(PosTag.EP /*PosTag.N, PosTag.V,  PosTag.M, PosTag.IC, PosTag.J,*/ , PosTag.E, PosTag.EP /*, PosTag.X*/);
		//확장종결어미. 검색용. 했다+가 등이 가능.
		setConnectable(PosTag.X /*PosTag.UNK, PosTag.N, PosTag.V, /*PosTag.M, PosTag.IC*/, PosTag.J /*, PosTag.E, PosTag.X*/);
		
		setConnectable(PosTag.UNK, PosTag.EOE);
		setConnectable(PosTag.N, PosTag.EOE);
		setConnectable(PosTag.J, PosTag.EOE);
		setConnectable(PosTag.E, PosTag.EOE);
		setConnectable(PosTag.IC, PosTag.EOE);
		setConnectable(PosTag.M, PosTag.EOE);
		//어절의 끝은 UNK, N, J, E, M, IC  만이 올 수 있다. 
		
	}
	
	private List<TagProb> replaceAndSearch(CharVector cv, int pos, char ch, char replaceChar){
		cv.setChar(pos, replaceChar);
		List<TagProb> tagList = koreanDic.find(cv);
		cv.setChar(pos, ch);
		return tagList;
	}
	
	@Override
	protected List<TagProb> modifyIrregularVerb(CharVector cv){

		int lastPos = cv.length() - 1;
		char ch = cv.charAt(lastPos);
		char[] chrs = MorphUtil.decompose(ch);
		if ( chrs.length ==  1)
			return null;
		
//		logger.debug("{}", new String (cv.array, cv.start, cv.length) );
		if ( chrs.length == 3 ) 
		{
//			logger.debug("chars : {} {} {} | {}", chrs[0], chrs[1], chrs[2], MorphUtil.combine(chrs[0], chrs[1]));
			
			if ( chrs[2] == 'ㄴ' || chrs[2] == 'ㄹ' || chrs[2] == 'ㅁ' || chrs[2] == 'ㅂ')
				return replaceAndSearch(cv, lastPos, ch, MorphUtil.combine(chrs[0], chrs[1]));
			else if ( chrs[1] == 'ㅘ' )
				return replaceAndSearch(cv, lastPos, ch, MorphUtil.makeChar(ch, 8, 0 ));
			else if ( chrs[1] == 'ㅝ' )
				return replaceAndSearch(cv, lastPos, ch, MorphUtil.makeChar(ch, 13, 0 ));
			else if ( chrs[1] == 'ㅙ' )
				return replaceAndSearch(cv, lastPos, ch, MorphUtil.makeChar(ch, 11, 0 ));
			else if ( chrs[1] == 'ㅕ' )
				return replaceAndSearch(cv, lastPos, ch, MorphUtil.makeChar(ch, 20, 0 ));
			else if ( chrs[1] == 'ㅐ' )
				return replaceAndSearch(cv, lastPos, ch, MorphUtil.makeChar(ch, 0, 0 ));
			else if ( chrs[1] == 'ㅒ' )
				return replaceAndSearch(cv, lastPos, ch, MorphUtil.makeChar(ch, 20, 0 ));			
		}
		return null;
	}
	@Override
	protected boolean isConnectableByRule(TagProb headTagProb, int headRow, int headColumn, Entry tail) {
		
//		logger.debug("test >> {} + {}", new String(source, headRow - headColumn + 1, headColumn), tail);
		//제일 큰 속도저하는 한글자의 연속된 명사들 때문이므로 여기서 처리하도록 한다.
		//1. 미등록어 + 1글자  
		if(headTagProb.posTag() == PosTag.UNK && tail.column() == 1){
			return false;
		}
		// 1글자+ 미등록어.
		if(tail.posTag() == PosTag.UNK && headColumn == 1){
			return false;
		}
		
		//2. 2개 연속된 한글자짜리 명사
		if(headTagProb.posTag() == PosTag.N
				&& tail.posTag() == PosTag.N
				&& headColumn == 1 && tail.column() == 1){
			return false;
		}

		if(headTagProb.posTag() == PosTag.N && tail.posTag() == PosTag.N && headColumn == 1){
			if(tail.column() == 2) {
				//"발 + 매트" 와 같이 1+2 명사만 허용한다.
				return true;
			} else {
				// 두개연속의 체언중 앞의것이 명사면 허용하지 않음. 접두사이어야만함.
				return false;
			}
		}

		if((headTagProb.posTag() == PosTag.N || headTagProb.posTag() == PosTag.XPN)
				&& tail.posTag() == PosTag.N ) {
//				&& headColumn > 1 && tail.column() == 1){
			//연속된 3개의 명사중 2개이상 한글자이면 허용하지 않음.
			if(tail.next() != null && tail.next().posTag() == PosTag.N){
				int count = 0;
				if(headColumn == 1){
					count++;
				}
				if(tail.column() == 1){
					count++;
				}
				if(tail.next().column() == 1){
					count++;
				}
				return count <= 1;
			}
		}
		
		if(headTagProb.posTag() == PosTag.E
				&& tail.posTag() == PosTag.J){
			return false;
		}
		
		//은,이 와 는,가 일때 앞의 단어에 받침이 있는 지 확인.
		if(tail.tagProb().posTag() == PosTag.J && tail.column() == 1){
//			logger.debug("last >> {}", last.getChainedString(str));
			//ch는 tail의 첫번째 글자.
			//source[headRow]는 head의 마지막 글자.
			char ch = source[tail.row() - tail.column() + 1];
			if(ch == '은' || ch == '이'){
				return MorphUtil.hasLastElement(source[headRow]);
			}else if(ch == '는' || ch == '가'){
				return !MorphUtil.hasLastElement(source[headRow]);
			}
			
		}
		return true;
	}
	/**
	 * tail은 변경되면 안됨.
	 */
	@Override
	protected Entry modifyAndConnect(TagProb tagProb, int row, int column, Entry tail) {
//		logger.debug("modifyAndConnect > {} + {}", lastEntry.getChainedString(str), str.substring(row, row+column)+" / "+posTag);
		//둘다 미등록어이면 합쳐준다.
		if(tagProb.posTag() == PosTag.UNK && tail.posTag() == PosTag.UNK){
			tail = tail.clone();
			tail.column(tail.column() + column);
			tail.tagProb(TagProb.UNK);
			return tail;
		}
		//두개 모두 한글자분석이면서 미등록어가 하나라도 있으면 통짜로 미등록어처리.
		if(column == 1 && tail.column() == 1 
				&& (tagProb.posTag() == PosTag.UNK || tail.posTag() == PosTag.UNK) ){
			//한글자 늘려준다.
			tail = tail.clone();
			tail.column(tail.column() + 1);
			tail.tagProb(TagProb.UNK);
			return tail;
		}
//		int scoreAdd = 0;
//		if(tagProb.posTag() == PosTag.N && tail.posTag() == PosTag.J){
//			scoreAdd = 1;
//		}
		Entry newEntry = new Entry(row, column, tagProb);
		return newEntry.next(tail);
	}
	@Override
	protected boolean isBetterThan(Entry entry, Entry bestEntry) {
		//점수가 큰쪽이 우선.
		if(entry.totalScore() > bestEntry.totalScore()){
			return true;
		}
		return false;
	}

}
