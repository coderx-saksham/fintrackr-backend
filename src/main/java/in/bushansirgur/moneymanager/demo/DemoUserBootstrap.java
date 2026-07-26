package in.bushansirgur.moneymanager.demo;

import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoUserBootstrap implements ApplicationRunner {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        profileRepository.findByEmail(DemoUserData.EMAIL).ifPresentOrElse(existing -> {
            existing.setPassword(passwordEncoder.encode(DemoUserData.PASSWORD));
            existing.setIsActive(true);
            existing.setFullName(DemoUserData.FULL_NAME);
            profileRepository.save(existing);
            log.info("Demo user ready: {} / {}", DemoUserData.EMAIL, DemoUserData.PASSWORD);
        }, () -> {
            ProfileEntity demo = ProfileEntity.builder()
                    .fullName(DemoUserData.FULL_NAME)
                    .email(DemoUserData.EMAIL)
                    .password(passwordEncoder.encode(DemoUserData.PASSWORD))
                    .isActive(true)
                    .activationToken("demo-token")
                    .build();
            profileRepository.save(demo);
            log.info("Demo user created: {} / {}", DemoUserData.EMAIL, DemoUserData.PASSWORD);
        });
    }
}
