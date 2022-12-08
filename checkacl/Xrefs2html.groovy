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
 * xrefs to html
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
import fr.univrennes1.cri.jtacl.lib.ip.*

def static void main(String input, String output, String pagetitle, String filelink, boolean debug) {
	/*
	 * read xrefs
	 */
	XrefsIP xrefs = new XrefsIP()
	xrefs.fromFile(input)
	Map<IPNet, List<XrefIP>> xrefsbyip = xrefs.asMapByIp()
	List<IPNet> listips = new ArrayList<IPNet>()
	listips.addAll(xrefsbyip.keySet())
	listips.sort()

	/*
	 * export html
	 */
	def sw = new StringWriter()
	def html = new groovy.xml.MarkupBuilder(sw)
	html.html{
		head{
			title(pagetitle)
		}
		body{
			h1(pagetitle);
			h2("Generated: " + new Date())
			a(href:filelink, 'csv source file')
			br(); br(); br();
			listips.each() {
				ip ->
					List<XrefIP> xrefip = xrefsbyip.get(ip)
					XrefIP xref = xrefip.get(0)
					String sip = xref.ip.toString('::i')
					String hostname = xref.hostname
					boolean fdns = xref.dns
					String sdns = "--dns--"
					if (fdns)
						sdns = "++dns++"
					boolean fping = xref.ping
					String sping = "--ping--"
					if (fping)
						sping = "++ping++"
					String col = Config.ipColor(fdns, fping)
				a(name:sip)
				font(color:col) {
					b(sip + '; ' + hostname + ' ; ' + sdns + '; ' + sping)
				}
				table(bgcolor:col, border:1, width:'100%') {
					tr{
						th('Equipment')
						th('File')
						th('Type')
						th('Context')
						th('Rules')
					}

					xrefip.each() {
						sxref ->
						tr{
							td(width:'5%', sxref.equipment)
							td(width:'15%', sxref.fileandline)
							td(width:'10%', sxref.ctxtype)
							td(width:'10%', sxref.ctx)
							td(width:'60%', sxref.rule)
						}
					}
				}
				br()
			}
		}
	} // html

	if (debug)
		print sw.toString()

	def f = new File(output)
	f.write(sw.toString())
}

def String[] argss = lsfw.getArgs()

if (argss.size() != 3) {
    println("Usage xrefs2html filename-in filename-out filelink")
    return;
}

def filename = argss[0]
def out = argss[1]
def filelink = argss[2]

main(filename, out, '', filelink, true)


