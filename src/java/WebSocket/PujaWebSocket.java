package WebSocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    }
    
    @OnClose
    public void onClose(Session session) {
        activeSessions.remove(session);
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            
            // Validate message type
            if (!"placeBid".equals(jsonMessage.getString("type"))) {
                return;
            }
            
            // Extract bid details
            int subastaId = jsonMessage.getInt("subastaId");
            int usuarioId = jsonMessage.getInt("usuarioId");
            double monto = jsonMessage.getDouble("monto");
            
            // Validate bid
            ServicioSubasta servicioSubasta = new ServicioSubasta();
            Subasta subasta = servicioSubasta.leerSubasta(subastaId);
            
            ServicioUsuario servicioUsuario = new ServicioUsuario();
            Usuario usuario = servicioUsuario.leerUsuario(usuarioId);
            
            // Validate auction and user exist
            if (subasta == null || usuario == null) {
                sendErrorMessage(session, "Invalid auction or user");
                return;
            }
            
            // Check auction status and bid validity
            if (!isValidBid(subasta, monto)) {
                sendErrorMessage(session, "Invalid bid amount");
                return;
            }
            
            // Create and save bid
            Puja puja = new Puja();
            puja.setSubasta(subasta);
            puja.setUsuario(usuario);
            puja.setMonto(monto);
            
            ServicioPuja servicioPuja = new ServicioPuja();
            servicioPuja.crearPuja(puja);
            
            // Broadcast successful bid
            broadcastBid(puja);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private boolean isValidBid(Subasta subasta, double monto) {
        // Check if auction is active
        if (!"Activo".equals(subasta.getEstadoSubasta())) {
            return false;
        }
        
        // Get highest existing bid
        ServicioPuja servicioPuja = new ServicioPuja();
        List<Puja> existingBids = servicioPuja.listaPujasPorSubasta(subasta.getIdSubasta());
        
        double highestBid = existingBids.isEmpty() ? 
            subasta.getPrecioInicial() : 
            existingBids.stream()
                .mapToDouble(Puja::getMonto)
                .max()
                .orElse(subasta.getPrecioInicial());
        
        // New bid must be higher than current highest bid
        return monto > highestBid;
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
