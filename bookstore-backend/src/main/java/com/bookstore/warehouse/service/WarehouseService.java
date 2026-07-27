package com.bookstore.warehouse.service;

import com.bookstore.warehouse.dto.WarehouseRequest;
import com.bookstore.warehouse.dto.WarehouseResponse;
import com.bookstore.warehouse.entity.Warehouse;
import com.bookstore.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public List<WarehouseResponse> getAll() {
        return warehouseRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        Warehouse warehouse = new Warehouse(
                request.name(), request.address(), request.ghnDistrictId(), request.ghnWardCode());
        warehouseRepository.save(warehouse);
        return toResponse(warehouse);
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        UUID id = Objects.requireNonNull(warehouse.getId(), "Persisted warehouse must have an id");
        return new WarehouseResponse(
                id, warehouse.getName(), warehouse.getAddress(),
                warehouse.getGhnDistrictId(), warehouse.getGhnWardCode());
    }
}