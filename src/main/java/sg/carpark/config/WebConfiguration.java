package sg.carpark.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sg.carpark.ratelimit.SearchRateLimitInterceptor;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    private final SearchRateLimitInterceptor interceptor;
    public WebConfiguration(SearchRateLimitInterceptor interceptor) { this.interceptor = interceptor; }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/api/v1/carparks/nearest");
    }
}
