package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.MetaEconomia;

public interface SalvarMetaEconomiaPort {
    MetaEconomia salvar(MetaEconomia meta);
}
