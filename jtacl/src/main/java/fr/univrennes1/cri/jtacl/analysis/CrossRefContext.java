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

package fr.univrennes1.cri.jtacl.analysis;

import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

/**
 * Context of a cross reference
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CrossRefContext {
	protected ParseContext _parseContext;
	protected String _contextName;
	protected String _comment;

	public CrossRefContext(ParseContext parseContext, String contextName,
			String comment) {
		_parseContext = parseContext;
		_contextName = contextName;
		_comment = comment;
	}

	public String getComment() {
		return _comment;
	}
	
	public String getContextName() {
		return _contextName;
	}

	public ParseContext getParseContext() {
		return _parseContext;
	}

}
