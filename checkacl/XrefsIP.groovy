/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * List of ip references
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.lib.ip.*;

class XrefsIP extends ArrayList<XrefIP> {

	void fromList(def list) {
		clear()
		list.each() {
			XrefIP xr = new XrefIP()
			if (!it.isEmpty()) {
				xr.fromString(it)
				add(xr)
			}
		}
	}

	void fromFile(String filename) {
		clear()
		fromList(new File(filename).readLines())
	}

	void toFile(String filename) {
		new File(filename).withWriter {
			out ->

			this.each() {
				xref ->
				out.println xref.toString()
			}
		}
	}

	Map<IPNet, List<XrefIP>> asMapByIp() {
		Map<IPNet, List<XrefIP>> map = new HashMap<IPNet, List<XrefIP>>()
		this.each() {
			List<XrefIP> list = map.get(it.ip)
			if (list == null) {
				list = new ArrayList<XrefIP>()
				map.put(it.ip, list)
			}
			list.add(it)
		}
		return map
	}

	Xhosts toXhosts() {
		Map<IPNet, List<XrefIP>> xrefsbyip = asMapByIp()
		List<IPNet> listips = new ArrayList<IPNet>()
		listips.addAll(xrefsbyip.keySet())
		listips.sort()

		Xhosts xhosts = new Xhosts()
		listips.each() {
			ip ->
			List<XrefIP> xrefs = xrefsbyip.get(ip)
			XrefIP xref = xrefs.get(0)
			Xhost xhost = new Xhost(xref.ip, xref.hostname, xref.equipment, xref.dns, xref.ping)
			xhosts.add(xhost)
		}
		return xhosts
	}

}

