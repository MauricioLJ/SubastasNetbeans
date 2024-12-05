/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WebSocket;

import Controller.EmailNotificacion;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import model.Notificacion;

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
 * @author Dariana
 */
@ServerEndpoint("/subastaSocket")
public class SubastaWebSocket {

    private static final ConcurrentHashMap<String, Session> activeSessions = new ConcurrentHashMap<>();
    //private static final Set<Session> activeSessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        activeSessions.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        activeSessions.remove(session.getId(), session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject jsonMessage = new JSONObject(message);

            String messageType = jsonMessage.getString("type");

            switch (messageType) {
                case "placeBid":
                    handlePlaceBid(jsonMessage, session);
                    break;
                case "endAuction":
                    handleEndAuction(jsonMessage);
                    break;
                default:
                    sendErrorMessage(session, "Invalid message type");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            sendErrorMessage(session, "Error processing message");
        }
    }

    private boolean isValidBid(Subasta subasta, double monto, List<Puja> existingBids) {
        if (!"Activo".equals(subasta.getEstadoSubasta())) {
            return false;
        }

        double highestBid = existingBids.isEmpty()
                ? subasta.getPrecioInicial()
                : existingBids.stream()
                        .mapToDouble(Puja::getMonto)
                        .max()
                        .orElse(subasta.getPrecioInicial());
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

            // Broadcast to all active sessions
            for (Session session : activeSessions.values()) {
                try {
                    session.getBasicRemote().sendText(bidMessage.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlePlaceBid(JSONObject jsonMessage, Session session) {
        try {
            int idSubasta = jsonMessage.getInt("idSubasta");
            int idUsuario = jsonMessage.getInt("idUsuario");
            double monto = jsonMessage.getDouble("monto");

            ServicioSubasta servicioSubasta = new ServicioSubasta();
            Subasta subasta = servicioSubasta.leerSubasta(idSubasta);

            ServicioUsuario servicioUsuario = new ServicioUsuario();
            Usuario usuario = servicioUsuario.leerUsuario(idUsuario);

            if (subasta == null || usuario == null) {
                sendErrorMessage(session, "Invalid auction or user");
                return;
            }

            ServicioPuja servicioPuja = new ServicioPuja();
            List<Puja> existingBids = servicioPuja.listaPujasPorSubasta(idSubasta);
            Puja highestBid = existingBids.stream()
                    .max((p1, p2) -> Double.compare(p1.getMonto(), p2.getMonto()))
                    .orElse(null);

            if (!isValidBid(subasta, monto, existingBids)) {
                sendErrorMessage(session, "Invalid bid amount");
                return;
            }

            Puja puja = new Puja();
            puja.setSubasta(subasta);
            puja.setUsuario(usuario);
            puja.setMonto(monto);
            servicioPuja.crearPuja(puja);

            if (highestBid != null && highestBid.getUsuario().getIdUsuario() != idUsuario) {
                sendNotification(highestBid.getUsuario().getIdUsuario(),
                        "Has sido superado en la subasta #" + idSubasta);
            }

            broadcastBid(puja);

        } catch (JSONException e) {
            e.printStackTrace();
            sendErrorMessage(session, "Error handling bid");
        }
    }

    private void handleEndAuction(JSONObject jsonMessage) {
        try {
            int idSubasta = jsonMessage.getInt("idSubasta");

            ServicioSubasta servicioSubasta = new ServicioSubasta();
            Subasta subasta = servicioSubasta.leerSubasta(idSubasta);

            if (subasta == null || !"Activo".equals(subasta.getEstadoSubasta())) {
                return;
            }

            ServicioPuja servicioPuja = new ServicioPuja();
            List<Puja> existingBids = servicioPuja.listaPujasPorSubasta(idSubasta);
            Puja highestBid = existingBids.stream()
                    .max((p1, p2) -> Double.compare(p1.getMonto(), p2.getMonto()))
                    .orElse(null);

            if (highestBid != null) {
                Usuario ganador = highestBid.getUsuario();
                sendNotification(ganador.getIdUsuario(),
                        "¡Felicidades! Ganaste la subasta #" + idSubasta);
            }

            subasta.setEstadoSubasta("Finalizada");
            servicioSubasta.actualizarSubasta(subasta);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void sendNotification(int idUsuario, String mensaje) {
        JSONObject notificacionJson = new JSONObject();
        notificacionJson.put("tipo", "Finalizado");
        notificacionJson.put("mensaje", mensaje);
        notificacionJson.put("fecha", System.currentTimeMillis());

        for (Session session : activeSessions.values()) {
            Integer sessionIdUsuario = (Integer) session.getUserProperties().get("idUsuario");
            if (sessionIdUsuario != null && sessionIdUsuario.equals(idUsuario)) {
                try {
                    session.getBasicRemote().sendText(notificacionJson.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
