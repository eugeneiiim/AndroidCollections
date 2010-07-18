/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;

import androidcollections.annotations.Nullable;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;

/**
 * Static utility methods pertaining to {@link InetAddress} instances.
 *
 * <p><b>Important note:</b> Unlike {@code InetAddress.getByName()}, the
 * methods of this class never cause DNS services to be accessed. For
 * this reason, you should prefer these methods as much as possible over
 * their JDK equivalents whenever you are expecting to handle only
 * IP address string literals -- there is no blocking DNS penalty for a
 * malformed string.
 *
 * <p>This class hooks into the {@code sun.net.util.IPAddressUtil} class
 * to make use of the {@code textToNumericFormatV4} and
 * {@code textToNumericFormatV6} methods directly as a means to avoid
 * accidentally traversing all nameservices (it can be vitally important
 * to avoid, say, blocking on DNS at times).
 *
 * <p>When dealing with {@link Inet4Address} and {@link Inet6Address}
 * objects as byte arrays (vis. {@code InetAddress.getAddress()}) they
 * are 4 and 16 bytes in length, respectively, and represent the address
 * in network byte order.
 *
 * <p>Examples of IP addresses and their byte representations:
 * <ul>
 * <li>The IPv4 loopback address, {@code "127.0.0.1"}.<br/>
 *     {@code 7f 00 00 01}
 *
 * <li>The IPv6 loopback address, {@code "::1"}.<br/>
 *     {@code 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01}
 *
 * <li>From the IPv6 reserved documentation prefix ({@code 2001:db8::/32}),
 *     {@code "2001:db8::1"}.<br/>
 *     {@code 20 01 0d b8 00 00 00 00 00 00 00 00 00 00 00 01}
 *
 * <li>An IPv6 "IPv4 compatible" (or "compat") address,
 *     {@code "::192.168.0.1"}.<br/>
 *     {@code 00 00 00 00 00 00 00 00 00 00 00 00 c0 a8 00 01}
 *
 * <li>An IPv6 "IPv4 mapped" address, {@code "::ffff:192.168.0.1"}.<br/>
 *     {@code 00 00 00 00 00 00 00 00 00 00 ff ff c0 a8 00 01}
 * </ul>
 *
 * <p>A few notes about IPv6 "IPv4 mapped" addresses and their observed
 * use in Java.
 * <br><br>
 * "IPv4 mapped" addresses were originally a representation of IPv4
 * addresses for use on an IPv6 socket that could receive both IPv4
 * and IPv6 connections (by disabling the {@code IPV6_V6ONLY} socket
 * option on an IPv6 socket).  Yes, it's confusing.  Nevertheless,
 * these "mapped" addresses were never supposed to be seen on the
 * wire.  That assumption was dropped, some say mistakenly, in later
 * RFCs with the apparent aim of making IPv4-to-IPv6 transition simpler.
 *
 * <p>Technically one <i>can</i> create a 128bit IPv6 address with the wire
 * format of a "mapped" address, as shown above, and transmit it in an
 * IPv6 packet header.  However, Java's InetAddress creation methods
 * appear to adhere doggedly to the original intent of the "mapped"
 * address: all "mapped" addresses return {@link Inet4Address} objects.
 *
 * <p>For added safety, it is common for IPv6 network operators to filter
 * all packets where either the source or destination address appears to
 * be a "compat" or "mapped" address.  Filtering suggestions usually
 * recommend discarding any packets with source or destination addresses
 * in the invalid range {@code ::/3}, which includes both of these bizarre
 * address formats.  For more information on "bogons", including lists
 * of IPv6 bogon space, see:
 *
 * <ul>
 * <li><a target="_parent"
 *        href="http://en.wikipedia.org/wiki/Bogon_filtering"
 *       >http://en.wikipedia.org/wiki/Bogon_filtering</a>
 * <li><a target="_parent"
 *        href="http://www.cymru.com/Bogons/ipv6.txt"
 *       >http://www.cymru.com/Bogons/ipv6.txt</a>
 * <li><a target="_parent"
 *        href="http://www.cymru.com/Bogons/v6bogon.html"
 *       >http://www.cymru.com/Bogons/v6bogon.html</a>
 * <li><a target="_parent"
 *        href="http://www.space.net/~gert/RIPE/ipv6-filters.html"
 *       >http://www.space.net/~gert/RIPE/ipv6-filters.html</a>
 * </ul>
 *
 * @author Erik Kline
 * @since 5
 */
