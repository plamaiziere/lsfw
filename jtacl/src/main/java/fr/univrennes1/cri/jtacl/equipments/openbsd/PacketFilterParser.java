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

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.lib.misc.StringsList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * Parser rules for PF.
 *
 * Taken from OpenBSD rules (parse.y, OpenBSD 4.7)
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class PacketFilterParser extends PacketFilterBaseParser {

	protected String _ruleName;
	protected String _name;
	protected String _value;
	protected char _quotec;
	protected boolean _quoted;
	protected char _previousChar;
	protected String _lastString;
	protected String _pfString;
	protected boolean _pfStringEnd;
	protected String _ifItem;
	protected boolean _pfnot;
	protected Xhost _pfXhost;
	protected PortItemTemplate _pfPortItem;
	protected IcmpItem _pfIcmpItem;
	protected String _pfStateOptItem;
	protected String _flags;
	protected String _flagset;
	protected List<Xhost> _ipspec = new ArrayList<Xhost>();
	protected List<PortItemTemplate> _portspec = new ArrayList<PortItemTemplate>();
	protected List<IcmpItem> _icmpspec = new ArrayList<IcmpItem>();
	protected List<IcmpItem> _icmp6spec = new ArrayList<IcmpItem>();
	protected StringsList _stateOptSpec = new StringsList();
	protected ScrubOptsTemplate _scrubOpts;
	protected PoolOptsTemplate _poolOpts;
	protected String _pfQname;
	protected String _pfPQname;
	protected List<Xhost> _redirHosts = new ArrayList<Xhost>();
	protected RedirSpecTemplate _redirSpec;
	protected RouteOptsTemplate _routeOpts;
	protected String _ifname;
	protected StringsList _macroValues = new StringsList();
	protected String _matchedText;
	protected StringBuilder _curExpandedContext;

	protected RuleTemplate _pfRule;
	protected TableTemplate _pfTable;
	protected AnchorTemplate _pfAnchor;

	protected boolean macroValuesClear() {
		_macroValues.clear();
		return true;
	}

	protected boolean macroValuesToValue() {
		_value = "";
		for (String s: _macroValues)
			_value = _value + s + " ";
		_value = _value.trim();
		return true;
	}

	protected boolean newTableTemplate() {
		_pfTable = new TableTemplate();
		return true;
	}

	protected boolean ipSpecClear() {
		_ipspec.clear();
		return true;
	}

	protected boolean newRuleTemplate() {
		_pfRule = new RuleTemplate();
		return true;
	}

	protected boolean newAnchorTemplate() {
		_pfAnchor = new AnchorTemplate();
		return true;
	}

	protected boolean newScrubOptsTemplate() {
		_scrubOpts = new ScrubOptsTemplate();
		return true;
	}

	protected boolean icmpSpecClear() {
		_icmpspec.clear();
		return true;
	}

	protected boolean icmp6SpecClear() {
		_icmp6spec.clear();
		return true;
	}

	protected boolean stateOptSpecClear() {
		_stateOptSpec.clear();
		return true;
	}

	protected boolean pfRuleSetFilterOpts() {
		_pfRule.getFilterOpts().setFlags(_flags);
		_pfRule.getFilterOpts().setFlagset(_flagset);
		_pfRule.getFilterOpts().getIcmpspec().addAll(_icmpspec);
		_pfRule.getFilterOpts().getIcmp6spec().addAll(_icmp6spec);
		return true;
	}

	protected boolean pfRuleSetIfItem(boolean pfnot, String ifName) {
		if (pfnot)
			ifName = "!" + ifName;
		_pfRule.getIfList().add(ifName);
		return true;
	}

	protected boolean newRouteOpts() {
		_routeOpts = new RouteOptsTemplate();
		return true;
	}

	protected boolean portSpecClear() {
		_portspec.clear();
		return true;
	}

	protected boolean newPfXhost() {
		_pfXhost = new Xhost();
		return true;
	}

	protected boolean newPfPortItem() {
		_pfPortItem = new PortItemTemplate();
		return true;
	}

	protected boolean pfPortItemFromRange(String portRange) {
		/*
		 * XXX: why ':' is not in PORTBINARY?
		 */
		int p = portRange.indexOf(':');
		if (p > 0 && p < (portRange.length() -1)) {
			_pfPortItem.setOperator(":");
			String first = portRange.substring(0, p);
			String last = portRange.substring(p + 1);
			_pfPortItem.setFirstPort(first);
			_pfPortItem.setLastPort(last);
		} else
			_pfPortItem.setFirstPort(portRange);
		return true;
	}

	protected boolean newPfIcmpItem() {
		_pfIcmpItem = new IcmpItem();
		return true;
	}

	protected boolean redirHostsClear() {
		_redirHosts.clear();
		return true;
	}

	protected boolean newRedirSpec() {
		_redirSpec = new RedirSpecTemplate();
		return true;
	}

	protected boolean newPoolOpts() {
		_poolOpts = new PoolOptsTemplate();
		return true;
	}

	protected boolean quotedStringStart(String string) {
		_quotec = string.charAt(0);
		_lastString = "";
		_previousChar = '\0';
		_pfStringEnd = false;
		return true;
	}

	protected boolean quotedStringContinue() {
		return !_pfStringEnd;
	}

	protected boolean quotedString(String string) {
		char c = string.charAt(0);
		/*
		 * escaped character.
		 */
		if (_previousChar == '\\') {
			if (c != '\n')
				_lastString += c;
			_previousChar = '\0';
			return true;
		} else {
			if (c == '\\') {
				_previousChar = c;
				return true;
			}
			if (c != _quotec && c != '\n') {
				_previousChar = c;
				_lastString += c;
			}
			if (c == _quotec)
				_pfStringEnd = true;
			return true;
		}
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

	public boolean setFlagset(String flagset) {
		_flagset = flagset;
		return true;
	}

	public String getName() {
		return _name;
	}

	public boolean setName(String name) {
		_name = name;
		return true;
	}

	public String getRuleName() {
		return _ruleName;
	}

	public boolean setRuleName(String ruleName) {
		_ruleName = ruleName;
		return true;
	}

	public String getMatchedText() {
		return _matchedText;
	}

	public boolean setMatchedText(String matchedText) {
		_matchedText = matchedText;
		return true;
	}

	public String getValue() {
		return _value;
	}

	public boolean setValue(String value) {
		_value = value;
		return true;
	}

	public String getLastString() {
		return _lastString;
	}

	public boolean setLastString(String lastString) {
		_lastString = lastString;
		return true;
	}

	public RuleTemplate getPfRule() {
		return _pfRule;
	}

	public boolean setPfRule(RuleTemplate pfRule) {
		_pfRule = pfRule;
		return true;
	}

	public TableTemplate getPfTable() {
		return _pfTable;
	}

	public boolean setPfTable(TableTemplate pfTable) {
		_pfTable = pfTable;
		return true;
	}

	public AnchorTemplate getPfAnchor() {
		return _pfAnchor;
	}

	public boolean setPfAnchor(AnchorTemplate pfAnchor) {
		_pfAnchor = pfAnchor;
		return true;
	}

	public StringBuilder getCurExpandedContext() {
		return _curExpandedContext;
	}

	public boolean setCurExpandedContext(StringBuilder curExpandedContext) {
		_curExpandedContext = curExpandedContext;
		return true;
	}

	public boolean getPfnot() {
		return _pfnot;
	}

	public boolean setPfnot(boolean pfnot) {
		_pfnot = pfnot;
		return true;
	}

	public String getIfItem() {
		return _ifItem;
	}

	public boolean setIfItem(String ifItem) {
		_ifItem = ifItem;
		return true;
	}

	public String getPfStateOptItem() {
		return _pfStateOptItem;
	}

	public boolean setPfStateOptItem(String state) {
		_pfStateOptItem = state;
		return true;
	}

	public String getPfPQname() {
		return _pfPQname;
	}

	public boolean setPfPQname(String pfPQname) {
		_pfPQname = pfPQname;
		return true;
	}

	public String getPfQname() {
		return _pfQname;
	}

	public boolean setPfQname(String pfQname) {
		_pfQname = pfQname;
		return true;
	}

	public String getIfname() {
		return _ifname;
	}

	public boolean setIfname(String ifname) {
		_ifname = ifname;
		return true;
	}

	public String getPfString() {
		return _pfString;
	}

	public boolean setPfString(String pfString) {
		_pfString = pfString;
		return true;
	}

	public static int untilSpecials(String string) {

		int i = 0;
		for (i = 0; i < string.length(); i++) {
			if (SPECIALS.indexOf(string.charAt(i)) >= 0)
				break;
		}
		return i;
	}

	private List<StringBuilder> expandLine(StringBuilder line, Map<String, String> symbols) {

		StringBuilder sb = new StringBuilder("");
		int length = line.length();

		for (int i = 0; i < line.length(); ) {
			char c = line.charAt(i);
			/*
			 * string
			 */
			if (_quoted) {
				sb.append(c);
				i++;
				if (c == _quotec)
					_quoted = false;
				continue;
			}
			/*
			 * end of string
			 */
			if (c == '\'' || c == '"') {
				sb.append(c);
				_quotec = c;
				_quoted = true;
				i++;
				continue;
			}
			/*
			 * comment
			 */
			if (c == '#') {
				sb.append('\n');
				break;
			}

			/*
			 * macro expansion
			 */
			if (c == '$') {
				StringBuilder sym = new StringBuilder("");
				for (i++; i < length; ) {
					c = line.charAt(i);
					if (IDENT.indexOf(c) >= 0) {
						sym.append(c);
						i++;
					} else
						break;
				}
				if (sym.length() == 0)
					throw new JtaclConfigurationException("empty macro");
				String value = symbols.get(sym.toString());
				if (value == null)
					throw new JtaclConfigurationException("unknown macro " + sym);
				sb.append(value);
				continue;
			}

			/*
			 * other
			 */
			sb.append(c);
			i++;
		}
		List<StringBuilder> slist = new ArrayList<StringBuilder>();
		StringBuilder sline = new StringBuilder("");
		slist.add(sline);
		for (int i = 0; i < sb.length(); i ++) {
			char c = sb.charAt(i);
			sline.append(c);
			_curExpandedContext.append(c);
			if (c == '\n' && i < sb.length() - 1) {
				sline = new StringBuilder("");
				slist.add(sline);
			}
		}
		return slist;
	}

	private StringBuilder getLine(StringBuilder buffer, int index) {
		String line;
		int p = buffer.indexOf("\n", index);
		if (p >= 0)
			line = buffer.substring(index, p + 1);
		else
			line = buffer.substring(index);
		return new StringBuilder(line);
	}

	private int _charRuleLineRead;
	private int _lineRuleLineRead;
	private void getRuleLine(ExpandedRule exRule, StringBuilder buffer, int index,
			Map<String, String> symbols) {

		StringBuilder line;
		StringBuilder rule;
		_quoted = false;
		_charRuleLineRead = 0;
		_lineRuleLineRead = 0;
		for (;;) {
			ExpandedRuleLine exLine = new ExpandedRuleLine();
			exRule.add(exLine);
			_lineRuleLineRead++;
			line = new StringBuilder(getLine(buffer, index));
			exLine.setLine(line);
			index = index + line.length();
			_charRuleLineRead += line.length();
			exLine.getExpanded().addAll(expandLine(line, symbols));
			if (index >= buffer.length())
				break;
			if (_quoted)
				continue;
			/*
			 * test if the line continue ('\'\n)
			 */
			rule = exRule.get(exRule.size() - 1).getLine();
			int p = rule.lastIndexOf("\\\n");
			if ( p >= 0 && p == rule.length() - 2) {
				continue;
			}
			break;
		}
		if (_quoted)
			throw new JtaclConfigurationException("unterminated string");
	}

	/*
	 * Parse Runners for getRule
	 */
	protected BasicParseRunner _parseRunIsClosingBrace =
			new BasicParseRunner(IsClosingBrace());

	protected BasicParseRunner _parseRunIsNewRule =
			new BasicParseRunner(IsNewRule());

	public ExpandedRule getRule(StringBuilder buffer, Map<String, String> symbols) {

		ParsingResult<?> result;
		int lindex = 0;
		int blength = buffer.length();
		int lineRead = 0;
		/*
		 * begining of the rule
		 */
		ExpandedRule exRule = new ExpandedRule();
		_curExpandedContext = new StringBuilder();
		getRuleLine(exRule, buffer, lindex, symbols);
		lindex += _charRuleLineRead;
		lineRead += _lineRuleLineRead;

		/*
		 * brace '}\n'
		 */
		result = _parseRunIsClosingBrace.run(exRule.expandedToString());
		if (!result.matched) {
			/*
			 * find the next rule.
			 */
			boolean nextFound = false;
			ExpandedRule nextRule = new ExpandedRule();
			while (lindex < blength) {
				nextRule.clear();
				try {
					getRuleLine(nextRule, buffer, lindex, symbols);
				} catch (JtaclConfigurationException ex) {
					nextFound = true;
				}
				if (!nextFound) {
					result = _parseRunIsNewRule.run(nextRule.expandedToString());
					nextFound = result.matched;
				}
				if (nextFound)
					break;
				exRule.addAll(nextRule);
				lindex += _charRuleLineRead;
				lineRead += _lineRuleLineRead;
			}
		}
		return exRule;
 	}

	/**
	 * Resets the resulting values of the parsing to null.
	 */
	protected boolean clear() {
		_ruleName = null;
		_name = null;
		_value = null;
		_lastString = null;
		_ifItem = null;
		_pfRule = null;
		_pfTable = null;
		_pfAnchor = null;
		return true;
	}

	/**
	 * Returns true if the line in argument should match a rule.
	 * @param line
	 * @return true if the line in argument should match a rule.
	 */
	public boolean shouldMatch(String line) {
		String [] should = {
			"pass ",
			"block ",
			"match ",
			"include ",
			"load ",
			"table ",
			"anchor ",
			"set skip"
		};

		String nline = line.trim();
		for (String s: should) {
			if (nline.startsWith(s))
				return true;
		}
		return false;
	}


	/**
	 * Matches the begining of a rule
	 * @return a Rule
	 */
	@SuppressSubnodes
	public Rule IsNewRule() {
		return
			Sequence(
				SkipSpaces(),
				FirstOf(
					INCLUDE(),
					SET(),
					PASS(),
					MATCH(),
					BLOCK(),
					ANCHOR(),
					LOAD(),
					ALTQ(),
					QUEUE(),
					ANTISPOOF(),
					TABLE(),
					PfMacro()
				)
			);
	}

	/**
	 * Matches '}\n'
	 * @return a Rule
	 */
	public Rule IsClosingBrace() {
		return
			Sequence(
				SkipSpaces(),
				Ch('}'),
				SkipSpaces(),
				FirstOf(
					Ch('\n'),
					EOI
				)
			);
	}

	/**
	 * Generic PF syntax rule
	 *
	 * @return a rule
	 */
	@SuppressSubnodes
	public Rule PfGenericRule() {
		return
			Sequence(
				Sequence(
					SkipSpaces(),
					ZeroOrMore(
						Sequence(
							SkipSpaces(),
							FirstOf(
								PfGenericList(),
								PfGenericRuleItem()
							)
						)
					),
					SkipSpaces(),
					FirstOf(
						EOI,
						Eol()
					)
				),
				/*
				 * text matched
				 */
				setMatchedText(match())
			);
	}

	public Rule PfGenericRuleItem() {
		return
			Sequence(
				TestNot(
					PfGenericSpecial()
				),
				FirstOf(
					PfQuotedString(),
					PfGenericAtom()
				)
			);
	}

	public Rule PfGenericList() {
		return
			FirstOf(
				Sequence(
					Ch('{'),
					PfOptnl(),
					Ch('}')
				),
				Sequence(
					Ch('{'),
					PfOptnl(),
					PfGenericListItem(),
					Ch('}')
				)
			);
	}

	public Rule PfGenericListItem() {
		return
			Sequence(
				TestNot(
					Ch('}')
				),
				SkipSpaces(),
				PfGenericRuleItem(),
				PfOptnl(),
				Optional(
					PfGenericListItem()
				)
			);
	}

	public Rule PfGenericSpecial() {
		return
			FirstOf(
				Eol(),
				AnyOf("{}")
				);
	}

	public Rule PfGenericAtom() {
		return OneOrMore(
					Sequence(
						TestNot(
							WhiteSpaces()
						),
						TestNot(
							PfGenericSpecial()
						),
						ANY
					)
				);
	}

	/**
	 * Main rule
	 * @return a {@link Rule}
	 */
	@SuppressSubnodes
	public Rule Parse() {
		return
			Sequence(
				clear(),
				Sequence(
					SkipSpaces(),
					FirstOf(
						PfMacro(),
						PfOption(),
						PfInclude(),
						PfRule(),
						PfTableDef(),
						PfLoadRule(),
						PfAnchorRule(),
						PfClosingBrace()
					),
					FirstOf(
						EOI,
						Ch('\n')
					)
				),
				/*
				 * text matched
				 */
				setMatchedText(match())
			);
	}

	/**
	 * Matches '}'
	 * @return a Rule
	 */
	public Rule PfClosingBrace() {
		return
			Sequence(
				SkipSpaces(),
				Ch('}'),
				SkipSpaces(),
				setRuleName("closing brace")
			);
	}

	/**
	 * Matches a macro
	 * @return a Rule.
	 */
	public Rule PfMacro() {
		return
			Sequence(
				PfString(),
				setName(_pfString),
				macroValuesClear(),
				SkipSpaces(),
				String("="),
				SkipSpaces(),
				PfMacroValue(),
				macroValuesToValue(),
				setRuleName("macro")
			);
	}

	/**
	 * macro value
	 * macrovalue : STRING
	 *   | STRING macrovalue
	 *
	 * @return a Rule
	 */
	public Rule PfMacroValue() {
		return
			Sequence(
				PfString(),
				_macroValues.add(_pfString),
				Optional(
					Sequence(
						WhiteSpaces(),
						PfMacroValue()
					)
				)
			);
	}

	/**
	 * Matches not
	 * not : '!'
	 *	| empty
	 *
	 * @return a Rule
	 */
	public Rule PfNot() {
		return
			Sequence(
				setPfnot(false),
				Optional(
					Sequence(
						Ch('!'),
						setPfnot(true)
					)
				)
			);
	}

	/**
	 * Matches tabledef
	 * tabledef	: TABLE '<' STRING '>' table_opts
	 *
	 * @return a Rule
	 */
	public Rule PfTableDef() {
		return
			Sequence(
				TABLE(),
				newTableTemplate(),
				SkipSpaces(),
				Ch('<'),
				SkipSpaces(),
				PfString(),
				_pfTable.setName(_pfString),
				SkipSpaces(),
				Ch('>'),
				SkipSpaces(),
				PfTableOpts(),
				setRuleName("tabledef")
			);
	}

	/**
	 * Matches table_opts
	 * table_opts : table_opts_l
	 *  | empty
	 *
	 * @return a Rule
	 */
	public Rule PfTableOpts() {
		return
			Optional(
				PfTableOptsL()
			);
	}

	/**
	 * Matches table_opts_l
	 * table_opts_l : table_opts_l table_opt
	 *  | table_opt
	 */
	public Rule PfTableOptsL() {
		return
			Sequence(
				PfTableOpt(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfTableOptsL()
					)
				)
			);
	}

	/**
	 * Matches table_opt
	 * table_opt : STRING
	 *	| '{' optnl '}'
	 *	| '{' optnl host_list '}'
	 *	| FILENAME STRING
	 *
	 * @return a Rule
	 */
	public Rule PfTableOpt() {
		return
			Sequence(
				ipSpecClear(),
				FirstOf(
					/*
					 * FILENAME STRING
					 */
					Sequence(
						FILENAME(),
						WhiteSpaces(),
						PfString(),
						_pfTable.getFileNames().add(_pfString)
					),
					/*
					 * '{' optnl '}'
					 */
					Sequence(
						Ch('{'),
						PfOptnl(),
						Ch('}')
					),
					/*
					 * '{' optnl host_list '}'
					 */
					Sequence(
						Ch('{'),
						PfOptnl(),
						PfHostList(),
						SkipSpaces(),
						Ch('}'),
						_pfTable.getHosts().addAll(_ipspec)
					),
					/*
					 * STRING
					 */
					Sequence(
						PfString(),
						_pfTable.getOptions().add(_pfString)
					)
				)
			);
	}

	/**
	 * Matches include rule
	 * include	: INCLUDE STRING
	 *
	 * @return a Rule.
	 */
	public Rule PfInclude() {
		return
			Sequence(
				INCLUDE(),
				WhiteSpaces(),
				PfString(),
				setValue(_pfString),
				setRuleName("include"),
				UntilEOI()
			);
	}

	/**
	 * Matches option: set skip interface
	 *
	 * @return a Rule
	 */
	public Rule PfOption() {
		return
			/*
			 * set skip interface
			 */
			Sequence(
				SET(),
				WhiteSpace(),
				SKIP(),
				WhiteSpace(),
				newRuleTemplate(),
				PfInterface(),
				setRuleName("option set skip")
			);
	}

	/**
	 * Matches anchorrule
	 * anchorrule : ANCHOR anchorname dir quick interface af proto fromto
	 *	    filter_opts pfa_anchor
	 *
	 * @return a Rule
	 */
	public Rule PfAnchorRule() {
		return
			Sequence(
				/*
				 * ANCHOR
				 */
				ANCHOR(),
				newAnchorTemplate(),
				newRuleTemplate(),
				_pfAnchor.setRule(_pfRule),
				/*
				 * [anchorname]
				 */
				Optional(
					Sequence(
						WhiteSpaces(),
						PfString(),
						_pfAnchor.setName(_pfString)
					)
				),
				SkipSpaces(),
				/*
				 * dir
				 */
				Optional(
					Sequence(
						PfDir(),
						_pfRule.setDir(match())
					)
				),
				SkipSpaces(),
				/*
				 * quick
				 */
				Optional(
					PfQuick()
				),
				SkipSpaces(),
				/*
				 * interface
				 */
				Optional(
					PfInterface()
				),
				SkipSpaces(),
				/*
				 * af
				 */
				Optional(
					PfAf()
				),
				SkipSpaces(),
				/*
				 * proto
				 */
				Optional(
					PfProto()
				),
				SkipSpaces(),
				/*
				 * fromto
				 */
				Optional(
					PfFromTo()
				),
				SkipSpaces(),
				/*
				 * filteropts
				 */
				Optional(
					PfFilterOpts()
				),
				SkipSpaces(),
				/*
				 * '{'
				 */
				Optional(
					Sequence(
						Ch('{'),
						_pfAnchor.setInlined(true),
						SkipSpaces()
					)
				),
				setRuleName("anchorrule")
			);
	}

	/**
	 * Matches loadrule
	 * loadrule : LOAD ANCHOR string FROM string
	 *
	 * @return a Rule
	 */
	public Rule PfLoadRule() {
		return
			Sequence(
				LOAD(),
				WhiteSpaces(),
				ANCHOR(),
				WhiteSpaces(),
				PfString(),
				setName(_pfString),
				WhiteSpaces(),
				FROM(),
				WhiteSpaces(),
				PfString(),
				setValue(_pfString),
				setRuleName("loadrule")
			);
	}

	/**
	 * Matches scrub_opts
	 * scrub_opts :	scrub_opts_l
	 *
	 * @return a Rule
	 */
	public Rule PfScrubOpts() {
		return
			Sequence(
				newScrubOptsTemplate(),
				PfScrubOptsL()
			);
	}

	/**
	 * Matches scrub_opts_l
	 * scrub_opts_l : scrub_opts_l comma scrub_opt
	 *  | scrub_opt
	 *
	 * @return a Rule
	 */
	public Rule PfScrubOptsL() {
		return
			Sequence(
				PfScrubOpt(),
				SkipSpaces(),
				Optional(
					Sequence(
						PfComma(),
						SkipSpaces(),
						PfScrubOptsL()
					)
				)
			);
	}

	/**
	 * Matches scrub_opt
	 * scrub_opt : NODF
	 *	| MINTTL NUMBER
	 *	| MAXMSS NUMBER
	 *	| SETTOS tos
	 *	| REASSEMBLE STRING
	 *	| RANDOMID
	 *
	 * @return a Rule
	 */
	public Rule PfScrubOpt() {
		return
			FirstOf(
				/*
				 * NODF
				 */
				Sequence(
					NODF(),
					_scrubOpts.setNodf(true)
				),
				/*
				 * MINTTL NUMBER
				 */
				Sequence(
					MINTTL(),
					WhiteSpaces(),
					PfString(),
					_scrubOpts.setMinttl(_pfString)
				),
				/*
				 * MAXMSS NUMBER
				 */
				Sequence(
					MAXMSS(),
					WhiteSpaces(),
					PfString(),
					_scrubOpts.setMaxmss(_pfString)
				),
				/*
				 * SETTOS tos
				 */
				Sequence(
					SETTOS(),
					WhiteSpaces(),
					PfTos(),
					_scrubOpts.setSettos(_pfString)
				),
				/*
				 * REASSEMBLE STRING
				 */
				Sequence(
					REASSEMBLE(),
					WhiteSpaces(),
					/*
					 * scrub reassemble supports only tcp
					 */
					String("tcp"),
					_scrubOpts.setReassemble_tcp(true)
				),
				/*
				 * RANDOMID
				 */
				Sequence(
					RANDOMID(),
					_scrubOpts.setRandomid(true)
				)
			);
	}


	/**
	 * Matches a filter rules
	 * pfrule : action dir logquick interface af proto fromto filter_opts
	 *
	 * @return a Rule.
	 */
	public Rule PfRule() {
		return
			Sequence(
				newRuleTemplate(),
				/*
				 * action
				 */
				PfAction(),
				SkipSpaces(),
				/*
				 * dir
				 */
				Optional(
					Sequence(
						PfDir(),
						_pfRule.setDir(match())
					)
				),
				SkipSpaces(),
				/*
				 * logquick
				 */
				Optional(
					PfLogQuick()
				),
				SkipSpaces(),
				/*
				 * interface
				 */
				Optional(
					PfInterface()
				),
				SkipSpaces(),
				/*
				 * af
				 */
				Optional(
					PfAf()
				),
				SkipSpaces(),
				/*
				 * proto
				 */
				Optional(
					PfProto()
				),
				SkipSpaces(),
				/*
				 * fromto
				 */
				Optional(
					PfFromTo()
				),
				SkipSpaces(),
				/*
				 * filteropts
				 */
				Optional(
					PfFilterOpts()
				),
				setRuleName("pfrule")
			);
	}

	/**
	 * Matches filter_opts
	 * filter_opts : filter_opts_l
	 * 	| empty
	 *
	 * @return a Rule
	 */
	public Rule PfFilterOpts() {
		return
			Sequence(
				icmpSpecClear(),
				icmp6SpecClear(),
				setFlags(null),
				setFlagset(null),
				stateOptSpecClear(),
				PfFilterOptsl(),
				pfRuleSetFilterOpts()
			);
	}

	/**
	 * Matches filter_opts_l
	 * filter_opts_l : filter_opts_l filter_opt
	 *	| filter_opt
	 */
	public Rule PfFilterOptsl() {
		return
			Sequence(
				SkipSpaces(),
				PfFilterOpt(),
				SkipSpaces(),
				Optional(
					PfFilterOptsl()
				)
			);
	}

	/**
	 * Matches filter_opt
	 * filter_opt : USER uids
	 *	| GROUP gids
	 *	| flags
	 * 	| icmpspec
	 *	| TOS tos
	 *	| keep
	 *	| FRAGMENT
	 *	| ALLOWOPTS
	 *	| LABEL label
	 *	| QUEUE qname
	 *	| TAG string
	 *	| not TAGGED string
	 *	| PROBABILITY probability
	 *	| RTABLE NUMBER
	 *	| DIVERTTO STRING PORT portplain
	 *	| DIVERTREPLY
	 *	| DIVERTPACKET PORT number
	 * 	| SCRUB '(' scrub_opts ')'
	 *  | NATTO redirpool pool_opts
	 *	| RDRTO redirpool pool_opts
	 *	| BINATTO redirpool pool_opts
	 *	| FASTROUTE
	 *	| ROUTETO routespec pool_opts
	 *	| REPLYTO routespec pool_opts
	 *	| DUPTO routespec pool_opts
	 *	| RECEIVEDON if_item
	 *
	 * @return a Rule
	 */
	public Rule PfFilterOpt() {
		return
			FirstOf(
				/*
				 * USER uids
				 */
				Sequence(
					USER(),
					SkipSpaces(),
					PfUids(),
					SkipSpaces()
				),
				/*
				 * GROUP gid
				 */
				Sequence(
					GROUP(),
					SkipSpaces(),
					PfGids(),
					SkipSpaces()
				),
				/*
				 * flags
				 */
				Sequence(
					PfFlags(),
					SkipSpaces()
				),
				/*
				 * icmpspec
				 */
				Sequence(
					PfIcmpSpec(),
					SkipSpaces()
				),
				/*
				 * TOS tos
				 */
				Sequence(
					TOS(),
					SkipSpaces(),
					PfTos(),
					_pfRule.getFilterOpts().setTos(_pfString),
					SkipSpaces()
				),
				/*
				 * keep
				 */
				Sequence(
					PfKeep(),
					SkipSpaces()
				),
				/*
				 * FRAGMENT
				 */
				Sequence(
					FRAGMENT(),
					SkipSpaces(),
					_pfRule.getFilterOpts().setFragment(true)
				),
				/*
				 * ALLOWOPTS
				 */
				Sequence(
					ALLOWOPTS(),
					SkipSpaces(),
					_pfRule.getFilterOpts().setAllowopts(true)
				),
				/*
				 * LABEL label
				 */
				Sequence(
					LABEL(),
					WhiteSpaces(),
					PfLabel(),
					SkipSpaces(),
					_pfRule.getFilterOpts().setLabel(_pfString)
				),
				/*
				 * QUEUE qname
				 */
				Sequence(
					QUEUE(),
					WhiteSpaces(),
					PfQname(),
					_pfRule.getFilterOpts().setQname(_pfQname),
					_pfRule.getFilterOpts().setPQname(_pfPQname)
				),
				/*
				 * TAG string
				 */
				Sequence(
					TAG(),
					WhiteSpaces(),
					PfString(),
					_pfRule.getFilterOpts().setTag(_pfString)
				),
				/*
				 * not TAGGED string
				 */
				Sequence(
					PfNot(),
					SkipSpaces(),
					TAGGED(),
					WhiteSpaces(),
					PfString(),
					_pfRule.getFilterOpts().setMatchTagNot(_pfnot),
					_pfRule.getFilterOpts().setMatchTag(_pfString)
				),
				/*
				 * PROBABILITY probability
				 */
				Sequence(
					PROBABILITY(),
					WhiteSpaces(),
					PfProbability(),
					_pfRule.getFilterOpts().setProbability(_pfString)
				),
				/*
				 * RTABLE NUMBER
				 */
				Sequence(
					RTABLE(),
					WhiteSpaces(),
					PfString(),
					_pfRule.getFilterOpts().setRtableId(_pfString)
				),
				/*
				 * DIVERTTO STRING PORT portplain
				 */
				Sequence(
					DIVERTTO(),
					WhiteSpaces(),
					PfString(),
					_pfRule.getFilterOpts().setDivertAddr(_pfString),
					WhiteSpaces(),
					PORT(),
					WhiteSpaces(),
					PfPortPlain(),
					_pfRule.getFilterOpts().setDivertPort(_pfString)
				),
				/*
				 * DIVERTREPLY
				 */
				Sequence(
					DIVERTREPLY(),
					_pfRule.getFilterOpts().setDivertPort("1")
				),
				/*
				 * DIVERTPACKET PORT number
				 */
				Sequence(
					DIVERTPACKET(),
					WhiteSpaces(),
					PORT(),
					WhiteSpaces(),
					PfString(),
					_pfRule.getFilterOpts().setDivertPacketPort(_pfString)
				),
				/*
				 * SCRUB '(' scrub_opts ')'
				 */
				Sequence(
					SCRUB(),
					SkipSpaces(),
					Ch('('),
					SkipSpaces(),
					PfScrubOpts(),
					SkipSpaces(),
					Ch(')'),
					_pfRule.getFilterOpts().setScrubOpts(_scrubOpts)
				),
				/*
				 * NATTO redirpool pool_opts
				 */
				Sequence(
					NATTO(),
					WhiteSpaces(),
					PfRedirPool(),
					_pfRule.getFilterOpts().setNat(_redirSpec),
					Optional(
						Sequence(
							SkipSpaces(),
							PfPoolOpts(),
							_redirSpec.setPoolOpts(_poolOpts)
						)
					)
				),
				/*
				 * RDRTO redirpool pool_opts
				 */
				Sequence(
					RDRTO(),
					WhiteSpaces(),
					PfRedirPool(),
					_pfRule.getFilterOpts().setRdr(_redirSpec),
					Optional(
						Sequence(
							SkipSpaces(),
							PfPoolOpts(),
							_redirSpec.setPoolOpts(_poolOpts)
						)
					)
				),
				/*
				 * BINATTO redirpool pool_opts
				 */
				Sequence(
					BINATTO(),
					WhiteSpaces(),
					PfRedirPool(),
					_pfRule.getFilterOpts().setNat(_redirSpec),
					_pfRule.getFilterOpts().setBinat(true),
					Optional(
						Sequence(
							SkipSpaces(),
							PfPoolOpts(),
							_redirSpec.setPoolOpts(_poolOpts),
							_poolOpts.setStaticPort(true)
						)
					)
				),
				/*
				 * FASTROUTE
				 */
				Sequence(
					FASTROUTE(),
					newRouteOpts(),
					_routeOpts.setRt(PfConst.PF_FASTROUTE),
					_pfRule.getFilterOpts().setRouteOpts(_routeOpts)
				),
				/*
				 * ROUTETO routespec pool_opts
				 */
				Sequence(
					ROUTETO(),
					WhiteSpaces(),
					PfRouteSpec(),
					SkipSpaces(),
					Optional(
						PfPoolOpts()
					),
					_routeOpts.setRt(PfConst.PF_ROUTETO),
					_routeOpts.setPoolOpts(_poolOpts),
					_pfRule.getFilterOpts().setRouteOpts(_routeOpts)
				),
				/*
				 * REPLYTO routespec pool_opts
				 */
				Sequence(
					REPLYTO(),
					WhiteSpaces(),
					PfRouteSpec(),
					SkipSpaces(),
					Optional(
						PfPoolOpts()
					),
					_routeOpts.setRt(PfConst.PF_REPLYTO),
					_routeOpts.setPoolOpts(_poolOpts),
					_pfRule.getFilterOpts().setRouteOpts(_routeOpts)
				),
				/*
				 * DUPTO routespec pool_opts
				 */
				Sequence(
					DUPTO(),
					WhiteSpaces(),
					PfRouteSpec(),
					SkipSpaces(),
					Optional(
						PfPoolOpts()
					),
					_routeOpts.setRt(PfConst.PF_DUPTO),
					_routeOpts.setPoolOpts(_poolOpts),
					_pfRule.getFilterOpts().setRouteOpts(_routeOpts)
				),
				/*
				 * RECEIVEDON if_item
				 */
				Sequence(
					RECEIVEDON(),
					WhiteSpaces(),
					PfIfItem(),
					_pfRule.getFilterOpts().setRcv(_pfString)
				)
			);
	}

	/**
	 * Matches probability
	 * probability : STRING
	 *	| NUMBER
	 *
	 * @return a Rule
	 */
	public Rule PfProbability() {
		return PfString();
	}

	/**
	 * Matches action rule
	 * action : PASS
	 *	| MATCH
	 *	| BLOCK blockspec
	 * @return a Rule.
	 */
	public Rule PfAction() {
		return
			FirstOf(
				/*
				 * PASS
				 */
				Sequence(
					PASS(),
					_pfRule.setAction("pass")
				),
				/*
				 * MATCH
				 */
				Sequence(
					MATCH(),
					_pfRule.setAction("match")
				),
				/*
				 * BLOCK blockspec
				 */
				Sequence(
					BLOCK(),
					Optional(
						Sequence(
							SkipSpaces(),
							PfBlockSpec()
						)
					),
					_pfRule.setAction("block")
				)
			);
	}

	/**
	 * Matches blockspec
	 * blockspec : empty
	 * 	| DROP
	 *	| RETURNRST
	 *	| RETURNRST '(' TTL NUMBER ')'
	 *	| RETURNICMP
	 * 	| RETURNICMP6
	 *	| RETURNICMP '(' reticmpspec ')'
	 *	| RETURNICMP6 '(' reticmp6spec ')'
	 *	| RETURNICMP '(' reticmpspec comma reticmp6spec ')'
	 *	| RETURN
	 *
	 * @return a Rule.
	 */
	public Rule PfBlockSpec() {
		return
			FirstOf(
				/*
				 * DROP
				 */
				DROP(),
				/*
				 * RETURNRST '(' TTL NUMBER ')'
				 */
				Sequence(
					RETURNRST(),
					SkipSpaces(),
					Ch('('),
					SkipSpaces(),
					TTL(),
					WhiteSpaces(),
					Number(),
					SkipSpaces(),
					Ch(')')
				),
				/*
				 * RETURNRST
				 */
				RETURNRST(),
				/*
				 * RETURNICMP '(' reticmpspec comma reticmp6spec ')'
				 */
				Sequence(
					RETURNICMP(),
					SkipSpaces(),
					Ch('('),
					SkipSpaces(),
					PfRetIcmpSpec(),
					SkipSpaces(),
					PfComma(),
					SkipSpaces(),
					PfRetIcmp6Spec(),
					SkipSpaces(),
					Ch(')')
				),
				/*
				 * RETURNICMP '(' reticmpspec ')'
				 */
				Sequence(
					RETURNICMP(),
					SkipSpaces(),
					Ch('('),
					SkipSpaces(),
					PfRetIcmpSpec(),
					SkipSpaces(),
					Ch(')')
				),
				/*
				 * RETURNICMP
				 */
				RETURNICMP(),
				/*
				 * RETURNICMP6 '(' reticmp6spec ')'
				 */
				Sequence(
					RETURNICMP6(),
					SkipSpaces(),
					Ch('('),
					SkipSpaces(),
					PfRetIcmp6Spec(),
					SkipSpaces(),
					Ch(')')
				),
				/*
				 * RETURNICMP6
				 */
				RETURNICMP6(),
				/*
				 * RETURN
				 */
				RETURN()
			);
	}

	/**
	 * Matches reticmpspec
	 *	reticmpspec : STRING
	 *	| NUMBER
	 *
	 * @return a Rule
	 */
	public Rule PfRetIcmpSpec() {
		return PfString();
	}

	/**
	 * Matches reticmp6spec
	 *	reticmp6spec : STRING
	 *	| NUMBER
	 *
	 * @return a Rule
	 */
	public Rule PfRetIcmp6Spec() {
		return PfString();
	}

	/**
	 * Matches dir
	 * dir	: empty
	 *	| IN
	 *	| OUT
     *
	 * @return a Rule.
	 */
	public Rule PfDir() {
		return
			FirstOf(
				IN(),
				OUT()
			);
	}

	/**
	 * Matches quick
	 * 	quick : empty
	 *	| QUICK
	 *
	 * @return a Rule
	 */
	public Rule PfQuick() {
		return
			Sequence(
				QUICK(),
				_pfRule.setQuick(true)
			);
	}

	/**
	 * Matches logquick
	 *	logquick : empty
	 *	| log
	 *	| QUICK
	 *	| log QUICK
	 *	| QUICK log
	 *
	 * @return a Rule.
	 */
	public Rule PfLogQuick() {
		return
			FirstOf(
				Sequence(
					PfLog(),
					WhiteSpaces(),
					PfQuick()
				),
				Sequence(
					PfQuick(),
					WhiteSpaces(),
					PfLog()
				),
				PfQuick(),
				PfLog()
			);
	}


	/**
	 * Matches log
	 *	log	: LOG
	 *	| LOG '(' logopts ')'
	 *
	 * @return a Rule
	 */
	public Rule PfLog() {
		return
			FirstOf(
				Sequence(
					LOG(),
					SkipSpaces(),
					Ch('('),
					SkipSpaces(),
					PfLogOpts(),
					SkipSpaces(),
					Ch(')')
				),
				LOG()
			);
	}

	/**
	 * Matches logopts
	 * logopts	: logopt
	 * | logopts comma logopt
	 *
	 * @return a Rule
	 */
	public Rule PfLogOpts() {
		return
			FirstOf(
				PfLogOpt(),
				Sequence(
					PfLogOpts(),
					SkipSpaces(),
					PfComma(),
					SkipSpaces(),
					PfLogOpt()
				)
			);
	}

	/**
	 * Matches logopt
	 *	logopt : ALL
	 *	| USER
     *	| GROUP
 	 *	| TO string
	 *
	 * @return a Rule
	 */
	public Rule PfLogOpt() {
		return
			FirstOf(
				ALL(),
				USER(),
				GROUP(),
				Sequence(
					TO(),
					WhiteSpaces(),
					PfAtom()
				)
			);
	}

	/**
	 * Matches interface
	 * interface : empty
	 *	| ON if_item_not
	 *	| ON '{' optnl if_list '}'
	 *
	 * @return a Rule.
	 */
	public Rule PfInterface() {
		return
			Sequence(
				ON(),
				WhiteSpaces(),
				FirstOf(
					PfIfItemNot(),
					Sequence(
						Ch('{'),
						PfOptnl(),
						PfIfList(),
						SkipSpaces(),
						Ch('}')
					)
				)
			);
	}

	/**
	 * Matches if_list
	 * if_list : if_item_not optnl
	 *	| if_list comma if_item_not optnl
	 *
	 * @return a Rule
	 */
	public Rule PfIfList() {
		return
			Sequence(
				PfIfItemNot(),
				SkipSpaces(),
				PfOptnl(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfComma(),
						SkipSpaces(),
						PfIfList()
					)
				)
		 );
	}

	/**
	 * Matches if_item_not
	 * if_item_not	: not if_item
	 *
	 * @return a Rule
	 */
	public Rule PfIfItemNot() {
		return
			Sequence(
				PfNot(),
				SkipSpaces(),
				PfIfItem(),
				pfRuleSetIfItem(_pfnot, _ifItem)
			);
	}

	/**
	 * Matches if_item
	 * if_item	: STRING
	 *
	 * @return a Rule
	 */
	public Rule PfIfItem() {
		return
			Sequence(
				PfString(),
				setIfItem(_pfString)
			);
	}

	/**
	 * Matches af
	 * af : empty
	 *	| INET
	 *	| INET6
	 *
	 * @return a Rule
	 */
	public Rule PfAf() {
		return
			Sequence(
				FirstOf(
					INET6(),
					INET()
				),
				_pfRule.setAf(match())
			);
	}

	/**
	 * Matches proto
	 * proto : empty
	 *	| PROTO proto_item
	 *	| PROTO '{' optnl proto_list '}'
	 *
	 * @return a Rule.
	 */
	public Rule PfProto() {
		return
			Sequence(
				PROTO(),
				WhiteSpaces(),
				FirstOf(
					PfProtoItem(),
					Sequence(
						Ch('{'),
						PfOptnl(),
						PfProtoList(),
						SkipSpaces(),
						Ch('}')
					)
				)
			);
	}

	/**
	 * Matches proto_list
	 * proto_list : proto_item optnl
	 *	| proto_list comma proto_item optnl
	 *
	 * @return a Rule
	 */
	public Rule PfProtoList() {
		return
			Sequence(
				PfProtoItem(),
				PfOptnl(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfComma(),
						SkipSpaces(),
						PfProtoList()
					)
				)
		 );
	}

	/**
	 * Matches proto_item
	 * proto_item	: protoval
	 *
	 * @return a Rule
	 */
	public Rule PfProtoItem() {
		return
			Sequence(
				PfString(),
				_pfRule.getProtoList().add(_pfString)
			);
	}

	/**
	 * Matches fromto
	 * fromto : ALL
	 *	| from os to
	 *
	 * @return a Rule
	 */
	public Rule PfFromTo() {
		return
			FirstOf(
				Sequence(
					ALL(),
					_pfRule.setAll(true)
				),
				/*
				 * from os to
				 */
				Sequence(
					Optional(
						PfFrom()
					),
					SkipSpaces(),
					Optional(
						PfOs()
					),
					SkipSpaces(),
					Optional(
						PfTo()
					)
				)
			);
	}

	/**
	 * Matches os
	 * os : empty
	 *	| OS xos
	 *	| OS '{' optnl os_list '}'
	 *
	 * @return a Rule
	 */
	public Rule PfOs() {
		return
			Sequence(
				OS(),
				WhiteSpaces(),
				FirstOf(
					PfXOs(),
					Sequence(
						Ch('{'),
						PfOptnl(),
						PfOsList(),
						SkipSpaces(),
						Ch('}')
					)
				)
			);
	}

	/**
	 * Matches xos
	 * xos : STRING
	 *
	 * @return a Rule
	 */
	public Rule PfXOs() {
		return
			Sequence(
				PfString(),
				_pfRule.getOsList().add(_pfString)
			);
	}


	/**
	 * Matches os_list
	 * os_list : xos optnl
	 *	| os_list comma xos optnl
	 *
	 * @return a Rule
	 */
	public Rule PfOsList() {
		return
			Sequence(
				PfXOs(),
				PfOptnl(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfComma(),
						SkipSpaces(),
						PfOsList()
					)
				)
		 );
	}


	/**
	 * Matches from
	 * from	: empty
	 *	| FROM ipportspec
	 *
	 * @return a Rule
	 */
	public Rule PfFrom() {
		return
			Sequence(
				FROM(),
				SkipSpaces(),
				PfIpPortSpec(),
				_pfRule.addSourceHost(_ipspec),
				_pfRule.addSourcePort(_portspec)
			);
	}

	/**
	 * Matches to
	 *  to	: empty
	 *	| TO ipportspec
	 *
	 * @return a Rule
	 */
	public Rule PfTo() {
		return
			Sequence(
				TO(),
				SkipSpaces(),
				PfIpPortSpec(),
				_pfRule.addDestinationHost(_ipspec),
				_pfRule.addDestinationPort(_portspec)
			);
	}

	/**
	 * Matches ipportspec
	 * ipportspec : ipspec
	 *	| ipspec PORT portspec
	 *	| PORT portspec
	 *
	 * @return a Rule
	 */
	public Rule PfIpPortSpec() {
		return
			Sequence(
				ipSpecClear(),
				portSpecClear(),
				FirstOf(
					Sequence(
						PORT(),
						SkipSpaces(),
						PfPortSpec()
					),
					Sequence(
						PfIpSpec(),
						SkipSpaces(),
						PORT(),
						SkipSpaces(),
						PfPortSpec()
					),
					Sequence(
						// ipspec PORT port may have change _ippsec
						ipSpecClear(),
						PfIpSpec(),
						SkipSpaces()
					)
				)
			);
	}

	/**
	 * Matches optnl
	 * optnl : '\n'
	 *  | empty
	 *
	 * @return a Rule.
	 */
	public Rule PfOptnl() {
		return
			Optional(
				Sequence(
					SkipSpaces(),
					Optional(
						OneOrMore(
							Sequence(
								Ch('\n'),
								SkipSpaces()
							)
						)
					)
				)
			);
	}

	/**
	 * Matches ipspec
	 * ipspec : ANY
	 *	| xhost
	 *	| '{' optnl host_list '}'
	 *
	 * @return a Rule
	 */
	public Rule PfIpSpec() {
		return
			Sequence(
				newPfXhost(),
				FirstOf(
					/*
					 * ANY
					 */
					Sequence(
						ANY(),
						_pfXhost.setAny(true),
						_ipspec.add(_pfXhost)
					),
					/*
					 * xhost
					 */
					PfXHost(),
					/*
					 * '{' optnl host_list '}'
					 */
					Sequence(
						Ch('{'),
						PfOptnl(),
						PfHostList(),
						SkipSpaces(),
						Ch('}')
					)
				)
			);
	}

	/**
	 * Matches host_list
	 * host_list : ipspec optnl
	 *	| host_list comma ipspec optnl
	 *
	 * @return a Rule
	 */
	public Rule PfHostList() {
		return
			Sequence(
				PfIpSpec(),
				PfOptnl(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfComma(),
						SkipSpaces(),
						PfHostList()
					)
				)
		 );
	}

	/**
	 * Matches xhost
	 *	xhost : not host
 	 *	| not NOROUTE
	 *	| not URPFFAILED
	 *
	 * @return a Rule
	 */
	public Rule PfXHost() {
		return
			FirstOf(
				/*
				 * not NOROUTE
				 */
				Sequence(
					PfNot(),
					SkipSpaces(),
					NOROUTE(),
					_pfXhost.setNot(_pfnot),
					_pfXhost.setNoroute(true),
					_ipspec.add(_pfXhost)
				),
				/*
				 * not URPFFAILED
				 */
				Sequence(
					PfNot(),
					SkipSpaces(),
					URPFFAILED(),
					_pfXhost.setNot(_pfnot),
					_pfXhost.setUrpffailed(true),
					_ipspec.add(_pfXhost)
				),
				/*
				 * not host
				 */
				Sequence(
					PfNot(),
					SkipSpaces(),
					PfHost(),
					_pfXhost.setNot(_pfnot),
					_ipspec.add(_pfXhost)
				)
			);
	}

	/**
	 * Matches host
	 * host	: STRING
	 *	| STRING '-' STRING
	 *	| STRING '/' NUMBER
	 *	| NUMBER '/' NUMBER
	 *	| dynaddr
	 *	| dynaddr '/' NUMBER
	 *	| '<' STRING '>'
	 *	| ROUTE	STRING
	 *
	 * @return a Rule
	 */
	public Rule PfHost() {
		return
			FirstOf(
				/*
				 * STRING '-' STRING
				 */
				Sequence(
					PfString(),
					newPfXhost(),
					_pfXhost.setFirstAddress(_pfString),
					SkipSpaces(),
					Ch('-'),
					SkipSpaces(),
					PfString(),
					_pfXhost.setLastAddress(_pfString)
				),
				/*
				 * STRING '/' STRING
				 */
				Sequence(
					PfString(),
					newPfXhost(),
					_pfXhost.setFirstAddress(_pfString),
					SkipSpaces(),
					Ch('/'),
					SkipSpaces(),
					PfString(),
					_pfXhost.setFirstAddress(_pfXhost.getFirstAddress() + "/" +
						_pfString)
				),
				/*
				 * '<' STRING '>'
				 */
				Sequence(
					Ch('<'),
					SkipSpaces(),
					PfString(),
					newPfXhost(),
					_pfXhost.setTable(_pfString),
					SkipSpaces(),
					Ch('>')
				),
				/*
				 * route STRING
				 */
				Sequence(
					ROUTE(),
					WhiteSpaces(),
					PfString(),
					newPfXhost(),
					_pfXhost.setRoute(_pfString)
				),
				/*
				 * dynaddr
				 * '(' STRING ')' ['/' STRING]
				 */
				Sequence(
					Ch('('),
					SkipSpaces(),
					PfString(),
					newPfXhost(),
					_pfXhost.setDynaddr(_pfString),
					SkipSpaces(),
					Ch(')'),
					SkipSpaces(),
					Optional(
						Sequence(
							Ch('/'),
							SkipSpaces(),
							PfString(),
							_pfXhost.setDynaddrMask(_pfString)
						)
					)
				),
				/*
				 * STRING
				 */
				Sequence(
					PfString(),
					newPfXhost(),
					_pfXhost.setFirstAddress(_pfString)
				)
			);
	}

	/**
	 * Matches portspec
	 * portspec	: port_item
	 *	| '{' optnl port_list '}'
	 *
	 * @return a Rule
	 */
	public Rule PfPortSpec() {
		return
			FirstOf(
				PfPortItem(),
				Sequence(
					Ch('{'),
					PfOptnl(),
					PfPortList(),
					SkipSpaces(),
					Ch('}')
				)
			);
	}

	/**
	 * Matches port_list
	 * port_list	: port_item optnl
	 *	| port_list comma port_item optnl
	 *
	 * @return a Rule
	 */
	public Rule PfPortList() {
		return
			Sequence(
				PfPortItem(),
				PfOptnl(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfComma(),
						SkipSpaces(),
						PfPortList()
					)
				)
		 );
	}


	/**
	 * Matches port_item
	 *
	 * port_item : portrange
	 *	| unaryop portrange
	 * 	| portrange PORTBINARY portrange
	 *
	 * @return a Rule
	 */
	public Rule PfPortItem() {
		return
			Sequence(
				FirstOf(
					/*
					 * unaryop portrange
					 */
					Sequence(
						PfUnaryOp(),
						newPfPortItem(),
						_pfPortItem.setOperator(match()),
						SkipSpaces(),
						PfPortRange(),
						_pfPortItem.setFirstPort(_pfString)
					),
					/*
					 * portrange PORTBINARY portrange
					 */
					Sequence(
						PfPortRange(),
						SkipSpaces(),
						PfPortBinary(),
						newPfPortItem(),
						_pfPortItem.setFirstPort(_pfString),
						_pfPortItem.setOperator(match()),
						SkipSpaces(),
						PfPortRange(),
						_pfPortItem.setLastPort(_pfString)
					),
					/*
					 * portrange
					 */
					Sequence(
						PfPortRange(),
						newPfPortItem(),
						/*
						 * XXX: why ':' is not in PORTBINARY?
						 */
						pfPortItemFromRange(_pfString)
					)
				),
				_portspec.add(_pfPortItem)
			);
	}

	/**
	 * Matches portplain
	 * portplain : numberstring
	 *
	 * @return a Rule
	 */
	public Rule PfPortPlain() {
		return PfString();
	}

	/**
	 * Matches portrange.
	 * portrange : numberstring
	 *
	 * @return a Rule
	 */
	public Rule PfPortRange() {
		return PfString();
	}

	/**
	 * Matches uids
	 * uids	: uid_item
	 *	| '{' optnl uid_list '}'
	 *
	 * @return a Rule
	 */
	public Rule PfUids() {
		return
			FirstOf(
				PfUidItem(),
				Sequence(
					Ch('{'),
					PfOptnl(),
					PfUidList(),
					SkipSpaces(),
					Ch('}')
				)
			);
	}

	/**
	 * Matches uid_list
	 * uid_list	: uid_item optnl
	 *	| uid_list comma uid_item optnl
	 *
	 * @return a Rule
	 */
	public Rule PfUidList() {
		return
			Sequence(
				PfUidItem(),
				PfOptnl(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfComma(),
						SkipSpaces(),
						PfUidList()
					)
				)
			);
	}

	/**
	 * Matches uid_item
	 * uid_item	: uid
	 *	| unaryop uid
	 *	| uid PORTBINARY uid
	 *
	 * @return a Rule
	 */
	public Rule PfUidItem() {
		return
			FirstOf(
				Sequence(
					PfUnaryOp(),
					SkipSpaces(),
					PfUid()
				),
				Sequence(
					PfUid(),
					SkipSpaces(),
					PfPortBinary(),
					SkipSpaces(),
					PfUid()
				),
				PfUid()
			);
	}

	/**
	 * Matches uid
	 * uid : STRING
	 *	| NUMBER
	 *
	 * @return a Rule
	 */
	public Rule PfUid() {
		return PfString();
	}

	/**
	 * Matches gids
	 * gids	: gid_item
	 *	| '{' optnl gid_list '}'
	 *
	 * @return a Rule
	 */
	public Rule PfGids() {
		return
			FirstOf(
				PfGidItem(),
				Sequence(
					Ch('{'),
					PfOptnl(),
					PfGidList(),
					SkipSpaces(),
					Ch('}')
				)
			);
	}

	/**
	 * Matches gid_list
	 * gid_list	: gid_item optnl
	 *	| gid_list comma gid_item optnl
	 *
	 * @return a Rule
	 */
	public Rule PfGidList() {
		return
			Sequence(
				PfGidItem(),
				PfOptnl(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfComma(),
						SkipSpaces(),
						PfGidList()
					)
				)
			);
	}

	/**
	 * Matches gid_item
	 * gid_item	: gid
	 *	| unaryop gid
	 *	| gid PORTBINARY gid
	 *
	 * @return a Rule
	 */
	public Rule PfGidItem() {
		return
			FirstOf(
				Sequence(
					PfUnaryOp(),
					SkipSpaces(),
					PfGid()
				),
				Sequence(
					PfGid(),
					SkipSpaces(),
					PfPortBinary(),
					SkipSpaces(),
					PfGid()
				),
				PfGid()
			);
	}

	/**
	 * Matches gid
	 * gid : STRING
	 *	| NUMBER
	 *
	 * @return a Rule
	 */
	public Rule PfGid() {
		return PfString();
	}

	/**
	 * Matches flag
	 * flag : STRING
	 *
	 * @return a Rule
	 */
	public Rule PfFlag() {
		return PfString();
	}

	/**
	 * Matches flags
	 * flags : FLAGS flag '/' flag
	 *	| FLAGS '/' flag
	 *	| FLAGS ANY
	 *
	 * @return a Rule
	 */
	public Rule PfFlags() {
		return
			FirstOf(
				/*
				 * FLAGS ANY
				 */
				Sequence(
					FLAGS(),
					WhiteSpaces(),
					ANY(),
					setFlags("any")
				),
				/*
				 * FLAGS '/' flag
				 */
				Sequence(
					FLAGS(),
					SkipSpaces(),
					Ch('/'),
					SkipSpaces(),
					PfFlag(),
					setFlagset(_pfString)
				),
				/*
				 * FLAGS flag '/' flag
				 */
				Sequence(
					FLAGS(),
					SkipSpaces(),
					PfFlag(),
					setFlags(_pfString),
					SkipSpaces(),
					Ch('/'),
					SkipSpaces(),
					PfFlag(),
					setFlagset(_pfString)
				)
			);
	}

	/**
	 *
	 * Matches icmpspec
	 * icmpspec	: ICMPTYPE icmp_item
	 *	| ICMPTYPE '{' optnl icmp_list '}'
	 *	| ICMP6TYPE icmp6_item
	 *	| ICMP6TYPE '{' optnl icmp6_list '}'
	 *
	 * @return a Rule
	 */
	public Rule PfIcmpSpec() {
		return
			FirstOf(
				/*
				 * ICMPTYPE icmp_item
				 */
				Sequence(
					ICMPTYPE(),
					SkipSpaces(),
					PfIcmpItem(),
					_icmpspec.add(_pfIcmpItem)
				),
				/*
				 * ICMPTYPE '{' optnl icmp_list '}'
				 */
				Sequence(
					ICMPTYPE(),
					SkipSpaces(),
					Ch('{'),
					PfOptnl(),
					PfIcmpList(),
					SkipSpaces(),
					Ch('}')
				),
				/*
				 * ICMP6TYPE icmp6_item
				 */
				Sequence(
					ICMP6TYPE(),
					SkipSpaces(),
					PfIcmpItem(),
					_icmp6spec.add(_pfIcmpItem)
				),
				/*
				 * ICMP6TYPE '{' optnl icmp6_list '}'
				 */
				Sequence(
					ICMP6TYPE(),
					SkipSpaces(),
					Ch('{'),
					PfOptnl(),
					PfIcmp6List(),
					SkipSpaces(),
					Ch('}')
				)
			);
	}

	/**
	 * Matches icmp_list
	 * icmp_list	: icmp_item optnl
	 *	| icmp_list comma icmp_item optnl
	 *
	 * @return a Rule
	 */
	public Rule PfIcmpList() {
		return
			Sequence(
				PfIcmpItem(),
				_icmpspec.add(_pfIcmpItem),
				PfOptnl(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfComma(),
						SkipSpaces(),
						PfIcmpList()
					)
				)
			);
	}

	/**
	 * Matches icmp6_list
	 * icmp6_list	: icmp_item optnl
	 *	| icmp6_list comma icmp_item optnl
	 *
	 * @return a Rule
	 */
	public Rule PfIcmp6List() {
		return
			Sequence(
				PfIcmpItem(),
				_icmp6spec.add(_pfIcmpItem),
				PfOptnl(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfComma(),
						SkipSpaces(),
						PfIcmp6List()
					)
				)
			);
	}

	/**
	 * Matches icmp_item
	 * icmp_item : icmptype
	 *	| icmptype CODE STRING
	 *	| icmptype CODE NUMBER
	 *
	 * @return a Rule
	 */
	public Rule PfIcmpItem() {
		return
			Sequence(
				/*
				 * icmptype
				 */
				newPfIcmpItem(),
				PfIcmpType(),
				_pfIcmpItem.setIcmpType(_pfString),
				/*
				 * [CODE STRING]
				 */
				SkipSpaces(),
				Optional(
					Sequence(
						CODE(),
						WhiteSpaces(),
						PfString(),
						_pfIcmpItem.setIcmpCode(_pfString),
						SkipSpaces()
					)
				)
			);
	}

	/**
	 * Matches icmptype
	 *  icmptype : STRING
	 *	| NUMBER
	 *
	 * @return a Rule
	 */
	public Rule PfIcmpType() {
		return PfString();
	}

	/**
	 * Matches tos
	 * tos : STRING
	 *  | NUMBER
	 *
	 * @return a Rule
	 */
	public Rule PfTos() {
		return PfString();
	}

	/**
	 * Matches sourcetrack
	 * sourcetrack : empty
	 *	| GLOBAL
	 *	| RULE
	 *
	 * @return a Rule
	 */
	public Rule PfSourceTrack() {
		return
			FirstOf(
				GLOBAL(),
				RULE()
			);
	}

	/**
	 * Matches statelock
	 * statelock : IFBOUND
	 *	| FLOATING
	 */
	public Rule PfStateLock() {
		return
			FirstOf(
				IFBOUND(),
				FLOATING()
			);
	}

	/**
	 * Matches keep
	 * keep	: NO STATE
	 *	| KEEP STATE state_opt_spec
	 *	| MODULATE STATE state_opt_spec
	 *	| SYNPROXY STATE state_opt_spec
	 *
	 * @return a Rule
	 */
	public Rule PfKeep() {
		return
			FirstOf(
				/*
				 * NO STATE
				 */
				Sequence(
					NO(),
					WhiteSpaces(),
					STATE(),
					_pfRule.getFilterOpts().setAction("no-state")
				),
				/*
				 * KEEP STATE state_opt_spec
				 */
				Sequence(
					KEEP(),
					WhiteSpaces(),
					STATE(),
					Optional(
						Sequence(
							WhiteSpaces(),
							PfStateOptSpec()
						)
					),
					_pfRule.getFilterOpts().setAction("keep-state")
				),
				/*
				 * MODULATE STATE state_opt_spec
				 */
				Sequence(
					MODULATE(),
					WhiteSpaces(),
					STATE(),
					Optional(
						Sequence(
							WhiteSpaces(),
							PfStateOptSpec()
						)
					),
					_pfRule.getFilterOpts().setAction("modulate-state")
				),
				/*
				 * SYNPROXY STATE state_opt_spec
				 */
				Sequence(
					SYNPROXY(),
					WhiteSpaces(),
					STATE(),
					Optional(
						Sequence(
							WhiteSpaces(),
							PfStateOptSpec()
						)
					),
					_pfRule.getFilterOpts().setAction("synproxy-state")
				)
			);

	}

	/**
	 * Matches state_opt_spec
	 * state_opt_spec	: '(' state_opt_list ')'
	 *	| empty
	 *
	 * @return a Rule
	 */
	public Rule PfStateOptSpec() {
		return
			Sequence(
				Ch('('),
				SkipSpaces(),
				PfStateOptList(),
				SkipSpaces(),
				Ch(')')
			);
	}

	/**
	 * Matches flush
	 * flush : empty
	 *	| FLUSH
	 *	| FLUSH GLOBAL
	 *
	 * @return a Rule
	 */
	public Rule PfFlush() {
		return
			FirstOf(
				Sequence(
					FLUSH(),
					WhiteSpaces(),
					GLOBAL()
				),
				FLUSH()
			);
	}

	/**
	 * Matches state_opt_list
	 * state_opt_list : state_opt_item
	 *	| state_opt_list comma state_opt_item
	 *
	 * @return a Rule
	 */
	public Rule PfStateOptList() {
		return
			Sequence(
				PfStateOptItem(),
				SkipSpaces(),
				Optional(
					Sequence(
						PfComma(),
						SkipSpaces(),
						PfStateOptList()
					)
				)
			);
	}

	/**
	 * Matches state_opt_item
	 * state_opt_item : MAXIMUM NUMBER
	 *	| NOSYNC
	 *	| MAXSRCSTATES NUMBER
	 *	| MAXSRCCONN NUMBER
	 *	| MAXSRCCONNRATE NUMBER '/' NUMBER
	 *	| OVERLOAD '<' STRING '>' flush
	 *	| MAXSRCNODES NUMBER
	 *	| SOURCETRACK sourcetrack
	 *	| statelock
	 *	| SLOPPY
	 *	| PFLOW
	 *	| STRING NUMBER	// nota: time out
	 *
	 * @return a Rule
	 */
	public Rule PfStateOptItem() {
		return
			Sequence(
				setPfStateOptItem(""),
				FirstOf(
					/*
					 * MAXIMUM NUMBER
					 */
					Sequence(
						MAXIMUM(),
						WhiteSpaces(),
						PfString(),
						setPfStateOptItem("MAXIMUM " + _pfString)
					),
					/*
					 * NOSYNC
					 */
					Sequence(
						NOSYNC(),
						setPfStateOptItem("NOSYNC")
					),
					/*
					 * MAXSRCSTATES NUMBER
					 */
					Sequence(
						MAXSRCSTATES(),
						WhiteSpaces(),
						PfString(),
						setPfStateOptItem("MAXSRCSTATES " + _pfString)
					),
					/*
					 * MAXSRCCONN NUMBER
					 */
					Sequence(
						MAXSRCCONN(),
						WhiteSpaces(),
						PfString(),
						setPfStateOptItem("MAXSRCCONN " + _pfString)
					),
					/*
					 * MAXSRCCONNRATE NUMBER '/' NUMBER
					 */
					Sequence(
						MAXSRCCONNRATE(),
						WhiteSpaces(),
						PfString(),
						setPfStateOptItem("MAXSRCCONNRATE " + _pfString),
						SkipSpaces(),
						Ch('/'),
						SkipSpaces(),
						PfString(),
						setPfStateOptItem(_pfStateOptItem + " " + _pfString)
					),
					/*
					 * OVERLOAD '<' STRING '>' flush
					 */
					Sequence(
						OVERLOAD(),
						WhiteSpaces(),
						Ch('<'),
						SkipSpaces(),
						PfString(),
						SkipSpaces(),
						Ch('>'),
						setPfStateOptItem("OVERLOAD " + "<" + _pfString + ">"),
						SkipSpaces(),
						Optional(
							Sequence(
								PfFlush(),
								setPfStateOptItem(_pfStateOptItem +	" "
									+ match())
							)
						)
					),
					/*
					 * MAXSRCNODES NUMBER
					 */
					Sequence(
						MAXSRCNODES(),
						WhiteSpaces(),
						PfString(),
						setPfStateOptItem("MAXSRCNODES " + _pfString)
					),
					/*
					 * SOURCETRACK sourcetrack
					 */
					Sequence(
						SOURCETRACK(),
						setPfStateOptItem("SOURCETRACK"),
						Optional(
							Sequence(
								WhiteSpaces(),
								PfSourceTrack(),
								setPfStateOptItem(_pfStateOptItem + " "
									+ match())
							)
						)
					),
					/*
					 * statelock
					 */
					Sequence(
						PfStateLock(),
						setPfStateOptItem("STATELOCK " + match())
					),
					/*
					 * SLOPPY
					 */
					Sequence(
						SLOPPY(),
						setPfStateOptItem("SLOPPY")
					),
					/*
					 * PFLOW
					 */
					Sequence(
						PFLOW(),
						setPfStateOptItem("PFLOW")
					),
					/*
					 * STRING NUMBER
					 */
					Sequence(
						PfString(),
						setPfStateOptItem("STRING " + _pfString),
						WhiteSpaces(),
						PfString(),
						setPfStateOptItem(_pfStateOptItem +	" " +  _pfString)
					)
				),
				_pfRule.getFilterOpts().getOptions().add(_pfStateOptItem)
			);
	}

	/**
	 * Matches label
	 * label : STRING
	 *
	 * @return a Rule
	 */
	 public Rule PfLabel() {
		 return PfString();
	 }

	/**
	 * Matches qname
	 * qname : STRING
	 *	 | '(' STRING ')'
	 *	 | '(' STRING comma STRING ')'
	 *
	 * @return a Rule
	 */
	public Rule PfQname() {
		return
			FirstOf(
				/*
				 * STRING
				 */
				Sequence(
					PfString(),
					setPfQname(_pfString),
					setPfPQname(null)
				),
				/*
				 * '(' STRING ')'
				 */
				Sequence(
					Ch('('),
					SkipSpaces(),
					PfString(),
					SkipSpaces(),
					Ch(')'),
					setPfQname(_pfString),
					setPfPQname(null)
				),
				/*
				 * '(' STRING comma STRING ')'
				 */
				Sequence(
					Ch('('),
					SkipSpaces(),
					PfString(),
					setPfQname(_pfString),
					setPfPQname(null),
					SkipSpaces(),
					PfComma(),
					SkipSpaces(),
					PfString(),
					SkipSpaces(),
					Ch(')'),
					setPfPQname(_pfString)
				)
			);
	}

	/**
	 * Matches redirspec
	 * redirspec : host
	 *  | '{' optnl redir_host_list '}'
	 *
	 * @return a Rule
	 */
	public Rule PfRedirSpec() {
		return
			Sequence(
				redirHostsClear(),
				FirstOf(
					Sequence(
						PfHost(),
						_redirHosts.add(_pfXhost)
					),
					Sequence(
						Ch('{'),
						PfOptnl(),
						PfRedirHostList(),
						SkipSpaces(),
						Ch('}')
					)
				)
			);
	}

	/**
	 * Matches redir_host_list
	 * redir_host_list : host optnl
	 *  | redir_host_list comma host optnl
	 *
	 * @return a Rule
	 */
	public Rule PfRedirHostList() {
		return
			Sequence(
				PfHost(),
				_redirHosts.add(_pfXhost),
				PfOptnl(),
				Optional(
					Sequence(
						PfComma(),
						SkipSpaces(),
						PfRedirHostList()
					)
				)
			);
	}

	/**
	 * Matches redirpool
	 * redirpool : redirspec
	 *	| redirspec PORT portstar
	 *
	 * @return a Rule
	 */
	public Rule PfRedirPool() {
		return
			Sequence(
				newRedirSpec(),
				FirstOf(
					/*
					 * redirspec PORT portstar
					 */
					Sequence(
						PfRedirSpec(),
						WhiteSpaces(),
						PORT(),
						WhiteSpaces(),
						PfString(),
						_redirSpec.addHost(_redirHosts),
						_redirSpec.setPortstar(_pfString)
					),
					/*
					 * redirspec
					 */
					Sequence(
						PfRedirSpec(),
						_redirSpec.addHost(_redirHosts)
					)
				)
			);
	}

	/**
	 * Matches pool_opts
	 *	pool_opts :	pool_opts_l
	 *	 | empty
	 *
	 * @return a Rule
	 */
	public Rule PfPoolOpts() {
		 return
			Sequence(
				newPoolOpts(),
				PfPoolOptsL()
			);
	}

	/**
	 * Matches pool_opts_l
	 * pool_opts_l	: pool_opts_l pool_opt
	 *  | pool_opt
	 *
	 * @return a Rule
	 */
	public Rule PfPoolOptsL() {
		return
			Sequence(
				PfPoolOpt(),
				SkipSpaces(),
				Optional(
					PfPoolOptsL()
				)
			);
	}

	/**
	 * Matches pool_opt
	   pool_opt	: BITMASK
		| RANDOM
		| SOURCEHASH hashkey
		| ROUNDROBIN
		| STATICPORT
		| STICKYADDRESS
	 *
	 * @return a Rule
	 */
	public Rule PfPoolOpt() {
		return
			FirstOf(
				/*
				 * BITMASK
				 */
				Sequence(
					BITMASK(),
					_poolOpts.setType(PfConst.PF_POOL_BITMASK)
				),
				/*
				 * RANDOM
				 */
				Sequence(
					RANDOM(),
					_poolOpts.setType(PfConst.PF_POOL_RANDOM)
				),
				/*
				 * SOURCEHASH [hashkey]
				 */
				Sequence(
					SOURCEHASH(),
					_poolOpts.setType(PfConst.PF_POOL_SRCHASH),
					SkipSpaces(),
					Optional(
						Sequence(
							PfString(),
							_poolOpts.setKey(_pfString)
						)
					)
				),
				/*
				 * ROUNDROBIN
				 */
				Sequence(
					ROUNDROBIN(),
					_poolOpts.setType(PfConst.PF_POOL_ROUNDROBIN)
				),
				/*
				 * STATICPORT
				 */
				Sequence(
					STATICPORT(),
					_poolOpts.setStaticPort(true)
				),
				/*
				 * STICKYADDRESS
				 */
				Sequence(
					STICKYADDRESS(),
					_poolOpts.setOpts(_poolOpts.getOpts()
						| PfConst.PF_POOL_STICKYADDR)
				)
			);
	}

	/**
	 * Matches route_host
	 * route_host : STRING
	 *	| STRING '/' STRING
	 *	| '<' STRING '>'
	 *	| dynaddr '/' NUMBER
	 *	| '(' STRING host ')'
	 *
	 * @return a Rule
	 */
	public Rule PfRouteHost() {
		return
			FirstOf(
				/*
				 * STRING '/' STRING
				 */
				Sequence(
					PfString(),
					newPfXhost(),
					_pfXhost.setFirstAddress(_pfString),
					Ch('/'),
					PfString(),
					_pfXhost.setFirstAddress(_pfXhost.getFirstAddress() +
							"/" + _pfString)
				),
				/*
				 * '<' STRING '>'
				 */
				Sequence(
					Ch('<'),
					PfString(),
					newPfXhost(),
					_pfXhost.setTable(_pfString),
					Ch('>')
				),
				/*
				 * dynaddr
				 * '(' STRING ')' ['/' STRING]
				 */
				Sequence(
					Ch('('),
					SkipSpaces(),
					PfString(),
					newPfXhost(),
					_pfXhost.setDynaddr(_pfString),
					SkipSpaces(),
					Ch(')'),
					SkipSpaces(),
					Optional(
						Sequence(
							Ch('/'),
							PfString(),
							_pfXhost.setDynaddrMask(_pfString)
						)
					)
				),
				/*
				 * STRING
				 */
				Sequence(
					PfString(),
					newPfXhost(),
					_pfXhost.setFirstAddress(_pfString)
				),
				/*
				 *	'(' STRING host ')'
				 */
				Sequence(
					Ch('('),
					SkipSpaces(),
					PfString(),
					setIfname(_pfString),
					WhiteSpaces(),
					PfHost(),
					_pfXhost.setIfName(_ifname),
					SkipSpaces(),
					Ch(')')
				)
			);
	}

	/**
	 * Matches route_host_list
	 * route_host_list : route_host optnl
	 *	| route_host_list comma route_host optnl
	 *
	 * @return a Rule
	 */
	public Rule PfRouteHostList() {
		return
			Sequence(
				PfRouteHost(),
				_routeOpts.getHosts().add(_pfXhost),
				PfOptnl(),
				Optional(
					Sequence(
						SkipSpaces(),
						PfComma(),
						SkipSpaces(),
						PfRouteHostList()
					)
				)
			);
	}

	/**
	 * Matches routespec
	 * routespec : route_host
	 *	| '{' optnl route_host_list '}'
	 *
	 * @return a Rule
	 */
	public Rule PfRouteSpec() {
		return
			Sequence(
				newRouteOpts(),
				FirstOf(
					Sequence(
						PfRouteHost(),
						_routeOpts.getHosts().add(_pfXhost)
					),
					Sequence(
						Ch('{'),
						PfOptnl(),
						PfRouteHostList(),
						SkipSpaces(),
						Ch('}')
					)
				)
			);
	}

	/**
	 * Matches comma
	 * comma : ','
	 *  | empty
	 */
	public Rule PfComma() {
		return Optional(
					Ch(',')
				);
	}

	/**
	 * Matches unaryop
	 * unaryop : '='
	 *	| NE
	 *	| LE
	 *	| '<'
	 *	| GE
	 *	| '>'
	 *
	 * @return a Rule
	 */
	public Rule PfUnaryOp() {
		return
			FirstOf(
				String("!="),
				String("<="),
				String("<"),
				String(">="),
				String(">"),
				String("=")
			);
	}

	/**
	 * Matches PORTBINARY
	 * PORTBINAY : "<>" | "><"
	 *
	 * @return a Rule
	 */
	 public Rule PfPortBinary() {
		 return
			FirstOf(
				String("<>"),
				String("><")
			);
	 }

	/**
	 * Matches STRING : Atom or PfQuotedString
	 * @return a Rule.
	 */
	public Rule PfString() {
		return FirstOf(
			Sequence(
				PfQuotedString(),
				setPfString(_lastString)
			),
			Sequence(
				TestNot(
					PfKeyword()
				),
				PfAtom(),
				setPfString(match())
			)
		);
	}

	/**
	 * Matches a quoted string.
	 * @return a Rule
	 */
	public Rule PfQuotedString() {
		return
		Sequence(
			AnyOf("'\""),
			quotedStringStart(match()),
			OneOrMore(
				Sequence(
					quotedStringContinue(),
					ANY,
					quotedString(match())
				)
			)
		);
	}

}
