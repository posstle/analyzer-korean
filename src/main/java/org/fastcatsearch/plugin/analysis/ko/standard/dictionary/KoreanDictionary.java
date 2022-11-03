//package org.fastcatsearch.plugin.analysis.ko.standard.dictionary;
//
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.fastcatsearch.ir.dic.Dictionary;
//import org.fastcatsearch.ir.io.CharVector;
//import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//
//public class KoreanDictionary extends Dictionary<TagProb> {
//	private static Logger logger = LoggerFactory.getLogger(KoreanDictionary.class);
//	
//	private Map<CharVector, List<TagProb>> probMap;
//	private Map<CharVector, CharVector[]> synonymMap;
//	private Set<CharVector> stopwordSet;
//	
//	public KoreanDictionary(Map<CharVector, List<TagProb>> probMap) {
//		this.probMap = probMap;
//	}
//	
//	public void basicMap(Map<CharVector, List<TagProb>> probMap){
//		this.probMap = probMap;
//	}
//	
//	public Set<CharVector> stopwordSet(){
//		return stopwordSet;
//	}
//	public void stopwordSet(Set<CharVector> stopwordSet){
//		this.stopwordSet = stopwordSet;
//	}
//	public Map<CharVector, CharVector[]> synonymMap(){
//		return synonymMap;
//	}
//	public void synonymMap(Map<CharVector, CharVector[]> synonymMap){
//		this.synonymMap = synonymMap;
//	}
//
//	@Override
//	public List<TagProb> find(CharVector key){
////		logger.debug("search Dictionary : {}", key);
//		if(probMap.containsKey(key)) {
//			return probMap.get(key);
//		} else {
//			return null;
//		}
//	}
//
//	public void checkUseStatus(){
//
//		Iterator<Map.Entry<CharVector, List<TagProb>>> iterator = probMap.entrySet().iterator();
//		while(iterator.hasNext()){
//			Map.Entry<CharVector, List<TagProb>> entry = iterator.next();
//			CharVector key = entry.getKey();
//			List<TagProb> tagList = entry.getValue();
//			int typeSize = tagList.size();
//			logger.info("{}:\t{}", key, typeSize);
//			for (int i = 0; i < typeSize; i++) {
//				TagProb tagProb = tagList.get(i);
//				logger.info("\t{}", tagProb);	
//			}
//		}
//	}
//
//	@Override
//	public int size() {
//		return probMap.size();
//	}
//}