@Beta
public final class InetAddresses {

  private static final int IPV4_PART_COUNT = 4;
  private static final int IPV6_PART_COUNT = 8;
  private static final Inet4Address LOOPBACK4 =
      (Inet4Address) forString("127.0.0.1");
  private static final Inet4Address ANY4 =
      (Inet4Address) forString("0.0.0.0");

  private InetAddresses() {}

  /**
   * Returns an {@link Inet4Address}, given a byte array representation
   * of the IPv4 address.
   *
   * @param bytes byte array representing an IPv4 address (should be
   *              of length 4).
   * @return {@link Inet4Address} corresponding to the supplied byte
   *         array.
   * @throws IllegalArgumentException if a valid {@link Inet4Address}
   *         can not be created.
   */
  private static Inet4Address getInet4Address(byte[] bytes) {
    Preconditions.checkArgument(bytes.length == 4,
        "Byte array has invalid length for an IPv4 address: %s != 4.",
        bytes.length);

    try {
      InetAddress ipv4 = InetAddress.getByAddress(bytes);
      if (!(ipv4 instanceof Inet4Address)) {
        throw new UnknownHostException(
            String.format("'%s' is not an IPv4 address.",
                          ipv4.getHostAddress()));
      }

      return (Inet4Address) ipv4;
    } catch (UnknownHostException e) {

      /*
       * This really shouldn't happen in practice since all our byte
       * sequences should be valid IP addresses.
       *
       * However {@link InetAddress#getByAddress} is documented as
       * potentially throwing this "if IP address is of illegal length".
       *
       * This is mapped to IllegalArgumentException since, presumably,
       * the argument triggered some bizarre processing bug.
       */
      throw new IllegalArgumentException(
          String.format("Host address '%s' is not a valid IPv4 address.",
                        Arrays.toString(bytes)),
          e);
    }
  }

  /**
   * Returns the {@link InetAddress} having the given string
   * representation.
   *
   * <p>This deliberately avoids all nameservice lookups (e.g. no DNS).
   *
   * @param ipString {@code String} containing an IPv4 or IPv6 string literal,
   *                 e.g. {@code "192.168.0.1"} or {@code "2001:db8::1"}
   * @return {@link InetAddress} representing the argument
   * @throws IllegalArgumentException if the argument is not a valid
   *         IP string literal
   */
  public static InetAddress forString(String ipString) {
    byte[] addr = textToNumericFormatV4(ipString);
    if (addr == null) {
      // Scanning for IPv4 string literal failed; try IPv6.
      addr = textToNumericFormatV6(ipString);
    }

    // The argument was malformed, i.e. not an IP string literal.
    if (addr == null) {
      throw new IllegalArgumentException(
          String.format("'%s' is not an IP string literal.", ipString));
    }

    try {
      return InetAddress.getByAddress(addr);
    } catch (UnknownHostException e) {

      /*
       * This really shouldn't happen in practice since all our byte
       * sequences should be valid IP addresses.
       *
       * However {@link InetAddress#getByAddress} is documented as
       * potentially throwing this "if IP address is of illegal length".
       *
       * This is mapped to IllegalArgumentException since, presumably,
       * the argument triggered some processing bug in either
       * {@link IPAddressUtil#textToNumericFormatV4} or
       * {@link IPAddressUtil#textToNumericFormatV6}.
       */
      throw new IllegalArgumentException(
          String.format("'%s' is extremely broken.", ipString), e);
    }
  }

