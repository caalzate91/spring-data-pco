package com.banking.accounting.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "CuentaBancaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCuenta;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column(nullable = false)
    private Double saldo;

    @Column
    private String tipoCuenta;

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;
}
