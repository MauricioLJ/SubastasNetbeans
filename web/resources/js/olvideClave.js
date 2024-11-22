document.addEventListener("DOMContentLoaded", function () {
    // Página de "Enviar correo recuperacion"
    const emailInput = document.getElementById('email');
    const submitBtn = document.getElementById('submitBtn');

    if (emailInput && submitBtn) {
        // Validar email
        function validateEmail(email) {
            const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return regex.test(email);
        }

        function validateEmailButton() {
            const emailValido = validateEmail(emailInput.value);
            submitBtn.disabled = !emailValido;
        }

        emailInput.addEventListener('input', validateEmailButton);
        validateEmailButton();
    }

    // Página de "Cambiar Contraseña"
    const passInput = document.getElementById('nuevaClave');
    const newPassBtn = document.getElementById('nuevaClaveBtn');

    if (passInput && newPassBtn) {
        // Validar contraseña
        function validatePasswordButton() {
            const isPassValid = passInput.value.length >= 8;
            newPassBtn.disabled = !isPassValid;
        }

        passInput.addEventListener('input', validatePasswordButton);
        validatePasswordButton();
    }
    
});
