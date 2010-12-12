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

}
