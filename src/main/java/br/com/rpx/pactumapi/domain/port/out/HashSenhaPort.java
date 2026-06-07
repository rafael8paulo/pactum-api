package br.com.rpx.pactumapi.domain.port.out;

public interface HashSenhaPort {
    String hash(String senha);
}
