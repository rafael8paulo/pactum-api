package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.model.Patrimonio;
import br.com.rpx.pactumapi.domain.port.out.BuscarPatrimonioPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarPatrimonioPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatrimonioServiceTest {

    @Mock SalvarPatrimonioPort salvarPort;
    @Mock BuscarPatrimonioPort buscarPort;
    @InjectMocks PatrimonioService service;

    private Patrimonio patrimonioFixture(UUID id) {
        return new Patrimonio(id, "Caixinha Turbo Nubank", new BigDecimal("5069.02"), YearMonth.of(2025, 7));
    }

    @Test
    void deve_cadastrar_patrimonio() {
        UUID id = UUID.randomUUID();
        Patrimonio patrimonio = patrimonioFixture(id);
        when(salvarPort.salvar(patrimonio)).thenReturn(patrimonio);

        Patrimonio resultado = service.cadastrar(patrimonio);

        assertThat(resultado).isEqualTo(patrimonio);
        verify(salvarPort).salvar(patrimonio);
    }

    @Test
    void deve_consultar_patrimonio_por_competencia() {
        YearMonth competencia = YearMonth.of(2025, 7);
        Patrimonio patrimonio = patrimonioFixture(UUID.randomUUID());
        when(buscarPort.buscarPorCompetencia(competencia)).thenReturn(List.of(patrimonio));

        List<Patrimonio> resultado = service.consultar(competencia);

        assertThat(resultado).hasSize(1);
        verify(buscarPort).buscarPorCompetencia(competencia);
    }

    @Test
    void deve_retornar_lista_vazia_quando_sem_itens() {
        YearMonth competencia = YearMonth.of(2025, 1);
        when(buscarPort.buscarPorCompetencia(competencia)).thenReturn(List.of());

        List<Patrimonio> resultado = service.consultar(competencia);

        assertThat(resultado).isEmpty();
    }
}
