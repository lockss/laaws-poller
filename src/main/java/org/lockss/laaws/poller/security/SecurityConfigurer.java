package org.lockss.laaws.poller.security;

import org.lockss.rs.auth.SpringSecurityConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Custom Spring security configurator.
 * <br />
 * This class is needed because the Spring discovery process is not able to
 * find the parent class (because it is in a jar?).
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfigurer extends SpringSecurityConfigurer {
  // To use an authentication filter different than the default
  // org.lockss.rs.auth.SpringAuthenticationFilter, override the
  // configure(HttpSecurity http) method of this class.
}
