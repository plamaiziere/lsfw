/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.pix;

/**
 * Interface to be abble to search the type of an object group.<br/>
 * This interface is used by the pix parser to search the type of an
 * object-group while parsing because the syntax is ambigous without the type.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public interface GroupTypeSearchable {

	/**
	 * Retrieves the type of the groupId in argument.
	 * @param groupId the groupId of the group.
	 * @return the {@link ObjectGroupType} type of the group. Null if the group
	 * is unknown.
	 */
	ObjectGroupType getGroupType(String groupId);

}
