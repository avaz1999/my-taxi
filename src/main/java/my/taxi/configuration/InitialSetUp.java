package my.taxi.configuration;

import lombok.RequiredArgsConstructor;
import my.taxi.entities.user.StaffAccount;
import my.taxi.entities.user.StaffProfile;
import my.taxi.entities.user.User;
import my.taxi.entities.user.enums.Language;
import my.taxi.entities.user.enums.Role;
import my.taxi.repository.StaffAccountRepository;
import my.taxi.repository.StaffProfileRepository;
import my.taxi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by Avaz Absamatov
 * Date: 9/24/2025
 */
@Component
@RequiredArgsConstructor
public class InitialSetUp implements CommandLineRunner {
    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final StaffAccountRepository staffAccountRepository;


    @Override
    public void run(String... args) {
        if (!userRepository.existsByPhone("998999701899")) {
            User user = new User();
            user.setPhone("998999701899");
            user.setRoles(Set.of(Role.ADMIN));
            user.setLanguage(Language.EN);
            user.setActive(true);
            userRepository.save(user);

            StaffProfile staffProfile = new StaffProfile();
            staffProfile.setUser(user);
            staffProfile.setFirstName("Avaz");
            staffProfile.setLastName("Absamatov");
            staffProfileRepository.save(staffProfile);

            StaffAccount staffAccount = new StaffAccount();
            staffAccount.setUser(user);
            staffAccount.setPassword("123456");

            staffAccountRepository.save(staffAccount);
        }
    }
}
