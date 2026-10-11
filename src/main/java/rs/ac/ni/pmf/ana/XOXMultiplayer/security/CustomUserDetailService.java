package rs.ac.ni.pmf.ana.XOXMultiplayer.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.UserEntity;
import rs.ac.ni.pmf.ana.XOXMultiplayer.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService
{
	private final UserRepository _userRepository;

	@Override
	@NonNull
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(final @NonNull String username) throws UsernameNotFoundException
	{
		final UserEntity userEntity = _userRepository
				.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronađen: " + username));

		return new CustomUserDetails(
				userEntity.getId(),
				userEntity.getUsername(),
				userEntity.getPassword());
	}
}
