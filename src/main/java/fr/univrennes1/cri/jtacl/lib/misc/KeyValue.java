/*
 * Copyright (c) 2010, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.misc;

/**
 * Describe a key and value item.
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public class KeyValue {
	String _key;
	String _value;

	/**
	 * Construct a new KeyValue object.
	 * @param key the key of the object (its name).
	 * @param value the value set to the object.
	 */
	public KeyValue(String key, String value) {
		_key = key;
		_value = value;
	}

	/**
	 * Returns the key.
	 * @return the key.
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * Returns the value.
	 * @return the value.
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * Sets the value.
	 * @param value value to set.
	 */
	public void setValue(String value) {
		_value = value;
	}

}
