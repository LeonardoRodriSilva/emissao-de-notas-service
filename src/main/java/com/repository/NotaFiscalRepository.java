package com.repository;

import com.config.PostgresConfig;
import com.entity.ItemNotaFiscal;
import com.entity.NotaFiscal;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

@Slf4j
public class NotaFiscalRepository {

    public void salvar(NotaFiscal notaFiscal) throws SQLException {
        String sqlNotaFiscal = "INSERT INTO notas_fiscais (id, numero, data_emissao, status, valor_total_nota, cliente_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        String sqlItemNotaFiscal = "INSERT INTO item_nota_fiscal (id, nota_fiscal_id, produto_id, nome_produto, preco_unitario, quantidade, valor_total) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        log.debug("Iniciando método salvar no repositório.");
        Connection connection = null;
        try {
            log.debug("Passo 1: Tentando obter conexão com o banco de dados...");
            connection = PostgresConfig.getConnection();
            log.debug("Passo 2: Conexão com o banco de dados obtida com sucesso.");

            connection.setAutoCommit(false);
            log.debug("Passo 3: Auto-commit desativado. Transação iniciada.");

            try (PreparedStatement psNota = connection.prepareStatement(sqlNotaFiscal)) {
                psNota.setString(1, notaFiscal.id());
                psNota.setString(2, notaFiscal.numero());
                psNota.setTimestamp(3, Timestamp.from(notaFiscal.dataEmissao().toInstant()));
                psNota.setObject(4, notaFiscal.status().name(), Types.OTHER);
                psNota.setBigDecimal(5, notaFiscal.valorTotalNota());
                psNota.setLong(6, notaFiscal.clienteId());

                log.debug("Passo 4: Preparando para executar o INSERT na tabela notas_fiscais.");
                psNota.executeUpdate();
                log.debug("Passo 5: INSERT em notas_fiscais executado com sucesso.");
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
                log.debug("Passo 6: Preparando para executar o batch INSERT na tabela item_nota_fiscal.");
                psItem.executeBatch();
                log.debug("Passo 7: Batch INSERT em item_nota_fiscal executado com sucesso.");
            }

            connection.commit();
            log.debug("Passo 8: Transação commitada com sucesso.");

        } catch (SQLException e) {
            log.error("ERRO DE SQL OCORREU. Revertendo a transação.", e);
            if (connection != null) {
                try {
                    connection.rollback();
                    log.warn("Transação foi revertida com sucesso.");
                } catch (SQLException ex) {
                    log.error("Falha CRÍTICA ao tentar reverter a transação (rollback).", ex);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                    log.debug("Conexão com o banco de dados fechada.");
                } catch (SQLException e) {
                    log.error("Erro ao fechar a conexão com o banco de dados.", e);
                }
            }
        }
    }
}