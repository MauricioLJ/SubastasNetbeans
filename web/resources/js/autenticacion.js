// Global elements that need to be accessed before DOMContentLoaded
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
    // Get all DOM elements first
    const correoInput = document.getElementById('correo');
    const claveInput = document.getElementById('clave');
    const loginBtn = document.getElementById('loginBtn');
    const nombreInput = document.getElementById('nombre');
    const apellidosInput = document.getElementById('apellidos');
    const emailInput = document.getElementById('email');
    const passInput = document.getElementById('pass');
    const signupBtn = document.getElementById('signupBtn');
    const emailStatus = document.getElementById('email-status');

    // Variable to store email verification timeout and status
    let timeoutId = null;
    let isEmailAvailable = false;

    // Validation functions
    function validateEmail(email) {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    }

    function validateLoginButton() {
        const correoValido = correoInput.value.trim() !== '';
        const claveValida = claveInput.value.length >= 8;
        loginBtn.disabled = !(correoValido && claveValida);
    }

    function validateSignupButton() {
        const nombreValido = nombreInput.value.trim() !== '';
        const apellidosValido = apellidosInput.value.trim() !== '';
        const emailValido = validateEmail(emailInput.value);
        const passValida = passInput.value.length >= 8;

        // Check if email is available or if the field is empty
        signupBtn.disabled = !(nombreValido && apellidosValido && emailValido && passValida &&
                (isEmailAvailable || emailInput.value.trim() === ''));
    }

    async function checkEmail(email) {
        try {
            const response = await fetch(`verificar-email?email=${encodeURIComponent(email)}`);
            const data = await response.json();

            if (data.exists) {
                emailStatus.textContent = 'Este email ya está registrado';
                emailStatus.className = 'email-status error';
                isEmailAvailable = false;
            } else {
                emailStatus.textContent = 'Email disponible';
                emailStatus.className = 'email-status success';
                isEmailAvailable = true;
            }
            validateSignupButton();
        } catch (error) {
            emailStatus.textContent = 'Error al verificar email';
            emailStatus.className = 'email-status error';
            isEmailAvailable = false;
            console.error('Error:', error);
            validateSignupButton();
        }
    }

    // Event Listeners
    correoInput.addEventListener('input', validateLoginButton);
    claveInput.addEventListener('input', validateLoginButton);

    nombreInput.addEventListener('input', validateSignupButton);
    apellidosInput.addEventListener('input', validateSignupButton);
    passInput.addEventListener('input', validateSignupButton);

    emailInput.addEventListener('input', function () {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }

        const email = this.value.trim();

        if (!email) {
            emailStatus.textContent = '';
            emailStatus.className = 'email-status';
            isEmailAvailable = false;
            validateSignupButton();
            return;
        }

        if (!validateEmail(email)) {
            emailStatus.textContent = 'Email inválido';
            emailStatus.className = 'email-status error';
            isEmailAvailable = false;
            validateSignupButton();
            return;
        }

        emailStatus.textContent = 'Verificando...';
        emailStatus.className = 'email-status email-checking';

        timeoutId = setTimeout(() => {
            checkEmail(email);
        }, 500);
    });

    // Check URL parameters for messages
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has("error")) {
        document.getElementById("error-message").style.display = "block";
    }
    if (urlParams.has("registroError")) {
        document.getElementById("menaje-error-registro").style.display = "block";
    }
    if (urlParams.has("registroExitoso")) {
        document.getElementById("registro-exitoso").style.display = "block";
    }

    // Initial validation
    validateLoginButton();
    validateSignupButton();
});