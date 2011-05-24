/*
 * Copyright (c) 2010, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.lib.misc;

/**
 * Context while parsing
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ParseContext {

	protected String _fileName;
	protected int _lineNumber;
	protected String _line;

	public String getFileName() {
		return _fileName;
	}

	public String getLine() {
		return _line;
	}

	public int getLineNumber() {
		return _lineNumber;
	}

	public void set(String fileName, int lineNumber, String line) {
		_fileName = fileName;
		_lineNumber = lineNumber;
		_line = line;
	}

	public void setLine(String line) {
		_line = line;
	}

	public void setLineNumber(int lineNumber) {
		_lineNumber = lineNumber;
	}

	public void setFileName(String fileName) {
		_fileName = fileName;
	}

	@Override
	public String toString() {
		return "File: " + _fileName + " line#: " + _lineNumber +
			" "  + _line.trim() + " : ";
	}

	public String getFileNameAndLine() {
		return _fileName + " #" + _lineNumber;
	}
}
