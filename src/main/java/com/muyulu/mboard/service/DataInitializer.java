package com.muyulu.mboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyulu.mboard.entity.Album;
import com.muyulu.mboard.entity.AlbumComment;
import com.muyulu.mboard.entity.AlbumRatingAggregate;
import com.muyulu.mboard.entity.AlbumRatingUser;
import com.muyulu.mboard.entity.Artist;
import com.muyulu.mboard.entity.Song;
import com.muyulu.mboard.entity.SongRatingAggregate;
import com.muyulu.mboard.entity.SongRatingUser;
import com.muyulu.mboard.entity.User;
import com.muyulu.mboard.enums.RoleType;
import com.muyulu.mboard.mapper.AlbumCommentMapper;
import com.muyulu.mboard.mapper.AlbumMapper;
import com.muyulu.mboard.mapper.AlbumRatingAggregateMapper;
import com.muyulu.mboard.mapper.AlbumRatingUserMapper;
import com.muyulu.mboard.mapper.ArtistMapper;
import com.muyulu.mboard.mapper.SongMapper;
import com.muyulu.mboard.mapper.SongRatingAggregateMapper;
import com.muyulu.mboard.mapper.SongRatingUserMapper;
import com.muyulu.mboard.mapper.UserMapper;
import com.muyulu.mboard.search.AlbumSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Set<String> DEMO_ALBUM_NAMES = Set.of(
            "Neon Aftertaste",
            "Blue Platform",
            "Static Bloom"
    );

    private static final Set<String> DEMO_ARTIST_NAMES = Set.of(
            "Aurora Meridian",
            "Velvet Transit"
    );

    private final UserMapper userMapper;
    private final ArtistMapper artistMapper;
    private final AlbumMapper albumMapper;
    private final SongMapper songMapper;
    private final AlbumCommentMapper albumCommentMapper;
    private final AlbumRatingAggregateMapper albumRatingAggregateMapper;
    private final AlbumRatingUserMapper albumRatingUserMapper;
    private final SongRatingAggregateMapper songRatingAggregateMapper;
    private final SongRatingUserMapper songRatingUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final AlbumSearchService albumSearchService;
    private final AlbumSearchRepository albumSearchRepository;
    private final AlbumDetailCacheService albumDetailCacheService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(String... args) {
        seedUsers();
        cleanupDemoCatalog();
    }

    private void seedUsers() {
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "admin")) == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setDisplayName("MBoard Admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole(RoleType.ADMIN);
            userMapper.insert(admin);
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "demo")) == null) {
            User demo = new User();
            demo.setUsername("demo");
            demo.setDisplayName("Demo Listener");
            demo.setPasswordHash(passwordEncoder.encode("demo123"));
            demo.setRole(RoleType.USER);
            userMapper.insert(demo);
        }
    }

    private void cleanupDemoCatalog() {
        List<Album> demoAlbums = albumMapper.selectList(new LambdaQueryWrapper<Album>()
                .in(Album::getName, DEMO_ALBUM_NAMES));
        if (demoAlbums.isEmpty()) {
            cleanupDemoArtists();
            return;
        }

        List<Long> albumIds = demoAlbums.stream()
                .map(Album::getId)
                .toList();

        List<Song> demoSongs = songMapper.selectList(new LambdaQueryWrapper<Song>()
                .in(Song::getAlbumId, albumIds));
        List<Long> songIds = demoSongs.stream()
                .map(Song::getId)
                .toList();

        if (!songIds.isEmpty()) {
            songRatingUserMapper.delete(new LambdaQueryWrapper<SongRatingUser>().in(SongRatingUser::getSongId, songIds));
            songRatingAggregateMapper.delete(new LambdaQueryWrapper<SongRatingAggregate>().in(SongRatingAggregate::getSongId, songIds));
            songMapper.delete(new LambdaQueryWrapper<Song>().in(Song::getId, songIds));
            songIds.forEach(this::clearSongRedisCache);
        }

        albumCommentMapper.delete(new LambdaQueryWrapper<AlbumComment>().in(AlbumComment::getAlbumId, albumIds));
        albumRatingUserMapper.delete(new LambdaQueryWrapper<AlbumRatingUser>().in(AlbumRatingUser::getAlbumId, albumIds));
        albumRatingAggregateMapper.delete(new LambdaQueryWrapper<AlbumRatingAggregate>().in(AlbumRatingAggregate::getAlbumId, albumIds));
        albumMapper.delete(new LambdaQueryWrapper<Album>().in(Album::getId, albumIds));

        for (Album album : demoAlbums) {
            albumDetailCacheService.evict(album.getId());
            albumSearchRepository.deleteById(album.getId().toString());
            clearAlbumRedisCache(album.getId());
        }

        cleanupDemoArtists();
        albumSearchService.reindexAllPublishedAlbums();
    }

    private void cleanupDemoArtists() {
        List<Artist> artists = artistMapper.selectList(new LambdaQueryWrapper<Artist>()
                .in(Artist::getName, DEMO_ARTIST_NAMES));
        if (!artists.isEmpty()) {
            artistMapper.delete(new LambdaQueryWrapper<Artist>()
                    .in(Artist::getId, artists.stream().map(Artist::getId).toList()));
        }
    }

    private void clearAlbumRedisCache(Long albumId) {
        stringRedisTemplate.delete(List.of(
                "album:rating:user:" + albumId,
                "album:rating:agg:" + albumId
        ));
        stringRedisTemplate.opsForZSet().remove("album:hot:zset", albumId.toString());
    }

    private void clearSongRedisCache(Long songId) {
        stringRedisTemplate.delete(List.of(
                "song:rating:user:" + songId,
                "song:rating:agg:" + songId
        ));
    }
}
