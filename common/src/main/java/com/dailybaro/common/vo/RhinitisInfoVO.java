package com.project.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class RhinitisInfoVO {
    private Long movieId;
    private String movieName;
    private Integer movieLength;
    private String moviePoster;
    private String movieArea;
    private String releaseDate;
    private Double movieBoxOffice;
    private String movieIntroduction;
    private String moviePictures;
    private List<Long> categoryIds;
}