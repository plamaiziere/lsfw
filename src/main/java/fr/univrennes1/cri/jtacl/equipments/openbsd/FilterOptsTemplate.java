/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import java.util.ArrayList;
import java.util.List;

/**
 * Template to build PF rule filter options. This class is used at parsing time
 * as an intermediate storage.
 * @see PfRule
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class FilterOptsTemplate {
	
	private String _action;
	private String _rcv;
	private String _flags;
	private String _flagset;
	private List<IcmpItem> _icmpspec = new ArrayList<>();
	private List<IcmpItem> _icmp6spec = new ArrayList<>();
	private boolean _fragment;
	private boolean _allowopts;
	private String _tag;
	private String _matchTag;
	private boolean _matchTagNot;
	private String _tos;
	private StringsList _options = new StringsList();
	private String _label;
	private String _qname;
	private String _pqname;
	private String _probability;
	private String _rtableId;
	private String _divertAddr;
	private String _divertPort;
	private String _divertPacketPort;
	private ScrubOptsTemplate _scrubOpts;
	private RedirSpecTemplate _nat;
	private boolean _binat;
	private RedirSpecTemplate _rdr;
	private RedirSpecTemplate _rroute;
	private RouteOptsTemplate _routeOpts;

	/* scrub opts */
	private int _nodf;
	private int	_minttl;
	private int	_settos;
	private int	_randomid;
	private int	_max_mss;

	public String getAction() {
		return _action;
	}

	public boolean setAction(String action) {
		_action = action;
		return true;
	}

	public boolean isAllowopts() {
		return _allowopts;
	}

	public boolean setAllowopts(boolean allowopts) {
		_allowopts = allowopts;
		return true;
	}

	public boolean isBinat() {
		return _binat;
	}

	public boolean setBinat(boolean binat) {
		_binat = binat;
		return true;
	}

	public String getDivertAddr() {
		return _divertAddr;
	}

	public boolean setDivertAddr(String divertAddr) {
		_divertAddr = divertAddr;
		return true;
	}

	public String getDivertPort() {
		return _divertPort;
	}

	public boolean setDivertPort(String divertPort) {
		_divertPort = divertPort;
		return true;
	}

	public String getDivertPacketPort() {
		return _divertPacketPort;
	}

	public boolean setDivertPacketPort(String divertPacketPort) {
		_divertPacketPort = divertPacketPort;
		return true;
	}

	public String getFlags() {
		return _flags;
	}

	public boolean setFlags(String flags) {
		_flags = flags;
		return true;
	}

	public String getFlagset() {
		return _flagset;
	}

	public boolean setFlagset(String flags) {
		_flagset = flags;
		return true;
	}

	public boolean isFragment() {
		return _fragment;
	}

	public boolean setFragment(boolean fragment) {
		_fragment = fragment;
		return true;
	}

	public List<IcmpItem> getIcmp6spec() {
		return _icmp6spec;
	}

	public List<IcmpItem> getIcmpspec() {
		return _icmpspec;
	}

	public String getLabel() {
		return _label;
	}

	public boolean setLabel(String label) {
		_label = label;
		return true;
	}

	public String getMatchTag() {
		return _matchTag;
	}

	public boolean setMatchTag(String matchTag) {
		_matchTag = matchTag;
		return true;
	}

	public boolean isMatchTagNot() {
		return _matchTagNot;
	}

	public boolean setMatchTagNot(boolean matchTagNot) {
		_matchTagNot = matchTagNot;
		return true;
	}

	public int getMax_mss() {
		return _max_mss;
	}

	public boolean setMax_mss(int max_mss) {
		_max_mss = max_mss;
		return true;
	}

	public int getMinttl() {
		return _minttl;
	}

	public boolean setMinttl(int minttl) {
		_minttl = minttl;
		return true;
	}

	public int getNodf() {
		return _nodf;
	}

	public boolean setNodf(int nodf) {
		_nodf = nodf;
		return true;
	}

	public StringsList getOptions() {
		return _options;
	}

	public String getPQname() {
		return _pqname;
	}

	public boolean setPQname(String PQname) {
		_pqname = PQname;
		return true;
	}

	public String getProbability() {
		return _probability;
	}

	public boolean setProbability(String probability) {
		_probability = probability;
		return true;
	}

	public String getQname() {
		return _qname;
	}

	public boolean setQname(String qname) {
		_qname = qname;
		return true;
	}

	public int getRandomid() {
		return _randomid;
	}

	public boolean setRandomid(int randomid) {
		_randomid = randomid;
		return true;
	}

	public String getRcv() {
		return _rcv;
	}

	public boolean setRcv(String rcv) {
		_rcv = rcv;
		return true;
	}

	public RouteOptsTemplate getRouteOpts() {
		return _routeOpts;
	}

	public boolean setRouteOpts(RouteOptsTemplate routeOpts) {
		_routeOpts = routeOpts;
		return true;
	}

	public String getRtableId() {
		return _rtableId;
	}

	public boolean setRtableId(String rtableId) {
		_rtableId = rtableId;
		return true;
	}

	public ScrubOptsTemplate getScrubOpts() {
		return _scrubOpts;
	}

	public boolean setScrubOpts(ScrubOptsTemplate scrubOpts) {
		_scrubOpts = scrubOpts;
		return true;
	}

	public int getSettos() {
		return _settos;
	}

	public boolean setSettos(int settos) {
		_settos = settos;
		return true;
	}

	public String getTag() {
		return _tag;
	}

	public boolean setTag(String tag) {
		_tag = tag;
		return true;
	}

	public String getTos() {
		return _tos;
	}

	public boolean setTos(String tos) {
		_tos = tos;
		return true;
	}

	public RedirSpecTemplate getNat() {
		return _nat;
	}

	public boolean setNat(RedirSpecTemplate nat) {
		_nat = nat;
		return true;
	}

	public RedirSpecTemplate getRdr() {
		return _rdr;
	}

	public boolean setRdr(RedirSpecTemplate rdr) {
		_rdr = rdr;
		return true;
	}

	public RedirSpecTemplate getRroute() {
		return _rroute;
	}

	public boolean setRroute(RedirSpecTemplate rroute) {
		_rroute = rroute;
		return true;
	}

}

