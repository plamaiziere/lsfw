/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.core.probing;

import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import java.util.ArrayList;

/**
 * TCP flags in a probe request.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbeTcpFlags extends ArrayList<TcpFlags> {

	/**
	 * Returns true if at least one tcp flags in the probe matches all the flags
	 * in argument
	 * @param flags flags to check.
	 * @return true if at least one tcp flags in the probe matches all the flags
	 * in argument
	 */
	public boolean matchAll(String flags) {

		for (TcpFlags f: this) {
			if (f.testFlagsAll(flags))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if at least one tcp flags in the probe matches all the flags
	 * in argument
	 * @param flags flags to check.
	 * @return true if at least one tcp flags in the probe matches all the flags
	 * in argument
	 */
	public boolean matchAll(TcpFlags flags) {

		for (TcpFlags f: this) {
			if (f.testFlagsAll(flags))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if at least one tcp flags in the probe matches all the flags
	 * in argument, without matching flagset.
	 * @param flags flags to check.
	 * @param flagset flagset to not match.
	 * @return true true if at least one tcp flags in the probe matches all the flags
	 * in argument, without matching flagset.
	 */
	public boolean matchAllWithout(TcpFlags flags, TcpFlags flagset) {

		for (TcpFlags f: this) {
			if (f.testFlagsAll(flags) && !f.testFlagsAll(flagset))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if at least one tcp flags in the probe matches any of the flags
	 * in argument
	 * @param flags flags to check.
	 * @return true if at least one tcp flags in the probe matches any of the flags
	 * in argument
	 */
	public boolean matchAny(String flags) {

		for (TcpFlags f: this) {
			if (f.testFlagsAny(flags))
				return true;
		}
		return false;
	}

}
