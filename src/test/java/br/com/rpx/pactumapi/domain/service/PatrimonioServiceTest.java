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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatrimonioServiceTest {

    @Mock SalvarPatrimonioPort salvarPatrimonioPort;
    @Mock BuscarPatrimonioPort buscarPatrimonioPort;
    @InjectMocks PatrimonioService service;

    private final UUID usuarioId = UUID.randomUUID();

    private Patrimonio patrimonioFixture(UUID id) {
        return new Patrimonio(id, "Caixinha Turbo Nubank", new BigDecimal("5069.02"),
                YearMonth.of(2025, 7), usuarioId);
    }

    @Test
    void deve_cadastrar_patrimonio() {
        when(salvarPatrimonioPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Patrimonio resultado = service.cadastrar(patrimonioFixture(null), usuarioId);

        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
        verify(salvarPatrimonioPort).salvar(any());
    }

    @Test
    void deve_consultar_patrimonio_por_competencia() {
        YearMonth competencia = YearMonth.of(2025, 7);
        Patrimonio patrimonio = patrimonioFixture(UUID.randomUUID());
        when(buscarPatrimonioPort.buscarPorCompetencia(competencia, usuarioId)).thenReturn(List.of(patrimonio));

        List<Patrimonio> resultado = service.consultar(competencia, usuarioId);

        assertThat(resultado).hasSize(1);
        verify(buscarPatrimonioPort).buscarPorCompetencia(competencia, usuarioId);
    }

    @Test
    void deve_retornar_lista_vazia_quando_sem_itens() {
        YearMonth competencia = YearMonth.of(2025, 1);
        when(buscarPatrimonioPort.buscarPorCompetencia(competencia, usuarioId)).thenReturn(List.of());

        List<Patrimonio> resultado = service.consultar(competencia, usuarioId);

        assertThat(resultado).isEmpty();
    }
}
