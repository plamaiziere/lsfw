/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.core.topology;

import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.IfaceLinks;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;

/**
 * A TopologicalLink is a hint given by configuration to help connecting
 * equipments to each others.<br/>
 * It connects {@link IfaceLinks} links of the equipments via the IP address.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class TopologicalLink {

	protected IfaceLinks _links;
	protected boolean _borderLink;
	protected IPNet _network;

	public TopologicalLink(boolean borderLink, IPNet network) {
		_links = new IfaceLinks();
		_borderLink = borderLink;
		_network = network;
	}

	@Override
	public String toString() {
		String s = "";
		for (IfaceLink link: _links) {
			s += link.toString() + ", ";
		}
		return s;
	}

	public IfaceLinks getLinks() {
		return _links;
	}

	public IPNet getNetwork() {
		return _network;
	}

	public boolean isBorderLink() {
		return _borderLink;
	}
}
