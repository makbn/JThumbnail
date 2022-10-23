package io.github.makbn.thumbnailer;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;


@SpringBootApplication
public class JThumbnailerStarter {
    private static JThumbnailer mInstance;

    public JThumbnailerStarter(){
        // we need this for spring
    }
    public static JThumbnailer init(String[] args) {
        if (mInstance == null || Arrays.stream(args).anyMatch(e->e.contains("force_init"))) {
            SpringApplication app = new SpringApplication(JThumbnailerStarter.class);
            app.setBannerMode(Banner.Mode.OFF);
            ConfigurableApplicationContext cac = app.run(args);
            mInstance = cac.getBean(JThumbnailer.class);
        }
        return mInstance;

    }
}
