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
 * Tested ports to html
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
import fr.univrennes1.cri.jtacl.lib.ip.*

def static void main(String input, String output, String pagetitle, String htmlxref, String filelink, boolean debug) {
	/*
	 * read open ports from file
	 */
	TestPorts ports = new TestPorts()
	ports.fromFile(input)

	Map<IPNet, List<TestPort>> portsbyip = ports.asMapByIp()
	List<IPNet> listips = new ArrayList<IPNet>()
	listips.addAll(portsbyip.keySet())
	listips.sort()

	/*
	 * export html
	 */
	def pw = new PrintWriter(new FileOutputStream(output));
	def html = new groovy.xml.MarkupBuilder(pw);

	html.html{
		head{
			title(pagetitle);
		}
		body{
			h1(pagetitle);
			h2("Generated: " + new Date());
			a(href:filelink, 'csv source file');
			br(); br(); br();

			listips.each() {
				ip ->
					List<TestPort> portsip = portsbyip.get(ip)
					TestPort port = portsip.get(0)
					String sip = port.ip.toString('::i')
					String hostname = port.hostname
					boolean fdns = port.dns
					String sdns = "--dns--"
					if (fdns)
						sdns = "++dns++"
					boolean fping = port.ping
					String sping = "--ping--"
					if (fping)
						sping = "++ping++"
					String col = Config.ipColor(fdns, fping)
				a(name:sip)
				font(color:col) {
					b(sip + '; ' + hostname + ' ; ' + sdns + '; ' + sping)
				}
				br()
				a(href:htmlxref +'#' + sip, 'xref')
				br()
				table(bgcolor:col, border:1, width:'100%') {
					tr{
						th('IP')
						th('Hostname')
						th('Protocol')
						th('Port')
					}
					portsip.each() {
						sports ->
						tr{
							td(width:'40%', sip)
							td(width:'40%', hostname)
							td(width:'10%', sports.protocol)
							td(width:'10%', sports.destPort)
						}
					}
				}
				br()
			}
		} // html
	}
}

def String[] argss = lsfw.getArgs()

if (argss.size() < 5) {
    println("Usage ports2html filename-in filename-out xrefhtml filelink pagetitle")
    return
}

def filename = argss[0]
def out = argss[1]
def htmlxref = argss[2]
def filelink = arrgs[3]
String pagetitle = ''

for (int i= 4; i < argss.size(); i++) {
	pagetitle = pagetitle + ' ' + argss[i]
}

main(filename, out, pagetitle, htmlxref, filelink, true)

