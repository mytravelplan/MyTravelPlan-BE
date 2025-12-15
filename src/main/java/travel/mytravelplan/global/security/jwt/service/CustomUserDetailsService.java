package travel.mytravelplan.global.security.jwt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travel.mytravelplan.domain.user.entity.User;
import travel.mytravelplan.domain.user.mapper.UserMapper;
import travel.mytravelplan.domain.user.repository.UserRepository;
import travel.mytravelplan.global.security.jwt.CustomUserDetails;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. username: " + username));

        return new CustomUserDetails(user);
    }
}
