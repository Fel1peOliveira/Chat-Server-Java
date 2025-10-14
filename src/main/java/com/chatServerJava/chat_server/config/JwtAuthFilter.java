package com.chatServerJava.chat_server.config;

import com.chatServerJava.chat_server.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Marca esta classe como um bean do Spring, para que possamos injetá-la
@RequiredArgsConstructor // Cria um construtor com os campos 'final'
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extrair o cabeçalho de autorização
        final String authHeader = request.getHeader("Authorization");

        // 2. Verificar se o cabeçalho existe e se começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Se não tiver o token, passa para o próximo filtro
            return;
        }

        // 3. Extrair o token JWT (removendo o prefixo "Bearer ")
        final String jwt = authHeader.substring(7);

        // 4. Extrair o nome de usuário do token
        final String username = jwtService.extractUsername(jwt);

        // 5. Validar o token e verificar se o usuário ainda não está autenticado
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Carrega os detalhes do usuário do banco de dados
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Verifica se o token é válido para este usuário
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Se o token for válido, cria um objeto de autenticação
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credenciais são nulas pois estamos usando JWT
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // ATUALIZA O CONTEXTO DE SEGURANÇA
                // Esta é a linha que informa ao Spring Security que o usuário atual está autenticado
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Passa a requisição para o próximo filtro na cadeia
        filterChain.doFilter(request, response);
    }
}