package com.rebrowse.session.ws.v1;

import com.rebrowse.session.sessions.ws.v1.SessionSocketImpl;
import io.quarkus.test.common.http.TestHTTPResource;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.LinkedBlockingDeque;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

public class AbstractSessionSocketTest {

  @TestHTTPResource(SessionSocketImpl.PATH)
  URI uri;

  protected static final LinkedBlockingDeque<String> MESSAGES = new LinkedBlockingDeque<>();

  protected Session connect() throws IOException, DeploymentException {
    return ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri);
  }

  @ClientEndpoint
  protected static class Client {

    @OnOpen
    public void open(Session session) {
      MESSAGES.add(String.format("OPEN %s", session.getId()));
    }

    @OnClose
    public void onClose(Session session) {
      MESSAGES.add(String.format("CLOSE %s", session.getId()));
    }

    @OnMessage
    void message(String msg) {
      MESSAGES.add(msg);
    }
  }
}
