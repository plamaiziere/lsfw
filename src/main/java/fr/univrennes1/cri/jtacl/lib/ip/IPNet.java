/*
 * Copyright (c) 2010 - 2013, Universite de Rennes 1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the 2-clause BSD license.
 *
 * See COPYING for more details.
 */

package fr.univrennes1.cri.jtacl.lib.ip;

import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class and tools to deal with IPv4 and IPv6 addresses and networks.<br/>
 * An IP address is a {@link BigInteger} number between 0 and 2^128, and could
 * have a prefix length to specify network mask.<br/><br/>
 * Mostly taken from IPy http://pypi.python.org/pypi/IPy
 *
 * @author Patrick Lamaiziere <patrick.lamaiziere@univ-rennes1.fr>
 */
public final class IPNet implements Comparable, IPRangeable {

    /*
     * ip, prefixlen, ipversion
     */
    protected final IPBase _ip;

    /*
     * cache of these costly to compute values
     */
    protected IPNet _hostAddress;
    protected IPNet _networkAddress;
    protected IPNet _lastNetworkAddress;

    /*
     * dns cache
     */
    protected static long _dnsCacheTtl = 60000;

    protected static final Map<String, DnsCacheEntry> _dnsCache =
            new ConcurrentHashMap<>();

    protected static final Map<InetAddress, RevertDnsCacheEntry> _dnsRevertCache =
            new ConcurrentHashMap<>();

    protected static final Map<InetAddress, RevertDnsCacheEntry> _dnsPtrCache =
            new ConcurrentHashMap<>();

    protected static class CacheCollector extends Thread {

