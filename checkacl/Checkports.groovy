/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * Check of open ports
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
import fr.univrennes1.cri.jtacl.lib.ip.*;

def static void main(String input, String output, List<IPNet> networks, List<IPNet> excludeNetworks, int ncpu, boolean debug) {

	 // crossref from file
	XrefsIP xrefs = new XrefsIP()
	xrefs.fromFile(input)

	// filter by ip
	Xhosts xhosts = xrefs.toXhosts()
	xhosts = xhosts.filterIpDnsPing(networks, false, false)
	xhosts = xhosts.filterExcludeIp(excludeNetworks)


	// one test file per ncpu
	println("Generating tests: ip= " + xhosts.size())

	def nbpercpu = xhosts.size() / ncpu
	def i = 0
	def proccount = 0
	def probes = []
	xhosts.each() {
		xhost ->

		String ip = xhost.ip.toString('::i')
		String hostname = xhost.hostname
		String dns = '--dns--'
		if (xhost.dns)
			dns = '++dns++'
		String ping = '--ping--'
		if (xhost.ping)
			ping = '++ping++'
		probes.add('$checkipports udp/tcp 1.2.3.4 dyn ' + "$ip 1 65535 $hostname $dns $ping" )
		i++;
		if (i >= nbpercpu) {
			++proccount
			String file = Config.TMPDIR + "/$output" + proccount
			println (file)
			Utils.listToFile(Config.TMPDIR + "/$output" + proccount, probes)
			probes = []
			i = 0
		}
	}

	if (! probes.isEmpty()) {
		++proccount
		Utils.listToFile(Config.TMPDIR + "/$output" + proccount, probes)
	}

	println("Date " + new Date() + " : start tests on " + proccount + " processes. Please wait...")

	def processus = []
	def souts = []
	for (i = 1; i <= proccount; i++) {
		// start lsfw to probe
		def procb = new ProcessBuilder(Config.JAVA, "-jar", Config.LSFWJAR, "-f", Config.LSFWCFG ,"-i", Config.TMPDIR + "/$output" + i, "-no-interactive" )
		procb.redirectErrorStream(true)
		def proc = procb.start()
		processus.add(proc)
		def sout = new FileOutputStream(Config.TMPDIR + "/$output" + "res" + i)
		def serr = new StringBuffer()
		souts.add(sout)
		proc.consumeProcessOutputStream(sout)
	}

	// wait the end of all processes
	for (i = 0; i < proccount; i++) {
		def proc = processus[i]
		proc.waitFor()
	}

	// merge the results into one file
	def result = [];
	for (i = 1; i <= proccount; i++) {
		def rf = Utils.listFromFile(Config.TMPDIR + "/$output" + "res" + i)
		result.addAll(rf)
	}

	Utils.listToFile(output, result)

	println("Date " + new Date() + " : end of tests.")
}

//------

def String[] argss = lsfw.getArgs()

if (argss.size() < 3) {
	println("Usage checkports filename-in filename-out nbprocessus [network]")
	return
}

def filename = argss[0]
def out = argss[1]
def ncpu = Integer.valueOf(argss[2])
def testNetworks = []
def excludeNetworks = []

// networks to test
for (i = 3; i < argss.size(); i ++) {
	IPNet ip = new IPNet(argss[i])
	testNetworks.add(ip)
}

main(filename, out, testNetworks, excludeNetworks, ncpu, true)

