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

package fr.univrennes1.cri.jtacl.equipments.generic;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclConfigurationException;
import fr.univrennes1.cri.jtacl.core.monitor.Log;
import fr.univrennes1.cri.jtacl.core.monitor.Monitor;
import fr.univrennes1.cri.jtacl.core.network.Iface;
import fr.univrennes1.cri.jtacl.core.network.IfaceLink;
import fr.univrennes1.cri.jtacl.core.network.IfaceLinksByIp;
import fr.univrennes1.cri.jtacl.core.network.IfacesByName;
import fr.univrennes1.cri.jtacl.core.network.NetworkEquipment;
import fr.univrennes1.cri.jtacl.core.network.Route;
import fr.univrennes1.cri.jtacl.core.network.RoutingEngine;
import fr.univrennes1.cri.jtacl.core.network.ShowableRoutes;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp4;
import fr.univrennes1.cri.jtacl.lib.ip.IPIcmp6;
import fr.univrennes1.cri.jtacl.lib.ip.IPNet;
import fr.univrennes1.cri.jtacl.lib.ip.IPProtocols;
import fr.univrennes1.cri.jtacl.lib.ip.IPServices;
import fr.univrennes1.cri.jtacl.lib.misc.FilesMonitor;
import fr.univrennes1.cri.jtacl.lib.misc.KeyValue;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The generic equipment provides common methods and services for an equipment.
 * You may extend it to implement a new equipment.<br/>
 *
 * It provides:
 * <ul>
 * <li>a routing engine and the ability to add routes from the XML configuration
 * file.</li>
 * <li>access to options defined by a pair of key/value (still from XML).</li>
 * </ul>
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class GenericEquipment extends NetworkEquipment {

	/**
	 * writer used to dump the configuration to a file, may be null.
	 */
	private BufferedWriter _dumpConfigurationWriter;

	/**
	 * Routing engine.
	 */
	protected RoutingEngine _routingEngine;

	/**
	 * services database
	 */
	protected IPServices _ipServices;

	/**
	 * protocols database
	 */
	protected IPProtocols _ipProtocols;

	/**
	 * icmp type database (ipv4)
	 */
	protected IPIcmp _ipIcmp4Types;

	/**
	 * icmp type database (ipv6)
	 */
	protected IPIcmp _ipIcmp6Types;

	/**
	 * Filters are regexp uses to filter-out some input from configuration.
	 * @see #loadFiltersFromXML(org.w3c.dom.Document)
	 */
	 protected List<Pattern> _filters = new ArrayList<Pattern>();

	/**
	 * Options are pair of key, value.<br/>
	 * You are free to define more options for an equipment, but
	 * see below for predefined common options.
	 *
	 * @see #loadOptionsFromXML(org.w3c.dom.Document)
	 */
	protected Map<String, KeyValue> _options = new HashMap<String, KeyValue>();

	/**
	 * sub shell registered on this equipment.
	 */
	protected List<GenericEquipmentShell> _shells
			= new ArrayList<GenericEquipmentShell>();

	/**
	 * File alteration monitor for this equipment
	 */
	protected FilesMonitor _fam = new FilesMonitor();

	/**
	 * option dump-configuration value = file name.<br/>
	 * If set, the equipment module should dump the lines of configuration taken
	 * in account to a file. It is useful for diagnostic and debugging purpose.
	 * @see #dumpConfiguration(java.lang.String)
	 */
	static final String OPT_DUMPCONFIG = "dump-configuration";

	/**
	 * option parse-only value = true | false.<br/>
	 * If true, the equipment module must parse configurations files only
	 * without interpretating them. Usefull for debugging.
	 */
	static final String OPT_PARSEONLY = "parse-only";

	/**
	 * Returns true if option "parse-only" is set to true.
	 * @return true if option parse-only is set to true.
	 */
	protected boolean hasOptParseOnly() {
		KeyValue kvalue = _options.get(GenericEquipment.OPT_PARSEONLY);
		return kvalue != null && kvalue.getValue().equals("true");
	}

	protected void dumpConfiguration(String line) {

		/*
		 * open a writer if needed.
		 */
		if (_dumpConfigurationWriter == null) {
			if (_options.containsKey(GenericEquipment.OPT_DUMPCONFIG)) {
				FileWriter writer;
				String fileName = _options.get(GenericEquipment.OPT_DUMPCONFIG).getValue();
				try {
					writer = new FileWriter(fileName);
					_dumpConfigurationWriter = new BufferedWriter(writer);
				} catch (IOException ex) {
					throw new JtaclConfigurationException("Cannot write to file:" +
						fileName + " " + ex.getMessage());
				}
			}
		}
		if (_dumpConfigurationWriter == null)
			return;
		/*
		 * write the line
		 */
		try {
			_dumpConfigurationWriter.write(line);
			_dumpConfigurationWriter.newLine();
			_dumpConfigurationWriter.flush();
		} catch (IOException ex) {
			throw new JtaclConfigurationException("cannot write to file: " +
				_options.get(GenericEquipment.OPT_DUMPCONFIG).getValue() +
				" " + ex.getMessage());
		}
	}

	/**
	 * Creates a route from an XML element.
	 * @param element XML element.
	 * @return a new route
	 */
	protected Route<IfaceLink> routeFromXmlElement(Element element) {
		String tagName = element.getTagName();
		String sprefix = element.getAttribute("prefix");
		String snexthop = element.getAttribute("nexthop");
		String smetric = element.getAttribute("metric");
		String sLink = element.getAttribute("link");

		String s = "equipment: " + _name +
				" " + tagName +
				" prefix: " + sprefix +
				" nexthop: " + snexthop +
				" link: " + sLink +
				" metric: " + smetric;

		if (sprefix.isEmpty())
			throw new JtaclConfigurationException("Missing route prefix: " + s);

		if (snexthop.isEmpty())
			throw new JtaclConfigurationException("Missing route nexthop: " + s);

		if (smetric.isEmpty())
			smetric="1";

		IPNet prefix;
		try {
			prefix = new IPNet(sprefix);
		} catch (UnknownHostException ex) {
			throw new JtaclConfigurationException("Invalid route prefix: " + s);
		}

		/*
		 * null route 
		 */
		if (snexthop.equalsIgnoreCase("null-route")) {
			Route<IfaceLink> route = new Route<IfaceLink>(prefix);
			return route;
		}
		
		IPNet nexthop;
		try {
			nexthop = new IPNet(snexthop);
		} catch (UnknownHostException ex) {
			throw new JtaclConfigurationException("Invalid route nexthop: " + s);
		}

		int metric;
		try {
			metric = Integer.valueOf(smetric);
		} catch (NumberFormatException ex) {
			throw new JtaclConfigurationException("Invalid route metric: " + s);
		}

		Iface iface = null;
		IfaceLink link = null;

		/*
		 * If no link was specified, use the directly connected network
		 * containing nextHop
		 */
		if (sLink.isEmpty()) {
			try {
				iface = getIfaceConnectedTo(nexthop);
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid route " +
						s + " " + ex.getMessage());
			}
			if (iface == null) {
				throw new JtaclConfigurationException("Invalid route, nexthop" +
						" is not on a subnet of this equipment: " + s);
			}

			try {
				link = iface.getLinkConnectedTo(nexthop);
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid route " +
						s + " " + ex.getMessage());
			}
		} else {
			/*
			 * link specified
			 */
			IPNet ip = null;
			try {
				ip = new IPNet(sLink);
			} catch (UnknownHostException ex) {
				throw new JtaclConfigurationException("Invalid route " +
						s + " " + ex.getMessage());
			}
			link = getIfaceLink(ip);
		}

		if (link == null)
			throw new JtaclConfigurationException("Invalid route: coud not find link " + s);

		Route<IfaceLink> route = new Route<IfaceLink>(prefix, nexthop, metric, link);
		return route;
	}
	/**
	 * Loads routes and source-routes from an XML document.
	 * @param doc document to use.
	 * @throws JtaclConfigurationException if error occurs.
	 */
	protected void loadRoutesFromXML(Document doc) {

		NodeList list = doc.getElementsByTagName("route");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			Route<IfaceLink> route = routeFromXmlElement(e);
			Log.debug().info(_name + " add route: " + route.toString());
			_routingEngine.addRoute(route);
		}

		list = doc.getElementsByTagName("source-route");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			Route<IfaceLink> route = routeFromXmlElement(e);
			Log.debug().info(_name + " add source-route: " + route.toString());
			_routingEngine.addSourceRoute(route);
		}
	}

	/**
	 * Loads filters from an XML document.
	 * <filter pattern="regular expression" />
	 */
	protected void loadFiltersFromXML(Document doc) {
		_filters.clear();
		NodeList list = doc.getElementsByTagName("filter");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String sregexp= e.getAttribute("pattern");

			if (sregexp.isEmpty())
				throw new JtaclConfigurationException("Missing pattern");

			Pattern pattern = Pattern.compile(sregexp);
			_filters.add(pattern);
		}
	}

	/**
	 * Loads options from an XML document.
	 * Options are key value pair.<br/>
	 * <option key="a key" value="a value"/>
	 */
	protected void loadOptionsFromXML(Document doc) {
		_options.clear();
		NodeList list = doc.getElementsByTagName("option");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String skey= e.getAttribute("key");
			String svalue = e.getAttribute("value");

			String s = "key: " + skey +
					" value: " + svalue;

			if (skey.isEmpty())
				throw new JtaclConfigurationException("Missing option key: " + s);

			KeyValue k = new KeyValue(skey, svalue);
			_options.put(skey, k);
		}
	}

	/**
	 * Adds a route for each directly connected network on this equipment.<br/>
	 * This method can be called after all interfaces have been added to the
	 * equipment.
	 */
	protected void routeDirectlyConnectedNetworks() {

		/*
		 * each interface
		 */
		IfacesByName ifaces = getIfaces();
		for (Iface iface: ifaces.values()) {
			/*
			 * each links
			 */
			IfaceLinksByIp links = iface.getLinks();
			for (IfaceLink link: links.values()) {
				/*
				 * add a route to directly connected network.
				 */
				IPNet network = link.getNetwork();
				Route<IfaceLink> route = new Route<IfaceLink>(network, network, 0, link);
				Log.debug().info("add route on " + getName() + " " + route.toString());
				_routingEngine.addRoute(route);
			}
		}
	}

	/**
	 * filter out the string in argument according the filters in place.
	 * @param string string to filter out.
	 * @return an empty string if the string matches any filter in place,
	 * returns the string otherwise.
	 */
	protected String filter(String string) {

		for(Pattern pattern: _filters) {
			Matcher m = pattern.matcher(string);
			if (m.matches())
				return "";
		}
		return string;
	}

	/**
	 * Adds the filename in argument to the file alteration monitor of
	 * this equipment.
	 * @param fileName Filename to add
	 */
	protected void famAdd(String fileName) {
		_fam.addFile(fileName);
	}
	
	/**
	 * Create a new {@link GenericEquipment} with this name and this comment.<br/>
	 * @param monitor the {@link Monitor} monitor associated with this equipment.
	 * @param name the name of the equipment.
	 * @param comment a free comment for this equipment.
	 * @param configurationFileName name of the configuration file to use (may be null).
	 */
	public GenericEquipment(Monitor monitor, String name, String comment,
			String configurationFileName) {

		super(monitor, name, comment, configurationFileName);
		_routingEngine = new RoutingEngine();
		_ipServices = IPServices.getInstance();
		_ipProtocols = IPProtocols.getInstance();
		_ipIcmp4Types = IPIcmp4.getInstance();
		_ipIcmp6Types = IPIcmp6.getInstance();
	}

	@Override
	public ShowableRoutes getShowableRoutes() {
		return _routingEngine;
	}

	@Override
	public void runShell(String command, PrintStream output) {

		if (command.equalsIgnoreCase("help")) {
			for (GenericEquipmentShell shell: _shells) {
				shell.shellHelp(output);
			}
			return;
		}

		boolean cmdMatch = false;
		for (GenericEquipmentShell shell: _shells) {
			if (shell.shellCommand(command, output)) {
				cmdMatch = true;
 				break;
			}
		}

		if (!cmdMatch) {
			super.runShell(command, output);
		}
	}

	/**
	 * Registers the specified shell in argument.
	 * @param shell shell to register.
	 */
	public void registerShell(GenericEquipmentShell shell) {
		_shells.add(shell);
	}

	/**
	 * Returns the file alteration monitor of this equipment.
	 * @return the file alteration monitor of this equipment.
	 */
	public FilesMonitor getFam() {
		return _fam;
	}
	
	@Override
	public boolean hasChanged() {
		List<String> files = _fam.checkFiles();
		return !files.isEmpty();
	}
}
