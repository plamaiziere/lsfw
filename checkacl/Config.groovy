/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * Configuration class for CheckAcl
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

class Config {

	/*
	 * networks to check
	 */
	def static networksToCheck = [
		'10.0.1.0/24',
		'10.0.2.0/24',
		'10.0.3.0/24',
		'192.168.0.0/24',
		'192.168.1.0/24',
		'192.168.10.0/24',
		'192.168.11.0/24',
		'192.168.12.0/24'
	];

	/*
	 * networks to exclude for port check
	 */
	def static networksToExclude = [
		'127.0.0.1'
	]

	/*
	 * reports to produce
	 */
	def static reportsNetworks = [
		'10.0.1.0/24',
		'10.0.2.0/24',
		'10.0.3.0/24',
		'192.168.0.0/24',
		'192.168.1.0/24',
		'192.168.10.0/24',
		'192.168.11.0/24',
		'192.168.12.0/24'
	];

	/*
	 * Link to documentation
	 */
	static String documentationlink = 'https://your_doc_here.org'

	/*
	 * path to java program
	 */
//	static String JAVA = '/usr/bin/java'
	static String JAVA = '/usr/local/bin/java'

	/*
	 * path to lsfw's jar file
	 */
//	static String LSFWJAR = '/usr/local/lsfw/lsfw-1.0-SNAPSHOT-jar-with-dependencies.jar'
	static String LSFWJAR = '/home/patrick/devel/jtacl/target/lsfw-1.0-SNAPSHOT-jar-with-dependencies.jar'

	/*
	 * path to lsfw's config file
	 */
	static String LSFWCFG = 'lsfw.xml'

	/*
	 * temp dir : DO NOT use /tmp as temp files used are predictable and some of them are executed by lsfw.
	 */
	static String TMPDIR = './tmp'

	/*
	 * tests dir
     */
	static TESTDIR = './checks'

	/*
	 * html dir : output in html
	 */
	static String HTMLDIR = TESTDIR + '/html'

	/*
	 * html xref dir
	 */
	static String XREFDIR = HTMLDIR + '/infra'

	/*
	 * xref link relative to HTMLDIR
	 */
	static String XREFLINK = 'infra/xrefs.html'

	/*
	 * number of processes to launch to test open ports
	 */
	static int CHECKPROC = 4

	/*
     * ping command to use
     */
	static boolean ping(String host, String timeout) {

		// Linux ping
//		def procb = new ProcessBuilder("/bin/ping", "-c 1" , "-q", "-w $timeout", host);

		// FreeBSD ping
		def procb = new ProcessBuilder("/sbin/ping", "-c 1" , "-q", "-t $timeout", host)
		def proc = procb.start()
		proc.waitFor()
		return proc.exitValue() == 0
	}

	/*
	 * Color according dns and ping flag
	 */
	static String ipColor(boolean dns, boolean ping) {
		String col = 'limegreen'
		if (!dns) {
			col = 'orangered'
		} else {
			if (!ping)
				col = 'orange'
		}
		return col
	}
}

