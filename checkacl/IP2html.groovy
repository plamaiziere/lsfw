/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * IPs to html
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
import fr.univrennes1.cri.jtacl.lib.ip.*;

def static void main(String input, String output, String pagetitle, String htmlxref, String filelink, boolean debug) {
	/*
	 * read ips from file
	 */
	Xhosts xhosts = new Xhosts()
	xhosts.fromFile(input)

	/*
	 * html export
	 */
	def sw = new StringWriter()
	def html = new groovy.xml.MarkupBuilder(sw)

	html.html{
		head{
			title(pagetitle)
		}
		body{
			h1(pagetitle)
			h2('Generated: ' + new Date())
			a(href:filelink, 'csv source file')
			br(); br(); br()
			table(border:1, width:'100%') {
				tr{
					th('IP')
					th('Host')
					th('Dns')
					th('Ping')
					th('Xref')
				}

				xhosts.each() {
					xhost ->

					String sip = xhost.ip.toString('::i')
					String host = xhost.hostname
					String dns = '--dns--'
					if (xhost.dns)
						dns = '++dns++'
					String ping = '--ping--'
					if (xhost.ping)
						ping = '++ping++'

					String col = Config.ipColor(xhost.dns, xhost.ping)
					tr{
						td(bgcolor:col, width:'25%', sip)
						td(bgcolor:col, width:'40%', host)
						td(bgcolor:col, width:'5%', dns)
						td(bgcolor:col, width:'5%', ping)
						td(bgcolor:col, width:'5%') {
							a(href:htmlxref +'#' + sip, 'xref')
						}
					}
				}
			}
		}
	}

	if (debug)
		print sw.toString()

	def f = new File(output)
	f.write(sw.toString())
}

def String[] argss = lsfw.getArgs()

if (argss.size() < 5) {
    println("Usage IP2html filename-in filename-out xrefhtml filelink pagetitle")
    return
}

def filename = argss[0]
def out = argss[1]
def htmlxref = argss[2]
def filelink = argss[3]
String pagetitle = ''

for (int i= 4; i < argss.size(); i++) {
	pagetitle = pagetitle + ' ' + argss[i]
}

main(filename, out, pagetitle, htmlxref, filelink, true)

