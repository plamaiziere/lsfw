/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.equipments.cisco.router;

import fr.univrennes1.cri.jtacl.core.exceptions.JtaclInternalException;
import fr.univrennes1.cri.jtacl.core.probing.MatchResult;
import fr.univrennes1.cri.jtacl.lib.ip.PortOperator;
import fr.univrennes1.cri.jtacl.lib.ip.PortSpec;

/**
 * Describes a port object.
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class PortObject {

    /**
     * Operator equal
     */
    public final String EQ = "eq";

    /**
     * Operator less than
     */
    public final String LT = "lt";

    /**
     * Operator greater than
     */
    public final String GT = "gt";

    /**
     * Operator not equal
     */
    public final String NEQ = "neq";

    /**
     * Operator range
     */
    public final String RANGE = "range";

    /**
     * Current operator
     */
    protected String _operator;

    /**
     * port spec corresponding to this Port Object
     */
    protected PortSpec _portSpec;

    /**
     * Constructs a new {@link PortObject} instance using an operator
     * with one operand.
     *
     * @param operator operator to apply.
     * @param port     port operand.
     */
    public PortObject(String operator, int port) {
        _operator = operator;

        if (_operator.equals(EQ)) {
            _portSpec = new PortSpec(PortOperator.EQ, port);
        }
        if (_operator.equals(GT)) {
            _portSpec = new PortSpec(PortOperator.GT, port);
        }
        if (_operator.equals(LT)) {
            _portSpec = new PortSpec(PortOperator.LT, port);
        }
        if (_operator.equals(NEQ)) {
            _portSpec = new PortSpec(PortOperator.NEQ, port);
        }

        if (_portSpec == null)
            throw new JtaclInternalException("Invalid port operator" +
                    _operator);
    }

    /**
     * Constructs a new {@link PortObject} instance using an operator
     * with two operands (operator range).
     *
     * @param operator  operator to apply.
     * @param firstPort first port operand.
     * @param lastPort  last port operand.
     */
    public PortObject(String operator, int firstPort, int lastPort) {
        _operator = operator;

        if (_operator.equals(RANGE))
            _portSpec = new PortSpec(PortOperator.RANGE, firstPort,
                    lastPort);

        if (_portSpec == null)
            throw new JtaclInternalException("Invalid port operator" +
                    _operator);

    }

    /**
     * Checks if this {@link PortObject} matches the {@link PortSpec} in argument.
     *
     * @param port port spec to check.
     * @return the {@link MatchResult} between the portSpec in argument and
     * this port object.
     */
    public MatchResult matches(PortSpec port) {
        return _portSpec.matches(port);
    }

    /**
     * Returns the operator of this port object.
     *
     * @return the operator of this port object.
     */
    public String getOperator() {
        return _operator;
    }

    /**
     * Returns the {@link PortSpec} associated to this port object
     *
     * @return the {@link PortSpec} associated to this port object
     */
    public PortSpec getPortSpec() {
        return _portSpec;
    }


}
