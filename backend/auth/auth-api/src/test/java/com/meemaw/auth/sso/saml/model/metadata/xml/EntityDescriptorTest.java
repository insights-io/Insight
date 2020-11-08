package com.meemaw.auth.sso.saml.model.metadata.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityDescriptorTest {

  private static final XmlMapper xmlMapper = new XmlMapper();

  @Test
  public void xml_parser__should_correctly_parse__when_okta_metadata()
      throws URISyntaxException, IOException {
    InputStream is =
        Files.newInputStream(
            Path.of(getClass().getResource("/sso/saml/metadata/okta_metadata.xml").toURI()));

    EntityDescriptor entityDescriptor = xmlMapper.readValue(is, EntityDescriptor.class);

    assertEquals("http://www.okta.com/exkligrqDovHJsGmk5d5", entityDescriptor.getEntityId());

    assertEquals(
        "MIIDqDCCApCgAwIBAgIGAXWjU495MA0GCSqGSIb3DQEBCwUAMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECAwKQ2FsaWZvcm5pYTEWMBQGA1UEBwwNU2FuIEZyYW5jaXNjbzENMAsGA1UECgwET2t0YTEUMBIGA1UECwwLU1NPUHJvdmlkZXIxFTATBgNVBAMMDHNudWRlcmxzdGVzdDEcMBoGCSqGSIb3DQEJARYNaW5mb0Bva3RhLmNvbTAeFw0yMDExMDcxNTI4MjNaFw0zMDExMDcxNTI5MjNaMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECAwKQ2FsaWZvcm5pYTEWMBQGA1UEBwwNU2FuIEZyYW5jaXNjbzENMAsGA1UECgwET2t0YTEUMBIGA1UECwwLU1NPUHJvdmlkZXIxFTATBgNVBAMMDHNudWRlcmxzdGVzdDEcMBoGCSqGSIb3DQEJARYNaW5mb0Bva3RhLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKS1RkuPEsj0/kp+xCVrS9a5vR+RNNnbHClREfI+MQ6x70TbnZ8G+i9gY4RQBaxk5p/4sThIhM5x1xkVv5HtCfIbajwZBzenfAg7oBcIDx10Cvs6hpypIHUo9aNT4ousbvnxKIvZzUeg3/EhQNAVFoI2JGQsv4fE2QNNffvJIbuTDAnLo0ZukiExSincgJJqqUz8cgrdQN+VqFfM28LrMrEohyig5NMVXmqGQLVK6+cfc3D69Fr8GfieTHxLwstPYeTzSEl872uDoodXCwCxYyr3MubvT7LrHQTKiYsJH0jALf36KKeWEa3Hn7DOaZ28j9+YhTxD26suaoOpZExa20MCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAHej3zfzWTmtqZBDg+/7N8Xt9WxHxRQTig+b9Iy/o4APHU5Yl+SgeqXgEFu6HW4rT6JYO7w83P/eQGbL2yTcpJkpZdxjz0zHAzxuXXeSq9mTwNLPyxdOoa7GZlQK0O7eCfqz3c8dlsIzHoJqQNSzBqzohLhjNIGkZYzzHZar5fLAQZ8gkZ4fPHSe8Z91sYXh78mo9KsNXN6eOvnqFP36yRD+DUQhVD3zj93AOjiwC+h7XTxfHif6f9Qz6m5tUAqlPpoC5dEKho6ARJiCCXpf65EsRMQaAWnMA0vZxy33WujGZ89bPo2RfFoUfx7c29virjlVa+er5p5PNHfOCmrY2Ow==",
        entityDescriptor.getCertificate());

    assertEquals(
        "https://snuderlstest.okta.com/app/snuderlsorg2948061_rebrowse_2/exkligrqDovHJsGmk5d5/sso/saml",
        entityDescriptor.getHttpRedirectSignOnService().get().getLocation());

    assertEquals(
        "https://snuderlstest.okta.com/app/snuderlsorg2948061_rebrowse_2/exkligrqDovHJsGmk5d5/sso/saml",
        entityDescriptor.getHttpPostSignOnService().get().getLocation());
  }

  @Test
  public void xml_parser__should_correctly_parse__when_auth0_metadata()
      throws URISyntaxException, IOException {
    InputStream is =
        Files.newInputStream(
            Path.of(getClass().getResource("/sso/saml/metadata/auth0_metadata.xml").toURI()));

    EntityDescriptor entityDescriptor = xmlMapper.readValue(is, EntityDescriptor.class);

    assertEquals("urn:dev-p4ltuwq1.us.auth0.com", entityDescriptor.getEntityId());

    assertEquals(
        "MIIDDTCCAfWgAwIBAgIJSPujeY0XUQajMA0GCSqGSIb3DQEBCwUAMCQxIjAgBgNVBAMTGWRldi1wNGx0dXdxMS51cy5hdXRoMC5jb20wHhcNMjAxMTA3MDkyNzIxWhcNMzQwNzE3MDkyNzIxWjAkMSIwIAYDVQQDExlkZXYtcDRsdHV3cTEudXMuYXV0aDAuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxzmF5nto1VrSFj1U3mx3NU4P2veKY48gCNYMpLScTsrHfn4nbT6i1ewp/DG/QZmU85HHEWEdV6igoqcNQ5sD7hZ2ZGsdwQ17z9LvfmyYC+w7M1g3adclUA9ulnkVN0QZbxpKm1ei8ymkHEC5x2Ebf3BOJosM30rBjTq/ybUTNdzowcEg/xWn02ZbPHVaGtF7r+vb7Mw4cfG8D/OEE0bzslmQkqdyodXYfTv7EFpDdz9zsKDk7veIJuIR6v8Yv4mBSx+YQDqf/dTcQ35+2S1FGfGpnHiMzs4ob4ObhcJIb5Qjawr0PaF2srbqwUR/nKkDOa/ORiHvv4RZ+YIZQ8SCkQIDAQABo0IwQDAPBgNVHRMBAf8EBTADAQH/MB0GA1UdDgQWBBRoXV5PqbGRTpWK5WTS7gVnCJTJLjAOBgNVHQ8BAf8EBAMCAoQwDQYJKoZIhvcNAQELBQADggEBACeXSzQdA1Qu2EKI7pAjw3IDVE0rQbzvtOmYQE+24pQ6Yrmd7Or9//o5vEZILWpNJ6Ap5aeDWFDWLlQUZAy1nqVpJ/sPH62tIAF2D7O2BDWQhqk0lnIHhm/sS1beY5RBBliN+PV9nBIU5jpytOtnGizQ2ELvRKHdSIH+b4dFWJ9zBf5DZCyP8Kxsr+X4XNlxRbWzEmT+pNi6fmyjxsYE7OAS12VSHgnxNEgcYLJtzGYWvaclHrkv1PnoGXjuadW9JfARXv11skii7AEKh9sl2tROEXlfz/Y8hAo09cCnOQwVExJknuKiYPMMtLeuIGFDLft0vlQaqbExfAs1NHO2oTw=",
        entityDescriptor.getCertificate());

    assertEquals(
        "https://dev-p4ltuwq1.us.auth0.com/samlp/QCt7fH1cNLx4z4eJGwFQ6110nwkBbYMM/logout",
        entityDescriptor.getHttpPostLogoutService().get().getLocation());

    assertEquals(
        "https://dev-p4ltuwq1.us.auth0.com/samlp/QCt7fH1cNLx4z4eJGwFQ6110nwkBbYMM/logout",
        entityDescriptor.getHttpRedirectLogoutService().get().getLocation());

    assertEquals(
        "https://dev-p4ltuwq1.us.auth0.com/samlp/QCt7fH1cNLx4z4eJGwFQ6110nwkBbYMM",
        entityDescriptor.getHttpRedirectSignOnService().get().getLocation());

    assertEquals(
        "https://dev-p4ltuwq1.us.auth0.com/samlp/QCt7fH1cNLx4z4eJGwFQ6110nwkBbYMM",
        entityDescriptor.getHttpPostSignOnService().get().getLocation());

    assertEquals(
        "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
        entityDescriptor.getIdpSsoDescriptor().getAttributes().get(0).getName());
  }

  @Test
  public void xml_parser__should_correctly_parse__when_onelogin_metadata()
      throws URISyntaxException, IOException {
    InputStream is =
        Files.newInputStream(
            Path.of(getClass().getResource("/sso/saml/metadata/onelogin_metadata.xml").toURI()));

    EntityDescriptor entityDescriptor = xmlMapper.readValue(is, EntityDescriptor.class);

    assertEquals(
        "https://app.onelogin.com/saml/metadata/4fbf4cdb-fca6-4492-94aa-06bd7a46f0ef",
        entityDescriptor.getEntityId());

    assertEquals(
        "MIID3zCCAsegAwIBAgIUdZhCyE1LTMLSwAOFeJJXVAyYUegwDQYJKoZIhvcNAQEFBQAwRjERMA8GA1UECgwIUmVicm93c2UxFTATBgNVBAsMDE9uZUxvZ2luIElkUDEaMBgGA1UEAwwRT25lTG9naW4gQWNjb3VudCAwHhcNMjAxMTA3MDkzMTM4WhcNMjUxMTA3MDkzMTM4WjBGMREwDwYDVQQKDAhSZWJyb3dzZTEVMBMGA1UECwwMT25lTG9naW4gSWRQMRowGAYDVQQDDBFPbmVMb2dpbiBBY2NvdW50IDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKV4H0XCpvkBOC0ypZGnf/iUnbEHbSUSAqZAWnZZVvuwJnl4QbNT6NyQ8sSyHH2JeFiRyn9kkqNn/HH8l5uRuN4ZYKpL9Cm8g4zP6Bp4fEe8EOlb9NR+UCRvWXiEMU1nZSxwp4oFseFMjYtOzMdyIS3XJVLFzEd43ZrQ8j5r3qPoANUuXEp7/tS98j89c5l8mbVdWgigjocYzaXduJk2b0OaZhW8miyi1+sIPiElOKzSmisa5DPb9bO22Tbd1d2b0bc16J9vLibs4JqYN7cwUhV1vNtShHHws7ZCiQKNk60RGXuwsMkpPt12ATCVAqtnILc4k13AI78c/NwLK8IXabMCAwEAAaOBxDCBwTAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBTgMK3nNxrJJRP4BIswJASAN0ylZjCBgQYDVR0jBHoweIAU4DCt5zcaySUT+ASLMCQEgDdMpWahSqRIMEYxETAPBgNVBAoMCFJlYnJvd3NlMRUwEwYDVQQLDAxPbmVMb2dpbiBJZFAxGjAYBgNVBAMMEU9uZUxvZ2luIEFjY291bnQgghR1mELITUtMwtLAA4V4kldUDJhR6DAOBgNVHQ8BAf8EBAMCB4AwDQYJKoZIhvcNAQEFBQADggEBADIjAy8mAG4wd6refCY0banPEobdDH5yISA5FU+2j4AtgPb5zEv1TOWOd8jV6gzYcqHJnrDhrSVvqdgkG5wF86mDApciI71v35JaLC1efOyYg0GiAl0IE9Di60eI942k7+fKC/zib/bLZJ8KZRCv9R2vGL7GdwtngJDkCx1hpmFmfiJW0WjWgy0iXmi2IYVh9WlzpU4Huo4wlEHMk2w8pkq+QT7vBgSHY0DfHFHjXcg7vDMznayEvxQewLHK3UT+DNWlfIvVtMHvCiyOLXQtYuW2la0LDcLpBk4BkifwRk25tVrsI7yhb0F8dVlBMU+S58shGaE/BXnS2idZVrt0TOY=",
        entityDescriptor.getCertificate());

    assertEquals(
        "https://rebrowse.onelogin.com/trust/saml2/http-redirect/slo/1298785",
        entityDescriptor.getHttpRedirectLogoutService().get().getLocation());

    assertEquals(
        "https://rebrowse.onelogin.com/trust/saml2/http-redirect/sso/4fbf4cdb-fca6-4492-94aa-06bd7a46f0ef",
        entityDescriptor.getHttpRedirectSignOnService().get().getLocation());

    assertEquals(
        "https://rebrowse.onelogin.com/trust/saml2/http-post/sso/4fbf4cdb-fca6-4492-94aa-06bd7a46f0ef",
        entityDescriptor.getHttpPostSignOnService().get().getLocation());
  }
}
