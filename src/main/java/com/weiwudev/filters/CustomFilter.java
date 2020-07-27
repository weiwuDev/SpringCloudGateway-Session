package com.weiwudev.filters;

import com.weiwudev.models.MyUserDetails;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
//@Order(Ordered.LOWEST_PRECEDENCE)
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {
    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            return exchange.getSession().flatMap(session -> isAuthorizationValid(session)).flatMap(
                    user ->
                    {
                        ServerHttpRequest modifiedRequest = createUserHeaders(exchange.getRequest(), user);
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    }
            ).switchIfEmpty(Mono.defer(() -> {
                return chain.filter(exchange);
            }));

        };
    }

    private ServerHttpRequest createUserHeaders(ServerHttpRequest request, MyUserDetails userDetails) {
        StringBuilder roles = new StringBuilder();
        String separator = "";
        for (String role : userDetails.getRoles()) {
            roles.append(separator);
            separator = ":";
            roles.append(role);
        }
        return request.mutate().header("USER_DETAILS", userDetails.getUsername())
                .header("USER_ROLES", roles.toString()).build();
    }

    private Mono<MyUserDetails> isAuthorizationValid(WebSession session) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext -> {
                    if (SecurityContext.getAuthentication().isAuthenticated()) {
                        MyUserDetails userDetails = (MyUserDetails) SecurityContext.getAuthentication().getPrincipal();
                        List<String> roles = SecurityContext.getAuthentication().getAuthorities().stream().map(r -> r.getAuthority()).collect(Collectors.toList());
                        userDetails.setRoles(roles);
                        return userDetails;
                    }
                    return new MyUserDetails("","", new ArrayList<>());
                }).switchIfEmpty(Mono.defer(() -> {
                    return Mono.just(new MyUserDetails("","", new ArrayList<>()));
                }));
    }


    public static class Config {
        // Put the configuration properties
    }
}

