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
 * Template to build anchor rule. This class is used at parsing time
 * as an intermediate storage.
 * @see PfAnchorRule
 * 
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class AnchorTemplate {

	private String _name;
	private RuleTemplate _rule;
	private boolean _inlined;

	public boolean isInlined() {
		return _inlined;
	}

	public void setInlined(boolean inlined) {
		_inlined = inlined;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public RuleTemplate getRule() {
		return _rule;
	}

	public void setRule(RuleTemplate rule) {
		_rule = rule;
	}


}
