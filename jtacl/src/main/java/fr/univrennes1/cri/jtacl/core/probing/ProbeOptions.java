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
 * Probe Options
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbeOptions {
	protected int _flags = 0;
	
	protected static final int NoAction = 1;

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
	 * Returns true if the NoAction flag is set.
	 * @return true if the NoAction flag is set.
	 */
	public boolean hasNoAction() {
		return (_flags & NoAction) != 0;
	}

	/**
	 * Sets the NoAction flag
	 */
	public void setNoAction() {
		_flags |= NoAction;
	}

	/**
	 * Clears the NoAction flag
	 */
	public void clearNoAction() {
		_flags = (~NoAction & _flags) & _msk;
	}

	/**
	 * Sets the NoAction flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setNoAction(boolean flag) {
		if (flag)
			setNoAction();
		else
			clearNoAction();
	}

	@Override
	public String toString() {
		String r = "";

		if (hasNoAction())
			r += "NoAction";

		return r;
	}
	
	
}