  /**
   * Returns {@code true} if the supplied string is a valid IP string
   * literal, {@code false} otherwise.
   *
   * @param ipString {@code String} to evaluated as an IP string literal
   * @return {@code true} if the argument is a valid IP string literal
   */
  public static boolean isInetAddress(String ipString) {
    try {
      forString(ipString);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static byte[] textToNumericFormatV4(String ipString) {

    boolean isIpv6 = false;

    // handle IPv6 forms of IPv4 addresses
    // TODO: use Ascii.toUpperCase() when available
    if (ipString.toUpperCase(Locale.US).startsWith("::FFFF:")) {
      ipString = ipString.substring(7);
    } else if (ipString.startsWith("::")) {
      ipString = ipString.substring(2);
      isIpv6 = true;
    }

    String[] address = ipString.split("\\.");
    if (address.length != IPV4_PART_COUNT) {
      return null;
    }
    try {
      byte[] bytes = new byte[IPV4_PART_COUNT];
      for (int i = 0; i < bytes.length; i++) {
        int piece = Integer.parseInt(address[i]);
        if (piece < 0 || piece > 255) {
          return null;
        }

        // No leading zeroes are allowed.  See
        // http://tools.ietf.org/html/draft-main-ipaddr-text-rep-00
        // section 2.1 for discussion.

        if (address[i].startsWith("0") && address[i].length() != 1) {
          return null;
        }
        bytes[i] = (byte) piece;
      }

      if (isIpv6) { // prepend with zeroes;
        byte[] data = new byte[2 * IPV6_PART_COUNT]; // Java initializes arrays to zero
        System.arraycopy(bytes, 0, data, 12, IPV4_PART_COUNT);
        return data;
      } else {
        return bytes;
      }
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private static byte[] textToNumericFormatV6(String ipString) {
    if (!ipString.contains(":")) {
      return null;
    }
    if (ipString.contains(":::")) {
      return null;
    }

    if (ipString.contains(".")) {
      ipString = convertDottedQuadToHex(ipString);
      if (ipString == null) {
        return null;
      }
    }

    ipString = padIpString(ipString);
    try {
      String[] address = ipString.split(":", IPV6_PART_COUNT);
      if (address.length != IPV6_PART_COUNT) {
        return null;
      }
      byte[] bytes = new byte[2 * IPV6_PART_COUNT];
      for (int i = 0; i < IPV6_PART_COUNT; i++) {
        int piece = address[i].equals("") ? 0 : Integer.parseInt(address[i], 16);
        bytes[2 * i] = (byte) ((piece & 0xFF00) >>> 8);
        bytes[2 * i + 1] = (byte) (piece & 0xFF);
      }
      return bytes;
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  // Fill in any omitted colons
  private static String padIpString(String ipString) {
    if (ipString.contains("::")) {
      int count = numberOfColons(ipString);
      StringBuilder buffer = new StringBuilder("::");
      for (int i = 0; i + count < 7; i++) {
        buffer.append(":");
      }
      ipString = ipString.replace("::", buffer);
    }
    return ipString;
  }

  private static int numberOfColons(String s) {
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == ':') {
        count++;
      }
    }
    return count;
  }

  private static String convertDottedQuadToHex(String ipString) {
    int lastColon = ipString.lastIndexOf(':');
    String initialPart = ipString.substring(0, lastColon + 1);
    String dottedQuad = ipString.substring(lastColon + 1);
    byte[] quad = textToNumericFormatV4(dottedQuad);
    if (quad == null) {
      return null;
    }
    String penultimate = Integer.toHexString(((quad[0] & 0xff) << 8) | (quad[1] & 0xff));
    String ultimate = Integer.toHexString(((quad[2] & 0xff) << 8) | (quad[3] & 0xff));
    return initialPart + penultimate + ":" + ultimate;
  }

  /**
   * Returns the string representation of an {@link InetAddress} suitable
   * for inclusion in a URI.
   *
   * <p>For IPv4 addresses, this is identical to
   * {@link InetAddress#getHostAddress()}, but for IPv6 addresses it
   * surrounds this text with square brackets; for example
   * {@code "[2001:db8::1]"}.
   *
   * <p>Per section 3.2.2 of
   * <a target="_parent"
   *    href="http://tools.ietf.org/html/rfc3986#section-3.2.2"
   *  >http://tools.ietf.org/html/rfc3986</a>,
   * a URI containing an IPv6 string literal is of the form
   * {@code "http://[2001:db8::1]:8888/index.html"}.
   *
   * <p>Use of either {@link InetAddress#getHostAddress()} or this
   * method is recommended over {@link InetAddress#toString()} when an
   * IP address string literal is desired.  This is because
   * {@link InetAddress#toString()} prints the hostname and the IP
   * address string joined by a "/".
   *
   * @param ip {@link InetAddress} to be converted to URI string literal
   * @return {@code String} containing URI-safe string literal
   */
  public static String toUriString(InetAddress ip) {
    if (ip instanceof Inet6Address) {
      return "[" + ip.getHostAddress() + "]";
    }
    return ip.getHostAddress();
  }

  /**
   * Returns an InetAddress representing the literal IPv4 or IPv6 host
   * portion of a URL, encoded in the format specified by RFC 3986 section 3.2.2.
   *
   * <p>This function is similar to {@link InetAddresses#forString(String)},
   * however, it requires that IPv6 addresses are surrounded by square brackets.
   *
   * <p>This function is the inverse of
   * {@link InetAddresses#toUriString(java.net.InetAddress)}.
   *
   * @param hostAddr A RFC 3986 section 3.2.2 encoded IPv4 or IPv6 address
   * @return an InetAddress representing the address in {@code hostAddr}
   * @throws IllegalArgumentException if {@code hostAddr} is not a valid
   *     IPv4 address, or IPv6 address surrounded by square brackets
   */
  public static InetAddress forUriString(String hostAddr) {
    Preconditions.checkNotNull(hostAddr);
    Preconditions.checkArgument(hostAddr.length() > 0, "host string is empty");
    InetAddress retval = null;

    // IPv4 address?
    try {
      retval = forString(hostAddr);
      if (retval instanceof Inet4Address) {
        return retval;
      }
    } catch (IllegalArgumentException e) {
      // Not a valid IP address, fall through.
    }

    // IPv6 address
    if (!(hostAddr.startsWith("[") && hostAddr.endsWith("]"))) {
      throw new IllegalArgumentException("Not a valid address: \"" + hostAddr + '"');
    }

    retval = forString(hostAddr.substring(1, hostAddr.length() - 1));
    if (retval instanceof Inet6Address) {
      return retval;
    }

    throw new IllegalArgumentException("Not a valid address: \"" + hostAddr + '"');
  }

  /**
   * Returns {@code true} if the supplied string is a valid URI IP string
   * literal, {@code false} otherwise.
   *
   * @param ipString {@code String} to evaluated as an IP URI host string literal
   * @return {@code true} if the argument is a valid IP URI host
   */
  public static boolean isUriInetAddress(String ipString) {
    try {
      forUriString(ipString);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Evaluates whether the argument is an IPv6 "compat" address.
   *
   * <p>An "IPv4 compatible", or "compat", address is one with 96 leading
   * bits of zero, with the remaining 32 bits interpreted as an
   * IPv4 address.  These are conventionally represented in string
   * literals as {@code "::192.168.0.1"}, though {@code "::c0a8:1"} is
   * also considered an IPv4 compatible address (and equivalent to
   * {@code "::192.168.0.1"}).
   *
   * <p>For more on IPv4 compatible addresses see section 2.5.5.1 of
   * <a target="_parent"
   *    href="http://tools.ietf.org/html/rfc4291#section-2.5.5.1"
   *    >http://tools.ietf.org/html/rfc4291</a>
   *
   * <p>NOTE: This method is different from
   * {@link Inet6Address#isIPv4CompatibleAddress} in that it more
   * correctly classifies {@code "::"} and {@code "::1"} as
   * proper IPv6 addresses (which they are), NOT IPv4 compatible
   * addresses (which they are generally NOT considered to be).
   *
   * @param ip {@link Inet6Address} to be examined for embedded IPv4
   *           compatible address format
   * @return {@code true} if the argument is a valid "compat" address
   */
  public static boolean isCompatIPv4Address(Inet6Address ip) {
    if (!ip.isIPv4CompatibleAddress()) {
      return false;
    }

    byte[] bytes = ip.getAddress();
    if ((bytes[12] == 0) && (bytes[13] == 0) && (bytes[14] == 0)
            && ((bytes[15] == 0) || (bytes[15] == 1))) {
      return false;
    }

    return true;
  }

  /**
   * Returns the IPv4 address embedded in an IPv4 compatible address.
   *
   * @param ip {@link Inet6Address} to be examined for an embedded
   *           IPv4 address
   * @return {@link Inet4Address} of the embedded IPv4 address
   * @throws IllegalArgumentException if the argument is not a valid
   *         IPv4 compatible address
   */
  public static Inet4Address getCompatIPv4Address(Inet6Address ip) {
    Preconditions.checkArgument(isCompatIPv4Address(ip),
        "Address '%s' is not IPv4-compatible.", ip.getHostAddress());

    return getInet4Address(copyOfRange(ip.getAddress(), 12, 16));
  }

  /**
   * Evaluates whether the argument is a 6to4 address.
   *
   * <p>6to4 addresses begin with the {@code "2002::/16"} prefix.
   * The next 32 bits are the IPv4 address of the host to which
   * IPv6-in-IPv4 tunneled packets should be routed.
   *
   * <p>For more on 6to4 addresses see section 2 of
   * <a target="_parent" href="http://tools.ietf.org/html/rfc3056#section-2"
   *    >http://tools.ietf.org/html/rfc3056</a>
   *
   * @param ip {@link Inet6Address} to be examined for 6to4 address
   *        format
   * @return {@code true} if the argument is a 6to4 address
   */
  public static boolean is6to4Address(Inet6Address ip) {
    byte[] bytes = ip.getAddress();
    return (bytes[0] == (byte) 0x20) && (bytes[1] == (byte) 0x02);
  }

  /**
   * Returns the IPv4 address embedded in a 6to4 address.
   *
   * @param ip {@link Inet6Address} to be examined for embedded IPv4
   *           in 6to4 address.
   * @return {@link Inet4Address} of embedded IPv4 in 6to4 address.
   * @throws IllegalArgumentException if the argument is not a valid
   *         IPv6 6to4 address.
   */
  public static Inet4Address get6to4IPv4Address(Inet6Address ip) {
    Preconditions.checkArgument(is6to4Address(ip),
        "Address '%s' is not a 6to4 address.", ip.getHostAddress());

    return getInet4Address(copyOfRange(ip.getAddress(), 2, 6));
  }

  /**
   * A simple data class to encapsulate the information to be found in a
   * Teredo address.
   *
   * <p>All of the fields in this class are encoded in various portions
   * of the IPv6 address as part of the protocol.  More protocols details
   * can be found at:
   * <a target="_parent" href="http://en.wikipedia.org/wiki/Teredo_tunneling"
   *    >http://en.wikipedia.org/wiki/Teredo_tunneling</a>.
   *
   * <p>The RFC can be found here:
   * <a target="_parent" href="http://tools.ietf.org/html/rfc4380"
   *    >http://tools.ietf.org/html/rfc4380</a>.
   *
   * @since 5
   */
  public static class TeredoInfo {
    private final Inet4Address server;
    private final Inet4Address client;
    private final int port;
    private final int flags;

    /**
     * Constructs a TeredoInfo instance.
     *
     * <p>Both server and client can be {@code null}, in which case the
     * value {@code "0.0.0.0"} will be assumed.
     *
     * @throws IllegalArgumentException if either of the {@code port}
     *         or the {@code flags} arguments are out of range of an
     *         unsigned short
     */
    public TeredoInfo(@Nullable Inet4Address server,
                      @Nullable Inet4Address client,
                      int port, int flags) {
      Preconditions.checkArgument((port >= 0) && (port <= 0xffff),
          "port '%d' is out of range (0 <= port <= 0xffff)", port);
      Preconditions.checkArgument((flags >= 0) && (flags <= 0xffff),
          "flags '%d' is out of range (0 <= flags <= 0xffff)", flags);

      if (server != null) {
        this.server = server;
      } else {
        this.server = ANY4;
      }

      if (client != null) {
        this.client = client;
      } else {
        this.client = ANY4;
      }

      this.port = port;
      this.flags = flags;
    }

    public Inet4Address getServer() {
      return server;
    }

    public Inet4Address getClient() {
      return client;
    }

    public int getPort() {
      return port;
    }

    public int getFlags() {
      return flags;
    }
  }

  /**
   * Evaluates whether the argument is a Teredo address.
   *
   * <p>Teredo addresses begin with the {@code "2001::/32"} prefix.
   *
   * @param ip {@link Inet6Address} to be examined for Teredo address
   *        format.
   * @return {@code true} if the argument is a Teredo address
   */
  public static boolean isTeredoAddress(Inet6Address ip) {
    byte[] bytes = ip.getAddress();
    return (bytes[0] == (byte) 0x20) && (bytes[1] == (byte) 0x01)
           && (bytes[2] == 0) && (bytes[3] == 0);
  }

  /**
   * Returns the Teredo information embedded in a Teredo address.
   *
   * @param ip {@link Inet6Address} to be examined for embedded Teredo
   *           information
   * @return extracted {@code TeredoInfo}
   * @throws IllegalArgumentException if the argument is not a valid
   *         IPv6 Teredo address
   */
  public static TeredoInfo getTeredoInfo(Inet6Address ip) {
    Preconditions.checkArgument(isTeredoAddress(ip),
        "Address '%s' is not a Teredo address.", ip.getHostAddress());

    byte[] bytes = ip.getAddress();
    Inet4Address server = getInet4Address(copyOfRange(bytes, 4, 8));

    int flags = ByteStreams.newDataInput(bytes, 8).readShort() & 0xffff;

    // Teredo obfuscates the mapped client port, per section 4 of the RFC.
    int port = ~ByteStreams.newDataInput(bytes, 10).readShort() & 0xffff;

    byte[] clientBytes = copyOfRange(bytes, 12, 16);
    for (int i = 0; i < clientBytes.length; i++) {
      // Teredo obfuscates the mapped client IP, per section 4 of the RFC.
      clientBytes[i] = (byte) ~clientBytes[i];
    }
    Inet4Address client = getInet4Address(clientBytes);

    return new TeredoInfo(server, client, port, flags);
  }

  /**
   * Evaluates whether the argument is an ISATAP address.
   *
   * <p>From RFC 5214: "ISATAP interface identifiers are constructed in
   * Modified EUI-64 format [...] by concatenating the 24-bit IANA OUI
   * (00-00-5E), the 8-bit hexadecimal value 0xFE, and a 32-bit IPv4
   * address in network byte order [...]"
   *
   * <p>For more on ISATAP addresses see section 6.1 of
   * <a target="_parent" href="http://tools.ietf.org/html/rfc5214#section-6.1"
   *    >http://tools.ietf.org/html/rfc5214</a>
   *
   * @param ip {@link Inet6Address} to be examined for ISATAP address
   *        format.
   * @return {@code true} if the argument is an ISATAP address
   */
  public static boolean isIsatapAddress(Inet6Address ip) {

    // If it's a Teredo address with the right port (41217, or 0xa101)
    // which would be encoded as 0x5efe then it can't be an ISATAP address.
    if (isTeredoAddress(ip)) {
      return false;
    }

    byte[] bytes = ip.getAddress();

    if ((bytes[8] | (byte) 0x03) != (byte) 0x03) {

      // Verify that high byte of the 64 bit identifier is zero, modulo
      // the U/L and G bits, with which we are not concerned.
      return false;
    }

    return (bytes[9] == (byte) 0x00) && (bytes[10] == (byte) 0x5e)
           && (bytes[11] == (byte) 0xfe);
  }

  /**
   * Returns the IPv4 address embedded in an ISATAP address.
   *
   * @param ip {@link Inet6Address} to be examined for embedded IPv4
   *           in ISATAP address
   * @return {@link Inet4Address} of embedded IPv4 in an ISATAP address
   * @throws IllegalArgumentException if the argument is not a valid
   *         IPv6 ISATAP address
   */
  public static Inet4Address getIsatapIPv4Address(Inet6Address ip) {
    Preconditions.checkArgument(isIsatapAddress(ip),
        "Address '%s' is not an ISATAP address.", ip.getHostAddress());

    return getInet4Address(copyOfRange(ip.getAddress(), 12, 16));
  }

  /**
   * Examines the InetAddress to extract the embedded IPv4 client address
   * if the InetAddress is an IPv6 address of one of the specified address
   * types that contain an embedded IPv4 address.
   *
   * <p>NOTE: ISATAP addresses are explicitly excluded from this method
   * due to their trivial spoofability.  With other transition addresses
   * spoofing involves (at least) infection of Google's BGP routing table.
   *
   * @param ip {@link Inet6Address} to be examined for embedded IPv4
   *           client address.
   * @return {@link Inet4Address} of embedded IPv4 client address.
   * @throws IllegalArgumentException if the argument does not have a valid
   *         embedded IPv4 address.
   */
  public static Inet4Address getEmbeddedIPv4ClientAddress(Inet6Address ip) {
    if (isCompatIPv4Address(ip)) {
      return getCompatIPv4Address(ip);
    }

    if (is6to4Address(ip)) {
      return get6to4IPv4Address(ip);
    }

    if (isTeredoAddress(ip)) {
      return getTeredoInfo(ip).getClient();
    }

    throw new IllegalArgumentException(
        String.format("'%s' has no embedded IPv4 address.",
                      ip.getHostAddress()));
  }

  /**
   * Returns an Inet4Address having the integer value specified by
   * the argument.
   *
   * @param address {@code int}, the 32bit integer address to be converted
   * @return {@link Inet4Address} equivalent of the argument
   */
  public static Inet4Address fromInteger(int address) {
    return getInet4Address(Ints.toByteArray(address));
  }

  /**
   * Returns an address from a <b>little-endian ordered</b> byte array
   * (the opposite of what {@link InetAddress#getByAddress} expects).
   *
   * <p>IPv4 address byte array must be 4 bytes long and IPv6 byte array
   * must be 16 bytes long.
   *
   * @param addr the raw IP address in little-endian byte order
   * @return an InetAddress object created from the raw IP address
   * @throws UnknownHostException if IP address is of illegal length
   */
  public static InetAddress fromLittleEndianByteArray(byte[] addr)
      throws UnknownHostException {
    byte[] reversed = new byte[addr.length];
    for (int i = 0; i < addr.length; i++) {
      reversed[i] = addr[addr.length - i - 1];
    }
    return InetAddress.getByAddress(reversed);
  }

  /**
   * This method emulates the Java 6 method
   * {@code Arrays.copyOfRange(byte, int, int)}, which is not available in
   * Java 5, and thus cannot be used in Guava code.
   */
  private static byte[] copyOfRange(byte[] original, int from, int to) {
    Preconditions.checkNotNull(original);

    int end = Math.min(to, original.length);
    byte[] result = new byte[to - from];

    System.arraycopy(original, from, result, 0, end - from);
    return result;
  }
}
