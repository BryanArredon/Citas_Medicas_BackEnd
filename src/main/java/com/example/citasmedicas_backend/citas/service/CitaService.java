package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.dto.PagoRequestDto;
import com.example.citasmedicas_backend.citas.dto.MetodoPagoDto;
import com.example.citasmedicas_backend.citas.dto.SimulacionResponseDto;
import com.example.citasmedicas_backend.citas.model.Cita;
import com.example.citasmedicas_backend.citas.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CitaService {
    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PagoClient pagoClient;

    public List<Cita> getAllCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> getCitaById(Long id) {
        return citaRepository.findById(id);
    }

    public Cita createCita(Cita cita) {
        // Build a payment request using the service cost (if available)
        PagoRequestDto pagoReq = new PagoRequestDto();
        if (cita.getServicio() != null && cita.getServicio().getCosto() != null) {
            pagoReq.setMonto(cita.getServicio().getCosto());
        }
        // Optionally set a reference and method (default: approve)
        MetodoPagoDto metodo = new MetodoPagoDto();
        metodo.setTipo("OTRO");
        pagoReq.setMetodoPago(metodo);

        // Call pagos microservice to simulate payment
        SimulacionResponseDto resp = pagoClient.simularPago(pagoReq);
        if (resp == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No response from pagos microservice");
        }

        if ("APROBADO".equalsIgnoreCase(resp.getEstado())) {
            return citaRepository.save(cita);
        } else {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Pago rechazado: " + resp.getMessage());
        }
    }

    public Cita updateCita(Long id, Cita citaDetails) {
        return citaRepository.findById(id).map(cita -> {
            cita.setPaciente(citaDetails.getPaciente());
            cita.setMedico(citaDetails.getMedico());
            cita.setServicio(citaDetails.getServicio());
            cita.setAgenda(citaDetails.getAgenda());
            cita.setEstatus(citaDetails.getEstatus());
            cita.setFechaSolicitud(citaDetails.getFechaSolicitud());
            cita.setMotivo(citaDetails.getMotivo());
            return citaRepository.save(cita);
        }).orElse(null);
    }

    public boolean deleteCita(Long id) {
        if (citaRepository.existsById(id)) {
            citaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
