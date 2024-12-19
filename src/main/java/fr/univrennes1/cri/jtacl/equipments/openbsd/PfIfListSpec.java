/*
 * Copyright (c) 2011, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import java.util.ArrayList;

/**
 * Interface List specification in rule
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class PfIfListSpec extends ArrayList<PfIfSpec> {

    /**
     * Checks that this Interface List Specification matches at least one time
     * the interface name in parameter
     *
     * @param ifName Interface name to check.
     * @return true if this Interface List Specification matches at least one time
     * the interface name
     */
    public boolean matches(String ifName) {

        for (PfIfSpec ifspec : this) {
            if (!ifspec.isIfNot() && ifspec.getIfName().equals(ifName))
                return true;
            if (ifspec.isIfNot() && !ifspec.getIfName().equals(ifName))
                return true;
        }
        return false;
    }

}
