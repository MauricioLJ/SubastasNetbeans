package Controller;

import java.io.IOException;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Usuario;
import servicio.ServicioUsuario;

/**
 *
 * @author Mauricio
 */
@WebServlet({"/login", "/register", "/logout"})
public class UsuarioController extends HttpServlet {

    private ServicioUsuario servicioUsuario = new ServicioUsuario();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getServletPath();

        if ("/login".equals(action)) {
            loginUsuario(request, response);
        } else if ("/register".equals(action)) {
            registrarUsuario(request, response);
        }
    }

    private void loginUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("clave");
        Usuario usuario = servicioUsuario.loginUsuario(correo, contrasena);
        if (usuario != null) {
            // Guarda el usuario en la sesión
            HttpSession httpSession = request.getSession();
            httpSession.setAttribute("usuario", usuario);
            response.sendRedirect("pagprin.xhtml");
        } else {
            response.sendRedirect("autenticacion.html?error=true");
        }
    }

    private void registrarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = request.getParameter("nombre");
        String apellidos = request.getParameter("apellidos");
        String correo = request.getParameter("email");
        String contrasena = request.getParameter("pass");

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setApellidos(apellidos);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setContrasena(contrasena);

        servicioUsuario.crearUsuario(nuevoUsuario);
        Usuario usuario = servicioUsuario.obtenerUsuarioPorCorreo(correo);

        if (usuario != null) {
            HttpSession session = request.getSession();
            session.setAttribute("usuario", usuario);
            response.sendRedirect("autenticacion.html?registro=true");
        } else {
            response.sendRedirect("error.html");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getServletPath();
        if ("/logout".equals(action)) {
            logout(request, response);
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Invalidate the session
        }
        response.sendRedirect("autenticacion.html"); // Redirect to login page
    }
}
