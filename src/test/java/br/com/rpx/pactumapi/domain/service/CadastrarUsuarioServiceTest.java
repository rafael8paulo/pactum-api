package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.exception.EmailJaCadastradoException;
import br.com.rpx.pactumapi.domain.model.Usuario;
import br.com.rpx.pactumapi.domain.port.out.BuscarUsuarioPorEmailPort;
import br.com.rpx.pactumapi.domain.port.out.HashSenhaPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarUsuarioPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarUsuarioServiceTest {

    @Mock
    private BuscarUsuarioPorEmailPort buscarPort;
    @Mock
    private SalvarUsuarioPort salvarPort;
    @Mock
    private HashSenhaPort hashSenhaPort;

    @InjectMocks
    private CadastrarUsuarioService service;

    private Usuario usuarioBase;

    @BeforeEach
    void setUp() {
        usuarioBase = new Usuario(null, "Rafael", "rafael@email.com", "senha123");
    }

    @Test
    void deve_cadastrar_usuario_quando_email_nao_existe() {
        when(buscarPort.buscarPorEmail("rafael@email.com")).thenReturn(Optional.empty());
        when(hashSenhaPort.hash("senha123")).thenReturn("$2a$10$hash");
        Usuario salvo = new Usuario(UUID.randomUUID(), "Rafael", "rafael@email.com", "$2a$10$hash");
        when(salvarPort.salvar(any())).thenReturn(salvo);

        Usuario resultado = service.cadastrar(usuarioBase);

        assertThat(resultado.id()).isNotNull();
        assertThat(resultado.email()).isEqualTo("rafael@email.com");
        verify(salvarPort).salvar(any());
    }

    @Test
    void deve_lancar_excecao_quando_email_ja_cadastrado() {
        when(buscarPort.buscarPorEmail("rafael@email.com"))
                .thenReturn(Optional.of(usuarioBase));

        assertThatThrownBy(() -> service.cadastrar(usuarioBase))
                .isInstanceOf(EmailJaCadastradoException.class)
                .hasMessageContaining("rafael@email.com");
    }

    @Test
    void deve_persistir_senha_como_hash() {
        when(buscarPort.buscarPorEmail(anyString())).thenReturn(Optional.empty());
        when(hashSenhaPort.hash("senha123")).thenReturn("$2a$10$bcryptHash");
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        service.cadastrar(usuarioBase);

        verify(hashSenhaPort).hash("senha123");
        verify(salvarPort).salvar(argThat(u -> u.senha().startsWith("$2a$10$")));
    }

    private static <T> T argThat(java.util.function.Predicate<T> pred) {
        return org.mockito.ArgumentMatchers.argThat(pred::test);
    }
}
