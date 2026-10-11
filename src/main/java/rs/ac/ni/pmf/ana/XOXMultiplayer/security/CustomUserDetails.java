package rs.ac.ni.pmf.ana.XOXMultiplayer.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails
{
	private final Long _id;
	private final String _username;
	private final String _password;

	// Uloga i permisija nema, svi prijavljeni korisnici su obični igrači
	@Override
	@NullMarked
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		return Collections.emptySet();
	}

	@Override
	public @Nullable String getPassword()
	{
		return _password;
	}

	@Override
	@NullMarked
	public String getUsername()
	{
		return _username;
	}

	public Long getId()
	{
		return _id;
	}
}
