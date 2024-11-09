/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.http.HttpSession;
import model.Usuario;

/**
 *
 * @author Mauricio
 */
@ManagedBean (name = "usuarioBean")
@SessionScoped
public class UsuarioBean {
    
    
    private Usuario usuario;

    public UsuarioBean() {
        HttpSession session = SessionUtils.getSession();
        this.usuario = (Usuario) session.getAttribute("usuario");
    }

    public Usuario getUsuario() {
        if (usuario == null) {
            HttpSession  session = SessionUtils.getSession();
            usuario = (Usuario) session.getAttribute("usuario");
        }
        return usuario;
    }

    public String logout() {
        SessionUtils.getSession().invalidate();
        return "autenticacion.xhtml?faces-redirect=true";
    }
}
