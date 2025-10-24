package com.example.citasmedicas_backend.citas.dto.pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoCompletoDto {
    private Long id;
    private BigDecimal monto;
    private String referencia;
    private String descripcion;
    private MetodoPagoCompletoDto metodoPago;
    private TarjetaCompletoDto tarjeta;
    private EstadoPagoDto estadoPago;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public PagoCompletoDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public MetodoPagoCompletoDto getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPagoCompletoDto metodoPago) { this.metodoPago = metodoPago; }

    public TarjetaCompletoDto getTarjeta() { return tarjeta; }
    public void setTarjeta(TarjetaCompletoDto tarjeta) { this.tarjeta = tarjeta; }

    public EstadoPagoDto getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPagoDto estadoPago) { this.estadoPago = estadoPago; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    @Override
    public String toString() {
        return "PagoCompletoDto{" +
                "id=" + id +
                ", monto=" + monto +
                ", referencia='" + referencia + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", metodoPago=" + metodoPago +
                ", tarjeta=" + tarjeta +
                ", estadoPago=" + estadoPago +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                '}';
    }
}