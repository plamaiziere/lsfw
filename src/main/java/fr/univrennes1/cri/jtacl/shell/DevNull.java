/*
 * Copyright (c) 2012, Universite de Rennes 1
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

package fr.univrennes1.cri.jtacl.shell;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * PrintStream a la /dev/null
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public final class DevNull {
	/**
	 * "/dev/null" out stream.
	 */
    public final static PrintStream out
			= new PrintStream(new OutputStream() {
		@Override
        public void close() {}
		
		@Override
        public void flush() {}
		
		@Override
        public void write(byte[] b) {}
		
		@Override
        public void write(byte[] b, int off, int len) {}
		
		@Override
        public void write(int b) {}
    } );
}
