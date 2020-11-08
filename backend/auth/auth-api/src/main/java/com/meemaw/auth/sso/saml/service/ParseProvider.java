package com.meemaw.auth.sso.saml.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.meemaw.auth.sso.saml.model.metadata.xml.EntityDescriptor;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.w3c.dom.Document;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@ApplicationScoped
@Slf4j
public class ParseProvider {

  private XmlMapper xmlMapper;
  private CertificateFactory certificateFactory;
  private BasicParserPool pool;

  public void init(@Observes StartupEvent event) {
    xmlMapper = new XmlMapper();
    try {
      InitializationService.initialize();
      certificateFactory = CertificateFactory.getInstance("X.509");
      pool = new BasicParserPool();
      pool.initialize();
    } catch (InitializationException | CertificateException | ComponentInitializationException ex) {
      log.error("Failed to initialize", ex);
    }
  }

  public Document parse(InputStream is) throws XMLParserException {
    return pool.parse(is);
  }

  public EntityDescriptor readEntityDescriptor(InputStream is) throws IOException {
    return xmlMapper.readValue(is, EntityDescriptor.class);
  }

  public Credential certificate(String body) throws CertificateException {
    String certificate = "-----BEGIN CERTIFICATE-----\n" + body + "-----END CERTIFICATE-----";
    return new BasicX509Credential(
        (X509Certificate)
            certificateFactory.generateCertificate(
                new ByteArrayInputStream(certificate.getBytes())));
  }
}
