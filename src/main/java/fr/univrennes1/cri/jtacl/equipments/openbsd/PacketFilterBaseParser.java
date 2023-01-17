/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.parsers.CommonRules;
import org.parboiled.Rule;

/**
 * Base rule definition for the parser.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class PacketFilterBaseParser extends CommonRules<Object> {

	/*
	 * Special characters.
	 */
	public static final String SPECIALS = "/{},!=<>() \t\n";

	/*
	 * characters allowed in ident.
	 */
	public static final String IDENT
			= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";


	/*
	 * PF keywords.
	 *
	 * (nb: we use the same name as Packet Filter, even if it breaks style)
	 */

	public Rule ALL() {
		return
			Sequence(
				String("all"),
				NextIsSeparator()
			);
	}

	public Rule ALLOWOPTS() {
		return
			Sequence(
				String("allow-opts"),
				NextIsSeparator()
			);
	}

	public Rule ALTQ() {
		return
			Sequence(
				String("altq"),
				NextIsSeparator()
			);
	}

	public Rule ANCHOR() {
		return
			Sequence(
				String("anchor"),
				NextIsSeparator()
			);
	}

	public Rule ANTISPOOF() {
		return
			Sequence(
				String("antispoof"),
				NextIsSeparator()
			);
	}

	public Rule ANY() {
		return
			Sequence(
				String("any"),
				NextIsSeparator()
			);
	}

	public Rule BANDWIDTH() {
		return
			Sequence(
				String("bandwidth"),
				NextIsSeparator()
			);
	}

	public Rule BINATTO() {
		return
			Sequence(
				String("binat-to"),
				NextIsSeparator()
			);
	}

	public Rule BITMASK() {
		return
			Sequence(
				String("bitmask"),
				NextIsSeparator()
			);
	}

	public Rule BLOCK() {
		return
			Sequence(
				String("block"),
				NextIsSeparator()
			);
	}

	public Rule BLOCKPOLICY() {
		return
			Sequence(
				String("block-policy"),
				NextIsSeparator()
			);
	}

	public Rule CBQ() {
		return
			Sequence(
				String("cbq"),
				NextIsSeparator()
			);
	}

	public Rule CODE() {
		return
			Sequence(
				String("code"),
				NextIsSeparator()
			);
	}

	public Rule FRAGCROP() {
		return
			Sequence(
				String("crop"),
				NextIsSeparator()
			);
	}

	public Rule DEBUG() {
		return
			Sequence(
				String("debug"),
				NextIsSeparator()
			);
	}

	public Rule DIVERTPACKET() {
		return
			Sequence(
				String("divert-packet"),
				NextIsSeparator()
			);
	}

	public Rule DIVERTREPLY() {
		return
			Sequence(
				String("divert-reply"),
				NextIsSeparator()
			);
	}

	public Rule DIVERTTO() {
		return
			Sequence(
				String("divert-to"),
				NextIsSeparator()
			);
	}

	public Rule DROP() {
		return
			Sequence(
				String("drop"),
				NextIsSeparator()
			);
	}

	public Rule FRAGDROP() {
		return
			Sequence(
				String("drop-ovl"),
				NextIsSeparator()
			);
	}

	public Rule DUPTO() {
		return
			Sequence(
				String("dup-to"),
				NextIsSeparator()
			);
	}

	public Rule FASTROUTE() {
		return
			Sequence(
				String("fastroute"),
				NextIsSeparator()
			);
	}

	public Rule FILENAME() {
		return
			Sequence(
				String("file"),
				NextIsSeparator()
			);
	}

	public Rule FINGERPRINTS() {
		return
			Sequence(
				String("fingerprints"),
				NextIsSeparator()
			);
	}

	public Rule FLAGS() {
		return
			Sequence(
				String("flags"),
				NextIsSeparator()
			);
	}

	public Rule FLOATING() {
		return
			Sequence(
				String("floating"),
				NextIsSeparator()
			);
	}

	public Rule FLUSH() {
		return
			Sequence(
				String("flush"),
				NextIsSeparator()
			);
	}

	public Rule FOR() {
		return
			Sequence(
				String("for"),
				NextIsSeparator()
			);
	}

	public Rule FRAGMENT() {
		return
			Sequence(
				String("fragment"),
				NextIsSeparator()
			);
	}

	public Rule FROM() {
		return
			Sequence(
				String("from"),
				NextIsSeparator()
			);
	}

	public Rule GLOBAL() {
		return
			Sequence(
				String("global"),
				NextIsSeparator()
			);
	}

	public Rule GROUP() {
		return
			Sequence(
				String("group"),
				NextIsSeparator()
			);
	}

	public Rule HFSC() {
		return
			Sequence(
				String("hfsc"),
				NextIsSeparator()
			);
	}

	public Rule HOSTID() {
		return
			Sequence(
				String("hostid"),
				NextIsSeparator()
			);
	}

	public Rule ICMPTYPE() {
		return
			Sequence(
				String("icmp-type"),
				NextIsSeparator()
			);
	}

	public Rule ICMP6TYPE() {
		return
			Sequence(
				String("icmp6-type"),
				NextIsSeparator()
			);
	}

	public Rule IFBOUND() {
		return
			Sequence(
				String("if-bound"),
				NextIsSeparator()
			);
	}

	public Rule IN() {
		return
			Sequence(
				String("in"),
				NextIsSeparator()
			);
	}

	public Rule INCLUDE() {
		return
			Sequence(
				String("include"),
				NextIsSeparator()
			);
	}

	public Rule INET() {
		return
			Sequence(
				String("inet"),
				NextIsSeparator()
			);
	}

	public Rule INET6() {
		return
			Sequence(
				String("inet6"),
				NextIsSeparator()
			);
	}

	public Rule KEEP() {
		return
			Sequence(
				String("keep"),
				NextIsSeparator()
			);
	}

	public Rule LABEL() {
		return
			Sequence(
				String("label"),
				NextIsSeparator()
			);
	}

	public Rule LIMIT() {
		return
			Sequence(
				String("limit"),
				NextIsSeparator()
			);
	}

	public Rule LINKSHARE() {
		return
			Sequence(
				String("linkshare"),
				NextIsSeparator()
			);
	}

	public Rule LOAD() {
		return
			Sequence(
				String("load"),
				NextIsSeparator()
			);
	}

	public Rule LOG() {
		return
			Sequence(
				String("log"),
				NextIsSeparator()
			);
	}

	public Rule LOGINTERFACE() {
		return
			Sequence(
				String("loginterface"),
				NextIsSeparator()
			);
	}

	public Rule MATCH() {
		return
			Sequence(
				String("match"),
				NextIsSeparator()
			);
	}

	public Rule MAXIMUM() {
		return
			Sequence(
				String("max"),
				NextIsSeparator()
			);
	}

	public Rule MAXMSS() {
		return
			Sequence(
				String("max-mss"),
				NextIsSeparator()
			);
	}

	public Rule MAXSRCCONN() {
		return
			Sequence(
				String("max-src-conn"),
				NextIsSeparator()
			);
	}

	public Rule MAXSRCCONNRATE() {
		return
			Sequence(
				String("max-src-conn-rate"),
				NextIsSeparator()
			);
	}

	public Rule MAXSRCNODES() {
		return
			Sequence(
				String("max-src-nodes"),
				NextIsSeparator()
			);
	}

	public Rule MAXSRCSTATES() {
		return
			Sequence(
				String("max-src-states"),
				NextIsSeparator()
			);
	}

	public Rule MINTTL() {
		return
			Sequence(
				String("min-ttl"),
				NextIsSeparator()
			);
	}

	public Rule MODULATE() {
		return
			Sequence(
				String("modulate"),
				NextIsSeparator()
			);
	}

	public Rule NAT() {
		return
			Sequence(
				String("nat"),
				NextIsSeparator()
			);
	}

	public Rule NATANCHOR() {
		return
			Sequence(
				String("nat-anchor"),
				NextIsSeparator()
			);
	}

	public Rule NATTO() {
		return
			Sequence(
				String("nat-to"),
				NextIsSeparator()
			);
	}

	public Rule NO() {
		return
			Sequence(
				String("no"),
				NextIsSeparator()
			);
	}

	public Rule NODF() {
		return
			Sequence(
				String("no-df"),
				NextIsSeparator()
			);
	}

	public Rule NOROUTE() {
		return
			Sequence(
				String("no-route"),
				NextIsSeparator()
			);
	}

	public Rule NOSYNC() {
		return
			Sequence(
				String("no-sync"),
				NextIsSeparator()
			);
	}

	public Rule ON() {
		return
			Sequence(
				String("on"),
				NextIsSeparator()
			);
	}

	public Rule OPTIMIZATION() {
		return
			Sequence(
				String("optimization"),
				NextIsSeparator()
			);
	}

	public Rule OS() {
		return
			Sequence(
				String("os"),
				NextIsSeparator()
			);
	}

	public Rule OUT() {
		return
			Sequence(
				String("out"),
				NextIsSeparator()
			);
	}

	public Rule OVERLOAD() {
		return
			Sequence(
				String("overload"),
				NextIsSeparator()
			);
	}

	public Rule PASS() {
		return
			Sequence(
				String("pass"),
				NextIsSeparator()
			);
	}

	public Rule PFLOW() {
		return
			Sequence(
				String("pflow"),
				NextIsSeparator()
			);
	}

	public Rule PORT() {
		return
			Sequence(
				String("port"),
				NextIsSeparator()
			);
	}

	public Rule PRIORITY() {
		return
			Sequence(
				String("priority"),
				NextIsSeparator()
			);
	}

	public Rule PRIQ() {
		return
			Sequence(
				String("priq"),
				NextIsSeparator()
			);
	}

	public Rule PROBABILITY() {
		return
			Sequence(
				String("probability"),
				NextIsSeparator()
			);
	}

	public Rule PROTO() {
		return
			Sequence(
				String("proto"),
				NextIsSeparator()
			);
	}

	public Rule QLIMIT() {
		return
			Sequence(
				String("qlimit"),
				NextIsSeparator()
			);
	}

	public Rule QUEUE() {
		return
			Sequence(
				String("queue"),
				NextIsSeparator()
			);
	}

	public Rule QUICK() {
		return
			Sequence(
				String("quick"),
				NextIsSeparator()
			);
	}

	public Rule RANDOM() {
		return
			Sequence(
				String("random"),
				NextIsSeparator()
			);
	}

	public Rule RANDOMID() {
		return
			Sequence(
				String("random-id"),
				NextIsSeparator()
			);
	}

	public Rule RDR() {
		return
			Sequence(
				String("rdr"),
				NextIsSeparator()
			);
	}

	public Rule RDRANCHOR() {
		return
			Sequence(
				String("rdr-anchor"),
				NextIsSeparator()
			);
	}

	public Rule RDRTO() {
		return
			Sequence(
				String("rdr-to"),
				NextIsSeparator()
			);
	}

	public Rule REALTIME() {
		return
			Sequence(
				String("realtime"),
				NextIsSeparator()
			);
	}

	public Rule REASSEMBLE() {
		return
			Sequence(
				String("reassemble"),
				NextIsSeparator()
			);
	}

	public Rule RECEIVEDON() {
		return
			Sequence(
				String("received-on"),
				NextIsSeparator()
			);
	}

	public Rule REPLYTO() {
		return
			Sequence(
				String("reply-to"),
				NextIsSeparator()
			);
	}

	public Rule REQUIREORDER() {
		return
			Sequence(
				String("require-order"),
				NextIsSeparator()
			);
	}

	public Rule RETURN() {
		return
			Sequence(
				String("return"),
				NextIsSeparator()
			);
	}

	public Rule RETURNICMP() {
		return
			Sequence(
				String("return-icmp"),
				NextIsSeparator()
			);
	}

	public Rule RETURNICMP6() {
		return
			Sequence(
				String("return-icmp6"),
				NextIsSeparator()
			);
	}

	public Rule RETURNRST() {
		return
			Sequence(
				String("return-rst"),
				NextIsSeparator()
			);
	}

	public Rule ROUNDROBIN() {
		return
			Sequence(
				String("round-robin"),
				NextIsSeparator()
			);
	}

	public Rule ROUTE() {
		return
			Sequence(
				String("route"),
				NextIsSeparator()
			);
	}

	public Rule ROUTETO() {
		return
			Sequence(
				String("route-to"),
				NextIsSeparator()
			);
	}

	public Rule RTABLE() {
		return
			Sequence(
				String("rtable"),
				NextIsSeparator()
			);
	}

	public Rule RULE() {
		return
			Sequence(
				String("rule"),
				NextIsSeparator()
			);
	}

	public Rule RULESET_OPTIMIZATION() {
		return
			Sequence(
				String("ruleset-optimization"),
				NextIsSeparator()
			);
	}

	public Rule SCRUB() {
		return
			Sequence(
				String("scrub"),
				NextIsSeparator()
			);
	}

	public Rule SET() {
		return
			Sequence(
				String("set"),
				NextIsSeparator()
			);
	}

	public Rule SETTOS() {
		return
			Sequence(
				String("set-tos"),
				NextIsSeparator()
			);
	}

	public Rule SKIP() {
		return
			Sequence(
				String("skip"),
				NextIsSeparator()
			);
	}

	public Rule SLOPPY() {
		return
			Sequence(
				String("sloppy"),
				NextIsSeparator()
			);
	}

	public Rule SOURCEHASH() {
		return
			Sequence(
				String("source-hash"),
				NextIsSeparator()
			);
	}

	public Rule SOURCETRACK() {
		return
			Sequence(
				String("source-track"),
				NextIsSeparator()
			);
	}

	public Rule STATE() {
		return
			Sequence(
				String("state"),
				NextIsSeparator()
			);
	}

	public Rule STATEDEFAULTS() {
		return
			Sequence(
				String("state-defaults"),
				NextIsSeparator()
			);
	}

	public Rule STATEPOLICY() {
		return
			Sequence(
				String("state-policy"),
				NextIsSeparator()
			);
	}

	public Rule STATICPORT() {
		return
			Sequence(
				String("static-port"),
				NextIsSeparator()
			);
	}

	public Rule STICKYADDRESS() {
		return
			Sequence(
				String("sticky-address"),
				NextIsSeparator()
			);
	}

	public Rule SYNPROXY() {
		return
			Sequence(
				String("synproxy"),
				NextIsSeparator()
			);
	}

	public Rule TABLE() {
		return
			Sequence(
				String("table"),
				NextIsSeparator()
			);
	}

	public Rule TAG() {
		return
			Sequence(
				String("tag"),
				NextIsSeparator()
			);
	}

	public Rule TAGGED() {
		return
			Sequence(
				String("tagged"),
				NextIsSeparator()
			);
	}

	public Rule TBRSIZE() {
		return
			Sequence(
				String("tbrsize"),
				NextIsSeparator()
			);
	}

	public Rule TIMEOUT() {
		return
			Sequence(
				String("timeout"),
				NextIsSeparator()
			);
	}

	public Rule TO() {
		return
			Sequence(
				String("to"),
				NextIsSeparator()
			);
	}

	public Rule TOS() {
		return
			Sequence(
				String("tos"),
				NextIsSeparator()
			);
	}

	public Rule TTL() {
		return
			Sequence(
				String("ttl"),
				NextIsSeparator()
			);
	}

	public Rule UPPERLIMIT() {
		return
			Sequence(
				String("upperlimit"),
				NextIsSeparator()
			);
	}

	public Rule URPFFAILED() {
		return
			Sequence(
				String("urpf-failed"),
				NextIsSeparator()
			);
	}

	public Rule USER() {
		return
			Sequence(
				String("user"),
				NextIsSeparator()
			);
	}

	/*********************** end of keywords ******************/

	/**
	 * Matches a keyword
	 * @return a Rule.
	 */
	public Rule PfKeyword() {
		return
			FirstOf(
				ALL(),
				ALLOWOPTS(),
				ALTQ(),
				ANCHOR(),
				ANTISPOOF(),
				ANY(),
				BANDWIDTH(),
				BINATTO(),
				BITMASK(),
				BLOCK(),
				BLOCKPOLICY(),
				CBQ(),
				CODE(),
				FRAGCROP(),
				DEBUG(),
				DIVERTPACKET(),
				DIVERTREPLY(),
				DIVERTTO(),
				DROP(),
				FRAGDROP(),
				DUPTO(),
				FASTROUTE(),
				FILENAME(),
				FINGERPRINTS(),
				FLAGS(),
				FLOATING(),
				FLUSH(),
				FOR(),
				FRAGMENT(),
				FROM(),
				GLOBAL(),
				GROUP(),
				HFSC(),
				HOSTID(),
				ICMPTYPE(),
				ICMP6TYPE(),
				IFBOUND(),
				IN(),
				INCLUDE(),
				INET(),
				INET6(),
				KEEP(),
				LABEL(),
				LIMIT(),
				LINKSHARE(),
				LOAD(),
				LOG(),
				LOGINTERFACE(),
				MATCH(),
				MAXIMUM(),
				MAXMSS(),
				MAXSRCCONN(),
				MAXSRCCONNRATE(),
				MAXSRCNODES(),
				MAXSRCSTATES(),
				MINTTL(),
				MODULATE(),
				NAT(),
				NATANCHOR(),
				NATTO(),
				NO(),
				NODF(),
				NOROUTE(),
				NOSYNC(),
				ON(),
				OPTIMIZATION(),
				OS(),
				OUT(),
				OVERLOAD(),
				PASS(),
				PFLOW(),
				PORT(),
				PRIORITY(),
				PRIQ(),
				PROBABILITY(),
				PROTO(),
				QLIMIT(),
				QUEUE(),
				QUICK(),
				RANDOM(),
				RANDOMID(),
				RDR(),
				RDRANCHOR(),
				RDRTO(),
				REALTIME(),
				REASSEMBLE(),
				RECEIVEDON(),
				REPLYTO(),
				REQUIREORDER(),
				RETURN(),
				RETURNICMP(),
				RETURNICMP6(),
				RETURNRST(),
				ROUNDROBIN(),
				ROUTE(),
				ROUTETO(),
				RTABLE(),
				RULE(),
				RULESET_OPTIMIZATION(),
				SCRUB(),
				SET(),
				SETTOS(),
				SKIP(),
				SLOPPY(),
				SOURCEHASH(),
				SOURCETRACK(),
				STATE(),
				STATEDEFAULTS(),
				STATEPOLICY(),
				STATICPORT(),
				STICKYADDRESS(),
				SYNPROXY(),
				TABLE(),
				TAG(),
				TAGGED(),
				TBRSIZE(),
				TIMEOUT(),
				TO(),
				TOS(),
				TTL(),
				UPPERLIMIT(),
				URPFFAILED(),
				USER()
			);
	}

	/**
	 * Matches an atom: a string delimited by specials.
	 * @return a Rule.
	 */
	public Rule PfAtom() {
		return OneOrMore(
					Sequence(
						TestNot(PfSeparators()),
						ANY
					)
				);
	}

	public Rule PfSeparators() {
		return
			FirstOf(
				String("\\\n"),
				AnyOf(SPECIALS)
			);
	}

	@Override
	public Rule WhiteSpace() {
		return
			FirstOf(
				super.WhiteSpace(),
				String("\\\n")
			);
	}

	/**
	 * Tests if the next input is a separator.
	 * @return a Rule
	 */
	public Rule NextIsSeparator() {
		return
			FirstOf(
				Test(EOI),
				Test(PfSeparators())
			);
	}

	/**
	 * Matches an ident: a string with alpha characters or '_'
	 * @return a Rule.
	 */
	public Rule PfIdent() {
		return OneOrMore(
					AnyOf(IDENT)
				);
	}


}
