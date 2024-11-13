/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import java.io.IOException;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Usuario;
import servicio.ServicioUsuario;

/**
 *
 * @author Mauricio
 */
@WebServlet({"/login", "/register"})
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
            request.getSession().setAttribute("usuario", usuario);
            response.sendRedirect("perfil.xhtml");
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

        // Validación de campos en el backend
        if (nombre == null || nombre.trim().isEmpty()
                || apellidos == null || apellidos.trim().isEmpty()
                || correo == null || correo.trim().isEmpty()
                || contrasena == null || contrasena.trim().isEmpty()) {

            response.sendRedirect("autenticacion.html?registroError=true");
            return;
        }

        if (servicioUsuario.existeCorreo(correo)) {
            response.sendRedirect("autenticacion.html?registroError=true");
            return;
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setApellidos(apellidos);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setContrasena(contrasena);

        servicioUsuario.crearUsuario(nuevoUsuario);
        response.sendRedirect("autenticacion.html?registroExitoso=true");
    }
}
