package dev.alan20111.todolist.reports;

import dev.alan20111.todolist.model.Task;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelReportStrategy implements ReportStrategy {

    @Override
    public void generateReport(List<Task> tasks) throws Exception {
        String dest = "Reporte_Tareas.xlsx";

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Listado de Tareas");

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID de Tarea", "Nombre", "Descripción", "Estatus", "Fecha de Creación", "Fecha de Vencimiento"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Task task : tasks) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(task.getId() != null ? task.getId() : "N/A");
            row.createCell(1).setCellValue(task.getName());
            row.createCell(2).setCellValue(task.getDescription());
            row.createCell(3).setCellValue(task.getStatus());
            row.createCell(4).setCellValue(task.getCreationDate());
            row.createCell(5).setCellValue(task.getDueDate());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(dest)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }

        File file = new File(dest);
        System.out.println("[SISTEMA] Reporte Excel generado exitosamente en: " + file.getAbsolutePath());
    }
}