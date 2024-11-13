// autenticacion.js
const container = document.getElementById('container');
const registerBtn = document.getElementById('register');
const loginBtn = document.getElementById('login');

registerBtn.addEventListener('click', () => {
    container.classList.add("active");
});

loginBtn.addEventListener('click', () => {
    container.classList.remove("active");
});

document.addEventListener("DOMContentLoaded", function () {
    // Elementos del formulario de login
    const correoInput = document.getElementById('correo');
    const claveInput = document.getElementById('clave');
    const loginBtn = document.getElementById('loginBtn');

    // Elementos del formulario de registro
    const nombreInput = document.getElementById('nombre');
    const apellidosInput = document.getElementById('apellidos');
    const emailInput = document.getElementById('email');
    const passInput = document.getElementById('pass');
    const signupBtn = document.getElementById('signupBtn');

    // Función para validar el botón de login
    function validateLoginButton() {
        const correoValido = correoInput.value.trim() !== '';
        const claveValida = claveInput.value.length >= 8;
        loginBtn.disabled = !(correoValido && claveValida);
    }

    // Añadir eventos al formulario de login
    correoInput.addEventListener('input', validateLoginButton);
    claveInput.addEventListener('input', validateLoginButton);

    // Validar al cargar la página (en caso de que los campos ya tengan valores)
    validateLoginButton();

    // Función para validar el botón de registro
    function validateSignupButton() {
        const nombreValido = nombreInput.value.trim() !== '';
        const apellidosValido = apellidosInput.value.trim() !== '';
        const emailValido = validateEmail(emailInput.value);
        const passValida = passInput.value.length >= 8;

        // Habilitar el botón si todos los campos son válidos
        signupBtn.disabled = !(nombreValido && apellidosValido && emailValido && passValida);
    }

    // Validación del formato del correo electrónico
    function validateEmail(email) {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    }

    // Añadir eventos al formulario de registro
    nombreInput.addEventListener('input', validateSignupButton);
    apellidosInput.addEventListener('input', validateSignupButton);
    emailInput.addEventListener('input', validateSignupButton);
    passInput.addEventListener('input', validateSignupButton);

    // Validar al cargar la página (en caso de que los campos ya tengan valores)
    validateSignupButton();

    // Mostrar mensaje de error si hay un parámetro en la URL
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has("error")) {
        document.getElementById("error-message").style.display = "block";
    }
    if (urlParams.has("registroError")) {
        document.getElementById("menaje-error-registro").style.display = "block";
    }
    
    if (urlParams.has("registroExitoso")) {
        const successDiv = document.getElementById("registro-exitoso");
        successDiv.style.display = "block";
    }
});