/*
 * Copyright (c) 2013, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.checkpoint;

import fr.univrennes1.cri.jtacl.core.probing.MatchResult;

/**
 * Match results for Checkpoint service
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */

public class CpServiceMatch {
    protected MatchResult _matchResult;
    protected CpService _service;

    public CpServiceMatch(CpService service, MatchResult result) {
        _service = service;
        _matchResult = result;
    }

    public MatchResult getMatchResult() {
        return _matchResult;
    }

    public CpService getService() {
        return _service;
    }

    public boolean isInspected() {
        return _service.hasProtocolType();
    }

}
