package com.dailybaro.consultation.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("consultation_info")
public class ConsultationInfo {
    @TableId(value = "consultation_id", type = IdType.AUTO)
    private Long consultationId;
    private String consultationContent;
    private Integer isdeleted;
} 