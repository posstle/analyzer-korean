package org.fastcatsearch.plugin.analysis.ko.advanced;

import java.util.List;

import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.analysis.ko.common.MorphUtil;
import org.fastcatsearch.plugin.analysis.ko.standard.PosTag;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;


public class AdvStandardKoreanExtractor extends AdvAbstractKoreanExtractor {
	
	public AdvStandardKoreanExtractor(CommonDictionary<TagProb, PreResult<CharVector>> koreanDic){
		super(koreanDic, 32);
	}
	//tabularSize는 한번에 처리할수 있는 어절길이를 의미한다.
	public AdvStandardKoreanExtractor(CommonDictionary<TagProb, PreResult<CharVector>> koreanDic, int tabularSize) {
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
		
		setConnectable(PosTag.UNK, PosTag.UNK, PosTag.N, PosTag.V, PosTag.M, PosTag.IC, PosTag.J/*, PosTag.E*/, PosTag.X);
		setConnectable(PosTag.N, PosTag.UNK, PosTag.N, PosTag.V /*PosTag.M, PosTag.IC,*/ , PosTag.J /*PosTag.E,*/, PosTag.X);
		setConnectable(PosTag.V, PosTag.UNK /*PosTag.N, PosTag.V,  PosTag.M, PosTag.IC, PosTag.J,*/ , PosTag.E, PosTag.EP /*, PosTag.X*/);
		setConnectable(PosTag.EP /*PosTag.N, PosTag.V,  PosTag.M, PosTag.IC, PosTag.J,*/ , PosTag.E, PosTag.EP /*, PosTag.X*/);
		
		//부사 및 관형사.
		setConnectable(PosTag.M, /*PosTag.UNK, PosTag.N,*/ PosTag.V, /*PosTag.M, PosTag.IC, PosTag.J,*/ PosTag.E /*, PosTag.X*/);
		setConnectable(PosTag.IC, PosTag.UNK /*PosTag.N, PosTag.V, PosTag.M, PosTag.IC, PosTag.J, PosTag.E , PosTag.X*/);
		setConnectable(PosTag.J /*PosTag.UNK, PosTag.N, PosTag.V, PosTag.M, PosTag.IC, PosTag.J, PosTag.E, PosTag.X*/);
		setConnectable(PosTag.E /*PosTag.UNK, PosTag.N, PosTag.V, PosTag.M, PosTag.IC,*/ ,PosTag.J, PosTag.E /*, PosTag.X*/);
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
		logger.debug("{}", new String (cv.array(), cv.start(), cv.length()) );
		List<TagProb> tagList = koreanDic.find(cv);
		if ( tagList == null )
			return tagList;
		
		//FIXME TagProb에 key가 없음.
		
		
//		for ( TagProb element : tagList )
//			element.key = cv.clone();
		cv.setChar(pos, ch);
		return tagList;
	}
	
	
	private void pEomi(CharVector cv, int row, int column)
	{
//		logger.debug("선어말어미 처리 : row : {}, column : {}, str : {}", row, column, cv);
		int lastPos = cv.length() -1;
		char ch = cv.charAt(lastPos);
		char lexicalChar='\0';
		char[] chrs = MorphUtil.decompose(ch);
		if ( chrs.length ==  1)
			return;
		
		if ( (chrs.length == 3) && (ch != '겠') && (chrs[2] == 'ㅆ') )
		{
			if ( chrs[0] =='ㅎ' && chrs[2] == 'ㅆ' )	{
//				logger.debug("하 검색 ");
				setMatrix(replaceAndSearch(cv, lastPos, ch, '하'), row, column);
			}
			else if (chrs[0] =='ㄷ' && chrs[2] == 'ㅆ'  ) {
				setMatrix(replaceAndSearch(cv, lastPos, ch, '되'), row, column);
			}
			else {
			logger.debug("set row :{}, column:{} 선어말어미 '었' ", row+1, column-1);
			setMatrix(koreanDic.find(new CharVector("었")), row+1, column-1);
			if ( chrs[1] == 'ㅘ' || chrs[1] == 'ㅝ' )	{
				//ㅂ 불규칙 
				irregularBI(cv, row, column, true);
				}
			}
			return;
		}
		else if ( ch == '십' )
		{
			setMatrix(koreanDic.find(new CharVector("시")), row+1, column-1);
			CharVector temp = new CharVector(cv.array(), cv.start(), cv.length()-1);
			if (temp.charAt(temp.length()-1) == '우')
				irregularBI(temp, row, column-1, false);
			return;
		}
	}
	
