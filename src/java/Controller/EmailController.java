/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import servicio.ServicioUsuario;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Mauricio
 */
@WebServlet("/verificar-email")
public class EmailController extends HttpServlet{
    private ServicioUsuario servicioUsuario = new ServicioUsuario();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        boolean exists = servicioUsuario.existeCorreo(email);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = "{\"exists\": " + exists + "}";
        response.getWriter().write(jsonResponse);
    }
}
