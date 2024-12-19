/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

import fr.univrennes1.cri.jtacl.equipments.generic.GenericEquipmentShellParser;
import org.parboiled.Rule;

/**
 * PIX Jtacl sub shell parser
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PixShellParser extends GenericEquipmentShellParser {

    @Override
    protected boolean clear() {
        return super.clear();
    }

    public Rule CommandLine() {
        return
                Sequence(
                        clear(),
                        FirstOf(
                                CommandHelp(),
                                CommandXrefIp(),
                                CommandXrefService(),
                                CommandShow()
                        )
                );
    }

    /**
     * show (names | enhanced-service | icmp-group | network-group | protocol-group
     * | service-group) | (used | unused)
     */
    Rule CommandShow() {
        return Sequence(
                IgnoreCase("show"),
                WhiteSpaces(),
                FirstOf(
                        IgnoreCase("name"),
                        IgnoreCase("enhanced-service"),
                        IgnoreCase("icmp-group"),
                        IgnoreCase("network-group"),
                        IgnoreCase("protocol-group"),
                        IgnoreCase("service-group")
                ),
                setCommand("show-".concat(match().toLowerCase())),
                Optional(
                        Sequence(
                                WhiteSpace(),
                                FirstOf(
                                        IgnoreCase("used"),
                                        IgnoreCase("unused")
                                ),
                                getParam().add(match().toLowerCase())
                        )
                ),
                EOI
        );
    }

}
