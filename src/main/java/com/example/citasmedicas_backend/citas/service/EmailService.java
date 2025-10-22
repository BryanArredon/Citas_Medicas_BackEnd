package com.example.citasmedicas_backend.citas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envía un correo electrónico con contenido HTML
     */
    public void enviarEmailHtml(String destinatario, String asunto, String contenidoHtml) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(contenidoHtml, true); // true indica que es HTML

        mailSender.send(message);
    }

    /**
     * Genera HTML para correo de bienvenida a nuevos usuarios
     */
    public String generarHtmlBienvenida(String nombreUsuario, String emailUsuario) {
        return generarHtmlBienvenida(nombreUsuario, emailUsuario, null);
    }

    /**
     * Genera HTML para correo de bienvenida a nuevos usuarios
     */
    public String generarHtmlBienvenida(String nombreUsuario, String emailUsuario, String logoUrl) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"es\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>Bienvenido a MediCitas</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f8fafc; min-height: 100vh; }");
        html.append(".container { max-width: 600px; margin: 20px auto; background-color: white; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #e5e7eb; }");
        html.append(".header { background: linear-gradient(135deg, #1e40af 0%, #0f766e 100%); color: white; padding: 40px 30px; text-align: center; }");
        html.append(".content { padding: 40px 30px; color: #374151; line-height: 1.7; }");
        html.append(".button { display: inline-block; background: linear-gradient(135deg, #1e40af 0%, #0f766e 100%); color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 14px; margin: 20px 0; box-shadow: 0 2px 4px rgba(30, 64, 175, 0.2); }");
        html.append(".button:hover { box-shadow: 0 4px 8px rgba(30, 64, 175, 0.3); }");
        html.append(".footer { background-color: #f9fafb; padding: 30px; text-align: center; color: #6b7280; font-size: 13px; border-top: 1px solid #e5e7eb; }");
        html.append(".highlight { color: #1e40af; font-weight: 600; }");
        html.append(".info-section { background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; margin: 25px 0; }");
        html.append(".info-item { margin: 8px 0; color: #374151; }");
        html.append(".features { background-color: white; border: 1px solid #e2e8f0; border-radius: 8px; padding: 25px; margin: 25px 0; }");
        html.append(".feature-item { display: flex; align-items: flex-start; margin-bottom: 15px; }");
        html.append(".feature-icon { width: 20px; height: 20px; background: linear-gradient(135deg, #1e40af 0%, #0f766e 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 12px; font-weight: bold; margin-right: 15px; margin-top: 2px; flex-shrink: 0; }");
        html.append(".welcome-message { border-left: 4px solid #1e40af; padding-left: 20px; margin: 20px 0; background-color: #f8fafc; padding: 20px; border-radius: 0 8px 8px 0; }");
        html.append("@media (max-width: 640px) { .container { margin: 10px; } .header { padding: 30px 20px; } .content, .footer { padding: 20px; } .feature-item { flex-direction: column; align-items: flex-start; } .feature-icon { margin-bottom: 8px; margin-right: 0; } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class=\"container\">");
        html.append("<div class=\"header\">");
        html.append("<h1 style=\"margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.025em;\">MediCitas</h1>");
        html.append("<p style=\"margin: 8px 0 0 0; opacity: 0.9; font-size: 14px; font-weight: 400;\">Sistema de Gestión Médica Integral</p>");
        html.append("</div>");
        html.append("<div class=\"content\">");
        html.append("<div class=\"welcome-message\">");
        html.append("<h2 style=\"margin: 0 0 10px 0; color: #1f2937; font-size: 20px; font-weight: 600;\">Estimado(a) ").append(nombreUsuario).append("</h2>");
        html.append("<p style=\"margin: 0; color: #4b5563; font-size: 15px;\">Le damos la bienvenida a MediCitas. Su cuenta ha sido creada y validada satisfactoriamente. A continuación, encontrará información relevante sobre las funcionalidades disponibles en nuestra plataforma.</p>");
        html.append("</div>");

        html.append("<div class=\"info-section\">");
        html.append("<h3 style=\"margin: 0 0 15px 0; color: #1f2937; font-size: 16px; font-weight: 600;\">Información de su cuenta</h3>");
        html.append("<div class=\"info-item\"><strong>Correo electrónico:</strong> ").append(emailUsuario).append("</div>");
        html.append("<div class=\"info-item\"><strong>Estado de la cuenta:</strong> <span style=\"color: #059669; font-weight: 600;\">Activa y verificada</span></div>");
        html.append("<div class=\"info-item\"><strong>Fecha de registro:</strong> ").append(java.time.LocalDate.now()).append("</div>");
        html.append("</div>");

        html.append("<div class=\"features\">");
        html.append("<h3 style=\"margin: 0 0 20px 0; color: #1f2937; font-size: 16px; font-weight: 600;\">Funcionalidades del Sistema MediCitas</h3>");
        html.append("<div class=\"feature-item\">");
        html.append("<div class=\"feature-icon\">1</div>");
        html.append("<div><strong>Gestión de Citas Médicas</strong><br><span style=\"color: #6b7280; font-size: 14px;\">Sistema integrado para programación de consultas con especialistas según disponibilidad y especialidad.</span></div>");
        html.append("</div>");
        html.append("<div class=\"feature-item\">");
        html.append("<div class=\"feature-icon\">2</div>");
        html.append("<div><strong>Expediente Clínico Electrónico</strong><br><span style=\"color: #6b7280; font-size: 14px;\">Acceso seguro a historial médico completo, con confidencialidad garantizada según normativas vigentes.</span></div>");
        html.append("</div>");
        html.append("<div class=\"feature-item\">");
        html.append("<div class=\"feature-icon\">3</div>");
        html.append("<div><strong>Sistema de Notificaciones</strong><br><span style=\"color: #6b7280; font-size: 14px;\">Recordatorios personalizados de citas programadas, resultados de laboratorio y seguimiento de tratamientos.</span></div>");
        html.append("</div>");
        html.append("<div class=\"feature-item\">");
        html.append("<div class=\"feature-icon\">4</div>");
        html.append("<div><strong>Portal de Administración Personal</strong><br><span style=\"color: #6b7280; font-size: 14px;\">Gestión integral de información demográfica, preferencias de contacto y configuración de cuenta.</span></div>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div style=\"text-align: center; margin: 30px 0;\">");
        html.append("<a href=\"#\" class=\"button\">ACCEDER A MI CUENTA</a>");
        html.append("</div>");

        html.append("<p style=\"text-align: center; color: #6b7280; font-size: 14px; margin: 20px 0;\">Para consultas técnicas o asistencia personalizada, nuestro departamento de soporte se encuentra disponible en horario laboral.</p>");

        html.append("<div style=\"text-align: center; margin-top: 25px; padding: 20px; background-color: #f8fafc; border-radius: 8px; border: 1px solid #e2e8f0;\">");
        html.append("<p style=\"margin: 0; color: #1f2937; font-weight: 600; font-size: 15px;\">Comprometidos con la excelencia en servicios de salud</p>");
        html.append("<p style=\"margin: 5px 0 0 0; color: #6b7280; font-size: 14px;\">Dirección General - Sistema MediCitas</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class=\"footer\">");
        html.append("<p style=\"margin: 0 0 8px 0; font-weight: 500;\">Comunicación oficial del Sistema MediCitas</p>");
        html.append("<p style=\"margin: 0; color: #9ca3af;\">Este es un mensaje generado automáticamente. Para asistencia técnica, comuníquese a través de nuestros canales oficiales de atención.</p>");
        html.append("<hr style=\"border: none; border-top: 1px solid #e5e7eb; margin: 15px 0;\">");
        html.append("<p style=\"margin: 0; font-size: 12px; color: #9ca3af;\">&copy; 2024 MediCitas. Todos los derechos reservados.<br>");
        html.append("Sistema de Gestión Médica Integral - Versión 2.0</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}