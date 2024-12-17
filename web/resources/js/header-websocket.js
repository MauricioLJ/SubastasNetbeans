document.addEventListener('DOMContentLoaded', function () {
    const userIdElement = document.getElementById('currentUserId');
    if (!userIdElement) {
        console.error('Usuario no encontrado en el header');
        return;
    }

    const userId = userIdElement.value;
    let notificationSocket = null;

    let notificationContainer = null;
    let notificationBadge = null;

    const observer = new MutationObserver(() => {
        notificationContainer = document.getElementById('notification-container');
        notificationBadge = document.getElementById('notification-badge');
        if (notificationContainer && notificationBadge) {
            console.log('Contenedor de notificaciones detectado');
            observer.disconnect();
            initNotificationWebSocket();
        }
    });

    observer.observe(document.body, {childList: true, subtree: true});

    function initNotificationWebSocket() {
        const wsUrl = `ws://${window.location.host}/SubastasFront/notificationSocket/${userId}`;
        console.log('Conectando WebSocket del header:', wsUrl);

        notificationSocket = new WebSocket(wsUrl);

        notificationSocket.onopen = function () {
            console.log('WebSocket del header conectado');
        };

        notificationSocket.onmessage = function (event) {
            try {
                const message = JSON.parse(event.data);
                console.log('Notificación recibida en el header:', message);
                if (message.type === 'notification') {
                    handleNotification(message);
                }
            } catch (error) {
                console.error('Error al manejar la notificación del header:', error);
            }
        };

        notificationSocket.onclose = function () {
            console.log('WebSocket del header cerrado. Reconectando...');
            setTimeout(initNotificationWebSocket, 5000);
        };

        notificationSocket.onerror = function (error) {
            console.error('Error en WebSocket del header:', error);
        };
    }

    function handleNotification(notification) {
        const notificationsList = document.getElementById('notifications-list');
        if (!notificationsList) {
            console.error('Lista de notificaciones no encontrada');
            return;
        }

        const notificationElement = document.createElement('li');
        notificationElement.classList.add('dropdown-item');

        let messageContent = '';
        switch (notification.tipoNotificacion) {
            case 'PUJA':
                messageContent = `
                <div>
                    <strong>Subasta: </strong> ${notification.subastaNombre} <br>
                    <strong>Monto de la puja: </strong> $${notification.montoPuja.toFixed(2)} <br>
                    <small class="text-white">${notification.mensaje}</small>
                </div>
            `;
                break;
            case 'SUBASTA':
                messageContent = `
                <div>
                    <strong>Subasta: </strong> ${notification.subastaNombre} <br>
                    <small class="text-white">${notification.mensaje}</small>
                </div>
            `;
                break;
            case 'MENSAJE':
                messageContent = `
                <div>
                    <strong>Mensaje: </strong> ${notification.mensaje}
                </div>
            `;
                break;
            default:
                messageContent = `
                <div>
                    ${notification.mensaje}
                </div>
            `;
        }

        // Estructura de la notificación con botón de eliminar
        notificationElement.innerHTML = `
        <div class="d-flex justify-content-between align-items-center w-100">
            ${messageContent}
            <button class="btn btn-sm btn-outline-danger ms-2" onclick="markNotificationAsRead(${notification.idNotificacion})" title="Eliminar notificación">
                <i class="fa fa-times"></i>
            </button>
        </div>
    `;

        // Insertar la notificación al inicio
        notificationsList.prepend(notificationElement);
        updateNotificationBadge();
    }

    function updateNotificationBadge() {
        const notificationsList = document.getElementById('notifications-list');
        const unreadCount = notificationsList.querySelectorAll('.dropdown-item').length;

        notificationBadge.textContent = unreadCount;
        notificationBadge.style.display = unreadCount > 0 ? 'inline-block' : 'none';

        // Manejar mensaje "No hay notificaciones nuevas"
        if (unreadCount === 0) {
            notificationsList.innerHTML = '<li class="dropdown-item no-notifications">No hay notificaciones nuevas</li>';
        }
    }



    window.markNotificationAsRead = function (notificationId) {
        if (notificationSocket && notificationSocket.readyState === WebSocket.OPEN) {
            const readMessage = {
                type: 'markAsRead',
                notificationId: notificationId
            };
            notificationSocket.send(JSON.stringify(readMessage));
        }

        const notificationItem = document.querySelector(`.dropdown-item button[onclick="markNotificationAsRead(${notificationId})"]`);
        if (notificationItem) {
            notificationItem.closest('.dropdown-item').remove();
        }

        updateNotificationBadge();
    };

    const notificationDropdown = document.getElementById('notificationDropdown');
    notificationDropdown.addEventListener('click', () => {
        const readMessage = {
            type: 'markAllAsRead'
        };
        notificationSocket.send(JSON.stringify(readMessage));
    });


});
