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

package fr.univrennes1.cri.jtacl.parsers;

import org.parboiled.BaseParser;
import org.parboiled.Rule;

/**
 * Commons Parboiled rules.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CommonRules<T> extends BaseParser<T> {

	private char _quotec;
	private String _lastqtString;
	private char _previousChar;
	private boolean _qtStringEnd = false;

	public boolean setLastQuotedString(String string) {
		_lastqtString = string;
		return true;
	}

	/**
	 * Matches an atom: a string delimited by white spaces.
	 * @return a {@link Rule}
	 */
	public Rule StringAtom() {
		return OneOrMore(
					Sequence(
						TestNot(WhiteSpaces()),
						ANY
					)
				);
	}

	/**
	 * Matches white space: ' ' or tabulation.
	 * @return a {@link Rule}
	 */
	public Rule WhiteSpace() {
		return AnyOf(" \t");
	}

	/**
	 * Matches a number
	 * @return a {@link Rule}.
	 */
	public Rule Number() {
		return OneOrMore(
					AnyOf("0123456789")
				);
	}

	/**
	 * Matches white spaces.
	 * @return a {@link Rule}
	 */
	public Rule WhiteSpaces() {
		return OneOrMore(WhiteSpace());
	}

	/**
	 * Match and skip optional white spaces if any
	 * @return a{@link Rule}
	 */
	public Rule SkipSpaces() {
		return Optional(WhiteSpaces());
	}

	/**
	 * Matches anything until the end of input.
	 * @return a {@link Rule}
	 */
	public Rule UntilEOI() {
		return ZeroOrMore(ANY);
	}

	/**
	 * Matches anything except end of line.
	 * @return a {@link Rule}
	 */
	public Rule UntilEol() {
		return ZeroOrMore(
					Sequence(
						TestNot(
							Eol()
						),
						ANY
					)
				);
	}

	/**
	 * Matches end of line.
	 *
	 * @return {@link Rule}
	 */
	public Rule Eol() {
		return FirstOf(
				Ch('\n'),
				String("\r\n")
			);
	}

	/**
	 * Matches either white spaces or the end of the input.
	 * @return a {@link Rule}
	 */
	public Rule WhiteSpacesOrEoi() {
		return
			FirstOf(
				WhiteSpaces(),
				EOI
			);
	}

	public boolean quotedStringStart(String string) {
		_quotec = string.charAt(0);
		_lastqtString = "";
		_previousChar = '\0';
		_qtStringEnd = false;
		return true;
	}

	public boolean quotedStringContinue() {
		return !_qtStringEnd;
	}

	public boolean quotedString(String string) {
		char c = string.charAt(0);
		/*
		 * escaped character.
		 */
		if (_previousChar == '\\') {
			if (c != '\n')
				_lastqtString += c;
			_previousChar = '\0';
			return true;
		} else {
			if (c == '\\') {
				_previousChar = c;
				return true;
			}
			if (c != _quotec && c != '\n') {
				_previousChar = c;
				_lastqtString += c;
			}
			if (c == _quotec)
				_qtStringEnd = true;
			return true;
		}
	}

	/**
	 * Returns the last quoted string that matches the rule QuotedString.
	 * @return he last quoted string that matches the rule QuotedString.
	 */
	public String getLastQuotedString() {
		return _lastqtString;
	}

	/**
	 * Matches a quoted string.
	 * @return a Rule; result in getLastQuotedString()
	 */
	public Rule QuotedString() {
		return
		Sequence(
			AnyOf("'\""),
			quotedStringStart(match()),
			OneOrMore(
				Sequence(
					quotedStringContinue(),
					ANY,
					quotedString(match())
				)
			)
		);
	}

	/**
	 * Matches a String atom or a quoted String
	 * return a Rule; result in getLastQuotedString()
	 */
	public Rule StringOrQuotedString() {
		return (
			FirstOf(
				QuotedString(),
				Sequence(
					StringAtom(),
					setLastQuotedString(match())
				)
			)
		);
	}

}
