package pe.edu.upeu.autoandes.config;

import java.time.*;
import org.springframework.context.annotation.*;
@Configuration
public class ClockConfig {
 @Bean Clock clock() {return Clock.system(ZoneId.of("America/Lima"));}
}
