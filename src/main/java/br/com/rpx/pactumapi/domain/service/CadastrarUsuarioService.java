package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.exception.EmailJaCadastradoException;
import br.com.rpx.pactumapi.domain.model.Usuario;
import br.com.rpx.pactumapi.domain.port.in.CadastrarUsuarioUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarUsuarioPorEmailPort;
import br.com.rpx.pactumapi.domain.port.out.HashSenhaPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarUsuarioPort;

@UseCase
public class CadastrarUsuarioService implements CadastrarUsuarioUseCase {

    private final BuscarUsuarioPorEmailPort buscarPort;
    private final SalvarUsuarioPort salvarPort;
    private final HashSenhaPort hashSenhaPort;

    public CadastrarUsuarioService(BuscarUsuarioPorEmailPort buscarPort,
                                   SalvarUsuarioPort salvarPort,
                                   HashSenhaPort hashSenhaPort) {
        this.buscarPort = buscarPort;
        this.salvarPort = salvarPort;
        this.hashSenhaPort = hashSenhaPort;
    }

    @Override
    public Usuario cadastrar(Usuario usuario) {
        if (buscarPort.buscarPorEmail(usuario.email()).isPresent()) {
            throw new EmailJaCadastradoException(usuario.email());
        }
        String senhaHash = hashSenhaPort.hash(usuario.senha());
        Usuario comHash = new Usuario(null, usuario.nome(), usuario.email(), senhaHash);
        return salvarPort.salvar(comHash);
    }
}
