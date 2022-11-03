package org.fastcatsearch.plugin.analysis.ko.standard.action;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.dic.CommonDictionary;
import org.fastcatsearch.ir.dic.PreResult;
import org.fastcatsearch.ir.dictionary.SynonymDictionary;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.PosTagProbEntry;
import org.fastcatsearch.plugin.analysis.ko.standard.dictionary.TagCharVector;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

import javax.servlet.ServletException;
import java.io.CharArrayReader;
import java.io.Writer;

/**
 * Created by 전제현 on 2016-02-22.
 * source 파라미터를 받아 해당 파라미터의 키워드 분석 결과를 리턴해주는 서비스
 * /_plugin/KOREAN/analyze.json?source=(키워드)
 */

@ActionMapping("/analyze")
public class GetAnalyzedResult extends ServiceAction {

    @Override
    public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        String keyword = request.getParameter("source");
        String upperCaseKeyword = keyword.toUpperCase();
        Writer writer = response.getWriter();

        writeHeader(response);

        ResponseWriter responseWriter = getDefaultResponseWriter(writer);
        responseWriter.object()
                .key("source").value(keyword)
                .key("words").array("terms");

        AnalyzerPool analyzerPool = null;
        Analyzer analyzer = null;
        Throwable error = null;
        try {
            PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
            AnalysisPlugin plugin = (AnalysisPlugin) pluginService.getPlugin("KOREAN");
            analyzerPool = plugin.getAnalyzerPool("standard");
            AnalyzerOption analyzerOption = new AnalyzerOption();
            analyzer = analyzerPool.getFromPool();

            char[] fieldValue = upperCaseKeyword.toCharArray();
            TokenStream tokenStream = analyzer.tokenStream("", new CharArrayReader(fieldValue), analyzerOption);
            tokenStream.reset();
            CharsRefTermAttribute termAttribute = null;
            if (tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
                termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
            }
            CharTermAttribute charTermAttribute = null;
            if (tokenStream.hasAttribute(CharTermAttribute.class)) {
                charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
            }
            while (tokenStream.incrementToken()) {
                String key = "";
                if (termAttribute != null) {
                    key = termAttribute.toString();
                } else if (charTermAttribute != null) {
                    key = charTermAttribute.toString();
                }
                responseWriter.value(key);
            }
        } catch (Throwable t) {
            error = t;
        } finally {
            responseWriter.endArray();
            if(analyzerPool != null && analyzer != null) {
                analyzerPool.releaseToPool(analyzer);
            }
            if(error != null) {
                responseWriter.key("error").value(error.toString());
            }
            responseWriter.endObject();
            responseWriter.done();
        }
    }
}
