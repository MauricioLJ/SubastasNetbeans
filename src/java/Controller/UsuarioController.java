/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Usuario;
import service.ServicioUsuario;

/**
 *
 * @author Mauricio
 */
@WebServlet("/login")
public class UsuarioController extends HttpServlet {
    
    private ServicioUsuario servicioUsuario = new ServicioUsuario();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("clave");

        Usuario usuario = servicioUsuario.autenticar(correo, contrasena);
        if (usuario != null) {
            // Login exitoso, redirecciona o guarda la sesión
            request.getSession().setAttribute("correo", usuario);
            response.sendRedirect("home.html"); // Redirige a la página principal
        } else {
            // Error de login
            response.sendRedirect("login.html?error=1"); // Redirige de vuelta con un mensaje de error
        }
    }
    
}
