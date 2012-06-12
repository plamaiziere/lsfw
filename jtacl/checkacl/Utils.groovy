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
 * Tools
 * Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

/*
 * list to file
 */
def static listToFile(String filename, def list) {
	new File(filename).withWriter { out ->
		list.each {
			out.println it
		}
	}
}

/*
 * list from file
 */
def static listFromFile(String filename) {
	return new File(filename).readLines();
}
