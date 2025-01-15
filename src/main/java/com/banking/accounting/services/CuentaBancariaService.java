package com.banking.accounting.services;

import com.banking.accounting.entities.CuentaBancaria;
import com.banking.accounting.repositories.CuentaBancariaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CuentaBancariaService {

    private final CuentaBancariaRepository cuentaBancariaRepository;

    public CuentaBancariaService(CuentaBancariaRepository cuentaBancariaRepository) {
        this.cuentaBancariaRepository = cuentaBancariaRepository;
    }

    public List<CuentaBancaria> findAll() {
        return cuentaBancariaRepository.findAll();
    }

    public Optional<CuentaBancaria> findById(Long id) {
        return cuentaBancariaRepository.findById(id);
    }

    public CuentaBancaria save(CuentaBancaria cuentaBancaria) {
        return cuentaBancariaRepository.save(cuentaBancaria);
    }

    public void deleteById(Long id) {
        cuentaBancariaRepository.deleteById(id);
    }
}