package com.cloud.jml.config.connection;

import com.cloud.jml.utils.connection.ConnectionPropertiesUtils;
import com.cloud.jml.utils.connection.ConnectionUtils;
import com.cloud.jml.utils.general.GeneralUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class DatabaseConnectionConfig {

    private final ConnectionPropertiesUtils connectionPropertiesUtils;
    private final ConnectionUtils connectionUtils;
    private final GeneralUtils generalUtils;

    public DatabaseConnectionConfig(ConnectionPropertiesUtils connectionPropertiesUtils, ConnectionUtils connectionUtils, GeneralUtils generalUtils) {
        this.connectionPropertiesUtils = connectionPropertiesUtils;
        this.connectionUtils = connectionUtils;
        this.generalUtils = generalUtils;
        log.info("🔥 DatabaseConnectionConfig inicializado correctamente.");
    }

    /**
     * 🔹 Construye la conexion a BD sin depender de application.yml.
     * Prioridad:
     * 1️⃣ Variables de entorno (DB_URL, DB_USERNAME, DB_PASSWORD)
     * 2️⃣ Valores por defecto (para desarrollo local)
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("🔹 Inicializando conexion a base de datos...");

        // 1️⃣ Resolver URL
        String url = generalUtils.getEnvOrDefault("DB_URL", connectionPropertiesUtils.getDefaultUrl());

        // 2️⃣ Resolver usuario
        String username = generalUtils.getEnvOrDefault("DB_USERNAME", connectionPropertiesUtils.getDefaultUsername());

        // 3️⃣ Resolver password
        String password = generalUtils.getEnvOrDefault("DB_PASSWORD", connectionPropertiesUtils.getDefaultPassword());

        // 4️⃣ Crear el DataSource con Hikari (pool de conexiones eficiente)
        DataSource dataSource = connectionUtils.buildDataSource(url, username, password);

        log.info("✅ Conexion a base de datos configurada correctamente: {}", url);

        return dataSource;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactoryCustomizer() {
        log.info("🌐 Configurando servidor...");

        return factory -> {
            int portEnv = generalUtils.getEnvOrDefault("SERVER_PORT", connectionPropertiesUtils.getServerPort());
            factory.setPort(portEnv);

            log.info("🌐 Servidor configurado para iniciar en el puerto: {}", portEnv);
        };
    }
}
