package com.project.service;

import com.project.util.Result;
import java.util.Date;

public interface ExportService {

    /**
     * Generates a PDF report of the emotion analysis.
     * @param userId The ID of the user.
     * @param startDate The start date for the analysis.
     * @param endDate The end date for the analysis.
     * @return A Result containing the byte array of the generated PDF.
     */
    Result<byte[]> exportAnalysisToPdf(Long userId, Date startDate, Date endDate);
} 