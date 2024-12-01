package WebSocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import model.Usuario;

/**
 *
 * @author Mauricio
 */
@ServerEndpoint("/userConnection")
public class UserConnectionSocket {

    private static final Set<Session> activeSessions = Collections.synchronizedSet(new HashSet<>());
    private static final Set<Integer> connectedUserIds = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        Usuario usuario = (Usuario) session.getUserProperties().get("usuario");
        if (usuario != null) {
            activeSessions.add(session);
            connectedUserIds.add(usuario.getIdUsuario());
            broadcastUserConnection(usuario, true);
        }
    }

    @OnClose
    public void onClose(Session session) {
        Usuario usuario = (Usuario) session.getUserProperties().get("usuario");
        if (usuario != null) {
            activeSessions.remove(session);
            connectedUserIds.remove(usuario.getIdUsuario());
            broadcastUserConnection(usuario, false);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String type = jsonMessage.getString("type");
            
            switch (type) {
                case "connect":
                    // Handle connection message if needed
                    break;
                // Add more message type handlers
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void broadcastUserConnection(Usuario usuario, boolean connected) {
        JSONObject message = new JSONObject();
        try {
            message.put("type", "userConnection");
            message.put("userId", usuario.getIdUsuario());
            message.put("connected", connected);
            
            synchronized (activeSessions) {
                for (Session session : activeSessions) {
                    try {
                        session.getBasicRemote().sendText(message.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Method to get currently connected users
    public static Set<Integer> getConnectedUserIds() {
        return new HashSet<>(connectedUserIds);
    }

}
