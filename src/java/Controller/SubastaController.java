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
import java.util.List;
import model.Subasta;
import model.Usuario;
import org.primefaces.model.file.UploadedFile;
import servicio.ServicioSubasta;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author Kirsten
 */
@ManagedBean(name = "subastaController")
@SessionScoped
public class SubastaController implements Serializable {

    @ManagedProperty(value = "#{usuarioBean}")
    private UsuarioBean usuarioBean;

    private ServicioSubasta servicioSubasta = new ServicioSubasta();
    private Subasta selectSubasta = new Subasta();
    private List<Subasta> listaSubasta;
    private UploadedFile files;
    private String query;
    private String uploadedFilePath;

    public SubastaController() {

    }

    @PostConstruct
    public void init() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openNew() {
        this.selectSubasta = new Subasta();
    }

    public void setUsuarioBean(UsuarioBean usuarioBean) {
        this.usuarioBean = usuarioBean;
    }

    public void guardarSuba() {
        System.out.println(this.selectSubasta);
        if (this.selectSubasta.getIdSubasta() == null) {

            selectSubasta.setEstadoSubasta("Activo");

            if (files != null) {
                handleFileUpload();
            }

            Usuario usuarioLogueado = usuarioBean.getUsuario();

            if (usuarioLogueado != null) {
                selectSubasta.setPropietario(usuarioLogueado);
            }

            try {
                servicioSubasta.crearSubasta(selectSubasta);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Subasta guardada", "La subasta ha sido guardada correctamente."));
                listarSubastas();
                this.selectSubasta = new Subasta();
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error al guardar la subasta", "Hubo un problema al guardar la subasta: " + e.getMessage()));
                e.printStackTrace();
            }
        }

    }

    public void handleFileUpload() {
        try {
            System.out.println("===>>> " + this.files);
            System.out.println("===>>> " + this.files.getFileName() + " size: " + this.files.getSize());
            String rutaImagen = this.copyFile(this.files.getFileName(), this.files.getInputStream(), false);

            selectSubasta.setImagen(rutaImagen);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Éxito", "Se subió éxitosamente la imagen"));

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

    public void listarSubastas() {
        try {
            this.listaSubasta = servicioSubasta.listarSubastas();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServicioSubasta getServicioSubasta() {
        return servicioSubasta;
    }

    public void setServicioSubasta(ServicioSubasta servicioSubasta) {
        this.servicioSubasta = servicioSubasta;
    }

    public Subasta getSelectSubasta() {
        return selectSubasta;
    }

    public void setSelectSubasta(Subasta selectSubasta) {
        this.selectSubasta = selectSubasta;
    }

    public List<Subasta> getListaSubasta() {
        return listaSubasta;
    }

    public void setListaSubasta(List<Subasta> listaSubasta) {
        this.listaSubasta = listaSubasta;
    }

    public UploadedFile getFiles() {
        return files;
    }

    public void setFiles(UploadedFile files) {
        this.files = files;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getUploadedFilePath() {
        return uploadedFilePath;
    }

    public void setUploadedFilePath(String uploadedFilePath) {
        this.uploadedFilePath = uploadedFilePath;
    }

}