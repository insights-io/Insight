package com.rebrowse.session.sessions.ws.v1;

import com.rebrowse.session.sessions.service.SessionSocketService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

@ServerEndpoint(SessionSocketImpl.PATH)
@ApplicationScoped
@Slf4j
public class SessionSocketImpl {

  public static final String PATH = "/v1/sessions";

  @Inject
  SessionSocketService sessionSocketService;

  @OnOpen
  public void onOpen(Session session) {
    sessionSocketService.onOpen(session);
  }

  @OnClose
  public void onClose(Session session) {
    sessionSocketService.onClose(session);
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    sessionSocketService.onError(session, throwable);
  }
}
