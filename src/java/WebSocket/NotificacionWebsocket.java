package WebSocket;

import model.Notificacion;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import java.util.List;
import servicio.ServicioNotificacion;

/**
 *
 * @author Mauricio
 */
@ServerEndpoint("/notificationSocket/{userId}")
public class NotificacionWebsocket {

    // Store active WebSocket sessions for each user
    private static Map<String, Session> userSessions = new ConcurrentHashMap<>();
    private Gson gson = new Gson();
    private ServicioNotificacion servicioNotificacion = new ServicioNotificacion();

    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session) {
        // Store the session when a new WebSocket connection is opened
        userSessions.put(userId, session);
        System.out.println("Notification WebSocket opened for user: " + userId);

        // Retrieve and send any unread notifications when user connects
        try {
            Integer userIdInt = Integer.parseInt(userId);
            List<Notificacion> unreadNotifications = servicioNotificacion.obtenerNotificacionesNoLeidas(userIdInt);

            for (Notificacion notification : unreadNotifications) {
                sendNotificationToUser(userId, notification);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid user ID format: " + userId);
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        // Remove the session when WebSocket connection is closed
        userSessions.remove(userId);
        System.out.println("Notification WebSocket closed for user: " + userId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        try {
            Map<String, Object> messageMap = gson.fromJson(message, Map.class);
            String messageType = (String) messageMap.get("type");

            if ("markAsRead".equals(messageType)) {
                Integer notificationId = ((Number) messageMap.get("notificationId")).intValue();
                markNotificationAsRead(notificationId);
            } else if ("markAllAsRead".equals(messageType)) {
                markAllNotificationsAsRead(Integer.parseInt(userId));
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + message);
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Notification WebSocket error occurred");
        throwable.printStackTrace();
    }

    // Method to send notification to a specific user
    public static void sendNotificationToUser(String userId, Notificacion notificacion) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> notificationMessage = new HashMap<>();
                notificationMessage.put("type", "notification");
                notificationMessage.put("idNotificacion", notificacion.getIdNotificacion());
                notificationMessage.put("mensaje", notificacion.getMensaje());
                notificationMessage.put("tipoNotificacion", notificacion.getTipoNotificacion());
                notificationMessage.put("subastaId", notificacion.getSubastaId());
                notificationMessage.put("montoPuja", notificacion.getMontoPuja());

                    notificationMessage.put("subastaNombre", notificacion.getSubastaNombre());

                session.getBasicRemote().sendText(new Gson().toJson(notificationMessage));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to mark a specific notification as read
    private void markNotificationAsRead(Integer notificationId) {
        try {
            Notificacion notification = servicioNotificacion.leerNotificacion(notificationId);
            if (notification != null) {
                notification.setLeida(true);
                servicioNotificacion.actualizarNotificacion(notification);
            }
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + notificationId);
            e.printStackTrace();
        }
    }

    private void markAllNotificationsAsRead(Integer userId) {
        List<Notificacion> notifications = servicioNotificacion.obtenerNotificacionesNoLeidas(userId);
        for (Notificacion notificacion : notifications) {
            notificacion.setLeida(true);
            servicioNotificacion.actualizarNotificacion(notificacion);
        }
    }

    // Method to broadcast notifications to multiple users if needed
    public static void broadcastNotification(List<String> userIds, Notificacion notificacion) {
        for (String userId : userIds) {
            sendNotificationToUser(userId, notificacion);
        }
    }
}
