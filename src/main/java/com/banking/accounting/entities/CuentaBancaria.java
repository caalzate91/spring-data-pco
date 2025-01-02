package com.banking.accounting.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "CuentaBancaria")
public class CuentaBancaria {

    @Id
    @GeneratedValue
    private Long idCuenta;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column(nullable = false)
    private Double saldo;

    @Column
    private String tipoCuenta;

    @ManyToOne
    @JoinColumn(name = "idUsuario", table = "Usuarios")
    private Usuario usuario;
}
