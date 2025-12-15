package travel.mytravelplan.domain.category.entity;

import jakarta.persistence.*;
import lombok.*;
import travel.mytravelplan.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int depth;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Category(String name, int depth, Category parent) {
        this.name = name;
        this.depth = depth;
        this.parent = parent;
    }

    public static Category createCategory(String name, int depth, Category parent) {
        return Category.builder()
                .name(name)
                .depth(depth)
                .parent(parent)
                .build();
    }

    public void addChildCategory(Category child) {
        this.children.add(child);
        child.setParent(this);
    }

    public void update(String name) {
        this.name = name;
    }
}
