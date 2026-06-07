package br.com.rpx.pactumapi.config.security;

import br.com.rpx.pactumapi.domain.model.Usuario;
import br.com.rpx.pactumapi.domain.port.out.BuscarUsuarioPorEmailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PactumUserDetailsService implements UserDetailsService {

    private final BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = buscarUsuarioPorEmailPort.buscarPorEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return User.withUsername(usuario.email())
                .password(usuario.senha())
                .roles("USER")
                .build();
    }
}
