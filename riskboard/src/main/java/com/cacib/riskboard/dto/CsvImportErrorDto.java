package com.cacib.riskboard.dto;

public record CsvImportErrorDto(int lineNumber, String rawLine, String errorMessage) {
}