	private void eomi(CharVector cv, int row, int column) {
//		logger.debug("input {}", cv);
		int lastPos = cv.length() -1;
		if ( lastPos < 0 )
			return;
		char ch = cv.charAt(lastPos);
		char lexicalChar='\0';
		char[] chrs = MorphUtil.decompose(ch);
		if ( chrs.length ==  1)
			return;
		
		lexicalChar = MorphUtil.combine(chrs[0], chrs[1]);
		/*
		if (chrs.length == 3 && ( chrs[2] == 'ㄴ' || chrs[2] == 'ㄹ' || chrs[2] == 'ㅁ' || chrs[2] == 'ㅂ') ) {
			logger.debug("ㄴㄹㅁㅂ 받침 처리 {}", cv);
			lexicalChar = MorphUtil.combine(chrs[0], chrs[1]);
			setMatrix(replaceAndSearch(cv, lastPos, ch, lexicalChar ), row, column);
			}*/
		if ( chrs[1] == 'ㅘ' ) {
			lexicalChar = MorphUtil.makeChar(ch, 8, 0 );
			setMatrix(replaceAndSearch(cv, lastPos, ch, lexicalChar), row, column);
			}
		else if ( chrs[1] == 'ㅝ' ) {
			lexicalChar = MorphUtil.makeChar(ch, 13, 0 );
			setMatrix(replaceAndSearch(cv, lastPos, ch, lexicalChar), row, column);
		}
		else if ( chrs[1] == 'ㅙ' ) {
			lexicalChar = MorphUtil.makeChar(ch, 11, 0 );
			setMatrix(replaceAndSearch(cv, lastPos, ch, lexicalChar), row, column);
		}
		else if ( chrs[1] == 'ㅕ' ) {
			lexicalChar = MorphUtil.makeChar(ch, 20, 0 );
			setMatrix(replaceAndSearch(cv, lastPos, ch, lexicalChar), row, column);
		}
		else if ( chrs[1] == 'ㅐ' ) {				
			lexicalChar = MorphUtil.makeChar(ch, 0, 0 );
			setMatrix(replaceAndSearch(cv, lastPos, ch, lexicalChar), row, column);
		}
		else if ( chrs[1] == 'ㅒ' ) {			
			lexicalChar = MorphUtil.makeChar(ch, 20, 0 );
			setMatrix(replaceAndSearch(cv, lastPos, ch, lexicalChar), row, column);
		}
		
		char eomi = cv.charAt(cv.length());
		char prevChar = ( cv.length() > 1 ) ? cv.charAt(cv.length()-2) : '\0';
		char[] prvChrs = null;
		char[] eomiChrs = null;
		eomiChrs= MorphUtil.decompose(eomi);
		
		//불규칙 처리부 
		logger.debug("input: {}, stem : {}, next: {}, lexical : {} ", cv, ch, eomi, lexicalChar);
			
		if ( (eomi == '아' ) ||  
			 (eomi == '어' ) ||
			 (eomi == '으' ) )
			{
			if (chrs.length == 3 && chrs[2] == 'ㄹ') 	{
				//ㄷ 불규칙
				irregularDI(cv, row, column, false);
				return;
				}
			else if ((chrs.length == 2) || (chrs[2] == '\0') ) {
				//ㅅ 불규칙
				irregularSI(cv, row, column, false);
				return;
				}
			}				
		else if ( ( (eomiChrs.length > 1) && (eomiChrs[0] == 'ㄴ'  || eomiChrs[0] == 'ㅅ'  ||  eomiChrs[0] == 'ㄹ'  || eomiChrs[0] == 'ㅁ') )  && 
			     (lexicalChar == '우') )
			{//ㅂ 불규칙 
				irregularBI(cv, row, column, false);
				return;
			}
		else
			if ( (chrs.length== 2) && ( (eomi == '오') || ( eomiChrs.length > 1 && ( (eomiChrs[0] == 'ㄴ') || ( eomiChrs[0] == 'ㅂ' ) || (eomiChrs[0] == 'ㅅ') ) )) ) 
			{//ㄹ 탈락. 어간 + ㄴ,ㅂ,ㅅ,오 로 시작하는 어미 앞에서 어간의 받침 ㄹ 탈락 현상  
				if (( ch == '시') || ( chrs.length == 3 && chrs[0] == 'ㅅ' && chrs[2] == 'ㅆ' ))
					//선어말 어미 '시', '셨' 처리 부분, 시겠 선어말 어미 처리 
				{
//					logger.debug("{}", new String( cv.array, row, column-1));
					irregularRI(cv, row, column-1 , true);
				}
				else
				{
//					logger.debug("{}", new String( cv.array, row, column));
					irregularRI(cv, row, column , false);
				}
				return;
			}
		else 
			{
			if ( column == 2 )
			{
				//2음절일 경우 2번째 음절에 받침이 ㄴ,ㄹ,ㅁ,ㅂ 
				//빨갛 -> 빨간,파랗 -> 파란, 파랍니다, 하얗 -> 하얀, 
				if ( chrs.length == 3 && (chrs[2] == 'ㄴ' || chrs[2] == 'ㄹ' || chrs[2] == 'ㅁ' || chrs[2] == 'ㅂ'))
					irregularHI(cv, row, column, false);
			}
			else 
			{
				//3음절일 경우 아 + {서, 등등} 결합 
				// 하얗 -> 하얘서 : 하얗 + 아 + 서 
				// 파랗 -> 파래서 : 파랗 + 아 + 서 
				
			}
			}
//		if ( eomiChrs.length ==3 && eomiChrs.length > 2 &&( (chrs[2] == 'ㄴ') || (chrs[2] == 'ㄹ') || (chrs[2] == 'ㅁ') || (chrs[2] == 'ㅂ') ) && 
//					( (eomiChrs[1] == 'ㅏ') || (eomiChrs[1] == 'ㅓ' ) || (eomiChrs[1] == 'ㅑ') || (eomiChrs[1] == 'ㅕ') )) 
//			{
//				irregularHI(cv, row, column, false);
//				return;
//			}
//			
	}
	
