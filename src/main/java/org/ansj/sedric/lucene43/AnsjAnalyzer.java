package org.ansj.sedric.lucene43;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

public class AnsjAnalyzer extends StopwordAnalyzerBase {

	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	/**
	 * 是否分析英文单词词干
	 */
	private boolean filtEnStemmer;

	/**
	 * 忽略词
	 */
	public Set<String> execludeWords;

	/**
	 * ansj分词策略
	 */
	private Class<? extends Analysis> analysis;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	/**
	 * An unmodifiable set containing some common English words that are usually
	 * not useful for searching.
	 */
	public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

	/**
	 * ansj标准分词
	 * 
	 * @param version
	 */
	public AnsjAnalyzer(Version version) {
		super(version);
		this.analysis = ToAnalysis.class;
	}

	/**
	 * 指定分词策略分词
	 * 
	 * @param version
	 * @param analysis
	 */
	public AnsjAnalyzer(Version version, Class<? extends Analysis> analysis) {
		super(version);
		this.analysis = analysis;
	}

	/**
	 * 指定分词策略分词，并忽略部分词组
	 * 
	 * @param version
	 * @param analysis
	 * @param execludeWords
	 */
	public AnsjAnalyzer(Version version, Class<? extends Analysis> analysis, Set<String> execludeWords) {
		super(version);
		this.analysis = analysis;
		this.execludeWords = execludeWords;
	}

	/**
	 * 指定分词策略分词，并忽略部分词组
	 * 
	 * @param matchVersion
	 * @param analysis
	 * @param execludeWords
	 * @param stopwords
	 *            通过文件指定忽略词
	 * @throws IOException
	 */
	public AnsjAnalyzer(Version matchVersion, Class<? extends Analysis> analysis, Set<String> execludeWords,
			Reader stopwords) throws IOException {
		super(matchVersion, loadStopwordSet(stopwords, matchVersion));
		this.analysis = analysis;
		this.execludeWords = execludeWords;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		final AnsjTokenizer src = new AnsjTokenizer(reader, analysis, this, true);
		src.setMaxTokenLength(maxTokenLength);
		TokenStream tok = new LowerCaseFilter(matchVersion, src);
		return new TokenStreamComponents(src, tok) {

			@Override
			protected void setReader(final Reader reader) throws IOException {
				src.setMaxTokenLength(AnsjAnalyzer.this.maxTokenLength);
				super.setReader(reader);
			}
		};
	}

	public boolean isFiltEnStemmer() {
		return filtEnStemmer;
	}

	public void setFiltEnStemmer(boolean filtEnStemmer) {
		this.filtEnStemmer = filtEnStemmer;
	}

	public Set<String> getExecludeWords() {
		return execludeWords;
	}

	public void setExecludeWords(Set<String> execludeWords) {
		this.execludeWords = execludeWords;
	}

	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	public void setMaxTokenLength(int maxTokenLength) {
		this.maxTokenLength = maxTokenLength;
	}

}
