package org.mafisher.togetherbackend.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.mafisher.togetherbackend.dto.UserDto;
import org.mafisher.togetherbackend.dto.request.LoginRequest;
import org.mafisher.togetherbackend.dto.request.RegisterRequest;
import org.mafisher.togetherbackend.email.EmailTemplateName;
import org.mafisher.togetherbackend.entity.ActivationToken;
import org.mafisher.togetherbackend.entity.Role;
import org.mafisher.togetherbackend.entity.User;
import org.mafisher.togetherbackend.handler.BusinessErrorCodes;
import org.mafisher.togetherbackend.handler.CustomException;
import org.mafisher.togetherbackend.mappers.Mapper;
import org.mafisher.togetherbackend.repository.RolesRepository;
import org.mafisher.togetherbackend.repository.UserRepository;
import org.mafisher.togetherbackend.service.ActivationTokenService;
import org.mafisher.togetherbackend.service.AuthService;
import org.mafisher.togetherbackend.service.EmailService;
import org.mafisher.togetherbackend.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Mapper<User, UserDto> mapper;
    private final RolesRepository rolesRepository;
    private final EmailService emailService;
    private final ActivationTokenService activationTokenService;

    @Value("${mailing.backend.activation-url}")
    private String activationUrl;

    @Override
    public UserDto createUser(RegisterRequest registerRequest) throws MessagingException {
        userRepository.findByEmail(registerRequest.getEmail())
                .ifPresent(email -> {
                    throw new CustomException(BusinessErrorCodes.EMAIL_IS_USED);
                });

        userRepository.findByNickName(registerRequest.getNickName())
                .ifPresent(email -> {
                    throw new CustomException(BusinessErrorCodes.NICKNAME_IS_USED);
                });

        Role role = rolesRepository.findByName("USER").orElseThrow(
                () -> new RuntimeException("Role not found"));

        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .nickName(registerRequest.getNickName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(Set.of(role))
                .accountLocked(false)
                .enable(false)
                .build();
        User savedUser = userRepository.save(user);
        activationTokenService.createActivationToken(savedUser);
        sendValidationEmail(savedUser);
        return mapper.mapTo(savedUser);
    }

    @Override
    public String verify(LoginRequest loginRequest) throws MessagingException {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CustomException(BusinessErrorCodes.BAD_CREDENTIALS));

        if(!user.isEnabled())
            throw new CustomException(BusinessErrorCodes.ACCOUNT_DISABLED);

        if(user.isAccountLocked())
            throw new CustomException(BusinessErrorCodes.ACCOUNT_LOCKED);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            String email = loginRequest.getEmail();
            return jwtService.generateToken(email);
        }
        throw new CustomException(BusinessErrorCodes.BAD_CREDENTIALS);
    }

    @Override
    public void activateUser(Long id, String token) throws MessagingException {
        User userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if(userEntity.isEnabled())
            throw new CustomException(BusinessErrorCodes.USER_IS_ENABLE);

        ActivationToken activationToken = activationTokenService.getActivationTokenByUser(userEntity);

        if(!token.equals(activationToken.getToken())) {
            throw new CustomException(BusinessErrorCodes.INVALID_TOKEN);
        }else if(activationTokenService.verifyExpiration(activationToken)){
            activationTokenService.deleteActivationToken(activationToken);
            sendValidationEmail(userEntity);
            throw new CustomException(BusinessErrorCodes.TOKEN_EXPIRED);
        }
        else{
            userEntity.setEnable(true);
            userRepository.save(userEntity);
            activationTokenService.deleteActivationToken(activationToken);
        }
    }

    @Override
    public boolean verifyToken(String token) {
        return jwtService.validateJwtToken(token);
    }

    @Override
    public UserDto findUserByEmail(String email) {
        return mapper.mapTo(userRepository.findByEmail(email).orElseThrow(
                ()->new UsernameNotFoundException("User not found")));
    }

    @Override
    public UserDto findUserById(Long id) {
        return mapper.mapTo(userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found")));
    }

    @Override
    public UserDto findUserByNickname(String nickname) {
        return mapper.mapTo(userRepository.findByNickName(nickname).orElseThrow(
                () -> new UsernameNotFoundException("User not found")));
    }

    private void sendValidationEmail(User user) throws MessagingException {
        ActivationToken activationToken = activationTokenService.getActivationTokenByUser(user);
        emailService.sendEmail(
                user.getEmail(),
                user.getFirstName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                generateURL(activationUrl, user.getId(), activationToken.getToken()),
                "Account activation"
        );
    }

    private String generateURL(String baseUrl, Long user_id, String token){
        return String.format("%s/%s/%s", baseUrl ,user_id, token);
    }
}
