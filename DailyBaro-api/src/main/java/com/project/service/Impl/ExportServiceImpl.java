package com.project.service.Impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.project.model.vo.EmotionShareVO;
import com.project.service.EmotionAnalysisService;
import com.project.service.ExportService;
import com.project.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import com.project.model.vo.EmotionDataPointVO;

import java.util.Calendar;

@Slf4j
@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private EmotionAnalysisService emotionAnalysisService;

    @Override
    public Result<byte[]> exportAnalysisToPdf(Long userId, Date startDate, Date endDate) {
        log.info("Generating PDF report for user {} from {} to {}", userId, startDate, endDate);

        // 1. Fetch data using the EmotionAnalysisService
        Result<List<EmotionShareVO>> distributionResult = emotionAnalysisService.getEmotionDistribution(userId, startDate, endDate);
        Result<List<EmotionDataPointVO>> fluctuationResult = emotionAnalysisService.getEmotionFluctuation(userId, startDate, endDate);
        if (distributionResult.getCode() != 200 || distributionResult.getData() == null) {
            return Result.fail("Could not fetch emotion data to generate PDF.");
        }
        List<EmotionShareVO> emotionData = distributionResult.getData();
        List<EmotionDataPointVO> fluctuationData = fluctuationResult.getData();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Use a font that supports Chinese characters
            PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

            // 2. Add content to the PDF
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String title = String.format("情绪分析报告 (%s to %s)", sdf.format(startDate), sdf.format(endDate));
            document.add(new Paragraph(title).setFont(font).setBold().setFontSize(20));

            // 情绪波动折线图
            if (fluctuationData != null && !fluctuationData.isEmpty()) {
                TimeSeries series = new TimeSeries("情绪值");
                for (EmotionDataPointVO point : fluctuationData) {
                    Date date = point.getDate();
                    double value = point.getValue() == null ? 0 : point.getValue();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    series.add(new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)), value);
                }
                TimeSeriesCollection dataset = new TimeSeriesCollection(series);
                JFreeChart chart = ChartFactory.createTimeSeriesChart(
                        "情绪波动", "日期", "情绪值", dataset, false, false, false);
                ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
                ChartUtils.writeChartAsPNG(chartOut, chart, 500, 300);
                ImageData imageData = ImageDataFactory.create(chartOut.toByteArray());
                Image image = new Image(imageData);
                document.add(new Paragraph("情绪波动折线图：").setFont(font).setBold());
                document.add(image);
            }

            if (emotionData.isEmpty()) {
                document.add(new Paragraph("在此期间内没有找到足够的情绪数据。").setFont(font));
            } else {
                document.add(new Paragraph("情绪分布:").setFont(font).setBold());
                for (EmotionShareVO share : emotionData) {
                    String line = String.format("%s: %.2f%%", share.getEmotion(), share.getPercentage());
                    document.add(new Paragraph(line).setFont(font));
                }
            }

            document.close();
            log.info("PDF report generated successfully for user {}", userId);
            return Result.success(baos.toByteArray());

        } catch (IOException e) {
            log.error("Error while generating PDF: {}", e.getMessage());
            return Result.fail("生成PDF时出错: " + e.getMessage());
        }
    }
} 