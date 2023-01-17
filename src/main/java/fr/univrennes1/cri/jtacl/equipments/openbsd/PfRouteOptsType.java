/*
 * Copyright (c) 2011, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */
package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * Route option type
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public enum PfRouteOptsType {
	NONE,
	PF_ROUTETO,
	PF_DUPTO,
	PF_REPLYTO,
	PF_FASTROUTE
}
