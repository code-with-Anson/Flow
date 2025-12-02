package com.flow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileProcessingMessage implements Serializable {
    private Long fileId;
    private String userId;
    private String model;
    private String description;
}
