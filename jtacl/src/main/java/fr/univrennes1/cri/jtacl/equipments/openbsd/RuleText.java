/*
 * Copyright (c) 2010, Université de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the ESUP-Portail license as published by the
 * ESUP-Portail consortium.
 *
 * Alternatively, this software may be distributed under the terms of BSD
 * license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * Describes the text representation of a rule.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class RuleText {
	private int _lineNumber;
	private int _lineCount;
	private int _charCount;
	private String _text;
	private String _inputText;

	public int getCharCount() {
		return _charCount;
	}

	public void setCharCount(int charCount) {
		_charCount = charCount;
	}

	public int getLineCount() {
		return _lineCount;
	}

	public void setLineCount(int lineCount) {
		_lineCount = lineCount;
	}

	public int getLineNumber() {
		return _lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		_lineNumber = lineNumber;
	}

	public String getText() {
		return _text;
	}

	public void setText(String text) {
		_text = text;
	}

	public String getInputText() {
		return _inputText;
	}

	public void setInputText(String inputText) {
		_inputText = inputText;
	}

}
