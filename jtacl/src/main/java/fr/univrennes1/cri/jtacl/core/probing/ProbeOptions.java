/*
 * Copyright (c) 2011, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.core.probing;

/**
 * Probe Options.<br/>
 * Options to use while probing:
 * <li>NOACTION: Do not make any action on the probe (like routing or packet
 * transformation).</li>
 * <li>QUICKDENY: Stop probing if the probe is certainly denied.</li>
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbeOptions {
	protected int _flags = 0;

	public static final int NOACTION = 1;
	public static final int QUICKDENY = 2;

	protected static final int _msk = 0xFF;

	/**
	 * Constructs a new and empty instance.
	 */
	public ProbeOptions() {
	}

	/**
	 * Constructs a new instance with the flags in argument.
	 * @param flags flags to set.
	 */
	public ProbeOptions(int flags) {
		_flags = flags;
	}

	/**
	 * Constructs a new instance with the flags in argument.
	 * @param flags to set
	 */
	public ProbeOptions(ProbeOptions flags) {
		_flags = flags.getFlags();
	}

	/**
	 * Clears all the flags.
	 */
	public void clearAll() {
		_flags = 0;
	}

	/**
	 * Returns the flags as an integer.
	 * @return the flags as an integer.
	 */
	public int getFlags() {
		return _flags;
	}

	/**
	 * Sets the flags from an integer.
	 * @param flags flags to set.
	 */
	public void setFlags(int flags) {
		_flags = flags;
	}

	/**
	 * Returns true if the NOACTION flag is set.
	 * @return true if the NOACTION flag is set.
	 */
	public boolean hasNoAction() {
		return (_flags & NOACTION) != 0;
	}

	/**
	 * Sets the NOACTION flag.
	 */
	public void setNoAction() {
		_flags |= NOACTION;
	}

	/**
	 * Clears the NOACTION flag.
	 */
	public void clearNoAction() {
		_flags = (~NOACTION & _flags) & _msk;
	}

	/**
	 * Sets the NOACTION flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setNoAction(boolean flag) {
		if (flag)
			setNoAction();
		else
			clearNoAction();
	}

	/**
	 * Returns true if the QUICKDENY flag is set.
	 * @return true if the QUICKDENY flag is set.
	 */
	public boolean hasQuickDeny() {
		return (_flags & QUICKDENY) != 0;
	}

	/**
	 * Sets the QUICKDENY flag.
	 */
	public void setQuickDeny() {
		_flags |= QUICKDENY;
	}

	/**
	 * Clears the QUICKDENY flag.
	 */
	public void clearQuickDeny() {
		_flags = (~QUICKDENY & _flags) & _msk;
	}

	/**
	 * Sets the QUICKDENY flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setQuickDeny(boolean flag) {
		if (flag)
			setQuickDeny();
		else
			clearQuickDeny();
	}

	@Override
	public String toString() {
		String r = "";

		if (hasNoAction())
			r += "NOACTION";

		if (hasQuickDeny()) {
			if (!r.isEmpty())
				r += ", ";
			r += "QUICKDENY";
		}

		return r;
	}


}
