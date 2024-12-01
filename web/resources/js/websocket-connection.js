// websocket-connection.js
let socket = null;

function initWebSocket() {
    // Ensure we have a user before establishing connection
    const userIdElement = document.getElementById('currentUserId');
    if (!userIdElement)
        return;

    const userId = userIdElement.value;
    if (!userId)
        return;

    // Establish WebSocket connection
    socket = new WebSocket('ws://localhost:8080/SubastasFront/userConnection');

    socket.onopen = function (event) {
        console.log('WebSocket connection established');
        
        // Optional: Send user ID or additional connection info
        socket.send(JSON.stringify({
            type: 'connect',
            userId: userId
        }));
    };

    socket.onmessage = function (event) {
        console.log('Received WebSocket message:', event.data);

        try {
            const message = JSON.parse(event.data);
            // Handle different types of messages
            switch (message.type) {
                case 'userConnection':
                    handleUserConnection(message);
                    break;
                case 'newAuction':
                    handleNewAuction(message);
                    break;
                    // Add more message type handlers as needed
            }
        } catch (error) {
            console.error('Error parsing message:', error);
        }
    };

    socket.onclose = function (event) {
        console.log('WebSocket connection closed');
        // Attempt to reconnect after 5 seconds
        setTimeout(initWebSocket, 5000);
    };

    socket.onerror = function (error) {
        console.error('WebSocket error:', error);
    };
}

function handleUserConnection(message) {
    // Handle user connection/disconnection notifications
    console.log(message.connected ? 'User connected:' : 'User disconnected:', message.userId);
}

function handleNewAuction(message) {
    // Handle new auction notifications
    console.log('New auction:', message.auctionDetails);
}

// Initialize WebSocket when the page loads
document.addEventListener('DOMContentLoaded', initWebSocket);