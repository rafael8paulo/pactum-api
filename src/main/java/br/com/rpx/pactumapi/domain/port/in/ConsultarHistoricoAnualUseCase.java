package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ResumoMensal;

import java.util.List;

public interface ConsultarHistoricoAnualUseCase {
    List<ResumoMensal> consultar(int ano);
}
