/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Mauricio
 */
@WebFilter("/perfil.xhtml")
public class AuthFilter implements Filter{
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Verifica si hay un usuario en la sesión
        if (req.getSession().getAttribute("usuario") == null) {
            // Si no hay usuario en la sesión, redirige a la página de login
            res.sendRedirect("autenticacion.html");
        } else {
            // Si el usuario está autenticado, permite que la solicitud continúe
            chain.doFilter(request, response);
        }
        
        
    }

    
    
    
}
