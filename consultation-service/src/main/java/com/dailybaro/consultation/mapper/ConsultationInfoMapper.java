package com.dailybaro.consultation.mapper;

import com.dailybaro.consultation.model.ConsultationInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConsultationInfoMapper {
    List<ConsultationInfo> selectAllConsultations();
    ConsultationInfo selectById(Long consultationId);
    int insertConsultation(ConsultationInfo consultationInfo);
    int updateConsultation(ConsultationInfo consultationInfo);
    int deleteConsultation(Long consultationId);
} 