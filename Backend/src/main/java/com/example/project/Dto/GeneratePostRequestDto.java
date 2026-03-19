package com.example.project.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePostRequestDto {

    private String topic;
    private String targetAudience;
    private String tone;
    private String goal;
    private String length;
}
