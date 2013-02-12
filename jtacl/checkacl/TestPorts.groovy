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
 * List of tested ports
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

import fr.univrennes1.cri.jtacl.lib.ip.*;

class TestPorts extends ArrayList<TestPort> {

	void fromList(def list) {
		clear()
		list.each() {
			TestPort tp = new TestPort()
			if (!it.isEmpty()) {
				tp.fromString(it)
				add(tp)
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
				port ->
				out.println port.toString()
			}
		}
	}

	Map<IPNet, List<TestPort>> asMapByIp() {
		Map<IPNet, List<TestPort>> map = new HashMap<IPNet, List<TestPort>>()
		this.each() {
			List<TestPort> list = map.get(it.ip)
			if (list == null) {
				list = new ArrayList<TestPort>()
				map.put(it.ip, list)
			}
			list.add(it)
		}
		return map
	}

	TestPorts filterIp(List<IPNet> filterNetworks) {

		TestPorts output = new TestPorts()

		this.each() {
			port ->

			boolean itest = false || filterNetworks.isEmpty()
			for (IPNet network: filterNetworks) {
				if (network.contains(port.ip)) {
					itest = true
					break
				}
			}

			if (!itest)
				return
			output.add(port)
		}
		return output
	}
}

