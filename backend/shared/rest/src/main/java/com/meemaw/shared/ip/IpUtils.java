package com.meemaw.shared.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class IpUtils {

  private IpUtils() {}

  public static boolean isLocalAddress(String ip) throws UnknownHostException {
    InetAddress address = InetAddress.getAllByName(ip)[0];
    return address.isSiteLocalAddress() || address.isLoopbackAddress();
  }
}
