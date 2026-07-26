package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.CategoryDTO;
import in.bushansirgur.moneymanager.entity.CategoryEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.CategoryRepository;
import in.bushansirgur.moneymanager.demo.DemoUserData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    //save category
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())) {
            throw new RuntimeException("Category with this name already exists");
        }

        CategoryEntity newCategory = toEntity(categoryDTO, profile);
        newCategory = categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    //get categories for current user
    public List<CategoryDTO> getCategoriesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (DemoUserData.isDemoEmail(profile.getEmail())) {
            return DemoUserData.categories();
        }
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    //get categories by type for current user
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (DemoUserData.isDemoEmail(profile.getEmail())) {
            return DemoUserData.categoriesByType(type);
        }
        List<CategoryEntity> entities = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        if (entities.isEmpty()) {
            seedDefaultCategories(profile);
            entities = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        }
        return entities.stream().map(this::toDTO).toList();
    }

    private void seedDefaultCategories(ProfileEntity profile) {
        if (!categoryRepository.findByProfileId(profile.getId()).isEmpty()) {
            return;
        }
        List<CategoryEntity> defaults = List.of(
                CategoryEntity.builder().name("Food & Dining").icon("🍔").type("expense").profile(profile).build(),
                CategoryEntity.builder().name("Transport").icon("🚗").type("expense").profile(profile).build(),
                CategoryEntity.builder().name("Shopping").icon("🛍️").type("expense").profile(profile).build(),
                CategoryEntity.builder().name("Utilities").icon("💡").type("expense").profile(profile).build(),
                CategoryEntity.builder().name("Entertainment").icon("🎬").type("expense").profile(profile).build(),
                CategoryEntity.builder().name("Health").icon("🏥").type("expense").profile(profile).build(),
                CategoryEntity.builder().name("Rent").icon("🏠").type("expense").profile(profile).build(),
                CategoryEntity.builder().name("Salary").icon("💼").type("income").profile(profile).build(),
                CategoryEntity.builder().name("Freelance").icon("💻").type("income").profile(profile).build(),
                CategoryEntity.builder().name("Investments").icon("📈").type("income").profile(profile).build()
        );
        categoryRepository.saveAll(defaults);
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or not accessible"));
        existingCategory.setName(dto.getName());
        existingCategory.setIcon(dto.getIcon());
        existingCategory = categoryRepository.save(existingCategory);
        return toDTO(existingCategory);
    }

    //helper methods
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType())
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .profileId(entity.getProfile() != null ?  entity.getProfile().getId(): null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .type(entity.getType())
                .build();

    }
}
