package org.fastcatsearch.plugin.analysis.ko.standard.action;

import java.io.Writer;

import javax.servlet.ServletException;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.dictionary.SynonymDictionary;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.ko.KoreanAnalysisPlugin;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.TagCharVector;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry.TagProb;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/get-synonym-list")
public class GetSynonymListAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String keyword = request.getParameter("keyword");
		String upperCaseKeyword = keyword.toUpperCase();
		Writer writer = response.getWriter();
		
		writeHeader(response);
		
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object()
		.key("keyword").value(keyword)
		.key("synonym").array("item");
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		AnalysisPlugin plugin = (AnalysisPlugin) pluginService.getPlugin("KOREAN");
		CommonDictionary<TagProb, PreResult<TagCharVector>> commonDictionary = plugin.getDictionary();
		SynonymDictionary synonymMap = (SynonymDictionary) commonDictionary.getDictionary("synonym");
		CharVector[] synonyms = synonymMap.map().get(new CharVector(upperCaseKeyword));
		try {
			if(synonyms != null){
				for (int i = 0; i < synonyms.length; i++) {
					responseWriter.value(synonyms[i].toString());
				}
			}
		}catch(Exception e){
			throw new ServletException(e);
		}
		responseWriter.endArray();
		responseWriter.endObject();
		responseWriter.done();
		
	}
}
