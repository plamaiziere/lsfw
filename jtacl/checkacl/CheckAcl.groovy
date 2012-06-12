/*
 * Copyright (c) 2012, Universite de Rennes 1
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

/*
 * Script to checks access-lists.
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.shell.*
import fr.univrennes1.cri.jtacl.App
import fr.univrennes1.cri.jtacl.lib.ip.*
import fr.univrennes1.cri.jtacl.core.monitor.*


def static int execute(String cmd) {
	def proc = cmd.execute()
	proc.waitFor()
	return proc.exitValue()
}

def static List<String> getEquipments() {
	List<String> list = new ArrayList<String>()

	def equipments = Monitor.getInstance().getEquipments()
	equipments.each() {
		entry ->
			String name = entry.getKey()
			String className = entry.getValue().getClass().getName();
			if (className != 'fr.univrennes1.cri.jtacl.equipments.SimpleRouter')
				list.add(name)
	}
	list.sort()
	return list
}

def void main(boolean test) {
	boolean debug = false

	String flistip = 'ip'
	/*
	 * path
	 */
	String fxrefs = Config.TESTDIR + '/xrefs.txt'
	String fports = Config.TESTDIR + '/testport.txt'
	String fxrefsckd = Config.TESTDIR + '/xrefsckd.txt'
	String linkxrefs = Config.XREFLINK
	String fxrefshtml = Config.HTMLDIR + '/' + linkxrefs
	String findex = Config.HTMLDIR + '/index.html'

	/*
	 * networks to check
	 */
	def networksToCheck = []
	Config.networksToCheck.each() {
		network ->
		IPNet net = new IPNet(network)
		networksToCheck.add(net)
	}

	/*
	 * reports to produce
	 */
	def reportsNetworks = []
	Config.reportsNetworks.each() {
		network ->
		IPNet net = new IPNet(network)
		reportsNetworks.add(net)
	}

	def index = []
	def startdate = new Date()

	if (test) {
		println('Generating xrefs')
		Getxrefs.main(Monitor.getInstance(), fxrefs, debug)

		println('Checking xrefs')
		Checkxrefs.main(fxrefs, fxrefsckd, debug)

		println('Checking open ports')
		Checkports.main(fxrefsckd, fports, networksToCheck, Config.CHECKPROC, debug)
	}

	index.add('Topology; topology.png')

	// xrefs in html
	println('xrefs in html')
	Xrefs2html.main(fxrefsckd, fxrefshtml, 'Reference of IP addresses', 'xrefs.txt', debug)
	index.add('Reference of IP addresses; ' + linkxrefs)

	// copy xrefs source file
	XrefsIP xrefsckd = new XrefsIP()
	xrefsckd.fromFile(fxrefsckd)
	xrefsckd.toFile(Config.XREFDIR + '/xrefs.txt')

	// ips from xrefs
	Xhosts xhosts = xrefsckd.toXhosts()

	xrefsckd = null

	List<String> equipments = getEquipments()
	equipments.add(0, "")

	// ip by equipment
	equipments.each() {
		equipment ->

		index.add('');
		println("IP addresses on $equipment")
		String txtlink = 'ip' + equipment + '.txt'
		String txtfile = Config.HTMLDIR + '/ip' + equipment + '.txt'
		String htmlfile = Config.HTMLDIR + '/ip' + equipment + '.html'
		String htmllink = 'ip' + equipment + '.html'
		Xhosts ips = xhosts.filterEq(equipment)
		ips.toFile(txtfile)
		if (!ips.isEmpty()) {
			IP2html.main(txtfile, htmlfile, 'IP addresses ' + equipment, linkxrefs, txtlink, debug)
			index.add('IP addresses ' + equipment + '; ' + htmllink)
		}

		txtlink = 'ipdns' + equipment + '.txt'
		txtfile = Config.HTMLDIR + '/ipdns' + equipment + '.txt'
		htmlfile = Config.HTMLDIR + '/ipdns' + equipment + '.html'
		htmllink = 'ipdns' + equipment + '.html'
		Xhosts ipsw = ips.filterIpDnsPing([], true, false)
		ipsw.toFile(txtfile)
		if (!ipsw.isEmpty()) {
			IP2html.main(txtfile, htmlfile, 'IP addresses without DNS ' + equipment, linkxrefs, txtlink, debug)
			index.add('IP addresses without DNS ' + equipment + '; ' + htmllink)
		}

		txtlink = 'ipping' + equipment + '.txt'
		txtfile = Config.HTMLDIR + '/ipping' + equipment + '.txt'
		htmlfile = Config.HTMLDIR + '/ipping' + equipment + '.html'
		htmllink = 'ipping' + equipment + '.html'
		ipsw = ips.filterIpDnsPing([], false, true)
		ipsw.toFile(txtfile)
		if (!ipsw.isEmpty()) {
			IP2html.main(txtfile, htmlfile, 'IP addresses without ping ' + equipment, linkxrefs, txtlink, debug)
			index.add('IP addresses without ping ' + equipment + '; ' + htmllink)
		}
	}

	/*
   	 * reports
   	 */
	TestPorts ports = new TestPorts()
	ports.fromFile(fports)

	reportsNetworks.each() {

		ip ->
		String sip = ip.toString('s::')
		String iptos = ip.toString('i::')
		println("IP addresses on $iptos")
		String txtlink = 'ip' + sip + '.txt'
		String txtfile = Config.HTMLDIR + '/ip' + sip + '.txt'
		String htmlfile = Config.HTMLDIR + '/ip' + sip + '.html'
		String htmllink = 'ip' + sip + '.html'

		index.add('');
		/*
		 * IP addresses
		 */
		Xhosts ips = xhosts.filterIpDnsPing([ip], false, false)
		ips.toFile(txtfile)
		IP2html.main(txtfile, htmlfile, 'IP addresses ' + iptos, linkxrefs, txtlink, debug)
		index.add('IP addresses ' + iptos + '; ' + htmllink)

		/*
		 * IP addresses without DNS
		 */
		txtlink = 'ipdns' + sip + '.txt'
		txtfile = Config.HTMLDIR + '/ipdns' + sip + '.txt'
		htmlfile = Config.HTMLDIR + '/ipdns' + sip + '.html'
		htmllink = 'ipdns' + sip + '.html'
		ips = xhosts.filterIpDnsPing([ip], true, false)
		ips.toFile(txtfile)
		IP2html.main(txtfile, htmlfile, 'IP addresses without DNS ' + iptos, linkxrefs, txtlink, debug)
		index.add('IP addresses without DNS ' + iptos + '; ' + htmllink)

		/*
		 * IP addresses without ping
		 */
		txtlink = 'ipping' + sip + '.txt'
		txtfile = Config.HTMLDIR + '/ipping' + sip + '.txt'
		htmlfile = Config.HTMLDIR + '/ipping' + sip + '.html'
		htmllink = 'ipping' + sip + '.html'
		ips = xhosts.filterIpDnsPing([ip], false, true)
		ips.toFile(txtfile)
		IP2html.main(txtfile, htmlfile, 'IP addresses without ping ' + iptos, linkxrefs, txtlink, debug)
		index.add('IP addresses without ping ' + iptos + '; ' + htmllink)

		/*
		 * open ports
		 */
		txtlink = 'ports' + sip + '.txt'
		txtfile = Config.HTMLDIR + '/ports' + sip + '.txt'
		htmlfile = Config.HTMLDIR + '/ports' + sip + '.html'
		htmllink = 'ports' + sip + '.html'
		println("Open ports on $iptos")
		TestPorts rports = ports.filterIp([ip])
		rports.toFile(txtfile)
		Ports2html.main(txtfile, htmlfile, 'Open ports on ' + iptos, linkxrefs, txtlink, debug)
		index.add('Open ports on ' + iptos + '; ' + htmllink)
	}


	/*
	 * index page
  	 */
	println('index page');
	def sw = new StringWriter();
	def html = new groovy.xml.MarkupBuilder(sw)
	html.html{
		head{
			title('Acl check')
		}
		body{
			h1('Acl check ' + startdate + ' - ' + new Date())
			a(href:Config.documentationlink, 'documentation')
			h2('index')
			table(border:1) {
				tr{
					th('titre')
					th('url')
				}
				index.each() {
					line ->
					def split
					if (line.isEmpty()) {
						split = ['-', '-'];
					} else
						split = line.split(';')
					tr{
						td(split[0].trim());
						if (split[1] != '-')
							td{
								a(href:split[1].trim(), split[1].trim())
							}
						else
							td('-')
					}
				}
			}
			hr();
			p(style:'font-style:italic;',align:'right', 'Generated by lsfw(R)')
		}
	}
	def f = new File(findex)
	f.write(sw.toString())
}

def String[] argss = lsfw.getArgs()

if (argss.size() != 1 || (argss[0] != 'check' && argss[0] != 'report')) {
    println("Usage CheckAcl check|report")
    return
}

main(argss[0] == 'check')
