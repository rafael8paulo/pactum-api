package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.exception.ContaRecorrenteNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.FrequenciaCobranca;
import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarHistoricoValorPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoValorServiceTest {

    @Mock BuscarContasRecorrentesPort buscarContasRecorrentesPort;
    @Mock BuscarHistoricoValorPort buscarHistoricoValorPort;
    @InjectMocks HistoricoValorService service;

    private final UUID usuarioId = UUID.randomUUID();

    private ContaRecorrente contaFixture(UUID id) {
        return new ContaRecorrente(id, "Streaming", new BigDecimal("39.90"), CategoriaDespesa.LAZER, null,
                YearMonth.of(2026, 1), null, StatusContaRecorrente.ATIVA, usuarioId,
                FrequenciaCobranca.MENSAL, null, LocalDate.of(2026, 1, 10));
    }

    @Test
    void deve_retornar_historico_ordenado_da_conta() {
        UUID id = UUID.randomUUID();
        when(buscarContasRecorrentesPort.buscarPorId(id)).thenReturn(Optional.of(contaFixture(id)));
        HistoricoValorContaRecorrente registro = new HistoricoValorContaRecorrente(
                UUID.randomUUID(), id, new BigDecimal("39.90"), LocalDate.of(2026, 1, 10), null);
        when(buscarHistoricoValorPort.buscarPorContaRecorrenteOrdenado(id)).thenReturn(List.of(registro));

        List<HistoricoValorContaRecorrente> resultado = service.consultar(id, usuarioId);

        assertThat(resultado).containsExactly(registro);
    }

    @Test
    void deve_lancar_excecao_para_conta_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarContasRecorrentesPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultar(id, usuarioId))
                .isInstanceOf(ContaRecorrenteNaoEncontradaException.class);
    }

    @Test
    void deve_lancar_excecao_para_conta_de_outro_usuario() {
        UUID id = UUID.randomUUID();
        when(buscarContasRecorrentesPort.buscarPorId(id)).thenReturn(Optional.of(contaFixture(id)));

        assertThatThrownBy(() -> service.consultar(id, UUID.randomUUID()))
                .isInstanceOf(ContaRecorrenteNaoEncontradaException.class);
    }
}
