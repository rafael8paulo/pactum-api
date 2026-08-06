package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.exception.ContaRecorrenteNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import br.com.rpx.pactumapi.domain.port.in.AtualizarStatusContaRecorrenteUseCase;
import br.com.rpx.pactumapi.domain.port.in.CadastrarContaRecorrenteUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarContaRecorrenteUseCase;
import br.com.rpx.pactumapi.domain.port.in.GerarLancamentosRecorrentesUseCase;
import br.com.rpx.pactumapi.domain.port.in.GerarLoteLancamentosRecorrentesUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarContasRecorrentesUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverContaRecorrenteUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ContaRecorrenteController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ContaRecorrenteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CadastrarContaRecorrenteUseCase cadastrarUseCase;
    @MockitoBean ListarContasRecorrentesUseCase listarUseCase;
    @MockitoBean EditarContaRecorrenteUseCase editarUseCase;
    @MockitoBean AtualizarStatusContaRecorrenteUseCase atualizarStatusUseCase;
    @MockitoBean RemoverContaRecorrenteUseCase removerUseCase;
    @MockitoBean GerarLancamentosRecorrentesUseCase gerarLancamentosUseCase;
    @MockitoBean GerarLoteLancamentosRecorrentesUseCase gerarLoteLancamentosUseCase;
    @MockitoBean UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    private static final UUID ID = UUID.randomUUID();
    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final ContaRecorrente CONTA = new ContaRecorrente(ID, "Financiamento do Carro", new BigDecimal("1335.50"),
            CategoriaDespesa.FINANCIAMENTO, 10, YearMonth.of(2026, 1), null, StatusContaRecorrente.ATIVA, USUARIO_ID);

    private static final String PAYLOAD_VALIDO = """
            {
              "descricao": "Financiamento do Carro",
              "valorPadrao": 1335.50,
              "categoria": "FINANCIAMENTO",
              "diaVencimento": 10,
              "competenciaInicio": "2026-01"
            }
            """;

    @BeforeEach
    void setup() {
        when(usuarioAutenticadoResolver.getUsuarioId()).thenReturn(USUARIO_ID);
    }

    @Test
    void deve_retornar201_ao_cadastrar_conta_recorrente() throws Exception {
        when(cadastrarUseCase.cadastrar(any(), any())).thenReturn(CONTA);

        mockMvc.perform(post("/api/v1/contas-recorrentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @Test
    void deve_retornar422_ao_cadastrar_com_payload_invalido() throws Exception {
        mockMvc.perform(post("/api/v1/contas-recorrentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"\",\"valorPadrao\":0}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void deve_retornar200_ao_listar_contas_recorrentes() throws Exception {
        when(listarUseCase.listar(any(), any())).thenReturn(List.of(CONTA));

        mockMvc.perform(get("/api/v1/contas-recorrentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contasRecorrentes[0].id").value(ID.toString()));
    }

    @Test
    void deve_retornar200_ao_listar_filtrando_por_status() throws Exception {
        when(listarUseCase.listar(eq(StatusContaRecorrente.ATIVA), any())).thenReturn(List.of(CONTA));

        mockMvc.perform(get("/api/v1/contas-recorrentes").param("status", "ATIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contasRecorrentes[0].status").value("ATIVA"));
    }

    @Test
    void deve_retornar200_ao_editar_conta_recorrente() throws Exception {
        when(editarUseCase.editar(eq(ID), any(), any())).thenReturn(CONTA);

        mockMvc.perform(put("/api/v1/contas-recorrentes/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void deve_retornar404_ao_editar_conta_inexistente() throws Exception {
        when(editarUseCase.editar(eq(ID), any(), any()))
                .thenThrow(new ContaRecorrenteNaoEncontradaException(ID));

        mockMvc.perform(put("/api/v1/contas-recorrentes/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deve_retornar200_ao_atualizar_status() throws Exception {
        when(atualizarStatusUseCase.atualizar(eq(ID), any(), any())).thenReturn(CONTA);

        mockMvc.perform(patch("/api/v1/contas-recorrentes/{id}/status", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAUSADA\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deve_retornar404_ao_atualizar_status_de_conta_inexistente() throws Exception {
        when(atualizarStatusUseCase.atualizar(eq(ID), any(), any()))
                .thenThrow(new ContaRecorrenteNaoEncontradaException(ID));

        mockMvc.perform(patch("/api/v1/contas-recorrentes/{id}/status", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAUSADA\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deve_retornar204_ao_remover_conta_recorrente() throws Exception {
        mockMvc.perform(delete("/api/v1/contas-recorrentes/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deve_retornar404_ao_remover_conta_inexistente() throws Exception {
        doThrow(new ContaRecorrenteNaoEncontradaException(ID)).when(removerUseCase).remover(eq(ID), any());

        mockMvc.perform(delete("/api/v1/contas-recorrentes/{id}", ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void deve_retornar200_com_despesas_geradas_ao_chamar_gerar() throws Exception {
        Despesa gerada = new Despesa(UUID.randomUUID(), "Financiamento do Carro", new BigDecimal("1335.50"),
                StatusDespesa.PENDENTE, YearMonth.of(2026, 8), CategoriaDespesa.FINANCIAMENTO, USUARIO_ID, ID);
        when(gerarLancamentosUseCase.gerar(eq(YearMonth.of(2026, 8)), any())).thenReturn(List.of(gerada));

        mockMvc.perform(post("/api/v1/contas-recorrentes/gerar").param("competencia", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.despesas[0].contaRecorrenteId").value(ID.toString()));
    }

    @Test
    void deve_retornar400_quando_competencia_ausente_ao_gerar() throws Exception {
        mockMvc.perform(post("/api/v1/contas-recorrentes/gerar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deve_retornar400_quando_competencia_invalida_ao_gerar() throws Exception {
        mockMvc.perform(post("/api/v1/contas-recorrentes/gerar").param("competencia", "agosto-2026"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deve_retornar200_com_despesas_geradas_ao_chamar_gerar_todos() throws Exception {
        Despesa gerada = new Despesa(UUID.randomUUID(), "Financiamento do Carro", new BigDecimal("1335.50"),
                StatusDespesa.PENDENTE, YearMonth.of(2026, 8), CategoriaDespesa.FINANCIAMENTO, USUARIO_ID, ID);
        when(gerarLoteLancamentosUseCase.gerarTodos(eq(ID), any())).thenReturn(List.of(gerada));

        mockMvc.perform(post("/api/v1/contas-recorrentes/{id}/gerar-todos", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.despesas[0].contaRecorrenteId").value(ID.toString()));
    }

    @Test
    void deve_retornar200_com_lista_vazia_ao_chamar_gerar_todos_para_conta_pausada() throws Exception {
        when(gerarLoteLancamentosUseCase.gerarTodos(eq(ID), any())).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/contas-recorrentes/{id}/gerar-todos", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.despesas").isEmpty());
    }

    @Test
    void deve_retornar404_ao_chamar_gerar_todos_para_conta_inexistente() throws Exception {
        when(gerarLoteLancamentosUseCase.gerarTodos(eq(ID), any()))
                .thenThrow(new ContaRecorrenteNaoEncontradaException(ID));

        mockMvc.perform(post("/api/v1/contas-recorrentes/{id}/gerar-todos", ID))
                .andExpect(status().isNotFound());
    }
}