        protected void collect() {
            long date = new Date().getTime();
            for (DnsCacheEntry ce : _dnsCache.values()) {
                if (date - ce.getDate() >= _dnsCacheTtl) {
                    _dnsCache.remove(ce.getHostname());
                }
            }
            for (RevertDnsCacheEntry ce : _dnsPtrCache.values()) {
                if (date - ce.getDate() >= _dnsCacheTtl) {
                    _dnsPtrCache.remove(ce.getAddress());
                }
            }
            for (RevertDnsCacheEntry ce : _dnsRevertCache.values()) {
                if (date - ce.getDate() >= _dnsCacheTtl) {
                    _dnsPtrCache.remove(ce.getAddress());
                }
            }
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            while (true) {
                collect();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    //
                }
            }
        }
    }

    protected static final CacheCollector _collector = new CacheCollector();

    protected static String[] ipV4ToHextet(String addr)
            throws UnknownHostException {

        String[] result = new String[2];
        /*
         * parse the IPv4 address and return it as two hextets
         */
        BigInteger ipInt = parseAddressIPv4(addr);
        BigInteger bi = ipInt.shiftRight(16);
        result[0] = bi.toString(16);
        bi = ipInt.and(IP.BIG_INT_0xFFFF);
        result[1] = bi.toString(16);

        return result;
    }

    protected static BigInteger parseAddressIPv4(String addr) throws UnknownHostException {

        // assume IPv4  ('127' gets interpreted as '127.0.0.0')
        String[] sbytes = addr.split("\\.");
        if (sbytes.length > 4)
            throw new UnknownHostException("IPv4 Address with more than 4 bytes: " + addr);
        int[] ipInts = new int[4];
        for (int i = 0; i < 4; i++) {
            if (i < sbytes.length)
                try {
                    ipInts[i] = Integer.parseInt(sbytes[i]);
                } catch (NumberFormatException e) {
                    throw new UnknownHostException("IP Address with not a number: " + addr);
                }
            else
                ipInts[i] = 0;
            if (ipInts[i] < 0 || ipInts[i] > 255)
                throw new UnknownHostException("IPv4 Address with ! 0 <= bytes <= 255: " + addr);
        }

        BigInteger ipInt = BigInteger.ZERO;
        BigInteger bi;

        // ipInts[0] << 24
        bi = BigInteger.valueOf(ipInts[0]);
        bi = bi.shiftLeft(24);
        ipInt = ipInt.add(bi);

        // ipInts[1] << 16
        bi = BigInteger.valueOf(ipInts[1]);
        bi = bi.shiftLeft(16);
        ipInt = ipInt.add(bi);

        // ipInts[2] << 8
        bi = BigInteger.valueOf(ipInts[2]);
        bi = bi.shiftLeft(8);
        ipInt = ipInt.add(bi);

        // ipInts[3]
        bi = BigInteger.valueOf(ipInts[3]);
        ipInt = ipInt.add(bi);

        return ipInt;
    }

    protected static BigInteger parseAddressIPv6(String addr)
            throws UnknownHostException {

        /*
         * Split the address into two lists, one for items on the left of the '::'
         * the other for the items on the right
         *
         *  example: '1080:200C::417A'
         *		=> leftItems = "1080", "200C"
         *		=> rightItems = 417A
         */

        String left;
        String right;
        String[] saddr = addr.split("::");
        int count = saddr.length;

        if (count > 2)
            // Invalid IPv6, eg. '1::2::'
            throw new UnknownHostException(
                    "Invalid IPv6 address: more than one '::' : " + addr);

        if (count == 0) {
            // addr == "::"
            return BigInteger.ZERO;
        }

        if (count == 1) {
            /*
             * we have to test where is the "::" to know which string is
             * the left one
             */
            if (addr.startsWith("::")) {
                left = null;
                right = saddr[0];
            } else {
                left = saddr[0];
                right = null;
            }
        } else {
            left = saddr[0];
            right = saddr[1];
        }

        ArrayList<String> rightItems = new ArrayList<>();
        ArrayList<String> items = new ArrayList<>();

        String[] lItems = null;
        String[] rItems = null;


        if (left != null)
            lItems = left.split(":");

        if (right != null)
            rItems = right.split(":");

        for (int i = 0; rItems != null && i < rItems.length; i++) {
            String s = rItems[i];
            int p1 = s.indexOf('.');
            if (p1 >= 0) {
                // IPv6 ending with IPv4 like '::ffff:192.168.0.1'
                if (i != rItems.length - 1)
                    // Invalid IPv6: 'ffff:192.168.0.1::'
                    throw new UnknownHostException(
                            "Invalid IPv6 address: IPv4 only allowed at the tail: "
                                    + addr);
                String[] hextets = ipV4ToHextet(s);
                rightItems.add(hextets[0]);
                rightItems.add(hextets[1]);
            } else
                rightItems.add(s);
        }

        for (int i = 0; lItems != null && i < lItems.length; i++) {
            String s = lItems[i];
            if (s != null && !s.isEmpty()) {
                int p1 = s.indexOf('.');
                if (p1 >= 0) {
                    // IPv6 ending with IPv4 like '::ffff:192.168.0.1'
                    if ((i != lItems.length - 1) || rItems != null)
                        // Invalid IPv6: 'ffff:192.168.0.1::'
                        throw new UnknownHostException(
                                "Invalid IPv6 address: IPv4 only allowed at the tail: "
                                        + addr);
                    String[] hextets = ipV4ToHextet(s);
                    items.add(hextets[0]);
                    items.add(hextets[1]);
                } else
                    items.add(s);
            }
        }

        // pad with "0" between left and right
        while ((items.size() + rightItems.size()) < 8)
            items.add("0");

        // add right items
        for (String s : rightItems)
            items.add(s);

        /*
         * Here we have a list of 8 hextets
         */
        if (items.size() != 8)
            // Invalid IPv6, eg. '1:2:3'
            throw new UnknownHostException(
                    "Invalid IPv6 address: should have 8 hextets: " + addr);

        /*
         * Convert hextets to BigInteger
         */
        BigInteger value = BigInteger.ZERO;

        for (String item : items) {
            BigInteger hexlet;
            try {
                hexlet = new BigInteger(item, 16);
            } catch (NumberFormatException e) {
                throw new UnknownHostException(
                        "Invalid IPv6 address: invalid hextet: " + addr);
            }
            if ((hexlet.compareTo(BigInteger.ZERO) < 0) || (hexlet.compareTo(IP.BIG_INT_0xFFFF) > 0))
                throw new UnknownHostException(
                        "Invalid IPv6 address: invalid hextet: " + addr);
            value = value.multiply(IP.BIG_INT_0x10000);
            value = value.add(hexlet);
        }

        return value;
    }

    protected static IPBase parseAddress(String addr)
            throws UnknownHostException {

        IPBase result;

        try {
            if (addr.startsWith("0x")) {
                BigInteger bi = new BigInteger(addr.substring(2), 16);

                if (!IP.isValidIP(bi, IPversion.IPV6))
                    throw new UnknownHostException(
                            "IP Address must be 0 <= IP < 2^128: " + addr);

                if (bi.compareTo(IP.MAX_IPV4_NUMBER) <= 0) {
                    result = new IPBase(bi, 0, IPversion.IPV4);
                    return result;
                } else {
                    result = new IPBase(bi, 0, IPversion.IPV6);
                    return result;
                }
            }
            if (addr.contains(":")) {
                // IPv6 notation
                BigInteger bi = parseAddressIPv6(addr);
                result = new IPBase(bi, 0, IPversion.IPV6);
                return result;
            }
            /*
             * XXX: not sure if this is a good idea
             */
            if (addr.length() == 32) {
                // assume IPv6 in pure hexadecimal notation
                BigInteger bi = new BigInteger(addr, 16);
                result = new IPBase(bi, 0, IPversion.IPV6);
                return result;
            }

            if (addr.contains(".") || (addr.length() < 4) &&
                    Integer.parseInt(addr) < 256) {

                BigInteger bi = parseAddressIPv4(addr);
                result = new IPBase(bi, 0, IPversion.IPV4);
                return result;
            }

            /*
             * we try to interprete it as a decimal digit -
             * this only works for numbers > 255 ... others
             * will be interpreted as IPv4 first byte
             */
            BigInteger bi = new BigInteger(addr);

            if (!IP.isValidIP(bi, IPversion.IPV6))
                throw new UnknownHostException(
                        "IP Address must be 0 <= IP < 2^128: " + addr);

            if (bi.compareTo(IP.MAX_IPV4_NUMBER) <= 0) {
                result = new IPBase(bi, 0, IPversion.IPV4);
                return result;
            } else {
                result = new IPBase(bi, 0, IPversion.IPV6);
                return result;
            }

        } catch (NumberFormatException e) {
            throw new UnknownHostException("IP Address must contain numbers: "
                    + addr);
        }
    }

    protected static IPBase makeIP(BigInteger ip, int prefixLen,
                                   IPversion ipVersion) throws UnknownHostException {

        if (!IP.isValidIP(ip, ipVersion))
            throw new UnknownHostException("Invalid IP address 0 <= IP < " +
                    ((ipVersion == IPversion.IPV4) ? "2^32 :" : "2^128 :") + ip);

        if (!IP.isValidPrefixLen(prefixLen, ipVersion))
            throw new UnknownHostException("Invalid prefix length 0 <= prefix <= " +
                    IP.maxPrefixLen(ipVersion) + " :" + ip);

        return new IPBase(ip, prefixLen, ipVersion);
    }

    protected static IPBase makeFromIP(String sip)
            throws UnknownHostException {

        IPBase result = parseAddress(sip);
        return makeIP(result.getIP(), IP.maxPrefixLen(result.getIpVersion()),
                result.getIpVersion());
    }

    protected static IPBase makeFromRange(String data, String sfirst, String slast)
            throws UnknownHostException {

        IPBase first;
        IPBase last;

        first = parseAddress(sfirst);
        last = parseAddress(slast);
        if ((first.isIPv4() && !last.isIPv4())
                || (first.isIPv6() && !last.isIPv6()))
            throw new UnknownHostException(
                    "First-last notation must have the same address family: " + data);
        if (first.getIP().compareTo(last.getIP()) > 0)
            throw new UnknownHostException(
                    "Last address must be greater than first: " + data);

        int prefixlen = IP.maxPrefixLen(first.getIpVersion());
        IPNet iFirst = new IPNet(first.getIP(), first.getIpVersion(), prefixlen);
        IPNet iLast = new IPNet(last.getIP(), last.getIpVersion(), prefixlen);
        IPRange range = new IPRange(iFirst, iLast);
        if (!range.isNetwork())
            throw new UnknownHostException("Range is not on a network boundary");

        return new IPBase(range.toIPNet());
    }

    protected static int getPrefixFromNetmask(String smask, IPversion ipVersion)
            throws UnknownHostException {

        int prefix;
        /*
         * check if the netmask is like a.b.c.d/255.255.255.0
         */
        int pos = smask.indexOf('.');
        if (pos >= 0) {
            IPBase netmask = parseAddress(smask);
            if (!netmask.isIPv4())
                throw new UnknownHostException("Netmask must be IPv4: " + smask);
            if (ipVersion != IPversion.IPV4)
                throw new UnknownHostException(
                        "Dot netmask with not an IPv4 address: " + smask);
            prefix = IP.netmaskToPrefixLen(netmask.getIP());
        } else {
            // cidr notation /n
            try {
                prefix = Integer.parseInt(smask);
            } catch (NumberFormatException e) {
                throw new UnknownHostException(
                        "Netmask must contain numbers: " + smask);
            }
            if (!IP.isValidPrefixLen(prefix, ipVersion))
                throw new UnknownHostException(
                        "Invalid prefix 0 <= prefix <= " +
                                IP.maxPrefixLen(ipVersion) + " :" + smask);
        }
        return prefix;
    }

    protected static IPBase makeFromNetMask(String data, String sip, String smask)
            throws UnknownHostException {

        IPBase result = parseAddress(sip);
        int prefix = getPrefixFromNetmask(smask, result.getIpVersion());
        return makeIP(result.getIP(), prefix, result.getIpVersion());
    }

    /**
     * Constructs a new {@link IPNet} IP address from an IPBase address.
     *
     * @param ip the {@link IPBase} IP address.
     */
    public IPNet(IPBase ip) {

        _ip = ip;
    }

    /**
     * Constructs a new {@link IPNet} IP address.
     *
     * @param ip        the {@link BigInteger} IP address as a number
     * @param ipVersion the {@link IPversion} IP version of this address.
     * @param prefixLen the prefixlen for this IP address.
     * @throws UnknownHostException if some parameters are invalid
     */
    public IPNet(BigInteger ip, IPversion ipVersion, int prefixLen)
            throws UnknownHostException {

        _ip = makeIP(ip, prefixLen, ipVersion);
    }

    /**
     * Constructs a new {@link IPNet} object as a single IP with a prefixlen set
     * to 32 or 128 according to the IP version.
     *
     * @param ip        the {@link BigInteger} IP adress as a number.
     * @param ipVersion the {@link IPversion} IP version of this address.
     * @throws UnknownHostException if some parameters are invalid
     */
    public IPNet(BigInteger ip, IPversion ipVersion)
            throws UnknownHostException {

        _ip = makeIP(ip, IP.maxPrefixLen(ipVersion), ipVersion);
    }

    /**
     * Constructs a new {@link IPNet} object according to the string data.
     * <br/><br/>
     * Several notations are supported:
     * <ul>
     * 	<li>Decimal: IPNet("n")<br/>
     * 		n &lt= 255 -> assumed to be the IPv4 address n.0.0.0<br/>
     * 			example 127 -> 127.0.0.0</li>
     * <li> n &gt= 256 -> assumed to be the address 'n'.<br/>
     * 		If n is less than 2^32 theIPversion is set to IPv4, else to IPv6.</li>
     * <li>
     * 	Hexadecimal: IPNet("0xn")<br/>
     * 		the number n is converted to the address ip as a number<br/></li>
     * <li>
     *  Dot notation (IPv4) : 'IPNet("x.y.w.z")<br/>
     * 		if less than 4 bytes are specified, it is padded with some "0"<br/>
     * 			ex: 127.0 -> 127.0.0.0</li>
     * <li>
     * Ipv6:
     * 		IPv6 notation is supported, as well as an IPv4 address specified<br/>
     * 		at the tail of the string<br/>
     * 			ex: IPNet("::1:127.0.0.1")</li>
     * <li>
     *  Netmask and prefix<br/>
     * 		IPv4: /n or /w.x.y.z<br/>
     * 		IPv6: /n only</li>
     * <li>
     * 	Range<br/>
     * 		a range between two addresses can be specified, the first IP address
     * is used as the address of the {@link IPNet} ip and the second is used
     * to compute the prefixlen.<br/>
     * 			ex: IPNet("0.0.0.0-255.255.255.255)
     * </li>
     * <li>
     * Name resolution<br/>
     *   A data string starting with '@' or '@@' specifies a host name to
     *   resolve using dns. The number of '@' characters specifies the address
     *   family to use (IPv4 or IPv6). <br/>
     * 		ex : @localhost/34 (returns 127.0.0.1/24). <br/>
     * 		ex : @@localhost (returns ::1/128). <br/>
     * </ul>
     *
     * @param data The {@link String} string to parse.
     * @throws UnknownHostException if some parameters are invalid or if we
     *                              can't parse the string.
     */
    public IPNet(String data) throws UnknownHostException {

        // resolve IPV6
        if (data.startsWith("@@")) {
            String host = data.substring(2);
            IPNet ip = getByName(host, IPversion.IPV6);
            _ip = ip._ip;
            return;
        }

        // resolve IPV4
        if (data.startsWith("@")) {
            String host = data.substring(1);
            IPNet ip = getByName(host, IPversion.IPV4);
            _ip = ip._ip;
            return;
        }

        // splitting of a string into IP and prefixlen et. al.
        String[] split = data.split("-");
        if (split.length > 2)
            throw new UnknownHostException(
                    "Only one '-' allowed in IP Address: " + data);

        if (split.length == 2) {
            // a.b.c.0-a.b.c.255 specification ?
            _ip = makeFromRange(data, split[0].trim(), split[1].trim());
            return;
        }
        if (split.length == 1) {
            split = data.split("/");
            // netmask specification ?
            if (split.length > 2)
                throw new UnknownHostException(
                        "Only one '/' allowed in IP Address: " + data);

            if (split.length == 1) {
                // no prefix given, use defaults
                _ip = makeFromIP(split[0]);
                return;
            } else {
                _ip = makeFromNetMask(data, split[0], split[1]);
                return;
            }
        }
        throw new UnknownHostException("Can't parse IP Address: " + data);
    }

    /**
     * Constructs a new {@link IPNet} object from an InetAddress in argument.
     *
     * @param address InetAddress to construct from.
     * @throws UnknownHostException if problem occurs.
     */
    public IPNet(InetAddress address) throws UnknownHostException {
        _ip = makeFromIP(address.getHostAddress());
    }

    /**
     * Given the name of a host, returns a list of its IP addresses
     * matching the IP version in argument.
     * If the name of the host contains a mask, the mask is applied to the
     * resulting ip addresses.
     *
     * @param hostname  the name of the host
     * @param ipVersion the IP version of the addresses to return.
     * @return a list of all the IP addresses for a given host name.
     * @throws UnknownHostException if no IP address for the host could be
     *                              found, or if a scope_id was specified for a global IPv6 address.
     * @throws SecurityException    if a security manager exists and its
     *                              checkConnect method doesn't allow the operation.
     * @see InetAddress
     */
    public static List<IPNet> getAllByName(String hostname, IPversion ipVersion)
            throws UnknownHostException {

        /*
         * XXX: this required because Java handle hostname like "127.0.0.1 foo"
         * as "127.0.0.1"
         */
        int idx = hostname.indexOf(' ');
        if (idx >= 0)
            throw new UnknownHostException("hostname can not contain spaces: "
                    + hostname);

        String split[] = hostname.split("/");
        String hname = split[0];
        int prefix = -1;

        // netmask specification ?
        if (split.length > 2)
            throw new UnknownHostException("Only one '/' allowed in IP Address: " +
                    hostname);

        if (split.length == 2)
            prefix = getPrefixFromNetmask(split[1], ipVersion);

        InetAddress inet[] = null;
        DnsCacheEntry entry = _dnsCache.get(hname);
        long date = new Date().getTime();
        if (entry != null) {
            if (date - entry.getDate() < _dnsCacheTtl)
                inet = entry.getIps();
            else
                _dnsCache.remove(hname);
        }
        if (inet == null) {
            inet = InetAddress.getAllByName(hname);
            entry = new DnsCacheEntry(hname, inet, date);
            _dnsCache.put(hname, entry);
        }

        ArrayList<IPNet> addresses = new ArrayList<>();
        for (InetAddress addr : inet) {
            if ((addr instanceof Inet4Address && ipVersion == IPversion.IPV4) ||
                    (addr instanceof Inet6Address && ipVersion == IPversion.IPV6)) {
                IPNet address = new IPNet(addr);
                if (prefix != -1)
                    address = address.setMask(prefix);
                addresses.add(address);
            }
        }
        if (addresses.isEmpty())
            throw new UnknownHostException("No IP Address found for: " + hname);
        return addresses;
    }

    /**
     * Given the name of a host, returns its IP address matching the IP
     * version in argument.
     * If the name of the host contains a mask, the mask is applied to the
     * resulting ip address.
     *
     * @param hostname  the name of the host
     * @param ipVersion the IP version of the address to return.
     * @return the IP address for a given host name.
     * @throws UnknownHostException if no IP address for the host could be
     *                              found, or if a scope_id was specified for a global IPv6 address.
     * @throws SecurityException    if a security manager exists and its
     *                              checkConnect method doesn't allow the operation.
     * @see InetAddress
     */
    public static IPNet getByName(String hostname, IPversion ipVersion)
            throws UnknownHostException {

        return getAllByName(hostname, ipVersion).get(0);
    }

    /**
     * Gets the fully qualified domain name for this IP address.
     * Best effort method, meaning we may not be able to return
     * the FQDN depending on the underlying system configuration.
     *
     * @return the cannonical hostname for this IP address.
     * @throws UnknownHostException if problem occurs.
     * @see InetAddress#getCanonicalHostName
     */
    public String getCannonicalHostname() throws UnknownHostException {
        InetAddress ip = toInetAddress();
        RevertDnsCacheEntry entry = _dnsRevertCache.get(ip);
        long date = new Date().getTime();
        String hostname = null;
        if (entry != null) {
            if (date - entry.getDate() < _dnsCacheTtl)
                hostname = entry.getHostname();
            else
                _dnsRevertCache.remove(ip);
        }
        if (hostname == null) {
            hostname = ip.getCanonicalHostName();
            entry = new RevertDnsCacheEntry(ip, hostname, date);
            _dnsRevertCache.put(ip, entry);
        }
        return hostname;
    }

    /**
     * Gets the DNS PTR entry for this IP address.
     *
     * @return The PTR entry for this IP address, null if not found.
     * @throws UnknownHostException if problem occurs.
     * @see InetAddress#getCanonicalHostName
     */
    public String getPtrHostname() throws UnknownHostException {
        InetAddress ip = toInetAddress();
        RevertDnsCacheEntry entry = _dnsPtrCache.get(ip);
        long date = new Date().getTime();
        String hostname = null;
        if (entry != null) {
            if (date - entry.getDate() < _dnsCacheTtl)
                hostname = entry.getHostname();
            else
                _dnsPtrCache.remove(ip);
        }
        if (hostname == null) {
            Resolver res = new ExtendedResolver();
            Name name = ReverseMap.fromAddress(ip);
            Record rec = Record.newRecord(name, Type.PTR, DClass.IN);
            Message query = Message.newQuery(rec);
            Message response;
            try {
                response = res.send(query);
            } catch (IOException ex) {
                throw new UnknownHostException(ex.getMessage());
            }
            Record[] answers = response.getSectionArray(Section.ANSWER);
            hostname = null;
            if (answers.length > 0) {
                for (Record r : answers) {
                    if (r.getType() == Type.PTR) {
                        hostname = ((PTRRecord) r).getTarget().toString();
                        hostname = hostname.substring(0, hostname.length() - 1);
                        break;
                    }
                }
            }
            entry = new RevertDnsCacheEntry(ip, hostname, date);
            _dnsPtrCache.put(ip, entry);
        }
        return hostname;
    }

    /**
     * Gets the hostname for this IP address.
     *
     * @return The hostname for this IP address.
     * @throws UnknownHostException if problem occurs.
     */
    public String getHostname() throws UnknownHostException {
        String hostname = getPtrHostname();
        hostname = hostname != null ? hostname : toString("i");

        return hostname;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IPNet other = (IPNet) obj;
        return _ip.equals(other._ip);
    }

    @Override
    public final int hashCode() {
        return _ip.hashCode();
    }

    @Override
    public final boolean sameIPVersion(IPRangeable range) {
        return getIpVersion().equals(range.getIpVersion());
    }

    @Override
    public IPNet getIpFirst() {
        return networkAddress();
    }

    @Override
    public IPNet getIpLast() {
        return lastNetworkAddress();
    }

    @Override
    public final boolean contains(IPRangeable iprange) {

        IPNet first = networkAddress();
        IPNet last = lastNetworkAddress();
        IPNet firstOther = iprange.getIpFirst();
        IPNet lastOther = iprange.getIpLast();

        return firstOther.isBetweenIP(first, last) &&
                lastOther.isBetweenIP(first, last);
    }

    @Override
    public final boolean overlaps(IPRangeable iprange) {
        IPNet first = networkAddress();
        IPNet last = lastNetworkAddress();
        IPNet firstOther = iprange.getIpFirst();
        IPNet lastOther = iprange.getIpLast();

        return first.isBetweenIP(firstOther, lastOther) ||
                last.isBetweenIP(firstOther, lastOther) ||
                firstOther.isBetweenIP(first, last) ||
                lastOther.isBetweenIP(first, last);
    }

    @Override
    public IPNet toIPNet() {
        return this;
    }

    /**
     * Checks if this {@link IPNet} instance is between two another
     * {@link IPNet} objects.<br/>
     *
     * @param first  the first {@link IPNet} object to compare.
     * @param second the second {@link IPNet} object to compare.
     * @return true if the {@link BigInteger} IP address of this instance is:
     * first &lt= IP &lt= second.
     * We do not take care of the prefix length of the {@link IPNet} objects.
     */
    public final boolean isBetweenIP(IPNet first, IPNet second) {
        if (getIP().compareTo(first.getIP()) < 0)
            return false;
        if (getIP().compareTo(second.getIP()) > 0)
            return false;
        return true;
    }

    /**
     * Returns the network IP address of this {@link IPNet} instance.
     *
     * @return the {@link IPNet} network IP address.
     */
    public final IPNet networkAddress() {
        if (_networkAddress == null) {
            BigInteger bi = IP.prefixLenToNetmask(_ip.getPrefixlen(),
                    _ip.getIpVersion());
            bi = _ip.getIP().and(bi);
            IPBase ip = new IPBase(bi, _ip.getPrefixlen(), _ip.getIpVersion());
            _networkAddress = new IPNet(ip);
        }
        return _networkAddress;
    }

    /**
     * Returns the host IP address of this {@link IPNet} instance.
     * The host address is expressed by setting the prefixlen to 32 or 128
     * according to the IP version of the instance.
     *
     * @return the {@link IPNet} host address.
     */
    public final IPNet hostAddress() {
        if (_hostAddress == null) {
            IPBase ip = new IPBase(_ip.getIP(),
                    IP.maxPrefixLen(_ip.getIpVersion()), _ip.getIpVersion());
            _hostAddress = new IPNet(ip);
        }
        return _hostAddress;
    }

    /**
     * Returns the last network IP address of this {@link IPNet} instance.<br/>
     * For IPv4, this is the broadcast address.
     *
     * @return the {@link IPNet} last network IP address of the network associated
     * to this instance.
     */
    public final IPNet lastNetworkAddress() {
        if (_lastNetworkAddress == null) {
            IPNet net = networkAddress();
            BigInteger bi = net.getIP().add(IP.networkLength(_ip.getPrefixlen(),
                    _ip.getIpVersion()));
            bi = bi.subtract(BigInteger.ONE);
            IPBase ip = new IPBase(bi, _ip.getPrefixlen(), _ip.getIpVersion());
            _lastNetworkAddress = new IPNet(ip);
        }
        return _lastNetworkAddress;
    }

    /**
     * Returns a new IP address of this {@link IPNet} instance with the mask
     * set to the value in argument.
     *
     * @param mask mask to set in cidr notation.
     * @return a new {@link IPNet} instance.
     * @throws UnknownHostException if some parameters are invalid
     */
    public final IPNet setMask(int mask) throws UnknownHostException {
        return new IPNet(_ip.getIP(), _ip.getIpVersion(), mask);
    }

    @Override
    public final boolean isHost() {
        return _ip.getPrefixlen() == IP.maxPrefixLen(_ip.getIpVersion());
    }

    @Override
    public final boolean isNetwork() {
        /*
         * true by design
         */
        return true;
    }

    /**
     * The "null" network for IPv4 (0.0.0.0/0)
     */
    public static IPNet NULL_IPV4 = null;

    /**
     * The "null" network for IPv6 (::0/0)
     */
    public static IPNet NULL_IPV6 = null;

    static {
        try {
            NULL_IPV4 = new IPNet(BigInteger.ZERO, IPversion.IPV4, 0);
            NULL_IPV6 = new IPNet(BigInteger.ZERO, IPversion.IPV6, 0);
        } catch (UnknownHostException ex) {
            // should not happen
        }

        _collector.setName("CacheCollector");
        _collector.setDaemon(true);
        _collector.start();
    }

    @Override
    public final boolean isIPv4() {
        return _ip.isIPv4();
    }

    /**
     * Checks if this {@link IPNet} instance designates the "null" network.<br/>
     * The "null" network is 0.0.0.0/0 for IPv4 and ::0/0 for IPv6
     *
     * @return true if this {@link IPNet} instance designates the "null" network.
     */
    public final boolean isNullNetwork() {
        return (_ip.getIpVersion() == IPversion.IPV4) ?
                equals(NULL_IPV4) : equals(NULL_IPV6);
    }

    @Override
    public final boolean isIPv6() {
        return _ip.isIPv6();
    }


    @Override
    public final IPversion getIpVersion() {
        return _ip.getIpVersion();
    }

    /**
     * Returns the prefix length of this {@link IPNet} instance.
     *
     * @return the prefix length of this {@link IPNet} instance.
     */
    public final int getPrefixLen() {
        return _ip.getPrefixlen();
    }

    /**
     * Returns the {@link BigInteger} IP address of this {@link IPNet} instance
     * as a number.
     *
     * @return the {@link BigInteger} IP address of this {@link IPNet} instance.
     */
    public final BigInteger getIP() {
        return _ip.getIP();
    }

    /**
     * Returns the length of this {@link IPNet} instance. The length is the
     * number of IP addresses associates to the network designated by the
     * {@link IPNet} instance.<br/><br/>
     * examples:<br/>
     * IPNet("192.0.0.1/24") =&gt 256 addresses.<br/>
     * IPNet("192.0.0.1/32") =&gt 1 address.<br/>
     *
     * @return the {@link BigInteger} number of IP addresses associates to the
     * network designated by the {@link IPNet} instance.
     */
    public final BigInteger networkLength() {
        return IP.networkLength(_ip.getPrefixlen(), _ip.getIpVersion());
    }

    @Override
    public final BigInteger length() {
        return networkLength();
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public final String toString(String format) {
        boolean fshort = format.contains("s");
        boolean fcompress = format.contains("::");
        boolean fnetmask = format.contains("n");
        boolean fip = format.contains("i");

        String[] s = null;
        switch (_ip.getIpVersion()) {
            case IPV4:
                s = IP.ipv4ToStrings(_ip.getIP(), _ip.getPrefixlen());
                if (fnetmask) {
                    BigInteger netmask = IP.prefixLenToNetmask(
                            _ip.getPrefixlen(), _ip.getIpVersion());
                    String[] ss = IP.ipv4ToStrings(netmask, 32);
                    s[1] = ss[0];
                }
                break;
            case IPV6:
                s = IP.ipv6ToStrings(_ip.getIP(), _ip.getPrefixlen(), fcompress);
                break;
        }
        String result = s[0];
        if (!(fshort ||
                (fip && _ip.getPrefixlen() == IP.maxPrefixLen(_ip.getIpVersion()))))
            result = result + "/" + s[1];
        return result;
    }

    @Override
    public String toNetString(String format) {
        return toString(format);
    }

    @Override
    public IPNet nearestNetwork() {
        return this;
    }

    @Override
    public final int compareTo(Object o) {
        IPNet obj = (IPNet) o;
        if (equals(obj))
            return 0;
        return getIP().compareTo(obj.getIP());
    }

    /**
     * Converts this instance to an InetAddress.<br/>
     * Does not convert the prefix length.
     *
     * @return An InetAddress corresponding to this instance.
     * @throws UnknownHostException if problem occurs.
     */
    public final InetAddress toInetAddress() throws UnknownHostException {
        InetAddress ip = InetAddress.getByName(toString("s"));
        return ip;
    }

    /**
     * Test whether that address is reachable.
     * This method uses InetAddress.isReachable() to test.
     *
     * @param timeout the time, in milliseconds, before the call aborts.
     * @return true if the address is reachable.
     * @throws UnknownHostException if problem occurs.
     * @see InetAddress#isReachable(int)
     */
    public final boolean isReachable(int timeout) throws UnknownHostException {
        try {
            return toInetAddress().isReachable(timeout);
        } catch (IOException ex) {
            throw new UnknownHostException(ex.getMessage());
        }
    }

    /**
     * Returns the time to live of the DNS cache.
     *
     * @return the time to live of the DNS cache.
     */
    public static long getDnsCacheTtl() {
        return _dnsCacheTtl;
    }

    /**
     * Set the time to live of the DNS cache.
     *
     * @param ttl value to set in ms.
     */
    public static void setDnsCacheTt(long ttl) {
        _dnsCacheTtl = ttl;
    }

}
