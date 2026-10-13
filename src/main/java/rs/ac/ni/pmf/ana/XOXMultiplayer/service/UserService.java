package rs.ac.ni.pmf.ana.XOXMultiplayer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.entity.UserEntity;
import rs.ac.ni.pmf.ana.XOXMultiplayer.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService
{
	private final UserRepository _userRepository;
	private final PasswordEncoder _passwordEncoder;

	// Token se ne menja i važi i dalje, pa korisnik ostaje prijavljen.
	// Pogrešna trenutna lozinka je 422, a ne 401: na 401 bi front korisnika odjavio.
	@Transactional
	public void changePassword(final Long userId, final String currentPassword, final String newPassword)
	{
		final UserEntity userEntity = _userRepository.findById(userId)
		                                             .orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen"));

		if (!_passwordEncoder.matches(currentPassword, userEntity.getPassword()))
		{
			throw new InvalidOperationException("Trenutna lozinka nije tačna");
		}

		userEntity.setPassword(_passwordEncoder.encode(newPassword));
		_userRepository.save(userEntity);
	}
}
