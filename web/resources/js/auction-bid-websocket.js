document.addEventListener('DOMContentLoaded', function () {
    // Get modal and modal trigger elements
    const bidModal = new bootstrap.Modal(document.getElementById('bidModal'));
    const openBidModalButton = document.getElementById('open-bid-modal');
    const closeBidModalButtons = document.querySelectorAll('[data-bs-dismiss="modal"]');

    // Get necessary DOM elements
    const bidAmountInput = document.getElementById('bid-amount');
    const placeBidButton = document.getElementById('place-bid-btn');
    const currentBidDisplay = document.getElementById('current-bid');
    const bidsList = document.getElementById('bids-list');

    // Create bid status element
    const bidStatus = document.createElement('div');
    bidStatus.id = 'bid-status';
    bidStatus.className = 'bid-status';
    bidAmountInput.parentNode.insertBefore(bidStatus, bidAmountInput.nextSibling);

    // Get auction and user IDs
    const auctionIdElement = document.getElementById('currentAuctionId');
    const userIdElement = document.getElementById('currentUserId');

    if (!auctionIdElement || !userIdElement) {
        console.error('Required elements not found');
        return;
    }

    const auctionId = auctionIdElement.value;
    const userId = userIdElement.value;

    // WebSocket connection for bidding
    let bidSocket = null;
    let timeoutId = null;
    let isBidValid = false;

    // Open modal event
    openBidModalButton.addEventListener('click', function () {
        bidModal.show();
        // Reset input and validation
        bidAmountInput.value = '';
        validateBidAmount();
    });

    function validateBidAmount() {
        // Clear any previous timeout
        if (timeoutId) {
            clearTimeout(timeoutId);
        }

        const bidAmount = bidAmountInput.value.trim();

        // Obtener el valor actual de la oferta más alta
        const currentBid = parseFloat(currentBidDisplay.textContent.replace('$', '').trim());

        // Check si el valor ingresado es un número válido
        const numericBid = parseFloat(bidAmount);
        if (isNaN(numericBid)) {
            bidStatus.textContent = `Ingrese un monto de puja mayor a $ ${currentBid.toFixed(2)}`;
            bidStatus.className = 'bid-status error';
            isBidValid = false;
            placeBidButton.disabled = true;
            return;
        }

        // Mostrar mensaje de "Validando..."
        bidStatus.textContent = 'Validando...';
        placeBidButton.disabled = true;
        bidStatus.className = 'bid-status bid-checking';

        // Establecer un timeout para simular la validación
        timeoutId = setTimeout(() => {
            // Validar si la puja es mayor al monto actual
             if (numericBid <= currentBid) {
                bidStatus.textContent = `El monto debe ser mayor a $ ${currentBid.toFixed(2)}`;
                bidStatus.className = 'bid-status error';
                isBidValid = false;
                placeBidButton.disabled = true;
            } else {
                bidStatus.textContent = 'Monto de puja válido';
                bidStatus.className = 'bid-status success';
                isBidValid = true;
                placeBidButton.disabled = false;
            }
        }, 500);
    }


    function initBidWebSocket() {
        // Establish WebSocket connection specifically for bidding
        bidSocket = new WebSocket('ws://localhost:8080/SubastasFront/bidSocket');

        bidSocket.onopen = function (event) {
            console.log('Bid WebSocket connection established');
        };

        bidSocket.onmessage = function (event) {
            try {
                const message = JSON.parse(event.data);

                // Handle different types of messages
                switch (message.type) {
                    case 'newBid':
                        handleNewBid(message);
                        break;
                    case 'bidError':
                        handleBidError(message);
                        break;
                }
            } catch (error) {
                console.error('Error parsing message:', error);
            }
        };

        bidSocket.onclose = function (event) {
            console.log('Bid WebSocket connection closed');
            // Attempt to reconnect after 5 seconds
            setTimeout(initBidWebSocket, 5000);
        };

        bidSocket.onerror = function (error) {
            console.error('Bid WebSocket error:', error);
        };
    }

    function handleNewBid(bidMessage) {
        // Only process bids for the current auction
        if (bidMessage.subastaId != auctionId)
            return;

        // Update current bid display
        currentBidDisplay.textContent = `$ ${bidMessage.monto.toFixed(2)}`;

        // Update bid amount input's min attribute
        bidAmountInput.min = bidMessage.monto;

        // Reset bid validation after new bid
        validateBidAmount();

        // Add bid to bids list
        const bidEntry = document.createElement('div');
        bidEntry.classList.add('list-group-item', 'list-group-item-action');
        bidEntry.innerHTML = `
            <div class="d-flex w-100 justify-content-between">
                <h5 class="mb-1">Bid: $ ${bidMessage.monto.toFixed(2)}</h5>
                <small>${new Date(bidMessage.fechaPuja).toLocaleString()}</small>
            </div>
            <p class="mb-1">User ID: ${bidMessage.usuarioId}</p>
        `;
        bidsList.insertBefore(bidEntry, bidsList.firstChild);
    }

    function handleBidError(errorMessage) {
        // Display bid error
        bidStatus.textContent = errorMessage.message || 'Error al realizar la puja';
        bidStatus.className = 'bid-status error';
        isBidValid = false;
        placeBidButton.disabled = true;
    }

    function placeBid() {
        // Validate bid amount before placing
        if (!isBidValid) {
            return;
        }

        const bidAmount = parseFloat(bidAmountInput.value);

        // Prepare bid message
        const bidMessage = {
            type: 'placeBid',
            subastaId: auctionId,
            usuarioId: userId,
            monto: bidAmount
        };

        // Send bid through WebSocket
        if (bidSocket && bidSocket.readyState === WebSocket.OPEN) {
            bidSocket.send(JSON.stringify(bidMessage));
            bidModal.hide(); // Close modal on successful bid send
        } else {
            alert('WebSocket connection is not open. Please try again.');
        }
    }

    // Add event listeners
    bidAmountInput.addEventListener('input', validateBidAmount);
    placeBidButton.addEventListener('click', placeBid);

    // Initially disable place bid button
    placeBidButton.disabled = true;
    validateBidAmount();

    // Initialize WebSocket connection
    initBidWebSocket();
});