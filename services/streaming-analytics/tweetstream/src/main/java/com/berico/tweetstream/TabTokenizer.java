package com.berico.tweetstream;

import org.apache.commons.lang.StringUtils;
import org.springframework.batch.item.file.transform.DefaultFieldSetFactory;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.FieldSetFactory;
import org.springframework.batch.item.file.transform.LineTokenizer;

public class TabTokenizer implements LineTokenizer{
	private static final String TAB = "\t";
	
	private FieldSetFactory fieldSetFactory = new DefaultFieldSetFactory();
	
	
	@Override
	public FieldSet tokenize(String line) {
		String[] values = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, TAB);
		return fieldSetFactory.create(values);
	}

}
