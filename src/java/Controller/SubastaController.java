/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import WebSocket.SubastaWebSocket;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
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
import model.Interacciones;
import model.Interacciones.TipoInteraccion;
import model.Notificacion;
import model.Puja;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import servicio.ServicioCategoria;
import servicio.ServicioInteracciones;
import servicio.ServicioNotificacion;
import servicio.ServicioPuja;

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
    private ServicioNotificacion servicioNotificacion = new ServicioNotificacion();
    private ServicioPuja servicioPuja = new ServicioPuja();
    private Subasta selectSubasta = new Subasta();
    private Subasta subasta = new Subasta();
    private List<Subasta> listaSubasta;
    private UploadedFile files;
    private String query;
    private List<Categoria> listaCategorias;
    private String selectCategoria;
    private String uploadedFilePath;
    private Subasta selectedSubasta;

    public SubastaController() {

    }

    @PostConstruct
    public void init() {
        try {
            finalizarSubastasViejas();
            cargarSubasta();
            listarSubastas();
            listaCategorias = servicioCategoria.listarCategorias();
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

    public void cargarCategorias(String categoria) {
        if ("Todo".equals(categoria)) {
            listaSubasta = servicioSubasta.listarSubastas();
        } else {
            listaSubasta = servicioSubasta.listarSubasPorCategoria(categoria);
        }
    }

    public void registrarInteraccion(Subasta subasta, String tipoInteraccion) {
        try {
            Interacciones interaccion = new Interacciones();
            interaccion.setSubasta(subasta);
            interaccion.setUsuario(usuarioBean.getUsuario());
            interaccion.setTipoInteraccion(TipoInteraccion.valueOf(tipoInteraccion));
            interaccion.setFechaInteraccion(new Date());

            ServicioInteracciones servicioInteracciones = new ServicioInteracciones();
            servicioInteracciones.registrarInteraccion(interaccion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTabChange(TabChangeEvent event) {
        String categoria = event.getTab().getTitle();
        if ("Para ti".equals(categoria)) {
            cargarRecomendaciones();
        } else {
            cargarCategorias(categoria);
        }
    }

    public void guardarSuba() {
        System.out.println(this.selectSubasta);
        if (this.selectSubasta.getIdSubasta() == null) {
            if (this.selectSubasta.getIdSubasta() == null) {
                if (files != null) {
                    handleFileUpload();
                }

                selectSubasta.setFechaInicio(new Date());
                selectSubasta.setEstadoSubasta("Activo");

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

    public void finalizarSubasta() {
        try {
            if (selectSubasta != null) {

                List<Puja> pujas = servicioPuja.listaPujasPorSubasta(selectSubasta.getIdSubasta());

                Puja pujaGanadora = pujas.stream()
                        .max((p1, p2) -> Double.compare(p1.getMonto(), p2.getMonto()))
                        .orElse(null);

                if (pujaGanadora != null) {
                    selectSubasta.setPujaGanadora(pujaGanadora);
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Sin Ganador", "La subasta no tiene pujas."));
                }

                selectSubasta.setEstadoSubasta("Finalizado");
                selectSubasta.setFechaFin(new Date());

                servicioSubasta.actualizarSubasta(selectSubasta);

                enviarNotificacionGanador(selectSubasta);
                enviarNotificacionVendedor(selectSubasta);

                listarSubastas();

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Subasta finalizada", "La subasta ha finalizado con éxito."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se ha seleccionado ninguna subasta."));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo finalizar la subasta."));
            e.printStackTrace();
        }
    }

    public void finalizarSubastasViejas() {
        try {
            List<Subasta> subastasVie = servicioSubasta.listarSubastas()
                    .stream()
                    .filter(subasta -> "Activo".equals(subasta.getEstadoSubasta()) && subasta.getFechaFin().before(new Date()))
                    .collect(Collectors.toList());

            for (Subasta subasta : subastasVie) {
                List<Puja> pujas = servicioPuja.listaPujasPorSubasta(subasta.getIdSubasta());

                Puja pujaGanadora = pujas.stream()
                        .max((p1, p2) -> Double.compare(p1.getMonto(), p2.getMonto()))
                        .orElse(null);

                if (pujaGanadora != null) {
                    subasta.setPujaGanadora(pujaGanadora);
                }

                subasta.setEstadoSubasta("Finalizado");
                subasta.setFechaFin(new Date());

                servicioSubasta.actualizarSubasta(subasta);

                if (pujaGanadora != null) {
                    enviarNotificacionGanador(subasta);
                }
                enviarNotificacionVendedor(subasta);

            }

        } catch (Exception e) {
            System.err.println("Error al finalizar subasta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarNotificacionGanador(Subasta subasta) {
        if (subasta != null && subasta.getPujaGanadora() != null) {
            Usuario ganador = subasta.getPujaGanadora().getUsuario();
            if (ganador != null) {
                Notificacion notificacion = new Notificacion();
                notificacion.setUsuario(ganador);
                notificacion.setMensaje("¡Felicidades! Has ganado la subasta de " + subasta.getNombre());
                notificacion.setFechaCreacion(new Date());

                servicioNotificacion.crearNotificacion(notificacion);

                // Enviar notificación en tiempo real
                SubastaWebSocket.sendNotification(
                        ganador.getIdUsuario(),
                        "¡Felicidades! Has ganado la subasta de " + subasta.getNombre());

                EmailNotificacion.enviarNotificacion(
                        ganador.getCorreo(),
                        "¡Felicidades! Has ganado la subasta",
                        "Hola " + ganador.getNombre() + ",\n\n"
                        + "Has ganado la subasta '" + subasta.getNombre() + "' con tu oferta de "
                        + subasta.getPujaGanadora().getMonto() + ". ¡Felicidades!\n\n"
                        + "Gracias por participar en nuestra plataforma."
                );
            }
        }
    }

    public void enviarNotificacionVendedor(Subasta subasta) {
        if (subasta != null && subasta.getPropietario() != null) {
            Usuario vendedor = subasta.getPropietario();
            String mensaje = "La subasta de " + subasta.getNombre()
                    + " ha finalizado. "
                    + (subasta.getPujaGanadora() != null
                    ? "El ganador es " + subasta.getPujaGanadora().getUsuario().getNombre() + " con una oferta de "
                    + subasta.getPujaGanadora().getMonto() + "."
                    : "No hubo ganador.");

            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(vendedor);
            notificacion.setMensaje(mensaje);
            notificacion.setFechaCreacion(new Date());

            servicioNotificacion.crearNotificacion(notificacion);

            // Enviar notificación en tiempo real
            SubastaWebSocket.sendNotification(
                    vendedor.getIdUsuario(),
                    mensaje);

            EmailNotificacion.enviarNotificacion(
                    vendedor.getCorreo(),
                    "Resultado de la subasta: " + subasta.getNombre(),
                    "Hola " + vendedor.getNombre() + ",\n\n"
                    + mensaje + "\n\nGracias por utilizar nuestra plataforma."
            );
        }
    }

    public void handleFileUpload() {
        try {
            if (files != null) {
                String rutaImagen = copyFile(files.getFileName(), files.getInputStream(), false);
                selectSubasta.setImagen(rutaImagen);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Imagen subida correctamente."));
                files = null;
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo cargar la imagen."));
            e.printStackTrace();
        }
    }

    protected String copyFile(String fileName, InputStream in, boolean esTemporal) {
        try {
            if (fileName != null) {
                String destinationFile = FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRealPath("C:\\Users\\Kirsten\\Documents\\NetBeansProjects\\SubastasNetbeans\\web\\resources\\imagenes\\");

                destinationFile = "C:\\Users\\Admin\\Documents\\NetBeansProjects\\SubastasNetbeans\\web\\resources\\imagenes\\";
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
                return "resources/imagenes/" + nombreArchivo + "." + extensionArchivo;
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
                FacesContext.getCurrentInstance().getExternalContext().redirect("verSubasta.xhtml?id=" + selected.getIdSubasta());
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
                this.subasta = this.selectedSubasta;

                registrarInteraccion(selectedSubasta, "VER");
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public void cargarRecomendaciones() {
        Usuario usuarioLogueado = usuarioBean.getUsuario();
        if (usuarioLogueado != null) {
            try {

                ServicioInteracciones servicioInteracciones = new ServicioInteracciones();
                List<String> categoriasFrecuentes = servicioInteracciones.obtenerCategoriasFrecuentesPorUsuario(usuarioLogueado.getIdUsuario());

                listaSubasta = servicioSubasta.recomendarSubastas(categoriasFrecuentes, usuarioLogueado.getIdUsuario());
            } catch (Exception e) {
                e.printStackTrace();
                listaSubasta = Collections.emptyList();
            }
        } else {
            listaSubasta = servicioSubasta.listarSubastas();
        }
    }

    public void listarSubastas() {
        try {
            this.listaSubasta = servicioSubasta.listarSubastas();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getCurrentHighestBid() {
        ServicioPuja servicioPuja = new ServicioPuja();
        List<Puja> pujas = servicioPuja.listaPujasPorSubasta(this.selectedSubasta.getIdSubasta());

        return pujas.isEmpty()
                ? this.selectedSubasta.getPrecioInicial()
                : pujas.stream()
                        .mapToDouble(Puja::getMonto)
                        .max()
                        .orElse(this.selectedSubasta.getPrecioInicial());
    }

    public String viewAuction(Subasta subasta) {
        this.selectedSubasta = subasta;
        return "verSubasta?id=" + subasta.getIdSubasta() + "&faces-redirect=true";
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

    public List<Categoria> getListaCategorias() {
        return listaCategorias;
    }

    public void setListaCategorias(List<Categoria> listaCategorias) {
        this.listaCategorias = listaCategorias;
    }

    public String getSelectCategoria() {
        return selectCategoria;
    }

    public void setSelectCategoria(String selectCategoria) {
        this.selectCategoria = selectCategoria;
    }

    public ServicioNotificacion getServicioNotificacion() {
        return servicioNotificacion;
    }

    public void setServicioNotificacion(ServicioNotificacion servicioNotificacion) {
        this.servicioNotificacion = servicioNotificacion;
    }

    public ServicioPuja getServicioPuja() {
        return servicioPuja;
    }

    public void setServicioPuja(ServicioPuja servicioPuja) {
        this.servicioPuja = servicioPuja;
    }

}