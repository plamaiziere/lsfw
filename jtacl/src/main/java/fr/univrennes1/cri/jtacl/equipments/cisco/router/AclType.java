/*
 * Copyright (c) 2010, Université de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

/**
 * The type of an acl (standard, extended, named...) <br/>
 *
 * 1-99            IP standard access list <br/>
 * 100-199         IP extended access list <br/>
 * 1000-1099       IPX SAP access list <br/>
 * 1100-1199       Extended 48-bit MAC address access list <br/>
 * 1200-1299       IPX summary address access list <br/>
 * 1300-1999       IP standard access list (expanded range) <br/>
 * 200-299         Protocol type-code access list <br/>
 * 2000-2699       IP extended access list (expanded range) <br/>
 * 300-399         DECnet access list <br/>
 * 600-699         Appletalk access list <br/>
 * 700-799         48-bit MAC address access list <br/>
 * 800-899         IPX standard access list <br/>
 * 900-999         IPX extended access list <br/>
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr
 */
public enum  AclType {
	/**
	 * IP standard access list.
	 */
	IPSTD,
	/**
	 * IP extended access list.
	 */
	IPEXT,
	/**
	 * IPX SAP access list.
	 */
	IPXSAP,
	/**
	 * Extended 48-bit MAC address access list.
	 */
	MACEXT,
	/**
	 * IPX summary address access list.
	 */
	IPXSUM,
	/**
	 * Protocol type-code access list.
	 */
	PROTO,
	/**
	 * DECnet access list.
	 */
	DECNET,
	/**
	 * Appletalk access list.
	 */
	APPLETALK,
	/**
	 * 48-bit MAC address access list.
	 */
	MACSTD,
	/**
	 * IPX standard access list.
	 */
	IPXSTD,
	/**
	 * IPX extended access list.
	 */
	IPXEXT,
	/**
	 * Named access list.
	 */
	NAMED,
	/**
	 * Unknown type.
	 */
	UNKNOWN;

	/**
	 * Converts the number in argument to the corresponding access-list type
	 * @param n number to get the access-list type.
	 * @return the {@link AclType} type corresponding.
	 */
	public static AclType getType(int n) {
		if ((n >= 1 && n <= 99) || (n >= 1300 && n <= 1999))
			return IPSTD;
		if ((n >= 100 && n <= 199) || (n >= 2000 && n <= 2699))
			return IPEXT;
		if (n >= 1000 && n <= 1099)
			return IPXSAP;
		if (n >= 1100 && n <= 1199)
			return MACEXT;
		if (n >= 1200 && n <= 1299)
			return IPXSUM;
		if (n >= 1300 && n <= 1399)
			return IPXSUM;
		if (n >= 200 && n <= 299)
			return PROTO;
		if (n >= 300 && n <= 399)
			return DECNET;
		if (n >= 600 && n <= 699)
			return APPLETALK;
		if (n >= 700 && n <= 799)
			return MACSTD;
		if (n >= 800 && n <= 899)
			return IPXSTD;
		if (n >= 900 && n <= 999)
			return IPXEXT;

		return UNKNOWN;
	}
}
