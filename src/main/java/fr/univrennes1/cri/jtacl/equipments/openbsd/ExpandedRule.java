/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.openbsd;

import java.util.ArrayList;

/**
 * Describes a rule and its expanded form.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ExpandedRule extends ArrayList<ExpandedRuleLine> {

    public String expandedToString() {
        StringBuilder resSb = new StringBuilder();
        for (ExpandedRuleLine exLine : this) {
            for (StringBuilder sb : exLine.getExpanded()) {
                resSb.append(sb);
            }
        }
        return resSb.toString();
    }

    public String lineToString() {
        StringBuilder resSb = new StringBuilder();
        for (ExpandedRuleLine exLine : this) {
            resSb.append(exLine.getLine());
        }
        return resSb.toString();
    }

    public int expandedCountToLineCount(int lineCount) {
        int lc = 1;

        for (ExpandedRuleLine exLine : this) {
            int exLc = exLine.getExpanded().size();
            if ((lc + exLc) > lineCount)
                return lc;
            else
                lc++;
        }
        return -1;
    }
}
