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

package fr.univrennes1.cri.jtacl.core.probing;

import fr.univrennes1.cri.jtacl.lib.misc.Direction;
import java.util.ArrayList;

/**
 * This class is responsible to track all results associated to one
 * {@link Probe} probe.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ProbeResults {

	// the list of matching ACL in input.
	protected ArrayList<AccessControlList> _matchingAclsIn;

	// the list of matching ACL in output.
	protected ArrayList<AccessControlList> _matchingAclsOut;

	// the list of active ACL in input: acl which has accepted or denied the probe.
	protected ArrayList<AccessControlList> _activesAclsIn;

	// the list of active ACL in output: acl which has accepted or denied the probe.
	protected ArrayList<AccessControlList> _activesAclsOut;

	// the global result in input (accept, deny)
	protected FwResult _resultIn;

	// the global result in output (accept, deny)
	protected FwResult _resultOut;

	// the routing result
	protected RoutingResult _routingResult;

	// informational message associated to the routing result
	protected String _routingMessage;

	// informational name of the input interface.
	protected String _interfaceIn;

	// informational name of the output interface.
	protected String _interfaceOut;

	public ProbeResults() {
		_matchingAclsIn = new ArrayList<>();
		_matchingAclsOut = new ArrayList<>();
		_activesAclsIn = new ArrayList<>();
		_activesAclsOut = new ArrayList<>();
		_interfaceIn = "";
		_interfaceOut = "";

		_resultIn = new FwResult(FwResult.ACCEPT);
		_resultOut = new FwResult(FwResult.ACCEPT);
		_routingResult = RoutingResult.UNKNOWN;
		_routingMessage = "";
	}

	public void addMatchingAclIn(String aclString, FwResult result) {
		AccessControlList acl = new AccessControlList(aclString, result);
		_matchingAclsIn.add(acl);
	}

	public void addMatchingAclOut(String aclString, FwResult result) {
		AccessControlList acl = new AccessControlList(aclString, result);
		_matchingAclsOut.add(acl);
	}

	public void addMatchingAcl(Direction direction, String aclString, FwResult result) {
		if (direction == Direction.IN)
			addMatchingAclIn(aclString, result);
		else
			addMatchingAclOut(aclString, result);
	}

	public void addActiveAclIn(String aclString, FwResult result) {
		AccessControlList acl = new AccessControlList(aclString, result);
		_activesAclsIn.add(acl);
	}

	public void addActiveAclOut(String aclString, FwResult result) {
		AccessControlList acl = new AccessControlList(aclString, result);
		_activesAclsOut.add(acl);
	}

	public void addActiveAcl(Direction direction, String aclString, FwResult result) {
		if (direction == Direction.IN)
			addActiveAclIn(aclString, result);
		else
			addActiveAclOut(aclString, result);
	}

    public ArrayList<AccessControlList> getActiveAcl(Direction direction) {
        if (direction == Direction.IN)
            return getActivesAclsIn();
        else
            return getActivesAclsOut();
    }

	public FwResult getAclResultIn() {
		return _resultIn;
	}

	public void setAclResultIn(FwResult result) {
		_resultIn = result;
	}

	public void setAclResultOut(FwResult result) {
		_resultOut = result;
	}

	public void setAclResult(Direction direction, FwResult result) {
		if (direction == Direction.IN)
			setAclResultIn(result);
		else
			setAclResultOut(result);
	}

	public void setRoutingResult(RoutingResult result, String message) {
		_routingResult = result;
		_routingMessage = message;
	}

	public FwResult getAclResultOut() {
		return _resultOut;
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

	public FwResult getAclResult() {
		return _resultIn.concat(_resultOut);
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

}
