package ru.kata.spring.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.model.User;
import ru.kata.spring.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findUserById(id);
    }

    @Override
    public User findByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Fail to retrieve user: " + login));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User create(User user, String rawPassword, Integer[] selectedRoleIds) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        // roleService.findAllById ищет роли в базе, которые установил админ на страние регистрации
        user.getRoles().addAll(roleService.findAllById(List.of(selectedRoleIds)));
        return userRepository.save(user);
    }

    @Override
    public User update(User user, Integer[] selectedRoleIds) {
        user.setPassword(userRepository.findByIdIfUserExists(user.getId()).getPassword());
        // Две строки ниже заменяют роли юзера на роли, установленные админом
        user.getRoles().clear();
        user.getRoles().addAll(roleService.findAllById(List.of(selectedRoleIds)));
        return userRepository.save(user);
    }

    @Override
    public boolean delete(Long id) {
        return userRepository.findById(id)
                .map(entity -> {
                    userRepository.deleteById(id);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Fail to retrieve user: " + username));
    }
}
