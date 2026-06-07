package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.model.Patrimonio;
import br.com.rpx.pactumapi.domain.port.in.CadastrarPatrimonioUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarPatrimonioUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PatrimonioController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class PatrimonioControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CadastrarPatrimonioUseCase cadastrarUseCase;
    @MockitoBean ConsultarPatrimonioUseCase consultarUseCase;
    @MockitoBean UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    private static final UUID ID = UUID.randomUUID();
    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final Patrimonio PATRIMONIO = new Patrimonio(ID, "Caixinha Turbo Nubank",
            new BigDecimal("5069.02"), YearMonth.of(2025, 7), USUARIO_ID);

    private static final String PAYLOAD_VALIDO = """
            {
              "descricao": "Caixinha Turbo Nubank",
              "valor": 5069.02,
              "competencia": "2025-07"
            }
            """;

    @BeforeEach
    void setup() {
        when(usuarioAutenticadoResolver.getUsuarioId()).thenReturn(USUARIO_ID);
    }

    @Test
    void deve_retornar201_ao_cadastrar_patrimonio() throws Exception {
        when(cadastrarUseCase.cadastrar(any(), any())).thenReturn(PATRIMONIO);

        mockMvc.perform(post("/api/v1/patrimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.descricao").value("Caixinha Turbo Nubank"));
    }

    @Test
    void deve_retornar422_ao_cadastrar_com_payload_invalido() throws Exception {
        mockMvc.perform(post("/api/v1/patrimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"\",\"valor\":-1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void deve_retornar200_ao_consultar_patrimonio() throws Exception {
        when(consultarUseCase.consultar(any(), any())).thenReturn(List.of(PATRIMONIO));

        mockMvc.perform(get("/api/v1/patrimonio").param("competencia", "2025-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].id").value(ID.toString()))
                .andExpect(jsonPath("$.totalPatrimonio").value(5069.02));
    }
}
