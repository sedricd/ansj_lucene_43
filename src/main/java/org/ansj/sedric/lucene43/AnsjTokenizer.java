package org.ansj.sedric.lucene43;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.domain.TermNature;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

public class AnsjTokenizer extends Tokenizer {

	/**
	 * 分词策略
	 */
	private Analysis analysis = null;

	/**
	 * 忽略词
	 */
	private Set<String> execludeWords = null;

	/**
	 * 忽略词
	 */
	private CharArraySet stopWords = null;

	/**
	 * 是否分析英文词干
	 */
	private boolean filterStemmer = false;

	/**
	 * 分词内容
	 */
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	/**
	 * 词组偏移量
	 */
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

	/**
	 * 词组起始坐标
	 */
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

	private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

	/**
	 * 默认分词策略
	 * 
	 * @param factory
	 * @param reader
	 * @param filterStemmer
	 *            是否分析英文词干
	 */
	public AnsjTokenizer(Reader reader, boolean filterStemmer) {
		super(reader);
		this.analysis = new ToAnalysis(reader);
		this.filterStemmer = filterStemmer;
	}

	/**
	 * 
	 * @param reader
	 * @param analysisClass
	 * @param analyzer
	 * @param filterStemmer
	 *            是否分析英文词干
	 */
	public AnsjTokenizer(Reader reader, Class<? extends Analysis> analysisClass, AnsjAnalyzer analyzer,
			boolean filterStemmer) {
		super(reader);
		this.analysis = new ToAnalysis(reader);
		this.filterStemmer = filterStemmer;
		if (null != analyzer) {
			this.stopWords = analyzer.getStopwordSet();
			this.execludeWords = analyzer.getExecludeWords();
		}
		try {
			this.analysis = analysisClass.getConstructor(Reader.class).newInstance(reader);
		}
		catch (Exception e) {
			throw new RuntimeException("ansj analysis can't be instance !");
		}
	}

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		int position = 0;
		Term term = null;
		String name = null;
		int length = 0;
		boolean flag = true;
		do {
			term = analysis.next();
			if (term == null) {
				break;
			}
			length = term.getName().length();
			if (filterStemmer && term.getTermNatures().termNatures[0] == TermNature.EN) {
				name = new PorterStemmer().stem(term.getName());
				term.setName(name);
			}
			position++;

			if ((execludeWords != null && execludeWords.contains(name))
					|| (null != stopWords && !stopWords.isEmpty() && stopWords.contains(term.getName()))) {
				continue;
			} else {
				flag = false;
			}
		}
		while (flag);
		if (term != null) {
			posIncrAtt.setPositionIncrement(position);
			termAtt.setEmpty().append(term.getName());
			offsetAtt.setOffset(term.getOffe(), term.getOffe() + length);
			return true;
		} else {
			return false;
		}
	}

	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	public void setMaxTokenLength(int maxTokenLength) {
		this.maxTokenLength = maxTokenLength;
	}

	/**
	 * 必须重载的方法，否则在批量索引文件时将会导致文件索引失败
	 */
	@Override
	public void reset() throws IOException {
		analysis.resetContent(new BufferedReader(this.input));
	}

}
