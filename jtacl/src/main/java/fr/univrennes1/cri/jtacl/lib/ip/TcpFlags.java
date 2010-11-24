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

package fr.univrennes1.cri.jtacl.lib.ip;

/**
 * This class handles TCP flags
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class TcpFlags {

	protected static final int CWR = 1;
	protected static final int ECE = 2;
	protected static final int URG = 4;
	protected static final int ACK = 8;
	protected static final int PSH = 0x10;
	protected static final int RST = 0x20;
	protected static final int SYN = 0x40;
	protected static final int FIN = 0x80;

	protected static final int _msk = 0xFF;

	protected int _flags;

	public TcpFlags() {
	}

	public TcpFlags(int flags) {
		_flags = flags;
	}

	public TcpFlags(TcpFlags flags) {
		_flags = flags.getFlags();
	}

	public TcpFlags(String flags) {
		String f = flags.toUpperCase();

		setCWR(f.contains("W"));
		setECE(f.contains("E"));
		setURG(f.contains("U"));
		setACK(f.contains("A"));
		setPSH(f.contains("P"));
		setRST(f.contains("R"));
		setSYN(f.contains("S"));
		setFIN(f.contains("F"));
	}

	public void clearAll() {
		_flags = 0;
	}

	public int getFlags() {
		return _flags;
	}

	public void setFlags(int flags) {
		_flags = flags;
	}

	/**
	 * Returns true if the CWR flag is set.
	 * @return true if the CWR flag is set.
	 */
	public boolean hasCWR() {
		return (_flags & CWR) != 0;
	}

	/**
	 * Sets the CWR flag
	 */
	public void setCWR() {
		_flags |= CWR;
	}

	/**
	 * Clears the CWR flag
	 */
	public void clearCWR() {
		_flags = (~CWR & _flags) & _msk;
	}

	/**
	 * Sets the CWR flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setCWR(boolean flag) {
		if (flag)
			setCWR();
		else
			clearCWR();
	}

	/**
	 * Returns true if the ECE flag is set.
	 * @return true if the ECE flag is set.
	 */
	public boolean hasECE() {
		return (_flags & ECE) != 0;
	}

	/**
	 * Sets the ECE flag
	 */
	public void setECE() {
		_flags |= ECE;
	}

	/**
	 * Clears the ECE flag
	 */
	public void clearECE() {
		_flags = (~ECE & _flags) & _msk;
	}

	/**
	 * Sets the ECE flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setECE(boolean flag) {
		if (flag)
			setECE();
		else
			clearECE();
	}

	/**
	 * Returns true if the URG flag is set.
	 * @return true if the URG flag is set.
	 */
	public boolean hasURG() {
		return (_flags & URG) != 0;
	}

	/**
	 * Sets the URG flag
	 */
	public void setURG() {
		_flags |= URG;
	}

	/**
	 * Clears the URG flag
	 */
	public void clearURG() {
		_flags = (~URG & _flags) & _msk;
	}

	/**
	 * Sets the URG flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setURG(boolean flag) {
		if (flag)
			setURG();
		else
			clearURG();
	}

	/**
	 * Returns true if the ACK flag is set.
	 * @return true if the ACK flag is set.
	 */
	public boolean hasACK() {
		return (_flags & ACK) != 0;
	}

	/**
	 * Sets the ACK flag
	 */
	public void setACK() {
		_flags |= ACK;
	}

	/**
	 * Clears the ACK flag
	 */
	public void clearACK() {
		_flags = (~ACK & _flags) & _msk;
	}

	/**
	 * Sets the ACK flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setACK(boolean value) {
		if (value)
			setACK();
		else
			clearACK();
	}

	/**
	 * Returns true if the PSH flag is set.
	 * @return true if the PSH flag is set.
	 */
	public boolean hasPSH() {
		return (_flags & PSH) != 0;
	}

	/**
	 * Sets the PSH flag
	 */
	public void setPSH() {
		_flags |= PSH;
	}

	/**
	 * Clears the PSH flag
	 */
	public void clearPSH() {
		_flags = (~PSH & _flags) & _msk;
	}

	/**
	 * Sets the PSH flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setPSH(boolean value) {
		if (value)
			setPSH();
		else
			clearPSH();
	}

	/**
	 * Returns true if the RST flag is set.
	 * @return true if the RST flag is set.
	 */
	public boolean hasRST() {
		return (_flags & RST) != 0;
	}

	/**
	 * Sets the RST flag
	 */
	public void setRST() {
		_flags |= RST;
	}

	/**
	 * Clears the RST flag
	 */
	public void clearRST() {
		_flags = (~RST & _flags) & _msk;
	}

	/**
	 * Sets the RST flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setRST(boolean value) {
		if (value)
			setRST();
		else
			clearRST();
	}

	/**
	 * Returns true if the SYN flag is set.
	 * @return true if the SYN flag is set.
	 */
	public boolean hasSYN() {
		return (_flags & SYN) != 0;
	}

	/**
	 * Sets the SYN flag
	 */
	public void setSYN() {
		_flags |= SYN;
	}

	/**
	 * Clears the SYN flag
	 */
	public void clearSYN() {
		_flags = (~SYN & _flags) & _msk;
	}

	/**
	 * Sets the SYN flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setSYN(boolean value) {
		if (value)
			setSYN();
		else
			clearSYN();
	}

	/**
	 * Returns true if the FIN flag is set.
	 * @return true if the FIN flag is set.
	 */
	public boolean hasFIN() {
		return (_flags & FIN) != 0;
	}

	/**
	 * Sets the FIN flag
	 */
	public void setFIN() {
		_flags |= FIN;
	}

	/**
	 * Clears the FIN flag.
	 */
	public void clearFIN() {
		_flags = (~FIN & _flags) & _msk;
	}

	/**
	 * Sets the FIN flag according to the value in argument.
	 * @param flag value to set.
	 */
	public void setFIN(boolean value) {
		if (value)
			setFIN();
		else
			clearFIN();
	}

	/**
	 * Returns true if all flags are unset.
	 * @return true if all flags are unset.
	 */
	public boolean isEmpty() {
		return _flags == 0;
	}

	/**
	 * Returns true if this instance contains the flags in argument.
	 * @param flags flags to check
	 * @return true if this instance contains the flags in argument.
	 */
	public boolean contains(TcpFlags flags) {
		return (_flags & flags.getFlags()) == flags.getFlags();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TcpFlags other = (TcpFlags) obj;
		if (this._flags != other._flags) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + this._flags;
		return hash;
	}

	@Override
	public String toString() {
		String r = "";

		if (hasCWR())
			r += "W";
		if (hasECE())
			r += "E";
		if (hasURG())
			r += "U";
		if (hasACK())
			r += "A";
		if (hasPSH())
			r += "P";
		if (hasRST())
			r += "R";
		if (hasSYN())
			r += "S";
		if (hasFIN())
			r += "F";

		return r;
	}

}
