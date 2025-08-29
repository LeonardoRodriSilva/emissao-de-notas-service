package com.entity;

import com.enums.StatusNotaFiscal;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record NotaFiscal(
        String id,
        String numero,
        OffsetDateTime dataEmissao,
        StatusNotaFiscal status,
        BigDecimal valorTotalNota,
        Long clienteId,
        List<ItemNotaFiscal> itens
) {
}