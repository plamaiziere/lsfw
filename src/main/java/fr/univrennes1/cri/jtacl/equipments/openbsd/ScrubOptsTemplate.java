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
 * Template to build scrub options. This class is used at parsing time
 * as an intermediate storage.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ScrubOptsTemplate {

    private boolean _nodf;
    private String _minttl;
    private String _maxmss;
    private String _settos;
    private boolean _randomid;
    private boolean _reassemble_tcp;

    public String getMaxmss() {
        return _maxmss;
    }

    public boolean setMaxmss(String maxmss) {
        _maxmss = maxmss;
        return true;
    }

    public String getMinttl() {
        return _minttl;
    }

    public boolean setMinttl(String minttl) {
        _minttl = minttl;
        return true;
    }

    public boolean isNodf() {
        return _nodf;
    }

    public boolean setNodf(boolean nodf) {
        _nodf = nodf;
        return true;
    }

    public boolean isRandomid() {
        return _randomid;
    }

    public boolean setRandomid(boolean randomid) {
        _randomid = randomid;
        return true;
    }

    public boolean isReassemble_tcp() {
        return _reassemble_tcp;
    }

    public boolean setReassemble_tcp(boolean reassemble_tcp) {
        _reassemble_tcp = reassemble_tcp;
        return true;
    }

    public String getSettos() {
        return _settos;
    }

    public boolean setSettos(String settos) {
        _settos = settos;
        return true;
    }
}
