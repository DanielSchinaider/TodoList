package br.com.danieloliveira.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.danieloliveira.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository iUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var servletPath = request.getServletPath();
        if(servletPath.startsWith("/tasks/")) {
            var authorization = request.getHeader("Authorization");
            System.out.println("Authorization");
            System.out.println(authorization);

            var authEncoded = authorization.substring("Basic".length()).trim();

            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);

            var authString = new String(authDecoded);
            System.out.println("Auth");
            System.out.println(authDecoded);
            String[] credential = authString.split(":");
            String username = credential[0];
            String password = credential[1];

            var user = this.iUserRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401, "Usuario sem autorizacao");
            } else {
                var passVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (passVerify.verified) {
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401, "Usuario sem autorizacao");
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
