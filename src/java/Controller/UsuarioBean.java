/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import model.Usuario;
import org.primefaces.model.file.UploadedFile;
import servicio.ServicioUsuario;

/**
 * @author Mauricio
 */
@ManagedBean (name = "usuarioBean")
@SessionScoped
public class UsuarioBean implements Serializable{
    
    private ServicioUsuario servicioUsuario = new ServicioUsuario();
    private Usuario usuario;
    private Usuario selectUsuario = new Usuario();
    private UploadedFile files;
    private String uploadedFilePath;

    
    
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
    

    public void actualizarPerfil() {
        try {
            
             if (files != null) {
                handleFileUpload();
            }
            
            if (usuario != null) {
                servicioUsuario.actualizarUsuario(usuario);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Perfil actualizado con éxito"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario no encontrado en sesión"));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar el perfil"));
            e.printStackTrace();
        }
    }
    
    
      public void handleFileUpload() {
        try {
            System.out.println("===>>> " + this.files);
            System.out.println("===>>> " + this.files.getFileName() + " size: " + this.files.getSize());
            String rutaImagen = this.copyFile(this.files.getFileName(), this.files.getInputStream(), false);

            selectUsuario.setFotoPerfil(rutaImagen);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Se subió éxitosamente la imagen"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Hubo un problema al subir la imagen."));
            e.printStackTrace();
        }
    }
      
      

      protected String copyFile(String fileName, InputStream in, boolean esTemporal) {
        try {
            if (fileName != null) {
                String destinationFile = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/imagenes/");

                String[] partesArchivo = fileName.split(Pattern.quote("."));
                String nombreArchivo = partesArchivo[0];
                String extensionArchivo = partesArchivo[1];
                if (esTemporal) {
                    nombreArchivo += "_TMP";
                }

                File tmp = new File(destinationFile + nombreArchivo + "." + extensionArchivo);
                tmp.getParentFile().mkdirs();
                OutputStream out = new FileOutputStream(tmp);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }

                in.close();
                out.flush();
                out.close();
                return "/resources/imagenes/" + nombreArchivo + "." + extensionArchivo;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ServicioUsuario getServicioUsuario() {
        return servicioUsuario;
    }

    public void setServicioUsuario(ServicioUsuario servicioUsuario) {
        this.servicioUsuario = servicioUsuario;
    }

    public Usuario getSelectUsuario() {
        return selectUsuario;
    }

    public void setSelectUsuario(Usuario selectUsuario) {
        this.selectUsuario = selectUsuario;
    }

    public UploadedFile getFiles() {
        return files;
    }

    public void setFiles(UploadedFile files) {
        this.files = files;
    }

    public String getUploadedFilePath() {
        return uploadedFilePath;
    }

    public void setUploadedFilePath(String uploadedFilePath) {
        this.uploadedFilePath = uploadedFilePath;
    }
    
      
    
}




