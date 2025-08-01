package org.example.javabot.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Service
public class ExcelParserService {

//    public void parseExcel(File file) {
//        try (FileInputStream fis = new FileInputStream(file);
//             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
//
//            XSSFSheet sheet = workbook.getSheetAt(0); // Первый лист
//            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm"); // Формат для времени
//
//            for (Row row : sheet) {
//                for (Cell cell : row) {
//                    switch (cell.getCellType()) {
//                        case STRING -> System.out.print(cell.getStringCellValue() + "\t");
//
//                        case NUMERIC -> {
//                            if (DateUtil.isCellDateFormatted(cell)) {
//                                String time = timeFormat.format(cell.getDateCellValue());
//                                System.out.print(time + "\t");
//                            } else {
//                                System.out.print(cell.getNumericCellValue() + "\t");
//                            }
//                        }
//
//                        case BOOLEAN -> System.out.print(cell.getBooleanCellValue() + "\t");
//
//                        default -> System.out.print("?\t");
//                    }
//                }
//                System.out.println(); // Перенос строки
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public String getScheduleByGroup(File file, String groupName) {
        StringBuilder result = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            XSSFSheet sheet = workbook.getSheet(groupName);
            if (sheet == null) {
                return "❌ Не найден лист с названием группы: " + groupName; // "❌ Не найден лист с названием группы: " + groupName
            }

            // Загружаем первую строку — это дни недели
            Row headerRow = sheet.getRow(1);
            if (headerRow == null) return "⚠️ Расписание не заполнено!";

            // Определяем день недели
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            Map<String, Integer> dayColumnMap = Map.of(
                    "Понедельник", 1,
                    "Вторник", 2,
                    "Среда", 3,
                    "Четверг", 4,
                    "Пятница", 5,
                    "Суббота", 6
            );

            String todayName = getRussianDay(today);
            Integer colIndex = dayColumnMap.get(todayName);
            if (colIndex == null) return "🎉 Сегодня выходной!";

            result.append("🗓️ Группа: ").append(groupName).append("\n");
            result.append("📅 День: == ").append(todayName).append(" ==\n\n");

            // Расписание с 3 строки (индекса 2)
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell timeCell = row.getCell(0);
                Cell lessonCell = row.getCell(colIndex);

                String time = getCellValueAsString(timeCell);
                String lesson = getCellValueAsString(lessonCell);

                if (!time.isEmpty() && !lesson.isEmpty()) {
                    result.append(time).append(" — ").append(lesson).append("\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "❗ Ошибка при чтении файла.";
        }

        return result.toString();
    }

    private String getRussianDay(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Понедельник";
            case TUESDAY -> "Вторник";
            case WEDNESDAY -> "Среда";
            case THURSDAY -> "Четверг";
            case FRIDAY -> "Пятница";
            case SATURDAY -> "Суббота";
            default -> "";
        };
    }


    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? new SimpleDateFormat("HH:mm").format(cell.getDateCellValue())
                    : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    public List<String> getGroupNames(File excelFile) {
        List<String> groupNames = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelFile);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                groupNames.add(workbook.getSheetName(i));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return groupNames;
    }


}
