package com.meemaw.session.useragent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.test.rest.data.UserAgentData;
import com.meemaw.useragent.model.DeviceClass;
import com.meemaw.useragent.model.UserAgent;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class UserAgentServiceTest {

  @Inject UserAgentService userAgentService;

  /* E Readers User Agents */
  @Test
  public void user_agent_service__should_correctly_pause_ua__when_amazon_kindle_4() {
    assertEquals(
        new UserAgent(
            "Linux Desktop", "Unknown", DeviceClass.DESKTOP, "Linux", "ARMv7l", "Kindle", "3.0"),
        userAgentService.parse(
            "Mozilla/5.0 (X11; U; Linux armv7l like Android; en-us) AppleWebKit/531.2+ (KHTML, like Gecko) Version/5.0 Safari/533.2+ Kindle/3.0+"));
  }

  /* Bots and Crawlers User Agents */
  @Test
  public void user_agent_service__should_correctly_pause_ua__when_google_bot() {
    assertEquals(
        new UserAgent(
            "Google", "Google", DeviceClass.ROBOT, "Google", "Google", "Googlebot", "2.1"),
        userAgentService.parse(
            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"));
  }

  /* Game Consoles User Agents */
  @Test
  public void user_agent_service__should_correctly_pause_ua__when_nintendo_wii() {
    assertEquals(
        new UserAgent(
            "Nintendo Wiiu",
            "Nintendo",
            DeviceClass.GAME_CONSOLE,
            "Nintendo NX",
            "3.0.4.2.12",
            "NintendoBrowser",
            "4.3.1.11264.US"),
        userAgentService.parse(
            "Mozilla/5.0 (Nintendo WiiU) AppleWebKit/536.30 (KHTML, like Gecko) NX/3.0.4.2.12 NintendoBrowser/4.3.1.11264.US"));
  }

  /* Set Top Boxes User Agents */
  @Test
  public void user_agent_service__should_correctly_pause_ua__when_chromecast() {
    assertEquals(
        new UserAgent(
            "Google Chromecast",
            "Google",
            DeviceClass.SET_TOP_BOX,
            "Unknown",
            "??",
            "Chrome",
            "31.0.1650.0"),
        userAgentService.parse(
            "Mozilla/5.0 (CrKey armv7l 1.5.16041) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.0 Safari/537.36"));
  }

  @Test
  public void user_agent_service__should_correctly_pause_ua__when_amazon_4k_fire_tv() {
    assertEquals(
        new UserAgent(
            "Afts",
            "Unknown",
            DeviceClass.TABLET,
            "Android",
            "5.1",
            "Chrome Webview",
            "41.99900.2250.0242"),
        userAgentService.parse(UserAgentData.AMAZON_4K_FIRE_TV));
  }

  @Test
  public void user_agent_service__should_correctly_pause_ua__when_apple_tv_4th_gen() {
    assertEquals(
        new UserAgent(
            "Unknown", "Unknown", DeviceClass.UNKNOWN, "Unknown", "??", "AppleTV5,3", "9.1.1"),
        userAgentService.parse(UserAgentData.APPLE_TV));
  }

  /* Desktop User Agents */
  @Test
  public void user_agent_service__should_correctly_pause_ua__when_linux_firefox() {
    assertEquals(
        new UserAgent(
            "Linux Desktop", "Unknown", DeviceClass.DESKTOP, "Ubuntu", "??", "Firefox", "15.0.1"),
        userAgentService.parse(UserAgentData.LINUX__FIREFOX));
  }

  @Test
  public void user_agent_service__should_correctly_pause_ua__when_macosx_safari() {
    assertEquals(
        new UserAgent(
            "Apple Macintosh",
            "Apple",
            DeviceClass.DESKTOP,
            "Mac OS X",
            "10.11.2",
            "Safari",
            "9.0.2"),
        userAgentService.parse(UserAgentData.MAC__SAFARI));
  }

  @Test
  public void user_agent_service__should_correctly_pause_ua__when_windows_10_edge() {
    assertEquals(
        new UserAgent(
            "Desktop", "Unknown", DeviceClass.DESKTOP, "Windows NT", "10.0", "Edge", "20.??"),
        userAgentService.parse(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246"));
  }

  /* Tablet */
  @Test
  public void user_agent_service__should_correctly_pause_ua__when_google_pixel_c() {
    assertEquals(
        new UserAgent(
            "Google Pixel C",
            "Google",
            DeviceClass.TABLET,
            "Android",
            "7.0",
            "Chrome Webview",
            "52.0.2743.98"),
        userAgentService.parse(
            "Mozilla/5.0 (Linux; Android 7.0; Pixel C Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.98 Safari/537.36"));
  }

  /* MS Windows Phone User Agents */
  @Test
  public void user_agent_service__should_correctly_pause_ua__when_microsoft_lumia_650() {
    assertEquals(
        new UserAgent(
            "Microsoft RM-1152",
            "Microsoft",
            DeviceClass.PHONE,
            "Windows Phone",
            "10.0",
            "Edge",
            "40.??"),
        userAgentService.parse(
            "Mozilla/5.0 (Windows Phone 10.0; Android 6.0.1; Microsoft; RM-1152) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Mobile Safari/537.36 Edge/15.15254"));
  }

  /* iPhone User Agents */
  @Test
  public void user_agent_service__should_correctly_pause_ua__when_iphone_xr_safari() {
    assertEquals(
        new UserAgent("Apple iPhone", "Apple", DeviceClass.PHONE, "iOS", "12.0", "Safari", "12.0"),
        userAgentService.parse(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1"));
  }

  @Test
  public void user_agent_service__should_correctly_pause_ua__when_iphone_xs_chrome() {
    assertEquals(
        new UserAgent(
            "Apple iPhone", "Apple", DeviceClass.PHONE, "iOS", "12.0", "Chrome", "69.0.3497.105"),
        userAgentService.parse(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/69.0.3497.105 Mobile/15E148 Safari/605.1"));
  }

  @Test
  public void user_agent_service__should_correctly_pause_ua__when_iphone_7_plus() {
    assertEquals(
        new UserAgent(
            "Apple iPhone", "Apple", DeviceClass.PHONE, "iOS", "10.0.1", "Safari", "10.0"),
        userAgentService.parse(
            "Mozilla/5.0 (iPhone9,4; U; CPU iPhone OS 10_0_1 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) Version/10.0 Mobile/14A403 Safari/602.1"));
  }

  /* Android Mobile User Agents */
  @Test
  public void user_agent_service__should_correctly_pause_ua__when_htc_one_x10() {
    assertEquals(
        new UserAgent(
            "HTC ONE X10",
            "HTC",
            DeviceClass.PHONE,
            "Android",
            "6.0",
            "Chrome Webview",
            "61.0.3163.98"),
        userAgentService.parse(
            "Mozilla/5.0 (Linux; Android 6.0; HTC One X10 Build/MRA58K; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.98 Mobile Safari/537.36"));
  }

  @Test
  public void user_agent_service__should_correctly_pause_ua__when_sony_xperia_xz() {
    assertEquals(
        new UserAgent(
            "Sony G8231",
            "Sony",
            DeviceClass.PHONE,
            "Android",
            "7.1.1",
            "Chrome Webview",
            "59.0.3071.125"),
        userAgentService.parse(
            "Mozilla/5.0 (Linux; Android 7.1.1; G8231 Build/41.2.A.0.219; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/59.0.3071.125 Mobile Safari/537.36"));
  }

  @Test
  public void user_agent_service__should_correctly_pause_ua__when_nexus_6p() {
    assertEquals(
        new UserAgent(
            "Google Nexus 6P",
            "Google",
            DeviceClass.PHONE,
            "Android",
            "6.0.1",
            "Chrome",
            "47.0.2526.83"),
        userAgentService.parse(
            "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.83 Mobile Safari/537.36"));
  }

  @Test
  public void user_agent_service__should_correctly_pause_ua__when_samsung_galaxy_s9() {
    assertEquals(
        new UserAgent(
            "Samsung SM-G960F",
            "Samsung",
            DeviceClass.PHONE,
            "Android",
            "8.0.0",
            "Chrome",
            "62.0.3202.84"),
        userAgentService.parse(
            "Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36"));
  }

  @Test
  public void user_agent_service__should_correctly_pause_ua__when_samsung_galaxy_s8() {
    assertEquals(
        new UserAgent(
            "Samsung SM-G892A",
            "Samsung",
            DeviceClass.PHONE,
            "Android",
            "7.0",
            "Chrome Webview",
            "60.0.3112.107"),
        userAgentService.parse(
            "Mozilla/5.0 (Linux; Android 7.0; SM-G892A Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/60.0.3112.107 Mobile Safari/537.36"));
  }
}
