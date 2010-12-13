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
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.ReportingParseRunner;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.support.ParsingResult;

/**
 * Parser rules for PF.
 *
 * Taken from OpenBSD rules (parse.y, OpenBSD 4.7)
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class PfParser extends PfBaseParser {

	private String _ruleName;
	private String _name;
	private String _value;
	private char _quotec;
	private boolean _quoted;
	private char _previousChar;
	private String _lastString;
	private String _pfString;
	private boolean _pfStringEnd;
	private String _ifItem;
	private boolean _pfnot;
	private Xhost _pfXhost;
	private PortItemTemplate _pfPortItem;
	private IcmpItem _pfIcmpItem;
	private String _pfStateOptItem;
	private String _flags;
	private String _flagset;
	private List<Xhost> _ipspec = new ArrayList<Xhost>();
	private List<PortItemTemplate> _portspec = new ArrayList<PortItemTemplate>();
	private List<IcmpItem> _icmpspec = new ArrayList<IcmpItem>();
	private List<IcmpItem> _icmp6spec = new ArrayList<IcmpItem>();
	private StringsList _stateOptSpec = new StringsList();
	private ScrubOptsTemplate _scrubOpts;
	private PoolOptsTemplate _poolOpts;
	private String _pfQname;
	private String _pfPQname;
	private List<Xhost> _redirHosts = new ArrayList<Xhost>();
	private RedirSpecTemplate _redirSpec;
	private RouteOptsTemplate _routeOpts;
	private String _ifname;
	private StringsList _macroValues = new StringsList();
	private String _matchedText;
	private StringBuilder _curExpandedContext;

	private RuleTemplate _pfRule;
	private TableTemplate _pfTable;
	private AnchorTemplate _pfAnchor;

	public String getName() {
		return _name;
	}

	public String getRuleName() {
		return _ruleName;
	}

	public java.lang.String getMatchedText() {
		return _matchedText;
	}

	public String getValue() {
		return _value;
	}

	public String getLastString() {
		return _lastString;
	}

	public RuleTemplate getPfRule() {
		return _pfRule;
	}

	public TableTemplate getPfTable() {
		return _pfTable;
	}

	public AnchorTemplate getPfAnchor() {
		return _pfAnchor;
	}

	public StringBuilder getCurExpandedContext() {
		return _curExpandedContext;
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
		result = ReportingParseRunner.run(IsClosingBrace(),
				exRule.expandedToString());
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
					result = ReportingParseRunner.run(IsNewRule(),
						nextRule.expandedToString());
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
	public void clear() {
		_ruleName = null;
		_name = null;
		_value = null;
		_lastString = null;
		_ifItem = null;
		_pfRule = null;
		_pfTable = null;
		_pfAnchor = null;
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

		for (String s: should) {
			if (line.trim().startsWith(s))
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
					Eoi()
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
							new Action() {
								public boolean run(Context context) {
									return true;
								}
							},
							FirstOf(
								PfGenericList(),
								PfGenericRuleItem()
							),
							new Action() {
								public boolean run(Context context) {
									return true;
								}
							}
						)
					),
					SkipSpaces(),
					FirstOf(
						Eoi(),
						Eol()
					)
				),
				/*
				 * text matched
				 */
				new Action() {
					public boolean run(Context context) {
						_matchedText = context.getPrevText();
						return true;
					}
				}
			);
	}

	public Rule PfGenericRuleItem() {
		return
			Sequence(
				new Action() {
					public boolean run(Context context) {
						return true;
					}
				},
				TestNot(
					PfGenericSpecial()
				),
				new Action() {
					public boolean run(Context context) {
						return true;
					}
				},
				FirstOf(
					PfQuotedString(),
					PfGenericAtom()
				),
				new Action() {
					public boolean run(Context context) {
						return true;
					}
				}
			);
	}

	public Rule PfGenericList() {
		return
			FirstOf(
				Sequence(
					new Action() {
						public boolean run(Context context) {
							return true;
						}
					},
					Ch('{'),
					PfOptnl(),
					Ch('}')
				),
				Sequence(
					new Action() {
						public boolean run(Context context) {
							return true;
						}
					},
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
				new Action() {
					public boolean run(Context context) {
						return true;
					}
				},
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
				CharSet("{}")
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
						Any()
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
						Eoi(),
						Ch('\n')
					)
				),
				/*
				 * text matched
				 */
				new Action() {
					public boolean run(Context context) {
						_matchedText = context.getPrevText();
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_ruleName = "closing brace";
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_name = _pfString;
						_macroValues.clear();
						return true;
					}
				},
				SkipSpaces(),
				String("="),
				SkipSpaces(),
				PfMacroValue(),
				new Action() {
					public boolean run(Context context) {
						_value = "";
						for (String s: _macroValues)
							_value = _value + s + " ";
						_value = _value.trim();
						_ruleName = "macro";
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_macroValues.add(_pfString);
						return true;
					}
				},
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
				new Action() {
					public boolean run(Context context) {
						_pfnot = false;
						return true;
					}
				},
				Optional(
					Sequence(
						Ch('!'),
						new Action() {
							public boolean run(Context context) {
								_pfnot = true;
								return true;
							}
						}
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
				new Action() {
					public boolean run(Context context) {
						_pfTable = new TableTemplate();
						return true;
					}
				},
				SkipSpaces(),
				Ch('<'),
				SkipSpaces(),
				PfString(),
				new Action() {
					public boolean run(Context context) {
						_pfTable.setName(_pfString);
						return true;
					}
				},
				SkipSpaces(),
				Ch('>'),
				SkipSpaces(),
				PfTableOpts(),
				new Action() {
					public boolean run(Context context) {
						_ruleName = "tabledef";
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_ipspec.clear();
						return true;
					}
				},
				FirstOf(
					/*
					 * FILENAME STRING
					 */
					Sequence(
						FILENAME(),
						WhiteSpaces(),
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfTable.getFileNames().add(_pfString);
								return true;
							}
						}
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
						new Action() {
							public boolean run(Context context) {
								_pfTable.getHosts().addAll(_ipspec);
								return true;
							}
						}
					),
					/*
					 * STRING
					 */
					Sequence(
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfTable.getOptions().add(_pfString);
								return true;
							}
						}
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
				new Action() {
					public boolean run(Context context) {
						_value = _pfString;
						_ruleName = "include";
						return true;
					}
				},
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
				new Action() {
					public boolean run(Context context) {
						_pfRule = new RuleTemplate();
						return true;
					}
				},
				PfInterface(),
				new Action() {
					public boolean run(Context context) {
						_ruleName = "option set skip";
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_pfAnchor = new AnchorTemplate();
						_pfRule = new RuleTemplate();
						_pfAnchor.setRule(_pfRule);
						return true;
					}
				},
				/*
				 * [anchorname]
				 */
				Optional(
					Sequence(
						WhiteSpaces(),
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfAnchor.setName(_pfString);
								return true;
							}
						}
					)
				),
				SkipSpaces(),
				/*
				 * dir
				 */
				Optional(
					Sequence(
						PfDir(),
						new Action() {
							public boolean run(Context context) {
								_pfRule.setDir(context.getPrevText());
								return true;
							}
						}
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
						new Action() {
						public boolean run(Context context) {
							_pfAnchor.setInlined(true);
							return true;
							}
						},
						SkipSpaces()
					)
				),
				new Action() {
					public boolean run(Context context) {
						_ruleName = "anchorrule";
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_name = _pfString;
						return true;
					}
				},
				WhiteSpaces(),
				FROM(),
				WhiteSpaces(),
				PfString(),
				new Action() {
					public boolean run(Context context) {
						_value = _pfString;
						_ruleName = "loadrule";
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_scrubOpts = new ScrubOptsTemplate();
						return true;
					}
				},			
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
					new Action() {
						public boolean run(Context context) {
							_scrubOpts.setNodf(true);
							return true;
						}
					}
				),
				/*
				 * MINTTL NUMBER
				 */
				Sequence(
					MINTTL(),
					WhiteSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_scrubOpts.setMinttl(_pfString);
							return true;
						}
					}
				),
				/*
				 * MAXMSS NUMBER
				 */
				Sequence(
					MAXMSS(),
					WhiteSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_scrubOpts.setMaxmss(_pfString);
							return true;
						}
					}
				),
				/*
				 * SETTOS tos
				 */
				Sequence(
					SETTOS(),
					WhiteSpaces(),
					PfTos(),
					new Action() {
						public boolean run(Context context) {
							_scrubOpts.setSettos(_pfString);
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_scrubOpts.setReassemble_tcp(true);
							return true;
						}
					}
				),
				/*
				 * RANDOMID
				 */
				Sequence(
					RANDOMID(),
					new Action() {
						public boolean run(Context context) {
							_scrubOpts.setRandomid(true);
							return true;
						}
					}
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
				new Action() {
					public boolean run(Context context) {
						_pfRule = new RuleTemplate();
						return true;
					}
				},
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
						new Action() {
							public boolean run(Context context) {
								_pfRule.setDir(context.getPrevText());
								return true;
							}
						}
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
				new Action() {
					public boolean run(Context context) {
						_ruleName="pfrule";
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_icmpspec.clear();
						_icmp6spec.clear();
						_flags = null;
						_flagset = null;
						_stateOptSpec.clear();
						return true;
					}
				},			
				PfFilterOptsl(),
				new Action() {
					public boolean run (Context context) {
							_pfRule.getFilterOpts().setFlags(_flags);
							_pfRule.getFilterOpts().setFlagset(_flagset);
							_pfRule.getFilterOpts().getIcmpspec().addAll(_icmpspec);
							_pfRule.getFilterOpts().getIcmp6spec().addAll(_icmp6spec);
							return true;
					}
				}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setTos(_pfString);
							return true;
						}
					},
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setFragment(true);
							return true;
						}
					}
				),
				/*
				 * ALLOWOPTS
				 */
				Sequence(
					ALLOWOPTS(),
					SkipSpaces(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setAllowopts(true);
							return true;
						}
					}
				),
				/*
				 * LABEL label
				 */
				Sequence(
					LABEL(),
					WhiteSpaces(),
					PfLabel(),
					SkipSpaces(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setLabel(_pfString);
							return true;
						}
					}
				),
				/*
				 * QUEUE qname
				 */
				Sequence(
					QUEUE(),
					WhiteSpaces(),
					PfQname(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setQname(_pfQname);
							_pfRule.getFilterOpts().setPQname(_pfPQname);
							return true;
						}
					}
				),
				/*
				 * TAG string
				 */
				Sequence(
					TAG(),
					WhiteSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setTag(_pfString);
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setMatchTagNot(_pfnot);
							_pfRule.getFilterOpts().setMatchTag(_pfString);
							return true;
						}
					}
				),
				/*
				 * PROBABILITY probability
				 */
				Sequence(
					PROBABILITY(),
					WhiteSpaces(),
					PfProbability(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setProbability(_pfString);
							return true;
						}
					}
				),
				/*
				 * RTABLE NUMBER
				 */
				Sequence(
					RTABLE(),
					WhiteSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setRtableId(_pfString);
							return true;
						}
					}
				),
				/*
				 * DIVERTTO STRING PORT portplain
				 */
				Sequence(
					DIVERTTO(),
					WhiteSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setDivertAddr(_pfString);
							return true;
						}
					},
					WhiteSpaces(),
					PORT(),
					WhiteSpaces(),
					PfPortPlain(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setDivertPort(_pfString);
							return true;
						}
					}
				),
				/*
				 * DIVERTREPLY
				 */
				Sequence(
					DIVERTREPLY(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setDivertPort("1");
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setDivertPacketPort(_pfString);
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setScrubOpts(_scrubOpts);
							return true;
						}
					}
				),
				/*
				 * NATTO redirpool pool_opts
				 */
				Sequence(
					NATTO(),
					WhiteSpaces(),
					PfRedirPool(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setNat(_redirSpec);
							return true;
						}
					},
					Optional(
						Sequence(
							SkipSpaces(),
							PfPoolOpts(),
							new Action() {
								public boolean run(Context context) {
									_redirSpec.setPoolOpts(_poolOpts);
									return true;
								}
							}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setRdr(_redirSpec);
							return true;
						}
					},
					Optional(
						Sequence(
							SkipSpaces(),
							PfPoolOpts(),
							new Action() {
								public boolean run(Context context) {
									_redirSpec.setPoolOpts(_poolOpts);
									return true;
								}
							}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setNat(_redirSpec);
							_pfRule.getFilterOpts().setBinat(true);
							return true;
						}
					},
					Optional(
						Sequence(
							SkipSpaces(),
							PfPoolOpts(),
							new Action() {
								public boolean run(Context context) {
									_redirSpec.setPoolOpts(_poolOpts);
									_poolOpts.setStaticPort(true);
									return true;
								}
							}
						)
					)
				),
				/*
				 * FASTROUTE
				 */
				Sequence(
					FASTROUTE(),
					new Action() {
						public boolean run(Context context) {
							_routeOpts = new RouteOptsTemplate();
							_routeOpts.setRt(PfConst.PF_FASTROUTE);
							_pfRule.getFilterOpts().setRouteOpts(_routeOpts);
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_routeOpts.setRt(PfConst.PF_ROUTETO);
							_routeOpts.setPoolOpts(_poolOpts);
							_pfRule.getFilterOpts().setRouteOpts(_routeOpts);
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_routeOpts.setRt(PfConst.PF_REPLYTO);
							_routeOpts.setPoolOpts(_poolOpts);
							_pfRule.getFilterOpts().setRouteOpts(_routeOpts);
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_routeOpts.setRt(PfConst.PF_DUPTO);
							_routeOpts.setPoolOpts(_poolOpts);
							_pfRule.getFilterOpts().setRouteOpts(_routeOpts);
							return true;
						}
					}
				),
				/*
				 * RECEIVEDON if_item
				 */
				Sequence(
					RECEIVEDON(),
					WhiteSpaces(),
					PfIfItem(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setRcv(_pfString);
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.setAction("pass");
							return true;
						}
					}
				),
				/*
				 * MATCH
				 */
				Sequence(
					MATCH(),
					new Action() {
						public boolean run(Context context) {
							_pfRule.setAction("match");
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.setAction("block");
							return true;
						}
					}
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
				new Action() {
					public boolean run(Context context) {
						_pfRule.setQuick(true);
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						String ifname = _ifItem;
						if (_pfnot)
							ifname = "!" + ifname;
						_pfRule.getIfList().add(ifname);
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_ifItem = _pfString;
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_pfRule.setAf(context.getPrevText());
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_pfRule.getProtoList().add(_pfString);
						return true;
					}
				}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.setAll(true);
							return true;
						}
					}
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
				new Action() {
					public boolean run(Context context) {
						_pfRule.getOsList().add(_pfString);
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_pfRule.getSourceHostList().addAll(_ipspec);
						_pfRule.getSourcePortList().addAll(_portspec);
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_pfRule.getDestHostList().addAll(_ipspec);
						_pfRule.getDestPortList().addAll(_portspec);
						return true;
					}
				}
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
				new Action() {
					public boolean run(Context context) {
						_ipspec.clear();
						_portspec.clear();
						return true;
					}
				},
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
						new Action() {
							public boolean run(Context context) {
								// ipspec PORT port may have change _ippsec
								_ipspec.clear();
								return true;
							}
						},
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
						Sequence(
							Ch('\n'),
							SkipSpaces()
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
				new Action() {
					public boolean run(Context context) {
						_pfXhost = new Xhost();
						return true;
					}
				},
				FirstOf(
					/*
					 * ANY
					 */
					Sequence(
						ANY(),
						new Action() {
							public boolean run(Context context) {
								_pfXhost.setAny(true);
								_ipspec.add(_pfXhost);
								return true;
							}
						}
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
					new Action() {
						public boolean run(Context context) {
							_pfXhost.setNot(_pfnot);
							_pfXhost.setNoroute(true);
							_ipspec.add(_pfXhost);
							return true;
						}
					}
				),
				/*
				 * not URPFFAILED
				 */
				Sequence(
					PfNot(),
					SkipSpaces(),
					URPFFAILED(),
					new Action() {
						public boolean run(Context context) {
							_pfXhost.setNot(_pfnot);
							_pfXhost.setUrpffailed(true);
							_ipspec.add(_pfXhost);
							return true;
						}
					}
				),
				/*
				 * not host
				 */
				Sequence(
					PfNot(),
					SkipSpaces(),
					PfHost(),
					new Action() {
						public boolean run(Context context) {
							_pfXhost.setNot(_pfnot);
							_ipspec.add(_pfXhost);
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfXhost = new Xhost();
							_pfXhost.setFirstAddress(_pfString);
							return true;
						}
					},
					SkipSpaces(),
					Ch('-'),
					SkipSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfXhost.setLastAddress(_pfString);
							return true;
						}
					}
				),
				/*
				 * STRING '/' STRING
				 */
				Sequence(
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfXhost = new Xhost();
							_pfXhost.setFirstAddress(_pfString);
							return true;
						}
					},
					SkipSpaces(),
					Ch('/'),
					SkipSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							String addr = _pfXhost.getFirstAddress() +
									"/" + _pfString;
							_pfXhost.setFirstAddress(addr);
							return true;
						}
					}
				),
				/*
				 * '<' STRING '>'
				 */
				Sequence(
					Ch('<'),
					SkipSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfXhost = new Xhost();
							_pfXhost.setTable(_pfString);
							return true;
						}
					},
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
					new Action() {
						public boolean run(Context context) {
							_pfXhost = new Xhost();
							_pfXhost.setRoute(_pfString);
							return true;
						}
					}
				),
				/*
				 * dynaddr
				 * '(' STRING ')' ['/' STRING]
				 */
				Sequence(
					Ch('('),
					SkipSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfXhost = new Xhost();
							_pfXhost.setDynaddr(_pfString);
							return true;
						}
					},
					SkipSpaces(),
					Ch(')'),
					SkipSpaces(),
					Optional(
						Sequence(
							Ch('/'),
							SkipSpaces(),
							PfString(),
							new Action() {
								public boolean run(Context context) {
									_pfXhost.setDynaddrMask(_pfString);
									return true;
								}
							}
						)
					)
				),
				/*
				 * STRING
				 */
				Sequence(
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfXhost = new Xhost();
							_pfXhost.setFirstAddress(_pfString);
							return true;
						}
					}
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
						new Action() {
							public boolean run(Context context) {
								_pfPortItem = new PortItemTemplate();
								_pfPortItem.setOperator(context.getPrevText());
								return true;
							}
						},
						SkipSpaces(),
						PfPortRange(),
						new Action() {
							public boolean run(Context context) {
								_pfPortItem.setFirstPort(_pfString);
								return true;
							}
						}
					),
					/*
					 * portrange PORTBINARY portrange
					 */
					Sequence(
						PfPortRange(),
						SkipSpaces(),
						PfPortBinary(),
						new Action() {
							public boolean run(Context context) {
								_pfPortItem = new PortItemTemplate();
								_pfPortItem.setFirstPort(_pfString);
								_pfPortItem.setOperator(context.getPrevText());
								return true;
							}
						},
						SkipSpaces(),
						PfPortRange(),
						new Action() {
							public boolean run(Context context) {
								_pfPortItem.setLastPort(_pfString);
								return true;
							}
						}
					),
					/*
					 * portrange
					 */
					Sequence(
						PfPortRange(),
						new Action() {
							public boolean run(Context context) {
								_pfPortItem = new PortItemTemplate();
								/*
								 * XXX: why ':' is not in PORTBINARY?
								 */
								int p = _pfString.indexOf(':');
								if (p > 0 && p < (_pfString.length() -1)) {
									_pfPortItem.setOperator(":");
									String first = _pfString.substring(0, p);
									String last = _pfString.substring(p + 1);
									_pfPortItem.setFirstPort(first);
									_pfPortItem.setLastPort(last);
								} else
									_pfPortItem.setFirstPort(_pfString);
								return true;
							}
						}
					)
				),
				new Action() {
					public boolean run(Context context) {
						_portspec.add(_pfPortItem);
						return true;
					}
				}
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
					new Action() {
						public boolean run(Context context) {
							_flags = "any";
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_flagset = _pfString;
							return true;
						}
					}
				),
				/*
				 * FLAGS flag '/' flag
				 */
				Sequence(
					FLAGS(),
					SkipSpaces(),
					PfFlag(),
					new Action() {
						public boolean run(Context context) {
							_flags = _pfString;
							return true;
						}
					},
					SkipSpaces(),
					Ch('/'),
					SkipSpaces(),
					PfFlag(),
					new Action() {
						public boolean run(Context context) {
							_flagset = _pfString;
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_icmpspec.add(_pfIcmpItem);
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_icmp6spec.add(_pfIcmpItem);
							return true;
						}
					}
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
				new Action() {
					public boolean run(Context context) {
						_icmpspec.add(_pfIcmpItem);
						return true;
					}
				},
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
				new Action() {
					public boolean run(Context context) {
						_icmp6spec.add(_pfIcmpItem);
						return true;
					}
				},
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
				new Action() {
					public boolean run(Context context) {
						_pfIcmpItem = new IcmpItem();
						return true;
					}
				},
				PfIcmpType(),
				new Action() {
					public boolean run(Context context) {
						_pfIcmpItem.setIcmpType(_pfString);
						return true;
					}
				},
				/*
				 * [CODE STRING]
				 */
				SkipSpaces(),
				Optional(
					Sequence(
						CODE(),
						WhiteSpaces(),
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfIcmpItem.setIcmpCode(_pfString);
								return true;
							}
						},
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setAction("no-state");
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setAction("keep-state");
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setAction("modulate-state");
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfRule.getFilterOpts().setAction("synproxy-state");
							return true;
						}
					}
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
				new Action() {
					public boolean run(Context context) {
						_pfStateOptItem = "";
						return true;
					}
				},
				FirstOf(
					/*
					 * MAXIMUM NUMBER
					 */
					Sequence(
						MAXIMUM(),
						WhiteSpaces(),
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "MAXIMUM " + _pfString;
								return true;
							}
						}
					),
					/*
					 * NOSYNC
					 */
					Sequence(
						NOSYNC(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "NOSYNC";
								return true;
							}
						}
					),
					/*
					 * MAXSRCSTATES NUMBER
					 */
					Sequence(
						MAXSRCSTATES(),
						WhiteSpaces(),
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "MAXSRCSTATES " + _pfString;
								return true;
							}
						}
					),
					/*
					 * MAXSRCCONN NUMBER
					 */
					Sequence(
						MAXSRCCONN(),
						WhiteSpaces(),
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "MAXSRCCONN " + _pfString;
								return true;
							}
						}
					),
					/*
					 * MAXSRCCONNRATE NUMBER '/' NUMBER
					 */
					Sequence(
						MAXSRCCONNRATE(),
						WhiteSpaces(),
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "MAXSRCCONNRATE " + _pfString;
								return true;
							}
						},
						SkipSpaces(),
						Ch('/'),
						SkipSpaces(),
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem += " " + _pfString;
								return true;
							}
						}
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
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "OVERLOAD " +
									"<" + _pfString + ">";
								return true;
							}
						},
						SkipSpaces(),
						Optional(
							Sequence(
								PfFlush(),
								new Action() {
									public boolean run(Context context) {
										_pfStateOptItem = _pfStateOptItem +
											" " + context.getPrevText();
										return true;
									}
								}
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
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "MAXSRCNODES " + _pfString;
								return true;
							}
						}
					),
					/*
					 * SOURCETRACK sourcetrack
					 */
					Sequence(
						SOURCETRACK(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "SOURCETRACK";
								return true;
							}
						},
						Optional(
							Sequence(
								WhiteSpaces(),
								PfSourceTrack(),
								new Action() {
									public boolean run(Context context) {
										_pfStateOptItem = _pfStateOptItem +
											" " + context.getPrevText();
										return true;
									}
								}
							)
						)
					),
					/*
					 * statelock
					 */
					Sequence(
						PfStateLock(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "STATELOCK " +
									context.getPrevText();
								return true;
							}
						}
					),
					/*
					 * SLOPPY
					 */
					Sequence(
						SLOPPY(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "SLOPPY";
								return true;
							}
						}
					),
					/*
					 * PFLOW
					 */
					Sequence(
						PFLOW(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "PFLOW";
								return true;
							}
						}
					),
					/*
					 * STRING NUMBER
					 */
					Sequence(
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = "STRING " + _pfString;
								return true;
							}
						},
						WhiteSpaces(),
						PfString(),
						new Action() {
							public boolean run(Context context) {
								_pfStateOptItem = _pfStateOptItem +
									" " +  _pfString;
								return true;
							}
						}
					)
				),
				new Action() {
					public boolean run(Context context) {
						_pfRule.getFilterOpts().getOptions().add(_pfStateOptItem);
						return true;
					}
				}
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
					new Action() {
						public boolean run(Context context) {
							_pfQname = _pfString;
							_pfPQname = null;
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfQname = _pfString;
							_pfPQname = null;
							return true;
						}
					}
				),
				/*
				 * '(' STRING comma STRING ')'
				 */
				Sequence(
					Ch('('),
					SkipSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfQname = _pfString;
							_pfPQname = null;
							return true;
						}
					},
					SkipSpaces(),
					PfComma(),
					SkipSpaces(),
					PfString(),
					SkipSpaces(),
					Ch(')'),
					new Action() {
						public boolean run(Context context) {
							_pfPQname = _pfString;
							return true;
						}
					}
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
				new Action() {
					public boolean run(Context context) {
						_redirHosts.clear();
						return true;
					}
				},
				FirstOf(
					Sequence(
						PfHost(),
						new Action() {
							public boolean run(Context context) {
								_redirHosts.add(_pfXhost);
								return true;
							}
						}
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
				new Action() {
					public boolean run(Context context) {
						_redirHosts.add(_pfXhost);
						return true;
					}
				},
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
				new Action() {
					public boolean run(Context context) {
						_redirSpec = new RedirSpecTemplate();
						return true;
					}
				},
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
						new Action() {
							public boolean run(Context context) {
								_redirSpec.getHosts().addAll(_redirHosts);
								_redirSpec.setPortstar(_pfString);
								return true;
							}
						}
					),
					/*
					 * redirspec
					 */
					Sequence(
						PfRedirSpec(),
						new Action() {
							public boolean run(Context context) {
								_redirSpec.getHosts().addAll(_redirHosts);
								return true;
							}
						}
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
				new Action() {
					public boolean run(Context context) {
						_poolOpts = new PoolOptsTemplate();
						return true;
					}
				},
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
					new Action() {
						public boolean run(Context context) {
							_poolOpts.setType(PfConst.PF_POOL_BITMASK);
							return true;
						}
					}
				),
				/*
				 * RANDOM
				 */
				Sequence(
					RANDOM(),
					new Action() {
						public boolean run(Context context) {
							_poolOpts.setType(PfConst.PF_POOL_RANDOM);
							return true;
						}
					}
				),
				/*
				 * SOURCEHASH [hashkey]
				 */
				Sequence(
					SOURCEHASH(),
					new Action() {
						public boolean run(Context context) {
							_poolOpts.setType(PfConst.PF_POOL_SRCHASH);
							return true;
						}
					},
					SkipSpaces(),
					Optional(
						Sequence(
							PfString(),
							new Action() {
								public boolean run(Context context) {
									_poolOpts.setKey(_pfString);
									return true;
								}
							}
						)
					)
				),
				/*
				 * ROUNDROBIN
				 */
				Sequence(
					ROUNDROBIN(),
					new Action() {
						public boolean run(Context context) {
							_poolOpts.setType(PfConst.PF_POOL_ROUNDROBIN);
							return true;
						}
					}
				),
				/*
				 * STATICPORT
				 */
				Sequence(
					STATICPORT(),
					new Action() {
						public boolean run(Context context) {
							_poolOpts.setStaticPort(true);
							return true;
						}
					}
				),
				/*
				 * STICKYADDRESS
				 */
				Sequence(
					STICKYADDRESS(),
					new Action() {
						public boolean run(Context context) {
							_poolOpts.setOpts(_poolOpts.getOpts() |
								PfConst.PF_POOL_STICKYADDR);
							return true;
						}
					}
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
					new Action() {
						public boolean run(Context context) {
							_pfXhost = new Xhost();
							_pfXhost.setFirstAddress(_pfString);
							return true;
						}
					},
					Ch('/'),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							String addr = _pfXhost.getFirstAddress() +
									"/" + _pfString;
							_pfXhost.setFirstAddress(addr);
							return true;
						}
					}
				),
				/*
				 * '<' STRING '>'
				 */
				Sequence(
					Ch('<'),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfXhost = new Xhost();
							_pfXhost.setTable(_pfString);
							return true;
						}
					},
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
					new Action() {
						public boolean run(Context context) {
							_pfXhost = new Xhost();
							_pfXhost.setDynaddr(_pfString);
							return true;
						}
					},
					SkipSpaces(),
					Ch(')'),
					SkipSpaces(),
					Optional(
						Sequence(
							Ch('/'),
							PfString(),
							new Action() {
								public boolean run(Context context) {
									_pfXhost.setDynaddrMask(_pfString);
									return true;
								}
							}
						)
					)
				),
				/*
				 * STRING
				 */
				Sequence(
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_pfXhost = new Xhost();
							_pfXhost.setFirstAddress(_pfString);
							return true;
						}
					}
				),
				/*
				 *	'(' STRING host ')'
				 */
				Sequence(
					Ch('('),
					SkipSpaces(),
					PfString(),
					new Action() {
						public boolean run(Context context) {
							_ifname  = _pfString;
							return true;
						}
					},
					WhiteSpaces(),
					PfHost(),
					new Action() {
						public boolean run(Context context) {
							_pfXhost.setIfName(_ifname);
							return true;
						}
					},
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
				new Action() {
					public boolean run(Context context) {
						_routeOpts.getHosts().add(_pfXhost);
						return true;
					}
				},
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
				new Action() {
					public boolean run(Context context) {
						_routeOpts = new RouteOptsTemplate();
						return true;
					}
				},
				FirstOf(
					Sequence(
						PfRouteHost(),
						new Action() {
							public boolean run(Context context) {
								_routeOpts.getHosts().add(_pfXhost);
								return true;
							}
						}
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
				new Action() {
					public boolean run(Context context) {
						_pfString = _lastString;
						return true;
					}
				}
			),
			Sequence(
				TestNot(
					PfKeyword()
				),
				PfAtom(),
				new Action() {
					public boolean run(Context context) {
						_pfString = context.getPrevText();
						return true;
					}
				}
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
			CharSet("'\""),
			new Action() {
				public boolean run(Context context) {
					_quotec = context.getPrevText().charAt(0);
					_lastString = "";
					_previousChar = '\0';
					_pfStringEnd = false;
					return true;
				}
			},
			OneOrMore(
				Sequence(
					new Action() {
						public boolean run(Context context) {
							return !_pfStringEnd;
						}
					},
					Any(),
					new Action() {
						public boolean run(Context context) {
							char c = context.getPrevText().charAt(0);
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
					}
				)
			)
		);
	}

}
