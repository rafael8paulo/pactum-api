package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.exception.FormaPagamentoNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.FormaPagamento;
import br.com.rpx.pactumapi.domain.model.TipoFormaPagamento;
import br.com.rpx.pactumapi.domain.port.in.CadastrarFormaPagamentoUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarFormaPagamentoUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarFormasPagamentoUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverFormaPagamentoUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = FormaPagamentoController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class FormaPagamentoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CadastrarFormaPagamentoUseCase cadastrarUseCase;
    @MockitoBean ListarFormasPagamentoUseCase listarUseCase;
    @MockitoBean EditarFormaPagamentoUseCase editarUseCase;
    @MockitoBean RemoverFormaPagamentoUseCase removerUseCase;
    @MockitoBean UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    private static final UUID ID = UUID.randomUUID();
    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final FormaPagamento FORMA = new FormaPagamento(ID, "Nubank", TipoFormaPagamento.CARTAO_CREDITO, 25, USUARIO_ID);

    private static final String PAYLOAD_VALIDO = """
            {
              "nome": "Nubank",
              "tipo": "CARTAO_CREDITO",
              "diaFechamentoFatura": 25
            }
            """;

    @BeforeEach
    void setup() {
        when(usuarioAutenticadoResolver.getUsuarioId()).thenReturn(USUARIO_ID);
    }

    @Test
    void deve_retornar201_ao_cadastrar_forma_de_pagamento() throws Exception {
        when(cadastrarUseCase.cadastrar(any(), any())).thenReturn(FORMA);

        mockMvc.perform(post("/api/v1/formas-pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void deve_retornar422_ao_cadastrar_sem_nome() throws Exception {
        mockMvc.perform(post("/api/v1/formas-pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"CARTAO_CREDITO\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deve_retornar200_ao_listar_formas_de_pagamento() throws Exception {
        when(listarUseCase.listar(any())).thenReturn(List.of(FORMA));

        mockMvc.perform(get("/api/v1/formas-pagamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formasPagamento[0].id").value(ID.toString()));
    }

    @Test
    void deve_retornar200_ao_editar_forma_de_pagamento() throws Exception {
        when(editarUseCase.editar(eq(ID), any(), any())).thenReturn(FORMA);

        mockMvc.perform(put("/api/v1/formas-pagamento/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    void deve_retornar404_ao_editar_forma_inexistente() throws Exception {
        when(editarUseCase.editar(eq(ID), any(), any()))
                .thenThrow(new FormaPagamentoNaoEncontradaException(ID));

        mockMvc.perform(put("/api/v1/formas-pagamento/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void deve_retornar204_ao_remover_forma_de_pagamento() throws Exception {
        mockMvc.perform(delete("/api/v1/formas-pagamento/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deve_retornar404_ao_remover_forma_inexistente() throws Exception {
        doThrow(new FormaPagamentoNaoEncontradaException(ID)).when(removerUseCase).remover(eq(ID), any());

        mockMvc.perform(delete("/api/v1/formas-pagamento/{id}", ID))
                .andExpect(status().isNotFound());
    }
}
