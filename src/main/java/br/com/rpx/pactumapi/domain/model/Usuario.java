package br.com.rpx.pactumapi.domain.model;

import java.util.UUID;

public record Usuario(
        UUID id,
        String nome,
        String email,
        String senha
) {}
