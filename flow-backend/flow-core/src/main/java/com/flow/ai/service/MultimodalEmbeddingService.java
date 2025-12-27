package com.flow.ai.service;

import java.util.List;

public interface MultimodalEmbeddingService {
    /**
     * 获取文本的向量嵌入。
     * 
     * @param text      要嵌入的文本。
     * @param model     模型名称（可选）。
     * @param dimension 向量维度（可选）。
     * @return 代表向量的 Double 列表。
     */
    List<Double> getTextEmbedding(String text, String model, Integer dimension);

    /**
     * 获取图片 URL 的向量嵌入。
     * 
     * @param imageUrl  图片的 URL。
     * @param model     模型名称（可选）。
     * @param dimension 向量维度（可选）。
     * @return 代表向量的 Double 列表。
     */
    List<Double> getImageEmbedding(String imageUrl, String model, Integer dimension);

    /**
     * 获取视频 URL 的向量嵌入。
     * 
     * @param videoUrl  视频的 URL。
     * @param model     模型名称（可选）。
     * @param dimension 向量维度（可选）。
     * @return 代表向量的 Double 列表。
     */
    List<Double> getVideoEmbedding(String videoUrl, String model, Integer dimension);
}
