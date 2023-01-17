/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * Address type (PF_ADDR_*).
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public enum PfAddrType {
	PF_ADDR_ANY,	// not in PF, added for convenience
	PF_ADDR_ADDRMASK,
	PF_ADDR_NOROUTE,
	PF_ADDR_DYNIFTL,
	PF_ADDR_TABLE,
	PF_ADDR_RTLABEL,
	PF_ADDR_URPFFAILED,
	PF_ADDR_RANGE,
	PF_ADDR_NONE
}
