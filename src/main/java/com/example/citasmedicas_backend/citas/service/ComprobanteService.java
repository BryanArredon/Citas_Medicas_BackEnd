package com.example.citasmedicas_backend.citas.service;

import com.example.citasmedicas_backend.citas.model.Cita;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para generar comprobantes en PDF de citas médicas con pago
 */
@Service
public class ComprobanteService {
    
    private static final Logger log = LoggerFactory.getLogger(ComprobanteService.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // Colores corporativos
    private static final DeviceRgb COLOR_VERDE = new DeviceRgb(76, 175, 80);
    private static final DeviceRgb COLOR_AZUL = new DeviceRgb(33, 150, 243);
    private static final DeviceRgb COLOR_GRIS_CLARO = new DeviceRgb(245, 245, 245);

    /**
     * Genera un comprobante en PDF de la cita con información del pago
     * 
     * @param cita La cita con información de pago
     * @return Array de bytes con el PDF generado
     */
    public byte[] generarComprobantePDF(Cita cita) {
        log.info("Generando comprobante PDF para cita ID: {}", cita.getId());
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Configurar márgenes
            document.setMargins(40, 40, 40, 40);
            
            // ========== HEADER ==========
            agregarHeader(document, cita);
            
            // ========== INFORMACIÓN DEL PACIENTE ==========
            agregarSeccionPaciente(document, cita);
            
            // ========== INFORMACIÓN DE LA CITA ==========
            agregarSeccionCita(document, cita);
            
            // ========== INFORMACIÓN DEL PAGO ==========
            agregarSeccionPago(document, cita);
            
            // ========== FOOTER ==========
            agregarFooter(document);
            
            document.close();
            
            byte[] pdfBytes = baos.toByteArray();
            log.info("Comprobante PDF generado exitosamente. Tamanio: {} bytes", pdfBytes.length);
            
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("Error al generar comprobante PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar comprobante PDF", e);
        }
    }
    
    /**
     * Agrega el header del documento con título y logo
     */
    private void agregarHeader(Document document, Cita cita) {
        // Título principal
        Paragraph titulo = new Paragraph("COMPROBANTE DE PAGO")
                .setFontSize(24)
                .setBold()
                .setFontColor(COLOR_VERDE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(titulo);
        
        // Subtítulo
        Paragraph subtitulo = new Paragraph("Cita Médica")
                .setFontSize(16)
                .setFontColor(COLOR_AZUL)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitulo);
        
        // Información del comprobante
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
        
        headerTable.addCell(createHeaderCell("Folio de Comprobante:", false));
        headerTable.addCell(createHeaderCell(cita.getNumeroReferenciaPago() != null ? 
                cita.getNumeroReferenciaPago() : "N/A", true));
        
        headerTable.addCell(createHeaderCell("Fecha de Emisión:", false));
        headerTable.addCell(createHeaderCell(
                cita.getFechaPago() != null ? 
                        cita.getFechaPago().format(FORMATTER) : "N/A", true));
        
        document.add(headerTable);
        
        // Línea divisoria
        document.add(new Paragraph("\n").setMarginBottom(10));
    }
    
    /**
     * Agrega la sección de información del paciente
     */
    private void agregarSeccionPaciente(Document document, Cita cita) {
        // Título de sección
        Paragraph tituloPaciente = new Paragraph("DATOS DEL PACIENTE")
                .setFontSize(14)
                .setBold()
                .setFontColor(COLOR_AZUL)
                .setMarginBottom(10);
        document.add(tituloPaciente);
        
        // Tabla de información del paciente
        Table tablaPaciente = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
        
        String nombreCompleto = cita.getPaciente().getUsuario().getNombre() + " " +
                cita.getPaciente().getUsuario().getApellidoPaterno() + " " +
                (cita.getPaciente().getUsuario().getApellidoMaterno() != null ? 
                        cita.getPaciente().getUsuario().getApellidoMaterno() : "");
        
        tablaPaciente.addCell(createInfoCell("Nombre:", false));
        tablaPaciente.addCell(createInfoCell(nombreCompleto, true));
        
        tablaPaciente.addCell(createInfoCell("Correo Electrónico:", false));
        tablaPaciente.addCell(createInfoCell(
                cita.getPaciente().getUsuario().getCorreoElectronico(), true));
        
        tablaPaciente.addCell(createInfoCell("Teléfono:", false));
        tablaPaciente.addCell(createInfoCell(
                cita.getPaciente().getUsuario().getTelefono() != null ? 
                        cita.getPaciente().getUsuario().getTelefono() : "N/A", true));
        
        document.add(tablaPaciente);
    }
    
