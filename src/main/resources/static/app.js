const { jsx } = require("react/jsx-runtime");

let stompClient = null;
let username = null;
let jwtToken = null;

async function apiCall(endpoint,body){
    const response = await fetch(`/api/auth/${endpoint}`,{
        method:'POST',
        headers:{'Content-type':'application/json'},
        body:JSON.stringify(body)
    });
    if(!response.ok) throw new Error('Authentication failed');
    return response.json();
}

async function register() {
    username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').vale.trim();
    if(username && password){
        try{
            const data = apiCall('register',{username,password});
            jwtToken = data.token;
            connect();
        }catch(err){
            console.error('Registration failed:',err)
        }
    }
}

async function login(){
        username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').vale.trim();
    if(username && password){
        try{
            const data = apiCall('login',{username,password});
            jwtToken = data.token;
            connect();
        }catch(err){
            console.error('Login failed:',err)
        }
    }
}

function connect(){
    document.getElementById('login-page').style.display='none';
    document.getElementById('chat-page').style.display='block';

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    //Header crucial para a segurança da WS
    const headers ={
        // 'Authorization':`Bearer ${jwtToken}` // Aqui q passa o token
    };

    stompClient.connect(headers,function(frame){
        console.log('Connected: '+ frame);
        stompClient.subscribe('/topic/public', function (message){
            showMessage(JSON.parse(message.body))
        });
    });
}

function sendMessage(){
    const messageContent = document.getElementById('message').value.trim();
    if(message && stompClient){
        const chatMessage ={
            sender: username,
            content:messageContent
        };
        stompClient.send('/app/chat.sendMessage',{},JSON.stringify(chatMessage));
        document.getElementById('message').value ='';
    }
}

function showMessage(message){
    const messagesDiv = document.getElementById('messages');
    const messageElement = document.createElement('p');
    messageElement.appendChild(document.createTextNode(`${message.sender}:${message.content}`));
    messagesDiv.appendChild(messageElement);
}