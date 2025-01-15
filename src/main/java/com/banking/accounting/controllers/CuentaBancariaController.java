package com.banking.accounting.controllers;

import com.banking.accounting.entities.CuentaBancaria;
import com.banking.accounting.services.CuentaBancariaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaBancariaController {

    private final CuentaBancariaService cuentaBancariaService;

    public CuentaBancariaController(CuentaBancariaService cuentaBancariaService) {
        this.cuentaBancariaService = cuentaBancariaService;
    }

    @GetMapping
    public ResponseEntity<List<CuentaBancaria>> getAllCuentas() {
        return ResponseEntity.ok(cuentaBancariaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaBancaria> getCuentaById(@PathVariable Long id) {
        return cuentaBancariaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CuentaBancaria> createCuenta(@RequestBody CuentaBancaria cuentaBancaria) {
        return ResponseEntity.ok(cuentaBancariaService.save(cuentaBancaria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaBancaria> updateCuenta(@PathVariable Long id, @RequestBody CuentaBancaria cuentaBancaria) {
        return cuentaBancariaService.findById(id)
                .map(existingCuenta -> {
                    cuentaBancaria.setIdCuenta(existingCuenta.getIdCuenta());
                    return ResponseEntity.ok(cuentaBancariaService.save(cuentaBancaria));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCuenta(@PathVariable Long id) {
        cuentaBancariaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}