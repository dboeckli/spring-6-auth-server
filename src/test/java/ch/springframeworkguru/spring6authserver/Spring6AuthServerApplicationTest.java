package ch.springframeworkguru.spring6authserver;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@Slf4j
class Spring6AuthServerApplicationTest {

    @Test
    void contextLoads() {
        log.info("Testing Spring 6 Application...");
    }
  
}
