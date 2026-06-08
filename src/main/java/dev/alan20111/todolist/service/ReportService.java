package dev.alan20111.todolist.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import dev.alan20111.todolist.model.Task;
import java.io.FileOutputStream;
import java.util.List;

public class ReportService {

    public void generatePDF(List<Task> tasks, String filePath) throws Exception {
        // Usa el Document de iText, NO el de javax.swing
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
        Paragraph title = new Paragraph("REPORTE DE TAREAS\n\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        String[] headers = {"Tarea", "Descripción", "Categorías", "Estatus", "Vencimiento", "Creada"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (Task t : tasks) {
            table.addCell(t.getName());
            table.addCell(t.getDescription());
            table.addCell(t.getCategories() != null ? String.join(", ", t.getCategories()) : "");
            table.addCell(t.getStatus());
            table.addCell(t.getDueDate());
            table.addCell(t.getCreationDate());
        }

        document.add(table);
        document.close();
    }

    public void generateExcel(List<Task> tasks, String filePath) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Mis Tareas");

        Row headerRow = sheet.createRow(0);
        String[] columns = {"Tarea", "Descripción", "Categorías", "Estatus", "Vencimiento", "Creada"};
        CellStyle headerStyle = workbook.createCellStyle();

        // CORRECCIÓN: Le decimos a Java que esta "Font" es la de Excel, no la del PDF
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Task t : tasks) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(t.getName());
            row.createCell(1).setCellValue(t.getDescription());
            row.createCell(2).setCellValue(t.getCategories() != null ? String.join(", ", t.getCategories()) : "");
            row.createCell(3).setCellValue(t.getStatus());
            row.createCell(4).setCellValue(t.getDueDate());
            row.createCell(5).setCellValue(t.getCreationDate());
        }

        for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
}