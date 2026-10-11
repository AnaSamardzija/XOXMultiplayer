package rs.ac.ni.pmf.ana.XOXMultiplayer.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.DuplicateResourceException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.UserDto;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.auth.AuthResponse;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.auth.RegisterRequest;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.UserEntity;
import rs.ac.ni.pmf.ana.XOXMultiplayer.repository.UserRepository;
import rs.ac.ni.pmf.ana.XOXMultiplayer.security.CustomUserDetails;
import rs.ac.ni.pmf.ana.XOXMultiplayer.security.JwtTokenUtil;

@Service
@RequiredArgsConstructor
public class AuthService
{
	private final AuthenticationManager _authenticationManager;
	private final JwtTokenUtil _jwtTokenUtil;
	private final UserRepository _userRepository;
	private final PasswordEncoder _passwordEncoder;

	@Transactional(readOnly = true)
	public AuthResponse authenticate(final String username, final String password)
	{
		final Authentication authentication =
				_authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username.strip(), password));

		if (authentication.getPrincipal() instanceof final CustomUserDetails userDetails)
		{
			return toAuthResponse(userDetails);
		}

		throw new IllegalStateException("Invalid user authentication");
	}

	@Transactional
	public AuthResponse register(final RegisterRequest registerRequest)
	{
		final String username = registerRequest.getUsername().strip();

		if (_userRepository.existsByUsername(username))
		{
			throw new DuplicateResourceException("Korisničko ime je zauzeto");
		}

		final UserEntity userEntity = UserEntity.builder()
		                                        .username(username)
		                                        .password(_passwordEncoder.encode(registerRequest.getPassword()))
		                                        .build();

		final UserEntity saved = _userRepository.save(userEntity);

		// Novi korisnik je odmah prijavljen: dobija token kao i kod prijave
		return toAuthResponse(new CustomUserDetails(saved.getId(), saved.getUsername(), saved.getPassword()));
	}

	private AuthResponse toAuthResponse(final CustomUserDetails userDetails)
	{
		final String accessToken = _jwtTokenUtil.generateToken(null, userDetails);
		final UserDto user = UserDto.builder()
		                            .id(userDetails.getId())
		                            .username(userDetails.getUsername())
		                            .build();

		return AuthResponse.builder()
		                   .accessToken(accessToken)
		                   .user(user)
		                   .build();
	}
}
