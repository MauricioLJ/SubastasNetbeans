package Controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.List;

/**
 *
 * @author Mauricio
 */
@ManagedBean
@ApplicationScoped
public class PaisesBean {
    private List<String> listaPaises;

    public PaisesBean() {
        listaPaises = Arrays.asList(
            "Afganistán", "Albania", "Alemania", "Andorra", "Angola", 
            "Antigua y Barbuda", "Arabia Saudita", "Argelia", "Argentina", 
            "Armenia", "Australia", "Austria", "Azerbaiyán", "Bahamas", 
            "Bahrein", "Bangladesh", "Barbados", "Bélgica", "Belice", 
            "Benín", "Bielorrusia", "Bolivia", "Bosnia y Herzegovina", 
            "Botsuana", "Brasil", "Brunéi", "Bulgaria", "Burkina Faso", 
            "Burundi", "Bután", "Cabo Verde", "Camboya", "Camerún", 
            "Canadá", "Catar", "Chad", "Chile", "China", "Chipre", 
            "Ciudad del Vaticano", "Colombia", "Comoras", "Corea del Norte", 
            "Corea del Sur", "Costa de Marfil", "Costa Rica", "Croacia", 
            "Cuba", "Dinamarca", "Dominica", "Ecuador", "Egipto", 
            "El Salvador", "Emiratos Árabes Unidos", "Eritrea", "Eslovaquia", 
            "Eslovenia", "España", "Estados Unidos", "Estonia", "Etiopía", 
            "Filipinas", "Finlandia", "Fiyi", "Francia", "Gabón", "Gambia", 
            "Georgia", "Ghana", "Granada", "Grecia", "Guatemala", "Guinea", 
            "Guinea-Bisáu", "Guinea Ecuatorial", "Guyana", "Haití", 
            "Honduras", "Hungría", "India", "Indonesia", "Irak", "Irán", 
            "Irlanda", "Islandia", "Israel", "Italia", "Jamaica", "Japón", 
            "Jordania", "Kazajistán", "Kenia", "Kirguistán", "Kiribati", 
            "Kuwait", "Laos", "Lesoto", "Letonia", "Líbano", "Liberia", 
            "Libia", "Liechtenstein", "Lituania", "Luxemburgo", "Macedonia", 
            "Madagascar", "Malasia", "Malaui", "Maldivas", "Mali", "Malta", 
            "Marruecos", "Mauricio", "Mauritania", "México", "Micronesia", 
            "Moldavia", "Mónaco", "Mongolia", "Montenegro", "Mozambique", 
            "Namibia", "Nauru", "Nepal", "Nicaragua", "Níger", "Nigeria", 
            "Noruega", "Nueva Zelanda", "Omán", "Países Bajos", "Pakistán", 
            "Palaos", "Panamá", "Papúa Nueva Guinea", "Paraguay", "Perú", 
            "Polonia", "Portugal", "Reino Unido", "República Centroafricana", 
            "República Checa", "República Democrática del Congo", 
            "República Dominicana", "Ruanda", "Rumania", "Rusia", "Samoa", 
            "San Cristóbal y Nieves", "San Marino", "San Vicente y las Granadinas", 
            "Santa Lucía", "Santo Tomé y Príncipe", "Senegal", "Serbia", 
            "Seychelles", "Sierra Leona", "Singapur", "Siria", "Somalia", 
            "Sri Lanka", "Suazilandia", "Sudáfrica", "Sudán", "Sudán del Sur", 
            "Suecia", "Suiza", "Surinam", "Tailandia", "Tanzania", "Tayikistán", 
            "Timor Oriental", "Togo", "Tonga", "Trinidad y Tobago", "Túnez", 
            "Turkmenistán", "Turquía", "Tuvalu", "Ucrania", "Uganda", 
            "Uruguay", "Uzbekistán", "Vanuatu", "Venezuela", "Vietnam", 
            "Yemen", "Yibuti", "Zambia", "Zimbabue"
        ).stream().sorted().collect(java.util.stream.Collectors.toList());
    }

    public List<String> getListaPaises() {
        return listaPaises;
    }
}