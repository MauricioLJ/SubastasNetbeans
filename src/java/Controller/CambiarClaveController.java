package Controller;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import model.Usuario;
import servicio.ServicioToken;
import servicio.ServicioUsuario;

/**
 *
 * @author Mauricio
 */
@WebServlet("/cambiarClave")
public class CambiarClaveController extends HttpServlet {

    private ServicioToken servicioToken = new ServicioToken();
    private ServicioUsuario servicioUsuario = new ServicioUsuario(); // Instancia del servicio de usuario

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String email = servicioToken.obtenerEmailPorToken(token);

        if (email == null) {
            response.sendRedirect("error.html");
            return;
        }

        // Pasar el correo al request como atributo
        request.setAttribute("email", email);  // Asegúrate de que el correo se ha establecido

        request.setAttribute("token", token);
        request.getRequestDispatcher("cambiarClave.xhtml").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        String nuevaClave = request.getParameter("nuevaClave");
        String email = request.getParameter("email");

        System.out.println("Correo recibido: " + email);

        if (nuevaClave == null || nuevaClave.isEmpty()) {
            request.setAttribute("error", "La contraseña no puede estar vacía");
            request.getRequestDispatcher("cambiarClave.xhtml").forward(request, response);
            return;
        }

        // Actualizar la contraseña en la base de datos
        servicioUsuario.actualizarClavePorCorreo(email, nuevaClave);

        // Eliminar el token una vez que se ha usado
        servicioToken.eliminarToken(token);

        Usuario usuario = servicioUsuario.obtenerUsuarioPorCorreo(email);
        if (usuario != null) {
            HttpSession session = request.getSession();
            session.setAttribute("usuario", usuario);
            // Redirect with a success parameter
            response.sendRedirect("cambiarClave.xhtml?passwordChanged=true");
        } else {
            response.sendRedirect("error.html");
        }
    }

}
