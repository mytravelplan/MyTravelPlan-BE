package travel.mytravelplan.domain.place.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import travel.mytravelplan.domain.place.entity.CustomPlace;
import travel.mytravelplan.domain.place.enums.PlaceCategory;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.enums.Gender;
import travel.mytravelplan.domain.user.enums.Role;
import travel.mytravelplan.domain.user.enums.SocialType;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.support.RepositoryTestSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("나만의 장소 레포지토리 테스트")
class CustomPlaceRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private CustomPlaceRepository customPlaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("나만의 장소를 저장한다")
    void saveCustomPlace() {
        // given
        User user = createUser("testUser", "test@email.com");
        CustomPlace customPlace = createCustomPlace(
                "내가 좋아하는 카페",
                "서울시 강남구",
                "분위기 좋은 카페",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                PlaceCategory.CAFE,
                user
        );

        // when
        CustomPlace savedPlace = customPlaceRepository.save(customPlace);
        em.flush();
        em.clear();

        // then
        assertThat(savedPlace.getId()).isNotNull();
        assertThat(savedPlace.getName()).isEqualTo("내가 좋아하는 카페");
        assertThat(savedPlace.getAddress()).isEqualTo("서울시 강남구");
        assertThat(savedPlace.getDescription()).isEqualTo("분위기 좋은 카페");
        assertThat(savedPlace.getCategory()).isEqualTo(PlaceCategory.CAFE);
    }

    @Test
    @DisplayName("나만의 장소를 ID로 조회한다")
    void findCustomPlaceById() {
        // given
        User user = createUser("testUser", "test@email.com");
        CustomPlace customPlace = createCustomPlace(
                "맛집",
                "서울시 마포구",
                "자주 가는 맛집",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                PlaceCategory.RESTAURANT,
                user
        );
        CustomPlace savedPlace = customPlaceRepository.save(customPlace);
        em.flush();
        em.clear();

        // when
        CustomPlace foundPlace = customPlaceRepository.findById(savedPlace.getId()).orElse(null);

        // then
        assertThat(foundPlace).isNotNull();
        assertThat(foundPlace.getId()).isEqualTo(savedPlace.getId());
        assertThat(foundPlace.getName()).isEqualTo("맛집");
        assertThat(foundPlace.getCategory()).isEqualTo(PlaceCategory.RESTAURANT);
    }

    @Test
    @DisplayName("나만의 장소를 수정한다")
    void updateCustomPlace() {
        // given
        User user = createUser("testUser", "test@email.com");
        CustomPlace customPlace = createCustomPlace(
                "원래 장소",
                "서울시 종로구",
                "설명",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                PlaceCategory.OTHER,
                user
        );
        CustomPlace savedPlace = customPlaceRepository.save(customPlace);
        em.flush();
        em.clear();

        // when
        CustomPlace foundPlace = customPlaceRepository.findById(savedPlace.getId()).orElseThrow();
        foundPlace.update(
                "수정된 장소",
                "서울시 강남구",
                "수정된 설명",
                new BigDecimal("37.4979"),
                new BigDecimal("127.0276"),
                PlaceCategory.HOTEL
        );
        em.flush();
        em.clear();

        // then
        CustomPlace updatedPlace = customPlaceRepository.findById(savedPlace.getId()).orElse(null);
        assertThat(updatedPlace).isNotNull();
        assertThat(updatedPlace.getName()).isEqualTo("수정된 장소");
        assertThat(updatedPlace.getAddress()).isEqualTo("서울시 강남구");
        assertThat(updatedPlace.getDescription()).isEqualTo("수정된 설명");
        assertThat(updatedPlace.getCategory()).isEqualTo(PlaceCategory.HOTEL);
    }

    @Test
    @DisplayName("나만의 장소를 삭제한다")
    void deleteCustomPlace() {
        // given
        User user = createUser("testUser", "test@email.com");
        CustomPlace customPlace = createCustomPlace(
                "삭제할 장소",
                "서울시 송파구",
                "삭제될 장소",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                PlaceCategory.SHOPPING,
                user
        );
        CustomPlace savedPlace = customPlaceRepository.save(customPlace);
        em.flush();
        em.clear();

        // when
        customPlaceRepository.deleteById(savedPlace.getId());
        em.flush();
        em.clear();

        // then
        CustomPlace deletedPlace = customPlaceRepository.findById(savedPlace.getId()).orElse(null);
        assertThat(deletedPlace).isNull();
    }

    @Test
    @DisplayName("사용자 이름으로 나만의 장소를 조회한다")
    void findAllByCursor_byUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        createAndSaveCustomPlace("장소1", "주소1", "설명1", PlaceCategory.CAFE, user1);
        createAndSaveCustomPlace("장소2", "주소2", "설명2", PlaceCategory.RESTAURANT, user1);
        createAndSaveCustomPlace("장소3", "주소3", "설명3", PlaceCategory.HOTEL, user2);

        em.flush();
        em.clear();

        // when
        List<CustomPlace> places = customPlaceRepository.findAllByCursor(
                "user1",
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(places).hasSize(2);
        assertThat(places)
                .extracting(CustomPlace::getName)
                .containsExactlyInAnyOrder("장소1", "장소2");
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 나만의 장소를 조회한다")
    void findAllByCursor_orderByCreatedAtDesc() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveCustomPlace("장소1", "주소1", "설명1", PlaceCategory.CAFE, user);
        createAndSaveCustomPlace("장소2", "주소2", "설명2", PlaceCategory.RESTAURANT, user);
        createAndSaveCustomPlace("장소3", "주소3", "설명3", PlaceCategory.HOTEL, user);

        em.flush();
        em.clear();

        // when
        List<CustomPlace> places = customPlaceRepository.findAllByCursor(
                "testUser",
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
    @DisplayName("생성일 기준 오름차순으로 나만의 장소를 조회한다")
    void findAllByCursor_orderByCreatedAtAsc() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveCustomPlace("장소1", "주소1", "설명1", PlaceCategory.CAFE, user);
        createAndSaveCustomPlace("장소2", "주소2", "설명2", PlaceCategory.RESTAURANT, user);
        createAndSaveCustomPlace("장소3", "주소3", "설명3", PlaceCategory.HOTEL, user);

        em.flush();
        em.clear();

        // when
        List<CustomPlace> places = customPlaceRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "asc",
                null,
                null,
                10
        );

        // then
        assertThat(places).hasSize(3);
        assertThat(places.get(0).getName()).isEqualTo("장소1");
        assertThat(places.get(1).getName()).isEqualTo("장소2");
        assertThat(places.get(2).getName()).isEqualTo("장소3");
    }

    @Test
    @DisplayName("limit 개수만큼 나만의 장소를 조회한다")
    void findAllByCursor_withLimit() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveCustomPlace("장소1", "주소1", "설명1", PlaceCategory.CAFE, user);
        createAndSaveCustomPlace("장소2", "주소2", "설명2", PlaceCategory.RESTAURANT, user);
        createAndSaveCustomPlace("장소3", "주소3", "설명3", PlaceCategory.HOTEL, user);
        createAndSaveCustomPlace("장소4", "주소4", "설명4", PlaceCategory.ATTRACTION, user);
        createAndSaveCustomPlace("장소5", "주소5", "설명5", PlaceCategory.SHOPPING, user);

        em.flush();
        em.clear();

        // when
        List<CustomPlace> places = customPlaceRepository.findAllByCursor(
                "testUser",
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
    @DisplayName("커서 기반 페이지네이션으로 나만의 장소를 조회한다 - 내림차순")
    void findAllByCursor_withCursor_desc() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveCustomPlace("장소1", "주소1", "설명1", PlaceCategory.CAFE, user);
        createAndSaveCustomPlace("장소2", "주소2", "설명2", PlaceCategory.RESTAURANT, user);
        createAndSaveCustomPlace("장소3", "주소3", "설명3", PlaceCategory.HOTEL, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<CustomPlace> firstPage = customPlaceRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("장소3");
        assertThat(firstPage.get(1).getName()).isEqualTo("장소2");

        // when - 두 번째 페이지 조회
        CustomPlace lastPlace = firstPage.getLast();
        List<CustomPlace> secondPage = customPlaceRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                lastPlace.getCreatedAt().toString(),
                lastPlace.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getName()).isEqualTo("장소1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 나만의 장소를 조회한다 - 오름차순")
    void findAllByCursor_withCursor_asc() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveCustomPlace("장소1", "주소1", "설명1", PlaceCategory.CAFE, user);
        createAndSaveCustomPlace("장소2", "주소2", "설명2", PlaceCategory.RESTAURANT, user);
        createAndSaveCustomPlace("장소3", "주소3", "설명3", PlaceCategory.HOTEL, user);

        em.flush();
        em.clear();

        // when - 첫 페이지 조회
        List<CustomPlace> firstPage = customPlaceRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "asc",
                null,
                null,
                2
        );

        // then
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("장소1");
        assertThat(firstPage.get(1).getName()).isEqualTo("장소2");

        // when - 두 번째 페이지 조회
        CustomPlace lastPlace = firstPage.getLast();
        List<CustomPlace> secondPage = customPlaceRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "asc",
                lastPlace.getCreatedAt().toString(),
                lastPlace.getId(),
                2
        );

        // then
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getName()).isEqualTo("장소3");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 나만의 장소를 조회하면 빈 리스트를 반환한다")
    void findAllByCursor_nonExistentUser() {
        // given
        User user = createUser("testUser", "test@email.com");
        createAndSaveCustomPlace("장소1", "주소1", "설명1", PlaceCategory.CAFE, user);

        em.flush();
        em.clear();

        // when
        List<CustomPlace> places = customPlaceRepository.findAllByCursor(
                "nonExistentUser",
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(places).isEmpty();
    }

    @Test
    @DisplayName("username이 null일 때 모든 나만의 장소를 조회한다")
    void findAllByCursor_nullUsername() {
        // given
        User user1 = createUser("user1", "user1@email.com");
        User user2 = createUser("user2", "user2@email.com");

        createAndSaveCustomPlace("장소1", "주소1", "설명1", PlaceCategory.CAFE, user1);
        createAndSaveCustomPlace("장소2", "주소2", "설명2", PlaceCategory.RESTAURANT, user2);

        em.flush();
        em.clear();

        // when
        List<CustomPlace> places = customPlaceRepository.findAllByCursor(
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(places).hasSize(2);
    }

    @Test
    @DisplayName("동일한 생성일을 가진 장소들을 ID로 정렬하여 조회한다")
    void findAllByCursor_sameCreatedAt() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveCustomPlace("장소1", "주소1", "설명1", PlaceCategory.CAFE, user);
        createAndSaveCustomPlace("장소2", "주소2", "설명2", PlaceCategory.RESTAURANT, user);
        createAndSaveCustomPlace("장소3", "주소3", "설명3", PlaceCategory.HOTEL, user);

        em.flush();
        em.clear();

        // when
        List<CustomPlace> places = customPlaceRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(places).hasSize(3);
        // ID 순서가 보장되어야 함
        for (int i = 0; i < places.size() - 1; i++) {
            assertThat(places.get(i).getId()).isGreaterThan(places.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("존재하지 않는 ID로 장소를 조회하면 빈 Optional을 반환한다")
    void findCustomPlaceById_notFound() {
        // when
        CustomPlace foundPlace = customPlaceRepository.findById(999999L).orElse(null);

        // then
        assertThat(foundPlace).isNull();
    }

    @Test
    @DisplayName("다양한 카테고리의 장소를 저장하고 조회한다")
    void saveCustomPlaces_withDifferentCategories() {
        // given
        User user = createUser("testUser", "test@email.com");

        createAndSaveCustomPlace("카페", "주소1", "설명1", PlaceCategory.CAFE, user);
        createAndSaveCustomPlace("음식점", "주소2", "설명2", PlaceCategory.RESTAURANT, user);
        createAndSaveCustomPlace("호텔", "주소3", "설명3", PlaceCategory.HOTEL, user);
        createAndSaveCustomPlace("관광명소", "주소4", "설명4", PlaceCategory.ATTRACTION, user);
        createAndSaveCustomPlace("쇼핑", "주소5", "설명5", PlaceCategory.SHOPPING, user);
        createAndSaveCustomPlace("자연", "주소6", "설명6", PlaceCategory.NATURE, user);

        em.flush();
        em.clear();

        // when
        List<CustomPlace> places = customPlaceRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                null,
                null,
                10
        );

        // then
        assertThat(places).hasSize(6);
        assertThat(places)
                .extracting(CustomPlace::getCategory)
                .containsExactlyInAnyOrder(
                        PlaceCategory.CAFE,
                        PlaceCategory.RESTAURANT,
                        PlaceCategory.HOTEL,
                        PlaceCategory.ATTRACTION,
                        PlaceCategory.SHOPPING,
                        PlaceCategory.NATURE
                );
    }

    @Test
    @DisplayName("장소 이름만 수정한다")
    void updateCustomPlace_nameOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        CustomPlace customPlace = createCustomPlace(
                "원래 이름",
                "서울시 강남구",
                "설명",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                PlaceCategory.CAFE,
                user
        );
        CustomPlace savedPlace = customPlaceRepository.save(customPlace);
        em.flush();
        em.clear();

        // when
        CustomPlace foundPlace = customPlaceRepository.findById(savedPlace.getId()).orElseThrow();
        foundPlace.update(
                "수정된 이름",
                foundPlace.getAddress(),
                foundPlace.getDescription(),
                foundPlace.getLatitude(),
                foundPlace.getLongitude(),
                foundPlace.getCategory()
        );
        em.flush();
        em.clear();

        // then
        CustomPlace updatedPlace = customPlaceRepository.findById(savedPlace.getId()).orElse(null);
        assertThat(updatedPlace).isNotNull();
        assertThat(updatedPlace.getName()).isEqualTo("수정된 이름");
        assertThat(updatedPlace.getAddress()).isEqualTo("서울시 강남구");
    }

    @Test
    @DisplayName("장소 카테고리만 수정한다")
    void updateCustomPlace_categoryOnly() {
        // given
        User user = createUser("testUser", "test@email.com");
        CustomPlace customPlace = createCustomPlace(
                "장소 이름",
                "서울시 강남구",
                "설명",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                PlaceCategory.CAFE,
                user
        );
        CustomPlace savedPlace = customPlaceRepository.save(customPlace);
        em.flush();
        em.clear();

        // when
        CustomPlace foundPlace = customPlaceRepository.findById(savedPlace.getId()).orElseThrow();
        foundPlace.update(
                foundPlace.getName(),
                foundPlace.getAddress(),
                foundPlace.getDescription(),
                foundPlace.getLatitude(),
                foundPlace.getLongitude(),
                PlaceCategory.RESTAURANT
        );
        em.flush();
        em.clear();

        // then
        CustomPlace updatedPlace = customPlaceRepository.findById(savedPlace.getId()).orElse(null);
        assertThat(updatedPlace).isNotNull();
        assertThat(updatedPlace.getCategory()).isEqualTo(PlaceCategory.RESTAURANT);
        assertThat(updatedPlace.getName()).isEqualTo("장소 이름");
    }

    @Test
    @DisplayName("커서가 있지만 after가 null인 경우 정상적으로 조회한다")
    void findAllByCursor_cursorWithoutAfter() {
        // given
        User user = createUser("testUser", "test@email.com");

        CustomPlace place1 = createAndSaveCustomPlace("장소1", "주소1", "설명1", PlaceCategory.CAFE, user);
        CustomPlace place2 = createAndSaveCustomPlace("장소2", "주소2", "설명2", PlaceCategory.RESTAURANT, user);

        em.flush();
        em.clear();

        // when
        List<CustomPlace> places = customPlaceRepository.findAllByCursor(
                "testUser",
                null,
                "createdAt",
                "desc",
                place2.getCreatedAt().toString(),
                null,
                10
        );

        // then - cursor만 있고 after가 없으면 커서 조건이 적용되지 않음
        assertThat(places).hasSize(2);
    }

    // TestFixture 메서드들
    private User createUser(String username, String email) {
        User user = User.createUser(
                username,
                "password123",
                email,
                SocialType.LOCAL,
                null,
                LocalDate.of(1990, 1, 1),
                "010-1234-5678",
                Gender.MALE,
                Set.of(Role.USER)
        );
        return userRepository.save(user);
    }

    private CustomPlace createCustomPlace(String name, String address, String description,
                                          BigDecimal latitude, BigDecimal longitude,
                                          PlaceCategory category, User user) {
        return CustomPlace.createCustomPlace(
                name,
                address,
                description,
                latitude,
                longitude,
                category,
                user
        );
    }

    private CustomPlace createAndSaveCustomPlace(String name, String address, String description,
                                                 PlaceCategory category, User user) {
        CustomPlace customPlace = createCustomPlace(
                name,
                address,
                description,
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                category,
                user
        );
        return customPlaceRepository.save(customPlace);
    }
}