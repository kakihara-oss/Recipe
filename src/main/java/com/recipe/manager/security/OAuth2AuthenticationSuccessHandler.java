package com.recipe.manager.security;

import com.recipe.manager.config.AppProperties;
import com.recipe.manager.entity.Role;
import com.recipe.manager.entity.User;
import com.recipe.manager.exception.ForbiddenException;
import com.recipe.manager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppProperties appProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        String email = oidcUser.getEmail();
        String googleId = oidcUser.getSubject();
        String name = oidcUser.getFullName();
        String pictureUrl = oidcUser.getPicture();

        validateDomain(email);

        User user = userRepository.findByGoogleId(googleId)
                .map(existingUser -> updateExistingUser(existingUser, name, email, pictureUrl))
                .orElseGet(() -> createNewUser(googleId, email, name, pictureUrl));

        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());

        String redirectUrl = UriComponentsBuilder.fromUriString(appProperties.getFrontendUrl() + "/oauth2/callback")
                .queryParam("token", token)
                .build().toUriString();

        log.info("OAuth2 login successful for user: {} (role: {})", email, user.getRole());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void validateDomain(String email) {
        String domain = email.substring(email.indexOf('@') + 1);
        if (!domain.equals(appProperties.getAllowedDomain())) {
            throw new ForbiddenException("Access denied: email domain " + domain + " is not allowed");
        }
    }

    private User updateExistingUser(User user, String name, String email, String pictureUrl) {
        user.setName(name);
        user.setEmail(email);
        user.setPictureUrl(pictureUrl);
        return userRepository.save(user);
    }

    private User createNewUser(String googleId, String email, String name, String pictureUrl) {
        User newUser = User.builder()
                .googleId(googleId)
                .email(email)
                .name(name)
                .pictureUrl(pictureUrl)
                .role(Role.CHEF)
                .build();
        User saved = userRepository.save(newUser);
        log.info("New user created: {} with default role CHEF", email);
        return saved;
    }
}
