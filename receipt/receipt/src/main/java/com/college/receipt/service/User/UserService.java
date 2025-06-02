package com.college.receipt.service.User;

import com.college.receipt.DTO.DietDto;
import com.college.receipt.DTO.SimplifiedRecipeDto;
import com.college.receipt.DTO.SimplifiedUserDto;
import com.college.receipt.entities.*;
import com.college.receipt.repositories.*;
import com.college.receipt.DTO.UserDto;
import com.college.receipt.exceptions.UserAlreadyExistException;
import com.college.receipt.service.JWTService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    public static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private DietRepository dietRepository;

    public SimplifiedUserDto getSimplifiedUser() {
        User user = findAuthenticatedUser();
        List<Recipe> recipes = recipeRepository.findByCreatedBy(user);
        List<SimplifiedRecipeDto> recipeSummaries = recipes.stream()
                .map(r -> {
                    String avatar = r.getPhotos().stream()
                            .filter(UploadedFile::isPhotoFood)
                            .map(UploadedFile::getFilePath)
                            .findFirst()
                            .orElse(null);

                    return new SimplifiedRecipeDto(
                            r.getId(),
                            r.getName(),
                            r.getDescription(),
                            avatar
                    );
                })
                .collect(Collectors.toList());
        List<Rating> ratings = ratingRepository.findByUser(user);
        List<Diet> diets = dietRepository.findByUser(user);
        List<DietDto> dietDtos = diets.stream()
                .map(d -> {
                            return new DietDto(
                                    d.getId(),
                                    d.getRecommendation(),
                                    d.getTerm(),
                                    d.getDateOfCreation()
                            );
                        }
                ).toList();
        String role = user.getRoles().toString();
        SimplifiedUserDto response = new SimplifiedUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                recipeSummaries,
                ratings,
                dietDtos,
                role
        );
        logger.info("Запрос на получение пользователя {}", user.getUsername());
        return response;
    }

    private Set<Role> getRolesFromNames(List<String> roleNames) {
        return new HashSet<>(roleRepository.findByNameIn(roleNames));
    }

    public User registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException {
        if (emailExists(userDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: "
                    + userDto.getEmail());
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setRoles(getRolesFromNames(Arrays.asList("USER")));
        logger.info("Пользователь {} успешно зарегистрировался c ролью {}", user.getUsername(), user.getRoles());

        return userRepository.save(user);
    }

    public String verify(User user) throws AuthenticationException {
        try {
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

            if (authentication.isAuthenticated()) {
                return jwtService.generateToken(user.getEmail());
            }
        }catch (AuthenticationException e){
            throw new RuntimeException("Ошибка. Пользователь не вошёл в аккаунт: " + e.getMessage());
        }
        throw new RuntimeException("Ошибка. Пользователь не вошёл в аккаунт");
    }

    public User findAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()){
            return userRepository.findByEmail(authentication.getName());
        }
        else {
            throw new RuntimeException("Пользователь не вошёл в аккаунт");
        }
    }


    private boolean emailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }
}
