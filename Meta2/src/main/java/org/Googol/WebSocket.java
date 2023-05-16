package org.Googol;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.Googol.forms.Stats;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;


public class WebSocket {

    static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        
        /**
         * Called after the connection is established. The session can be used to send messages to the server.
         * @param session the established session
         * @param connectedHeaders the headers received from the server after the connection is established                    
         */
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {

            session.subscribe("/stats/update", new StompFrameHandler() {
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

    /**
     * Sends a message to the server's "/app/stats-update" destination
     * @param searches the message to be sent
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendMessage(Stats searches) {
        try {
            // Create a list (WebSocket)
            List<Transport> transports = new ArrayList<>();
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));

            // Create a SockJsClient
            SockJsClient sockJsClient = new SockJsClient(transports);

            // Create a WebSocketStompClient with the SockJsClient
            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            stompClient.setTaskScheduler(new ConcurrentTaskScheduler());

            StompSessionHandler sessionHandler = new MyStompSessionHandler();

            // Connect to the server's WebSocket
            StompSession stompSession = stompClient.connect("ws://localhost:8080//stats-register", sessionHandler).get();

            stompSession.send("/app/stats-update", searches);

            stompSession.disconnect();

            return true;

        } catch (InterruptedException | ExecutionException | IllegalStateException | MessageDeliveryException e) {
            return false;
        }
    }
}

