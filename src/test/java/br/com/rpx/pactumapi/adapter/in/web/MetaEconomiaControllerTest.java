package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.exception.CompetenciaEncerradaException;
import br.com.rpx.pactumapi.domain.exception.MetaEconomiaNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.MetaEconomia;
import br.com.rpx.pactumapi.domain.model.ProgressoMeta;
import br.com.rpx.pactumapi.domain.model.ResultadoMensalMeta;
import br.com.rpx.pactumapi.domain.model.StatusMeta;
import br.com.rpx.pactumapi.domain.port.in.ConsultarHistoricoMetasUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarProgressoMetaUseCase;
import br.com.rpx.pactumapi.domain.port.in.DefinirMetaEconomiaUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverMetaEconomiaUseCase;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = MetaEconomiaController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class MetaEconomiaControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean DefinirMetaEconomiaUseCase definirUseCase;
    @MockitoBean ConsultarProgressoMetaUseCase progressoUseCase;
    @MockitoBean RemoverMetaEconomiaUseCase removerUseCase;
    @MockitoBean ConsultarHistoricoMetasUseCase historicoUseCase;
    @MockitoBean UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);

    @BeforeEach
    void setup() {
        when(usuarioAutenticadoResolver.getUsuarioId()).thenReturn(UUID.randomUUID());
    }

    @Test
    void deve_retornar200_ao_definir_meta() throws Exception {
        when(definirUseCase.definir(any(), any(), any()))
                .thenReturn(new MetaEconomia(UUID.randomUUID(), OUTUBRO, new BigDecimal("1000.00"), UUID.randomUUID()));

        mockMvc.perform(put("/api/v1/metas-economia/2026-10")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\": 1000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencia").value("2026-10"))
                .andExpect(jsonPath("$.valor").value(1000.00));
    }

    @Test
    void deve_retornar422_para_valor_nao_positivo() throws Exception {
        mockMvc.perform(put("/api/v1/metas-economia/2026-10")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\": 0}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deve_retornar400_ao_definir_meta_de_competencia_passada() throws Exception {
        when(definirUseCase.definir(any(), any(), any())).thenThrow(new CompetenciaEncerradaException(YearMonth.of(2026, 8)));

        mockMvc.perform(put("/api/v1/metas-economia/2026-08")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\": 100}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deve_retornar200_com_progresso() throws Exception {
        when(progressoUseCase.consultar(any(), any())).thenReturn(new ProgressoMeta(OUTUBRO,
                new BigDecimal("1000.00"), new BigDecimal("600.00"), new BigDecimal("400.00"),
                new BigDecimal("60.00"), 10, new BigDecimal("40.00"), new BigDecimal("930.00"),
                StatusMeta.ADIANTADA, List.of(25, 50)));

        mockMvc.perform(get("/api/v1/metas-economia/2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencia").value("2026-10"))
                .andExpect(jsonPath("$.percentual").value(60.00))
                .andExpect(jsonPath("$.valorDiarioNecessario").value(40.00))
                .andExpect(jsonPath("$.status").value("ADIANTADA"))
                .andExpect(jsonPath("$.marcosAtingidos[1]").value(50));
    }

    @Test
    void deve_retornar404_sem_meta() throws Exception {
        when(progressoUseCase.consultar(any(), any())).thenThrow(new MetaEconomiaNaoEncontradaException(YearMonth.of(2026, 12)));

        mockMvc.perform(get("/api/v1/metas-economia/2026-12")).andExpect(status().isNotFound());
    }

    @Test
    void deve_retornar204_ao_remover() throws Exception {
        mockMvc.perform(delete("/api/v1/metas-economia/2026-11")).andExpect(status().isNoContent());
    }

    @Test
    void deve_retornar400_ao_remover_competencia_passada() throws Exception {
        doThrow(new CompetenciaEncerradaException(YearMonth.of(2026, 8)))
                .when(removerUseCase).remover(any(), any());

        mockMvc.perform(delete("/api/v1/metas-economia/2026-08")).andExpect(status().isBadRequest());
    }

    @Test
    void deve_retornar_historico_sem_conflitar_com_rota_de_competencia() throws Exception {
        when(historicoUseCase.consultar(org.mockito.ArgumentMatchers.eq(2026), any())).thenReturn(List.of(
                new ResultadoMensalMeta(YearMonth.of(2026, 8), new BigDecimal("1000.00"),
                        new BigDecimal("1200.00"), new BigDecimal("120.00"), true)));

        mockMvc.perform(get("/api/v1/metas-economia/historico").param("ano", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ano").value(2026))
                .andExpect(jsonPath("$.meses[0].competencia").value("2026-08"))
                .andExpect(jsonPath("$.meses[0].atingida").value(true));
    }

    @Test
    void deve_retornar400_sem_parametro_ano() throws Exception {
        mockMvc.perform(get("/api/v1/metas-economia/historico")).andExpect(status().isBadRequest());
    }
}
