package com.repository;

import com.config.PostgresConfig;
import com.entity.ItemNotaFiscal;
import com.entity.NotaFiscal;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@Slf4j
public class NotaFiscalRepository {

    public void salvar(NotaFiscal notaFiscal) throws SQLException {
        String sqlNotaFiscal = "INSERT INTO notas_fiscais (id, numero, data_emissao, status, valor_total_nota, cliente_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        String sqlItemNotaFiscal = "INSERT INTO item_nota_fiscal (id, nota_fiscal_id, produto_id, nome_produto, preco_unitario, quantidade, valor_total) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = PostgresConfig.getConnection()) {
            try {
                connection.setAutoCommit(false);

                try (PreparedStatement psNota = connection.prepareStatement(sqlNotaFiscal)) {
                    psNota.setString(1, notaFiscal.id());
                    psNota.setString(2, notaFiscal.numero());
                    psNota.setTimestamp(3, Timestamp.from(notaFiscal.dataEmissao().toInstant()));
                    psNota.setString(4, notaFiscal.status().name());
                    psNota.setBigDecimal(5, notaFiscal.valorTotalNota());
                    psNota.setLong(6, notaFiscal.clienteId());
                    psNota.executeUpdate();
                }

                try (PreparedStatement psItem = connection.prepareStatement(sqlItemNotaFiscal)) {
                    for (ItemNotaFiscal item : notaFiscal.itens()) {
                        psItem.setString(1, item.id());
                        psItem.setString(2, item.notaFiscalId());
                        psItem.setString(3, item.produtoId());
                        psItem.setString(4, item.nomeProduto());
                        psItem.setBigDecimal(5, item.precoUnitario());
                        psItem.setInt(6, item.quantidade());
                        psItem.setBigDecimal(7, item.valorTotal());
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }

                connection.commit();

            } catch (SQLException e) {
                log.error("Erro ao salvar nota fiscal {}. Revertendo a transação.", notaFiscal.id(), e);
                try {
                    connection.rollback();
                    log.warn("Transação da nota fiscal {} foi revertida com sucesso.", notaFiscal.id());
                } catch (SQLException ex) {
                    log.error("Falha CRÍTICA ao tentar reverter a transação (rollback) da nota fiscal {}.", notaFiscal.id(), ex);
                }
                throw e;
            }
        }
    }
}