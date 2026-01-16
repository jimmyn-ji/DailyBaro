package com.project.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@TableName("consultation_info")
public class ConsultationInfo {
    @Id
    private Long consultationId;
    private String consultationContent;
    private Integer isdeleted;
}