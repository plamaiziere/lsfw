/*
 * Copyright (c) 2010, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.equipments.openbsd;

/**
 * Packer Filter Const
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PfConst {

	/*
	 * pool options
	 */
	public static final int PF_POOL_IDMASK		= 0x0f;
	public static final int PF_POOL_NONE		= 0x00;
	public static final int PF_POOL_BITMASK		= 0x01;
	public static final int PF_POOL_RANDOM		= 0x02;
	public static final int PF_POOL_SRCHASH		= 0x03;
	public static final int PF_POOL_ROUNDROBIN	= 0x04;
	public static final int PF_POOL_TYPEMASK	= 0x0f;
	public static final int PF_POOL_STICKYADDR	= 0x20;

	/*
	 * route type
	 */
	public static final int PF_FASTROUTE	= 0x01;
	public static final int PF_ROUTETO		= 0x02;
	public static final int PF_REPLYTO		= 0x03;
	public static final int PF_DUPTO		= 0x04;

	/*
	 * interface flags lookup
	 */
	public final static int PFI_AFLAG_NETWORK	= 0x01;
	public final static int PFI_AFLAG_BROADCAST	= 0x02;
	public final static int PFI_AFLAG_PEER		= 0x04;
	public final static int PFI_AFLAG_MODEMASK	= 0x07;
	public final static int PFI_AFLAG_NOALIAS	= 0x08;

}
