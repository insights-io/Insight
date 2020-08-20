package com.meemaw.shared.ip;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;

public class IpUtilsTest {

  @Test
  public void test_is_local_address() throws UnknownHostException {
    assertTrue(IpUtils.isLocalAddress("127.0.0.1"));
    assertTrue(IpUtils.isLocalAddress("0:0:0:0:0:0:0:1"));
    assertTrue(IpUtils.isLocalAddress("172.25.0.1"));
  }
}
