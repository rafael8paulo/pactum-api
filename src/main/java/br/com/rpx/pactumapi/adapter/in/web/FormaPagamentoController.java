package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.application.dto.request.CadastrarFormaPagamentoRequest;
import br.com.rpx.pactumapi.application.dto.request.EditarFormaPagamentoRequest;
import br.com.rpx.pactumapi.application.dto.response.FormaPagamentoResponse;
import br.com.rpx.pactumapi.application.dto.response.ListaFormasPagamentoResponse;
import br.com.rpx.pactumapi.application.mapper.FormaPagamentoMapper;
import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.port.in.CadastrarFormaPagamentoUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarFormaPagamentoUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarFormasPagamentoUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverFormaPagamentoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/formas-pagamento")
@Tag(name = "Formas de Pagamento", description = "Gestão de formas de pagamento (cartão/conta)")
public class FormaPagamentoController {

    private final CadastrarFormaPagamentoUseCase cadastrarUseCase;
    private final ListarFormasPagamentoUseCase listarUseCase;
    private final EditarFormaPagamentoUseCase editarUseCase;
    private final RemoverFormaPagamentoUseCase removerUseCase;
    private final UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    public FormaPagamentoController(CadastrarFormaPagamentoUseCase cadastrarUseCase,
                                     ListarFormasPagamentoUseCase listarUseCase,
                                     EditarFormaPagamentoUseCase editarUseCase,
                                     RemoverFormaPagamentoUseCase removerUseCase,
                                     UsuarioAutenticadoResolver usuarioAutenticadoResolver) {
        this.cadastrarUseCase = cadastrarUseCase;
        this.listarUseCase = listarUseCase;
        this.editarUseCase = editarUseCase;
        this.removerUseCase = removerUseCase;
        this.usuarioAutenticadoResolver = usuarioAutenticadoResolver;
    }

    @Operation(summary = "Cadastrar forma de pagamento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Forma de pagamento criada"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<FormaPagamentoResponse> cadastrar(@Valid @RequestBody CadastrarFormaPagamentoRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var formaPagamento = cadastrarUseCase.cadastrar(FormaPagamentoMapper.toDomain(request), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(FormaPagamentoMapper.toResponse(formaPagamento));
    }

    @Operation(summary = "Listar formas de pagamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    @GetMapping
    public ResponseEntity<ListaFormasPagamentoResponse> listar() {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var formasPagamento = listarUseCase.listar(usuarioId);
        return ResponseEntity.ok(FormaPagamentoMapper.toListResponse(formasPagamento));
    }

    @Operation(summary = "Editar forma de pagamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Forma de pagamento editada"),
            @ApiResponse(responseCode = "404", description = "Forma de pagamento não encontrada"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<FormaPagamentoResponse> editar(
            @PathVariable UUID id,
            @Valid @RequestBody EditarFormaPagamentoRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var formaPagamento = editarUseCase.editar(id, FormaPagamentoMapper.toDomain(request), usuarioId);
        return ResponseEntity.ok(FormaPagamentoMapper.toResponse(formaPagamento));
    }

    @Operation(summary = "Remover forma de pagamento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Forma de pagamento removida"),
            @ApiResponse(responseCode = "404", description = "Forma de pagamento não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        removerUseCase.remover(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
