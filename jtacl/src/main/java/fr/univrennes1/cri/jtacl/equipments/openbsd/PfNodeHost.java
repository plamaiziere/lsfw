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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.lib.ip.AddressFamily;
import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * PF Host.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfNodeHost {

	/**
	 * address family.
	 */
	protected AddressFamily _af;

	/**
	 * not (!) host.
	 */
	protected boolean _not;

	/**
	 * type of this host.
	 */
	protected PfAddrType _type;

	/**
	 * list of IP addresses.
	 */
	protected List<IPNet> _addr;

	/**
	 * List of IP addresses, for range.
	 */
	protected List<IPNet> _rangeAddr;

	/**
	 * table associated to this host.
	 */
	protected String _tblName;

	/**
	 * route label associated to this host.
	 */
	protected String _rtlabelName;


	/**
	 * Creates a new PfNodeHost of type PF_ADDR_ANY.
	 * @return a new PfNodeHost of type PF_ADDR_ANY.
	 */
	public static PfNodeHost newAddrAny() {
		PfNodeHost host = new PfNodeHost();
		host._type = PfAddrType.PF_ADDR_ANY;
		return host;
	}

	/**
	 * Creates a new PfNodeHost of type PF_ADDR_ADDRMASK.
	 * @return a new PfNodeHost of type PF_ADDR_ADDRMASK.
	 */
	public static PfNodeHost newAddrMask() {
		PfNodeHost host = new PfNodeHost();
		host._type = PfAddrType.PF_ADDR_ADDRMASK;
		host._addr = new ArrayList<IPNet>();
		return host;
	}

	/**
	 * Creates a new PfNodeHost of type PF_ADDR_NOROUTE.
	 * @return a new PfNodeHost of type PF_ADDR_NOROUTE.
	 */
	public static PfNodeHost newAddrNoRoute() {
		PfNodeHost host = new PfNodeHost();
		host._type = PfAddrType.PF_ADDR_NOROUTE;
		return host;
	}

	/**
	 * Creates a new PfNodeHost of type PF_ADDR_TABLE.
	 * @param tableName the name of the table associated to this node.
	 * @return a new PfNodeHost of type PF_ADDR_TABLE.
	 */
	public static PfNodeHost newAddrTable(String tableName) {
		PfNodeHost host = new PfNodeHost();
		host._type = PfAddrType.PF_ADDR_TABLE;
		host._tblName = tableName;
		return host;
	}

	/**
	 * Creates a new PfNodeHost of type PF_ADDR_RTLABEL.
	 * @param routeLabel the name of the route label associated to this node.
	 * @return a new PfNodeHost of type PF_ADDR_RTLABEL.
	 */
	public static PfNodeHost newAddrRtLabel(String routeLabel) {
		PfNodeHost host = new PfNodeHost();
		host._type = PfAddrType.PF_ADDR_RTLABEL;
		host._rtlabelName = routeLabel;
		return host;
	}

	/**
	 * Creates a new PfNodeHost of type PF_ADDR_URPFFAILED.
	 * @return a new PfNodeHost of type PF_ADDR_URPFFAILED.
	 */
	public static PfNodeHost newAddrUrpfFailed() {
		PfNodeHost host = new PfNodeHost();
		host._type = PfAddrType.PF_ADDR_URPFFAILED;
		return host;
	}

	/**
	 * Creates a new PfNodeHost of type PF_ADDR_RANGE.
	 * @return a new PfNodeHost of type PF_ADDR_RANGE.
	 */
	public static PfNodeHost newAddrRange() {
		PfNodeHost host = new PfNodeHost();
		host._addr = new ArrayList<IPNet>();
		host._rangeAddr = new ArrayList<IPNet>();
		host._type = PfAddrType.PF_ADDR_RANGE;
		return host;
	}

	/**
	 * Add and IP address.
	 * @param addr IP address to add.
	 * @throws JtaclInternalException if the type of this instance is not an
	 * address or a range.
	 */
	public void addAddr(IPNet addr) {
		if (!isAddrMask() && !isAddrRange())
			throw new JtaclInternalException("invalid address type");
		_addr.add(addr);
	}

	/**
	 * Add a collection of IP address.
	 * @param addresses IP addresses to add.
	 * @throws JtaclInternalException if the type of this instance is not an
	 * address or a range.
	 */
	public void addAddr(Collection<IPNet> addresses) {
		if (!isAddrMask() && !isAddrRange())
			throw new JtaclInternalException("invalid address type");
		_addr.addAll(addresses);
	}

	/**
	 * Add and IP address into the range addresses.
	 * @param addr IP address to add.
	 * @throws JtaclInternalException if the type of this instance is not a range.
	 */
	public void addAddrRange(IPNet addr) {
		if (!isAddrRange())
			throw new JtaclInternalException("invalid address type");
		_rangeAddr.add(addr);
	}

	/**
	 * Add a collection of IP address into the range addresses.
	 * @param addresses IP addresses to add.
	 * @throws JtaclInternalException if the type of this instance is not a range.
	 */
	public void addAddrRange(Collection<IPNet> addresses) {
		if (!isAddrRange())
			throw new JtaclInternalException("invalid address type");
		_rangeAddr.addAll(addresses);
	}

	/**
	 * Retuns the address family.
	 * @return the address family.
	 */
	public AddressFamily getAf() {
		return _af;
	}

	/**
	 * Sets the address family.
	 * @param af address family to set.
	 */
	public void setAf(AddressFamily af) {
		_af = af;
	}

	/**
	 * Returns true if the not (!) flag is this set on this host.
	 * @return true if the not (!) flag is this set on this host.
	 */
	public boolean isNot() {
		return _not;
	}

	/**
	 * Sets the not (!) flag on this host.
	 * @param not flag to set.
	 */
	public void setNot(boolean not) {
		_not = not;
	}

	/**
	 * Returns true if this PfNodeHost instance is of type PF_ADDR_ANY.
	 * @return true if this PfNodeHost instance is of type PF_ADDR_ANY.
	 */
	public boolean isAddrAny() {
		return _type == PfAddrType.PF_ADDR_ANY;
	}

	/**
	 * Returns true if this PfNodeHost instance is of type PF_ADDR_ADDRMASK.
	 * @return true if this PfNodeHost instance is of type PF_ADDR_ADDRMASK.
	 */
	public boolean isAddrMask() {
		return _type == PfAddrType.PF_ADDR_ADDRMASK;
	}

	/**
	 * Returns true if this PfNodeHost instance is of type PF_ADDR_NOROUTE.
	 * @return true if this PfNodeHost instance is of type PF_ADDR_NOROUTE.
	 */
	public boolean isAddrNoRoute() {
		return _type == PfAddrType.PF_ADDR_NOROUTE;
	}

	/**
	 * Returns true if this PfNodeHost instance is of type PF_ADDR_DYNIFTL.
	 * @return true if this PfNodeHost instance is of type PF_ADDR_DYNIFTL.
	 */
	public boolean isAddrDynIftl() {
		return _type == PfAddrType.PF_ADDR_DYNIFTL;
	}

	/**
	 * Returns true if this PfNodeHost instance is of type PF_ADDR_TABLE.
	 * @return true if this PfNodeHost instance is of type PF_ADDR_TABLE.
	 */
	public boolean isAddrTable() {
		return _type == PfAddrType.PF_ADDR_TABLE;
	}

	/**
	 * Returns true if this PfNodeHost instance is of type PF_ADDR_RTLABEL.
	 * @return true if this PfNodeHost instance is of type PF_ADDR_RTLABEL.
	 */
	public boolean isAddrRtLabel() {
		return _type == PfAddrType.PF_ADDR_RTLABEL;
	}

	/**
	 * Returns true if this PfNodeHost instance is of type PF_ADDR_URPFFAILED.
	 * @return true if this PfNodeHost instance is of type PF_ADDR_URPFFAILED.
	 */
	public boolean isAddrUrpfFailed() {
		return _type == PfAddrType.PF_ADDR_URPFFAILED;
	}

	/**
	 * Returns true if this PfNodeHost instance is of type PF_ADDR_RANGE.
	 * @return true if this PfNodeHost instance is of type PF_ADDR_RANGE.
	 */
	public boolean isAddrRange() {
		return _type == PfAddrType.PF_ADDR_RANGE;
	}

	/**
	 * Returns true if this PfNodeHost instance is of type PF_ADDR_NONE.
	 * @return true if this PfNodeHost instance is of type PF_ADDR_NONE.
	 */
	public boolean isAddrNone() {
		return _type == PfAddrType.PF_ADDR_NONE;
	}

	/**
	 * Returns the list of IP addresses of this instance.
	 * @return the list of IP addresses of this instance.
	 * @throws JtaclInternalException if the type of this host is not an
	 * address or a range.
	 */
	public List<IPNet> getAddr() {
		if (!isAddrMask() && !isAddrRange())
			throw new JtaclInternalException("invalid address type");
		return _addr;
	}

	/**
	 * Returns the list of range IP addresses of this instance.
	 * @return the list of range IP addresses of this instance.
	 * @throws JtaclInternalException if the type of this host is not a range.
	 */
	public List<IPNet> getRangeAddr() {
		if (!isAddrRange())
			throw new JtaclInternalException("invalid address type");
		return _rangeAddr;
	}

	/**
	 * Returns the route label associated to this host.
	 * @return the route label associated to this host.
	 * @throws JtaclInternalException if the the type of this host is not
	 * a route label.
	 */
	public String getRtlabelName() {
		if (!isAddrRtLabel())
			throw new JtaclInternalException("invalid address type");
		return _rtlabelName;
	}

	/**
	 * Returns the name of the table associated to this host.
	 * @return the name of the table associated to this host.
	 * @throws JtaclInternalException if the type of this host is not a table.
	 */
	public String getTblName() {
		if (!isAddrTable())
			throw new JtaclInternalException("invalid address type");
		return _tblName;
	}

	/**
	 * Returns the type of this host.
	 * @return the type of this host.
	 */
	public PfAddrType getType() {
		return _type;
	}

	@Override
	public String toString() {

		String s = _type.toString() + ":";
		if (_not)
			s = s + " ! ";

		if (isAddrAny())
			return s + "ANY";

		if (isAddrMask() || isAddrRange()) {
			for (IPNet ip: _addr) {
				s = s + ip.toString("::i") + ", ";
			}
			if (!isAddrRange())
				return s;
		}
		if (isAddrRange()) {
			s = s + " - ";
			for (IPNet ip: _rangeAddr) {
				s = s + ip.toString("::i") + ", ";
			}
			return s;
		}
		if (isAddrRtLabel())
			return s + _rtlabelName;

		if (isAddrTable())
			return s + _tblName;

		return s;
	}

}
