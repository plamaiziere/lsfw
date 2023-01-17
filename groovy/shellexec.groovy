/*
 * Copyright (c) 2012, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

/*
 * Execute the command in argument via a shell
 * (NB: stdinput is not forwarded to the process)
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

if (lsfw.getArgs().isEmpty()) {
	println("Usage: shellexec command")
	return
}

def procb = new ProcessBuilder("/bin/sh", "-c" , lsfw.getcArgs())
def proc = procb.start()
proc.waitFor()
print proc.text
