package com.reservation.reservation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsServiceImpl userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Cała twoja dotychczasowa logika jest teraz w bloku try
            String jwt = parseJwt(request);
            if (jwt != null && jwtTokenService.validateToken(jwt)) {
                String email = jwtTokenService.getEmailFromToken(jwt);

                // Ładujemy użytkownika z bazy na podstawie emaila z tokenu
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Tworzymy obiekt autentykacji
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Ustawiamy zalogowanego użytkownika w kontekście Springa
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Przekaż żądanie dalej *tylko* jeśli nie było błędu

            }
            filterChain.doFilter(request, response);


        } catch (Exception e) {
            // === PUŁAPKA NA BŁĄD ===
            // Jeśli cokolwiek w bloku TRY zawiedzie (np. jwtTokenService.validateToken),
            // złapiemy błąd tutaj, zamiast pozwalać na pustą odpowiedź 200 OK.

            System.err.println("!!! KRYTYCZNY BŁĄD W JwtRequestFilter !!!");
            e.printStackTrace(); // Wypisz pełny błąd w konsoli IntelliJ

            // Zwróć błąd 500 bezpośrednio do Postmana
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json; charset=UTF-8"); // Ustaw kodowanie

            // Stwórz czytelny JSON z błędem
            String errorMessage = "{\"error\": \"Krytyczny błąd w filtrze JWT\", \"message\": \"" + e.getMessage() + "\"}";
            response.getWriter().write(errorMessage);

            // WAŻNE: Nie wywołujemy filterChain.doFilter(request, response)
            // Przerywamy łańcuch filtrów tutaj.
            return;
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if(StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}



















