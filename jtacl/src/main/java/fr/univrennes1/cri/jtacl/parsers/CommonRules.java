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


	/**
	 * Strip dupplicate white spaces from the string in argument.
	 * @param string String to strip.
	 * @return the string striped.
	 */
	public String stripWhiteSpaces(String string) {
		boolean wp = false;
		StringBuilder s = new StringBuilder("");

		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c == ' ' || c == '\t') {
				if (!wp) {
					s.append(c);
					wp = true;
				}
			} else {
				s.append(c);
				wp = false;
			}

		}
		return s.toString();
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
}
