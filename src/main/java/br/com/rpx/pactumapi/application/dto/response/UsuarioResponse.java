package br.com.rpx.pactumapi.application.dto.response;

import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email) {}
