package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.domain.exception.ReceitaNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import br.com.rpx.pactumapi.domain.model.Receita;
import br.com.rpx.pactumapi.domain.port.in.CadastrarReceitaUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarReceitaUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarReceitasUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverReceitaUseCase;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceitaController.class)
class ReceitaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CadastrarReceitaUseCase cadastrarUseCase;
    @MockitoBean ListarReceitasUseCase listarUseCase;
    @MockitoBean EditarReceitaUseCase editarUseCase;
    @MockitoBean RemoverReceitaUseCase removerUseCase;

    private static final UUID ID = UUID.randomUUID();
    private static final Receita RECEITA = new Receita(ID, "Salário", new BigDecimal("5000.00"),
            YearMonth.of(2025, 7), CategoriaReceita.SALARIO);

    private static final String PAYLOAD_VALIDO = """
            {
              "descricao": "Salário",
              "valor": 5000.00,
              "competencia": "2025-07",
              "categoria": "SALARIO"
            }
            """;

    @Test
    void deve_retornar201_ao_cadastrar_receita() throws Exception {
        when(cadastrarUseCase.cadastrar(any())).thenReturn(RECEITA);

        mockMvc.perform(post("/api/v1/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.descricao").value("Salário"));
    }

    @Test
    void deve_retornar422_ao_cadastrar_com_payload_invalido() throws Exception {
        mockMvc.perform(post("/api/v1/receitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"\",\"valor\":-1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void deve_retornar200_ao_listar_receitas() throws Exception {
        when(listarUseCase.listar(any(), any())).thenReturn(List.of(RECEITA));

        mockMvc.perform(get("/api/v1/receitas").param("competencia", "2025-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receitas[0].id").value(ID.toString()))
                .andExpect(jsonPath("$.total").value(5000.00));
    }

    @Test
    void deve_retornar200_ao_editar_receita() throws Exception {
        when(editarUseCase.editar(eq(ID), any())).thenReturn(RECEITA);

        mockMvc.perform(put("/api/v1/receitas/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void deve_retornar404_ao_editar_receita_inexistente() throws Exception {
        when(editarUseCase.editar(eq(ID), any()))
                .thenThrow(new ReceitaNaoEncontradaException(ID));

        mockMvc.perform(put("/api/v1/receitas/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deve_retornar204_ao_remover_receita() throws Exception {
        mockMvc.perform(delete("/api/v1/receitas/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deve_retornar404_ao_remover_receita_inexistente() throws Exception {
        doThrow(new ReceitaNaoEncontradaException(ID)).when(removerUseCase).remover(ID);

        mockMvc.perform(delete("/api/v1/receitas/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
