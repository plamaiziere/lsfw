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

package fr.univrennes1.cri.jtacl.core.monitor;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;

/**
 * Describes the position of a probe.<br/>
 * The position is defined by the {@link IfaceLink} link of the incoming
 * interface (incoming), the {@link IfaceLink} link of the outgoing interface (outgoing)
 * of the probe and the {@link IPNet} IP address of the nexthop.<br/>
 * The two IfaceLink must refered to the same {@link NetworkEquipment} equipment
 * and must be set in the order: 'incoming, outgoing, nexthop'.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbePosition {

	private IfaceLink _incoming;
	private IfaceLink _outgoing;
	private IPNet _nextHop;

	/**
	 * Returns the incoming {@link IfaceLink} link on this position.
	 * @return the incoming {@link IfaceLink} link on this position.
	 */
	public IfaceLink getIncoming() {
		return _incoming;
	}

	/**
	 * Sets the incoming {@link IfaceLink} link on this position. This method
	 * must not be called after the ougoing link was seted.
	 * @param link the incoming {@link IfaceLink} link to associate with this
	 * position.
	 */
	public void setIncoming(IfaceLink link) {
		if (_outgoing != null)
			throw new JtaclInternalException("The 'incoming' IfaceLink must be set before the 'outgoing' IfaceLink");
		_incoming = link;
	}

	/**
	 * Returns the {@link IPNet} nexthop IP address on this position.
	 * @return the {@link IPNet} nexthop IP address on this position.
	 */
	public IPNet getNextHop() {
		return _nextHop;
	}

	/**
	 * Sets the {@link IPNet} nexthop IP address on this position.
	 * @param nextHop the {@link IPNet} nexthop IP address to associate on this
	 * position.
	 */
	public void setNextHop(IPNet nextHop) {
		_nextHop = nextHop;
	}

	/**
	 * Returns the outgoing {@link IfaceLink} link on this position.
	 * @return the outgoing {@link IfaceLink} link on this position.
	 */
	public IfaceLink getOutgoing() {
		return _outgoing;
	}

	/**
	 * Sets the outgoing {@link IfaceLink} link and the {@link IPNet} nexthop
	 * IP address on this position. This method must not be called after the
	 * ougoing link was seted.
	 * @param link the outgoing {@link IfaceLink} link to associate with this
	 * position.
	 */
	public void setOutgoing(IfaceLink link, IPNet nextHop) {
		if (_incoming == null)
			throw new JtaclInternalException("The 'outgoing' IfaceLink must" +
					" be set after the 'incoming' IfaceLink");

		_outgoing = link;
		_nextHop = nextHop;

		if (!_incoming.getIface().getEquipment().equals(_outgoing.getIface().getEquipment()))
			throw new JtaclInternalException("The 'incoming' and 'outgoing'" +
					" IfaceLink must refered to the same equipment");
	}

	/**
	 * Creates and returns a new copy of this instance.
	 * @return a new copy of this instance.
	 */
	public ProbePosition newInstance() {
		ProbePosition pos = new ProbePosition();
		pos._incoming = _incoming;
		pos._outgoing = _outgoing;
		return pos;
	}

	@Override
	public String toString() {
		String str = "";

		if (_incoming != null) {
			str += _incoming.getIface().getEquipment().getName() + "(";
			str += _incoming.getIface().getName() + "-";
		} else
			return "none";

		if (_outgoing != null) {
			str += _outgoing.getIface().getName();
			str += "[" +_nextHop.toString("i") + "]";
		} else
			str += "none";
		str += ")";
		return str;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ProbePosition other = (ProbePosition) obj;
		if (_incoming != other._incoming &&
				(_incoming == null || !_incoming.equals(other._incoming))) {
			return false;
		}
		if (_outgoing != other._outgoing &&
				(_outgoing == null || !_outgoing.equals(other._outgoing))) {
			return false;
		}
		if (_nextHop != other._nextHop &&
				(_nextHop == null || !_nextHop.equals(other._nextHop))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 37 * hash + (_incoming != null ? _incoming.hashCode() : 0);
		hash = 37 * hash + (_outgoing != null ? _outgoing.hashCode() : 0);
		hash = 37 * hash + (_nextHop != null ? _nextHop.hashCode() : 0);
		return hash;
	}

}
