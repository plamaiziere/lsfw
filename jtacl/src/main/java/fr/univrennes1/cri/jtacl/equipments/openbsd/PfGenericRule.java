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

import fr.univrennes1.cri.jtacl.lib.misc.ParseContext;

/**
 * Generic rule type.
 * 
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public abstract class PfGenericRule {

	/**
	 * the line of configuration corresponding to this rule.
	 */
	protected String _configurationLine;

	/**
	 * the text corresponding to this rule.
	 */
	protected String _text;

	/**
	 * parse context of this rule.
	 */
	protected ParseContext _parseContext;
	
	/**
	 * Owner anchor of this rule
	 */
	protected PfAnchor _ownerAnchor; 
	
	/**
	 * Returns the line of configuration corresponding to this rule.
	 * @return the line of configuration corresponding to this rule.
	 */
	public String getConfigurationLine() {
		return _configurationLine;
	}

	/**
	 * Returns the text of this rule.
	 * @return the text of this rule.
	 */
	public String getText() {
		return _text;
	}

	/**
	 * Sets the text of this rule.
	 * @param text text to set.
	 */
	public void setText(String text) {
		_text = text;
	}

	/**
	 * Sets the line of configuration corresponding to this rule.
	 * @param configurationLine line to set.
	 */
	public void setConfigurationLine(String configurationLine) {
		_configurationLine = configurationLine;
	}

	/**
	 * Returns the parse context of this rule.
	 * @return the parse context of this rule. May be null.
	 */
	public ParseContext getParseContext() {
		return _parseContext;
	}

	/**
	 * Sets the parse context of this rule.
	 * @param parseContext parse context to set.
	 */
	public void setParseContext(ParseContext parseContext) {
		_parseContext = parseContext;
	}

	/**
	 * Returns the owner anchor of this rule.
	 * @return the owner anchor of this rule.
	 */
	public PfAnchor getOwnerAnchor() {
		return _ownerAnchor;
	}

	/**
	 * Sets the owner anchor of this rule.
	 * @param ownerAnchor anchor to set.
	 */
	public void setOwnerAnchor(PfAnchor ownerAnchor) {
		_ownerAnchor = ownerAnchor;
	}

}
