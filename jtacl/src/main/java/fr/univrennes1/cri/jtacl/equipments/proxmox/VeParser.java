/*
 * Copyright (c) 2021, Universite de Rennes 1
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
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.parsers.CommonRules;
import org.parboiled.Rule;

import java.util.LinkedList;
import java.util.List;

/*
 * Parser for proxmox VE https://pve.proxmox.com/pve-docs/chapter-pve-firewall.html
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class VeParser extends CommonRules<Object> {

	 /*
	 * characters allowed in ident
	 */
	public static final String IDENTCHR
			= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_.+:/-";

	public static String stripComment(String s) {
		return s.trim().split("#")[0];
	}

	protected String ident;
	protected List<String> listIdents;
	protected RuleTemplate ruleTpl;

	public RuleTemplate getRuleTpl() {
		return ruleTpl;
	}

	public boolean newRuleTpl() {
		this.ruleTpl = new RuleTemplate();
		return true;
	}

	protected boolean setIdent(String i) {
		ident = i;
		return true;
	}

	public boolean newListIdents() {
		listIdents = new LinkedList<>();
		return true;
	}

	public String getIdent() {
		return ident;
	}

// rules

	public Rule RveRule() {
		return Sequence(
				newRuleTpl(),
				SkipSpaces(),
				Optional(
					Sequence(
						RDisabled(),
						SkipSpaces()
					)
				),
				FirstOf(
					RGroupRule(),
					RRule()
				)
		);
	}

	public Rule RRule() {
		return Sequence(
				RDirection(),
				WhiteSpaces(),
				FirstOf(
					RMacro(),
					RAction()
				),
				SkipSpaces(),
				Optional(
					ROptions()
				)
		);
	}

	public Rule RGroupRule() {
		return Sequence(
				IgnoreCase("GROUP"),
				WhiteSpaces(),
				RIdent(),
				ruleTpl.setGroupName(ident),
				SkipSpaces()
		);
	}

	public Rule ROptions() {
		return ROptions_();
	}

	public Rule ROptions_() {
		return FirstOf(
					EOI,
					Sequence(
						FirstOf(RDestIp(),
								RDestPort(),
								RIcmpType(),
								RIface(),
								RProto(),
								RSourceIp(),
								RSourcePort(),
								RLog()
						),
						WhiteSpacesOrEoi(),
						ROptions_()
					)
		);
	}

	public Rule RDirection() {
		return Sequence(
				FirstOf(
					IgnoreCase("IN"),
					IgnoreCase("OUT")
				),
				ruleTpl.setDirection(match())
		);
	}

	public Rule RAction() {
		return Sequence(
				FirstOf(
					IgnoreCase("ACCEPT"),
					IgnoreCase("DROP"),
					IgnoreCase("REJECT")
				),
				ruleTpl.setAction(match())
		);
	}

	public Rule RMacro() {
		return Sequence(
					RIdent(),
					Ch('('),
					RAction(),
					Ch(')'),
					ruleTpl.setMacro(ident)
		);
	}

	public Rule RDisabled() {
		return Sequence(
				Ch('|'),
				ruleTpl.setDisabled(true)
		);
	}

	public Rule RSourceIp() {
		return Sequence(
				FirstOf(
						IgnoreCase("--source"),
						IgnoreCase("-source")
				),
				WhiteSpaces(),
				RListIdents(),
				ruleTpl.setSourceIpSpec(listIdents)
		);
	}

	public Rule RDestIp() {
		return Sequence(
				FirstOf(
						IgnoreCase("--dest"),
						IgnoreCase("-dest")
				),
				WhiteSpaces(),
				RListIdents(),
				ruleTpl.setDestIpSpec(listIdents)
		);
	}

	public Rule RSourcePort() {
		return Sequence(
				FirstOf(
						IgnoreCase("--sport"),
						IgnoreCase("-sport")
				),
				WhiteSpaces(),
				RListIdents(),
				ruleTpl.setSourcePortSpec(listIdents)
		);
	}

	public Rule RDestPort() {
		return Sequence(
				FirstOf(
						IgnoreCase("--dport"),
						IgnoreCase("-dport")
				),
				WhiteSpaces(),
				RListIdents(),
				ruleTpl.setDestPortSpec(listIdents)
		);
	}

	public Rule RIcmpType() {
		return Sequence(
				FirstOf(
						IgnoreCase("--icmp-type"),
						IgnoreCase("-icmp-type")
				),
				WhiteSpaces(),
				RIdent(),
				ruleTpl.setIcmpType(ident)
		);
	}

	public Rule RProto() {
		return Sequence(
				FirstOf(
						IgnoreCase("--proto"),
						IgnoreCase("-proto")
				),
				WhiteSpaces(),
				RIdent(),
				ruleTpl.setProto(ident)
		);
	}

	public Rule RIface() {
		return Sequence(
				FirstOf(
						IgnoreCase("--iface"),
						IgnoreCase("-iface")
				),
				WhiteSpaces(),
				RIdent(),
				ruleTpl.setIface(ident)
		);
	}

	public Rule RLog() {
		return Sequence(
				FirstOf(
						IgnoreCase("--log"),
						IgnoreCase("-log")
				),
				WhiteSpaces(),
				RIdent()
		);
	}

	public Rule RListIdents() {
		return Sequence(
				newListIdents(),
				RListIdents_()
		);
	}

	public Rule RListIdents_() {
		return Sequence(
				RIdent(),
				listIdents.add(ident),
				Optional(
						Sequence(
							SkipSpaces(),
							Ch(','),
							SkipSpaces(),
							RListIdents_()
						)
				)
		);
	}

	public Rule RIdent() {
		return Sequence(
				OneOrMore(
					AnyOf(IDENTCHR)
				),
				setIdent(match())
		);
	}
}
