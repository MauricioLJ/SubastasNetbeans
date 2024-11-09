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
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has("error")) {
        const errorDiv = document.getElementById("error-message");
        errorDiv.style.display = "block";
    }

    // Mostrar mensaje de error para el registro
    if (urlParams.has("registroError")) {
        const registerErrorDiv = document.getElementById("menaje-error-registro");
        registerErrorDiv.style.display = "block";
        // Activar la vista del registro
        container.classList.add("active");
    }

    if (urlParams.has("registroExitoso")) {
        const successDiv = document.getElementById("registro-exitoso");
        successDiv.style.display = "block";
    }

});

