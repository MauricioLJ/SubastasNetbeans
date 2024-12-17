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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import model.Puja;
import model.Subasta;
import model.Usuario;
import org.primefaces.event.RateEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.file.UploadedFile;
import servicio.ServicioPuja;
import servicio.ServicioSubasta;
import servicio.ServicioUsuario;

/**
 * @author Mauricio
 */
@ManagedBean(name = "usuarioBean")
@SessionScoped
public class UsuarioBean implements Serializable {

    private ServicioUsuario servicioUsuario = new ServicioUsuario();
    private ServicioSubasta servicioSubasta = new ServicioSubasta();
    private ServicioPuja servicioPuja = new ServicioPuja();
    private Usuario usuario;
    private Usuario selectUsuario = new Usuario();
    private UploadedFile files;
    private String uploadedFilePath;
    private Integer rating;
    private Integer calificacion;
    private Usuario selectedUsuario;
    private Double calificacionPromedio = 0.0;
    private boolean puedeCalificar;
    private List<Puja> historialPujas;
    private List<Subasta> todasLasSubastas;

    public UsuarioBean() {
        HttpSession session = SessionUtils.getSession();
        this.usuario = (Usuario) session.getAttribute("usuario");
        this.historialPujas = new ArrayList<>();
        this.todasLasSubastas = new ArrayList<>();

        if (this.usuario != null) {
            this.selectUsuario = this.usuario;
            this.selectedUsuario = this.usuario;
            cargarSubastasPorUsuario();
        } else {
            this.selectUsuario = new Usuario();
        }

    }

    public Usuario getUsuario() {
        if (usuario == null) {
            HttpSession session = SessionUtils.getSession();
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

            usuario.setFotoPerfil(rutaImagen);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Se subió éxitosamente la imagen"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Hubo un problema al subir la imagen."));
            e.printStackTrace();
        }
    }

    protected String copyFile(String fileName, InputStream in, boolean esTemporal) {
        try {
            if (fileName != null) {
                String destinationFile = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/fotosPer/");

                destinationFile = "C:\\Users\\Admin\\Documents\\NetBeansProjects\\SubastasNetbeans\\web\\resources\\fotosPer\\";

                //Hacer lo mismo copiar directirio exacto
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
                return "resources/fotosPer/" + nombreArchivo + "." + extensionArchivo;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void calificarUsuario() {
        if (rating == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Calificación no válida."));
            return;
        }

        try {

            if (selectedUsuario == null || selectedUsuario.getIdUsuario() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario del perfil no encontrado."));
                return;
            }

            Usuario usuarioACalificar = servicioUsuario.leerUsuario(selectedUsuario.getIdUsuario());
            if (usuarioACalificar == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario del perfil no encontrado en la base de datos."));
                return;
            }

            servicioUsuario.actualizarCalificacion(usuarioACalificar, rating);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Calificación registrada con éxito."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo registrar la calificación."));
            e.printStackTrace();
        }
    }

    public boolean isPuedeCalificar() {
        return puedeCalificar;
    }

    public void llevarPefiles(int idUsuario) {
        try {
            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .redirect("perfiles.xhtml?id=" + idUsuario);
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Hubo un error al redirigir al perfil."));
            e.printStackTrace();
        }
    }

    public void cargarPerfiles() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String idParam = params.get("id");

        if (idParam != null) {
            int idUsuario = Integer.parseInt(idParam);
            selectedUsuario = servicioUsuario.leerUsuario(idUsuario);
        }

        historialPujas = servicioPuja.obtenerHistorialPorUsuario(usuario.getIdUsuario());

        puedeCalificar = historialPujas.stream()
                .anyMatch(puja -> puja.getSubasta().getPropietario().getIdUsuario().equals(selectedUsuario.getIdUsuario())
                && "Finalizado".equals(puja.getSubasta().getEstadoSubasta()));
    }

    public ServicioUsuario getServicioUsuario() {
        return servicioUsuario;
    }

    public void cargarHistorial() {
        if (usuario != null && usuario.getIdUsuario() != null) {
            this.historialPujas = servicioPuja.obtenerHistorialPorUsuario(usuario.getIdUsuario());
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario no autenticado o ID no válido."));
        }
    }

    public void cargarSubastasPorUsuario() {
        if (selectUsuario != null && selectUsuario.getIdUsuario() != null) {
            this.todasLasSubastas = servicioSubasta.listaSubastasPorUsuario(selectedUsuario.getIdUsuario());
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario no autenticado o ID no válido."));
        }
    }

    public List<Subasta> getSubastasActivas() {
        if (todasLasSubastas != null) {
            return todasLasSubastas.stream()
                    .filter(subasta -> "activo".equalsIgnoreCase(subasta.getEstadoSubasta()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void setServicioUsuario(ServicioUsuario servicioUsuario) {
        this.servicioUsuario = servicioUsuario;
    }

    public Usuario getSelectUsuario() {
        return selectUsuario;
    }

    public void setSelectUsuario(Usuario selectUsuario) {
        this.selectUsuario = selectUsuario;
        cargarSubastasPorUsuario();
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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }

    public Usuario getSelectedUsuario() {
        return selectedUsuario;
    }

    public void setSelectedUsuario(Usuario selectedUsuario) {
        this.selectedUsuario = selectedUsuario;
    }

    public List<Puja> getHistorialPujas() {
        return historialPujas;
    }

    public void setHistorialPujas(List<Puja> historialPujas) {
        this.historialPujas = historialPujas;
    }

    public ServicioSubasta getServicioSubasta() {
        return servicioSubasta;
    }

    public void setServicioSubasta(ServicioSubasta servicioSubasta) {
        this.servicioSubasta = servicioSubasta;
    }

    public ServicioPuja getServicioPuja() {
        return servicioPuja;
    }

    public void setServicioPuja(ServicioPuja servicioPuja) {
        this.servicioPuja = servicioPuja;
    }

    public Double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(Double calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setPuedeCalificar(boolean puedeCalificar) {
        this.puedeCalificar = puedeCalificar;
    }

    public List<Subasta> getTodasLasSubastas() {
        return todasLasSubastas;
    }

    public void setTodasLasSubastas(List<Subasta> todasLasSubastas) {
        this.todasLasSubastas = todasLasSubastas;
    }

}
