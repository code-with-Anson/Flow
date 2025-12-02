package com.flow.repository.es;

import com.flow.model.es.MultimodalAsset;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultimodalAssetRepository extends ElasticsearchRepository<MultimodalAsset, String> {
    List<MultimodalAsset> findByUserId(String userId);
}
