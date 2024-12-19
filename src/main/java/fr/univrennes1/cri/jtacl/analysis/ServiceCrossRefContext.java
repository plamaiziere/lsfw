/*
 * Copyright (c) 2013, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.analysis;

import fr.univrennes1.cri.jtacl.lib.ip.ProtocolsSpec;

/**
 * Context of a service cross reference
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ServiceCrossRefContext {
    protected ProtocolsSpec _protoSpec;
    protected ServiceCrossRefType _type;
    protected String _contextString;
    protected String _contextName;
    protected String _comment;
    protected String _filename;
    protected int _linenumber;

    public ServiceCrossRefContext(ProtocolsSpec protoSpec,
                                  ServiceCrossRefType type,
                                  String contextString,
                                  String contextName,
                                  String comment,
                                  String filename,
                                  int linenumber) {

        _protoSpec = protoSpec;
        _type = type;
        _contextString = contextString;
        _contextName = contextName;
        _comment = comment;
        _filename = filename;
        _linenumber = linenumber;
    }

    public ProtocolsSpec getProtoSpec() {
        return _protoSpec;
    }

    public ServiceCrossRefType getType() {
        return _type;
    }

    public String getComment() {
        return _comment;
    }

    public String getContextName() {
        return _contextName;
    }

    public String getContextString() {
        return _contextString;
    }

    public String getFilename() {
        return _filename;
    }

    public int getLinenumber() {
        return _linenumber;
    }

}
