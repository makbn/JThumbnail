package io.github.makbn.jthumbnail;

import io.github.makbn.jthumbnail.core.JThumbnailer;
import java.util.Arrays;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class JThumbnailerStarter {
    private static JThumbnailer mInstance;

    public static void main(String[] args) {
        init(args);
    }

    public static JThumbnailer init(String[] args) {
        if (mInstance == null || Arrays.stream(args).anyMatch(e -> e.contains("force_init"))) {
            SpringApplication app = new SpringApplication(JThumbnailerStarter.class);
            app.setBannerMode(Banner.Mode.OFF);
            ConfigurableApplicationContext cac = app.run(args);
            mInstance = cac.getBean(JThumbnailer.class);
        }
        return mInstance;
    }
}
