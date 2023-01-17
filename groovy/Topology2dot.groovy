/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * This script output the topology in dot format that could be drawn by graphviz
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.shell.*
import fr.univrennes1.cri.jtacl.core.monitor.*
import fr.univrennes1.cri.jtacl.core.topology.*
import fr.univrennes1.cri.jtacl.core.network.*


class SimpleLink {
	List<NetworkLink> links
	String eq1
	String eq2

	SimpleLink(String eq1, String eq2) {
		links = new ArrayList<NetworkLink>()
		this.eq1 = eq1
		this.eq2 = eq2
	}

	void addNetworkLink(NetworkLink link) {
		links.add(link)
	}

	String id() {
		if (eq1 <= eq2)
			return eq1 + ' -- ' + eq2
		else
			return eq2 + ' -- ' + eq1
	}
}

class Topo2dot {

	static List<SimpleLink> nlinksToSlinks(NetworkLink nlink) {

		def listeq = []
		NetworkEquipmentsByName equipments = nlink.getEquipments()
		equipments.each() {
			equipment ->
				name = equipment.getKey()
				listeq.add(name)
		}
		List<SimpleLink> slinks = new ArrayList<SimpleLink>()

		for (int i = 0; i < listeq.size() - 1; i++) {
			String eq1 = listeq[i]
			for (int j = i + 1; j < listeq.size; j++) {
				SimpleLink slink = new SimpleLink(eq1, listeq[j])
				slink.addNetworkLink(nlink)
				slinks.add(slink)
			}
		}
		return slinks
	}

	static void toDot(Topology topology, String output) {

		Map<String, SimpleLink> mlinks = new HashMap<String, SimpleLink>()

		NetworkLinks nlinks = topology.getNetworkLinks()
		nlinks.each() {
			nlink ->
				List<SimpleLink> slinks = Topo2dot.nlinksToSlinks(nlink)

				slinks.each() {
					link ->
					SimpleLink slink = mlinks.get(link.id())
					if (slink == null) {
						mlinks.put(link.id(), link)
					} else {
						slink.addNetworkLink(nlink)
					}
				}
		}

		List<String> listout = new ArrayList<String>()
		listout.add('graph topology {')

		mlinks.each() {
			item ->
			SimpleLink link = item.getValue()
			String label = ""
			link.links.each() {
				nlink ->
				label += nlink.getNetwork().toString('i::') + '\\n'
			}
			String s = '"' + link.eq1 + '"' + " -- " + '"' + link.eq2 + '"' + ' [label=' + '"' + label + '"' + '];'
			listout.add(s)
		}

		listout.add('}')

		new File(output).withWriter { o ->
			listout.each {
				o.println it
			}
		}
	}

	static void usage() {
		println('Usage: topology2dot file');
		println();
		println('This script output the topology in dot format that could be drawn by graphviz.');
		println();
		println('Example:');
		println('   topology2dot topology.dot');
	}
}

def args = lsfw.getArgs()
if (args.size() == 1 && args[0] == '-help') {
	Topo2dot.usage()
	return
}

if (args.size() != 1) {
	Topo2dot.usage()
	return
}

String output = args[0]

Topology topology = lsfw.getMonitor().getTopology()

Topo2dot.toDot(topology, output)

