package br.com.rpx.pactumapi.domain.exception;

public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String email) {
        super("Email já cadastrado: " + email);
    }
}
