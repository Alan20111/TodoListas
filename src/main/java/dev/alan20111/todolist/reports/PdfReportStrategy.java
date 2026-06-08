package dev.alan20111.todolist.reports;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import dev.alan20111.todolist.model.Task;

import java.io.File;
import java.util.List;

public class PdfReportStrategy implements ReportStrategy {

    @Override
    public void generateReport(List<Task> tasks) throws Exception {
        String dest = "Reporte_Tareas.pdf";

        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Paragraph title = new Paragraph("Reporte Empresarial de Tareas")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        float[] columnWidths = {50F, 120F, 150F, 80F, 80F, 80F};
        Table table = new Table(columnWidths);

        String[] headers = {"ID (Parcial)", "Nombre", "Descripción", "Estatus", "Creación", "Vencimiento"};
        for (String header : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(header).setBold()));
        }

        for (Task task : tasks) {
            String displayId = (task.getId() != null && task.getId().length() > 5)
                    ? task.getId().substring(0, 5) + "..."
                    : "N/A";

            table.addCell(new Paragraph(displayId));
            table.addCell(new Paragraph(task.getName() != null ? task.getName() : ""));
            table.addCell(new Paragraph(task.getDescription() != null ? task.getDescription() : ""));
            table.addCell(new Paragraph(task.getStatus() != null ? task.getStatus() : ""));
            table.addCell(new Paragraph(task.getCreationDate() != null ? task.getCreationDate() : ""));
            table.addCell(new Paragraph(task.getDueDate() != null ? task.getDueDate() : ""));
        }

        document.add(table);
        document.close();

        File file = new File(dest);
        System.out.println("[SISTEMA] Reporte PDF generado exitosamente en: " + file.getAbsolutePath());
    }
}