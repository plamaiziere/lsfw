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
	private List<IcmpItem> _icmpspec = new ArrayList<IcmpItem>();
	private List<IcmpItem> _icmp6spec = new ArrayList<IcmpItem>();
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

	public void setAction(String action) {
		_action = action;
	}

	public boolean isAllowopts() {
		return _allowopts;
	}

	public void setAllowopts(boolean allowopts) {
		_allowopts = allowopts;
	}

	public boolean isBinat() {
		return _binat;
	}

	public void setBinat(boolean binat) {
		_binat = binat;
	}

	public String getDivertAddr() {
		return _divertAddr;
	}

	public void setDivertAddr(String divertAddr) {
		_divertAddr = divertAddr;
	}

	public String getDivertPort() {
		return _divertPort;
	}

	public void setDivertPort(String divertPort) {
		_divertPort = divertPort;
	}

	public String getDivertPacketPort() {
		return _divertPacketPort;
	}

	public void setDivertPacketPort(String divertPacketPort) {
		_divertPacketPort = divertPacketPort;
	}

	public String getFlags() {
		return _flags;
	}

	public void setFlags(String flags) {
		_flags = flags;
	}

	public String getFlagset() {
		return _flagset;
	}

	public void setFlagset(String flags) {
		_flagset = flags;
	}

	public boolean isFragment() {
		return _fragment;
	}

	public void setFragment(boolean fragment) {
		_fragment = fragment;
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

	public void setLabel(String label) {
		_label = label;
	}

	public String getMatchTag() {
		return _matchTag;
	}

	public void setMatchTag(String matchTag) {
		_matchTag = matchTag;
	}

	public boolean isMatchTagNot() {
		return _matchTagNot;
	}

	public void setMatchTagNot(boolean matchTagNot) {
		_matchTagNot = matchTagNot;
	}

	public int getMax_mss() {
		return _max_mss;
	}

	public void setMax_mss(int max_mss) {
		_max_mss = max_mss;
	}

	public int getMinttl() {
		return _minttl;
	}

	public void setMinttl(int minttl) {
		_minttl = minttl;
	}

	public int getNodf() {
		return _nodf;
	}

	public void setNodf(int nodf) {
		_nodf = nodf;
	}

	public StringsList getOptions() {
		return _options;
	}

	public String getPQname() {
		return _pqname;
	}

	public void setPQname(String PQname) {
		_pqname = PQname;
	}

	public String getProbability() {
		return _probability;
	}

	public void setProbability(String probability) {
		_probability = probability;
	}

	public String getQname() {
		return _qname;
	}

	public void setQname(String qname) {
		_qname = qname;
	}

	public int getRandomid() {
		return _randomid;
	}

	public void setRandomid(int randomid) {
		_randomid = randomid;
	}

	public String getRcv() {
		return _rcv;
	}

	public void setRcv(String rcv) {
		_rcv = rcv;
	}

	public RouteOptsTemplate getRouteOpts() {
		return _routeOpts;
	}

	public void setRouteOpts(RouteOptsTemplate routeOpts) {
		_routeOpts = routeOpts;
	}

	public String getRtableId() {
		return _rtableId;
	}

	public void setRtableId(String rtableId) {
		_rtableId = rtableId;
	}

	public ScrubOptsTemplate getScrubOpts() {
		return _scrubOpts;
	}

	public void setScrubOpts(ScrubOptsTemplate scrubOpts) {
		_scrubOpts = scrubOpts;
	}

	public int getSettos() {
		return _settos;
	}

	public void setSettos(int settos) {
		_settos = settos;
	}

	public String getTag() {
		return _tag;
	}

	public void setTag(String tag) {
		_tag = tag;
	}

	public String getTos() {
		return _tos;
	}

	public void setTos(String tos) {
		_tos = tos;
	}

	public RedirSpecTemplate getNat() {
		return _nat;
	}

	public void setNat(RedirSpecTemplate nat) {
		_nat = nat;
	}

	public RedirSpecTemplate getRdr() {
		return _rdr;
	}

	public void setRdr(RedirSpecTemplate rdr) {
		_rdr = rdr;
	}

	public RedirSpecTemplate getRroute() {
		return _rroute;
	}

	public void setRroute(RedirSpecTemplate rroute) {
		_rroute = rroute;
	}

}

