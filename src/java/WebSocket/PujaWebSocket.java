package WebSocket;

import Controller.NotificacionController;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import model.Puja;
import model.Subasta;
import model.Usuario;
import servicio.ServicioPuja;
import servicio.ServicioSubasta;
import servicio.ServicioUsuario;

/**
 *
 * @author Mauricio
 */
@ServerEndpoint("/bidSocket")
public class PujaWebSocket {
    private static final Set<Session> activeSessions = 
        Collections.synchronizedSet(new HashSet<>());
    
    @OnOpen
    public void onOpen(Session session) {
        activeSessions.add(session);
        System.out.println("Sesión abierta para WebSocket de pujas.");
    }
    
    @OnClose
    public void onClose(Session session) {
        activeSessions.remove(session);
        System.out.println("Sesión cerrada para WebSocket de pujas.");
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            
            // Validar tipo de mensaje
            if (!"placeBid".equals(jsonMessage.getString("type"))) {
                return;
            }
            
            // Extraer detalles de la puja
            int subastaId = jsonMessage.getInt("subastaId");
            int usuarioId = jsonMessage.getInt("usuarioId");
            double monto = jsonMessage.getDouble("monto");
            
            // Obtener subasta y usuario
            ServicioSubasta servicioSubasta = new ServicioSubasta();
            Subasta subasta = servicioSubasta.leerSubasta(subastaId);
            
            ServicioUsuario servicioUsuario = new ServicioUsuario();
            Usuario usuario = servicioUsuario.leerUsuario(usuarioId);
            
            if (subasta == null || usuario == null) {
                sendErrorMessage(session, "Subasta o usuario no válido");
                return;
            }
            
            // Validar si la puja es válida
            if (!isValidBid(subasta, monto)) {
                sendErrorMessage(session, "El monto de la puja no es válido");
                return;
            }

            // Obtener al usuario que tenía la puja más alta antes de la nueva puja
            Usuario usuarioAnterior = getUsuarioConPujaMasAlta(subasta);
            
            // Crear y guardar la nueva puja
            Puja puja = new Puja();
            puja.setSubasta(subasta);
            puja.setUsuario(usuario);
            puja.setMonto(monto);
            
            ServicioPuja servicioPuja = new ServicioPuja();
            servicioPuja.crearPuja(puja);

            // 🔥 Notificar al usuario superado (solo si no es el mismo usuario)
            if (usuarioAnterior != null && !Objects.equals(usuarioAnterior.getIdUsuario(), usuario.getIdUsuario())) {
                System.out.println("Usuario superado: " + usuarioAnterior.getIdUsuario());
                
                // Usar NotificacionController para enviar la notificación
                NotificacionController notificacionController = new NotificacionController();
                notificacionController.processBidNotification(subasta, usuarioAnterior, monto);
            }
            
            // 🔥 Broadcast de la nueva puja a todos los usuarios
            broadcastBid(puja);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private boolean isValidBid(Subasta subasta, double monto) {
        if (!"Activo".equals(subasta.getEstadoSubasta())) {
            return false;
        }
        
        ServicioPuja servicioPuja = new ServicioPuja();
        List<Puja> existingBids = servicioPuja.listaPujasPorSubasta(subasta.getIdSubasta());
        
        double highestBid = existingBids.isEmpty() ? 
            subasta.getPrecioInicial() : 
            existingBids.stream()
                .mapToDouble(Puja::getMonto)
                .max()
                .orElse(subasta.getPrecioInicial());
        
        return monto > highestBid;
    }
    
    private Usuario getUsuarioConPujaMasAlta(Subasta subasta) {
        ServicioPuja servicioPuja = new ServicioPuja();
        List<Puja> existingBids = servicioPuja.listaPujasPorSubasta(subasta.getIdSubasta());
        
        if (existingBids.isEmpty()) {
            return null;
        }
        
        Puja highestBid = existingBids.stream()
            .max((p1, p2) -> Double.compare(p1.getMonto(), p2.getMonto()))
            .orElse(null);
        
        return highestBid != null ? highestBid.getUsuario() : null;
    }
    
    private void sendErrorMessage(Session session, String errorMessage) {
        try {
            JSONObject errorJson = new JSONObject();
            errorJson.put("type", "bidError");
            errorJson.put("message", errorMessage);
            session.getBasicRemote().sendText(errorJson.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    
    public static void broadcastBid(Puja puja) {
        JSONObject bidMessage = new JSONObject();
        try {
            bidMessage.put("type", "newBid");
            bidMessage.put("subastaId", puja.getSubasta().getIdSubasta());
            bidMessage.put("monto", puja.getMonto());
            bidMessage.put("usuarioId", puja.getUsuario().getIdUsuario());
            bidMessage.put("fechaPuja", puja.getFechaPuja().toString());
            
            synchronized (activeSessions) {
                for (Session session : activeSessions) {
                    try {
                        session.getBasicRemote().sendText(bidMessage.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
