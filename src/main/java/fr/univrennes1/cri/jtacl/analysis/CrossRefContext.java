/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.analysis;

/**
 * Context of a cross reference
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class CrossRefContext {
    protected String _contextString;
    protected String _contextName;
    protected String _comment;
    protected String _filename;
    protected int _linenumber;

    public CrossRefContext(String contextString,
                           String contextName,
                           String comment,
                           String filename,
                           int linenumber) {
        _contextString = contextString;
        _contextName = contextName;
        _comment = comment;
        _filename = filename;
        _linenumber = linenumber;
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
