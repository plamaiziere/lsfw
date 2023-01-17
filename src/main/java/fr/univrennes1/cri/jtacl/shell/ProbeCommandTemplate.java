/*
 * Copyright (c) 2012 - 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.shell;

import fr.univrennes1.cri.jtacl.lib.misc.StringsList;

/**
 * Template for the probe command.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbeCommandTemplate {

	protected String _srcAddress;
	protected String _destAddress;
	protected String _equipments;
	protected String _protoSpecification;
	protected String _portSource;
	protected String _portDest;

	protected String _probeExpect;
	protected boolean _probe6flag;
	protected boolean _probeOptActive;
	protected boolean _probeOptMatching;
	protected boolean _probeOptNoAction;
	protected boolean _probeOptVerbose;
	protected boolean _probeOptLearn;
	protected boolean _probeOptQuickDeny;
	protected boolean _probeOptState;
	protected StringsList _tcpFlags;

	public String getDestAddress() {
		return _destAddress;
	}

	public boolean setDestAddress(String destAddress) {
		_destAddress = destAddress;
		return true;
	}

	public String getSrcAddress() {
		return _srcAddress;
	}

	public boolean setSrcAddress(String srcAddress) {
		_srcAddress = srcAddress;
		return true;
	}

	public String getEquipments() {
		return _equipments;
	}

	public boolean setEquipments(String equipments) {
		_equipments = equipments;
		return true;
	}

	public String getProtoSpecification() {
		return _protoSpecification;
	}

	public boolean setProtoSpecification(String protoSpecification) {
		_protoSpecification = protoSpecification;
		return true;
	}

	public String getPortDest() {
		return _portDest;
	}

	public boolean setPortDest(String portDest) {
		_portDest = portDest;
		return true;
	}

	public String getPortSource() {
		return _portSource;
	}

	public boolean setPortSource(String portSource) {
		_portSource = portSource;
		return true;
	}

	public String getProbeExpect() {
		return _probeExpect;
	}

	public boolean setProbeExpect(String probeExpect) {
		_probeExpect = probeExpect;
		return true;
	}

	public StringsList getTcpFlags() {
		return _tcpFlags;
	}

	public boolean setTcpFlags(StringsList tcpFlags) {
		_tcpFlags = tcpFlags;
		return true;
	}

	public boolean addTcpFlag(String tcpFlag) {
		return _tcpFlags.add(tcpFlag);
	}

	public boolean getProbe6flag() {
		return _probe6flag;
	}

	public boolean setProbe6flag(boolean probe6flag) {
		_probe6flag = probe6flag;
		return true;
	}

	public boolean setProbeOptNoAction(boolean probeOptNoAction) {
		_probeOptNoAction = probeOptNoAction;
		return true;
	}

	public boolean getProbeOptNoAction() {
		return _probeOptNoAction;
	}

	public boolean setProbeOptVerbose(boolean probeOptVerbose) {
		_probeOptVerbose = probeOptVerbose;
		return true;
	}

	public boolean getProbeOptVerbose() {
		return _probeOptVerbose;
	}

	public boolean getProbeOptActive() {
		return _probeOptActive;
	}

	public boolean setProbeOptActive(boolean probeOptActive) {
		_probeOptActive = probeOptActive;
		return true;
	}

	public boolean getProbeOptMatching() {
		return _probeOptMatching;
	}

	public boolean setProbeOptMatching(boolean probeOptMatching) {
		_probeOptMatching = probeOptMatching;
		return true;
	}

	public boolean getProbeOptLearn() {
		return _probeOptLearn;
	}

	public boolean setProbeOptLearn(boolean probeOptLearn) {
		_probeOptLearn = probeOptLearn;
		return true;
	}

	public boolean getProbeOptQuickDeny() {
		return _probeOptQuickDeny;
	}

	public boolean setProbeOptQuickDeny(boolean probeOptQuickDeny) {
		_probeOptQuickDeny = probeOptQuickDeny;
		return true;
	}

	public boolean getProbeOptState() {
		return _probeOptState;
	}
	
	public boolean setProbeOptState(boolean probeOptState) {
		_probeOptState = probeOptState;
		return true;
	}

}
