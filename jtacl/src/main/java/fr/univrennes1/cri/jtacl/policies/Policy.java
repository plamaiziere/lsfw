/*
 * Copyright (c) 2013, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.policies;

/**
 * Base class for security policy
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class Policy {
	String _name;
	String _comment;
	String _from;
	String _to;

	public Policy(String name, String comment) {
		_name = name;
		_comment = comment;
	}

	public String getName() {
		return _name;
	}

	public String getComment() {
		return _comment;
	}

	public String getFrom() {
		return _from;
	}

	public void setFrom(String from) {
		_from = from;
	}

	public String getTo() {
		return _to;
	}

	public void setTo(String to) {
		_to = to;
	}

}
