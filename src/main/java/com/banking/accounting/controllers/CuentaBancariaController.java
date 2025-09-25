package com.banking.accounting.controllers;

import com.banking.accounting.entities.CuentaBancaria;
import com.banking.accounting.services.CuentaBancariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
@Tag(name = "Cuenta Bancaria", description = "Endpoints for managing bank accounts")
public class CuentaBancariaController {

    private final CuentaBancariaService cuentaBancariaService;

    public CuentaBancariaController(CuentaBancariaService cuentaBancariaService) {
        this.cuentaBancariaService = cuentaBancariaService;
    }

    @GetMapping
    @Operation(summary = "Get all bank accounts")
    public ResponseEntity<List<CuentaBancaria>> getAllCuentas() {
        return ResponseEntity.ok(cuentaBancariaService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a bank account by ID")
    public ResponseEntity<CuentaBancaria> getCuentaById(@PathVariable Long id) {
        return cuentaBancariaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new bank account")
    public ResponseEntity<CuentaBancaria> createCuenta(@RequestBody CuentaBancaria cuentaBancaria) {
        return ResponseEntity.ok(cuentaBancariaService.save(cuentaBancaria));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing bank account")
    public ResponseEntity<CuentaBancaria> updateCuenta(@PathVariable Long id, @RequestBody CuentaBancaria cuentaBancaria) {
        return cuentaBancariaService.findById(id)
                .map(existingCuenta -> {
                    cuentaBancaria.setIdCuenta(existingCuenta.getIdCuenta());
                    return ResponseEntity.ok(cuentaBancariaService.save(cuentaBancaria));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank account by ID")
    public ResponseEntity<Void> deleteCuenta(@PathVariable Long id) {
        cuentaBancariaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}