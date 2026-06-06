package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.domain.exception.DespesaNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import br.com.rpx.pactumapi.domain.port.in.AtualizarStatusDespesaUseCase;
import br.com.rpx.pactumapi.domain.port.in.CadastrarDespesaUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarDespesaUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarDespesasUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverDespesaUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

@WebMvcTest(DespesaController.class)
class DespesaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CadastrarDespesaUseCase cadastrarUseCase;
    @MockitoBean ListarDespesasUseCase listarUseCase;
    @MockitoBean AtualizarStatusDespesaUseCase atualizarStatusUseCase;
    @MockitoBean EditarDespesaUseCase editarUseCase;
    @MockitoBean RemoverDespesaUseCase removerUseCase;

    private static final UUID ID = UUID.randomUUID();
    private static final Despesa DESPESA = new Despesa(ID, "Financiamento", new BigDecimal("1335.50"),
            StatusDespesa.PAGA, YearMonth.of(2025, 7), CategoriaDespesa.FINANCIAMENTO);

    private static final String PAYLOAD_VALIDO = """
            {
              "descricao": "Financiamento do Carro",
              "valor": 1335.50,
              "status": "PAGA",
              "competencia": "2025-07",
              "categoria": "FINANCIAMENTO"
            }
            """;

    @Test
    void deve_retornar201_ao_cadastrar_despesa() throws Exception {
        when(cadastrarUseCase.cadastrar(any())).thenReturn(DESPESA);

        mockMvc.perform(post("/api/v1/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.descricao").value("Financiamento"));
    }

    @Test
    void deve_retornar422_ao_cadastrar_com_payload_invalido() throws Exception {
        mockMvc.perform(post("/api/v1/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"\",\"valor\":-1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void deve_retornar200_ao_listar_despesas() throws Exception {
        when(listarUseCase.listar(any(), any(), any())).thenReturn(List.of(DESPESA));

        mockMvc.perform(get("/api/v1/despesas").param("competencia", "2025-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.despesas[0].id").value(ID.toString()))
                .andExpect(jsonPath("$.total").value(1335.50));
    }

    @Test
    void deve_retornar200_ao_atualizar_status() throws Exception {
        when(atualizarStatusUseCase.atualizar(eq(ID), any())).thenReturn(DESPESA);

        mockMvc.perform(patch("/api/v1/despesas/{id}/status", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAGA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGA"));
    }

    @Test
    void deve_retornar404_ao_atualizar_status_de_despesa_inexistente() throws Exception {
        when(atualizarStatusUseCase.atualizar(eq(ID), any()))
                .thenThrow(new DespesaNaoEncontradaException(ID));

        mockMvc.perform(patch("/api/v1/despesas/{id}/status", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAGA\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deve_retornar200_ao_editar_despesa() throws Exception {
        when(editarUseCase.editar(eq(ID), any())).thenReturn(DESPESA);

        mockMvc.perform(put("/api/v1/despesas/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void deve_retornar204_ao_remover_despesa() throws Exception {
        mockMvc.perform(delete("/api/v1/despesas/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deve_retornar404_ao_remover_despesa_inexistente() throws Exception {
        doThrow(new DespesaNaoEncontradaException(ID)).when(removerUseCase).remover(ID);

        mockMvc.perform(delete("/api/v1/despesas/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
