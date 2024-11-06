// login.js
document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has("error")) {
        const errorDiv = document.createElement("p");
        errorDiv.style.color = "red";
        errorDiv.textContent = "Credenciales incorrectas, intente de nuevo.";
        document.querySelector(".form").insertBefore(errorDiv, document.querySelector(".form").firstChild);
    }
});
