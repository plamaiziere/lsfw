/*
 * Copyright (c) 2010, Université de Rennes 1
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * OutputStream with 'tee'
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class ConsoleOutputStream extends FilterOutputStream {

	protected OutputStream _out;
	protected OutputStream _teeStream;

	public ConsoleOutputStream(OutputStream out) {
		super(out);
		_out = out;
	}

	@Override
	public void write(int b) throws IOException {
		super.write(b);
		if (hasTee())
			_teeStream.write(b);
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (hasTee())
			unTee();
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		if (hasTee())
			_teeStream.flush();
	}

	/**
	 * 'Tee' the specified file named filename to this stream.
	 * @param fileName of the file to tee
	 * @param append true if data will be appended.
	 * @throws FileNotFoundException if the file is not found
	 */
	public void tee(String fileName, boolean append) throws FileNotFoundException {
		_teeStream = new PrintStream(new FileOutputStream(fileName, append));
	}

	/**
	 * 'Untee' this stream
	 * @throws IOException if problem occur.
	 */
	public void unTee() throws IOException {
		if (hasTee())
			_teeStream.close();
	}

	/**
	 * Returns true if there is a tee on this stream.
	 * @return true if there is a tee on this stream.
	 */
	public boolean hasTee() {
		return _teeStream != null;
	}

}
