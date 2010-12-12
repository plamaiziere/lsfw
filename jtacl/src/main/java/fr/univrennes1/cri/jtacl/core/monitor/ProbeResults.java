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

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;

/**
 * This class is responsible to track all events and results associated to one
 * {@link Probe} probe.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbeResults {

	// a list of events
	protected ArrayList<String> _strEvents;

	// the list of matching ACL in input.
	protected ArrayList<AccessControlList> _matchingAclsIn;

	// the list of matching ACL in output.
	protected ArrayList<AccessControlList> _matchingAclsOut;

	// the list of active ACL in input: acl which has accepted or denied the probe.
	protected ArrayList<AccessControlList> _activesAclsIn;

	// the list of active ACL in output: acl which has accepted or denied the probe.
	protected ArrayList<AccessControlList> _activesAclsOut;

	// the global result in input (accept, deny)
	protected AclResult _resultIn;

	// the global result in output (accept, deny)
	protected AclResult _resultOut;

	// the routing result
	protected RoutingResult _routingResult;

	// informational message associated to the routing result
	protected String _routingMessage;

	// informational name of the input interface.
	protected String _interfaceIn;

	// informational name of the output interface.
	protected String _interfaceOut;

	public ProbeResults() {
		_strEvents = new ArrayList<String>();
		_matchingAclsIn = new ArrayList<AccessControlList>();
		_matchingAclsOut = new ArrayList<AccessControlList>();
		_activesAclsIn = new ArrayList<AccessControlList>();
		_activesAclsOut = new ArrayList<AccessControlList>();
		_interfaceIn = "";
		_interfaceOut = "";

		_resultIn = AclResult.ACCEPT;
		_resultOut = AclResult.ACCEPT;
		_routingResult = RoutingResult.UNKNOWN;
		_routingMessage = "";
	}

	public void addEvent(String message) {
		_strEvents.add(message);
	}

	public void addMatchingAclIn(String aclString, AclResult result) {
		AccessControlList acl = new AccessControlList(aclString, result);
		_matchingAclsIn.add(acl);
	}

	public void addMatchingAclOut(String aclString, AclResult result) {
		AccessControlList acl = new AccessControlList(aclString, result);
		_matchingAclsOut.add(acl);
	}

	public void addMatchingAcl(Direction direction, String aclString, AclResult result) {
		if (direction == Direction.IN)
			addMatchingAclIn(aclString, result);
		else
			addMatchingAclOut(aclString, result);
	}

	public void addActiveAclIn(String aclString, AclResult result) {
		AccessControlList acl = new AccessControlList(aclString, result);
		_activesAclsIn.add(acl);
	}

	public void addActiveAclOut(String aclString, AclResult result) {
		AccessControlList acl = new AccessControlList(aclString, result);
		_activesAclsOut.add(acl);
	}

	public void addActiveAcl(Direction direction, String aclString, AclResult result) {
		if (direction == Direction.IN)
			addActiveAclIn(aclString, result);
		else
			addActiveAclOut(aclString, result);
	}

	public void setAclResultIn(AclResult result) {
		_resultIn = result;
	}

	public void setAclResultOut(AclResult result) {
		_resultOut = result;
	}

	public void setAclResult(Direction direction, AclResult result) {
		if (direction == Direction.IN)
			setAclResultIn(result);
		else
			setAclResultOut(result);
	}

	public void setRoutingResult(RoutingResult result, String message) {
		_routingResult = result;
		_routingMessage = message;
	}

	public AclResult getAclResultOut() {
		return _resultOut;
	}

	public ArrayList<String> getStrEvents() {
		return _strEvents;
	}

	public ArrayList<AccessControlList> getActivesAclsIn() {
		return _activesAclsIn;
	}

	public ArrayList<AccessControlList> getActivesAclsOut() {
		return _activesAclsOut;
	}

	public ArrayList<AccessControlList> getMatchingAclsIn() {
		return _matchingAclsIn;
	}

	public ArrayList<AccessControlList> getMatchingAclsOut() {
		return _matchingAclsOut;
	}

	public AclResult getAclResult() {
	
		if (_resultIn == AclResult.ACCEPT && _resultOut == AclResult.ACCEPT)
			return AclResult.ACCEPT;

		if (_resultIn != AclResult.MAY && _resultOut != AclResult.MAY)
			return AclResult.DENY;

		return AclResult.MAY;
	}

	public RoutingResult getRoutingResult() {
		return _routingResult;
	}

	public String getRoutingMessage() {
		return _routingMessage;
	}

	public String getInterfaceIn() {
		return _interfaceIn;
	}

	public void setInterfaceIn(String interfaceIn) {
		_interfaceIn = interfaceIn;
	}

	public String getInterfaceOut() {
		return _interfaceOut;
	}

	public void setInterfaceOut(String interfaceOut) {
		_interfaceOut = interfaceOut;
	}

	public void setInterface(Direction direction, String interfaceText) {
		if (direction == Direction.IN)
			setInterfaceIn(interfaceText);
		else
			setInterfaceOut(interfaceText);
	}

	public ProbeResults newInstance() {
		ProbeResults pr = new ProbeResults();
		pr._activesAclsIn.addAll(_activesAclsIn);
		pr._activesAclsOut.addAll(_activesAclsOut);
		pr._matchingAclsIn.addAll(_matchingAclsIn);
		pr._matchingAclsOut.addAll(_matchingAclsOut);
		pr._resultIn = _resultIn;
		pr._resultOut = _resultOut;
		pr._routingResult = _routingResult;
		pr._interfaceIn = _interfaceIn;
		pr._interfaceOut = _interfaceOut;
		return pr;
	}

	public String showAclResults(boolean verbose) {
		CharArrayWriter swriter = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(swriter);

		if (verbose || ! _matchingAclsIn.isEmpty()) {
			writer.println("Matching ACL on input: " + _interfaceIn);
			for (AccessControlList acl: _matchingAclsIn)
				writer.println("  " + acl.toString());

			if (verbose) {
				writer.println("Active ACL on input: " + _interfaceIn);
				for (AccessControlList acl: _activesAclsIn)
					writer.println("  " + acl.toString());
			}
		}
		if (verbose || !_matchingAclsOut.isEmpty()) {
			writer.println("Matching ACL on output: " + _interfaceOut);
			for (AccessControlList acl: _matchingAclsOut)
				writer.println("  " + acl.toString());

			if (verbose) {
				writer.println("Active ACL on output: " + _interfaceOut);
				for (AccessControlList acl: _activesAclsOut)
					writer.println("  " + acl.toString());
			}
		}
		writer.flush();
		return swriter.toString();
	}

}
