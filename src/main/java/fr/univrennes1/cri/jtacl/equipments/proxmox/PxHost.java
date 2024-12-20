/*
 * Copyright (c) 2021, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.proxmox;

import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipmentsByName;
import fr.univrennes1.cri.jtacl.lib.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxmox host equipment (hyperviser / cluster)
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PxHost extends PxEquipment {

    @Override
    public boolean isHost() {
        return true;
    }

    NetworkEquipmentsByName _guests = new NetworkEquipmentsByName();

    /**
     * Create a new {@link PxHost} with this name and this comment.<br/>
     *
     * @param monitor               the {@link Monitor} monitor associated with this equipment.
     * @param name                  the name of the equipment.
     * @param comment               a free comment for this equipment.
     * @param configurationFileName name of the configuration file to use (may be null).
     */
    public PxHost(Monitor monitor, String name, String comment, String configurationFileName) {
        super(monitor, name, comment, configurationFileName, null);
    }

    @Override
    public NetworkEquipmentsByName configure() {
        if (_configurationFileName.isEmpty())
            return null;

        /*
         * Read the XML configuration file
         */
        famAdd(_configurationFileName);
        Document doc = XMLUtils.getXMLDocument(_configurationFileName);
        loadOptionsFromXML(doc);
        loadFiltersFromXML(doc);
        loadConfiguration(doc);
        if (_monitor.getOptions().getXref()) {
            crossReference();
        }

        // default policy
        PxRule policyIn = PxRule.ofImplicitRule(null, PxRuleDirection.IN, _options.getPolicyIn());
        PxRule policyOut = PxRule.ofImplicitRule(null, PxRuleDirection.OUT, _options.getPolicyOut());
        _rules.add(policyIn);
        _rules.add(policyOut);

        loadGuests(doc);
        return _guests;
    }

    protected void loadConfiguration(Document doc) {

        /* policy */
        NodeList list = doc.getElementsByTagName("policy");
        if (list.getLength() != 1) {
            throwCfgException("One policy must be specified", false);
        }

        List<String> filenames = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String filename = e.getAttribute("filename");
            if (!filename.isEmpty()) {
                filenames.add(filename);
            }
        }

        if (filenames.isEmpty()) {
            throwCfgException("Missing policy file name", false);
        }

        famAdd(filenames.get(0));
        parsePolicy(filenames.get(0));
    }

    protected void loadGuests(Document doc) {

        /* guests */
        NodeList list = doc.getElementsByTagName("guests");
        if (list.getLength() < 1) {
            throwCfgException("At least one guests must be specified", false);
        }

        List<String> filenames = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String filename = e.getAttribute("directory");
            if (!filename.isEmpty()) {
                filenames.add(filename);
            }
        }

        if (filenames.isEmpty()) {
            throwCfgException("Missing guests directory file name", false);
        }

        for (String f : filenames) {
            _guests.putAll(createGuestsEquipments(f));
            famAdd(f);
        }
    }

    protected NetworkEquipmentsByName createGuestsEquipments(String directory) {

        NetworkEquipmentsByName equipments = new NetworkEquipmentsByName();
        File dir = new File(directory);
        File[] files = dir.listFiles();
        if (files == null) {
            throwCfgException("Cannot read directory " + directory, false);
        }
        for (File f : files) {
            if (!f.isFile() || !f.getAbsolutePath().endsWith(".xml"))
                continue;

            String name = f.getName().split(".xml")[0];
            NetworkEquipment guest = new PxGuest(_monitor, getName() + "." + name
                    , name, dir.getAbsolutePath(), f.getAbsolutePath(), this);
            guest.configure();
            equipments.put(guest);
        }
        return equipments;
    }
}