    /**
     * Agrega la sección de información de la cita
     */
    private void agregarSeccionCita(Document document, Cita cita) {
        // Título de sección
        Paragraph tituloCita = new Paragraph("DETALLES DE LA CITA MÉDICA")
                .setFontSize(14)
                .setBold()
                .setFontColor(COLOR_AZUL)
                .setMarginBottom(10);
        document.add(tituloCita);
        
        // Tabla de información de la cita
        Table tablaCita = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
        
        tablaCita.addCell(createInfoCell("ID de Cita:", false));
        tablaCita.addCell(createInfoCell("#" + cita.getId(), true));
        
        tablaCita.addCell(createInfoCell("Servicio:", false));
        tablaCita.addCell(createInfoCell(
                cita.getServicio() != null ? 
                        cita.getServicio().getNombreServicio() : "Consulta General", true));
        
        // Agregar costo del servicio
        tablaCita.addCell(createInfoCell("Costo del Servicio:", false));
        String costoServicio = "N/A";
        if (cita.getServicio() != null && cita.getServicio().getCosto() != null) {
            costoServicio = String.format("$%.2f MXN", cita.getServicio().getCosto());
        }
        tablaCita.addCell(createInfoCell(costoServicio, true));
        
        String nombreMedico = "Dr. " + cita.getMedico().getUsuario().getNombre() + " " +
                cita.getMedico().getUsuario().getApellidoPaterno();
        
        tablaCita.addCell(createInfoCell("Médico:", false));
        tablaCita.addCell(createInfoCell(nombreMedico, true));
        
        tablaCita.addCell(createInfoCell("Especialidad:", false));
        tablaCita.addCell(createInfoCell(
                cita.getMedico().getServicio() != null ? 
                        cita.getMedico().getServicio().getNombreServicio() : "Medicina General", true));
        
        tablaCita.addCell(createInfoCell("Fecha y Hora:", false));
        tablaCita.addCell(createInfoCell(
                cita.getFechaSolicitud().format(FORMATTER), true));
        
        tablaCita.addCell(createInfoCell("Motivo de Consulta:", false));
        tablaCita.addCell(createInfoCell(
                cita.getMotivo() != null ? cita.getMotivo() : "Consulta general", true));
        
        document.add(tablaCita);
    }
    
    /**
     * Agrega la sección de información del pago
     */
    private void agregarSeccionPago(Document document, Cita cita) {
        // Título de sección con badge de aprobado
        Paragraph tituloPago = new Paragraph("INFORMACIÓN DEL PAGO")
                .setFontSize(14)
                .setBold()
                .setFontColor(COLOR_AZUL)
                .setMarginBottom(10);
        document.add(tituloPago);
        
        // Badge de estado
        Paragraph estadoBadge = new Paragraph("✓ PAGO APROBADO")
                .setFontSize(12)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(COLOR_VERDE)
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        document.add(estadoBadge);
        
        // Tabla de información del pago
        Table tablaPago = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
        
        tablaPago.addCell(createInfoCell("Monto Pagado:", false));
        String montoPagado = cita.getMontoPagado() != null ? 
                String.format("$%.2f MXN", cita.getMontoPagado()) : "$0.00 MXN";
        tablaPago.addCell(createInfoCell(montoPagado, true));
        
        tablaPago.addCell(createInfoCell("Método de Pago:", false));
        tablaPago.addCell(createInfoCell(
                cita.getMetodoPago() != null ? cita.getMetodoPago() : "Tarjeta de Crédito", true));
        
        tablaPago.addCell(createInfoCell("Referencia de Pago:", false));
        tablaPago.addCell(createInfoCell(
                cita.getNumeroReferenciaPago() != null ? cita.getNumeroReferenciaPago() : "N/A", true));
        
        tablaPago.addCell(createInfoCell("Fecha de Pago:", false));
        tablaPago.addCell(createInfoCell(
                cita.getFechaPago() != null ? cita.getFechaPago().format(FORMATTER) : "N/A", true));
        
        tablaPago.addCell(createInfoCell("Estado del Pago:", false));
        tablaPago.addCell(createInfoCell(
                cita.getEstadoPago() != null ? cita.getEstadoPago() : "APROBADO", true));
        
        document.add(tablaPago);
    }
    
    /**
     * Agrega el footer del documento
     */
    private void agregarFooter(Document document) {
        // Nota importante
        Paragraph nota = new Paragraph("NOTA IMPORTANTE")
                .setFontSize(10)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(5);
        document.add(nota);
        
        Paragraph notaTexto = new Paragraph(
                "• Por favor llegue 10 minutos antes de su cita.\n" +
                "• Presente este comprobante en recepción junto con su identificación oficial.\n" +
                "• En caso de cancelación, contacte al menos 24 horas antes de su cita.\n" +
                "• Este documento es válido como comprobante de pago.")
                .setFontSize(9)
                .setMarginBottom(20);
        document.add(notaTexto);
        
        // Línea divisoria
        document.add(new Paragraph("_".repeat(80))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));
        
        // Footer
        Paragraph footer = new Paragraph("Sistema de Citas Médicas - Comprobante Generado Electrónicamente")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(5);
        document.add(footer);
        
        Paragraph fecha = new Paragraph("Fecha de generación: " + 
                java.time.LocalDateTime.now().format(FORMATTER))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY);
        document.add(fecha);
    }
    
    /**
     * Crea una celda para el header
     */
    private Cell createHeaderCell(String text, boolean isValue) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFontSize(10))
                .setPadding(5)
                .setBorder(null);
        
        if (isValue) {
            cell.setBold().setTextAlignment(TextAlignment.RIGHT);
        }
        
        return cell;
    }
    
    /**
     * Crea una celda de información
     */
    private Cell createInfoCell(String text, boolean isValue) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFontSize(10))
                .setPadding(8);
        
        if (!isValue) {
            cell.setBackgroundColor(COLOR_GRIS_CLARO)
                .setBold();
        }
        
        return cell;
    }
}
