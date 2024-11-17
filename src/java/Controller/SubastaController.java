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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import model.Subasta;
import model.Usuario;
import org.primefaces.model.file.UploadedFile;
import servicio.ServicioSubasta;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import model.Categoria;
import org.primefaces.event.SelectEvent;
import servicio.ServicioCategoria;

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
    private ServicioCategoria servicioCategoria = new ServicioCategoria();
    private Subasta selectSubasta = new Subasta();
    private Subasta subasta = new Subasta();
    private List<Subasta> listaSubasta;
    private List<Categoria> categoriasLista;
    private UploadedFile files;
    private List<Subasta> listaSubastaFilter;
    private String query;
    List<String> categorias = new ArrayList<>();
    private String uploadedFilePath;
    private Subasta selectedSubasta;

    public SubastaController() {

    }

    @PostConstruct
    public void init() {

        try {
            listarSubastas();
            categoriasLista = servicioCategoria.listarCategorias();

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

            selectSubasta.setFechaInicio(new Date());
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

    public void eliminarSubasta() {
        try {
            if (selectSubasta != null) {
                servicioSubasta.eliminarSubasta(selectSubasta.getIdSubasta());
                listarSubastas();
                this.selectSubasta = new Subasta();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Subasta eliminada", ""));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se ha seleccionado ninguna subasta"));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la subasta"));
            e.printStackTrace();
        }
    }

    public void handleFileUpload() {
        try {
            System.out.println("===>>> " + this.files);
            System.out.println("===>>> " + this.files.getFileName() + " size: " + this.files.getSize());
            String rutaImagen = this.copyFile(this.files.getFileName(), this.files.getInputStream(), false);

            selectSubasta.setImagen(rutaImagen);
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

    public List<Subasta> complete(String query) {
        String querryLowerCase = query.toLowerCase();
        List<Subasta> subasta = servicioSubasta.listarSubastas();
        return subasta.stream().filter(u -> u.getNombre().toLowerCase().contains(querryLowerCase)).collect(Collectors.toList());
    }
    
    public void onSubastaSelect(SelectEvent<Subasta> event) throws ClassNotFoundException {
        Subasta selected = event.getObject();
        if (selected != null) {
            System.out.println("Subasta seleccionada: " + selected.getNombre());
            cargarSubasta();

            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("detalleSubasta.xhtml?id=" + selected.getIdSubasta());
            } catch (IOException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Hubo un error al redirigir."));
                e.printStackTrace();
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se seleccionó ninguna subasta."));
        }
    }

    public void cargarSubasta() {
    FacesContext context = FacesContext.getCurrentInstance();
    Map<String, String> params = context.getExternalContext().getRequestParameterMap();
    String idParam = params.get("id");
    if (idParam != null && !idParam.isEmpty()) {
        try {
            int id = Integer.parseInt(idParam);
            this.selectedSubasta = servicioSubasta.leerSubasta(id);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
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

    public ServicioCategoria getServicioCategoria() {
        return servicioCategoria;
    }

    public void setServicioCategoria(ServicioCategoria servicioCategoria) {
        this.servicioCategoria = servicioCategoria;
    }

    public List<Categoria> getCategoriasLista() {
        return categoriasLista;
    }

    public void setCategoriasLista(List<Categoria> categoriasLista) {
        this.categoriasLista = categoriasLista;
    }

    public Subasta getSubasta() {
        return subasta;
    }

    public void setSubasta(Subasta subasta) {
        this.subasta = subasta;
    }

    public Subasta getSelectedSubasta() {
        return selectedSubasta;
    }

    public void setSelectedSubasta(Subasta selectedSubasta) {
        this.selectedSubasta = selectedSubasta;
    }

}
