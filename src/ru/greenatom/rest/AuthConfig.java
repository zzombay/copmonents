package ru.greenatom.rest;


import de.mpdv.jtp.security.CustomLogoutHandler;
import de.mpdv.resterrordispatcher.ErrorDispatcherBasedOnUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import ru.greenatom.components.authentication.RestAuthenticationDetails;
import ru.greenatom.components.authentication.RestAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class AuthConfig {

    @Configuration()
    @Order(3)
    public static class Config extends WebSecurityConfigurerAdapter {
        private final String wspTenantId;
        private final ErrorDispatcherBasedOnUrl errorDispatcherBasedOnUrl;

        public Config(@Value("${mpdv.wsp.tenant.id:1}") String wspTenantId, @Autowired ErrorDispatcherBasedOnUrl errorDispatcherBasedOnUrl) {
            this.wspTenantId = wspTenantId;
            this.errorDispatcherBasedOnUrl = errorDispatcherBasedOnUrl;
        }

        public void configure(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(new RestAuthenticationProvider(this.wspTenantId));
        }

        protected void configure(HttpSecurity http) throws Exception {
            (((HttpSecurity) ((ExpressionUrlAuthorizationConfigurer.AuthorizedUrl) ((http.requestMatchers()
                    .antMatchers("/api/**"))
                    .and().csrf().disable()).authorizeRequests().anyRequest()).access("hasRole('ROLE_USER')").and())
                    .httpBasic().authenticationDetailsSource(RestAuthenticationDetails::new)
                    .and())
                    .logout().addLogoutHandler(new CustomLogoutHandler()).logoutUrl("/logout")
                    .permitAll().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());


        }
    }
}
