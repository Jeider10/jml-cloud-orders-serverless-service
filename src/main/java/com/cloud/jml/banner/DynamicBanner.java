package com.cloud.jml.banner;

import com.github.lalyos.jfiglet.FigletFont;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

public class DynamicBanner implements Banner {

    private final String microName;

    public DynamicBanner(String microName) {
        this.microName = microName;
    }

    @Override
    public void printBanner(Environment env, Class<?> sourceClass, PrintStream out) {

        String version = env.getProperty("application.version", "unknown");

        try {

            String ascii = FigletFont.convertOneLine(microName.toUpperCase());

            out.println();
            out.println(ascii);
            out.println(":: " + microName + " :: (v" + version + ")");
            out.println("Java " + System.getProperty("java.version")
                    + " | Spring Boot " + SpringBootVersion.getVersion());
            out.println();

        } catch (Exception e) {

            out.println(microName + " v" + version);

        }
    }
}
