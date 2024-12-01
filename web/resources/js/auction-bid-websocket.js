// auction-bid-websocket.js

document.addEventListener('DOMContentLoaded', function() {
    // Get necessary DOM elements
    const bidAmountInput = document.getElementById('bid-amount');
    const placeBidButton = document.getElementById('place-bid-btn');
    const currentBidDisplay = document.getElementById('current-bid');
    const bidHint = document.getElementById('bid-hint');
    const bidsList = document.getElementById('bids-list');

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

    function initBidWebSocket() {
        // Establish WebSocket connection specifically for bidding
        bidSocket = new WebSocket('ws://localhost:8080/SubastasFront/bidSocket');

        bidSocket.onopen = function(event) {
            console.log('Bid WebSocket connection established');
        };

        bidSocket.onmessage = function(event) {
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

        bidSocket.onclose = function(event) {
            console.log('Bid WebSocket connection closed');
            // Attempt to reconnect after 5 seconds
            setTimeout(initBidWebSocket, 5000);
        };

        bidSocket.onerror = function(error) {
            console.error('Bid WebSocket error:', error);
        };
    }

    function handleNewBid(bidMessage) {
        // Only process bids for the current auction
        if (bidMessage.subastaId != auctionId) return;

        // Update current bid display
        currentBidDisplay.textContent = `$ ${bidMessage.monto.toFixed(2)}`;
        
        // Update bid hint
        bidHint.textContent = `Minimum bid: $ ${bidMessage.monto.toFixed(2)}`;
        
        // Update bid amount input's min attribute
        bidAmountInput.min = bidMessage.monto;
        
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
        // Show error message to user
        alert(errorMessage.message);
    }

    function placeBid() {
        // Validate bid amount
        const bidAmount = parseFloat(bidAmountInput.value);
        const currentMinBid = parseFloat(currentBidDisplay.textContent.replace('$', '').trim());

        if (isNaN(bidAmount)) {
            alert('Please enter a valid bid amount');
            return;
        }

        if (bidAmount <= currentMinBid) {
            alert(`Bid must be higher than the current bid of $ ${currentMinBid}`);
            return;
        }

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
        } else {
            alert('WebSocket connection is not open. Please try again.');
        }
    }

    // Add event listener to place bid button
    placeBidButton.addEventListener('click', placeBid);

    // Initialize WebSocket connection
    initBidWebSocket();
});