package com.kap.mechanics_api.infra;

import com.kap.mechanics_api.core.cliente.usecase.ClienteNaoEncontradoException;
import com.kap.mechanics_api.core.itemestoque.domain.EstoqueInsuficienteException;
import com.kap.mechanics_api.core.itemestoque.domain.ItemEstoqueInativoException;
import com.kap.mechanics_api.core.itemestoque.usecase.ItemEstoqueNaoEncontradoException;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.MovimentacaoEstoqueNaoEncontradaException;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.PeriodoMovimentacaoInvalidoException;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ResponsavelNaoEncontradoException;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServicoNaoEstaEmExecucaoException;
import com.kap.mechanics_api.core.servico.usecase.ServicoNaoEncontradoException;
import com.kap.mechanics_api.core.usuario.usecase.UsuarioNaoEncontradoException;
import com.kap.mechanics_api.core.veiculo.usecase.VeiculoNaoEncontradoException;
import com.kap.mechanics_api.core.orcamento.domain.StatusOrcamentoInvalidoException;
import com.kap.mechanics_api.core.orcamento.usecase.OrcamentoNaoEncontradoException;
import com.kap.mechanics_api.core.ordemservico.domain.TransicaoStatusInvalidaException;
import com.kap.mechanics_api.core.ordemservico.usecase.OrcamentoNaoAprovadoException;
import com.kap.mechanics_api.core.ordemservico.usecase.OrdemServicoJaExisteException;
import com.kap.mechanics_api.adapter.web.NenhumCampoInformadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.kap.mechanics_api.core.ordemservico.usecase.AtendenteNaoEncontradoException;
import com.kap.mechanics_api.core.ordemservico.usecase.OrdemServicoNaoEncontradaException;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ProblemDetail lancarExcecaoClienteNaoEncontrado(ClienteNaoEncontradoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Cliente não encontrado");
        return problemDetail;
    }

    @ExceptionHandler(VeiculoNaoEncontradoException.class)
    public ProblemDetail lancarExcecaoVeiculoNaoEncontrado(VeiculoNaoEncontradoException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Veículo não encontrado");
        return problemDetail;
    }

    @ExceptionHandler(ServicoNaoEncontradoException.class)
    public ProblemDetail lancarExcecaoPecaNaoEncontrada(ServicoNaoEncontradoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Servico não encontrado");
        return problemDetail;
    }

    @ExceptionHandler(ItemEstoqueNaoEncontradoException.class)
    public ProblemDetail lancarExcecaoItemEstoqueNaoEncontrado(ItemEstoqueNaoEncontradoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Item de estoque não encontrado");
        return problemDetail;
    }

    @ExceptionHandler(MovimentacaoEstoqueNaoEncontradaException.class)
    public ProblemDetail lancarExcecaoMovimentacaoEstoqueNaoEncontrada(MovimentacaoEstoqueNaoEncontradaException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Movimentacao de estoque nao encontrada");
        return problemDetail;
    }


    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ProblemDetail lancarExcecaoEstoqueInsuficiente(EstoqueInsuficienteException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Estoque insuficiente");
        return problemDetail;
    }

    @ExceptionHandler({OrdemServicoNaoEstaEmExecucaoException.class, ItemEstoqueInativoException.class})
    public ProblemDetail lancarExcecaoRegraMovimentacaoEstoque(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Movimentacao de estoque invalida");
        return problemDetail;
    }

    @ExceptionHandler(PeriodoMovimentacaoInvalidoException.class)
    public ProblemDetail lancarExcecaoPeriodoMovimentacaoInvalido(PeriodoMovimentacaoInvalidoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Periodo de movimentacao invalido");
        return problemDetail;
    }

    @ExceptionHandler(TransicaoStatusInvalidaException.class)
    public ProblemDetail lancarExcecaoTransicaoStatusInvalida(TransicaoStatusInvalidaException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Transicao de status invalida");
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Erro de validação");
        Map<String, String> erros = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        problemDetail.setProperty("erros", erros);
        return problemDetail;
    }

    @ExceptionHandler(NenhumCampoInformadoException.class)
    public ProblemDetail handleNenhumCampoInformado(NenhumCampoInformadoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Nenhum campo informado");
        problemDetail.setProperty("camposDisponiveis", ex.getCamposDisponiveis());
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleJsonInvalido(
            HttpMessageNotReadableException ex) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "O corpo da requisição contém um campo com valor inválido."
                );

        problemDetail.setTitle("Requisição inválida");

        return problemDetail;
    }

    @ExceptionHandler({ResponsavelNaoEncontradoException.class, AtendenteNaoEncontradoException.class})
    public ProblemDetail lancarExcecaoResponsavelNaoEncontrado(RuntimeException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Usuario não encontrado");
        return problemDetail;
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ProblemDetail lancarExcecaoUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Usuario não encontrado");
        return problemDetail;
    }

    @ExceptionHandler(OrcamentoNaoEncontradoException.class)
    public ProblemDetail lancarExcecaoOrcamentoNaoEncontrado(OrcamentoNaoEncontradoException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Orçamento não encontrado");
        return problemDetail;
    }


    @ExceptionHandler(StatusOrcamentoInvalidoException.class)
    public ProblemDetail lancarExcecaoStatusOrcamentoInvalido(StatusOrcamentoInvalidoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Status de orçamento inválido");
        return problemDetail;
    }

    @ExceptionHandler(OrdemServicoNaoEncontradaException.class)
    public ProblemDetail lancarExcecaoOrdemServicoNaoEncontrada(OrdemServicoNaoEncontradaException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Ordem de serviço não encontrada");
        return problemDetail;
    }

//    @ExceptionHandler(StatusOrdemServicoNaoEncontradoException.class)
//    public ProblemDetail lancarExcecaoStatusOrdemServicoNaoEncontrado(StatusOrdemServicoNaoEncontradoException ex) {
//        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
//        problemDetail.setTitle("Status da ordem de serviço não encontrado");
//        return problemDetail;
//    }
      
    @ExceptionHandler(OrcamentoNaoAprovadoException.class)
    public ProblemDetail lancarExcecaoOrcamentoNaoAprovado(OrcamentoNaoAprovadoException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Orçamento precisa estar aprovado para gerar OS");
        return problemDetail;
    }

    @ExceptionHandler(OrdemServicoJaExisteException.class)
    public ProblemDetail lancarExcecaoOrdemServicoExistente(OrdemServicoJaExisteException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Este orçamento já possui uma OS gerada");
        return problemDetail;
    }

    // Invariantes dos objetos de domínio (Cliente, Veiculo, Servico, ItemEstoque...) violadas
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail lancarExcecaoArgumentoInvalido(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Dados inválidos");
        return problemDetail;
    }

}
