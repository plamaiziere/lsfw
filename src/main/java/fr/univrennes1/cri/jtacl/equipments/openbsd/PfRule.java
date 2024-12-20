/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.lib.ip.AddressFamily;
import fr.univrennes1.cri.jtacl.lib.ip.TcpFlags;
import fr.univrennes1.cri.jtacl.lib.misc.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a PF filering rule.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfRule extends PfGenericRule {

    /**
     * action pass
     */
    public static final String PASS = "pass";

    /**
     * action match
     */
    public static final String MATCH = "match";

    /**
     * action block
     */
    public static final String BLOCK = "block";

    /**
     * action (pass, match, block)
     */
    protected String _action;

    /**
     * direction
     */
    protected Direction _direction;

    /**
     * quick rule
     */
    protected boolean _quick;

    /**
     * interfaces list of the rule
     */
    protected PfIfListSpec _ifList = new PfIfListSpec();

    /**
     * protocols list
     */
    protected List<Integer> _protocols = new ArrayList<>();

    /**
     * Address family
     */
    protected AddressFamily _af;

    /**
     * ALL IP specification
     */
    protected boolean _all;

    /**
     * from IP specification
     */
    protected PfIpSpec _fromIpSpec = new PfIpSpec();

    /**
     * from port specification
     */
    protected PfPortSpec _fromPortSpec = new PfPortSpec();

    /**
     * to IP specification
     */
    protected PfIpSpec _toIpSpec = new PfIpSpec();

    /**
     * to port specification
     */
    protected PfPortSpec _toPortSpec = new PfPortSpec();

    /**
     * icmp specification
     */
    protected PfIcmpSpec _icmpspec = new PfIcmpSpec();

    /**
     * tcp flags
     */
    protected TcpFlags _flags;

    /**
     * Out-of tcp flags set
     */
    protected TcpFlags _flagset;

    /**
     * filter action (no-state, keep-state...)
     */
    protected String _filterAction;

    /**
     * route options (route-to, dup-to, ...)
     */
    protected PfRouteOpts _routeOpts;

    /**
     * tag option (tag)
     */
    protected PfTagOpt _tagOpt;

    /**
     * Tagged option (tagged)
     */
    protected PfTaggedOpt _taggedOpt;

    /**
     * Returns the action of this rule (pass, match, block).
     *
     * @return the action of this rule.
     */
    public String getAction() {
        return _action;
    }

    /**
     * Sets the action of this rule (pass, match, block).
     *
     * @param action action to set.
     */
    public void setAction(String action) {
        _action = action;
    }

    /**
     * Returns the address family of this rule.
     *
     * @return the address familly of this rule.
     */
    public AddressFamily getAf() {
        return _af;
    }

    /**
     * Sets the address family of this rule.
     *
     * @param af address family to set.
     */
    public void setAf(AddressFamily af) {
        _af = af;
    }

    /**
     * Returns true if the fromTo specification of this rule is "ALL".
     *
     * @return true if the fromTo specification of this rule is "ALL".
     */
    public boolean isAll() {
        return _all;
    }

    /**
     * Sets the fromTo specification of this rule to all if the argument is true.
     *
     * @param all true if the specification is ALL.
     */
    public void setAll(boolean all) {
        _all = all;
    }

    /**
     * Checks if the action is set to "block".
     *
     * @return true if the action is set to "block".
     */
    public boolean isBlock() {
        return _action != null && _action.equals(BLOCK);
    }

    /**
     * Checks if the action is set to "match".
     *
     * @return true if the action is set to "match".
     */
    public boolean isMatch() {
        return _action != null && _action.equals(MATCH);
    }

    /**
     * Checks if the action is set to "pass".
     *
     * @return true if the action is set to "pass".
     */
    public boolean isPass() {
        return _action != null && _action.equals(PASS);
    }

    /**
     * Returns the direction of this rule.
     *
     * @return the direction of this rule.
     */
    public Direction getDirection() {
        return _direction;
    }

    /**
     * Sets the direction of this rule.
     *
     * @param direction direction to set.
     */
    public void setDirection(Direction direction) {
        _direction = direction;
    }

    /**
     * Returns the from ip specification of this rule.
     *
     * @return the from ip specification of this rule.
     */
    public PfIpSpec getFromIpSpec() {
        return _fromIpSpec;
    }

    /**
     * Returns the from port specification of this rule.
     *
     * @return the from port specification of this rule.
     */
    public PfPortSpec getFromPortSpec() {
        return _fromPortSpec;
    }

    /**
     * returns the interfaces list of this rule.
     *
     * @return the interfaces list of this rule.
     */
    public PfIfListSpec getIfList() {
        return _ifList;
    }

    /**
     * returns the protocols  list of this rule.
     *
     * @return the protocols list of this rule.
     */
    public List<Integer> getProtocols() {
        return _protocols;
    }

    /**
     * Retuns true if this rule is a quick rule.
     *
     * @return true if this rule is a quick rule.
     */
    public boolean isQuick() {
        return _quick;
    }

    /**
     * Sets the quick option of this rule.
     *
     * @param quick quick option to set.
     */
    public void setQuick(boolean quick) {
        _quick = quick;
    }

    /**
     * Returns the to ip specification of this rule.
     *
     * @return the to ip specification of this rule.
     */
    public PfIpSpec getToIpSpec() {
        return _toIpSpec;
    }

    /**
     * Returns the to port specification of this rule.
     *
     * @return the to port specification of this rule.
     */
    public PfPortSpec getToPortSpec() {
        return _toPortSpec;
    }

    /**
     * Returns the icmp specification of this rule.
     *
     * @return the icmp specification of this rule.
     */
    public PfIcmpSpec getIcmpspec() {
        return _icmpspec;
    }

    /**
     * Returns the tcp flags of this rule (null = any).
     *
     * @return the tcp flags of this rule.
     */
    public TcpFlags getFlags() {
        return _flags;
    }

    /**
     * Sets the tcp flags of this rule (null = any).
     *
     * @param flags flags to set.
     */
    public void setFlags(TcpFlags flags) {
        _flags = flags;
    }

    /**
     * Returns the out-of tcp flags of this rule (null = none).
     *
     * @return the out-of tcp flags of this rule.
     */
    public TcpFlags getFlagset() {
        return _flagset;
    }

    /**
     * Sets the out-of tcp flags of this rule (null = none).
     *
     * @param flags flags to set.
     */
    public void setFlagset(TcpFlags flags) {
        _flagset = flags;
    }

    /**
     * Returns the filter action of this rule (ex: no-state).
     *
     * @return the filter action of this rule.
     */
    public String getFilterAction() {
        return _filterAction;
    }

    /**
     * Sets the filter action of this rule.
     *
     * @param action action to set.
     */
    public void setFilterAction(String action) {
        _filterAction = action;
    }

    /**
     * Returns the route options of this rule (null = none).
     *
     * @return the route options of this rule.
     */
    public PfRouteOpts getRouteOpts() {
        return _routeOpts;
    }

    /**
     * Sets the route options of this rule (null = none).
     *
     * @param routeOpts route options to set.
     */
    public void SetRouteOpts(PfRouteOpts routeOpts) {
        _routeOpts = routeOpts;
    }

    /**
     * Returns the tag option of this rule (null = none).
     *
     * @return the tag option of this rule (null = none).
     */
    public PfTagOpt getTagOpt() {
        return _tagOpt;
    }

    /**
     * Sets the tag option of this rule (null = none).
     *
     * @param tagOpt tag option to set.
     */
    public void setTagOpt(PfTagOpt tagOpt) {
        _tagOpt = tagOpt;
    }

    /**
     * Returns the tagged option of this rule (null = none).
     *
     * @return the tagged option of this rule (null = none).
     */
    public PfTaggedOpt getTaggedOpt() {
        return _taggedOpt;
    }

    /**
     * Sets the tagged option of this rule (null = none).
     *
     * @param taggedOpt tagged option to set.
     */
    public void setTaggedOpt(PfTaggedOpt taggedOpt) {
        _taggedOpt = taggedOpt;
    }

}
