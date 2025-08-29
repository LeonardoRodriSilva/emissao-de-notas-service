package com.dto;

import java.util.List;

public record CriarNotaFiscalRequest(
        Long clienteId,
        List<ItemRequest> itens
) {
}
