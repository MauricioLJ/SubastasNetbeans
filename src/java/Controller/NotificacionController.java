package Controller;

import model.Notificacion;
import model.Subasta;
import model.Usuario;
import servicio.ServicioNotificacion;
import WebSocket.NotificacionWebsocket;
import java.util.List;

/**
 *
 * @author Mauricio
 */
public class NotificacionController {

    private ServicioNotificacion servicioNotificacion;

    public NotificacionController() {
        this.servicioNotificacion = new ServicioNotificacion();
    }

    public void processBidNotification(Subasta subasta, Usuario usuarioAnterior, Double nuevoPrecio) {
        System.out.println("processBidNotification ejecutado. Subasta: " + subasta.getIdSubasta() + " Usuario Anterior: " + usuarioAnterior.getIdUsuario() + " Nuevo Precio: " + nuevoPrecio);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuarioAnterior);
        notificacion.setTipoNotificacion("PUJA");
        notificacion.setMensaje("Te han superado en la subasta");
        notificacion.setSubastaId(subasta.getIdSubasta());
        notificacion.setMontoPuja(nuevoPrecio);
        notificacion.setLeida(false);

        // Incluir el nombre de la subasta en la notificación
        notificacion.setSubastaNombre(subasta.getNombre());

        // Persistir la notificación
        servicioNotificacion.crearNotificacion(notificacion);

        // Enviar notificación por WebSocket
        NotificacionWebsocket.sendNotificationToUser(
                String.valueOf(usuarioAnterior.getIdUsuario()),
                notificacion
        );
    }

    // Method to create and send a general notification
    public void createGeneralNotification(Usuario usuario, String tipoNotificacion, String mensaje) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTipoNotificacion(tipoNotificacion);
        notificacion.setMensaje(mensaje);
        notificacion.setLeida(false);

        System.out.println("Antes de crear notificación en la BD");
        // Persist notification
        servicioNotificacion.crearNotificacion(notificacion);
        System.out.println("Notificación creada en la BD");
        // Send WebSocket notification
        NotificacionWebsocket.sendNotificationToUser(
                String.valueOf(usuario.getIdUsuario()),
                notificacion
        );
    }

    // Method to broadcast notification to multiple users
    public void broadcastNotification(List<Usuario> usuarios, String tipoNotificacion, String mensaje) {
        for (Usuario usuario : usuarios) {
            createGeneralNotification(usuario, tipoNotificacion, mensaje);
        }
    }

}
