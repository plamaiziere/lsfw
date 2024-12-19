/*
 * Copyright (c) 2013, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.parsers.CommonRules;
import org.parboiled.Rule;

/**
 * Checkpoint parser
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CpParser extends CommonRules<Object> {

    protected PortItemTemplate _portItem;

    protected boolean clear() {
        _portItem = null;
        return true;
    }

    protected boolean newPortItem() {
        _portItem = new PortItemTemplate();
        return true;
    }

    protected boolean portItemFromRange(String portRange) {
        int p = portRange.indexOf('-');
        if (p > 0 && p < (portRange.length() - 1)) {
            _portItem.setOperator("-");
            String first = portRange.substring(0, p);
            String last = portRange.substring(p + 1);
            _portItem.setFirstPort(first);
            _portItem.setLastPort(last);
        } else
            _portItem.setFirstPort(portRange);
        return true;
    }

    public PortItemTemplate getPortItem() {
        return _portItem;
    }

    /**
     * Matches port_item
     * <p>
     * port_item : portrange
     * | unaryop portrange
     * | portrange portrange
     *
     * @return a Rule
     */
    public Rule CpPortItem() {
        return
                Sequence(
                        newPortItem(),
                        FirstOf(
                                /*
                                 * unaryop portrange
                                 */
                                Sequence(
                                        CpUnaryOp(),
                                        _portItem.setOperator(match()),
                                        SkipSpaces(),
                                        StringAtom(),
                                        portItemFromRange(match())
                                ),
                                /*
                                 * portrange
                                 */
                                Sequence(
                                        StringAtom(),
                                        portItemFromRange(match())
                                )
                        ),
                        EOI
                );
    }

    /**
     * Matches unaryop
     * unaryop : '<' | '>'
     *
     * @return a Rule
     */
    public Rule CpUnaryOp() {
        return
                FirstOf(
                        String("<"),
                        String(">")
                );
    }

}
