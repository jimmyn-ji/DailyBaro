package com.project.controller;

import com.github.pagehelper.PageInfo;
import com.project.model.ConsultationInfo;
import com.project.service.ConsultationInfoService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultation")
public class ConsultationInfoController {

    @Autowired
    private ConsultationInfoService consultationInfoService;

    @RequestMapping("/getConsultationByPage")
    public Result<PageInfo<ConsultationInfo>> getConsultationByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ConsultationInfo> pageInfo = consultationInfoService.getConsultationByPage(pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @RequestMapping("/{consultationId}")
    public Result<ConsultationInfo> getConsultationById(@PathVariable Long consultationId) {
        ConsultationInfo consultationInfo = consultationInfoService.getConsultationById(consultationId);
        return Result.success(consultationInfo);
    }

    @RequestMapping("/add")
    public Result<ConsultationInfo> addConsultation(@RequestBody ConsultationInfo consultationInfo) {
        consultationInfoService.addConsultation(consultationInfo);
        return Result.success(consultationInfo);
    }

    @RequestMapping("/update")
    public Result<ConsultationInfo> updateConsultation(@RequestBody ConsultationInfo consultationInfo) {
        consultationInfoService.updateConsultation(consultationInfo);
        return Result.success(consultationInfo);
    }

    @DeleteMapping("/delete/{consultationId}")
    public Result<Void> deleteConsultation(@PathVariable Long consultationId) {
        consultationInfoService.deleteConsultation(consultationId);
        return Result.success();
    }
}