package com.rebrowse.shared.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

public final class IpUtils {

  private IpUtils() {}

  public static boolean isLocalAddress(String ip) throws UnknownHostException {
    return Stream.of(InetAddress.getAllByName(ip))
        .allMatch(address -> address.isSiteLocalAddress() || address.isLoopbackAddress());
  }
}
