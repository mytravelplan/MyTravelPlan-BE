package travel.mytravelplan;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import travel.mytravelplan.domain.category.entity.Category;
import travel.mytravelplan.domain.category.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

@Profile("local")
@Component
@Order(1)
@RequiredArgsConstructor
public class ProductCategoryInitializer implements ApplicationRunner {
    private final CategoryRepository productCategoryRepository;

    private final List<Category> allCategories = new ArrayList<>();

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 대분류
        Category fashion = createCategory("패션의류/잡화", 1, null);
        Category digital = createCategory("가전/디지털", 1, null);
        Category beauty = createCategory("뷰티", 1, null);
        Category food = createCategory("식품", 1, null);

        // 패션 - 여성의류
        Category women = createCategory("여성의류", 2, fashion);
        createCategory("원피스", 3, women);
        createCategory("티셔츠", 3, women);
        createCategory("바지", 3, women);

        // 패션 - 남성의류
        Category men = createCategory("남성의류", 2, fashion);
        createCategory("셔츠", 3, men);
        createCategory("자켓", 3, men);
        createCategory("청바지", 3, men);

        // 패션 - 신발
        Category shoes = createCategory("신발", 2, fashion);
        createCategory("스니커즈", 3, shoes);
        createCategory("구두", 3, shoes);

        // 가전/디지털 - 모바일/태블릿
        Category mobile = createCategory("모바일/태블릿", 2, digital);
        createCategory("스마트폰", 3, mobile);
        createCategory("태블릿", 3, mobile);

        // 가전/디지털 - 노트북/PC
        Category pc = createCategory("노트북/PC", 2, digital);
        createCategory("노트북", 3, pc);
        createCategory("데스크탑", 3, pc);

        // 가전/디지털 - TV/가전
        Category homeAppliances = createCategory("TV/가전", 2, digital);
        createCategory("TV", 3, homeAppliances);
        createCategory("냉장고", 3, homeAppliances);

        // 뷰티
        Category skincare = createCategory("스킨케어", 2, beauty);
        createCategory("로션", 3, skincare);
        createCategory("에센스", 3, skincare);

        Category makeup = createCategory("메이크업", 2, beauty);
        createCategory("립스틱", 3, makeup);
        createCategory("파운데이션", 3, makeup);

        Category hairbody = createCategory("헤어/바디", 2, beauty);
        createCategory("샴푸", 3, hairbody);
        createCategory("바디워시", 3, hairbody);

        // 식품
        Category fresh = createCategory("신선식품", 2, food);
        createCategory("과일", 3, fresh);
        createCategory("채소", 3, fresh);

        Category processed = createCategory("가공식품", 2, food);
        createCategory("라면", 3, processed);
        createCategory("과자", 3, processed);

        Category beverage = createCategory("음료", 2, food);
        createCategory("커피", 3, beverage);
        createCategory("주스", 3, beverage);

        productCategoryRepository.saveAll(allCategories);
    }

    private Category createCategory(String name, int depth, Category parent) {
        Category category = Category.createCategory(name, depth, parent);

        if (parent != null) {
            parent.addChildCategory(category);
        }

        allCategories.add(category);

        return category;
    }
}
