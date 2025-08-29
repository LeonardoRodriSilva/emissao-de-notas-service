package com.service;

import com.client.ProdutoClient;
import com.dto.CriarNotaFiscalRequest;
import com.dto.ItemRequest;
import com.dto.ProdutoDTO;
import com.entity.ItemNotaFiscal;
import com.entity.NotaFiscal;
import com.enums.StatusNotaFiscal;
import com.exception.PersistenciaException;
import com.repository.NotaFiscalRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class NotaFiscalService {

    private final NotaFiscalRepository notaFiscalRepository;
    private final ClienteCacheService clienteCacheService;
    private final ProdutoClient produtoClient;

    public NotaFiscalService(NotaFiscalRepository notaFiscalRepository, ClienteCacheService clienteCacheService, ProdutoClient produtoClient) {
        this.notaFiscalRepository = notaFiscalRepository;
        this.clienteCacheService = clienteCacheService;
        this.produtoClient = produtoClient;
    }

    public NotaFiscal criarNotaFiscal(CriarNotaFiscalRequest request) {
        log.info("Iniciando processo de criação de nota fiscal para o cliente ID: {}", request.clienteId());

        if (request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("A lista de itens não pode ser vazia.");
        }

        var cliente = clienteCacheService.getCliente(request.clienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente com ID " + request.clienteId() + " não encontrado no cache."));

        List<ItemNotaFiscal> itensProcessados = new ArrayList<>();
        BigDecimal valorTotalNota = BigDecimal.ZERO;
        String notaFiscalId = UUID.randomUUID().toString();

        for (ItemRequest itemRequest : request.itens()) {
            ProdutoDTO produto = produtoClient.buscarProdutoPorId(itemRequest.produtoId());
            if (produto == null) {
                throw new IllegalArgumentException("Produto com ID " + itemRequest.produtoId() + " não foi encontrado.");
            }

            BigDecimal valorTotalItem = produto.preco().multiply(BigDecimal.valueOf(itemRequest.quantidade()));

            ItemNotaFiscal itemProcessado = new ItemNotaFiscal(
                    UUID.randomUUID().toString(),
                    notaFiscalId,
                    produto.id(),
                    produto.nome(),
                    produto.preco(),
                    itemRequest.quantidade(),
                    valorTotalItem
            );
            itensProcessados.add(itemProcessado);
            valorTotalNota = valorTotalNota.add(valorTotalItem);
        }

        NotaFiscal notaFiscalCompleta = new NotaFiscal(
                notaFiscalId,
                String.valueOf(System.currentTimeMillis()),
                OffsetDateTime.now(),
                StatusNotaFiscal.PENDENTE,
                valorTotalNota,
                cliente.id(),
                itensProcessados
        );

        try {
            notaFiscalRepository.salvar(notaFiscalCompleta);
            log.info("Nota fiscal {} salva com sucesso no banco de dados.", notaFiscalCompleta.id());
        } catch (SQLException e) {
            log.error("Erro ao salvar nota fiscal no banco de dados.", e);
            throw new PersistenciaException("Erro de persistência ao salvar a nota fiscal.", e);
        }

        return notaFiscalCompleta;
    }
}