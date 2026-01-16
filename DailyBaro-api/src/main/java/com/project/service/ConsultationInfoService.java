package com.project.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.mapper.ConsultationInfoMapper;
import com.project.model.ConsultationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultationInfoService {

    @Autowired
    private ConsultationInfoMapper consultationInfoMapper;

    public PageInfo<ConsultationInfo> getConsultationByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ConsultationInfo> consultations = consultationInfoMapper.selectAllConsultations();
        return new PageInfo<>(consultations);
    }

    public ConsultationInfo getConsultationById(Long consultationId) {
        return consultationInfoMapper.selectById(consultationId);
    }

    public void addConsultation(ConsultationInfo consultationInfo) {
        consultationInfoMapper.insertConsultation(consultationInfo);
    }

    public void updateConsultation(ConsultationInfo consultationInfo) {
        consultationInfoMapper.updateConsultation(consultationInfo);
    }

    public void deleteConsultation(Long consultationId) {
        consultationInfoMapper.deleteConsultation(consultationId);
    }
}