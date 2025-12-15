package travel.mytravelplan.domain.place.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.place.entity.TripPlace;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("여행 장소 레포지토리 테스트")
class TripPlaceRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private TripPlaceRepository tripPlaceRepository;

    @Test
    @DisplayName("여행 장소를 저장한다")
    void saveTripPlace() {
        // given
        TripPlace tripPlace = createTripPlace("에펠탑", "프랑스 파리", "파리의 상징적인 랜드마크",
                new BigDecimal("48.8584"), new BigDecimal("2.2945"), PlaceCategory.ATTRACTION, "https://example.com/eiffel");

        // when
        TripPlace savedTripPlace = tripPlaceRepository.save(tripPlace);
        em.flush();
        em.clear();

        // then
        assertThat(savedTripPlace.getId()).isNotNull();
        assertThat(savedTripPlace.getName()).isEqualTo("에펠탑");
        assertThat(savedTripPlace.getAddress()).isEqualTo("프랑스 파리");
        assertThat(savedTripPlace.getDescription()).isEqualTo("파리의 상징적인 랜드마크");
        assertThat(savedTripPlace.getLatitude()).isEqualByComparingTo(new BigDecimal("48.8584"));
        assertThat(savedTripPlace.getLongitude()).isEqualByComparingTo(new BigDecimal("2.2945"));
        assertThat(savedTripPlace.getCategory()).isEqualTo(PlaceCategory.ATTRACTION);
        assertThat(savedTripPlace.getExternalUrl()).isEqualTo("https://example.com/eiffel");
    }

    @Test
    @DisplayName("여행 장소를 ID로 조회한다")
    void findTripPlaceById() {
        // given
        TripPlace tripPlace = createTripPlace("루브르 박물관", "프랑스 파리", "세계 최대의 박물관",
                new BigDecimal("48.8606"), new BigDecimal("2.3376"), PlaceCategory.CULTURE, "https://example.com/louvre");
        TripPlace savedTripPlace = tripPlaceRepository.save(tripPlace);
        em.flush();
        em.clear();

        // when
        TripPlace foundTripPlace = tripPlaceRepository.findById(savedTripPlace.getId()).orElse(null);

        // then
        assertThat(foundTripPlace).isNotNull();
        assertThat(foundTripPlace.getId()).isEqualTo(savedTripPlace.getId());
        assertThat(foundTripPlace.getName()).isEqualTo("루브르 박물관");
        assertThat(foundTripPlace.getCategory()).isEqualTo(PlaceCategory.CULTURE);
    }

    @Test
    @DisplayName("여행 장소를 수정한다")
    void updateTripPlace() {
        // given
        TripPlace tripPlace = createTripPlace("개선문", "프랑스 파리", "나폴레옹의 승리 기념비",
                new BigDecimal("48.8738"), new BigDecimal("2.2950"), PlaceCategory.ATTRACTION, "https://example.com/arc");
        TripPlace savedTripPlace = tripPlaceRepository.save(tripPlace);
        em.flush();
        em.clear();

        // when
        TripPlace foundTripPlace = tripPlaceRepository.findById(savedTripPlace.getId()).orElseThrow();
        foundTripPlace.update("개선문(Arc de Triomphe)", "프랑스 파리 샹젤리제", "나폴레옹의 승리를 기념하는 개선문",
                new BigDecimal("48.8738"), new BigDecimal("2.2950"), PlaceCategory.CULTURE, "https://example.com/arc-updated");
        em.flush();
        em.clear();

        // then
        TripPlace updatedTripPlace = tripPlaceRepository.findById(savedTripPlace.getId()).orElse(null);
        assertThat(updatedTripPlace).isNotNull();
        assertThat(updatedTripPlace.getName()).isEqualTo("개선문(Arc de Triomphe)");
        assertThat(updatedTripPlace.getAddress()).isEqualTo("프랑스 파리 샹젤리제");
        assertThat(updatedTripPlace.getDescription()).isEqualTo("나폴레옹의 승리를 기념하는 개선문");
        assertThat(updatedTripPlace.getCategory()).isEqualTo(PlaceCategory.CULTURE);
        assertThat(updatedTripPlace.getExternalUrl()).isEqualTo("https://example.com/arc-updated");
    }

    @Test
    @DisplayName("여행 장소를 삭제한다")
    void deleteTripPlace() {
        // given
        TripPlace tripPlace = createTripPlace("몽마르트 언덕", "프랑스 파리", "예술가들의 거리",
                new BigDecimal("48.8867"), new BigDecimal("2.3431"), PlaceCategory.CULTURE, "https://example.com/montmartre");
        TripPlace savedTripPlace = tripPlaceRepository.save(tripPlace);
        em.flush();
        em.clear();

        // when
        tripPlaceRepository.deleteById(savedTripPlace.getId());
        em.flush();
        em.clear();

        // then
        TripPlace deletedTripPlace = tripPlaceRepository.findById(savedTripPlace.getId()).orElse(null);
        assertThat(deletedTripPlace).isNull();
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 여행 장소를 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        TripPlace place1 = createAndSaveTripPlace("장소1", "주소1", "설명1",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), PlaceCategory.ATTRACTION, "https://example.com/1");
        TripPlace place2 = createAndSaveTripPlace("장소2", "주소2", "설명2",
                new BigDecimal("37.5666"), new BigDecimal("126.9781"), PlaceCategory.CAFE, "https://example.com/2");
        TripPlace place3 = createAndSaveTripPlace("장소3", "주소3", "설명3",
                new BigDecimal("37.5667"), new BigDecimal("126.9782"), PlaceCategory.RESTAURANT, "https://example.com/3");

        em.flush();
        em.clear();

        // when
        List<TripPlace> places = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(places).hasSize(3);
        assertThat(places.get(0).getName()).isEqualTo("장소3");
        assertThat(places.get(1).getName()).isEqualTo("장소2");
        assertThat(places.get(2).getName()).isEqualTo("장소1");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 여행 장소를 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        TripPlace place1 = createAndSaveTripPlace("장소A", "주소A", "설명A",
                new BigDecimal("35.1796"), new BigDecimal("129.0756"), PlaceCategory.HOTEL, "https://example.com/a");
        TripPlace place2 = createAndSaveTripPlace("장소B", "주소B", "설명B",
                new BigDecimal("35.1797"), new BigDecimal("129.0757"), PlaceCategory.SHOPPING, "https://example.com/b");
        TripPlace place3 = createAndSaveTripPlace("장소C", "주소C", "설명C",
                new BigDecimal("35.1798"), new BigDecimal("129.0758"), PlaceCategory.NATURE, "https://example.com/c");

        em.flush();
        em.clear();

        // when
        List<TripPlace> places = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(places).hasSize(3);
        assertThat(places.get(0).getName()).isEqualTo("장소A");
        assertThat(places.get(1).getName()).isEqualTo("장소B");
        assertThat(places.get(2).getName()).isEqualTo("장소C");
    }

    @Test
    @DisplayName("limit 개수만큼 여행 장소를 조회한다")
    void findAllByCursor_withLimit() {
        // given
        TripPlace place1 = createAndSaveTripPlace("서울타워", "서울 용산구", "서울의 랜드마크",
                new BigDecimal("37.5512"), new BigDecimal("126.9882"), PlaceCategory.ATTRACTION, "https://example.com/tower");
        TripPlace place2 = createAndSaveTripPlace("경복궁", "서울 종로구", "조선의 궁궐",
                new BigDecimal("37.5796"), new BigDecimal("126.9770"), PlaceCategory.CULTURE, "https://example.com/palace");
        TripPlace place3 = createAndSaveTripPlace("한강공원", "서울 영등포구", "서울의 공원",
                new BigDecimal("37.5219"), new BigDecimal("126.9389"), PlaceCategory.NATURE, "https://example.com/park");
        TripPlace place4 = createAndSaveTripPlace("명동", "서울 중구", "쇼핑 거리",
                new BigDecimal("37.5636"), new BigDecimal("126.9826"), PlaceCategory.SHOPPING, "https://example.com/myeongdong");
        TripPlace place5 = createAndSaveTripPlace("홍대", "서울 마포구", "젊음의 거리",
                new BigDecimal("37.5563"), new BigDecimal("126.9233"), PlaceCategory.NIGHTLIFE, "https://example.com/hongdae");

        em.flush();
        em.clear();

        // when
        List<TripPlace> places = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                3
        );

        // then
        assertThat(places).hasSize(3);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 여행 장소를 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        TripPlace place1 = createAndSaveTripPlace("부산 해운대", "부산 해운대구", "유명 해변",
                new BigDecimal("35.1586"), new BigDecimal("129.1603"), PlaceCategory.NATURE, "https://example.com/haeundae");
        TripPlace place2 = createAndSaveTripPlace("광안리 해수욕장", "부산 수영구", "야경이 아름다운 해변",
                new BigDecimal("35.1532"), new BigDecimal("129.1189"), PlaceCategory.NATURE, "https://example.com/gwangan");
        TripPlace place3 = createAndSaveTripPlace("감천문화마을", "부산 사하구", "예술 마을",
                new BigDecimal("35.0976"), new BigDecimal("129.0106"), PlaceCategory.CULTURE, "https://example.com/gamcheon");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlace> firstPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("감천문화마을");
        assertThat(firstPage.get(1).getName()).isEqualTo("광안리 해수욕장");

        // when - 두 번째 페이지 조회
        TripPlace lastPlace = firstPage.get(firstPage.size() - 1);
        List<TripPlace> secondPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "desc",
                lastPlace.getCreatedAt().toString(),
                lastPlace.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("부산 해운대");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 여행 장소를 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        TripPlace place1 = createAndSaveTripPlace("제주 한라산", "제주도", "제주의 상징",
                new BigDecimal("33.3617"), new BigDecimal("126.5292"), PlaceCategory.NATURE, "https://example.com/hallasan");
        TripPlace place2 = createAndSaveTripPlace("성산일출봉", "제주 서귀포시", "일출 명소",
                new BigDecimal("33.4584"), new BigDecimal("126.9426"), PlaceCategory.NATURE, "https://example.com/seongsan");
        TripPlace place3 = createAndSaveTripPlace("섭지코지", "제주 서귀포시", "아름다운 해안",
                new BigDecimal("33.4241"), new BigDecimal("126.9285"), PlaceCategory.NATURE, "https://example.com/seopjikoji");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlace> firstPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("제주 한라산");
        assertThat(firstPage.get(1).getName()).isEqualTo("성산일출봉");

        // when - 두 번째 페이지 조회
        TripPlace lastPlace = firstPage.get(firstPage.size() - 1);
        List<TripPlace> secondPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                lastPlace.getCreatedAt().toString(),
                lastPlace.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("섭지코지");
    }

    @Test
    @DisplayName("카테고리별로 여행 장소를 조회한다")
    void findTripPlacesByCategory() {
        // given
        TripPlace cafe1 = createAndSaveTripPlace("스타벅스 1호점", "서울 종로구", "유명 카페",
                new BigDecimal("37.5700"), new BigDecimal("126.9850"), PlaceCategory.CAFE, "https://example.com/cafe1");
        TripPlace cafe2 = createAndSaveTripPlace("블루보틀 서울", "서울 강남구", "고급 카페",
                new BigDecimal("37.5172"), new BigDecimal("127.0473"), PlaceCategory.CAFE, "https://example.com/cafe2");
        TripPlace restaurant = createAndSaveTripPlace("미슐랭 레스토랑", "서울 강남구", "고급 레스토랑",
                new BigDecimal("37.5173"), new BigDecimal("127.0474"), PlaceCategory.RESTAURANT, "https://example.com/restaurant");

        em.flush();
        em.clear();

        // when
        List<TripPlace> allPlaces = tripPlaceRepository.findAll();
        List<TripPlace> cafes = allPlaces.stream()
                .filter(place -> place.getCategory() == PlaceCategory.CAFE)
                .toList();

        // then
        assertThat(cafes).hasSize(2);
        assertThat(cafes)
                .extracting(TripPlace::getName)
                .containsExactlyInAnyOrder("스타벅스 1호점", "블루보틀 서울");
    }

    @Test
    @DisplayName("모든 여행 장소를 조회한다")
    void findAllTripPlaces() {
        // given
        TripPlace place1 = createAndSaveTripPlace("인천 차이나타운", "인천 중구", "중국 문화 거리",
                new BigDecimal("37.4757"), new BigDecimal("126.6177"), PlaceCategory.CULTURE, "https://example.com/chinatown");
        TripPlace place2 = createAndSaveTripPlace("수원 화성", "경기 수원시", "조선시대 성곽",
                new BigDecimal("37.2868"), new BigDecimal("127.0146"), PlaceCategory.CULTURE, "https://example.com/hwaseong");

        em.flush();
        em.clear();

        // when
        List<TripPlace> places = tripPlaceRepository.findAll();

        // then
        assertThat(places).hasSize(2);
        assertThat(places)
                .extracting(TripPlace::getName)
                .containsExactlyInAnyOrder("인천 차이나타운", "수원 화성");
    }

    @Test
    @DisplayName("커서와 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withNullCursorAndAfter() {
        // given
        createAndSaveTripPlace("장소1", "주소1", "설명1",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), PlaceCategory.ATTRACTION, "https://example.com/1");
        createAndSaveTripPlace("장소2", "주소2", "설명2",
                new BigDecimal("37.5666"), new BigDecimal("126.9781"), PlaceCategory.CAFE, "https://example.com/2");

        em.flush();
        em.clear();

        // when
        List<TripPlace> places = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(places).hasSize(2);
        assertThat(places.get(0).getName()).isEqualTo("장소1");
        assertThat(places.get(1).getName()).isEqualTo("장소2");
    }

    @Test
    @DisplayName("커서와 after가 모두 제공된 경우 다음 페이지를 조회한다 - 생성일 기준")
    void findAllByCursor_withBothCursorAndAfter_createdAt() {
        // given
        createAndSaveTripPlace("첫번째", "주소1", "설명1",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), PlaceCategory.ATTRACTION, "https://example.com/1");
        createAndSaveTripPlace("두번째", "주소2", "설명2",
                new BigDecimal("37.5666"), new BigDecimal("126.9781"), PlaceCategory.CAFE, "https://example.com/2");
        createAndSaveTripPlace("세번째", "주소3", "설명3",
                new BigDecimal("37.5667"), new BigDecimal("126.9782"), PlaceCategory.RESTAURANT, "https://example.com/3");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlace> firstPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                null,
                null,
                1
        );

        // then
        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.get(0).getName()).isEqualTo("첫번째");

        // when - 커서와 after를 모두 사용하여 두 번째 페이지 조회
        TripPlace lastPlace = firstPage.get(0);
        List<TripPlace> secondPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                lastPlace.getCreatedAt().toString(),
                lastPlace.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getName()).isEqualTo("두번째");
        assertThat(secondPage.get(1).getName()).isEqualTo("세번째");
    }

    @Test
    @DisplayName("커서와 after가 모두 제공된 경우 다음 페이지를 조회한다 - 생성일 내림차순")
    void findAllByCursor_withBothCursorAndAfter_createdAtDesc() {
        // given
        createAndSaveTripPlace("A 장소", "주소A", "설명A",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), PlaceCategory.ATTRACTION, "https://example.com/a");
        createAndSaveTripPlace("B 장소", "주소B", "설명B",
                new BigDecimal("37.5666"), new BigDecimal("126.9781"), PlaceCategory.CAFE, "https://example.com/b");
        createAndSaveTripPlace("C 장소", "주소C", "설명C",
                new BigDecimal("37.5667"), new BigDecimal("126.9782"), PlaceCategory.RESTAURANT, "https://example.com/c");
        createAndSaveTripPlace("D 장소", "주소D", "설명D",
                new BigDecimal("37.5668"), new BigDecimal("126.9783"), PlaceCategory.HOTEL, "https://example.com/d");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회 (내림차순)
        List<TripPlace> firstPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("D 장소");
        assertThat(firstPage.get(1).getName()).isEqualTo("C 장소");

        // when - 커서와 after를 모두 사용하여 두 번째 페이지 조회
        TripPlace lastPlace = firstPage.getLast();
        List<TripPlace> secondPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "desc",
                lastPlace.getCreatedAt().toString(),
                lastPlace.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(2);
        assertThat(secondPage.get(0).getName()).isEqualTo("B 장소");
        assertThat(secondPage.get(1).getName()).isEqualTo("A 장소");
    }

    @Test
    @DisplayName("cursor만 있고 after가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withCursorOnly() {
        // given
        createAndSaveTripPlace("장소X", "주소X", "설명X",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), PlaceCategory.ATTRACTION, "https://example.com/x");
        createAndSaveTripPlace("장소Y", "주소Y", "설명Y",
                new BigDecimal("37.5666"), new BigDecimal("126.9781"), PlaceCategory.CAFE, "https://example.com/y");

        em.flush();
        em.clear();

        // when - cursor만 있고 after는 null
        List<TripPlace> places = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                LocalDateTime.now().toString(),
                null,
                10
        );

        // then - cursor와 after가 모두 있어야 조건이 적용되므로 모든 데이터 조회
        assertThat(places).hasSize(2);
    }

    @Test
    @DisplayName("after만 있고 cursor가 null인 경우 첫 페이지를 조회한다")
    void findAllByCursor_withAfterOnly() {
        // given
        createAndSaveTripPlace("장소P", "주소P", "설명P",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), PlaceCategory.ATTRACTION, "https://example.com/p");
        createAndSaveTripPlace("장소Q", "주소Q", "설명Q",
                new BigDecimal("37.5666"), new BigDecimal("126.9781"), PlaceCategory.CAFE, "https://example.com/q");

        em.flush();
        em.clear();

        // when - after만 있고 cursor는 null
        List<TripPlace> places = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                null,
                999L,
                10
        );

        // then - cursor와 after가 모두 있어야 조건이 적용되므로 모든 데이터 조회
        assertThat(places).hasSize(2);
    }

    @Test
    @DisplayName("같은 생성일을 가진 장소들에서 커서 페이지네이션이 정상 동작한다 - 오름차순")
    void findAllByCursor_sameCreatedAt_asc() {
        // given
        TripPlace place1 = createAndSaveTripPlace("같은시간1", "주소1", "설명1",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), PlaceCategory.ATTRACTION, "https://example.com/1");
        TripPlace place2 = createAndSaveTripPlace("같은시간2", "주소2", "설명2",
                new BigDecimal("37.5666"), new BigDecimal("126.9781"), PlaceCategory.CAFE, "https://example.com/2");
        TripPlace place3 = createAndSaveTripPlace("같은시간3", "주소3", "설명3",
                new BigDecimal("37.5667"), new BigDecimal("126.9782"), PlaceCategory.RESTAURANT, "https://example.com/3");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<TripPlace> firstPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);

        // when - 커서와 after를 모두 사용하여 다음 페이지 조회
        TripPlace lastPlace = firstPage.getLast();
        List<TripPlace> secondPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                lastPlace.getCreatedAt().toString(),
                lastPlace.getId(),
                2
        );

        // then - ID가 더 큰 것만 조회됨
        assertThat(secondPage).isNotEmpty();
    }

    @Test
    @DisplayName("같은 생성일을 가진 장소들에서 커서 페이지네이션이 정상 동작한다 - 내림차순")
    void findAllByCursor_sameCreatedAt_desc() {
        // given
        createAndSaveTripPlace("역순1", "주소1", "설명1",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), PlaceCategory.ATTRACTION, "https://example.com/1");
        createAndSaveTripPlace("역순2", "주소2", "설명2",
                new BigDecimal("37.5666"), new BigDecimal("126.9781"), PlaceCategory.CAFE, "https://example.com/2");
        createAndSaveTripPlace("역순3", "주소3", "설명3",
                new BigDecimal("37.5667"), new BigDecimal("126.9782"), PlaceCategory.RESTAURANT, "https://example.com/3");

        em.flush();
        em.clear();

        // when - 첫 페이지 조회 (내림차순)
        List<TripPlace> firstPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);

        // when - 커서와 after를 모두 사용하여 다음 페이지 조회
        TripPlace lastPlace = firstPage.getLast();
        List<TripPlace> secondPage = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "desc",
                lastPlace.getCreatedAt().toString(),
                lastPlace.getId(),
                2
        );

        // then - ID가 더 작은 것만 조회됨
        assertThat(secondPage).isNotEmpty();
    }

    @Test
    @DisplayName("페이지네이션 중간에서 시작하는 경우 - 오름차순")
    void findAllByCursor_startFromMiddle_asc() {
        // given
        createAndSaveTripPlace("페이지1", "주소1", "설명1",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), PlaceCategory.ATTRACTION, "https://example.com/1");
        createAndSaveTripPlace("페이지2", "주소2", "설명2",
                new BigDecimal("37.5666"), new BigDecimal("126.9781"), PlaceCategory.CAFE, "https://example.com/2");
        createAndSaveTripPlace("페이지3", "주소3", "설명3",
                new BigDecimal("37.5667"), new BigDecimal("126.9782"), PlaceCategory.RESTAURANT, "https://example.com/3");
        createAndSaveTripPlace("페이지4", "주소4", "설명4",
                new BigDecimal("37.5668"), new BigDecimal("126.9783"), PlaceCategory.HOTEL, "https://example.com/4");
        createAndSaveTripPlace("페이지5", "주소5", "설명5",
                new BigDecimal("37.5669"), new BigDecimal("126.9784"), PlaceCategory.SHOPPING, "https://example.com/5");

        em.flush();
        em.clear();

        // when - 전체 조회 후 중간 요소의 커서 추출
        List<TripPlace> allPlaces = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );
        TripPlace middlePlace = allPlaces.get(2); // 세번째 요소

        // when - 중간 커서부터 조회
        List<TripPlace> remainingPlaces = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "asc",
                middlePlace.getCreatedAt().toString(),
                middlePlace.getId(),
                10
        );

        // then - 중간 요소 이후의 데이터만 조회됨
        assertThat(remainingPlaces).hasSize(2);
        assertThat(remainingPlaces.get(0).getName()).isEqualTo("페이지4");
        assertThat(remainingPlaces.get(1).getName()).isEqualTo("페이지5");
    }

    @Test
    @DisplayName("페이지네이션 중간에서 시작하는 경우 - 내림차순")
    void findAllByCursor_startFromMiddle_desc() {
        // given
        createAndSaveTripPlace("역페이지1", "주소1", "설명1",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), PlaceCategory.ATTRACTION, "https://example.com/1");
        createAndSaveTripPlace("역페이지2", "주소2", "설명2",
                new BigDecimal("37.5666"), new BigDecimal("126.9781"), PlaceCategory.CAFE, "https://example.com/2");
        createAndSaveTripPlace("역페이지3", "주소3", "설명3",
                new BigDecimal("37.5667"), new BigDecimal("126.9782"), PlaceCategory.RESTAURANT, "https://example.com/3");
        createAndSaveTripPlace("역페이지4", "주소4", "설명4",
                new BigDecimal("37.5668"), new BigDecimal("126.9783"), PlaceCategory.HOTEL, "https://example.com/4");
        createAndSaveTripPlace("역페이지5", "주소5", "설명5",
                new BigDecimal("37.5669"), new BigDecimal("126.9784"), PlaceCategory.SHOPPING, "https://example.com/5");

        em.flush();
        em.clear();

        // when - 전체 조회 후 중간 요소의 커서 추출 (내림차순)
        List<TripPlace> allPlaces = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );
        TripPlace middlePlace = allPlaces.get(2); // 세번째 요소

        // when - 중간 커서부터 조회 (내림차순)
        List<TripPlace> remainingPlaces = tripPlaceRepository.findAllByCursor(
                null,
                "createdAt",
                "desc",
                middlePlace.getCreatedAt().toString(),
                middlePlace.getId(),
                10
        );

        // then - 중간 요소 이후의 데이터만 조회됨
        assertThat(remainingPlaces).hasSize(2);
        assertThat(remainingPlaces.get(0).getName()).isEqualTo("역페이지2");
        assertThat(remainingPlaces.get(1).getName()).isEqualTo("역페이지1");
    }

    // TestFixture 메서드들
    private TripPlace createTripPlace(String name, String address, String description,
                                      BigDecimal latitude, BigDecimal longitude,
                                      PlaceCategory category, String externalUrl) {
        return TripPlace.createTripPlace(name, address, description, latitude, longitude, category, externalUrl);
    }

    private TripPlace createAndSaveTripPlace(String name, String address, String description,
                                             BigDecimal latitude, BigDecimal longitude,
                                             PlaceCategory category, String externalUrl) {
        TripPlace tripPlace = TripPlace.createTripPlace(name, address, description, latitude, longitude, category, externalUrl);
        return tripPlaceRepository.save(tripPlace);
    }
}