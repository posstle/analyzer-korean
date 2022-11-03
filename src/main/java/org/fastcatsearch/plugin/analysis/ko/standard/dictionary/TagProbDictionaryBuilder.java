package org.fastcatsearch.plugin.analysis.ko.standard.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.store.OutputStreamDataOutput;
import org.fastcatsearch.ir.dictionary.WritableDictionary;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.util.CharVectorHashMap;
import org.fastcatsearch.plugin.analysis.ko.standard.PosTag;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TagProbDictionaryBuilder implements WritableDictionary {
	protected static Logger logger = LoggerFactory.getLogger(TagProbDictionaryBuilder.class);

	protected Map<CharVector, List<TagProb>> probMap;
	protected boolean ignoreCase;
	
	//확률기본사전파일.
	protected File probDictionarySourceFile;
	
	//사용할 태그.null이면 모든 태그사용됨.
	protected PosTag[] tags;
	
	//확률이 정해져있지 않은 모든 단어사전 (체언,용언,조사등..) 
	protected File[] dictionarySourceList;
	
	public TagProbDictionaryBuilder(){ 
		this(true);
	}
	public TagProbDictionaryBuilder(boolean ignoreCase){ 
		this.ignoreCase = ignoreCase;
		probMap = new CharVectorHashMap<List<TagProb>>(ignoreCase);
	}
	
	public TagProbDictionaryBuilder(File probDictionarySourceFile, PosTag[] usableTags, File[] dictionarySourceList){
		this.probDictionarySourceFile = probDictionarySourceFile;
		this.tags = usableTags;
		this.dictionarySourceList = dictionarySourceList;
	}
	
	public Map<CharVector, List<TagProb>> map(){
		return probMap;
	}
	
	public void loadDictionary(){
		loadProbDictionary();
		loadTagedDictionary();
	}
	
	// 확률사전 로딩. 아래와같음포맷임. 파일인코딩은 utf-8이어야함.
	// ㅂ니까 \t EC \t -12.0584954471018 
	//
	private void loadProbDictionary(){
		InputStream pis = null;
		try{
			probMap = new CharVectorHashMap<List<TagProb>>(ignoreCase);
			pis = new FileInputStream(probDictionarySourceFile);
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new InputStreamReader(pis, "utf-8"));
			String line = null;
			while((line = br.readLine()) != null){
				addEntry(line);
			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			if(pis != null){
				try{
					pis.close();
				} catch (IOException ignore) { }
			}
		}
	}
	
	// 일반사전을 로딩한다.
	// 미리 loadProbDictionary()을 통해 확률사전이 로딩되어있어야 기존 사전map에 추가될 수 있다.
	protected void loadTagedDictionary(){
		for (int i = 0; i < dictionarySourceList.length; i++) {
			File file =  dictionarySourceList[i];
			if(!file.exists()){
				logger.error("사전소스파일을 찾을수 없습니다. file={}", file.getAbsolutePath());
				continue;
			}
			String fileName = file.getName();
			String[] parts = fileName.split("\\.");
			String posName = parts[1];
			boolean isExtention = false;
			PosTag posTag = null;
			if(posName.equalsIgnoreCase("Extension")
				|| parts.length == 4 && parts[2].equalsIgnoreCase("Extension")
					){
				isExtention = true;
			}else{
				try{
					posTag = PosTag.valueOf(posName);
				}catch(IllegalArgumentException e){
					logger.error("Undefined pos tag = {}", posName);
					throw e;
				}
			}
			
			logger.info("load dic {}", file.getAbsolutePath());
			InputStream is = null;
			try {
				is = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				logger.error("사전소스파일 {}을 찾을수 없습니다.", e);
				continue;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
			String line = null;
			try {
				while((line = br.readLine()) != null){
					line = line.trim();
					if(line.startsWith("#") || line.startsWith("//") || line.length() == 0){
						//주석처리.
						continue;
					}
					addAppendedEntry(line, posTag, isExtention);
				}
			} catch (IOException e) {
				logger.error("", e);
			} finally {
				if(is != null){
					try{
						is.close();
					} catch (IOException ignore) { }
				}
				if(br != null){
					try {
						br.close();
					} catch (IOException ignore) { }
				}
			}
		}
	}
	
	//기본적으로 대소문자 무시.
	public void addEntry(String line) {
		
		String[] tmp = line.split("\t");
		PosTag posTag = null;
		if(tmp[1].startsWith("N")){
			posTag = PosTag.N;
		}else if(tmp[1].startsWith("V")){
			posTag = PosTag.V;
		}else if(tmp[1].startsWith("M")){
			posTag = PosTag.M;
		}else if(tmp[1].startsWith("IC")){
			posTag = PosTag.IC;
		}else if(tmp[1].startsWith("J")){
			posTag = PosTag.J;
		}else if(tmp[1].startsWith("E")){
			posTag = PosTag.E;
		}else if(tmp[1].startsWith("XPN")){
			posTag = PosTag.XPN;
		}else{
//			logger.debug("unknown >> {}", pline);
			return;
		}
		
		if(tags != null){
			boolean allowed = false;
			for (PosTag tag : tags) {
				if(tag == posTag){
					allowed = true;
					break;
				}
			}
			if(!allowed){
				//허용하지 않는 태그는 건너뛴다.
				return;
			}
		}
		CharVector key = new CharVector(tmp[0].trim());
		if(ignoreCase){
			key.setIgnoreCase();
		}
		double freq = Double.parseDouble(tmp[2].trim());
		List<TagProb> tagProbList = probMap.get(key);
		TagProb tagProb = new TagProb(posTag, freq);
		if(tagProbList == null){
			tagProbList = new ArrayList<TagProb>(1);
			probMap.put(key, tagProbList);
			tagProbList.add(tagProb);
		}else{
			if(tagProbList.contains(tagProb)){
				for (int i = 0; i < tagProbList.size(); i++) {
					TagProb tagProb2 = tagProbList.get(i);
					if(tagProb2.posTag() == tagProb.posTag()){
						if(tagProb2.prob() < tagProb.prob()){
							tagProbList.remove(i);
							tagProbList.add(tagProb);
							break;
						}
					}
				}
			}else{
				tagProbList.add(tagProb);
			}
		}
	}

	//append되는 사전의 대소문자는 해당사전의 대소문자 규칙에 따르므로, 단어를 그대로 넣어준다.
	protected void addAppendedEntry(String line, PosTag posTag, boolean isExtended){
		CharVector key = null;
		TagProb tagProb = null;
		if(isExtended){
			String[] tmp = line.split("\\t");

			//if(tmp.length < 2){
			//	logger.error("Extension Entry error >> {}", line);
			//	//"단어\t품사" 와 같이 기록되어있어야 한다. 
			//	return;
			//}
			key = new CharVector(tmp[0].trim());
			String tagName = PosTag.N.toString();
			if(tmp.length > 1) {
				tagName = tmp[1].trim();
				if(tagName.length() == 0){
					for(int t = 1; t < tmp.length; t++){
						tagName = tmp[t].trim();
						if(tagName.length() > 0){
							break;
						}
					}
				}
			}
			//custom tag는 최고점수를 준다.
			posTag = PosTag.valueOf(tagName);
			double probScore = TagProb.MAX_PROB;
			if(key.length() <= 2) {
				probScore = TagProb.LOW_PROB;
			}
			tagProb = new TagProb(posTag, probScore);
//			if(posTag == null){
//				logger.error("Custom Dic error >> {}", line);
//			}
		}else{
			String[] tmp = line.split("/");
			String tmpKey = tmp[0];
			if(tmpKey.contains(":")){
				tmpKey = tmpKey.split(":")[0];
			}
			if(tmpKey.contains(" ")){
				//공백이 들어있을 경우 떼어내서 추가한다. 사용자사전의 경우가 이에 포함됨.
				String[] tmpKeys = tmpKey.split(" ");
				for(String k : tmpKeys){
					k = k.trim();
					
					if(k.length() > 0){
						key = new CharVector(k);
						tagProb = new TagProb(posTag);
					}
					
				}
			}else{
				key = new CharVector(tmpKey);
				tagProb = new TagProb(posTag);
			}
		}
		
		
		if(tags != null){
			boolean allowed = false;
			for (PosTag tag : tags) {
				if(tag == posTag){
					allowed = true;
					break;
				}
			}
			if(!allowed){
				//허용하지 않는 태그는 건너뛴다.
				return;
			}
		}
		
		List<TagProb> tagProbList = probMap.get(key);
		
		if(tagProbList == null){
			tagProbList = new ArrayList<TagProb>(1);
			tagProbList.add(tagProb);
			probMap.put(key, tagProbList);
		}else{
			if(!tagProbList.contains(tagProb)){
				tagProbList.add(tagProb);
			}
		}
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		
		@SuppressWarnings("resource")
		DataOutput output = new OutputStreamDataOutput(out);
		Iterator<CharVector> keySet = probMap.keySet().iterator();
		//write size of map
		
		output.writeInt(probMap.size());
		//write key and value map
		for(;keySet.hasNext();) {
			//write key
			CharVector key = keySet.next();
			output.writeString(key.toString());
			
			//write values
			List<TagProb> tagProbs = probMap.get(key);
			output.writeInt(tagProbs.size());
			for(TagProb tagProb : tagProbs) {
				output.writeString(tagProb.posTag().toString());
				output.writeDouble(tagProb.prob());
			}
		}
	}

	
}
