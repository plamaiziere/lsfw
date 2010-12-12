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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes the line of a rule and its expanded form.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ExpandedRuleLine {

	private StringBuilder _line;
	private List <StringBuilder>  _expanded = new ArrayList<StringBuilder>();

	public List<StringBuilder> getExpanded() {
		return _expanded;
	}

	public StringBuilder getLine() {
		return _line;
	}

	public void setLine(StringBuilder line) {
		_line = line;
	}

}
