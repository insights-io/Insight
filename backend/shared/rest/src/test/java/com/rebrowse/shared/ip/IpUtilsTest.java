package com.rebrowse.shared.ip;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;

public class IpUtilsTest {

  @Test
  public void test_is_local_address() throws UnknownHostException {
    assertTrue(IpUtils.isLocalAddress("127.0.0.1"));
    assertTrue(IpUtils.isLocalAddress("0:0:0:0:0:0:0:1"));
    assertTrue(IpUtils.isLocalAddress("172.25.0.1"));

    assertFalse(IpUtils.isLocalAddress("40.84.27.125"));
    assertFalse(IpUtils.isLocalAddress("104.46.4.9"));
  }
}
