package com.example.citasmedicas_backend.pagos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.citasmedicas_backend.pagos.model.MetodoPago;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

}
