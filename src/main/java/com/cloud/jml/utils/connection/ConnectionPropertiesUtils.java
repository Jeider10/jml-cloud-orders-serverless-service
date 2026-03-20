package com.cloud.jml.utils.connection;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class ConnectionPropertiesUtils {

    private String defaultUrl = "jdbc:mysql://localhost:3306/application_table_db";
    private String defaultUsername = "root";
    private String defaultPassword = "root";
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
    private int serverPort = 1085;
}
