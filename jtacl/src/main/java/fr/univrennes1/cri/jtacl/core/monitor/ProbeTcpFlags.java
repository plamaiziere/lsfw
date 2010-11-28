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

package fr.univrennes1.cri.jtacl.core.monitor;

import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import java.util.ArrayList;

/**
 * TCP flags in a probe request.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbeTcpFlags extends ArrayList<TcpFlags> {

	public boolean matchesAll(String flags) {

		for (TcpFlags f: this) {
			if (f.testFlagsAll(flags))
				return true;
		}
		return false;
	}

	public boolean matchesAny(String flags) {

		for (TcpFlags f: this) {
			if (f.testFlagsAny(flags))
				return true;
		}
		return false;
	}

}
