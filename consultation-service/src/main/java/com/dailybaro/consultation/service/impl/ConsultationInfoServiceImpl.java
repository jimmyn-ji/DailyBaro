package com.dailybaro.consultation.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.dailybaro.consultation.mapper.ConsultationInfoMapper;
import com.dailybaro.consultation.model.ConsultationInfo;
import com.dailybaro.consultation.service.ConsultationInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultationInfoServiceImpl implements ConsultationInfoService {

    @Autowired
    private ConsultationInfoMapper consultationInfoMapper;

    @Override
    public PageInfo<ConsultationInfo> getConsultationByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ConsultationInfo> consultations = consultationInfoMapper.selectAllConsultations();
        return new PageInfo<>(consultations);
    }

    @Override
    public ConsultationInfo getConsultationById(Long consultationId) {
        return consultationInfoMapper.selectById(consultationId);
    }

    @Override
    public void addConsultation(ConsultationInfo consultationInfo) {
        consultationInfoMapper.insertConsultation(consultationInfo);
    }

    @Override
    public void updateConsultation(ConsultationInfo consultationInfo) {
        consultationInfoMapper.updateConsultation(consultationInfo);
    }

    @Override
    public void deleteConsultation(Long consultationId) {
        consultationInfoMapper.deleteConsultation(consultationId);
    }
} 