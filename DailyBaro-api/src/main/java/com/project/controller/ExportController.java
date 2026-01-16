package com.project.controller;

import com.project.service.ExportService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.Date;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    private static final Long MOCK_USER_ID = 1L;

    @GetMapping("/analysis.pdf")
    public ResponseEntity<InputStreamResource> exportAnalysisPdf(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        Result<byte[]> result = exportService.exportAnalysisToPdf(MOCK_USER_ID, startDate, endDate);

        if (result.getCode() != 200 || result.getData() == null) {
            // In a real app, you'd handle this more gracefully,
            // maybe returning a JSON error or a specific error page.
            return ResponseEntity.badRequest().build();
        }

        byte[] pdfContents = result.getData();
        ByteArrayInputStream bis = new ByteArrayInputStream(pdfContents);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=emotion-analysis.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
} 