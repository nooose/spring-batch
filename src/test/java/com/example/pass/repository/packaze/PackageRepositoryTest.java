package com.example.pass.repository.packaze;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PackageRepositoryTest {

    @Autowired
    private PackageRepository packageRepository;

    @Test
    public void testSave() {
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디 챌린지 PT 12주");
        packageEntity.setPeriod(84);

        packageRepository.save(packageEntity);

        assertThat(packageEntity.getPackageSeq()).isNotNull();
    }

    @Test
    void findByCreatedAtAfter() {
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(1);

        PackageEntity packageEntity1 = new PackageEntity();
        packageEntity1.setPackageName("학생 전용 3개월");
        packageEntity1.setPeriod(90);
        packageRepository.save(packageEntity1);

        PackageEntity packageEntity2 = new PackageEntity();
        packageEntity2.setPackageName("학생 전용 6개월");
        packageEntity2.setPeriod(180);
        packageRepository.save(packageEntity2);

        final List<PackageEntity> packageEntities = packageRepository.findByCreatedAtAfter(dateTime, PageRequest.of(0, 1, Sort.by("packageSeq").descending()));

        assertThat(packageEntities).hasSize(1);
    }

    @Test
    void updateCountAndPeriod() {
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디프로필 이벤트 4개월");
        packageEntity.setPeriod(90);
        packageRepository.save(packageEntity);

        int updatedCount = packageRepository.updateCountAndPeriod(packageEntity.getPackageSeq(), 30, 60);
        final PackageEntity updatedPackageEntity = packageRepository.findById(packageEntity.getPackageSeq()).get();

        assertAll(
                () -> assertThat(updatedCount).isEqualTo(1),
                () -> assertThat(updatedPackageEntity.getCount()).isEqualTo(30),
                () -> assertThat(updatedPackageEntity.getPeriod()).isEqualTo(60)
        );
    }

    @Test
    void delete() {
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("제거할 이용권");
        packageEntity.setPeriod(90);
        packageRepository.save(packageEntity);

        packageRepository.deleteById(packageEntity.getPackageSeq());

        assertThat(packageRepository.findById(packageEntity.getPackageSeq())).isEmpty();
    }
}
