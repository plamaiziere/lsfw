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
 *  Template to build scrub options. This class is used at parsing time
 * as an intermediate storage.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ScrubOptsTemplate {

	private boolean _nodf;
	private String _minttl;
	private String _maxmss;
	private String _settos;
	private boolean _randomid;
	private boolean _reassemble_tcp;

	public String getMaxmss() {
		return _maxmss;
	}

	public void setMaxmss(String maxmss) {
		_maxmss = maxmss;
	}

	public String getMinttl() {
		return _minttl;
	}

	public void setMinttl(String minttl) {
		_minttl = minttl;
	}

	public boolean isNodf() {
		return _nodf;
	}

	public void setNodf(boolean nodf) {
		_nodf = nodf;
	}

	public boolean isRandomid() {
		return _randomid;
	}

	public void setRandomid(boolean randomid) {
		_randomid = randomid;
	}

	public boolean isReassemble_tcp() {
		return _reassemble_tcp;
	}

	public void setReassemble_tcp(boolean reassemble_tcp) {
		_reassemble_tcp = reassemble_tcp;
	}

	public String getSettos() {
		return _settos;
	}

	public void setSettos(String settos) {
		_settos = settos;
	}
}
