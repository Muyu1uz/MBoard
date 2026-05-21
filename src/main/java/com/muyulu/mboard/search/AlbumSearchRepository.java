package com.muyulu.mboard.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AlbumSearchRepository extends ElasticsearchRepository<AlbumSearchDocument, String> {
}
