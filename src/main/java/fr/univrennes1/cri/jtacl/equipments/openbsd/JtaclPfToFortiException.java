/*
 * Copyright (c) 2023, Universite de Rennes
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclRuntimeException;

/**
 * Exception related to the PF to Fortigate convertor
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class JtaclPfToFortiException extends JtaclRuntimeException {

    public JtaclPfToFortiException(String message) {
        super(message);
    }
}
