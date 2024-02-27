package com.mdx.anarchistgame.config;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class MyWebSocketHandler implements WebSocketHandler {

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // This method is called when a WebSocket connection is established
  }

  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    // This method is called when a WebSocket message is received
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    // This method is called when a WebSocket transport error occurs
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
    // This method is called when a WebSocket connection is closed
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }
}
