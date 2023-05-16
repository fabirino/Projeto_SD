package org.Googol;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.Googol.Stats;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;



public class WebSocket implements WebSocketMessageBrokerConfigurer{

    public boolean sendMessage(String[] searches) {
        try {
            // Create a list of transports and add a WebSocket transport
            List<Transport> transports = new ArrayList<>();
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
    
            // Create a SockJsClient with the transports
            SockJsClient sockJsClient = new SockJsClient(transports);
    
            // Create a WebSocketStompClient with the SockJsClient
            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
    
            // Configure the message converter to use JSON
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    
            // Set a task scheduler for concurrent processing
            stompClient.setTaskScheduler(new ConcurrentTaskScheduler());
    
            // Create a StompSessionHandler to handle session events
            StompSessionHandler sessionHandler = new MyStompSessionHandler();
    
            // Connect to the WebSocket endpoint and get a StompSession
            StompSession stompSession = stompClient.connect("ws://localhost:8080/stats-websocket", sessionHandler).get();
    
            // Send a message to the server's "/app/update-stats" destination
            stompSession.send("/app/update-stats", new Stats(searches));
    
            // Disconnect the StompSession
            stompSession.disconnect();
    
            return true;
        } catch (InterruptedException | ExecutionException | IllegalStateException | MessageDeliveryException e) {
            return false;
        }
    }
    

    static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {

            System.out.println("Connected");

            session.subscribe("/stats/messages", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    System.out.println("Received message: " + payload);
                }
            });
        }
    }
}

