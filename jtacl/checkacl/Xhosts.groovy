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
 * List of Xosts (host description)
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.lib.ip.*;

class Xhosts extends ArrayList<Xhost> {

	void fromList(def list) {
		clear()
		list.each() {
			Xhost xh = new Xhost()
			if (!it.isEmpty()) {
				xh.fromString(it)
				add(xh)
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
				xhost ->
				out.println xhost.toString()
			}
		}
	}

	Xhosts filterIpDnsPing(List<IPNet> filterNetworks, boolean withoutdns, boolean withoutping) {

		Xhosts output = new Xhosts()

		this.each() {
			xhost ->

			boolean itest = false || filterNetworks.isEmpty()
			for (IPNet network: filterNetworks) {
				if (network.networkContains(xhost.ip)) {
					itest = true
					break
				}
			}

			if (!itest)
				return
			if (withoutdns && xhost.dns)
				return
			if (withoutping && xhost.ping)
				return

			output.add(xhost)
		}
		return output
	}

	Xhosts filterEq(String equipment) {

		Xhosts output = new Xhosts()

		this.each() {
			xhost ->

			if (equipment.isEmpty() || equipment.equals(xhost.equipment))
				output.add(xhost)
		}
		return output
	}


}

