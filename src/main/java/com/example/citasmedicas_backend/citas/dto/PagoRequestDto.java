package com.example.citasmedicas_backend.citas.dto;

import java.math.BigDecimal;

public class PagoRequestDto {
    private BigDecimal monto;
    private MetodoPagoDto metodoPago;
    private TarjetaDto tarjeta;
    private String referencia;

    public PagoRequestDto() {}

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public MetodoPagoDto getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPagoDto metodoPago) { this.metodoPago = metodoPago; }

    public TarjetaDto getTarjeta() { return tarjeta; }
    public void setTarjeta(TarjetaDto tarjeta) { this.tarjeta = tarjeta; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
