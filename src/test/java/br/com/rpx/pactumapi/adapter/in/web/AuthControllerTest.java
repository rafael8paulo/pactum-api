package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.application.dto.response.UsuarioResponse;
import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.exception.EmailJaCadastradoException;
import br.com.rpx.pactumapi.domain.model.Usuario;
import br.com.rpx.pactumapi.domain.port.in.CadastrarUsuarioUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarUsuarioPorEmailPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CadastrarUsuarioUseCase cadastrarUseCase;

    @MockitoBean
    private BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort;

    @MockitoBean
    private UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    @Test
    void deve_cadastrar_usuario_e_retornar_201() throws Exception {
        UUID id = UUID.randomUUID();
        when(cadastrarUseCase.cadastrar(any())).thenReturn(
                new Usuario(id, "Rafael", "rafael@email.com", "$2a$10$hash"));

        mockMvc.perform(post("/api/v1/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Rafael","email":"rafael@email.com","senha":"senha123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("rafael@email.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deve_retornar_409_quando_email_duplicado() throws Exception {
        when(cadastrarUseCase.cadastrar(any()))
                .thenThrow(new EmailJaCadastradoException("rafael@email.com"));

        mockMvc.perform(post("/api/v1/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Rafael","email":"rafael@email.com","senha":"senha123"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void deve_retornar_422_quando_campo_ausente() throws Exception {
        mockMvc.perform(post("/api/v1/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Rafael","email":"rafael@email.com"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }
}