	private void irregularBI(CharVector cv, int row, int column, boolean pEomi)
	{// ㅂ 불규칙 
		logger.debug("ㅂ 불규칙 ");
		if ( cv.length() < 2 ) return;
		int lastPos = cv.length()-1;
		if ( lastPos >  0 ) {
		char ch = cv.charAt(lastPos);
		
		char nCh = cv.charAt(lastPos-1);
		char chrs[] = MorphUtil.decompose(nCh);
		nCh = MorphUtil.combine(chrs[0], chrs[1], 'ㅂ');
		logger.debug("ㅂ 불규칙 {}",  nCh);
		CharVector temp = new CharVector(new String(cv.array(), cv.start(), lastPos));
		temp.setChar(lastPos-1, nCh);
		if ( pEomi == false ) {
			logger.debug("어미 + ㅂ 불규칙");
			setMatrix(koreanDic.find(temp), row, column);
		} else {
			logger.debug("선어말 어미 + ㅂ 불규칙");
			setMatrix(koreanDic.find(temp), row, column-1);
	 		}
		}
	}
	
	private void irregularSI(CharVector cv, int row, int column, boolean pEomi)
	{// ㅅ 불규칙 
//		logger.debug("ㅅ 불규칙 ");
//		if ( cv.length < 1 ) return;
		//ㅅ 불규칙은 1글자로 들어오는 케이스도 많음. 
		
		int lastPos = cv.length()-1;
		
		try
		{
		char ch = cv.charAt(lastPos);
		
		char chrs[] = MorphUtil.decompose(ch);
		char nCh = MorphUtil.combine(chrs[0], chrs[1], 'ㅅ');
		logger.debug("원형 : {}",  nCh);
		if ( pEomi == false )
			setMatrix(replaceAndSearch(cv, lastPos, ch, nCh), row, column);
		else 
			setMatrix(replaceAndSearch(cv, lastPos, ch, nCh), row, column-1);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	private void irregularHI(CharVector cv, int row, int column, boolean pEomi)
	{
// ㅎ 불규칙 
//		logger.debug("ㅎ 불규칙 ");
//		if ( cv.length < 2 ) return;
		
		int lastPos = cv.length()-1;
		
		if ( lastPos > 0 ) {
		char ch = cv.charAt(lastPos);
		
		char chrs[] = MorphUtil.decompose(ch);
		if ( chrs.length <= 1 )
			return ;
		char nCh = MorphUtil.combine(chrs[0], chrs[1], 'ㅎ');
//		logger.debug("ㅎ 불규칙 {}",  nCh);
		if ( pEomi == false )
			setMatrix(replaceAndSearch(cv, lastPos, ch, nCh), row, column);
		else 
			setMatrix(replaceAndSearch(cv, lastPos, ch, nCh), row, column-1);
		}
	}
	
	private void irregularDI(CharVector cv, int row, int column, boolean pEomi)
	{// ㄷ 불규칙 
//		logger.debug("ㄷ 불규칙 ");
		
		int lastPos = cv.length()-1;
		if ( lastPos >  0 )	{
		char ch = cv.charAt(lastPos);
		
		char chrs[] = MorphUtil.decompose(ch);
		char nCh = MorphUtil.combine(chrs[0], chrs[1], 'ㄷ');
//		logger.debug("ㄷ 불규칙 원형 {}->{}", ch, nCh);
		if ( pEomi == false )
			setMatrix(replaceAndSearch(cv, lastPos, ch, nCh), row, column);
		else 
			setMatrix(replaceAndSearch(cv, lastPos, ch, nCh), row, column-1);
		}
	}
	
	private void irregularRI(CharVector cv, int row, int column, boolean pEomi)
	{// ㄹ 탈락  
		CharVector temp = new CharVector(cv.array(), row, column);
//		logger.debug("ㄹ 탈락 row:{}, column:{}, {}", row, column, temp);
		if(column > 0) {
			int lastPos = column-1;
			char ch = temp.charAt(lastPos);
			
			char chrs[] = MorphUtil.decompose(ch);
			char replaceCh = MorphUtil.combine(chrs[0], chrs[1], 'ㄹ');
		//		logger.debug("ㄹ 탈락 원형 : {}->{} ",  temp, replaceCh);
			if ( pEomi == false ) {
				setMatrix(replaceAndSearch(temp, lastPos, ch, replaceCh), row, column);
		//			logger.debug("row :{} , column : {}", row, column);
			} else {
				setMatrix(replaceAndSearch(temp, lastPos, ch, replaceCh), row, column-1);
		//			logger.debug("row :{} , column : {}", row, column-1);
				}
		//		logger.debug(": {}->{} ",  temp, replaceCh);
		}
	}
	
	 
	
	@Override
	protected void modifyIrregularVerb(CharVector cv, int row, int column){
		int lastPos = cv.length() - 1;
		logger.debug("col :{} row ; {} , len : {} , str : {}", column, row, length, new String(cv.array(), cv.start(), cv.length()));
		
	
		char ch = cv.charAt(lastPos);
		char lexicalChar='\0';
		char[] chrs = MorphUtil.decompose(ch);
		if ( chrs.length ==  1)
			return ;
			
		logger.debug("check eomi({}) : {} ", row, checkEomi(row+1));
		logger.debug("check jaso : {} ", chrs);
		if (chrs.length == 3 && ( chrs[2] == 'ㄴ' || chrs[2] == 'ㄹ' || chrs[2] == 'ㅁ' || chrs[2] == 'ㅂ')) {
//			logger.debug("어미 ㅁ,ㄴ,ㄹ,ㅂ 처리 col :{} row ; {} , len : {} , str : {}", column, row, length, new String(cv.array, cv.start, cv.length));
			if ( ch == '십' )
				//선어말 어미 십.
				pEomi(cv, row, column);
			else				
				eomi(cv, row, column);
			}
		else //if ( checkEomi(row+1) == true )
		{
			logger.debug("other char col :{} row ; {} , len : {} , str : {}", column, row, length, new String(cv.array(), cv.start(), cv.length()));
			//선어말 어미 체크, '겠'은 위에서 이미 검색 했기 때문에 넘김.
			
			if ( (chrs.length == 3) && (ch != '겠') && (chrs[2] == 'ㅆ') ) {
				pEomi(cv, row, column);
			}
			else {
				eomi(cv, row, column);
			}
		}
		return;
	}
	@Override
	protected boolean isConnectableByRule(Entry tail, TagProb tagProb, int nextColumn) {
		
//		logger.debug("test >> {} + {}", last.getChainedString(source), tagProb);
		//제일 큰 속도저하는 한글자의 연속된 명사들 때문이므로 여기서 처리하도록 한다.
		//1. 미등록어 + 1글자 
		if(tail.posTag() == PosTag.UNK && nextColumn == 1){
			return false;
		}
		
		//2. 2개 연속된 한글자짜리 명사
		if(tail.posTag() == PosTag.N
				&& tagProb.posTag() == PosTag.N
				&& tail.column() == 1 && nextColumn == 1){
//			logger.debug("## prev1 = {}", last.getChainedString(str));
//			logger.debug("## next1 = {}", nextPosTag);
			return false;
		}
		
		if(tail.posTag() == PosTag.N
				&& tagProb.posTag() == PosTag.N
				&& tail.column() == 1 && nextColumn > 1){
			//이전것이 길이 2이상의 명사이면, N+ 1+ N 이므로 허용하지 않는다.
			if(tail.prev() != null && tail.prev().posTag() == PosTag.N){
				return false;
			}
		}
		
		if(tail.posTag() == PosTag.E
				&& tagProb.posTag() == PosTag.J	){
			return false;
		}
		
		if(tail.posTag() == PosTag.N
				&& tagProb.posTag() == PosTag.N
				&& tail.column() == 1 && nextColumn > 1){
			//이전것이 길이 2이상의 명사이면, N+ 1+ N 이므로 허용하지 않는다.
			if(tail.prev() != null && tail.prev().posTag() == PosTag.XPN){
				return false;
			}
		}
		
		if(tagProb.posTag() == PosTag.J && nextColumn == 1){
//			logger.debug("last >> {}", last.getChainedString(str));
			//은,이 와 는,가 일때 앞의 단어에 받침이 있는 지 확인.
			int nextPos = tail.row() + tail.column();
			char ch = source[nextPos];
			if(ch == '은' || ch == '이'){
//				logger.debug("check {} + {}", str[nextPos - 1], ch);
				return MorphUtil.hasLastElement(source[nextPos - 1]);
			}else if(ch == '는' || ch == '가'){
//				logger.debug("check {} + {}", str[nextPos - 1], ch);
				return !MorphUtil.hasLastElement(source[nextPos - 1]);
			}
			
		}
		return true;
	}
	@Override
	protected Entry modifyAndConnect(Entry tail, int row, int column, TagProb tagProb) {
//		logger.debug("modifyAndConnect > {} + {}", lastEntry.getChainedString(str), str.substring(row, row+column)+" / "+posTag);
		//둘다 미등록어이면 합쳐준다.
		if(tail.posTag() == PosTag.UNK && tagProb.posTag() ==  PosTag.UNK){
			tail = tail.clone();
			tail.column(tail.column() + column);
			tail.tagProb(TagProb.UNK);
			return tail;
		}
		//두개 모두 한글자분석이면서 미등록어가 하나라도 있으면 통짜로 미등록어처리.
		if(tail.column() == 1 && column == 1
				&& (tail.posTag() == PosTag.UNK || tagProb.posTag() ==  PosTag.UNK) ){
			//한글자 늘려준다.
			tail = tail.clone();
			tail.column(tail.column() + 1);
			tail.tagProb(TagProb.UNK);
			return tail;
		}
		
		Entry newEntry = new Entry(row, column, tagProb);
		return tail.next(newEntry);
	}
	
	@Override
	protected boolean isBetterThan(Entry entry, Entry bestEntry) {
		//점수가 큰쪽이 우선.
		if(entry.totalScore() > bestEntry.totalScore()){
			return true;
		}
		return false;
	}
	
	@Override
	protected void verbProcess()
	{
		int row = length -1;
		int column =1;
		CharVector cv = new CharVector(source, row, column);
		char ch = cv.charAt(cv.length()-1);
		char chrs[] = MorphUtil.decompose(ch);

		//가장 마지막 음절의 받침이 ㅁ,ㄴ,ㄹ,ㅂ으로 끝나면 어미 체크를 한번 한다. 
		if ( checkEomi(row) == false && (ch=='ㅁ' || ch == 'ㄴ' || ch == 'ㅂ' || ch == 'ㄹ' ) )
			modifyIrregularVerb(cv, row, column);
		
		logger.debug("어미가 있는 열을 찾음");
		for ( row = length - 1; row >= 0; row--) {
			if ( checkEP(row) == true )
				{
				row ++;
				break;
				}
		}
		
		if ( row == 0 )	{
			for ( row = length - 1; row >= 0; row--) {
				if ( checkEomi(row) == false )
					break;
			}			
		}
		
		
		if ( row != length-1 && row > 0 )	{			
			int wLen = row;
			cv = new CharVector(source, 0, wLen);
			logger.debug("{}에서 어미 찾음 동사 처리 : {}", row, cv);
			modifyIrregularVerb(cv, 0, wLen);
		}
		else {
			logger.debug("모든 행에서 어미 찾음 0 열에서 순차 검색 ", row);
			for ( int i = 1 ; i <= length ; i ++ )
			{
			cv = new CharVector(source, 0, i);
			modifyIrregularVerb(cv, 0, i);
			}
		} 
	}	
}

