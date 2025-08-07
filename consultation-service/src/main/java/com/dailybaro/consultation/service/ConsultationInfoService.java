package com.dailybaro.consultation.service;

import com.github.pagehelper.PageInfo;
import com.dailybaro.consultation.model.ConsultationInfo;

public interface ConsultationInfoService {

    PageInfo<ConsultationInfo> getConsultationByPage(Integer pageNum, Integer pageSize);

    ConsultationInfo getConsultationById(Long consultationId);

    void addConsultation(ConsultationInfo consultationInfo);

    void updateConsultation(ConsultationInfo consultationInfo);

    void deleteConsultation(Long consultationId);
} 