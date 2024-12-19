/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.generic;

import java.util.List;

import junit.framework.TestCase;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class GenericEquipmentShellParserTest extends TestCase {

    GenericEquipmentShellParser parser =
            Parboiled.createParser(GenericEquipmentShellParser.class);
    ParsingResult<?> result;

    public GenericEquipmentShellParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of expandFormat method, of class GenericEquipmentShellParser.
     */
    public void testExpandFormat() {
        System.out.println("expandFormat");
        String format = ";%i %o";
        GenericEquipmentShellParser instance = new GenericEquipmentShellParser();

        List<String> fmtresult = GenericEquipmentShellParser.expandFormat(format);
        assertEquals(";", fmtresult.get(0));
        assertEquals("%i", fmtresult.get(1));
        assertEquals(" ", fmtresult.get(2));
        assertEquals("%o", fmtresult.get(3));
    }

    /**
     * Test of xref ip.
     */
    public void testXrefIP() {
        System.out.println("xref-ip");

        ReportingParseRunner parseRunner =
                new ReportingParseRunner(parser.CommandXrefIp());
        String line = "xref    ip     fmt  \"toto\"    host     IP";

        result = parseRunner.run(line);
        assertTrue(result.matched);
        assertEquals("xref-ip", parser.getCommand());
    }

    /**
     * Test of xref service.
     */
    public void testXrefService() {
        System.out.println("xref-service");

        ReportingParseRunner parseRunner =
                new ReportingParseRunner(parser.CommandXrefService());
        String line = "xref    service";

        result = parseRunner.run(line);
        assertTrue(result.matched);
        assertEquals("xref-service", parser.getCommand());
    }

}
