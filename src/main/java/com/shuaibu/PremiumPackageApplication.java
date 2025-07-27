package com.shuaibu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.awt.Desktop;
import java.net.URI;

import com.shuaibu.components.MachineLockUtil;
import com.shuaibu.service.UserService;

@SpringBootApplication
@EnableAsync
public class PremiumPackageApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(PremiumPackageApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!MachineLockUtil.verifyOrStoreMachine()) {
            System.out.println("❌ This application is locked to another computer.");
            System.exit(1);
        }

        userService.createAdminUserIfNotExists();
        userService.createDeveloperUserIfNotExists();
        openBrowser("http://localhost:8080/dashboard");
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime runtime = Runtime.getRuntime();
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    runtime.exec("open " + url);
                } else {
                    runtime.exec("xdg-open " + url);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
