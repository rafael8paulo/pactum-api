package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.domain.model.ResumoMensal;
import br.com.rpx.pactumapi.domain.port.in.ConsultarHistoricoAnualUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarResumoMensalUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumoController.class)
class ResumoControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean ConsultarResumoMensalUseCase resumoMensalUseCase;
    @MockitoBean ConsultarHistoricoAnualUseCase historicoAnualUseCase;

    private static final ResumoMensal RESUMO = new ResumoMensal(
            YearMonth.of(2025, 7),
            new BigDecimal("5000.00"),
            new BigDecimal("3000.00"),
            new BigDecimal("2000.00")
    );

    @Test
    void deve_retornar200_ao_consultar_resumo_mensal() throws Exception {
        when(resumoMensalUseCase.consultar(any())).thenReturn(RESUMO);

        mockMvc.perform(get("/api/v1/resumo").param("competencia", "2025-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencia").value("2025-07"))
                .andExpect(jsonPath("$.totalReceitas").value(5000.00))
                .andExpect(jsonPath("$.totalDespesas").value(3000.00))
                .andExpect(jsonPath("$.saldo").value(2000.00));
    }

    @Test
    void deve_retornar200_ao_consultar_historico_anual() throws Exception {
        List<ResumoMensal> historico = List.of(RESUMO);
        when(historicoAnualUseCase.consultar(anyInt())).thenReturn(historico);

        mockMvc.perform(get("/api/v1/resumo/anual").param("ano", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ano").value(2025))
                .andExpect(jsonPath("$.meses[0].competencia").value("2025-07"))
                .andExpect(jsonPath("$.totalAnualReceitas").value(5000.00))
                .andExpect(jsonPath("$.saldoAnual").value(2000.00));
    }
}
