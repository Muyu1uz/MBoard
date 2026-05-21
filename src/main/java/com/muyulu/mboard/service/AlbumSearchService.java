package com.muyulu.mboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyulu.mboard.entity.Album;
import com.muyulu.mboard.entity.Artist;
import com.muyulu.mboard.mapper.AlbumMapper;
import com.muyulu.mboard.mapper.ArtistMapper;
import com.muyulu.mboard.search.AlbumSearchDocument;
import com.muyulu.mboard.search.AlbumSearchRepository;
import com.muyulu.mboard.service.dto.AlbumQueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AlbumSearchService {

    private final AlbumSearchRepository albumSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final AlbumMapper albumMapper;
    private final ArtistMapper artistMapper;
    private final GenreCatalogService genreCatalogService;

    public void reindexAlbum(Album album) {
        Artist artist = artistMapper.selectById(album.getArtistId());
        AlbumSearchDocument document = AlbumSearchDocument.builder()
                .id(album.getId().toString())
                .albumId(album.getId())
                .artistId(album.getArtistId())
                .name(album.getName())
                .genres(genreCatalogService.parseCodes(album.getGenre()))
                .artistName(artist == null ? "" : artist.getName())
                .coverUrl(album.getCoverUrl())
                .summary(album.getSummary())
                .releaseDate(album.getReleaseDate())
                .trending(album.isTrending())
                .published(album.isPublished())
                .build();
        albumSearchRepository.save(document);
    }

    public void reindexAllPublishedAlbums() {
        albumMapper.selectList(new LambdaQueryWrapper<Album>()
                        .eq(Album::isPublished, true))
                .forEach(this::reindexAlbum);
    }

    public SearchAlbumPage search(AlbumQueryRequest request) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    b.must(m -> m.term(t -> t.field("published").value(true)));
                    if (request.getGenre() != null && !request.getGenre().isBlank()) {
                        String genreCode = genreCatalogService.normalizeCodes(List.of(request.getGenre())).getFirst();
                        b.must(m -> m.term(t -> t.field("genre").value(genreCode)));
                    }
                    if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                        b.must(m -> m.match(mt -> mt.field("name").query(request.getKeyword())));
                    }
                    return b;
                }))
                .withPageable(PageRequest.of((int) Math.max(0, request.getPage() - 1), (int) request.getSize()))
                .build();
        SearchHits<AlbumSearchDocument> hits = elasticsearchOperations.search(query, AlbumSearchDocument.class);
        List<Long> albumIds = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(AlbumSearchDocument::getAlbumId)
                .filter(Objects::nonNull)
                .toList();
        return new SearchAlbumPage(albumIds, hits.getTotalHits());
    }

    public record SearchAlbumPage(List<Long> albumIds, long total) {
    }
}
