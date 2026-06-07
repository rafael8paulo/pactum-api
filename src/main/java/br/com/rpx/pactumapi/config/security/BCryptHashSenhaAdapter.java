package br.com.rpx.pactumapi.config.security;

import br.com.rpx.pactumapi.domain.port.out.HashSenhaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptHashSenhaAdapter implements HashSenhaPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hash(String senha) {
        return passwordEncoder.encode(senha);
    }
}
