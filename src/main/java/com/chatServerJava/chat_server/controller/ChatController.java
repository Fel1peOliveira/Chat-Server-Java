package com.chatServerJava.chat_server.controller;

import com.chatServerJava.chat_server.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller

public class ChatController {
    @MessageMapping("/chat.sendMessage")//Cliente envia para /app/chat.sendMessage
    @SendTo("/topic/public")//Mensagem é transmitida para todos os inscritos

    public ChatMessage sendMessage(@Payload ChatMessage chatMessage,Principal principal) {
        //LEMBRAR IMPLEMENTAÇÃO DO PRINCIPAL.
        return new ChatMessage(chatMessage.content(),chatMessage.sender());
    }
}