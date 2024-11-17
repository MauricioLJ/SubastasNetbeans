package Converter;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import model.Subasta;
import servicio.ServicioSubasta;

@ApplicationScoped
@FacesConverter(value = "subastaConverter", managed = true)
public class SubastaConverter implements Converter<Subasta> {

    private ServicioSubasta servicioSubasta = new ServicioSubasta();

    @Override
    public Subasta getAsObject(FacesContext fc, UIComponent uic, String string) {
        if (string != null && !string.trim().isEmpty()) {
            try {
                Integer id = Integer.valueOf(string);
                return servicioSubasta.leerSubasta(id);
            } catch (NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "No es un ID de subasta válido."));
            }
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Subasta subasta) {
        if (subasta != null) {
            return String.valueOf(subasta.getIdSubasta());
        } else {
            return null;
        }
    }
}
