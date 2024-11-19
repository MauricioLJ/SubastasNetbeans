package Controller;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Random;
import javax.servlet.annotation.WebServlet;
import servicio.ServicioEmail;
import servicio.ServicioToken;

/**
 *
 * @author Mauricio
 */
@WebServlet("/forgot-password")
public class OlvideClaveController extends HttpServlet {

    private ServicioEmail servicioEmail = new ServicioEmail();
    private ServicioToken servicioToken = new ServicioToken();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");

        if (email == null || email.isEmpty()) {
            request.setAttribute("error", "Por favor, ingresa un correo válido");
            request.getRequestDispatcher("correoEnviado.html").forward(request, response);
            return;
        }

        // Generar token único
        String token = servicioToken.generarToken(email);
        String enlace = "http://localhost:8080/SubastasFront/cambiarClave?token=" + token;

        // Enviar correo con el enlace
        servicioEmail.enviarEnlaceRecuperacion(email, enlace);

        // Redirigir a la página de confirmación
        request.getRequestDispatcher("correoEnviado.html").forward(request, response);
    }
}
