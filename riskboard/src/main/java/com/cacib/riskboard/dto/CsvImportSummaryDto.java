package com.cacib.riskboard.dto;

import java.util.List;

public record CsvImportSummaryDto(int successfulRows, int errorRows, List<CsvImportErrorDto> errors) {
}
