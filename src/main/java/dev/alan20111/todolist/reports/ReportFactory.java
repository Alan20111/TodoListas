package dev.alan20111.todolist.reports;

public class ReportFactory {
    public static ReportStrategy getReportStrategy(String reportType) {
        if (reportType == null) {
            throw new IllegalArgumentException("El tipo de reporte no puede ser nulo.");
        }

        return switch (reportType.toUpperCase()) {
            case "PDF" -> new PdfReportStrategy();
            case "EXCEL" -> new ExcelReportStrategy();
            default -> throw new IllegalArgumentException("Tipo de reporte no soportado: " + reportType);
        };
    }
}