/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * Describes a PF anchor rule.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfAnchorRule extends PfGenericRule {

	/**
	 * anchor name of the anchor associated to this rule.
	 * (ie the anchorname used in the rule using path and modifier)
	 */
	protected String _anchorName;

	/**
	 * filtering rule associated to this rule.
	 */
	protected PfRule _pfRule;

	/**
	 * inlined anchor
	 */
	protected boolean _inlined;

	/**
	 * Pointer to the associated anchor is the rule
	 * describes an inlined anchor.
	 */
	protected PfAnchor _inlinedAnchor;

	/**
	 * Returns true if this anchor rule is an inlined anchor.
	 * @return  true if this anchor rule is an inlined anchor.
	 */
	public boolean isInlined() {
		return _inlined;
	}

	/**
	 * Returns the associated anchor if this anchor rule is an inlined anchor.
	 * @return the associated anchor if this anchor rule is an inlined anchor.
	 */
	public PfAnchor getInlinedAnchor() {
		return _inlinedAnchor;
	}

	/**
	 * Sets the associated anchor if this anchor rule is an inlined anchor.
	 * @param anchor anchor to associate.
	 */
	public void setInlinedAnchor(PfAnchor anchor) {
		_inlinedAnchor = anchor;
	}

	/**
	 * Sets the inlined flag of this rule.
	 * @param inlined flag to set.
	 */
	public void setInlined(boolean inlined) {
		_inlined = inlined;
	}

	/**
	 * Returns the anchor name associated to this rule
	 * ie the anchorname used in the rule using path and modifier,
	 * may be null for inlined anchor).
	 *
	 * @return the anchor name associated to this rule
	 * (may be null for inlined anchor).
	 */
	public String getAnchorName() {
		return _anchorName;
	}

	/**
	 * Sets the name of the anchor associated to this rule.
	 * ie the anchorname used in the rule using path and modifier,
	 * may be null for inlined anchor).
	 * @param anchorName name to set.
	 */
	public void setAnchorName(String anchorName) {
		_anchorName = anchorName;
	}

	/**
	 * Returns the filtering rule of this anchor rule.
	 * @return the filtering rule of this anchor rule.
	 */
	public PfRule getPfRule() {
		return _pfRule;
	}

	/**
	 * Sets the filtering rule of this anchor rule.
	 * @param pfRule the filtering rule to set.
	 */
	public void setPfRule(PfRule pfRule) {
		_pfRule = pfRule;
	}

}
